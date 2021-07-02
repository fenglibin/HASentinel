package com.eeeffff.hasentinel.influxdb.util.sqlparser;

import java.util.List;
import java.util.Map;

import lombok.Builder;
import lombok.Data;
/**
 * SQL语句原数据解析类型
 * @author fenglibin
 *
 */
@Data
@Builder
public class SqlMeta {
	private List<String> filedNames;
	private String tableName;
	private Map<String,Object> whereKeyValues;
	private int limit;
}
