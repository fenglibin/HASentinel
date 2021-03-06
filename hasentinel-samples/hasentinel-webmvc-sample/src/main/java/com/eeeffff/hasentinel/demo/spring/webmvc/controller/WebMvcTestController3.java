/*
 * Copyright 1999-2019 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.eeeffff.hasentinel.demo.spring.webmvc.controller;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Test controller
 * 
 * @author fenglibin
 */
@Controller
@RequestMapping("/test3")
public class WebMvcTestController3 {

	@GetMapping("/hello")
	public String apiHello(HttpServletRequest request) {
		int sleepTime = 100;
		String sleepTimeStr = request.getParameter("sleepTime");
		if (sleepTimeStr != null) {
			sleepTime = Integer.parseInt(sleepTimeStr);
		}
		doBusiness(sleepTime);
		return "Hello!";
	}

	@GetMapping("/hello2")
	public String apiHello2(String str) {
		doBusiness();
		return "Hello!";
	}

	@GetMapping("/hello3")
	public String apiHello3(String str1, String str2) {
		doBusiness();
		return "Hello!";
	}

	@GetMapping("/hello4/{id}")
	public String apiHello4(String id, String str1, String str2) {
		doBusiness();
		return "Hello!";
	}

	@GetMapping("/err")
	public String apiError() {
		doBusiness();
		return "Oops...";
	}
	@ResponseBody
	@GetMapping("/foo/{id}")
	public String apiFoo(@PathVariable("id") Long id) {
		doBusiness();
		return "Hello " + id;
	}

	@GetMapping("/exclude/{id}")
	public String apiExclude(@PathVariable("id") Long id) {
		doBusiness();
		return "Exclude " + id;
	}
	@GetMapping("/{hello}/isit.json")
	public String apiExclude2(@PathVariable("hello") String hello) {
		doBusiness();
		return "Exclude " + hello;
	}

	private void doBusiness(int sleepTime) {
		try {
			TimeUnit.MILLISECONDS.sleep(sleepTime);
		} catch (InterruptedException e) {
		}
	}

	private void doBusiness() {
		Random random = new Random(1);
		try {
			TimeUnit.MILLISECONDS.sleep(random.nextInt(100));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
