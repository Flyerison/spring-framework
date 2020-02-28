package com.flyerison.spring;

import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;


public class XmlContextApplication {

	public static void main(String[] args) {
		/**
		 * 扫描类
		 * 实例化bean
		 */
		// AnnotationConfigApplicationContext annotationConfigApplicationContext =
		// 		new AnnotationConfigApplicationContext(AppConfig.class);

		/**
		 * ClassPathXmlApplicationContext 也是一个上下文环境的实现，更多用于xml环境下
		 * AnnotationConfigApplicationContext 则是用于注解
		 */
		// System.out.println(annotationConfigApplicationContext.getBean(Test2Service.class));

		/**
		 * 这一块主要是针对比较老旧的xml进行配置bean的功能记录
		 * 1. 资源加载：根据classPath的方式加载Application.xml配置文件
		 * 2. bean工厂：继续采用Spring默认的DefaultListableBeanFactory
		 * 3. 解析器配置
		 * 4. 解析配置
		 */
		// ClassPathResource resource = new ClassPathResource("Application.xml"); // <1>
		// DefaultListableBeanFactory factory = new DefaultListableBeanFactory(); // <2>
		// XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(factory); // <3>
		// reader.loadBeanDefinitions(resource); // <4>

		// 注释说明 基于xml的IoC容器的初始化
		ClassPathXmlApplicationContext xmlApplicationContext =
				new ClassPathXmlApplicationContext("Application.xml");

	}
}
