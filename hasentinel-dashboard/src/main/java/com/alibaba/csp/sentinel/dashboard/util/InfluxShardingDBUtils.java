package com.alibaba.csp.sentinel.dashboard.util;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.influxdb.dto.BoundParameterQuery;
import org.influxdb.dto.QueryResult;
import org.influxdb.impl.InfluxDBResultMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.alibaba.csp.sentinel.dashboard.repository.metric.InfluxDBWrapper;

/**
 * @author fenglibin
 */
@Component
public class InfluxShardingDBUtils {

	private static Logger logger = LoggerFactory.getLogger(InfluxShardingDBUtils.class);

	private static String url;

	private static String[] urlsArr = null;

	private static String username;

	private static String password;

	private static InfluxDBResultMapper resultMapper = new InfluxDBResultMapper();

	// url可能是多个url，以英文的逗号","分隔
	@Value("${influxdb.url}")
	public void setUrl(String url) {
		initUrls(url);
	}

	@Value("${influxdb.username}")
	public void setUsername(String username) {
		InfluxShardingDBUtils.username = username;
	}

	@Value("${influxdb.password}")
	public void setPassword(String password) {
		InfluxShardingDBUtils.password = password;
	}

	private static void initUrls(String url) {
		InfluxShardingDBUtils.url = url;
		if (url != null && url.trim().length() > 0) {
			urlsArr = url.split(",");
		}
	}

	/**
	 * 根据当前InfluxDB配置的后端数据库的数量，与当前应用的hash code进行求与运算，选择一个后端的数据库
	 * 
	 * @param app  应用名称
	 * @return
	 */
	public static String getUrl(String app) {
		int shardingId = Math.abs(app.hashCode()) % urlsArr.length;
		return urlsArr[shardingId];
	}

	public static void init(String url, String username, String password) {
		initUrls(url);
		InfluxShardingDBUtils.username = username;
		InfluxShardingDBUtils.password = password;
	}

	public static <T> T process(String app, String database, InfluxDBCallback callback) {
		InfluxDBWrapper influxDBWrapper = null;
		T t = null;
		String url = null;
		try {
			url = getUrl(app);
			influxDBWrapper = InfluxDBConnecionPool.connect(url, username, password);
			influxDBWrapper.setDatabase(database);

			t = callback.doCallBack(database, influxDBWrapper);
		} catch (Exception e) {
			logger.error("[process exception]", e);
		} finally {
			if (influxDBWrapper != null) {
				try {
					// influxDBWrapper.getInfluxDB().close();
					InfluxDBConnecionPool.returnConnection(url, influxDBWrapper.getInfluxDB());
				} catch (Exception e) {
					logger.error("[influxDB.close exception]", e);
				}
			}
		}

		return t;
	}

	public static void insert(String app, String database, InfluxDBInsertCallback influxDBInsertCallback) {
		process(app, database, new InfluxDBCallback() {
			@Override
			public <T> T doCallBack(String database, InfluxDBWrapper influxDBWrapper) {
				influxDBInsertCallback.doCallBack(database, influxDBWrapper);
				return null;
			}
		});

	}

	public static QueryResult query(String app, String database, InfluxDBQueryCallback influxDBQueryCallback) {
		return process(app, database, new InfluxDBCallback() {
			@SuppressWarnings("unchecked")
			@Override
			public <T> T doCallBack(String database, InfluxDBWrapper influxDBWrapper) {
				QueryResult queryResult = influxDBQueryCallback.doCallBack(database, influxDBWrapper);
				return (T) queryResult;
			}
		});
	}

	public static <T> List<T> queryList(String app, String database, String sql, Map<String, Object> paramMap,
			Class<T> clasz) {
		QueryResult queryResult = query(app, database, new InfluxDBQueryCallback() {
			@Override
			public QueryResult doCallBack(String database, InfluxDBWrapper influxDBWrapper) {
				BoundParameterQuery.QueryBuilder queryBuilder = BoundParameterQuery.QueryBuilder.newQuery(sql);
				queryBuilder.forDatabase(database);

				if (paramMap != null && paramMap.size() > 0) {
					Set<Map.Entry<String, Object>> entries = paramMap.entrySet();
					for (Map.Entry<String, Object> entry : entries) {
						queryBuilder.bind(entry.getKey(), entry.getValue());
					}
				}

				return influxDBWrapper.query(queryBuilder.create());
			}
		});
		if (queryResult == null) {
			return null;
		}
		return resultMapper.toPOJO(queryResult, clasz);
	}

	public interface InfluxDBCallback {
		<T> T doCallBack(String database, InfluxDBWrapper influxDBWrapper);
	}

	public interface InfluxDBInsertCallback {
		void doCallBack(String database, InfluxDBWrapper influxDBWrapper);
	}

	public interface InfluxDBQueryCallback {
		QueryResult doCallBack(String database, InfluxDBWrapper influxDBWrapper);
	}
}