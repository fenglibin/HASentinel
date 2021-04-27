package com.alibaba.csp.sentinel.dashboard.service.app;

import java.util.List;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.MetricEntity;
import com.alibaba.csp.sentinel.dashboard.util.MetricUtil;
import com.alibaba.csp.sentinel.dashboard.wrapper.redis.RedisKey;
import com.alibaba.csp.sentinel.dashboard.wrapper.redis.RedisTemplateWrapper;
import com.alibaba.csp.sentinel.util.StringUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * 处理应用相关的服务
 * 
 * @author fenglibin
 *
 */
@Service
@Slf4j
public class AppService {
	/**
	 * 将应用信息增加到Redis缓存中，后面拿取的时候方便
	 * 
	 * @param metricEntity
	 */
	@Async
	public void addAppToCache(MetricEntity metricEntity) {
		log.info("将App的信息保存到Redis中。");
		String team = metricEntity.getTeam();
		team = team == null ? "default" : team;
		String app = metricEntity.getApp();
		// 将所有的app名称保存到一个Redis Key中
		RedisTemplateWrapper.hSet(RedisKey.SENTINEL_ALL_APPS, app, 0);

		String key = new StringBuilder(RedisKey.SENTINEL_ALL_APPS).append("-").append(team).toString();
		// 将所有的app名称分别保存到对应的team相对应的Redis Key中，方便于将来通过team直接查询
		RedisTemplateWrapper.hSet(key, app, 0);

		// 保存所有团队
		RedisTemplateWrapper.hSet(RedisKey.SENTINEL_ALL_TEAMS, team, 0);
		// 获取当前metric代表的请求类型:WEB、DUBBO、OTHER
		String requestType = MetricUtil.getRequestTypeByResource(metricEntity.getResource());

		// 生成按请求类型的key
		key = new StringBuilder(RedisKey.SENTINEL_ALL_APPS).append("-").append(requestType).toString();
		// 按metric的类型区分，将应用再保存到不同的请求类型中
		RedisTemplateWrapper.hSet(key, app, 0);

		// 生成按团队加应用的key
		key = new StringBuilder(RedisKey.SENTINEL_ALL_APPS).append("-").append(team).append("-").append(requestType)
				.toString();
		// 按metric的类型区分，将应用再保存到不同的请求类型中
		RedisTemplateWrapper.hSet(key, app, 0);
	}

	/**
	 * 获取归属于对应团队的应用名称信息，如果team的信息为空，则获取所有的应用
	 * 
	 * @param team        应用归属的团队
	 * @param requestType 请求的类型，包括WEB,DUBBO,OTHER三种
	 * @return
	 */
	public List<String> getApps(String team, String requestType) {
		String redisKey = null;
		if (StringUtil.isNotEmpty(team) && StringUtil.isNotEmpty(requestType)) {
			redisKey = new StringBuilder(RedisKey.SENTINEL_ALL_APPS).append("-").append(team).append("-")
					.append(requestType).toString();
		} else if (StringUtil.isNotEmpty(team)) {
			redisKey = new StringBuilder(RedisKey.SENTINEL_ALL_APPS).append("-").append(team).toString();
		} else if (StringUtil.isNotEmpty(requestType)) {
			redisKey = new StringBuilder(RedisKey.SENTINEL_ALL_APPS).append("-").append(requestType).toString();
		} else {
			redisKey = new StringBuilder(RedisKey.SENTINEL_ALL_APPS).toString();
		}
		return RedisTemplateWrapper.hGetKeys(redisKey);
	}

	/**
	 * 获取所有接入应用的团队信息
	 * 
	 * @return
	 */
	public List<String> getTeams() {
		return RedisTemplateWrapper.hGetKeys(RedisKey.SENTINEL_ALL_TEAMS);
	}
}
