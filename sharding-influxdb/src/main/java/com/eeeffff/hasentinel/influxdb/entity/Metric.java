package com.eeeffff.hasentinel.influxdb.entity;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

/**
 * 符合Grafana规范的{@link Metric}对象。<br>
 * 数据格式如下：<br>
 * { <br>
 * "results": [{ <br>
 * "statement_id": 0, <br>
 * "series": [{ <br>
 * "name": "sentinel_metric_dubbo", <br>
 * "columns": ["time", "mean"], <br>
 * "values": [ <br>
 * [1585901512000, null], <br>
 * ...... <br>
 * ] <br>
 * }] <br>
 * }, { <br>
 * "statement_id": 1, <br>
 * "series": [{ <br>
 * "name": "sentinel_metric_dubbo", <br>
 * "columns": ["time", "mean"], <br>
 * "values": [ <br>
 * [1585901512000, null], <br>
 * ...... <br>
 * ] <br>
 * }] <br>
 * }] <br>
 * } <br>
 */
@Data
public class Metric {
	private List<Results> results;

	public static Metric getEmptyMetricWithColumns(String[] cloumns) {
		Metric metric = new Metric();
		metric.setResults(new ArrayList<Results>());
		metric.getResults().add(new Results());
		metric.getResults().get(0).setSeries(new ArrayList<Series>());
		metric.getResults().get(0).getSeries().add(new Series());
		metric.getResults().get(0).getSeries().get(0).setName("metric");
		metric.getResults().get(0).getSeries().get(0).setColumns(cloumns);
		metric.getResults().get(0).getSeries().get(0).setValues(new ArrayList<Object[]>());
		return metric;
	}
}
