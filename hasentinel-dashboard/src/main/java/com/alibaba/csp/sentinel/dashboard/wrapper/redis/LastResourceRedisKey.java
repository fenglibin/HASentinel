package com.alibaba.csp.sentinel.dashboard.wrapper.redis;
/**
 * 该类定义了最近访问的资源的访问情况、并记录的Redis中的Redis的Key
 * @author fenglibin
 *
 */
public enum LastResourceRedisKey {
	// 用于存储根据平均响应时间排序好的资源的Redis zset key，该key中的value会包括所有的资源，资源由于超过有效期会被删除
	LAST_RESOURCE_SORTED_METRIC_KEY("last_resource_sorted_metric_key"),
	// 用于存储根据平均响应时间排序好的资源的Redis zset key，该key中的value会包括所有的资源，资源不会因为超过有效期被删除
	LAST_RESOURCE_SORTED_METRIC_KEY_NOEXPIRE("last_resource_sorted_metric_key_noexpire"),
	// 用于存储根据平均响应时间排序好的资源的Redis zset key，该key中的value只包括http web请求的资源，资源由于超过有效期会被删除
	LAST_RESOURCE_SORTED_METRIC_KEY_WEB("last_resource_sorted_metric_key_web"),
	// 用于存储根据平均响应时间排序好的资源的Redis zset key，该key中的value只包括http web请求的资源，资源不会因为超过有效期被删除
	LAST_RESOURCE_SORTED_METRIC_KEY_WEB_NOEXPIRE("last_resource_sorted_metric_key_web_noexpire"),
	// 用于存储根据平均响应时间排序好的资源的Redis zset key，该key中的value只包括dubbo请求的资源，资源由于超过有效期会被删除
	LAST_RESOURCE_SORTED_METRIC_KEY_DUBBO("last_resource_sorted_metric_key_dubbo"),
	// 用于存储根据平均响应时间排序好的资源的Redis zset key，该key中的value只包括dubbo请求的资源，资源不会因为超过有效期被删除
	LAST_RESOURCE_SORTED_METRIC_KEY_DUBBO_NOEXPIRE("last_resource_sorted_metric_key_dubbo_noexpire"),
	// 用于存储根据平均响应时间排序好的资源的Redis zset key，该key中的value只包括除http
	// web请求和dubbo请求的资源，如自定义的请求，资源由于超过有效期会被删除
	LAST_RESOURCE_SORTED_METRIC_KEY_OTHER("last_resource_sorted_metric_key_other"),
	// 用于存储根据平均响应时间排序好的资源的Redis zset key，该key中的value只包括除http
	// web请求和dubbo请求的资源，如自定义的请求，资源不会因为超过有效期被删除
	LAST_RESOURCE_SORTED_METRIC_KEY_OTHER_NOEXPIRE("last_resource_sorted_metric_key_other_noexpire"),
	
	// 用于存储代表每个请求资源的MetricEntity资源的Redis hashset key，该key中的value会包括所有的资源
	// 每个资源包括最后一次的成功QPS、拒绝QPS、响应时间等，资源由于超过有效期会被删除
	LAST_RESOURCE_ALL_METRIC_KEY("last_resource_all_metric_key"),
	// 用于存储代表每个请求资源的MetricEntity资源的Redis hashset key，该key中的value会包括所有的资源
	// 每个资源包括最后一次的成功QPS、拒绝QPS、响应时间等，资源不会因为超过有效期被删除
	LAST_RESOURCE_ALL_METRIC_KEY_NOEXPIRE("last_resource_all_metric_key_noexpire");
	
	private String value;
	LastResourceRedisKey(String value) {
		this.value = value;
	}
	public String getValue() {
		return value;
	}
	
}
