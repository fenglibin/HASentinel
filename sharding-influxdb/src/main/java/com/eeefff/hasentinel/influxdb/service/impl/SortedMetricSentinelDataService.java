package com.eeefff.hasentinel.influxdb.service.impl;

import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.eeefff.hasentinel.common.config.HASentineConfigProperties;
import com.eeefff.hasentinel.common.entity.MetricEntity;
import com.eeefff.hasentinel.influxdb.constant.HttpConstant;
import com.eeefff.hasentinel.influxdb.entity.Metric;
import com.eeefff.hasentinel.influxdb.service.SentinelDataService;
import com.eeefff.hasentinel.influxdb.util.HttpClientUtil;
import com.eeefff.hasentinel.influxdb.util.sqlparser.SqlMeta;
import com.eeefff.hasentinel.influxdb.vo.Result;

import lombok.extern.slf4j.Slf4j;

@Service(value = "sortedMetricSentinelDataService")
@Slf4j
public class SortedMetricSentinelDataService implements SentinelDataService {
	@Autowired
	private HASentineConfigProperties sentineConfigProperties;

	@Override
	public String getServiceName() {
		return "T";
	}

	@Override
	public Metric queryMetricFromSentinelServer(Integer page, Integer size, SqlMeta sqlMeta) {
		// 获取组装好字段的Metric对象
		Metric metric = Metric.getEmptyMetricWithColumns(
				sqlMeta.getFiledNames().toArray(new String[sqlMeta.getFiledNames().size()]));
		// 获取资源类型
		// 查询Reousrce的类型，type默认为空表示全部类型的资源，WEB表示只查询WEB类型的资源，DUBBO表示只查询DUBBO类型的资源，OTHER表示查询除WEB及DUBBO以外类型的资源
		String type = (String) sqlMeta.getWhereKeyValues().get("type");
		// 获取是否需要超期的数据
		boolean noExpire = (Boolean) Optional.ofNullable(sqlMeta.getWhereKeyValues().get("noExpire")).orElse(true);
		// 获取Sentinel Server的地址
		String sentinelServer = sentineConfigProperties.getSentinelServer();

		// 获取分页数据
		page = page == null ? 1 : page;
		size = size == null ? 100 : size;
		// 如果在SQL中指定了limit数据，则取sql中的指定的数据
		size = sqlMeta.getLimit() > 0 ? sqlMeta.getLimit() : size;

		// 拼装访问的URL
		StringBuilder url = new StringBuilder();
		if (!sentinelServer.startsWith(HttpConstant.HTTP_PREFIX)) {
			url.append(HttpConstant.HTTP_PREFIX);
		}
		url.append(sentinelServer).append("/metric/getLastResourceSortedMetricAll.json?page=").append(page)
				.append("&size=").append(size).append("&type=").append(type).append("&withExpireData=")
				.append(!noExpire);
		log.info("Query url:" + url);
		String result = HttpClientUtil.doGet(url.toString());

		Result<List<MetricEntity>> httpResult = JSON.parseObject(result,
				new TypeReference<Result<List<MetricEntity>>>() {
				});
		if (httpResult == null || CollectionUtils.isEmpty(httpResult.getData())) {
			return metric;
		}
		httpResult.getData().stream().filter(new Predicate<MetricEntity>() {
			// 过滤掉结果为空的记录
			@Override
			public boolean test(MetricEntity t) {
				if (t == null) {
					return false;
				}
				return true;
			}

		}).sorted(new Comparator<MetricEntity>() {
			// 按平均响应时间由高到低排序
			@Override
			public int compare(MetricEntity o1, MetricEntity o2) {
				if (o1 == null && o2 != null) {
					return 1;
				}
				if (o1 != null && o2 == null) {
					return -1;
				}
				if (o1 == null && o2 == null) {
					return 0;
				}
				if (o1.getAvgRt() < o2.getAvgRt()) {
					return 1;
				} else if (o1.getAvgRt() > o2.getAvgRt()) {
					return -1;
				}
				return 0;
			}
		}).forEach(v -> {
			// 组装结果响应数据
			String[] columns = metric.getResults().get(0).getSeries().get(0).getColumns();
			Object[] objs = new Object[columns.length];
			for (int index = 0; index < objs.length; index++) {
				try {

					if ("time".equals(columns[index])) {
						objs[index] = v.getTimestamp().getTime();
					} else {
						Field field = v.getClass().getDeclaredField(columns[index]);
						field.setAccessible(true);
						objs[index] = field.get(v);
					}
				} catch (Exception e) {
					log.error(e.toString(), e);
				}
			}
			metric.getResults().get(0).getSeries().get(0).getValues().add(objs);
		});
		return metric;
	}
}
