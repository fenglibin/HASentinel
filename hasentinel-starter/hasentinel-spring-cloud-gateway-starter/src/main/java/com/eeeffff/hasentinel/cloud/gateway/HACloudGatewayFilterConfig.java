package com.eeeffff.hasentinel.cloud.gateway;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.web.reactive.result.view.ViewResolver;
import org.springframework.web.server.WebFilter;

import com.alibaba.csp.sentinel.adapter.gateway.sc.exception.SentinelGatewayBlockExceptionHandler;
import com.eeeffff.hasentinel.cloud.gateway.filter.HASentinelGatewayFilter;
import com.eeeffff.hasentinel.cloud.gateway.filter.HASentinelGatewayWebFilter;
import com.eeeffff.hasentinel.common.config.HASentinelConfig;

import lombok.extern.slf4j.Slf4j;

/**
 * Spring Gateway配置中心配置加载初使化类
 * 
 * @author fenglibin
 * @date 2021年8月2日 下午2:30:52
 *
 */
@Slf4j
@Configuration
public class HACloudGatewayFilterConfig extends HASentinelConfig {
	private final List<ViewResolver> viewResolvers;
	private final ServerCodecConfigurer serverCodecConfigurer;

	public HACloudGatewayFilterConfig(ObjectProvider<List<ViewResolver>> viewResolversProvider, ServerCodecConfigurer serverCodecConfigurer) {
		this.viewResolvers = viewResolversProvider.getIfAvailable(Collections::emptyList);
		this.serverCodecConfigurer = serverCodecConfigurer;
	}

	@Bean
	@Order(-1)
	public SentinelGatewayBlockExceptionHandler sentinelGatewayBlockExceptionHandler() {
		log.info("Register the block exception handler for Spring Cloud Gateway.");
		return new SentinelGatewayBlockExceptionHandler(viewResolvers, serverCodecConfigurer);
	}

	@Bean
	@Order(-1)
	public GlobalFilter sentinelGatewayFilter() {
		log.info("Register the sentinel gateway filter");
		return new HASentinelGatewayFilter();
	}

	/**
	 * 还有问题待解决
	 * @return
	 */
	//@Bean
	@Order(0)
	public WebFilter webFilter() {
		log.info("Register the sentinel gateway web filter");
		return new HASentinelGatewayWebFilter();
	}
	
    
}
