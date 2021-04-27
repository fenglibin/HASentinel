package com.alibaba.csp.sentinel.dashboard.util;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.annotation.PostConstruct;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.alibaba.csp.sentinel.dashboard.repository.metric.InfluxDBWrapper;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class InfluxDBConnecionPool {

	private static int metricHandlerThreads;

	private static String url;

	private static String[] urlsArr = null;

	private static String username;

	private static String password;

	@Value("${metric.async.handler.threads:2}")
	public void setMetricHandlerThreads(int metricHandlerThreads) {
		InfluxDBConnecionPool.metricHandlerThreads = metricHandlerThreads;
	}

	// url可能是多个url，以英文的逗号","分隔
	@Value("${influxdb.url}")
	public void setUrl(String url) {
		InfluxDBConnecionPool.url = url;
	}

	@Value("${influxdb.username}")
	public void setUsername(String username) {
		InfluxDBConnecionPool.username = username;
	}

	@Value("${influxdb.password}")
	public void setPassword(String password) {
		InfluxDBConnecionPool.password = password;
	}

	static Map<String, ConcurrentLinkedQueue<InfluxDB>> queues = new HashMap<String, ConcurrentLinkedQueue<InfluxDB>>();

	/**
	 * 初使化连接池
	 */
	@PostConstruct
	public void initConnection() {
		if (url != null && url.trim().length() > 0) {
			urlsArr = url.split(",");
			for (String url : urlsArr) {
				InfluxDBConnecionPool.initConnection(url);
			}
		}
	}

	/**
	 * 根据Influxdb的ＵＲＬ初使化连接池
	 * 
	 * @param url
	 */
	private static void initConnection(String url) {
		synchronized (InfluxDBConnecionPool.class) {
			new Thread() {
				public void run() {
					log.info("开始创建Url：" + url + "的" + metricHandlerThreads + "个连接池...");
					ConcurrentLinkedQueue<InfluxDB> queue = queues.get(url);
					if (queue == null) {
						queues.put(url, new ConcurrentLinkedQueue<InfluxDB>());
					}
					for (int i = 0; i < metricHandlerThreads; i++) {
						if (url.startsWith("https")) {
							queues.get(url).add(InfluxDBFactoryHttps.connect(url, username, password));
						} else {
							queues.get(url).add(InfluxDBFactory.connect(url, username, password));
						}
					}
					log.info("完成创建Url：" + url + "的" + metricHandlerThreads + "个连接池.");
				}
			}.start();
		}
	}

	/**
	 * 获取一个连接，首先从连接池中获取，如果没有也创建，然后再异常创建连接池
	 * 
	 * @param url
	 * @param username
	 * @param password
	 * @return
	 */
	public static InfluxDBWrapper connect(final String url, final String username, final String password) {
		ConcurrentLinkedQueue<InfluxDB> queue = queues.get(url);
		InfluxDB influxDb = null;
		if (queue != null) {
			influxDb = queue.poll();
		}
		if (influxDb == null) {
			log.info("从连接池中未获取到url:" + url + "　的连接，创建一个新连接");
			if (url.startsWith("https")) {
				influxDb = InfluxDBFactoryHttps.connect(url, username, password);
			} else {
				influxDb = InfluxDBFactory.connect(url, username, password);
			}
			initConnection(url);
		}
		return new InfluxDBWrapper(url, influxDb);
	}

	/**
	 * 将使用的连接还回连接池中
	 * 
	 * @param url
	 * @param influxDb
	 */
	public static void returnConnection(String url, InfluxDB influxDb) {
		queues.get(url).add(influxDb);
	}

}
