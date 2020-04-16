package org.springframework.aop.aspectj;

public class TestBean {

	private int age;

	public void setAge(int age) {
		System.out.println("setAge");
		this.age = age;
	}

	public int getAge() {
		return age;
	}
}
