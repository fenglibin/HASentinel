package com.alibaba.csp.sentinel.dashboard.util;

public class ShardingTest {

	public static void main(String[] args) {
		String app = "crm_api";
		int shardingId = Math.abs(app.hashCode()) % 4;
		app = "crm_data";
		shardingId = Math.abs(app.hashCode()) % 4;
		System.out.println(app + "=" + app.hashCode() + "=" + shardingId);
		app = "crm_data1";
		shardingId = Math.abs(app.hashCode()) % 4;
		System.out.println(app + "=" + app.hashCode() + "=" + shardingId);
		app = "default_crm_data";
		shardingId = Math.abs(app.hashCode()) % 4;
		System.out.println(app + "=" + app.hashCode() + "=" + shardingId);
		app = "default_crm_data1";
		shardingId = Math.abs(app.hashCode()) % 4;
		System.out.println(app + "=" + app.hashCode() + "=" + shardingId);
		app = "xxx_api";
		shardingId = Math.abs(app.hashCode()) % 4;
		System.out.println(app + "=" + app.hashCode() + "=" + shardingId);
		app = "xxx_data";
		shardingId = Math.abs(app.hashCode()) % 4;
		System.out.println(app + "=" + app.hashCode() + "=" + shardingId);
		app = "default_xxx_api";
		shardingId = Math.abs(app.hashCode()) % 4;
		System.out.println(app + "=" + app.hashCode() + "=" + shardingId);
		app = "default_xxx_data";
		shardingId = Math.abs(app.hashCode()) % 4;
		System.out.println(app + "=" + app.hashCode() + "=" + shardingId);
	}

}
