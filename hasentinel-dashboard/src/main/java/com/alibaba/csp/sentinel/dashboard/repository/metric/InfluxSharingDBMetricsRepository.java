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
import org.influxdb.dto.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import com.alibaba.csp.sentinel.dashboard.wrapper.redis.LastResourceRedisKey;
import com.alibaba.csp.sentinel.dashboard.wrapper.redis.RedisTemplateWrapper;
import com.alibaba.csp.sentinel.util.StringUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * metrics数据InfluxDB存储实现
 * 
 * @author cdfive
 * @date 2018-10-19
 */
@Slf4j
@Repository("influxShardingDBMetricsRepository")
public class InfluxSharingDBMetricsRepository implements MetricsRepository<MetricEntity> {

	/** 时间格式 */
	private static final String DATE_FORMAT_PATTERN = "yyyy-MM-dd HH:mm:ss.SSS";

	/** 北京时间领先UTC时间8小时 UTC: Universal Time Coordinated,世界统一时间 */
	private static final Integer UTC_8 = 8;

	@Autowired 
	MetricsHandler metricsHandler;

	@Value("${metric.async:#{true}}")
	private boolean metricAsync;

	// 单个App的Resource的缓存时间，单位为秒，默认为300秒
	@Autowired
	private CacheTimeConfig cacheTimeConfig;
	
	@Autowired
	private AppService appService;

