// package com.flyerison.spring;
//
// import org.springframework.beans.BeansException;
// import org.springframework.beans.factory.config.BeanDefinition;
// import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
// import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
// import org.springframework.beans.factory.support.GenericBeanDefinition;
// import org.springframework.beans.factory.support.RootBeanDefinition;
// import org.springframework.stereotype.Component;
//
// // @Component
// public class DiyBeanFactoryPostPorcess implements BeanFactoryPostProcessor {
//
// 	@Override
// 	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
// 		System.out.println("执行自定义beanFactory后置处理器");
// 		GenericBeanDefinition genericBeanDefinition = (GenericBeanDefinition) beanFactory.getBeanDefinition("testService");
// 		System.out.println("干预之前的class " + genericBeanDefinition.getBeanClass());
// 		genericBeanDefinition.setBeanClass(Test2Service.class);
// 		System.out.println("干预之后的class" + genericBeanDefinition.getBeanClass());
//
// 	}
// }
