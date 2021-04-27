package com.alibaba.csp.sentinel.dashboard.wrapper.redis;

/**
 * 定义Redis Key
 * 
 * @author fenglibin
 *
 */
public class RedisKey {
	// 用于保存接入Sentinel的所有应用的Redis Key，值以hash的方式进行存储
	public static final String SENTINEL_ALL_APPS = "all-apps";
	// 用于保存接入Sentinel的所有团队的Redis Key，值以hash的方式进行存储
	public static final String SENTINEL_ALL_TEAMS = "all-teams";
}