	@Override
	public void save(MetricEntity metric) {
		if (metric == null || StringUtil.isBlank(metric.getApp())) {
			return;
		}

		InfluxShardingDBUtils.insert(metric.getApp(), SENTINEL_DATABASE,
				new InfluxShardingDBUtils.InfluxDBInsertCallback() {
					@SuppressWarnings("synthetic-access")
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
						@SuppressWarnings("synthetic-access")
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
	 * 根据资源的请求类型（WEB、DUBBO）将其写入到不同的表中，并且将每个应用的每个资源再单独到写入到一张表中
	 * 
	 * @param influxDBWrapper
	 * @param metric
	 */
	private void doSave(InfluxDBWrapper influxDBWrapper, MetricEntity metric) {
		MetricSaveAction action = new MetricSaveAction() {
			@SuppressWarnings("synthetic-access")
			public void doSave(boolean metricEnableTopTimeReport) {
				try {
					String currentThreadName = Thread.currentThread().getName();
					String table = getMetricTableByResource(metric.getResource());
					long start = 0;
					if (metricEnableTopTimeReport) {
						// influxdb中使用删除操作，其性能不好，暂时先去除该逻辑
						StringBuilder delete = new StringBuilder();
						delete.append("delete from ").append(METRIC_MEASUREMENT_EACH_LAST).append(" where app='")
								.append(metric.getApp()).append("'").append(" and resource='")
								.append(metric.getResource()).append("'");
						start = System.currentTimeMillis();
						influxDBWrapper.query(new Query(delete.toString()));
						log.info(currentThreadName + "-数据删除花费时间:" + (System.currentTimeMillis() - start));
					}

					// 根据资源的请求类型（WEB、DUBBO）将其写入到不同的表中
					// 将每个应用的每个资源再单独到写入到一张表中，用于出TOP请求报表
					BatchPoints batchPoints = BatchPoints.builder().point(Point.measurement(table)
							.time(metric.getTimestamp().getTime(), TimeUnit.MILLISECONDS).tag("app", metric.getApp())
							.tag("resource", metric.getResource()).addField("appName", metric.getApp())
							.addField("resourceName", metric.getResource()).addField("id", metric.getId())
							.addField("gmtCreate", metric.getGmtCreate().getTime())
							.addField("gmtModified", metric.getGmtModified().getTime())
							.addField("passQps", metric.getPassQps()).addField("successQps", metric.getSuccessQps())
							.addField("blockQps", metric.getBlockQps())
							.addField("exceptionQps", metric.getExceptionQps()).addField("rt", metric.getRt())
							.addField("count", metric.getCount()).addField("resourceCode", metric.getResourceCode())
							.build()).build();
					if (metricEnableTopTimeReport) {
						batchPoints.getPoints().add(Point.measurement(METRIC_MEASUREMENT_EACH_LAST)
								.time(metric.getTimestamp().getTime(), TimeUnit.MILLISECONDS)
								.tag("app", metric.getApp()).tag("resource", metric.getResource())
								.tag("requestType", MetricUtil.getRequestTypeByResource(metric.getResource()))
								.addField("appName", metric.getApp()).addField("resourceName", metric.getResource())
								.addField("id", metric.getId()).addField("gmtCreate", metric.getGmtCreate().getTime())
								.addField("gmtModified", metric.getGmtModified().getTime())
								.addField("passQps", metric.getPassQps()).addField("successQps", metric.getSuccessQps())
								.addField("blockQps", metric.getBlockQps())
								.addField("exceptionQps", metric.getExceptionQps()).addField("rt", metric.getRt())
								.addField("count", metric.getCount()).addField("resourceCode", metric.getResourceCode())
								.build());
					}
					start = System.currentTimeMillis();
					influxDBWrapper.write(batchPoints);
					if (metricsHandler.isMetricAsyncLogWriteTime()) {
						log.info(currentThreadName + "-数据写入Influxdb花费时间:" + (System.currentTimeMillis() - start));
					}
					start = System.currentTimeMillis();
					addLastResourceMetric(metric);
					if (metricsHandler.isMetricAsyncLogWriteTime()) {
						log.info(currentThreadName + "-数据写入Redis花费时间:" + (System.currentTimeMillis() - start));
					}
				} catch (Exception e) {
					log.error("Save metric exception happened:" + e.getMessage(), e);
				}
			}
		};
		if (metricAsync) {// 判断是否需要异步处理
			metricsHandler.addMetric(action);
		} else {// 如果不需要则同步处理
			action.doSave(metricsHandler.isMetricEnableTopTimeReport());
		}
		appService.addAppToCache(metric);
	}

	/**
	 * 将Metric加入到Redis中
	 * 
	 * @param metric
	 */
	public void addLastResourceMetric(MetricEntity metric) {
		String type = MetricUtil.getRequestTypeByResource(metric.getResource());
		// 将MetricEntityVO写入到redis的zset中，按耗时多少进行排序
		RedisTemplateWrapper.zSetAdd(LastResourceRedisKey.LAST_RESOURCE_SORTED_METRIC_KEY.getValue(),
				MetricEntityVO.builder().app(metric.getApp()).resource(metric.getResource()).build(),
				metric.getSuccessQps() > 0 ? metric.getRt() / metric.getSuccessQps() : metric.getRt());
		RedisTemplateWrapper.zSetAdd(LastResourceRedisKey.LAST_RESOURCE_SORTED_METRIC_KEY_NOEXPIRE.getValue(),
				MetricEntityVO.builder().app(metric.getApp()).resource(metric.getResource()).build(),
				metric.getSuccessQps() > 0 ? metric.getRt() / metric.getSuccessQps() : metric.getRt());
		if (MetricType.WEB.name().equals(type)) {
			RedisTemplateWrapper.zSetAdd(LastResourceRedisKey.LAST_RESOURCE_SORTED_METRIC_KEY_WEB.getValue(),
					MetricEntityVO.builder().app(metric.getApp()).resource(metric.getResource()).build(),
					metric.getSuccessQps() > 0 ? metric.getRt() / metric.getSuccessQps() : metric.getRt());
			RedisTemplateWrapper.zSetAdd(LastResourceRedisKey.LAST_RESOURCE_SORTED_METRIC_KEY_WEB_NOEXPIRE.getValue(),
					MetricEntityVO.builder().app(metric.getApp()).resource(metric.getResource()).build(),
					metric.getSuccessQps() > 0 ? metric.getRt() / metric.getSuccessQps() : metric.getRt());
		} else if (MetricType.DUBBO.name().equals(type)) {
			RedisTemplateWrapper.zSetAdd(LastResourceRedisKey.LAST_RESOURCE_SORTED_METRIC_KEY_DUBBO.getValue(),
					MetricEntityVO.builder().app(metric.getApp()).resource(metric.getResource()).build(),
					metric.getSuccessQps() > 0 ? metric.getRt() / metric.getSuccessQps() : metric.getRt());
			RedisTemplateWrapper.zSetAdd(LastResourceRedisKey.LAST_RESOURCE_SORTED_METRIC_KEY_DUBBO_NOEXPIRE.getValue(),
					MetricEntityVO.builder().app(metric.getApp()).resource(metric.getResource()).build(),
					metric.getSuccessQps() > 0 ? metric.getRt() / metric.getSuccessQps() : metric.getRt());
		} else if (MetricType.OTHER.name().equals(type)) {
			RedisTemplateWrapper.zSetAdd(LastResourceRedisKey.LAST_RESOURCE_SORTED_METRIC_KEY_OTHER.getValue(),
					MetricEntityVO.builder().app(metric.getApp()).resource(metric.getResource()).build(),
					metric.getSuccessQps() > 0 ? metric.getRt() / metric.getSuccessQps() : metric.getRt());
			RedisTemplateWrapper.zSetAdd(LastResourceRedisKey.LAST_RESOURCE_SORTED_METRIC_KEY_OTHER_NOEXPIRE.getValue(),
					MetricEntityVO.builder().app(metric.getApp()).resource(metric.getResource()).build(),
					metric.getSuccessQps() > 0 ? metric.getRt() / metric.getSuccessQps() : metric.getRt());
		}
		// 将每一份MetricEntiry都放入到HashSet中，以app+resource为Key，相同的MetricEntity为覆盖原来已经存在的
		RedisTemplateWrapper.hSetMetric(LastResourceRedisKey.LAST_RESOURCE_ALL_METRIC_KEY.getValue(),
				MD5Util.md5Of32(metric.getApp() + metric.getResource()), metric);
		RedisTemplateWrapper.hSetMetric(LastResourceRedisKey.LAST_RESOURCE_ALL_METRIC_KEY_NOEXPIRE.getValue(),
				MD5Util.md5Of32(metric.getApp() + metric.getResource()), metric);
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
		// List中的每个Object都代表着一个MetricEntity
		List<Object> result = RedisTemplateWrapper.hGet(key, hashKeysList);
		if (!CollectionUtils.isEmpty(result)) {
			// 过滤掉根据hashKey未获取到MetricEntity的null对象
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
		// 如果传入的hashKey列表的数量与得到的最终结果的数量不一致，表示某个或某些hashKey找到不对应的对象，
		// 但是不方便判断究竟是哪个hashKey没有取到值，这里通过循环判断每一个hashKey的情况，从zset中删除多余的对象
		if (hashKeysList.size() != result.size()) {
			log.warn("根据hashKey的数量" + hashKeysList.size() + " 从hashset中读取的资源为:" + result.size()
					+ "个，数量上不对等，以下会执行从zset中删除多余的数据的操作。");
			if (delNotExistMetric(set, type)) {
				result = getLastResourceSortedMetricAll(page, size, type, withExpireData);
			}
		}
		if (!withExpireData) {
			// 递归删除过期的对象，不展示超过有效期的请求数据
			if (delExpiredMetric(result, type)) {
				result = getLastResourceSortedMetricAll(page, size, type, withExpireData);
			}
		}
		return result;
	};

	/**
	 * 删除超过最大生存周期的对象，以避免在生成资源最后请求的报表时，展示了原来请求时间过久但是最近未被请求的资源
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
			// 检查key是否已经过了最大的存储周期
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
	 * 删除多余的数据，这部份只存在于zset中，但是不存在于保存全量数据的hashset中
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
		log.warn("开始从" + LastResourceRedisKey.LAST_RESOURCE_SORTED_METRIC_KEY.getValue() + "删除过期数据：" + values);
		RedisTemplateWrapper.zSetDel(LastResourceRedisKey.LAST_RESOURCE_SORTED_METRIC_KEY.getValue(), values);
		// 如果能够确实类型，则从指定类型的zset中删除
		if (MetricType.WEB.name().equals(type)) {
			RedisTemplateWrapper.zSetDel(LastResourceRedisKey.LAST_RESOURCE_SORTED_METRIC_KEY_WEB.getValue(), values);
		} else if (MetricType.DUBBO.name().equals(type)) {
			RedisTemplateWrapper.zSetDel(LastResourceRedisKey.LAST_RESOURCE_SORTED_METRIC_KEY_DUBBO.getValue(), values);
		} else if (MetricType.OTHER.name().equals(type)) {
			RedisTemplateWrapper.zSetDel(LastResourceRedisKey.LAST_RESOURCE_SORTED_METRIC_KEY_OTHER.getValue(), values);
		} else {// 如果不能够确实类型，则只能够所有的zset中去执行尝试删除
			RedisTemplateWrapper.zSetDel(LastResourceRedisKey.LAST_RESOURCE_SORTED_METRIC_KEY_WEB.getValue(), values);
			RedisTemplateWrapper.zSetDel(LastResourceRedisKey.LAST_RESOURCE_SORTED_METRIC_KEY_DUBBO.getValue(), values);
			RedisTemplateWrapper.zSetDel(LastResourceRedisKey.LAST_RESOURCE_SORTED_METRIC_KEY_OTHER.getValue(), values);
		}
	}
}
