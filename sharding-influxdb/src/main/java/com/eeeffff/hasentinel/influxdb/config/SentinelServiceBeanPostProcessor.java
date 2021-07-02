package com.eeeffff.hasentinel.influxdb.config;

import java.lang.reflect.Modifier;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Service;

import com.eeeffff.hasentinel.influxdb.service.SentinelDataService;

import lombok.extern.slf4j.Slf4j;

/**
 * 将SentinelDataService的实现子类，自动装配到SentinelDataService的services map中
 * 
 * @author fenglibin
 *
 */
@Slf4j
@Service
public class SentinelServiceBeanPostProcessor implements BeanPostProcessor {

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		if (bean instanceof SentinelDataService && !Modifier.isAbstract(bean.getClass().getModifiers())) {
			log.info("Add sentinel data service implemention:" + bean.getClass().getName());
			SentinelDataService sentinelService = (SentinelDataService) bean;
			SentinelDataService.services.put(sentinelService.getServiceName(), sentinelService);
		}
		return bean;
	}
}
