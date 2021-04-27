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
package com.eeefff.hasentinel.dubbo;

import javax.annotation.PostConstruct;

import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.ConsumerConfig;
import org.apache.dubbo.config.ProtocolConfig;
import org.apache.dubbo.config.ProviderConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.alibaba.csp.sentinel.adapter.dubbo.fallback.DubboFallbackRegistry;
import com.alibaba.csp.sentinel.util.AppNameUtil;
import com.eeefff.hasentinel.common.config.HASentineConfigProperties;
import com.eeefff.hasentinel.common.config.HASentinelConfig;
import com.eeefff.hasentinel.dubbo.exception.HADubboConsumerFallback;
import com.eeefff.hasentinel.dubbo.exception.HADubboProviderFallback;

import lombok.extern.slf4j.Slf4j;

/**
 * Dubbo自动装配类
 * 
 * @author fenglibin
 */
@Configuration
@EnableDubbo
@Slf4j
public class HADubboConfiguration extends HASentinelConfig {

	@Autowired
	private HASentineConfigProperties sentineConfigProperties;

	/*
	@Value("${dubbo.port:-1}")
	private int dubboPort;
	@Value("${dubbo.protocol.port:-1}")
	private int dubboProtocolPort;
	*/

	/* <dubbo:application name="boot-user-service-provider"></dubbo:application> */
	/*
	@Bean
	public ApplicationConfig applicationConfig() {
		ApplicationConfig applicationConfig = new ApplicationConfig();
		applicationConfig.setName(AppNameUtil.getAppName() + "-provider");
		return applicationConfig;
	}
	*/

	/*
	 * <dubbo:registry protocol="zookeeper"
	 * address="127.0.0.1:2181"></dubbo:registry>
	 */
	/*
	@Bean
	public RegistryConfig registryConfig() {
		RegistryConfig registryConfig = new RegistryConfig();
		registryConfig.setProtocol("zookeeper");
		registryConfig.setAddress(sentineConfigProperties.getZookeeperAddress());
		return registryConfig;
	}
	*/

	/* <dubbo:protocol name="dubbo" port="20882"></dubbo:protocol> */
	/*
	@Bean
	public ProtocolConfig protocolConfig() {
		int port = -1;
		if (dubboProtocolPort > -1) {
			port = dubboProtocolPort;
		} else if (dubboPort > -1) {
			port = dubboPort;
		}
		if (port == -1) {
			throw new RuntimeException(
					"Dubbo应用的端口没有设置，需要在application.properties增加dubbo.protocol.prot或dubbo.port并设置端口号");
		}
		log.info("Dubbo service is starting, the port is:" + port);
		ProtocolConfig protocolConfig = new ProtocolConfig();
		protocolConfig.setName("dubbo");
		protocolConfig.setPort(port);
		return protocolConfig;
	}
	*/

	@Bean
	public ProviderConfig providerConfig() {
		ProviderConfig config = new ProviderConfig();
		return config;
	}

	@Bean
	public ConsumerConfig consumerConfig() {
		ConsumerConfig config = new ConsumerConfig();
		/*
		 * 关闭 Sentinel 对应的 Service Consumer Filter * /* <dubbo:consumer
		 * filter="-sentinel.dubbo.consumer.filter"/>
		 * 即禁用类：com.alibaba.csp.sentinel.adapter.dubbo.SentinelDubboConsumerFilter
		 */
		if(!sentineConfigProperties.isEnableConsumerFilter()) {
			log.info("Disable SentinelDubboConsumerFilter");
			config.setFilter("-sentinel.dubbo.consumer.filter");
		}
		return config;
	}

	@PostConstruct
	public void doInit() {
		// 设置Dubbo请求被Block掉的异常处理逻辑
		DubboFallbackRegistry.setConsumerFallback(new HADubboConsumerFallback());
		DubboFallbackRegistry.setProviderFallback(new HADubboProviderFallback());
	}

}
