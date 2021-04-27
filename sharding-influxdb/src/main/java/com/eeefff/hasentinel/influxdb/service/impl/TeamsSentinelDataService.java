package com.eeefff.hasentinel.influxdb.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.eeefff.hasentinel.common.config.HASentineConfigProperties;
import com.eeefff.hasentinel.influxdb.constant.HttpConstant;
import com.eeefff.hasentinel.influxdb.entity.Metric;
import com.eeefff.hasentinel.influxdb.service.SentinelDataService;
import com.eeefff.hasentinel.influxdb.util.HttpClientUtil;
import com.eeefff.hasentinel.influxdb.util.sqlparser.SqlMeta;
import com.eeefff.hasentinel.influxdb.vo.Result;

import lombok.extern.slf4j.Slf4j;

/**
 * 获取所有接入Sentinel Service的团队名称
 * 
 * @author fenglibin
 *
 */
@Service(value = "teamsSentinelDataService")
@Slf4j
public class TeamsSentinelDataService implements SentinelDataService {
	@Autowired
	private HASentineConfigProperties sentineConfigProperties;

	@Override
	public String getServiceName() {
		return "all_teams";
	}

	@Override
	public Metric queryMetricFromSentinelServer(Integer page, Integer size, SqlMeta sqlMeta) {
		// 获取组装好字段的Metric对象
		Metric metric = Metric.getEmptyMetricWithColumns(
				sqlMeta.getFiledNames().toArray(new String[sqlMeta.getFiledNames().size()]));

		// 获取Sentinel Server的地址
		String sentinelServer = sentineConfigProperties.getSentinelServer();
		// 拼装访问的URL
		StringBuilder url = new StringBuilder();
		if (!sentinelServer.startsWith(HttpConstant.HTTP_PREFIX)) {
			url.append(HttpConstant.HTTP_PREFIX);
		}
		url.append(sentinelServer).append("/app/getAppTeams.json");
		log.info("Query url:" + url);
		String result = HttpClientUtil.doGet(url.toString());
		Result<List<String>> httpResult = JSON.parseObject(result, new TypeReference<Result<List<String>>>() {
		});
		if (httpResult == null || CollectionUtils.isEmpty(httpResult.getData())) {
			return metric;
		}

		httpResult.getData().forEach(team -> {
			// 组装结果响应数据
			String[] columns = metric.getResults().get(0).getSeries().get(0).getColumns();
			Object[] objs = new Object[columns.length];
			objs[0] = 0;
			objs[1] = team;
			metric.getResults().get(0).getSeries().get(0).getValues().add(objs);
		});

		return metric;
	}

}
