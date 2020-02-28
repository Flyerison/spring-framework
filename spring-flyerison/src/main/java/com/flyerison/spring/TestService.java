package com.flyerison.spring;

import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Data
// @Component
public class TestService {

	private String name;

	private Test2Service test2Service;

	public TestService() {
		System.out.println("Instance TestService.class");
	}


}
