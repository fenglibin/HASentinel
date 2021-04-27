package com.eeefff.hasentinel.influxdb.entity;

import java.util.List;

import lombok.Data;

@Data
public class Series {
	private String name;
	private String[] columns;
	private List<Object[]> values;
}
