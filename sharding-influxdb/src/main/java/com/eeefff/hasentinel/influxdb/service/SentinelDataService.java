package com.eeefff.hasentinel.influxdb.service;

import java.util.HashMap;
import java.util.Map;

import com.eeefff.hasentinel.influxdb.entity.Metric;
import com.eeefff.hasentinel.influxdb.util.sqlparser.SqlMeta;

/**
 * 调用Sentinel Server接口获取数据的服务接口
 * 
 * @author fenglibin
 *
 */
public interface SentinelDataService {
	/**
	 * 保存服务的Map，启动的时候自动将实现的子类加到该map中
	 */
	public static Map<String, SentinelDataService> services = new HashMap<String, SentinelDataService>();

	/**
	 * 获取服务的名称，注：其值与从Grafana中传过来的Influxdb　SQL中的表名相匹配
	 * 
	 * @return
	 */
	public String getServiceName();

	/**
	 * 从SentinelServer查询Metric属性，每个{@link MetricEntity}对象的所有属性都包括
	 * 
	 * @param page 当前查询的页数，默认为第一页
	 * @param size 每页Reousrce的数量，默认为100
	 * @param sqlMeta 根据查询语句组装出来的{@link SqlMeta}对象
	 * @return 符合Grafana规范的{@link Metric}对象
	 */
	public Metric queryMetricFromSentinelServer(Integer page, Integer size, SqlMeta sqlMeta);

}
