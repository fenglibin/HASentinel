package com.alibaba.csp.sentinel.dashboard.repository.metric;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.lang.time.DateFormatUtils;
import org.influxdb.InfluxDB;
import org.influxdb.dto.Point;
import org.influxdb.dto.Query;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.MetricEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.MetricOtherPO;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.MetricPO;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.MetricWebPO;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.MetricDubboPO;
import com.alibaba.csp.sentinel.dashboard.util.InfluxDBUtils;
import com.alibaba.csp.sentinel.dashboard.util.InfluxShardingDBUtils;
import com.alibaba.csp.sentinel.dashboard.util.MetricUtil;
import com.alibaba.csp.sentinel.util.StringUtil;

/**
 * metrics数据InfluxDB存储实现
 * 
 * @author cdfive
 * @date 2018-10-19
 */
@Repository("influxDBMetricsRepository")
public class InfluxDBMetricsRepository implements MetricsRepository<MetricEntity> {

	/** 时间格式 */
	private static final String DATE_FORMAT_PATTERN = "yyyy-MM-dd HH:mm:ss.SSS";

	/** 北京时间领先UTC时间8小时 UTC: Universal Time Coordinated,世界统一时间 */
	private static final Integer UTC_8 = 8;

	@Override
	public void save(MetricEntity metric) {
		if (metric == null || StringUtil.isBlank(metric.getApp())) {
			return;
		}

		InfluxDBUtils.insert(SENTINEL_DATABASE, new InfluxDBUtils.InfluxDBInsertCallback() {
			@SuppressWarnings("synthetic-access")
			@Override
			public void doCallBack(String database, InfluxDB influxDB) {
				if (metric.getId() == null) {
					metric.setId(System.currentTimeMillis());
				}
				doSave(influxDB, metric);
			}
		});
	}

	@Override
	public void saveAll(Iterable<MetricEntity> metrics) {
		if (metrics == null) {
			return;
		}

		Iterator<MetricEntity> iterator = metrics.iterator();
		boolean next = iterator.hasNext();
		if (!next) {
			return;
		}

		MetricEntity m = null;
		while ((m = iterator.next()) != null) {
			final MetricEntity metric = m;
			InfluxDBUtils.insert(SENTINEL_DATABASE, new InfluxDBUtils.InfluxDBInsertCallback() {
				@SuppressWarnings("synthetic-access")
				@Override
				public void doCallBack(String database, InfluxDB influxDB) {
					if (metric.getId() == null) {
						metric.setId(System.currentTimeMillis());
					}
					doSave(influxDB, metric);
				}
			});
		}
	}

	@Override
	public List<MetricEntity> queryByAppAndResourceBetween(String app, String resource, long startTime, long endTime) {
		List<MetricEntity> results = new ArrayList<MetricEntity>();
		if (StringUtil.isBlank(app)) {
			return results;
		}

		if (StringUtil.isBlank(resource)) {
			return results;
		}

		// 将查询的开始时间和结束减去8小时，因为influxdb使用的是UTC时间，北京时间比UTC时间慢8个小时
		endTime = endTime - UTC_8 * 60 * 60 * 1000;
		startTime = startTime - UTC_8 * 60 * 60 * 1000;

		StringBuilder sql = new StringBuilder();
		sql.append("SELECT * FROM " + getMetricTableByResource(resource));
		sql.append(" WHERE app=$app");
		sql.append(" AND resource=$resource");
		sql.append(" AND time>=$startTime");
		sql.append(" AND time<=$endTime");

		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("app", app);
		paramMap.put("resource", resource);
		paramMap.put("startTime", DateFormatUtils.format(new Date(startTime), DATE_FORMAT_PATTERN));
		paramMap.put("endTime", DateFormatUtils.format(new Date(endTime), DATE_FORMAT_PATTERN));

		List<MetricPO> metricPOS = new ArrayList<MetricPO>();
		if (MetricUtil.checkIsWebRequestByResource(resource)) {
			List<MetricWebPO> metricList = InfluxShardingDBUtils.queryList(app, SENTINEL_DATABASE, sql.toString(),
					paramMap, MetricWebPO.class);
			if (metricList != null && !metricList.isEmpty()) {
				metricPOS.addAll(metricList);
			}
		} else if (MetricUtil.checkIsDubboRequestByResource(resource)) {
			List<MetricDubboPO> metricList = InfluxShardingDBUtils.queryList(app, SENTINEL_DATABASE, sql.toString(),
					paramMap, MetricDubboPO.class);
			if (metricList != null && !metricList.isEmpty()) {
				metricPOS.addAll(metricList);
			}
		} else {
			List<MetricOtherPO> metricList = InfluxShardingDBUtils.queryList(app, SENTINEL_DATABASE, sql.toString(),
					paramMap, MetricOtherPO.class);
			if (metricList != null && !metricList.isEmpty()) {
				metricPOS.addAll(metricList);
			}
		}
		if (CollectionUtils.isEmpty(metricPOS)) {
			return results;
		}

		for (MetricPO metricPO : metricPOS) {
			results.add(convertToMetricEntity(metricPO));
		}

		return results;
	}

