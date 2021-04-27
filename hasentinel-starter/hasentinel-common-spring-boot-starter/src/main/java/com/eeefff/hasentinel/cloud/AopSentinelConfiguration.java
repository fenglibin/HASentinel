package com.eeefff.hasentinel.cloud;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.alibaba.csp.sentinel.annotation.aspectj.SentinelResourceAspect;
import com.eeefff.hasentinel.common.config.HASentinelConfig;
@Configuration
public class AopSentinelConfiguration extends HASentinelConfig{
	// 注解支持的配置Bean
	@Bean
    public SentinelResourceAspect sentinelResourceAspect() {
        return new SentinelResourceAspect();
    }
}
