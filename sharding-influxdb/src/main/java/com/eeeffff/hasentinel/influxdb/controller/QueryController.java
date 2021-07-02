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
package com.eeeffff.hasentinel.influxdb.controller;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.SocketConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.eeeffff.hasentinel.influxdb.config.InfluxdbConfigProperties;
import com.eeeffff.hasentinel.influxdb.config.RedisConfig;
import com.eeeffff.hasentinel.influxdb.entity.Metric;
import com.eeeffff.hasentinel.influxdb.entity.Results;
import com.eeeffff.hasentinel.influxdb.entity.Series;
import com.eeeffff.hasentinel.influxdb.service.SentinelDataService;
import com.eeeffff.hasentinel.influxdb.sharding.InfluxdbShardingMapper;
import com.eeeffff.hasentinel.influxdb.util.CharsetUtil;
import com.eeeffff.hasentinel.influxdb.util.HttpClientUtil;
import com.eeeffff.hasentinel.influxdb.util.InfluxdbUtil;
import com.eeeffff.hasentinel.influxdb.util.MD5Util;
import com.eeeffff.hasentinel.influxdb.util.StringUtils;
import com.eeeffff.hasentinel.influxdb.util.sqlparser.SqlMeta;
import com.eeeffff.hasentinel.influxdb.util.sqlparser.SqlParserUtil;
import com.eeeffff.hasentinel.influxdb.vo.Result;
import com.eeeffff.hasentinel.influxdb.wrapper.redis.RedisTemplateWrapper;

import lombok.extern.log4j.Log4j;

/**
 * @author fenglibin
 */
@Log4j
@Controller
@RequestMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
public class QueryController {

	@Autowired
	private InfluxdbShardingMapper influxdbShardingMapper;
	@Autowired
	private InfluxdbConfigProperties influxdbConfigProperties;
	@Autowired
	private RedisConfig redisConfig;
	private CloseableHttpClient httpClient;
	private static final String SENTINEL_SERVER_DB = "sentinel_server";

	@PostConstruct
	public void initHttpClient() throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
		SocketConfig soConfig = SocketConfig.custom().setSoTimeout(30000).build();
		ConnectionConfig connConfig = ConnectionConfig.custom().build();
		RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(30000).setConnectTimeout(10000)
				.setSocketTimeout(30000).build();

