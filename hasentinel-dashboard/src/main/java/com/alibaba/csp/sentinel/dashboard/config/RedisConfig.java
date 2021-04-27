package com.alibaba.csp.sentinel.dashboard.config;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@ConfigurationProperties(prefix = "spring.redis")
@Data
public class RedisConfig {
	// 使用的数据库
	private int database;
	// redis服务器地址
	private String host;
	// 端口
	private int port;
	// 密码
	private String password;
	// 超时时间
	private int timeout;
	// Key的前缀
	private String keyPrefix = "sentinel-";
	private static String redisKeyPrefix = null;
	@Autowired
	private LettucePoolConfig lettucePoolConfig;

	@PostConstruct
	public void init() {
		redisKeyPrefix = keyPrefix;
	}

	public static String getRedisKeyPrefix() {
		return redisKeyPrefix;
	}

	@ConfigurationProperties(prefix = "spring.redis.lettuce.pool")
	@Data
	public class LettucePoolConfig {
		private int maxActive;
		private int maxWait;
		private int maxIdle;
		private int minIdle;
	}

	@ConfigurationProperties(prefix = "spring.redis.cache-time")
	@Data
	public class CacheTimeConfig {
		// 应用App的缓存时间，单位为秒，加上缓存后，新连上来的App需要等最多５分钟才可见
		private int appResource = 300;
		// 耗时资源的最多在缓存中保存的时间，单位为秒
		private int topResource = 1800;
	}
}
