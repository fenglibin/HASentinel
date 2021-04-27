package com.alibaba.csp.sentinel.dashboard.repository.metric;

import org.influxdb.BatchOptions;
import org.influxdb.InfluxDB;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;

public class InfluxDBWrapper {
	private String url;
	private InfluxDB influxDB;

	public InfluxDBWrapper(String url, InfluxDB influxDB) {
		this.url = url;
		this.influxDB = influxDB;
		if (!influxDB.isBatchEnabled()) {
			influxDB.enableBatch(BatchOptions.DEFAULTS);
		}
	}

	public InfluxDB getInfluxDB() {
		return influxDB;
	}

	public InfluxDB setDatabase(final String database) {
		return influxDB.setDatabase(database);
	}

	public QueryResult query(final Query query) {
		return influxDB.query(query);
	}

	public void write(final BatchPoints batchPoints) {
		influxDB.write(batchPoints);
	}

	public void write(final Point point) {
		influxDB.write(point);
	}
}
