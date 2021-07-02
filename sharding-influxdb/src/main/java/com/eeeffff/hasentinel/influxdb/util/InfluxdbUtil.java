package com.eeeffff.hasentinel.influxdb.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class InfluxdbUtil {
	public static final String DEFAULT = "default";

	/**
	 * 从SQL中获取app字段的值，如果未传或获取失败，则返回default
	 * 
	 * @param sql
	 * @return 返回sql中app字段的值，如果未传或获取失败，则返回default
	 */
	public static String getAppFullName(String sql) {
		return getFiledValueFromSql(sql, "app");
	}

	/**
	 * 从查询条件中包括了app查询条件的SQL语句中，取出app条件对应的值。<br>
	 * Influxdb的值可以是正则表达式，可以同时传多个值，其值可以是如下的格式:<br>
	 * app_name：普通字符串代表的一个值<br>
	 * /^sentinel-webmvc-sample$/：正则表达式代表的一个值<br>
	 * /^(order-api|erp-ka|erp-crm|erp-es)$/：正则表达式代表的多个值<br>
	 * 
	 * @param sql
	 * @return 返回sql where条件中app参数对应的值，如果sql中没有app查询条件，则返回default
	 */
	public static List<String> getAppList(String sql) {
		List<String> appList = new ArrayList<String>();
		if (sql == null || sql.trim().length() == 0) {
			appList.add(DEFAULT);
			return appList;
		}
		if (sql.indexOf("\"app\"") < 0 && sql.indexOf(" app ") < 0 && sql.indexOf(" app=") < 0) {
			appList.add(DEFAULT);
			return appList;
		}
		String sqlLowerCase = sql.toLowerCase();
		if (!sqlLowerCase.startsWith("select")) {
			appList.add(DEFAULT);
			return appList;
		}
		try {
			int index = sqlLowerCase.indexOf("where");
			if (index < 0) {
				appList.add(DEFAULT);
				return appList;
			}
			sql = sql.substring(index);
			sqlLowerCase = sql.toLowerCase();
			if (sqlLowerCase.indexOf("\"app\"") > 0) {
				sql = sql.substring(sqlLowerCase.indexOf("\"app\""));
			} else if (sqlLowerCase.indexOf(" app ") > 0) {
				sql = sql.substring(sqlLowerCase.indexOf(" app "));
			} else if (sqlLowerCase.indexOf(" app=") > 0) {
				sql = sql.substring(sqlLowerCase.indexOf(" app="));
			}
			sqlLowerCase = sql.toLowerCase();

			String app = null;
			if (sql.indexOf("/^") >= 0) {
				sql = sql.substring(sqlLowerCase.indexOf("/^") + 2);
				sqlLowerCase = sql.toLowerCase();
				app = sql.substring(0, sqlLowerCase.indexOf("$/"));
			} else {
				sql = sql.substring(sqlLowerCase.indexOf("'") + 1);
				sqlLowerCase = sql.toLowerCase();
				app = sql.substring(0, sqlLowerCase.indexOf("'"));
			}

			if (app.startsWith("(") && app.endsWith(")")) {
				app = app.substring(app.indexOf("(") + 1, app.indexOf(")"));
				String[] appArr = app.split("\\|");
				for (String _app : appArr) {
					appList.add(_app);
				}
			} else {
				appList.add(app);
			}

			return appList;
		} catch (Exception e) {
			log.error("获取APP发生异常，SQL语句：" + sql, e);
		}
		appList.add(DEFAULT);
		return appList;
	}

	/**
	 * 从给定的SQL中获取app名称，并返回app名称与原始SQL的map
	 * 
	 * @param sqls
	 * @return
	 */
	public static Map<String, String> getAppSqlMap(String sqls) {
		if (sqls == null || sqls.trim().length() == 0) {
			return null;
		}
		Map<String, String> map = new HashMap<String, String>();
		String[] sqlArr = sqls.split(";");
		for (String sql : sqlArr) {
			List<String> appList = getAppList(sql);
			if (appList.size() == 0) {
				log.warn("不能够从SQL中解析出app:" + sql);
				continue;
			}
			if (appList.size() == 1) {
				map.put(appList.get(0), sql);
			} else {
				String appFullName = getAppFullName(sql);
				appList.forEach(app -> {
					map.put(app, sql.replace(appFullName, app));
				});
			}
		}
		return map;
	}

	/**
	 * 从SQL中获取指定字段的值，如果SQL中未包括指定字段，则返回default。<br>
	 * Influxdb SQL中字段的值可以是正则表达式，可以同时传多个值，其值可以是如下的格式:<br>
	 * １）普通字符串:如field_value<br>
	 * ２）代表单个值的正则表达式：/^field_value$/<br>
	 * ３）代表多个值的正则表达式：/^(order-api|erp-ka|erp-crm|erp-es)$/<br>
	 * 
	 * @param sql
	 * @return 返回sql where条件中app参数对应的值，如果sql中没有app查询条件，则返回default
	 */
	public static String getFiledValueFromSql(String sql, String fieldName) {
		if (sql == null || sql.trim().length() == 0) {
			return DEFAULT;
		}
		if (sql.indexOf("\"" + fieldName + "\"") < 0 && sql.indexOf(" " + fieldName + " ") < 0
				&& sql.indexOf(" " + fieldName + "=") < 0) {
			return DEFAULT;
		}
		String sqlLowerCase = sql.toLowerCase();
		// 非查询语句
		//if (!(sqlLowerCase.startsWith("select") || sqlLowerCase.startsWith("show tag values from"))) {
		//	return DEFAULT;
		//}
		try {
			// 没有带条件
			int index = sqlLowerCase.indexOf("where");
			if (index < 0) {
				return DEFAULT;
			}
			sql = sql.substring(index);
			sqlLowerCase = sql.toLowerCase();
			if (sqlLowerCase.indexOf("\"" + fieldName + "\"") > 0) {
				sql = sql.substring(sqlLowerCase.indexOf("\"" + fieldName + "\""));
			} else if (sqlLowerCase.indexOf(" " + fieldName + " ") > 0) {
				sql = sql.substring(sqlLowerCase.indexOf(" " + fieldName + " "));
			} else if (sqlLowerCase.indexOf(" " + fieldName + "=") > 0) {
				sql = sql.substring(sqlLowerCase.indexOf(" " + fieldName + "="));
			}
			sqlLowerCase = sql.toLowerCase();

			String value = null;
			if (sql.indexOf("/^") >= 0) {// 正则表达式
				sql = sql.substring(sqlLowerCase.indexOf("/^") + 2);
				sqlLowerCase = sql.toLowerCase();
				value = sql.substring(0, sqlLowerCase.indexOf("$/"));
			} else {
				sql = sql.substring(sqlLowerCase.indexOf("'") + 1);
				sqlLowerCase = sql.toLowerCase();
				value = sql.substring(0, sqlLowerCase.indexOf("'"));
			}
			return value;
		} catch (Exception e) {
			log.error("从SQL:" + sql + "中获取字段" + fieldName + "的值发生异常：" + e.getMessage(), e);
		}
		return DEFAULT;
	}
}
