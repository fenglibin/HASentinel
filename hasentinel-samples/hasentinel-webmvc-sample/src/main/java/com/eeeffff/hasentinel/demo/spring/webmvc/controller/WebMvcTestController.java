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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.eeeffff.hasentinel.demo.spring.webmvc.vo.Result;
import com.eeeffff.hasentinel.demo.spring.webmvc.vo.TestObj;

import lombok.extern.slf4j.Slf4j;

/**
 * Test controller
 * 
 * @author fenglibin
 */
@Slf4j
@RestController
public class WebMvcTestController {
	/*
	 * @Autowired private TestBean testBean;
	 */

	@Value("${TEST_KEY:#{null}}")
	private String testKey;

	@GetMapping("/hello")
	public String apiHello(HttpServletRequest request) {
		int sleepTime = 100;
		String sleepTimeStr = request.getParameter("sleepTime");
		if (sleepTimeStr != null) {
			sleepTime = Integer.parseInt(sleepTimeStr);
		}
		doBusiness(sleepTime);
		return "Hello! test key is:" + testKey;
	}

	@GetMapping("/helloNoWait")
	public String helloNoWait(HttpServletRequest request) {
		return "Hello! test key is:" + testKey;
	}

	@GetMapping("/helloRemote")
	public Result helloRemote(HttpServletRequest request) {
		String testValue = (String) request.getParameter("testKey");
		log.info("Get the request parameter testKey:" + testValue);
		return Result.builder().result("Hello! test key is:" + testValue).build();
	}

	@PostMapping("/helloRemote2")
	public Result helloRemote2(HttpServletRequest request) {
		String body = postReceive(request);
		JSONObject jsonObject = JSONObject.parseObject(body);
		String testValue = (String) jsonObject.get("testKey");
		log.info("Get the request parameter testKey:" + testValue);
		return Result.builder().result("Hello! test key is:" + testValue).build();
	}

	/**
	 * post请求解析Json格式
	 * 
	 * @param request 接口地址
	 * @return
	 */
	private String postReceive(HttpServletRequest request) {
		try {
			ByteArrayOutputStream inBuffer = new ByteArrayOutputStream();
			InputStream input = request.getInputStream();
			byte[] tmp = new byte[1024];
			int len = 0;
			while ((len = input.read(tmp)) > 0) {
				inBuffer.write(tmp, 0, len);
			}
			byte[] buffer = inBuffer.toByteArray();
			String requestJsonStr = new String(buffer, "UTF-8");
			return requestJsonStr;
		} catch (Exception e) {
			return null;
		}
	}
	
	@PostMapping("/helloRemote3")
	public Result helloRemote3(@RequestBody TestObj testObj) {
		String testValue = testObj.getTestKey();
		log.info("Get the request parameter testKey:" + testValue);
		return Result.builder().result("Hello! test key is:" + testValue).build();
	}
	
	@PostMapping("/helloRemote4")
	public Result helloRemote4(@RequestBody String testKey) {
		log.info("Get the request parameter testKey:" + testKey);
		return Result.builder().result("Hello! test key is:" + testKey).build();
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
