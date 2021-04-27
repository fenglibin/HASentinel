package com.eeefff.hasentinel.demo.spring.common.service;

import org.springframework.stereotype.Service;

import com.alibaba.csp.sentinel.annotation.SentinelResource;

@Service
public class TestFunc {
	@SentinelResource(value = "testFunc-writeNow", blockHandler = "handleException", blockHandlerClass = { ExceptionUtil.class })
	public void writeNow() {
		System.out.println(System.currentTimeMillis());
	}
}
