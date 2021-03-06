package com.alibaba.csp.sentinel.dashboard.repository.metric;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang.time.DateFormatUtils;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import com.alibaba.csp.sentinel.dashboard.config.RedisConfig.CacheTimeConfig;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.MetricDubboPO;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.MetricEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.MetricOtherPO;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.MetricPO;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.MetricWebPO;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.vo.MetricEntityVO;
import com.alibaba.csp.sentinel.dashboard.domain.vo.MetricType;
import com.alibaba.csp.sentinel.dashboard.service.app.AppService;
import com.alibaba.csp.sentinel.dashboard.util.InfluxShardingDBUtils;
import com.alibaba.csp.sentinel.dashboard.util.MD5Util;
import com.alibaba.csp.sentinel.dashboard.util.MetricUtil;
import com.alibaba.csp.sentinel.dashboard.util.UniqUtil;
import com.alibaba.csp.sentinel.dashboard.wrapper.redis.LastResourceRedisKey;
import com.alibaba.csp.sentinel.dashboard.wrapper.redis.RedisTemplateWrapper;
import com.alibaba.csp.sentinel.util.StringUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * metrics??????InfluxDB????????????
 * 
 * @author fenglibin
 */
@Slf4j
@Repository("influxShardingDBMetricsRepository")
public class InfluxSharingDBMetricsRepository implements MetricsRepository<MetricEntity>,ApplicationContextAware {

	/** ???????????? */
	private static final String DATE_FORMAT_PATTERN = "yyyy-MM-dd HH:mm:ss.SSS";

	/** ??????????????????UTC??????8?????? UTC: Universal Time Coordinated,?????????????????? */
	private static final Integer UTC_8 = 8;

	@Autowired 
	MetricsHandler metricsHandler;

	@Value("${metric.async:#{true}}")
	private boolean metricAsync;

	// ??????App???Resource??????????????????????????????????????????300???
	@Autowired
	private CacheTimeConfig cacheTimeConfig;
	
	@Autowired
	private AppService appService;
	
	private ApplicationContext applicationContext;

	@Override
	public void save(MetricEntity metric) {
		if (metric == null || StringUtil.isBlank(metric.getApp())) {
			return;
		}

		InfluxShardingDBUtils.insert(metric.getApp(), SENTINEL_DATABASE,
				new InfluxShardingDBUtils.InfluxDBInsertCallback() {
					@Override
					public void doCallBack(String database, InfluxDBWrapper influxDBWrapper) {
						if (metric.getId() == null) {
							metric.setId(System.currentTimeMillis());
						}
						doSave(influxDBWrapper, metric);
					}
				});
	}

