package com.eeeffff.hasentinel.influxdb.sharding;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BoundParameterQuery;
import org.influxdb.dto.QueryResult;
import org.influxdb.impl.InfluxDBResultMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.eeeffff.hasentinel.influxdb.config.InfluxdbConfigProperties;

/**
 * @author fenglibin
 */
@Component
public class InfluxdbShardingMapper {

	private static Logger logger = LoggerFactory.getLogger(InfluxdbShardingMapper.class);

	@Autowired
	private InfluxdbConfigProperties influxdbConfigProperties;

	private static InfluxDBResultMapper resultMapper = new InfluxDBResultMapper();

	/**
	 * 根据当前InfluxDB配置的后端数据库的数量，与当前应用的hash code进行求与运算，选择一个后端的数据库
	 * 
	 * @param app
	 * @return
	 */
	public String getUrl(String app) {
		// int shardingId = (influxdbConfigProperties.getUrlArr().length - 1) & app.hashCode();
		int shardingId = Math.abs(app.hashCode()) % influxdbConfigProperties.getUrlArr().length;
		return influxdbConfigProperties.getUrlArr()[shardingId];

	}

	public <T> T process(String app, String database, InfluxDBCallback callback) {
		InfluxDB influxDB = null;
		T t = null;
		try {
			String url = getUrl(app);
			if(url.startsWith("https")) {
        		influxDB = InfluxDBFactoryHttps.connect(url, influxdbConfigProperties.getUsername(), influxdbConfigProperties.getPassword());
        	}else {
        		influxDB = InfluxDBFactory.connect(url, influxdbConfigProperties.getUsername(), influxdbConfigProperties.getPassword());
        	}
			influxDB.setDatabase(database);

			t = callback.doCallBack(database, influxDB);
		} catch (Exception e) {
			logger.error("[process exception]", e);
		} finally {
			if (influxDB != null) {
				try {
					influxDB.close();
				} catch (Exception e) {
					logger.error("[influxDB.close exception]", e);
				}
			}
		}

		return t;
	}

	public void insert(String app, String database, InfluxDBInsertCallback influxDBInsertCallback) {
		process(app, database, new InfluxDBCallback() {
			@Override
			public <T> T doCallBack(String database, InfluxDB influxDB) {
				influxDBInsertCallback.doCallBack(database, influxDB);
				return null;
			}
		});

	}

	public QueryResult query(String app, String database, InfluxDBQueryCallback influxDBQueryCallback) {
		return process(app, database, new InfluxDBCallback() {
			@Override
			public <T> T doCallBack(String database, InfluxDB influxDB) {
				QueryResult queryResult = influxDBQueryCallback.doCallBack(database, influxDB);
				return (T) queryResult;
			}
		});
	}

	public <T> List<T> queryList(String app, String database, String sql, Map<String, Object> paramMap,
			Class<T> clasz) {
		QueryResult queryResult = query(app, database, new InfluxDBQueryCallback() {
			@Override
			public QueryResult doCallBack(String database, InfluxDB influxDB) {
				BoundParameterQuery.QueryBuilder queryBuilder = BoundParameterQuery.QueryBuilder.newQuery(sql);
				queryBuilder.forDatabase(database);

				if (paramMap != null && paramMap.size() > 0) {
					Set<Map.Entry<String, Object>> entries = paramMap.entrySet();
					for (Map.Entry<String, Object> entry : entries) {
						queryBuilder.bind(entry.getKey(), entry.getValue());
					}
				}

				return influxDB.query(queryBuilder.create());
			}
		});

		return resultMapper.toPOJO(queryResult, clasz);
	}
	
	public <T> List<T> queryBySql(String app, String database, String sql,Class<T> clasz) {
		QueryResult queryResult = query(app, database, new InfluxDBQueryCallback() {
			@Override
			public QueryResult doCallBack(String database, InfluxDB influxDB) {
				BoundParameterQuery.QueryBuilder queryBuilder = BoundParameterQuery.QueryBuilder.newQuery(sql);
				queryBuilder.forDatabase(database);

				return influxDB.query(queryBuilder.create());
			}
		});

		return resultMapper.toPOJO(queryResult, clasz);
	}

	public interface InfluxDBCallback {
		<T> T doCallBack(String database, InfluxDB influxDB);
	}

	public interface InfluxDBInsertCallback {
		void doCallBack(String database, InfluxDB influxDB);
	}

	public interface InfluxDBQueryCallback {
		QueryResult doCallBack(String database, InfluxDB influxDB);
	}
}