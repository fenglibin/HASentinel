package com.alibaba.csp.sentinel.dashboard.repository.metric;

/**
 * Metric保存操作接口
 * 
 * @author fenglibin
 *
 */
public interface MetricSaveAction {
	/**
	 * 
	 * @param metricEnableTopTimeReport 标识是否开启记录每个接口最近的消耗时间，同时会删除原来的数据，因而会执行删除数据的操作，这个比较耗时，默认为关闭了该功能
	 */
	public void doSave(boolean metricEnableTopTimeReport);
}
