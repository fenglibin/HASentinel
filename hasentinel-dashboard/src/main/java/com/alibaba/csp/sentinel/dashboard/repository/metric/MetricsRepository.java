/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.dashboard.repository.metric;

import java.util.List;
import java.util.Set;

import org.springframework.data.redis.core.ZSetOperations.TypedTuple;

import com.alibaba.csp.sentinel.dashboard.util.MetricUtil;

/**
 * Repository interface for aggregated metrics data.
 *
 * @param <T> type of metrics
 * @author Eric Zhao
 */
public interface MetricsRepository<T> {

	/** 数据库名称 */
	public static final String SENTINEL_DATABASE = "sentinel_db";

	/** 数据表名称：用于保存WEB请求 */
	public static final String METRIC_MEASUREMENT_WEB = "sentinel_metric_web";

	/** 数据表名称：用于保存DUBBO请求 */
	public static final String METRIC_MEASUREMENT_DUBBO = "sentinel_metric_dubbo";

	/** 数据表名称：用于保存非WEB及DUBBO请求的Metric，如使用注解@SentinelResource自定义的限流方式 */
	public static final String METRIC_MEASUREMENT_OTHER = "sentinel_metric_other";

	/** 数据表名称：用于存储每个应用中的每个请求的最后一次访问量，每个应用中的每个请求只会保存一条记录 */
	public static final String METRIC_MEASUREMENT_EACH_LAST = "sentinel_metric_each_last";

	/**
	 * Save the metric to the storage repository.
	 *
	 * @param metric metric data to save
	 */
	void save(T metric);

	/**
	 * Save all metrics to the storage repository.
	 *
	 * @param metrics metrics to save
	 */
	void saveAll(Iterable<T> metrics);

	/**
	 * Get all metrics by {@code appName} and {@code resourceName} between a period
	 * of time.
	 *
	 * @param app       application name for Sentinel
	 * @param resource  resource name
	 * @param startTime start timestamp
	 * @param endTime   end timestamp
	 * @return all metrics in query conditions
	 */
	List<T> queryByAppAndResourceBetween(String app, String resource, long startTime, long endTime);

	/**
	 * List resource name of provided application name.
	 *
	 * @param app application name
	 * @return list of resources
	 */
	List<String> listResourcesOfApp(String app);

	/**
	 * 根据资源返回Metric存放的表，以"/"开头表示WEB请求，其它默认为DUBBO请求
	 * 
	 * @param resource
	 * @return
	 */
	default String getMetricTableByResource(String resource) {
		if (MetricUtil.checkIsWebRequestByResource(resource)) {
			return METRIC_MEASUREMENT_WEB;
		} else if (MetricUtil.checkIsDubboRequestByResource(resource)) {
			return METRIC_MEASUREMENT_DUBBO;
		} else {
			return METRIC_MEASUREMENT_OTHER;
		}
	}

	/**
	 * 从Redis的zSet中获取按耗时最多的排序的接口
	 * 
	 * @param page
	 * @param size
	 * @param type
	 * @param withExpireData
	 * @return
	 */
	default Set<TypedTuple<Object>> getLastResourceSortedMetric(int page, int size, String type, boolean withExpireData) {
		return null;
	};

	/**
	 * 从Redis的zSet中获取按耗时最多的排序的接口
	 * 
	 * @param page
	 * @param size
	 * @param type
	 * @param withExpireData 是否需要过期的资源数据
	 * @return
	 */
	default List<Object> getLastResourceSortedMetricAll(int page, int size, String type, boolean withExpireData) {
		return null;
	}
}