	@Override
	public List<String> listResourcesOfApp(String app) {
		List<String> result = new ArrayList<String>();
		List<String> webResult = listResourcesOfApp(app, METRIC_MEASUREMENT_WEB);
		List<String> dubboResult = listResourcesOfApp(app, METRIC_MEASUREMENT_DUBBO);
		List<String> otherResult = listResourcesOfApp(app, METRIC_MEASUREMENT_OTHER);
		if (webResult != null && !webResult.isEmpty()) {
			result.addAll(webResult);
		}
		if (dubboResult != null && !dubboResult.isEmpty()) {
			result.addAll(dubboResult);
		}
		if (otherResult != null && !otherResult.isEmpty()) {
			result.addAll(otherResult);
		}
		return result;
	}

	public List<String> listResourcesOfApp(String app, String metricTable) {
		List<String> results = new ArrayList<>();
		if (StringUtil.isBlank(app)) {
			return results;
		}

		StringBuilder sql = new StringBuilder();
		sql.append("SELECT * FROM " + metricTable);
		sql.append(" WHERE app=$app");
		sql.append(" AND time>=$startTime");

		Map<String, Object> paramMap = new HashMap<String, Object>();
		long startTime = System.currentTimeMillis() - 1000 * 60;

		// 将查询的开始时间减去8小时，因为influxdb使用的是UTC时间，北京时间比UTC时间慢8个小时
		startTime = startTime - UTC_8 * 60 * 60 * 1000;
		// 取5分钟之内的数据
		startTime = startTime - 5 * 60 * 1000;

		paramMap.put("app", app);
		paramMap.put("startTime", DateFormatUtils.format(new Date(startTime), DATE_FORMAT_PATTERN));

		List<MetricPO> metricPOS = new ArrayList<MetricPO>();
		if (metricTable.contentEquals(METRIC_MEASUREMENT_WEB)) {
			List<MetricWebPO> metricList = InfluxShardingDBUtils.queryList(app, SENTINEL_DATABASE, sql.toString(),
					paramMap, MetricWebPO.class);
			if (metricList != null && !metricList.isEmpty()) {
				metricPOS.addAll(metricList);
			}
		} else if (metricTable.contentEquals(METRIC_MEASUREMENT_DUBBO)) {
			List<MetricDubboPO> metricList = InfluxShardingDBUtils.queryList(app, SENTINEL_DATABASE, sql.toString(),
					paramMap, MetricDubboPO.class);
			if (metricList != null && !metricList.isEmpty()) {
				metricPOS.addAll(metricList);
			}
		} else {
			List<MetricOtherPO> metricList = InfluxShardingDBUtils.queryList(app, SENTINEL_DATABASE, sql.toString(),
					paramMap, MetricOtherPO.class);
			if (metricList != null && !metricList.isEmpty()) {
				metricPOS.addAll(metricList);
			}
		}

		if (CollectionUtils.isEmpty(metricPOS)) {
			return results;
		}

		List<MetricEntity> metricEntities = new ArrayList<MetricEntity>();
		for (MetricPO metricPO : metricPOS) {
			metricEntities.add(convertToMetricEntity(metricPO));
		}

		Map<String, MetricEntity> resourceCount = new HashMap<>(32);

		for (MetricEntity metricEntity : metricEntities) {
			String resource = metricEntity.getResource();
			if (resourceCount.containsKey(resource)) {
				MetricEntity oldEntity = resourceCount.get(resource);
				oldEntity.addPassQps(metricEntity.getPassQps());
				oldEntity.addRtAndSuccessQps(metricEntity.getRt(), metricEntity.getSuccessQps());
				oldEntity.addBlockQps(metricEntity.getBlockQps());
				oldEntity.addExceptionQps(metricEntity.getExceptionQps());
				oldEntity.addCount(1);
			} else {
				resourceCount.put(resource, MetricEntity.copyOf(metricEntity));
			}
		}

		// Order by last minute b_qps DESC.
		return resourceCount.entrySet().stream().sorted((o1, o2) -> {
			MetricEntity e1 = o1.getValue();
			MetricEntity e2 = o2.getValue();
			int t = e2.getBlockQps().compareTo(e1.getBlockQps());
			if (t != 0) {
				return t;
			}
			return e2.getPassQps().compareTo(e1.getPassQps());
		}).map(Map.Entry::getKey).collect(Collectors.toList());
	}

