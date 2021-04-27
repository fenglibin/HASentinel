package com.eeefff.hasentinel.web;

import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.alibaba.csp.sentinel.adapter.spring.webmvc.SentinelWebTotalInterceptor;
import com.alibaba.csp.sentinel.adapter.spring.webmvc.config.SentinelWebMvcConfig;
import com.alibaba.csp.sentinel.adapter.spring.webmvc.config.SentinelWebMvcTotalConfig;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.eeefff.hasentinel.common.config.HASentineConfigProperties;
import com.eeefff.hasentinel.common.config.HASentinelConfig;
import com.eeefff.hasentinel.common.intercepter.HASentinelWebInterceptor;
import com.eeefff.hasentinel.common.util.StringUtil;
import com.eeefff.hasentinel.common.vo.config.ResultWrapper;
import com.eeefff.hasentinel.web.exception.HABlockExceptionHandler;

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

	private void addSpringMvcTotalInterceptor(InterceptorRegistry registry) {
		// Config
		SentinelWebMvcTotalConfig config = new SentinelWebMvcTotalConfig();

		// Custom configuration if necessary
		config.setRequestAttributeName("my_sentinel_spring_mvc_total_entity_container");
		config.setTotalResourceName("my-spring-mvc-total-url-request");

		// Add sentinel interceptor
		registry.addInterceptor(new SentinelWebTotalInterceptor(config)).addPathPatterns("/**");
	}

	/**
	 * 基于Spring的全局异常处理器，在 {@code BlockExceptionHandler} 抛出
	 * {@link BlockException}异常时，该异常处理会被激活。
	 *
	 * @author fenglibin
	 */
	@ControllerAdvice
	@Order(0)
	public class LDSentinelSpringMvcBlockHandlerConfig {

		private Logger logger = LoggerFactory.getLogger(this.getClass());

		@ExceptionHandler(BlockException.class)
		@ResponseBody
		public ResultWrapper sentinelBlockHandler(BlockException e) {
			logger.warn("Blocked by Sentinel: {}", e.getRule());
			// Return the customized result.
			return ResultWrapper.blocked();
		}
	}

	/**
	 * 从Controller中分析并获取Rest URL
	 * 
	 * @author fenglibin
	 *
	 */
	// @Configuration
	public class RestUrlFinderBeanPostProcessor implements BeanPostProcessor {
		@Override
		public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
			return bean;
		}

		@Override
		public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
			/**
			 * 自动发现Controller中的Rest URL
			 */
			if (bean.getClass().isAnnotationPresent(Controller.class)
					|| bean.getClass().isAnnotationPresent(RestController.class)) {
				log.info("Controller class:" + bean.getClass());
				String[] classPaths = null;
				if (bean.getClass().isAnnotationPresent(RequestMapping.class)) {
					RequestMapping requestMapping = bean.getClass().getAnnotation(RequestMapping.class);
					classPaths = requestMapping.value();
				}
				Method[] methods = bean.getClass().getMethods();
				for (Method method : methods) {
					String[] methodPaths = null;
					if (method.isAnnotationPresent(RequestMapping.class)) {
						RequestMapping mapping = method.getAnnotation(RequestMapping.class);
						methodPaths = mapping.value();
					} else if (method.isAnnotationPresent(GetMapping.class)) {
						GetMapping mapping = method.getAnnotation(GetMapping.class);
						methodPaths = mapping.value();
					} else if (method.isAnnotationPresent(PostMapping.class)) {
						PostMapping mapping = method.getAnnotation(PostMapping.class);
						methodPaths = mapping.value();
					} else if (method.isAnnotationPresent(PutMapping.class)) {
						PutMapping mapping = method.getAnnotation(PutMapping.class);
						methodPaths = mapping.value();
					} else if (method.isAnnotationPresent(DeleteMapping.class)) {
						DeleteMapping mapping = method.getAnnotation(DeleteMapping.class);
						methodPaths = mapping.value();
					} else if (method.isAnnotationPresent(PatchMapping.class)) {
						PatchMapping mapping = method.getAnnotation(PatchMapping.class);
						methodPaths = mapping.value();
					}
					if (methodPaths == null || methodPaths.length == 0) {
						continue;
					}
					if (classPaths == null || classPaths.length == 0) {
						for (String methodPath : methodPaths) {
							addUrl(methodPath);
						}
					} else {
						for (String classPath : classPaths) {
							for (String methodPath : methodPaths) {
								addUrl(classPath, methodPath);
							}
						}
					}
				}
			}
			return bean;
		}

		private void addUrl(String methodPath) {
			addUrl("", methodPath);
		}

		private void addUrl(String classPath, String methodPath) {
			if (classPath == null) {
				classPath = "";
			}
			if (methodPath.indexOf("{") > 0) {// 是一个Rest URL
				if (methodPath.indexOf("}") < methodPath.length() - 1) {
					/**
					 * 如果rest url中的"}"不是该url中的最后一个字符，这样的url可能是： <br>
					 * /{p1}/machine.json 或 <br>
					 * /{p1}/{p2}/machine.json 或 <br>
					 * /{p1}/xx/{p2}/machine.json <br>
					 * /{p1}/xx/{p2} 或 <br>
					 * /xx/{p1}/{p2} 或 <br>
					 * /xx/{p1}/yy/{p2} <br>
					 * 处理方式为将点位参数替换为正则表达式
					 */
					methodPath = StringUtil.restUrlToRegixUrl(methodPath);
				} else {
					/**
					 * rest url是这样的：/path/{p1}
					 */
					methodPath = methodPath.substring(0, methodPath.indexOf("{") - 1);
				}
				WebConfigManager.getRestUrls().add(classPath + methodPath);
			}
		}
	}
}
