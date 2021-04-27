package com.alibaba.csp.sentinel.dashboard.util;

import org.junit.Before;
import org.junit.Test;

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
}
