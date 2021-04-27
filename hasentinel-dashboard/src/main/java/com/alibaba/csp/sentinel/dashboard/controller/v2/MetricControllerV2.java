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
package com.alibaba.csp.sentinel.dashboard.controller.v2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.MetricEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.vo.MetricEntityVO;
import com.alibaba.csp.sentinel.dashboard.domain.Result;
import com.alibaba.csp.sentinel.dashboard.domain.vo.MetricVo;
import com.alibaba.csp.sentinel.dashboard.metric.MetricFetcherV2;
import com.alibaba.csp.sentinel.dashboard.repository.metric.MetricsHandler;
import com.alibaba.csp.sentinel.dashboard.repository.metric.MetricsRepository;
import com.alibaba.csp.sentinel.dashboard.util.MD5Util;
import com.alibaba.csp.sentinel.dashboard.wrapper.redis.LastResourceRedisKey;
import com.alibaba.csp.sentinel.dashboard.wrapper.redis.RedisTemplateWrapper;
import com.alibaba.csp.sentinel.util.StringUtil;

/**
 * @author leyou
 */
@Controller
@RequestMapping(value = "/metric", produces = MediaType.APPLICATION_JSON_VALUE)
public class MetricControllerV2 {

	private static Logger logger = LoggerFactory.getLogger(MetricControllerV2.class);

	private static final long maxQueryIntervalMs = 1000 * 60 * 60;

	@Qualifier("influxShardingDBMetricsRepository")
	@Autowired
	private MetricsRepository<MetricEntity> metricStore;
	@Autowired
	private MetricFetcherV2 metricFetcher;
	@Autowired
	private MetricsHandler metricsHandler;

	@ResponseBody
	@RequestMapping("/queryTopResourceMetric.json")
	public Result<?> queryTopResourceMetric(final String app, Integer pageIndex, Integer pageSize, Boolean desc,
			Long startTime, Long endTime, String searchKey) {
		if (StringUtil.isEmpty(app)) {
			return Result.ofFail(-1, "app can't be null or empty");
		}
		if (pageIndex == null || pageIndex <= 0) {
			pageIndex = 1;
		}
		if (pageSize == null) {
			pageSize = 6;
		}
		if (pageSize >= 20) {
			pageSize = 20;
		}
		if (desc == null) {
			desc = true;
		}
		if (endTime == null) {
			endTime = System.currentTimeMillis();
		}
		if (startTime == null) {
			startTime = endTime - 1000 * 60 * 5;
		}
		if (endTime - startTime > maxQueryIntervalMs) {
			return Result.ofFail(-1, "time intervalMs is too big, must <= 1h");
		}
		List<String> resources = metricStore.listResourcesOfApp(app);

		if (resources == null || resources.isEmpty()) {
			return Result.ofSuccess(null);
		}
		logger.debug("queryTopResourceMetric(), resources.size()={}", resources.size());

		if (!desc) {
			Collections.reverse(resources);
		}
		if (StringUtil.isNotEmpty(searchKey)) {
			List<String> searched = new ArrayList<>();
			for (String resource : resources) {
				if (resource.contains(searchKey)) {
					searched.add(resource);
				}
			}
			resources = searched;
		}
		int totalPage = (resources.size() + pageSize - 1) / pageSize;
		List<String> topResource = new ArrayList<>();
		if (pageIndex <= totalPage) {
			topResource = resources.subList((pageIndex - 1) * pageSize,
					Math.min(pageIndex * pageSize, resources.size()));
		}
		final Map<String, Iterable<MetricVo>> map = new ConcurrentHashMap<>();
		logger.debug("topResource={}", topResource);
		long time = System.currentTimeMillis();
		for (final String resource : topResource) {
			List<MetricEntity> entities = metricStore.queryByAppAndResourceBetween(app, resource, startTime, endTime);
			logger.debug("resource={}, entities.size()={}", resource, entities == null ? "null" : entities.size());
			List<MetricVo> vos = MetricVo.fromMetricEntities(entities, resource);
			Iterable<MetricVo> vosSorted = sortMetricVoAndDistinct(vos);
			map.put(resource, vosSorted);
		}
		logger.debug("queryTopResourceMetric() total query time={} ms", System.currentTimeMillis() - time);
		Map<String, Object> resultMap = new HashMap<>(16);
		resultMap.put("totalCount", resources.size());
		resultMap.put("totalPage", totalPage);
		resultMap.put("pageIndex", pageIndex);
		resultMap.put("pageSize", pageSize);

		Map<String, Iterable<MetricVo>> map2 = new LinkedHashMap<>();
		// order matters.
		for (String identity : topResource) {
			map2.put(identity, map.get(identity));
		}
		resultMap.put("metric", map2);
		return Result.ofSuccess(resultMap);
	}

