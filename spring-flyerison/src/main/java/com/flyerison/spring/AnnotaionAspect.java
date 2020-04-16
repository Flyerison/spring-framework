package com.flyerison.spring;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

public class AnnotaionAspect {

	public void after() {
		System.out.println("after");
	}


	public void before() {
		System.out.println("before");
	}

	public void afterReturn() {
		System.out.println("afterReturn");
	}

	public void afterThrow() {
		System.out.println("afterThrow");
	}
}
