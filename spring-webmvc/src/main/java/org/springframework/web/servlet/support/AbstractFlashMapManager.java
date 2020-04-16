/*
 * Copyright 2002-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.web.servlet.support;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.FlashMapManager;
import org.springframework.web.util.UrlPathHelper;

/**
 * A base class for {@link FlashMapManager} implementations.
 *
 * @author Rossen Stoyanchev
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @since 3.1.1
 */
public abstract class AbstractFlashMapManager implements FlashMapManager {

	private static final Object DEFAULT_FLASH_MAPS_MUTEX = new Object();


	protected final Log logger = LogFactory.getLog(getClass());

	private int flashMapTimeout = 180;

	private UrlPathHelper urlPathHelper = new UrlPathHelper();


	/**
	 * Set the amount of time in seconds after a {@link FlashMap} is saved
	 * (at request completion) and before it expires.
	 * <p>The default value is 180 seconds.
	 */
	public void setFlashMapTimeout(int flashMapTimeout) {
		this.flashMapTimeout = flashMapTimeout;
	}

	/**
	 * Return the amount of time in seconds before a FlashMap expires.
	 */
	public int getFlashMapTimeout() {
		return this.flashMapTimeout;
	}

	/**
	 * Set the UrlPathHelper to use to match FlashMap instances to requests.
	 */
	public void setUrlPathHelper(UrlPathHelper urlPathHelper) {
		Assert.notNull(urlPathHelper, "UrlPathHelper must not be null");
		this.urlPathHelper = urlPathHelper;
	}

	/**
	 * Return the UrlPathHelper implementation to use.
	 */
	public UrlPathHelper getUrlPathHelper() {
		return this.urlPathHelper;
	}


	@Override
	@Nullable
	// 从当前 session 对应的 FlashMap 集合获取上次请求的 FlashMap 作为本次请求的输入 FlashMap
	// 并从 session 对应的FlashMap 中移除获取到的 FlashMap 与过期的 FlashMap
	public final FlashMap retrieveAndUpdate(HttpServletRequest request, HttpServletResponse response) {
		// 通过请求关联的 session 获取 FlashMap 集合
		List<FlashMap> allFlashMaps = retrieveFlashMaps(request);
		// 如果是空的话 表示上次请求没有输出 直接返回就好
		if (CollectionUtils.isEmpty(allFlashMaps)) {
			return null;
		}

		// 每个 FlashMap 都有过期时间
		// 在返回重定向响应后 如果客户端未对重定向作出响应 将会导致重定向后的请求无法接收 导致重定向前请求的输出 FlashMap 无法清空 导致内存泄露
		List<FlashMap> mapsToRemove = getExpiredFlashMaps(allFlashMaps);
		// 根据路径获取匹配的一个
		FlashMap match = getMatchingFlashMap(allFlashMaps, request);
		// 找到了 就放到删除列表中
		if (match != null) {
			mapsToRemove.add(match);
		}

		// 删除列表不为空的话
		if (!mapsToRemove.isEmpty()) {
			// 获取 session 的操作锁
			Object mutex = getFlashMapsMutex(request);
			if (mutex != null) {
				synchronized (mutex) {
					// 为了线程安全 再获取一次
					allFlashMaps = retrieveFlashMaps(request);
					if (allFlashMaps != null) {
						// 把过期的和找到的那一个 删除掉
						allFlashMaps.removeAll(mapsToRemove);
						// 更新下状态
						updateFlashMaps(allFlashMaps, request, response);
					}
				}
			}
			else {
				allFlashMaps.removeAll(mapsToRemove);
				updateFlashMaps(allFlashMaps, request, response);
			}
		}
		// 返回匹配到的 FlashMap
		return match;
	}

	/**
	 * Return a list of expired FlashMap instances contained in the given list.
	 */
	private List<FlashMap> getExpiredFlashMaps(List<FlashMap> allMaps) {
		List<FlashMap> result = new LinkedList<>();
		for (FlashMap map : allMaps) {
			if (map.isExpired()) {
				result.add(map);
			}
		}
		return result;
	}

	/**
	 * Return a FlashMap contained in the given list that matches the request.
	 * @return a matching FlashMap or {@code null}
	 */
	@Nullable
	private FlashMap getMatchingFlashMap(List<FlashMap> allMaps, HttpServletRequest request) {
		List<FlashMap> result = new LinkedList<>();
		for (FlashMap flashMap : allMaps) {
			if (isFlashMapForRequest(flashMap, request)) {
				result.add(flashMap);
			}
		}
		if (!result.isEmpty()) {
			Collections.sort(result);
			if (logger.isTraceEnabled()) {
				logger.trace("Found " + result.get(0));
			}
			return result.get(0);
		}
		return null;
	}

