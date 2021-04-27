package com.alibaba.csp.sentinel.dashboard.util;

import java.util.List;

import org.influxdb.InfluxDB;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.influxdb.impl.InfluxDBResultMapper;
import org.junit.Test;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.MetricWebPO;

public class InfluxHttpsTest {
	private static InfluxDBResultMapper resultMapper = new InfluxDBResultMapper();

	@Test
	public void testHttpsConnectionTest() {
		String url = "https://192.168.10.133:8086";
		String username = "admin";
		String password = "admin";
		String database = "sentinel_db";
		String query = "select * from sentinel_metric_web limit 10;";
		InfluxDB influxDB = InfluxDBFactoryHttps.connect(url, username, password);
		influxDB.setDatabase(database);

		QueryResult result = influxDB.query(new Query(query));

		List<MetricWebPO> list = resultMapper.toPOJO(result, MetricWebPO.class);
		System.out.println(list.size());
	}

	@Test
	public void testHttpsConnectionPrd() {
		String url = "https://ts-2ze27qn5g49c7pzw1.influxdata.tsdb.aliyuncs.com:8086";
		// 这台本地访问不通
		url = "https://ts-2zee5d82x3z5eb076.influxdata.tsdb.aliyuncs.com:8086";
		url = "https://ts-2ze27qn5g49c7pzw1.influxdata.tsdb.aliyuncs.com:8086";
		String username = "influxdb_rw";
		String password = "a&jkuYJimax7noeuhc5l";
		String database = "sentinel_db";
		String query = "select * from sentinel_metric_web limit 10;";
		InfluxDB influxDB = InfluxDBFactoryHttps.connect(url, username, password);
		influxDB.setDatabase(database);

		QueryResult result = influxDB.query(new Query(query));

		List<MetricWebPO> list = resultMapper.toPOJO(result, MetricWebPO.class);
		System.out.println(list.size());
	}
}
