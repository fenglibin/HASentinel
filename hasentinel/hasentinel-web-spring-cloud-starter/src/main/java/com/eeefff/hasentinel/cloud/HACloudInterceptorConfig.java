package com.eeefff.hasentinel.cloud;

import java.lang.reflect.Field;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.alibaba.csp.sentinel.adapter.spring.webmvc.SentinelWebInterceptor;
import com.alibaba.csp.sentinel.adapter.spring.webmvc.config.SentinelWebMvcConfig;
import com.eeefff.hasentinel.common.config.HASentineConfigProperties;
import com.eeefff.hasentinel.common.config.HASentinelConfig;
import com.eeefff.hasentinel.common.intercepter.HASentinelWebInterceptor;
import com.eeefff.hasentinel.cloud.exception.HABlockExceptionHandler;

import lombok.extern.slf4j.Slf4j;

/**
 * 接入层Sentinel Interceptor自动装配类
 *
 * @author fenglibin
 */
@Slf4j
@Configuration
public class HACloudInterceptorConfig extends HASentinelConfig implements WebMvcConfigurer {

	@Autowired
	private HASentineConfigProperties sentineConfigProperties;
	@Autowired
	private Optional<SentinelWebInterceptor> sentinelWebInterceptorOptional;

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		log.info("Add sentinel interceptor:" + this.getClass());
		// Add Sentinel interceptor
		addSpringMvcInterceptor(registry);
	}

	private void addSpringMvcInterceptor(InterceptorRegistry registry) {
		try {

			// spring-cloud-starter-alibaba-sentinel-xxx.jar中的类：<br>
			// com.alibaba.cloud.sentinel.SentinelWebAutoConfiguration中也会对SentinelWebInterceptor<br>
			// 进行注册，如果此时也是注册一个新的SentinelWebInterceptor，就会造成冲突，因而此时检查是不是已经存在了 <br>
			// SentinelWebInterceptor，如果存在了则不注册新的，拿已经注册的对象修改其config属性即可。
			if (sentinelWebInterceptorOptional.isPresent()) {
				SentinelWebInterceptor inteceptor = sentinelWebInterceptorOptional.get();
				Field configField = inteceptor.getClass().getDeclaredField("config");
				configField.setAccessible(true);
				SentinelWebMvcConfig _config = (SentinelWebMvcConfig) configField.get(inteceptor);
				// Use the default handler.
				_config.setBlockExceptionHandler(new HABlockExceptionHandler());

				// Custom configuration if necessary
				_config.setHttpMethodSpecify(true);

				if (WebConfigManager.getUrlCleaner() == null) {
					WebConfigManager
							.setUrlCleaner(new ExcludeUrlCleaner(sentineConfigProperties.getExcludeUrlPrefix()));
				}
				_config.setUrlCleaner(WebConfigManager.getUrlCleaner());

				// config.setOriginParser(request -> request.getHeader("S-user"));
				_config.setOriginParser(WebConfigManager.getOriginParser());
			} else {
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
					WebConfigManager
							.setUrlCleaner(new ExcludeUrlCleaner(sentineConfigProperties.getExcludeUrlPrefix()));
				}
				config.setUrlCleaner(WebConfigManager.getUrlCleaner());

				// config.setOriginParser(request -> request.getHeader("S-user"));
				config.setOriginParser(WebConfigManager.getOriginParser());
				// Add sentinel interceptor
				registry.addInterceptor(new HASentinelWebInterceptor(config)).addPathPatterns("/**");
			}

		} catch (Exception e) {
			log.error("增加或修改SentinelWebInterceptor发生异常：" + e.getMessage(), e);
		}
	}
}