		httpClient = HttpClients.custom().setRedirectStrategy(new DefaultRedirectStrategy() {
			@Override
			protected boolean isRedirectable(final String method) {
				return false;
			}
		}).setMaxConnTotal(4000).setMaxConnPerRoute(1000).setDefaultSocketConfig(soConfig)
				.setSSLSocketFactory(HttpClientUtil.getSelfTrustSSLConnectionSocketFactory())
				.setDefaultRequestConfig(requestConfig).setDefaultConnectionConfig(connConfig).build();
		HttpClientUtil.setHttpClient(httpClient);

	}

	@ResponseBody
	@RequestMapping("query")
	public Metric queryTopResourceMetric(@NotEmpty String db, @NotEmpty String u, @NotEmpty String p,
			@NotEmpty String q, String epoch, Integer page, Integer size, HttpServletRequest request) {
		if (SENTINEL_SERVER_DB.equals(db)) {// 通过Sentinel Server查询按响应时间排序的资源
			if(q.startsWith("SHOW")) {
				return Metric.getEmptyMetricWithColumns(null);
			}
			// SQL解析
			SqlMeta sqlMeta = SqlParserUtil.parse(q);
			if (sqlMeta == null) {
				return Metric.getEmptyMetricWithColumns(null);
			}
			String tableName = sqlMeta.getTableName();
			SentinelDataService sentinelDataService = SentinelDataService.services.get(tableName);
			if (sentinelDataService == null) {
				log.warn("根据从sql:"+q+"中获取的表名:"+tableName+"，找不到对应的SentinelDataService实现类！");
				return Metric.getEmptyMetricWithColumns(null);
			}
			return sentinelDataService.queryMetricFromSentinelServer(page, size, sqlMeta);
		} else {
			return queryFromInfluxdb(db, u, p, q, epoch, request);
		}
	}

	/**
	 * 从Influxdb中查询
	 * 
	 * @param db
	 * @param u
	 * @param p
	 * @param q
	 * @param epoch
	 * @param request
	 * @return
	 */
	private Metric queryFromInfluxdb(@NotNull String db, @NotNull String u, @NotNull String p, @NotNull String q,
			String epoch, HttpServletRequest request) {
		Metric metric = null;
		String cacheKey = MD5Util.md5Hex(q);
		// 判断是否是不带time条件查询语句
		boolean isNoTimeConditionQuery = (q.indexOf(" time") < 0);
		try {
			if (isNoTimeConditionQuery) {// 针对不带时间条件的查询，将这些数据放到缓存中，可以确保基础的配置数据访问的速度
				metric = (Metric) RedisTemplateWrapper.get(cacheKey);
				if (metric != null) {
					log.info("使用从缓存中获取到数据，对应的查询SQL为：" + q);
					return metric;
				}
			}

			// 拼接除查询SQL语句以外的参数
			StringBuilder params = new StringBuilder();
			Map<String, String[]> paramMap = request.getParameterMap();
			for (Map.Entry<String, String[]> param : paramMap.entrySet()) {
				String key = param.getKey();
				if ("q".equalsIgnoreCase(key)) {
					continue;
				}
				String[] values = param.getValue();
				String value = "";
				if (values != null && values.length > 0) {
					value = values[0];
					try {
						value = URLEncoder.encode(value, CharsetUtil.DEFAULT_CHARSET.name());
					} catch (UnsupportedEncodingException e) {
					}
				}
				// 拼接参数
				params.append(key).append("=").append(value).append("&");
			}

			String[] sqls = q.split(";");
			int index = -1;
			/**
			 * 一条查询语句中包含了多条SQL，将每条SQL进行分拆执行，因为每条SQL的app并不一定相同，<br>
			 * 即不同的SQL需要从不同的Influxdb中去执行数据查询，并从每条SQL中去获取App，进行sharding查询，SQL类似可以：<br>
			 * select f1 from t1 where app='app1';select f2 from t2 where "app" =~
			 * /^(app1,app2)$/ <br>
			 */
			for (String sql : sqls) {
				index++;

				int influxDbs = 0;
				// 一条SQL中，app参数传入的App可能是多个值，如类似"app" =~ /^(app1,app2)$/这样的正则表达式，
				// 由于Sharding规则基于app，因而需要将其转换为多条带不同app参数的SQL进行执行，如：
				// 将select f2 from t2 where "app" =~ /^(app1,app2)$/ 拆份成以下两条语句：
				// select f2 from t2 where "app" =~ /^app1$/ 及 select f2 from t2 where "app" =~
				// /^app2$/
				// 然后再分别执行后汇总结果
				Map<String, String> appSqlMap = InfluxdbUtil.getAppSqlMap(sql);
				if (appSqlMap.size() == 0) {
					continue;
				}
				for (Map.Entry<String, String> entry : appSqlMap.entrySet()) {
					// 应用名称
					String _app = entry.getKey();
					// 查询SQL
					String _sql = entry.getValue();
					try {
						_sql = URLEncoder.encode(_sql, CharsetUtil.DEFAULT_CHARSET.name());
					} catch (UnsupportedEncodingException e) {
					}
					String[] influxdbUrls = { "app" };// 随意设置的默认值，因为该数据需要根据情况而改变size，要在这里设置成公共的
					if (_app.equalsIgnoreCase(InfluxdbUtil.DEFAULT)) {// 没有在where条件中传入app参数，则从后端所有的库中获取数据
						influxdbUrls = influxdbConfigProperties.getUrlArr();
					} else {// 如果在where条件中传入app参数，则根据其值进行sharding，选择对应的后端influxdb
						influxdbUrls = new String[1];
						influxdbUrls[0] = influxdbShardingMapper.getUrl(_app);
					}
					// 对从不同的Influxdb数据源中查询的结果进行汇总（注：没有指定app时，就会遍历所有后端的数据源去查询，并做结果汇总）
					for (String influxdbUrl : influxdbUrls) {
						influxDbs++;
						String url = new StringBuilder().append(influxdbUrl).append("/query?").append(params)
								.append("q=").append(_sql).toString();
						String result = HttpClientUtil.doGet(url);
						log.info("Influxdb的请求url:" + url + " 的响应结果为:" + result);
						if (Objects.equals(StringUtils.EMPTY_STRING, result)) {
							continue;
						}
						Metric _metric = JSONObject.parseObject(result, Metric.class);
						if ((index > 0 || influxDbs > 1) && metric != null && _metric != null
								&& _metric.getResults() != null) {
							Results results = _metric.getResults().get(0);
							if (results.getSeries() == null || results.getSeries().size() == 0) {
								continue;
							}
							int currentIndexSize = metric.getResults().size();
							if (currentIndexSize > index) {// 表示已经存在有值了
								Results _result = metric.getResults().get(index);
								if (_result == null) {
									log.info("根据index:" + index + "找不到对应的结果.");
									results.setStatement_id(index);
									metric.getResults().add(results);
								}

								Series series = null;
								List<Series> seriesList = _result.getSeries();
								if (seriesList != null && seriesList.size() > 0) {
									series = _result.getSeries().get(0);
								}
								if (series == null) {
									_result.getSeries().add(results.getSeries().get(0));
								} else {
									List<Object[]> currentValues = series.getValues();
									List<Object[]> newValues = results.getSeries().get(0).getValues();
									currentValues.addAll(newValues);
									mergeSameTimeMetric(currentValues);
								}
							} else {
								results.setStatement_id(index);
								metric.getResults().add(results);
							}
						} else {
							metric = _metric;
						}
					}
				}
			}

		} catch (Exception e) {
			log.error("执行数据查询发生异常：" + e.getMessage(), e);
		}
		if (isNoTimeConditionQuery && metric != null && metric.getResults() != null) {
			RedisTemplateWrapper.set(cacheKey, metric, redisConfig.getCacheTime(), TimeUnit.SECONDS);
		}
		return metric;
	}

	/**
	 * 合同来自于不同应用相同时间的Metric，把他们的值相加。<br>
	 * 如erp-crm在17:36:27的qps metric值为464，erp-cdt在17:36:27的qps
	 * metric值为141，则它们合起来在17:36:27的qps metric值为464+141=605<br>
	 * 其它的类似相加
	 * 
	 * @param currentValues
	 */
	private void mergeSameTimeMetric(List<Object[]> currentValues) {
		Map<Long, Integer> map = new TreeMap<Long, Integer>();
		currentValues.forEach(oa -> {
			if (oa.length == 2) {
				String keyStr = String.valueOf(oa[0]);
				String valStr = String.valueOf(oa[1]);
				if (!(NumberUtils.isNumber(keyStr) && NumberUtils.isNumber(valStr))) {
					return;
				}
				Long key = Long.parseLong(keyStr);
				Integer value = oa[1] == null ? 0 : Integer.parseInt(valStr);
				if (map.get(key) == null) {
					map.put(key, value);
				} else {
					map.put(key, map.get(key) + value);
				}
			}
		});
		if (map.size() > 0) {
			currentValues.clear();
			map.entrySet().stream().sorted(new Comparator<Entry<Long, Integer>>() {
				@Override
				public int compare(Entry<Long, Integer> o1, Entry<Long, Integer> o2) {
					if (o1.getKey() > o2.getKey()) {
						return 1;
					} else if (o1.getKey() < o2.getKey()) {
						return -1;
					}
					return 0;
				}
			}).forEach(new Consumer<Entry<Long, Integer>>() {
				@Override
				public void accept(Entry<Long, Integer> t) {
					Object[] arr = { t.getKey(), t.getValue() };
					currentValues.add(arr);
				}

			});
		}
	}

	@ResponseBody
	@RequestMapping("getCacheValue")
	public Object getCacheValue(@NotEmpty String q) {
		String cacheKey = MD5Util.md5Hex(q);
		return RedisTemplateWrapper.get(cacheKey);
	}

	@ResponseBody
	@RequestMapping("cleanCacheValue")
	public Result<?> cleanCacheValue(@NotEmpty String q) {
		String cacheKey = MD5Util.md5Hex(q);
		RedisTemplateWrapper.del(cacheKey);
		return Result.ofSuccess("清除ＳＱＬ语句:" + q + " 缓存Key为:" + cacheKey + " 的缓存成功！");
	}
	
	@ResponseBody
	@RequestMapping(value="health", produces ="text/plain")
	public String health(@NotEmpty String q) {
		return "OK";
	}

}