	/**
	 * Whether the given FlashMap matches the current request.
	 * Uses the expected request path and query parameters saved in the FlashMap.
	 */
	protected boolean isFlashMapForRequest(FlashMap flashMap, HttpServletRequest request) {
		String expectedPath = flashMap.getTargetRequestPath();
		if (expectedPath != null) {
			String requestUri = getUrlPathHelper().getOriginatingRequestUri(request);
			if (!requestUri.equals(expectedPath) && !requestUri.equals(expectedPath + "/")) {
				return false;
			}
		}
		MultiValueMap<String, String> actualParams = getOriginatingRequestParams(request);
		MultiValueMap<String, String> expectedParams = flashMap.getTargetRequestParams();
		for (Map.Entry<String, List<String>> entry : expectedParams.entrySet()) {
			List<String> actualValues = actualParams.get(entry.getKey());
			if (actualValues == null) {
				return false;
			}
			for (String expectedValue : entry.getValue()) {
				if (!actualValues.contains(expectedValue)) {
					return false;
				}
			}
		}
		return true;
	}

	private MultiValueMap<String, String> getOriginatingRequestParams(HttpServletRequest request) {
		String query = getUrlPathHelper().getOriginatingQueryString(request);
		return ServletUriComponentsBuilder.fromPath("/").query(query).build().getQueryParams();
	}

	// 保存本次请求输出的 FlashMap 到本 session 对应的 FlashMap 集合
	@Override
	public final void saveOutputFlashMap(FlashMap flashMap, HttpServletRequest request, HttpServletResponse response) {
		if (CollectionUtils.isEmpty(flashMap)) {
			return;
		}

		// 对本次请求返回的重定向响应对应的重定向地址进行标准化
		// 重定向请求要根据路径进行匹配 匹配成功才能作为这个请求的输入 FlashMap 使用
		String path = decodeAndNormalizePath(flashMap.getTargetRequestPath(), request);
		flashMap.setTargetRequestPath(path);
		// 设置下过期时间
		flashMap.startExpirationPeriod(getFlashMapTimeout());

		// 还是获取锁
		Object mutex = getFlashMapsMutex(request);
		if (mutex != null) {
			synchronized (mutex) {
				List<FlashMap> allFlashMaps = retrieveFlashMaps(request);
				allFlashMaps = (allFlashMaps != null ? allFlashMaps : new CopyOnWriteArrayList<>());
				allFlashMaps.add(flashMap);
				updateFlashMaps(allFlashMaps, request, response);
			}
		}
		else {
			List<FlashMap> allFlashMaps = retrieveFlashMaps(request);
			allFlashMaps = (allFlashMaps != null ? allFlashMaps : new LinkedList<>());
			allFlashMaps.add(flashMap);
			updateFlashMaps(allFlashMaps, request, response);
		}
	}

	@Nullable
	private String decodeAndNormalizePath(@Nullable String path, HttpServletRequest request) {
		if (path != null && !path.isEmpty()) {
			path = getUrlPathHelper().decodeRequestString(request, path);
			if (path.charAt(0) != '/') {
				String requestUri = getUrlPathHelper().getRequestUri(request);
				path = requestUri.substring(0, requestUri.lastIndexOf('/') + 1) + path;
				path = StringUtils.cleanPath(path);
			}
		}
		return path;
	}

	/**
	 * Retrieve saved FlashMap instances from the underlying storage.
	 * @param request the current request
	 * @return a List with FlashMap instances, or {@code null} if none found
	 */
	@Nullable
	protected abstract List<FlashMap> retrieveFlashMaps(HttpServletRequest request);

	/**
	 * Update the FlashMap instances in the underlying storage.
	 * @param flashMaps a (potentially empty) list of FlashMap instances to save
	 * @param request the current request
	 * @param response the current response
	 */
	protected abstract void updateFlashMaps(
			List<FlashMap> flashMaps, HttpServletRequest request, HttpServletResponse response);

	/**
	 * Obtain a mutex for modifying the FlashMap List as handled by
	 * {@link #retrieveFlashMaps} and {@link #updateFlashMaps},
	 * <p>The default implementation returns a shared static mutex.
	 * Subclasses are encouraged to return a more specific mutex, or
	 * {@code null} to indicate that no synchronization is necessary.
	 * @param request the current request
	 * @return the mutex to use (may be {@code null} if none applicable)
	 * @since 4.0.3
	 */
	@Nullable
	protected Object getFlashMapsMutex(HttpServletRequest request) {
		return DEFAULT_FLASH_MAPS_MUTEX;
	}

}
