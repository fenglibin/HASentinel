package com.eeefff.hasentinel.demo.spring.boot.provider;

import org.springframework.beans.factory.annotation.Value;

public class TestBean {
	@Value("${test.key:#{null}}")
	private String testKey;

	public String getTestKey() {
		return testKey;
	}

	public void setTestKey(String testKey) {
		this.testKey = testKey;
	}
	
}