	private MetricEntity convertToMetricEntity(MetricPO metricPO) {
		MetricEntity metricEntity = new MetricEntity();

		metricEntity.setId(metricPO.getId());
		metricEntity.setGmtCreate(new Date(metricPO.getGmtCreate()));
		metricEntity.setGmtModified(new Date(metricPO.getGmtModified()));
		metricEntity.setApp(metricPO.getApp());
		metricEntity.setTimestamp(Date.from(metricPO.getTime()));
		metricEntity.setResource(metricPO.getResource());
		metricEntity.setPassQps(metricPO.getPassQps());
		metricEntity.setSuccessQps(metricPO.getSuccessQps());
		metricEntity.setBlockQps(metricPO.getBlockQps());
		metricEntity.setExceptionQps(metricPO.getExceptionQps());
		metricEntity.setRt(metricPO.getRt());
		metricEntity.setCount(metricPO.getCount());

		return metricEntity;
	}

	private void doSave(InfluxDB influxDB, MetricEntity metric) {
		// 根据资源的请求类型（WEB、DUBBO）将其写入到不同的表中
		influxDB.write(Point.measurement(getMetricTableByResource(metric.getResource()))
				.time(metric.getTimestamp().getTime(), TimeUnit.MILLISECONDS).tag("app", metric.getApp())
				.tag("resource", metric.getResource()).addField("id", metric.getId())
				.addField("gmtCreate", metric.getGmtCreate().getTime())
				.addField("gmtModified", metric.getGmtModified().getTime()).addField("passQps", metric.getPassQps())
				.addField("successQps", metric.getSuccessQps()).addField("blockQps", metric.getBlockQps())
				.addField("exceptionQps", metric.getExceptionQps()).addField("rt", metric.getRt())
				.addField("count", metric.getCount()).addField("resourceCode", metric.getResourceCode()).build());

		StringBuilder delete = new StringBuilder();
		delete.append("delete from ").append(METRIC_MEASUREMENT_EACH_LAST).append(" where app='")
				.append(metric.getApp()).append("'").append(" and resource='").append(metric.getResource()).append("'");
		influxDB.query(new Query(delete.toString()));

		// 将每个应用的每个资源再单独到写入到一张表中，用于出TOP请求报表
		influxDB.write(Point.measurement(METRIC_MEASUREMENT_EACH_LAST)
				.time(metric.getTimestamp().getTime(), TimeUnit.MILLISECONDS).tag("app", metric.getApp())
				.tag("resource", metric.getResource())
				.tag("requestType", MetricUtil.getRequestTypeByResource(metric.getResource())).addField("id", metric.getId())
				.addField("gmtCreate", metric.getGmtCreate().getTime())
				.addField("gmtModified", metric.getGmtModified().getTime()).addField("passQps", metric.getPassQps())
				.addField("successQps", metric.getSuccessQps()).addField("blockQps", metric.getBlockQps())
				.addField("exceptionQps", metric.getExceptionQps()).addField("rt", metric.getRt())
				.addField("count", metric.getCount()).addField("resourceCode", metric.getResourceCode()).build());
	}
}
