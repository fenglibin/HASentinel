package com.alibaba.csp.sentinel.dashboard.util;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.junit.Before;
import org.junit.Test;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.MetricEntity;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class InfluxShardingDBUtilsTest {
	private static String[] urlsArr = null;

	@Before
	public void before() {
		String url = "http://172.16.100.231:8086,http://172.16.100.231:8087,http://172.16.100.231:8088";
		if (url != null && url.trim().length() > 0) {
			urlsArr = url.split(",");
		}
	}

	@Test
	public void test() {
		String app = "sentinel-dashboard";
		app = "erp-statistic";
		app = "erp-crm-data";
		app = "sharding-influxdb";
		int shardingId = (urlsArr.length - 1) & app.hashCode();
		String url = urlsArr[shardingId];
		System.out.println(url);
	}

	@Test
	public void test_2() {
		int hashCode = -833253152;
		int index = 0;
		while (index <= 100) {
			index++;
			hashCode = Math.abs(hashCode);
			System.out.println(hashCode % 3);
			hashCode++;
		}
	}
	
	@Test
	public void testShardingId() {
		String app = "erp-es";
		int shardingId = Math.abs(app.hashCode()) % 3;
		System.out.println("Sharding Id of app is:"+shardingId);
	}
	
	@Test
	public void testWriteData() {
		MetricEntity metric = new MetricEntity();
		metric.setApp("sentinel-dashboard");
		metric.setAppName("sentinel-dashboard-name");
		metric.setAvgRt(1);
		metric.setBlockQps(0L);
		metric.setCount(1);
		metric.setExceptionQps(0L);
		metric.setGmtCreate(new Date());
		metric.setGmtModified(new Date());
		metric.setId(1L);
		metric.setPassQps(1L);
		metric.setResource("/metric/saveMetric.json");
		metric.setResourceName("name");
		metric.setRt(1L);
		metric.setRtAndSuccessQps(1, 1L);
		metric.setSuccessQps(1L);
		metric.setTeam("test-team");
		metric.setTimestamp(new Date());
		
		InfluxDB influxDB = getInfluxDB();
		influxDB.setDatabase("sentinel_db");
		for(int i=0;i<10;i++) {
			doSave(influxDB,metric);
		}
		influxDB.close();
	}
	
	private InfluxDB getInfluxDB() {
		return InfluxDBFactory.connect("http://127.0.0.1:8086", "admin", "");
	}
	
	private void doSave(InfluxDB influxDB,MetricEntity metric) {
		try {
			String currentThreadName = Thread.currentThread().getName();
			String table = "sentinel_metric_other";
			long start = 0;

			// 根据资源的请求类型（WEB、DUBBO）将其写入到不同的表中
			// 将每个应用的每个资源再单独到写入到一张表中，用于出TOP请求报表
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
			influxDB.write(batchPoints);
		} catch (Exception e) {
			log.error("Save metric exception happened:" + e.getMessage(), e);
		}
	}
}
