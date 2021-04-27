package com.alibaba.csp.sentinel.dashboard.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
@Component
public class RedisTest {

	@Autowired
	private RedisTemplate<String, Object> redisTemplate;

	@Test
	public void set() {
		redisTemplate.opsForValue().set("test:set1", "testValue1");
		redisTemplate.opsForSet().add("test:set2", "asdf");
		redisTemplate.opsForHash().put("hash1", "name1", "lms1");
		redisTemplate.opsForHash().put("hash1", "name2", "lms2");
		redisTemplate.opsForHash().put("hash1", "name3", "lms3");
		System.out.println(redisTemplate.opsForValue().get("test:set"));
		System.out.println(redisTemplate.opsForHash().get("hash1", "name1"));
	}
}