	@Override
	public void saveAll(Iterable<MetricEntity> metrics) {
		if (metrics == null) {
			return;
		}

		Iterator<MetricEntity> iterator = metrics.iterator();

		while (iterator.hasNext()) {
			final MetricEntity metric = iterator.next();
			InfluxShardingDBUtils.insert(metric.getApp(), SENTINEL_DATABASE,
					new InfluxShardingDBUtils.InfluxDBInsertCallback() {
						@Override
						public void doCallBack(String database, InfluxDBWrapper influxDBWrapper) {
							if (metric.getId() == null) {
								metric.setId(System.currentTimeMillis());
							}
							doSave(influxDBWrapper, metric);
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

		// ???????????????????????????????????????8???????????????influxdb????????????UTC????????????????????????UTC?????????8?????????
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

	@SuppressWarnings("unchecked")
	@Override
	public List<String> listResourcesOfApp(String app) {
		final List<String> result = new ArrayList<String>();
		RedisTemplateWrapper.getAndConsumeValue(app, v -> {
			Optional.ofNullable(v).ifPresent(v1 -> {
				result.addAll((List<String>) v1);
			});
		});
		if (!CollectionUtils.isEmpty(result)) {
			return result;
		}
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
		RedisTemplateWrapper.set(app, result, cacheTimeConfig.getAppResource(), TimeUnit.SECONDS);
		return result;
	}

	private List<String> listResourcesOfApp(String app, String metricTable) {
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

		// ??????????????????????????????8???????????????influxdb????????????UTC????????????????????????UTC?????????8?????????
		startTime = startTime - UTC_8 * 60 * 60 * 1000;
		// ???5?????????????????????
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
				oldEntity.addCount(metricEntity.getCount());
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
		metricEntity.setGmtCreate(metricPO.getGmtCreate() == null ? null : new Date(metricPO.getGmtCreate()));
		metricEntity.setGmtModified(metricPO.getGmtModified() == null ? null : new Date(metricPO.getGmtModified()));
		metricEntity.setApp(metricPO.getApp());
		metricEntity.setTimestamp(metricPO.getTime() == null ? null : Date.from(metricPO.getTime()));
		metricEntity.setResource(metricPO.getResource());
		metricEntity.setPassQps(metricPO.getPassQps());
		metricEntity.setSuccessQps(metricPO.getSuccessQps());
		metricEntity.setBlockQps(metricPO.getBlockQps());
		metricEntity.setExceptionQps(metricPO.getExceptionQps());
		metricEntity.setRt(metricPO.getRt());
		metricEntity.setCount(metricPO.getCount());

		return metricEntity;
	}

	/**
	 * ??????????????????????????????WEB???DUBBO?????????????????????????????????????????????????????????????????????????????????????????????????????????
	 * 
	 * @param influxDBWrapper
	 * @param metric
	 */
	private void doSave(InfluxDBWrapper influxDBWrapper, MetricEntity metric) {
		MetricSaveAction action = new MetricSaveAction() {
			public void doSave() {
				try {
					String currentThreadName = Thread.currentThread().getName();
					String table = getMetricTableByResource(metric.getResource());
					long start = 0;

					// ??????????????????????????????WEB???DUBBO?????????????????????????????????
					// ???????????????????????????????????????????????????????????????????????????TOP????????????
					BatchPoints batchPoints = BatchPoints.builder().point(Point.measurement(table)
							.time(metric.getTimestamp().getTime(), TimeUnit.MILLISECONDS)
							.tag("app", metric.getApp())
							.tag("resource", metric.getResource())
							.tag("uniqKey", UniqUtil.getUniq32Key())
							.addField("appName", metric.getApp())
							.addField("resourceName", metric.getResource())
							.addField("id", metric.getId())
							.addField("gmtCreate", metric.getGmtCreate().getTime())
							.addField("gmtModified", metric.getGmtModified().getTime())
							.addField("passQps", metric.getPassQps())
							.addField("successQps", metric.getSuccessQps())
							.addField("blockQps", metric.getBlockQps())
							.addField("exceptionQps", metric.getExceptionQps()).addField("rt", metric.getRt())
							.addField("count", metric.getCount()).addField("resourceCode", metric.getResourceCode())
							.build()).build();
					start = System.currentTimeMillis();
					influxDBWrapper.write(batchPoints);
					if (metricsHandler.isMetricAsyncLogWriteTime()) {
						log.info(currentThreadName + "-????????????Influxdb????????????:" + (System.currentTimeMillis() - start));
					}
					start = System.currentTimeMillis();
					applicationContext.publishEvent(metric);
					if (metricsHandler.isMetricAsyncLogWriteTime()) {
						log.info(currentThreadName + "-????????????Redis????????????:" + (System.currentTimeMillis() - start));
					}
				} catch (Exception e) {
					log.error("Save metric exception happened:" + e.getMessage(), e);
				}
			}
		};
		if (metricAsync) {// ??????????????????????????????
			metricsHandler.addMetric(action);
		} else {// ??????????????????????????????
			action.doSave();
		}
		appService.addAppToCache(metric);
	}

	@Override
	public Set<TypedTuple<Object>> getLastResourceSortedMetric(int page, int size, String type,
			boolean withExpireData) {
		String key = null;
		if (withExpireData) {
			key = LastResourceRedisKey.LAST_RESOURCE_SORTED_METRIC_KEY_NOEXPIRE.getValue();
			if (MetricType.WEB.name().equals(type)) {
				key = LastResourceRedisKey.LAST_RESOURCE_SORTED_METRIC_KEY_WEB_NOEXPIRE.getValue();
			} else if (MetricType.DUBBO.name().equals(type)) {
				key = LastResourceRedisKey.LAST_RESOURCE_SORTED_METRIC_KEY_DUBBO_NOEXPIRE.getValue();
			} else if (MetricType.OTHER.name().equals(type)) {
				key = LastResourceRedisKey.LAST_RESOURCE_SORTED_METRIC_KEY_OTHER_NOEXPIRE.getValue();
			}
		} else {
			key = LastResourceRedisKey.LAST_RESOURCE_SORTED_METRIC_KEY.getValue();
			if (MetricType.WEB.name().equals(type)) {
				key = LastResourceRedisKey.LAST_RESOURCE_SORTED_METRIC_KEY_WEB.getValue();
			} else if (MetricType.DUBBO.name().equals(type)) {
				key = LastResourceRedisKey.LAST_RESOURCE_SORTED_METRIC_KEY_DUBBO.getValue();
			} else if (MetricType.OTHER.name().equals(type)) {
				key = LastResourceRedisKey.LAST_RESOURCE_SORTED_METRIC_KEY_OTHER.getValue();
			}
		}
		Set<TypedTuple<Object>> set = RedisTemplateWrapper.getZSet(key, page, size);
		return set;
	}

	@Override
	public List<Object> getLastResourceSortedMetricAll(int page, int size, String type, boolean withExpireData) {
		Set<TypedTuple<Object>> set = getLastResourceSortedMetric(page, size, type, withExpireData);
		if (set == null) {
			return null;
		}
		List<Object> hashKeysList = new ArrayList<Object>();
		set.forEach(t -> {
			MetricEntityVO value = (MetricEntityVO) t.getValue();
			hashKeysList.add(MD5Util.md5Of32(value.getApp() + value.getResource()));
		});
		String key = null;
		if (withExpireData) {
			key = LastResourceRedisKey.LAST_RESOURCE_ALL_METRIC_KEY_NOEXPIRE.getValue();
		} else {
			key = LastResourceRedisKey.LAST_RESOURCE_ALL_METRIC_KEY.getValue();
		}
		// List????????????Object??????????????????MetricEntity
		List<Object> result = RedisTemplateWrapper.hGet(key, hashKeysList);
		if (!CollectionUtils.isEmpty(result)) {
			// ???????????????hashKey????????????MetricEntity???null??????
			result = result.stream().filter(new Predicate<Object>() {
				@Override
				public boolean test(Object t) {
					if (t != null) {
						return true;
					}
					return false;
				}

			}).collect(Collectors.toList());
		}
		// ???????????????hashKey?????????????????????????????????????????????????????????????????????????????????hashKey???????????????????????????
		// ????????????????????????????????????hashKey???????????????????????????????????????????????????hashKey???????????????zset????????????????????????
		if (hashKeysList.size() != result.size()) {
			log.warn("??????hashKey?????????" + hashKeysList.size() + " ???hashset?????????????????????:" + result.size()
					+ "?????????????????????????????????????????????zset????????????????????????????????????");
			if (delNotExistMetric(set, type)) {
				result = getLastResourceSortedMetricAll(page, size, type, withExpireData);
			}
		}
		if (!withExpireData) {
			// ?????????????????????????????????????????????????????????????????????
			if (delExpiredMetric(result, type)) {
				result = getLastResourceSortedMetricAll(page, size, type, withExpireData);
			}
		}
		return result;
	};

	/**
	 * ???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
	 * 
	 * @param result
	 * @param type
	 * @return
	 */
	private boolean delExpiredMetric(List<Object> result, String type) {
		if (CollectionUtils.isEmpty(result)) {
			return false;
		}
		List<MetricEntity> needDelMetricList = new ArrayList<MetricEntity>();
		long now = System.currentTimeMillis();
		result.forEach(o -> {
			MetricEntity m = (MetricEntity) o;
			// ??????key???????????????????????????????????????
			if (m != null && m.getTimestamp() != null
					&& now - m.getTimestamp().getTime() > (cacheTimeConfig.getTopResource() * 1000)) {
				needDelMetricList.add(m);
			}
		});
		if (!CollectionUtils.isEmpty(needDelMetricList)) {
			List<String> delHashKeys = new ArrayList<String>();
			List<MetricEntityVO> delValues = new ArrayList<MetricEntityVO>();
			needDelMetricList.forEach(metric -> {
				delHashKeys.add(MD5Util.md5Of32(metric.getApp() + metric.getResource()));
				delValues.add(MetricEntityVO.builder().app(metric.getApp()).resource(metric.getResource()).build());
			});
			RedisTemplateWrapper.hDel(LastResourceRedisKey.LAST_RESOURCE_ALL_METRIC_KEY.getValue(),
					delHashKeys.toArray());
			zSetDelValue(type, delValues.toArray());
			return true;
		}
		return false;
	}

	/**
	 * ?????????????????????????????????????????????zset?????????????????????????????????????????????hashset???
	 * 
	 * @param set
	 * @param type
	 * @return
	 */
	private boolean delNotExistMetric(Set<TypedTuple<Object>> set, String type) {
		final StringBuilder delCheck = new StringBuilder();
		if (CollectionUtils.isEmpty(set)) {
			return false;
		}
		set.forEach(t -> {
			MetricEntityVO value = (MetricEntityVO) t.getValue();
			Object obj = RedisTemplateWrapper.hGet(LastResourceRedisKey.LAST_RESOURCE_ALL_METRIC_KEY.getValue(),
					MD5Util.md5Of32(value.getApp() + value.getResource()));
			if (obj == null) {
				zSetDelValue(type, value);
				delCheck.append("-");
			}
		});
		if (delCheck.length() > 0) {
			return true;
		}
		return false;
	}

	private void zSetDelValue(String type, Object... values) {
		log.warn("?????????" + LastResourceRedisKey.LAST_RESOURCE_SORTED_METRIC_KEY.getValue() + "?????????????????????" + values);
		RedisTemplateWrapper.zSetDel(LastResourceRedisKey.LAST_RESOURCE_SORTED_METRIC_KEY.getValue(), values);
		// ????????????????????????????????????????????????zset?????????
		if (MetricType.WEB.name().equals(type)) {
			RedisTemplateWrapper.zSetDel(LastResourceRedisKey.LAST_RESOURCE_SORTED_METRIC_KEY_WEB.getValue(), values);
		} else if (MetricType.DUBBO.name().equals(type)) {
			RedisTemplateWrapper.zSetDel(LastResourceRedisKey.LAST_RESOURCE_SORTED_METRIC_KEY_DUBBO.getValue(), values);
		} else if (MetricType.OTHER.name().equals(type)) {
			RedisTemplateWrapper.zSetDel(LastResourceRedisKey.LAST_RESOURCE_SORTED_METRIC_KEY_OTHER.getValue(), values);
		} else {// ???????????????????????????????????????????????????zset????????????????????????
			RedisTemplateWrapper.zSetDel(LastResourceRedisKey.LAST_RESOURCE_SORTED_METRIC_KEY_WEB.getValue(), values);
			RedisTemplateWrapper.zSetDel(LastResourceRedisKey.LAST_RESOURCE_SORTED_METRIC_KEY_DUBBO.getValue(), values);
			RedisTemplateWrapper.zSetDel(LastResourceRedisKey.LAST_RESOURCE_SORTED_METRIC_KEY_OTHER.getValue(), values);
		}
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
}
