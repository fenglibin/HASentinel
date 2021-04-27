package com.eeefff.hasentinel.influxdb.util;

import org.junit.Test;

import com.eeefff.hasentinel.influxdb.util.sqlparser.SqlMeta;
import com.eeefff.hasentinel.influxdb.util.sqlparser.SqlParserUtil;

public class SqlParserUtilTest {
	@Test
	public void testParse() {
		String sql = "SELECT sum(\"successQps\") FROM \"sentinel_metric_web\" WHERE (\"app\" =~ /^erp-crm$/) AND time >= now() - 6h GROUP BY time(1s) fill(null);";
		SqlMeta meta = SqlParserUtil.parse(sql);

	}
}