	@ResponseBody
	@RequestMapping("/queryByAppAndResource.json")
	public Result<?> queryByAppAndResource(String app, String identity, Long startTime, Long endTime) {
		if (StringUtil.isEmpty(app)) {
			return Result.ofFail(-1, "app can't be null or empty");
		}
		if (StringUtil.isEmpty(identity)) {
			return Result.ofFail(-1, "identity can't be null or empty");
		}
		if (endTime == null) {
			endTime = System.currentTimeMillis();
		}
		if (startTime == null) {
			startTime = endTime - 1000 * 60;
		}
		if (endTime - startTime > maxQueryIntervalMs) {
			return Result.ofFail(-1, "time intervalMs is too big, must <= 1h");
		}
		List<MetricEntity> entities = metricStore.queryByAppAndResourceBetween(app, identity, startTime, endTime);
		List<MetricVo> vos = MetricVo.fromMetricEntities(entities, identity);
		return Result.ofSuccess(sortMetricVoAndDistinct(vos));
	}

	private Iterable<MetricVo> sortMetricVoAndDistinct(List<MetricVo> vos) {
		if (vos == null) {
			return null;
		}
		Map<Long, MetricVo> map = new TreeMap<>();
		for (MetricVo vo : vos) {
			MetricVo oldVo = map.get(vo.getTimestamp());
			if (oldVo == null || vo.getGmtCreate() > oldVo.getGmtCreate()) {
				map.put(vo.getTimestamp(), vo);
			}
		}
		return map.values();
	}

	/**
	 * 接收应用上报的metric数据，并进行存储
	 * 
	 * @param team 应用归属团队
	 * @param app 应用名称
	 * @param request
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/saveMetric.json")
	public Result<?> saveMetric(String team, String app, HttpServletRequest request) {
		team = team == null ? "default" : team;
		String metric = request.getParameter("metric");
		metricFetcher.saveMetric(team, app, metric);
		return Result.ofSuccess(null);
	}

	@ResponseBody
	@RequestMapping("/getMetricSize.json")
	public Result<?> getMetricSize() {
		return Result.ofSuccess(metricsHandler.getQueueSize());
	}

	@ResponseBody
	@RequestMapping("/cleanMetricQueue.json")
	public Result<?> cleanMetricQueue() {
		metricsHandler.cleanQueue();
		return Result.ofSuccess(null);
	}

	@ResponseBody
	@RequestMapping("/getMetricCount.json")
	public Result<?> getMetricCount() {
		return Result.ofSuccess(metricsHandler.getMetricCount());
	}

	@ResponseBody
	@RequestMapping("/setAddMetric.json")
	public Result<?> setMetricCount(String addMetric) {
		return Result.ofSuccess(metricsHandler.setAddMetric(Boolean.parseBoolean(addMetric)));
	}

	@ResponseBody
	@RequestMapping("/setManualWrite.json")
	public Result<?> setManualWrite(String manualWrite) {
		return Result.ofSuccess(metricsHandler.setManualWrite(Boolean.parseBoolean(manualWrite)));
	}

	@ResponseBody
	@RequestMapping("/startManualWrite.json")
	public Result<?> startManualWrite() {
		metricsHandler.startManualWrite();
		return Result.ofSuccess(null);
	}

	@ResponseBody
	@RequestMapping("/setMetricEnableTopTimeReport.json")
	public Result<?> setMetricEnableTopTimeReport(String metricEnableTopTimeReport) {
		return Result.ofSuccess(
				metricsHandler.setMetricEnableTopTimeReport(Boolean.parseBoolean(metricEnableTopTimeReport)));
	}
	
	@ResponseBody
	@RequestMapping("/setMetricAsyncLogWriteTime.json")
	public Result<?> setMetricAsyncLogWriteTime(String metricAsyncLogWriteTime) {
		metricsHandler.setMetricAsyncLogWriteTime(Boolean.parseBoolean(metricAsyncLogWriteTime));
		return Result.ofSuccess(null);
	}

	/**
	 * 
	 * @param page 当前查看资源的页数，默认为第1页
	 * @param size 每一页要查看资源的数量，默认为100
	 * @param type 查看的资源的类型，默认为全部资源，类型可以为:WEB,DUBBO,OTHER
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/getLastResourceSortedMetric.json")
	public Result<?> getLastResourceSortedMetric(int page, int size, String type, boolean withExpireData) {
		return Result.ofSuccess(metricStore.getLastResourceSortedMetric(page, size,type,withExpireData));
	}

	/**
	 * 
	 * @param page 当前查看资源的页数，默认为第1页
	 * @param size 每一页要查看资源的数量，默认为100
	 * @param type 查看的资源的类型，默认为全部资源，类型可以为:WEB,DUBBO,OTHER
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/getLastResourceSortedMetricAll.json")
	public Result<?> getLastResourceSortedMetricAll(int page, int size, String type, boolean withExpireData) {
		return Result.ofSuccess(metricStore.getLastResourceSortedMetricAll(page, size,type,withExpireData));
	}
}
