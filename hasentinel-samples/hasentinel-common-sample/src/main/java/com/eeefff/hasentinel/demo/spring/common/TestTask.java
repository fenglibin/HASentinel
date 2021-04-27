package com.eeefff.hasentinel.demo.spring.common;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.eeefff.hasentinel.demo.spring.common.service.TestFunc;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class TestTask {
	@Autowired
	private TestFunc testFunc;
	@PostConstruct
	public void init() {
		new Thread() {
			public void run() {
				startTask();
			}
		}.start();
		
	}

	void startTask() {
		while(true) {
			try {
				testFunc.writeNow();
			}catch(Exception e) {
				log.error(e.getMessage(),e);
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
		}
	}
}
