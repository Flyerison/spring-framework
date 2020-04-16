package com.flyerison.spring;

public class TestBeanImpl implements TestBean {

	private int age;

	public void setAge(int age) {
		this.age = age;
	}

	public int getAge() {
		System.out.println("getAge");
		return age;
	}
}
