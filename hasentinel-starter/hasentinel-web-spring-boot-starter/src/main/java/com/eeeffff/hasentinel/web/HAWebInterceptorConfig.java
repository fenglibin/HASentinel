package com.eeeffff.hasentinel.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.alibaba.csp.sentinel.adapter.spring.webmvc.config.SentinelWebMvcConfig;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.eeeffff.hasentinel.common.config.HASentineConfigProperties;
import com.eeeffff.hasentinel.common.config.HASentinelConfig;
import com.eeeffff.hasentinel.common.intercepter.HASentinelWebInterceptor;
import com.eeeffff.hasentinel.common.vo.config.ResultWrapper;
import com.eeeffff.hasentinel.web.exception.HABlockExceptionHandler;

import lombok.extern.slf4j.Slf4j;

/**
 * 接入层Sentinel Interceptor自动装配类
 *
 * @author fenglibin
 */
@Slf4j
@Configuration
public class HAWebInterceptorConfig extends HASentinelConfig implements WebMvcConfigurer {

	@Autowired
	private HASentineConfigProperties sentineConfigProperties;

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		log.info("Add sentinel interceptor:" + this.getClass());
		// Add Sentinel interceptor
		addSpringMvcInterceptor(registry);
	}

	private void addSpringMvcInterceptor(InterceptorRegistry registry) {

		SentinelWebMvcConfig config = new SentinelWebMvcConfig();

		// Depending on your situation, you can choose to process the BlockException via
		// the BlockExceptionHandler or throw it directly, then handle it
		// in Spring web global exception handler.

		// config.setBlockExceptionHandler((request, response, e) -> { throw e; });

		// Use the default handler.
		config.setBlockExceptionHandler(new HABlockExceptionHandler());

		// Custom configuration if necessary
		config.setHttpMethodSpecify(true);

		if (WebConfigManager.getUrlCleaner() == null) {
			WebConfigManager.setUrlCleaner(new ExcludeUrlCleaner(sentineConfigProperties.getExcludeUrlPrefix()));
		}
		config.setUrlCleaner(WebConfigManager.getUrlCleaner());

		// config.setOriginParser(request -> request.getHeader("S-user"));
		config.setOriginParser(WebConfigManager.getOriginParser());

		// Add sentinel interceptor
		registry.addInterceptor(new HASentinelWebInterceptor(config)).addPathPatterns("/**");
	}

	/**
	 * 基于Spring的全局异常处理器，在 {@code BlockExceptionHandler} 抛出
	 * {@link BlockException}异常时，该异常处理会被激活。
	 *
	 * @author fenglibin
	 */
	@ControllerAdvice
	@Order(0)
	public class HASentinelSpringMvcBlockHandlerConfig {

		private Logger logger = LoggerFactory.getLogger(this.getClass());

		@ExceptionHandler(BlockException.class)
		@ResponseBody
		public ResultWrapper sentinelBlockHandler(BlockException e) {
			logger.warn("Blocked by Sentinel: {}", e.getRule());
			// Return the customized result.
			return ResultWrapper.blocked();
		}
	}

}
