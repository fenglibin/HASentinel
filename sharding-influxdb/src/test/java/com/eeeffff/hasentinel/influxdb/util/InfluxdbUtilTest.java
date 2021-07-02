package com.eeeffff.hasentinel.influxdb.util;

import org.junit.Test;

import com.eeeffff.hasentinel.influxdb.util.InfluxdbUtil;

import org.junit.Assert;

public class InfluxdbUtilTest {
	@Test
	public void test1() {
		String sql="SELECT mean(\"successQps\") FROM \"sentinel_metric\" WHERE 1=2 and (\"app\" = 'hasentinel-webmvc-sample') AND time >= now() - 30m GROUP BY time(2s) fill(null);";
		String app = InfluxdbUtil.getAppFullName(sql);
		Assert.assertEquals("hasentinel-webmvc-sample", app);
	}
	
	@Test
	public void test2() {
		String sql="SELECT mean(\"successQps\") FROM \"sentinel_metric\" WHERE 1=2 and app = 'hasentinel-webmvc-sample') AND time >= now() - 30m GROUP BY time(2s) fill(null);";
		String app = InfluxdbUtil.getAppFullName(sql);
		Assert.assertEquals("hasentinel-webmvc-sample", app);
	}
	
	@Test
	public void test3() {
		String sql="SELECT mean(\"successQps\") FROM \"sentinel_metric\" WHERE 1=2 and app= 'hasentinel-webmvc-sample') AND time >= now() - 30m GROUP BY time(2s) fill(null);";
		String app = InfluxdbUtil.getAppFullName(sql);
		Assert.assertEquals("hasentinel-webmvc-sample", app);
	}
	
	@Test
	public void test4() {
		String sql="SELECT mean(\"successQps\") FROM \"sentinel_metric\" WHERE 1=2 and app='hasentinel-webmvc-sample') AND time >= now() - 30m GROUP BY time(2s) fill(null);";
		String app = InfluxdbUtil.getAppFullName(sql);
		Assert.assertEquals("hasentinel-webmvc-sample", app);
	}
	
	@Test
	public void test5() {
		String sql = "SELECT sum(\"successQps\") FROM \"sentinel_metric_web\" WHERE (\"app\" =~ /^hasentinel-webmvc-sample$/) AND time >= now() - 6h GROUP BY time(1s) fill(null);";
		String app = InfluxdbUtil.getAppFullName(sql);
		Assert.assertEquals("hasentinel-webmvc-sample", app);
	}
	
	@Test
	public void test6() {
		String sql = "SELECT sum(\"successQps\") FROM \"sentinel_metric_web\" WHERE (\"app\" =~ /^(ld-order-api|erp-ka|erp-crm|erp-es|sharding-influxdb|erp-config|erp-file-center|erp-statistic|erp-gateway)$/) AND time >= now() - 6h GROUP BY time(1s) fill(null);";
		String app = InfluxdbUtil.getAppFullName(sql);
		Assert.assertEquals("(ld-order-api|erp-ka|erp-crm|erp-es|sharding-influxdb|erp-config|erp-file-center|erp-statistic|erp-gateway)", app);
	}
	
	@Test
	public void test7() {
		String sql = "SHOW TAG VALUES FROM \"sentinel_metric_web\"+WITH+KEY+=+\"resource\"+WHERE+\"app\"+=~+/^erp-visit$/";
		String app = InfluxdbUtil.getAppFullName(sql);
		Assert.assertEquals("erp-visit", app);
	}
}
