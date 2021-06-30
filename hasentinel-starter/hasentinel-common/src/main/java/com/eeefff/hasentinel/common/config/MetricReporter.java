package com.eeefff.hasentinel.common.config;

import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.csp.sentinel.concurrent.NamedThreadFactory;
import com.alibaba.csp.sentinel.config.SentinelConfig;
import com.alibaba.csp.sentinel.util.StringUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * 收集当前应用的Metrix数据，然后进行上报
 * 
 * @author fenglibin
 *
 */
@Slf4j
@Component
public class MetricReporter {
	@Autowired
	private HASentineConfigProperties sentineConfigProperties;
	@Autowired
	private HASentinelConfig sentinelConfig;

	private CloseableHttpAsyncClient httpclient;

	public static final String NO_METRICS = "No metrics";
	private static final int HTTP_OK = 200;
	private final long intervalSecond = 7;
	private static final long MAX_LAST_FETCH_INTERVAL_MS = 1000 * 15;
	private static final long FETCH_INTERVAL_SECOND = 6;
	private final static String METRIC_URL_PATH = "metric";
	static final Charset DEFAULT_CHARSET = Charset.forName(SentinelConfig.charset());

	private ScheduledExecutorService fetchScheduleService;

	/**
	 * 初使化线程池及http连接池
	 */
	@PostConstruct
	public void init() {
		if (sentinelConfig.getProjectName() == null) {
			log.warn("当前应用未配置project.name，不会启用Metrix数据上报的任务！");
			return;
		}
		fetchScheduleService = Executors.newScheduledThreadPool(1,
				new NamedThreadFactory("sentinel-metrics-fetch-task for app:" + sentinelConfig.getProjectName(),true));
		
		IOReactorConfig ioConfig = IOReactorConfig.custom().setConnectTimeout(3000).setSoTimeout(3000)
				.setIoThreadCount(1).build();
		httpclient = HttpAsyncClients.custom().setRedirectStrategy(new DefaultRedirectStrategy() {
			@Override
			protected boolean isRedirectable(final String method) {
				return false;
			}
		}).setMaxConnTotal(10).setMaxConnPerRoute(10).setDefaultIOReactorConfig(ioConfig).build();
		httpclient.start();

		start();
	}

	private void start() {
		fetchScheduleService.scheduleAtFixedRate(() -> {
			try {
				fetchAndReportMetric();
			} catch (Exception e) {
				log.error("fetchAndReportMetric error:", e);
			}
		}, 10, intervalSecond, TimeUnit.SECONDS);
	}

	private void fetchAndReportMetric() {
		long now = System.currentTimeMillis();
		long lastFetchMs = now - MAX_LAST_FETCH_INTERVAL_MS;
		// trim milliseconds
		lastFetchMs = lastFetchMs / 1000 * 1000;
		long endTime = lastFetchMs + FETCH_INTERVAL_SECOND * 1000;

		// update last_fetch in advance.
		final long finalLastFetchMs = lastFetchMs;
		final long finalEndTime = endTime;
		try {
			// do real fetch async
			fetchOnce(finalLastFetchMs, finalEndTime);
		} catch (Exception e) {
			log.info("submit fetchOnce(" + sentinelConfig.getProjectName() + ") fail, intervalMs [" + lastFetchMs + ", "
					+ endTime + "]", e);
		}
	}

