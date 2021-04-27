package com.eeefff.hasentinel.influxdb.wrapper.redis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.util.CollectionUtils;

import com.alibaba.csp.sentinel.util.StringUtil;
import com.eeefff.hasentinel.influxdb.config.RedisConfig;

/**
 * RedisTemplate包装器
 * 
 * @author fenglibin
 *
 */
public class RedisTemplateWrapper {
	private static RedisTemplate<String, Object> redisTemplate;

	public static RedisTemplate<String, Object> getRedisTemplate() {
		return redisTemplate;
	}

	public static void setRedisTemplate(RedisTemplate<String, Object> redisTemplate) {
		RedisTemplateWrapper.redisTemplate = redisTemplate;
	}

	/**
	 * 根据key从redis中获取值
	 * 
	 * @param key
	 * @return
	 */
	public static Object get(String key) {
		if (redisTemplate == null || StringUtil.isEmpty(key)) {
			return null;
		}
		return redisTemplate.opsForValue().get(getDefineKey(key));
	}

	/**
	 * 根据key获取到值后，并通过自定义实现的Consumer消费该key对应的内容
	 * 
	 * @param key
	 * @param valueConsumer
	 */
	public static void getAndConsumeValue(String key, Consumer<Object> valueConsumer) {
		Object value = get(key);
		valueConsumer.accept(value);
	}

	/**
	 * 将值写入到redis中
	 * 
	 * @param key
	 * @param value
	 */
	public static void set(String key, Object value) {
		Optional.ofNullable(redisTemplate).ifPresent(r -> {
			r.opsForValue().set(getDefineKey(key), value);
		});
	}

	/**
	 * 将值写入到redis中，并指定过期时间
	 * 
	 * @param key
	 * @param value
	 * @param timeout
	 * @param unit
	 */
	public static void set(String key, Object value, long timeout, TimeUnit unit) {
		Optional.ofNullable(redisTemplate).ifPresent(r -> {
			r.opsForValue().set(getDefineKey(key), value, timeout, unit);
		});
	}

	public static void del(String key) {
		Optional.ofNullable(redisTemplate).ifPresent(r -> {
			r.delete(getDefineKey(key));
		});
	}

	public static void del(Collection<String> keys) {
		List<String> sKeys = new ArrayList<String>();
		keys.forEach(k->{
			sKeys.add(getDefineKey(k));
		});
		Optional.ofNullable(redisTemplate).ifPresent(r -> {
			r.delete(sKeys);
		});
	}

	/**
	 * 将值写入到redis的zset中
	 * 
	 * @param key
	 * @param value
	 */
	public static void zSetAdd(String key, Object value, double score) {
		Optional.ofNullable(redisTemplate).ifPresent(r -> {
			r.opsForZSet().add(getDefineKey(key), value, score);
		});
	}

	public static void zSetDel(String key, Object... values) {
		Optional.ofNullable(redisTemplate).ifPresent(r -> {
			r.opsForZSet().remove(getDefineKey(key), values);
		});
	}

	/**
	 * 从zSet获取值
	 * 
	 * @param key
	 * @param range
	 * @param limit
	 * @return
	 */
	public static Set<TypedTuple<Object>> getZSet(String key, int page, int size) {
		Set<TypedTuple<Object>> set = null;
		if (redisTemplate != null) {
			page = page < 1 ? 1 : page;
			size = size < 1 ? 1 : size;
			int start = (page - 1) * size;
			int end = start + size - 1;
			set = redisTemplate.opsForZSet().reverseRangeWithScores(getDefineKey(key), start, end);
		}
		return set;
	}

	/**
	 * 将值写入到redis的hashset中
	 * 
	 * @param key
	 * @param value
	 */
	public static void hSet(String key, String hashKey, Object value) {
		Optional.ofNullable(redisTemplate).ifPresent(r -> {
			r.opsForHash().put(getDefineKey(key), hashKey, value);
		});
	}

	/**
	 * 从hashset中批量获取数据
	 * 
	 * @param key
	 * @param hashKeys
	 */
	public static Object hGet(String key, Object hashKey) {
		List<Object> hashKeyList = new ArrayList<Object>();
		hashKeyList.add(hashKey);
		List<Object> list = hGet(key, hashKeyList);
		if(!CollectionUtils.isEmpty(list)) {
			return list.get(0);
		}
		return null;
	}
	
	/**
	 * 从hashset中批量获取数据
	 * 
	 * @param key
	 * @param hashKeys
	 */
	public static List<Object> hGet(String key, Collection<Object> hashKeys) {
		List<Object> result = new ArrayList<Object>();
		Optional.ofNullable(redisTemplate).ifPresent(r -> {
			List<Object> list = r.opsForHash().multiGet(getDefineKey(key), hashKeys);
			if (list != null) {
				result.addAll(list);
			}
		});
		return result;
	}

	public static void hDel(String key, Object... hashKeys) {
		Optional.ofNullable(redisTemplate).ifPresent(r -> {
			r.opsForHash().delete(getDefineKey(key), hashKeys);
		});
	}

	private static String getDefineKey(String key) {
		return new StringBuilder(RedisConfig.getRedisKeyPrefix()).append(key).toString();
	}

}
