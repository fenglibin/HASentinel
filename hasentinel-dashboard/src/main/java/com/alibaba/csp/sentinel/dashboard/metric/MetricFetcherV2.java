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
package com.alibaba.csp.sentinel.dashboard.metric;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.alibaba.csp.sentinel.Constants;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.MetricEntity;
import com.alibaba.csp.sentinel.dashboard.repository.metric.MetricsRepository;
import com.alibaba.csp.sentinel.node.metric.MetricNode;

/**
 * Fetch metric of machines.
 *
 * @author fenglibin
 */
@Component
public class MetricFetcherV2 {

	private static Logger logger = LoggerFactory.getLogger(MetricFetcherV2.class);

	@Qualifier("influxShardingDBMetricsRepository")
	@Autowired
	private MetricsRepository<MetricEntity> metricStore;

	private void writeMetric(Map<String, MetricEntity> map) {
		if (map.isEmpty()) {
			return;
		}
		Date date = new Date();
		for (MetricEntity entity : map.values()) {
			entity.setGmtCreate(date);
			entity.setGmtModified(date);
		}
		metricStore.saveAll(map.values());
	}

	private String buildMetricKey(String app, String resource, long timestamp) {
		return new StringBuilder(app).append("__").append(resource).append("__")
				.append(timestamp / 1000).toString();
	}

	private boolean shouldFilterOut(String resource) {
		return RES_EXCLUSION_SET.contains(resource);
	}

	private static final Set<String> RES_EXCLUSION_SET = new HashSet<String>() {
		{
			add(Constants.TOTAL_IN_RESOURCE_NAME);
			add(Constants.SYSTEM_LOAD_RESOURCE_NAME);
			add(Constants.CPU_USAGE_RESOURCE_NAME);
		}
	};

	/**
	 * 保存通过Http请求接收到的Metric
	 * 
	 * @author fenglibin
	 * @param team 应用归属团队
	 * @param app
	 */
	public void saveMetric(String team, String app, String metric) {
		Map<String, MetricEntity> metricMap = makeMetricMap(team, app, metric);
		writeMetric(metricMap);
	}

	/**
	 * 解析通过Http请求接收到的Metric
	 * 
	 * @author fenglibin
	 * @param team   应用归属团队
	 * @param app    应用名称
	 * @param metric
	 * @return metric map
	 */
	private Map<String, MetricEntity> makeMetricMap(String team, String app, String metric) {
		Map<String, MetricEntity> map = new HashMap<String, MetricEntity>();
		String[] lines = metric.split("\n");
		// logger.info("handleBody() lines=" + lines.length + ", machine=" + machine);
		if (lines.length < 1) {
			return map;
		}

		for (String line : lines) {
			try {
				MetricNode node = MetricNode.fromThinString(line);
				if (shouldFilterOut(node.getResource())) {
					continue;
				}
				/*
				 * aggregation metrics by team_app_resource_timeSecond, ignore ip and port.
				 */
				String key = buildMetricKey(app, node.getResource(), node.getTimestamp());
				MetricEntity entity = map.get(key);
				if (entity != null) {
					entity.addPassQps(node.getPassQps());
					entity.addBlockQps(node.getBlockQps());
					entity.addRtAndSuccessQps(node.getRt(), node.getSuccessQps());
					entity.addExceptionQps(node.getExceptionQps());
					entity.addCount(1);
				} else {
					entity = new MetricEntity();
					entity.setTeam(team);
					entity.setApp(app);
					entity.setTimestamp(new Date(node.getTimestamp()));
					entity.setPassQps(node.getPassQps());
					entity.setBlockQps(node.getBlockQps());
					entity.setRtAndSuccessQps(node.getRt(), node.getSuccessQps());
					entity.setExceptionQps(node.getExceptionQps());
					entity.setCount(1);
					entity.setResource(node.getResource());
					map.put(key, entity);
				}

			} catch (Exception e) {
				logger.warn("handleBody line exception, app: {}, line: {}", app, line);
			}
		}
		return map;
	}

}