	/**
	 * fetch metric between [startTime, endTime], both side inclusive
	 */
	private void fetchOnce(long startTime, long endTime) {
		final String msg = "fetch";

		// 下面URL由com.alibaba.csp.sentinel.command.handler.SendMetricCommandHandler处理并返回结果
		final String url = "http://127.0.0.1:" + sentineConfigProperties.getApiPort() + "/" + METRIC_URL_PATH
				+ "?startTime=" + startTime + "&endTime=" + endTime + "&refetch=" + false;
		log.debug("url is:" + url);
		final HttpGet httpGet = new HttpGet(url);
		httpGet.setHeader(HTTP.CONN_DIRECTIVE, HTTP.CONN_CLOSE);
		httpclient.execute(httpGet, new FutureCallback<HttpResponse>() {
			@Override
			public void completed(final HttpResponse response) {
				try {
					handleResponse(response);
				} catch (Exception e) {
					log.error(msg + " metric " + url + " error:", e);
				}
			}

			@Override
			public void failed(final Exception ex) {
				httpGet.abort();
				if (ex instanceof SocketTimeoutException) {
					log.error("Failed to fetch metric from <{}>: socket timeout", url);
				} else if (ex instanceof ConnectException) {
					log.error("Failed to fetch metric from <{}> (ConnectionException: {})", url, ex.getMessage());
				} else {
					log.error(msg + " metric " + url + " error", ex);
				}
			}

			@Override
			public void cancelled() {
				httpGet.abort();
			}
		});
	}

	void handleResponse(final HttpResponse response) throws Exception {
		int code = response.getStatusLine().getStatusCode();
		if (code != HTTP_OK) {
			return;
		}
		Charset charset = null;
		try {
			String contentTypeStr = response.getFirstHeader("Content-type").getValue();
			if (StringUtil.isNotEmpty(contentTypeStr)) {
				ContentType contentType = ContentType.parse(contentTypeStr);
				charset = contentType.getCharset();
			}
		} catch (Exception ignore) {
		}
		String metrix = EntityUtils.toString(response.getEntity(), charset != null ? charset : DEFAULT_CHARSET);
		if (StringUtil.isEmpty(metrix) || metrix.startsWith(NO_METRICS)) {
			// logger.info(machine.getApp() + ":" + machine.getIp() + ":" +
			// machine.getPort() + ", bodyStr is empty");
			return;
		}
		sendMetric(metrix);
	}

	/**
	 * 将Metric数据发送到Sentinel控制台
	 * 
	 * @param metric
	 * @throws UnsupportedEncodingException
	 */
	private void sendMetric(String metric) throws UnsupportedEncodingException {
		String url = new StringBuilder("http://").append(sentineConfigProperties.getSentinelServer())
				.append("/metric/saveMetric.json?app=" + sentinelConfig.getProjectName() + "&team="
						+ sentinelConfig.getProjectTeam())
				.toString();
		final HttpPost httpPost = new HttpPost(url);
		httpPost.setHeader(HTTP.CONN_DIRECTIVE, HTTP.CONN_CLOSE);
		// 声明存放参数的List集合
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("metric", metric));

		// 创建form表单对象
		UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(params, DEFAULT_CHARSET);

		// 把表单对象设置到httpPost中
		httpPost.setEntity(formEntity);

		httpclient.execute(httpPost, new FutureCallback<HttpResponse>() {

			@Override
			public void completed(HttpResponse response) {
				try {
					Charset charset = null;
					try {
						String contentTypeStr = response.getFirstHeader("Content-type").getValue();
						if (StringUtil.isNotEmpty(contentTypeStr)) {
							ContentType contentType = ContentType.parse(contentTypeStr);
							charset = contentType.getCharset();
						}
					} catch (Exception ignore) {
					}
					String body = EntityUtils.toString(response.getEntity(),
							charset != null ? charset : DEFAULT_CHARSET);
					int code = response.getStatusLine().getStatusCode();
					if (code != HTTP_OK) {
						log.warn("发送metric到控制台失败，响应码：" + code + "，响应内容:" + body + "，url:" + url);
					}
				} catch (Exception e) {
					log.error("发送metric到控制台失败：" + e.getMessage() + "，URL:" + url, e);
				}
			}

			@Override
			public void failed(Exception ex) {
				httpPost.abort();
			}

			@Override
			public void cancelled() {
				httpPost.abort();
			}
		});

	}

}
