package com.flyerison.spring;

import net.sf.cglib.core.DebuggingClassWriter;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class JDKTest {


	public static void main(String[] args) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
		/**
		 * 有两个参数 一个是将TestBean加载进内存的类加载器 一个是他们共同实现的接口类
		 */
		Class<?> proxyClass = Proxy.getProxyClass(TestBean.class.getClassLoader(), TestBean.class);

		// 获取一个参数为 InvocationHandler 类型的构造器
		Constructor constructor = proxyClass.getConstructor(InvocationHandler.class);

		// 反射创建实例
		TestBean bean = (TestBean) constructor.newInstance(new InvocationHandler() {
			@Override
			public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
				TestBean bean = new TestBeanImpl();
				bean.setAge(1);
				Object result = method.invoke(bean, args);
				return result;
			}
		});

		System.out.println(bean.getAge());

		// 代理类class文件存入本地磁盘方便我们反编译查看源码
		System.setProperty(DebuggingClassWriter.DEBUG_LOCATION_PROPERTY, "/Users/mc/IdeaProjects/springCode/spring-framework/spring-flyerison/src/main/java");
		// 通过CGLIB动态代理获取代理对象的过程
		Enhancer enhancer = new Enhancer();
		// 设置enhancer对象的父类
		enhancer.setSuperclass(TestBeanImpl.class);
		// 设置enhancer的回调对象
		enhancer.setCallback(new MethodInterceptor() {
			@Override
			public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {

				System.out.println("======插入前置通知======");
				Object object = proxy.invokeSuper(obj, args);
				System.out.println("======插入后者通知======");
				return object;
			}
		});
		// 创建代理对象
		TestBeanImpl proxy= (TestBeanImpl)enhancer.create();
		// 通过代理对象调用目标方法
		proxy.getAge();
	}
}
