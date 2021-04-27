package com.alibaba.csp.sentinel.dashboard.repository.metric;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * Metric保存队列及定时从队列中获取数据并保存到Influxdb中
 * 
 * @author fenglibin
 *
 */
@Component
@Slf4j
public class MetricsHandler {
	// 用于将Metric写到Influxdb的线程数，也是创建Influxdb连接数的数量
	@Value("${metric.async.handler.threads:2}")
	private int metricHandlerThreads;
	// 队列中最多存储的metric数量，超过这个量的时候就会将队列中的metric清除掉，以免内存爆掉
	@Value("${metric.async.critical.queenNums:5000}")
	private int metricCriticalQueenNums;
	// 无数据时暂停的时间，单位为毫秒
	@Value("${metric.async.nometric.sleepTime:100}")
	private int SLEEP_TIME;
	// 是否允许增加Metric
	private boolean addMetric = true;
	// 标识是否允许手动操作
	private boolean manualWrite = false;
	// 标识是否开启记录每个接口最近的消耗时间，同时会删除原来的数据，因而会执行删除数据的操作，这个比较耗时，默认为关闭了该功能
	@Value("${metric.enable.topTimeReport:#{false}}")
	private boolean metricEnableTopTimeReport;
	// 标识是否开启打印数据写入Influxdb及Redis的时间到日志文件中
	@Value("${metric.async.logWriteTime:#{false}}")
	private boolean metricAsyncLogWriteTime;

	static Queue<MetricSaveAction> queue = new ConcurrentLinkedQueue<MetricSaveAction>();
	MetricCount metricCount = new MetricCount();

	@PostConstruct
	public void init() {
		metricCount.setThreads(metricHandlerThreads);
		for (int i = 0; i < metricHandlerThreads; i++) {
			new Thread() {
				@SuppressWarnings("synthetic-access")
				public void run() {
					try {
						while (true) {
							// 先保命:如果队列中待处理的数据过多，清除原来的数据
							if (queue.size() > metricCriticalQueenNums) {
								queue.clear();
								haveARest();
								continue;
							}
							MetricSaveAction action = queue.poll();
							if (action == null) {
								haveARest();
								continue;
							}
							metricCount.incrNums();
							long handStart = System.currentTimeMillis();
							action.doSave(metricEnableTopTimeReport);
							metricCount.addTimes(System.currentTimeMillis() - handStart);
						}
					} catch (Exception e) {
						log.error("Save Metric Timer error:", e);
					}
				}
			}.start();
		}
	}

	void haveARest() {
		try {
			Thread.currentThread().sleep(SLEEP_TIME);
		} catch (InterruptedException e) {
		}
	}

	/**
	 * 增加对象到队列中
	 * 
	 * @param action
	 */
	public void addMetric(MetricSaveAction action) {
		if (addMetric) {
			queue.add(action);
		}
	}

	/**
	 * 弹出一个对象处理
	 * 
	 * @return
	 */
	public MetricSaveAction poll() {
		return queue.poll();
	}

	/**
	 * 获取当前队列中数据的排除数量
	 * 
	 * @return
	 */
	public int getQueueSize() {
		return queue.size();
	}

	public void cleanQueue() {
		queue.clear();
	}

	public void startManualWrite() {
		while (manualWrite) {
			// 先保命
			MetricSaveAction action = queue.poll();
			if (action == null) {
				break;
			}
			metricCount.incrNums();
			long handStart = System.currentTimeMillis();
			action.doSave(metricEnableTopTimeReport);
			metricCount.addTimes(System.currentTimeMillis() - handStart);
		}
	}

	public MetricCount getMetricCount() {
		metricCount.setQueueNums(queue.size());
		return metricCount;
	}

	public boolean setAddMetric(boolean addMetric) {
		this.addMetric = addMetric;
		return true;
	}

	public boolean setManualWrite(boolean manualWrite) {
		this.manualWrite = manualWrite;
		return true;
	}

	public boolean isMetricEnableTopTimeReport() {
		return metricEnableTopTimeReport;
	}

	public boolean setMetricEnableTopTimeReport(boolean metricEnableTopTimeReport) {
		this.metricEnableTopTimeReport = metricEnableTopTimeReport;
		return true;
	}

	public boolean isMetricAsyncLogWriteTime() {
		return metricAsyncLogWriteTime;
	}

	public void setMetricAsyncLogWriteTime(boolean metricAsyncLogWriteTime) {
		this.metricAsyncLogWriteTime = metricAsyncLogWriteTime;
	}
	
}
