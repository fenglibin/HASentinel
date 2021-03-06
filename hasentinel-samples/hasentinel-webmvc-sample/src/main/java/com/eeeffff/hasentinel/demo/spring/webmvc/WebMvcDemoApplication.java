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
package com.eeeffff.hasentinel.demo.spring.webmvc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.eeeffff.hasentinel.demo.spring.webmvc.handler.WhiteAppRequestOriginParser;
import com.eeeffff.hasentinel.web.WebConfigManager;

/**
 * <p>
 * 启动时增加如下JVM参数:
 * </p>
 * {@code -Dcsp.sentinel.dashboard.server=127.0.0.1:8080 -Dproject.name=hasentinel-webmvc-sample}
 *
 * @author fenglibin
 */
@SpringBootApplication
public class WebMvcDemoApplication {

	public static void main(String[] args) {
		//WebConfigManager.setUrlCleaner(new TestUrlCleaner());
		WebConfigManager.setOriginParser(new WhiteAppRequestOriginParser());
		SpringApplication.run(WebMvcDemoApplication.class);
	}
}
