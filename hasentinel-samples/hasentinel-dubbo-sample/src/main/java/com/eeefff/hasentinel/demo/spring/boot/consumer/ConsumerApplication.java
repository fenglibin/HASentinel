/*
 *   Licensed to the Apache Software Foundation (ASF) under one or more
 *   contributor license agreements.  See the NOTICE file distributed with
 *   this work for additional information regarding copyright ownership.
 *   The ASF licenses this file to You under the Apache License, Version 2.0
 *   (the "License"); you may not use this file except in compliance with
 *   the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.eeefff.hasentinel.demo.spring.boot.consumer;

import org.example.HelloService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Service;

import com.eeefff.hasentinel.dubbo.HADubboConfiguration;

@SpringBootApplication(exclude= {HADubboConfiguration.class})
@Service
public class ConsumerApplication {

	@Autowired
	private HelloService helloService;

	public static void main(String[] args) throws InterruptedException {

		ConfigurableApplicationContext context = SpringApplication.run(ConsumerApplication.class, args);
		ConsumerApplication application = context.getBean(ConsumerApplication.class);
		// server 端的hystrix会抛出异常，本地的调用结果会走 fallbackMethod
		// 证明server 和 client端都经过 hystrix处理
		for (int i = 0; i < 1000000; i++) {
			try {
			String result = application.doSayHello("world");
			System.err.println("result :" + result);
			Thread.sleep(10);
			}catch(Exception e) {}
		}
	}

	public String doSayHello(String name) {
		return helloService.sayHello(name);
	}

	public String reliable(String name) {
		return "hystrix fallback value";
	}
}
