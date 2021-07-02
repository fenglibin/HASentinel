package com.eeeffff.hasentinel.influxdb.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.eeeffff.hasentinel.influxdb.config.RedisConfig.LettucePoolConfig;
import com.eeeffff.hasentinel.influxdb.wrapper.redis.RedisTemplateWrapper;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author fenglibin
 *
 */
@Configuration
@EnableCaching
@EnableConfigurationProperties({ RedisConfig.class, LettucePoolConfig.class})
@Slf4j
public class RedisTemplateConfig extends CachingConfigurerSupport {

	@Autowired
	private RedisConfig redisConfig;

	/**
	 * RedisTemplate配置
	 * 
	 * @param connectionFactory
	 * @return
	 */
	@Bean
	public RedisTemplate<String, Object> redisTemplate(LettuceConnectionFactory connectionFactory) {
		log.info("redis host is:" + redisConfig.getHost());
		// 配置redisTemplate
		RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
		redisTemplate.setConnectionFactory(connectionFactory);
		redisTemplate.setKeySerializer(new StringRedisSerializer());// key序列化
		redisTemplate.setHashKeySerializer(new StringRedisSerializer());
		redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());// value序列化
		redisTemplate.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
		redisTemplate.afterPropertiesSet();
		if (redisConfig.getHost() != null) {
			RedisTemplateWrapper.setRedisTemplate(redisTemplate);
		}
		return redisTemplate;
	}

}
