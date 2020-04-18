package com.flyerison.spring;

public class TestBeanImpl implements TestBean {

	public TestBeanImpl() {
		System.out.println("初始化TestBeanImpl");
	}

	private int age;

	public void setAge(int age) {
		this.age = age;
	}

	public int getAge() {
		System.out.println("getAge");
		return age;
	}
}
