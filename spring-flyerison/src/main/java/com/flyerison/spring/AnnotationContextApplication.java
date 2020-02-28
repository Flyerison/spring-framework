package com.flyerison.spring;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class AnnotationContextApplication {

	public static void main(String[] args) {

		AnnotationConfigApplicationContext annotationConfigApplicationContext =
				new AnnotationConfigApplicationContext();
		annotationConfigApplicationContext.register(TestService.class);
		annotationConfigApplicationContext.register(Test2Service.class);

		annotationConfigApplicationContext.refresh();

		System.out.println(annotationConfigApplicationContext.getBean(TestService.class));
	}
}
