package com.flyerison.spring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

// @Component
public class Test2Service {
	private TestService testService;

	public void setTestService(TestService testService) {
		this.testService = testService;
	}

	public TestService getTestService() {
		return testService;
	}
}
