/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.dashboard.config;

import javax.annotation.PostConstruct;
import javax.servlet.Filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.alibaba.csp.sentinel.adapter.servlet.CommonFilter;
import com.alibaba.csp.sentinel.adapter.servlet.callback.WebCallbackManager;
import com.alibaba.csp.sentinel.dashboard.auth.AuthorizationInterceptor;
import com.alibaba.csp.sentinel.dashboard.auth.LoginAuthenticationFilter;
import com.alibaba.csp.sentinel.dashboard.uniqueid.IdGenerator;
import com.alibaba.csp.sentinel.dashboard.uniqueid.SnowflakeIdGenerator;
import com.alibaba.csp.sentinel.util.StringUtil;

/**
 * @author leyou
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

	private final Logger logger = LoggerFactory.getLogger(WebConfig.class);

	@Autowired
	private LoginAuthenticationFilter loginAuthenticationFilter;

	@Autowired
	private AuthorizationInterceptor authorizationInterceptor;

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(authorizationInterceptor).addPathPatterns("/**");
	}

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/**").addResourceLocations("classpath:/resources/");
	}

	@Override
	public void addViewControllers(ViewControllerRegistry registry) {
		registry.addViewController("/").setViewName("forward:/index.htm");
	}

	/**
	 * Add {@link CommonFilter} to the server, this is the simplest way to use
	 * Sentinel for Web application.
	 */
	@Bean
	public FilterRegistrationBean sentinelFilterRegistration() {
		FilterRegistrationBean<Filter> registration = new FilterRegistrationBean<>();
		registration.setFilter(new CommonFilter());
		registration.addUrlPatterns("/*");
		registration.setName("sentinelFilter");
		registration.setOrder(1);

		logger.info("Sentinel servlet CommonFilter registered");

		return registration;
	}

	@PostConstruct
	public void doInit() {
		// Example: register a UrlCleaner to exclude URLs of common static resources.
		WebCallbackManager.setUrlCleaner(url -> {
			if (StringUtil.isEmpty(url)) {
				return url;
			}
			if (url.endsWith(".js") || url.endsWith(".css") || url.endsWith("html")) {
				return null;
			}
			return url;
		});
	}

	@Bean
	public FilterRegistrationBean authenticationFilterRegistration() {
		FilterRegistrationBean<Filter> registration = new FilterRegistrationBean<>();
		registration.setFilter(loginAuthenticationFilter);
		registration.addUrlPatterns("/*");
		registration.setName("authenticationFilter");
		registration.setOrder(0);
		return registration;
	}

	/**
	 * snowflake global unique id generator
	 *
	 * @param dataCenterId data center id
	 * @param workerId     worker id
	 * @return id generator
	 */
	@Bean
	public IdGenerator<Long> snowflakeIdGenerator(@Value("${id.dataCenterId:0}") long dataCenterId,
			@Value("${id.workerId:0}") long workerId) {
		return new SnowflakeIdGenerator(dataCenterId, workerId);
	}
}
