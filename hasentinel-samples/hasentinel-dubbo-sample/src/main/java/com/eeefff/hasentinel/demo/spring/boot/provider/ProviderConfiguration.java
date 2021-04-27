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
package com.eeefff.hasentinel.demo.spring.boot.provider;

import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.ConfigCenterConfig;
import org.apache.dubbo.config.MonitorConfig;
import org.apache.dubbo.config.ProtocolConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Eric Zhao
 */
@Configuration
public class ProviderConfiguration {
    
    /* <dubbo:application name="boot-user-service-provider"></dubbo:application> */
    @Bean
    public ApplicationConfig applicationConfig() {
        ApplicationConfig applicationConfig = new ApplicationConfig();
        applicationConfig.setName("demo-provider");
        return applicationConfig;
    }
    
    /* <dubbo:registry protocol="zookeeper" address="127.0.0.1:2181"></dubbo:registry> */
    @Bean
    public RegistryConfig registryConfig() {
        RegistryConfig registryConfig = new RegistryConfig();
        registryConfig.setProtocol("zookeeper");
        registryConfig.setAddress("127.0.0.1:2181");
        return registryConfig;
    }
    
    /* <dubbo:config-center address="zookeeper://127.0.0.1:2181"/> */
    /* 为了兼容2.6.x版本配置，在使用Zookeeper作为注册中心，且没有显示配置配置中心的情况下，Dubbo框架会默认将此Zookeeper用作配置中心，但将只作服务治理用途。*/
    /* 如果不配置ConfigCenter，是在Dubbo admin中看不到元数据，并且在Dubbo Admin中会展示警告信息：无元数据信息，请升级至Dubbo2.7及以上版本，或者查看application.properties中关于config center的配置 */
    @Bean
    public ConfigCenterConfig configCenterConfig() {
    	ConfigCenterConfig configCenterConfig = new ConfigCenterConfig();
    	configCenterConfig.setProtocol("zookeeper");
    	configCenterConfig.setAddress("127.0.0.1:2181");
    	return configCenterConfig;
    }
    
    /* <dubbo:protocol name="dubbo" port="20882"></dubbo:protocol> */
    @Bean
    public ProtocolConfig protocolConfig() {
        ProtocolConfig protocolConfig = new ProtocolConfig();
        protocolConfig.setName("dubbo");
        protocolConfig.setPort(20883);
        return protocolConfig;
    }
    
    @Bean
    public MonitorConfig monitorConfig() {
    	MonitorConfig monitorConfig = new MonitorConfig();
    	monitorConfig.setProtocol("dubbo");
    	monitorConfig.setAddress("127.0.0.1:20888");
    	return monitorConfig;
    }
    
    @Bean
    public TestBean testBean() {
    	return new TestBean();
    }
}
