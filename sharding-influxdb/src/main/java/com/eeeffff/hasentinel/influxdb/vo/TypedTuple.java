package com.eeeffff.hasentinel.influxdb.vo;

import lombok.Data;

@Data
public class TypedTuple<V> {
	private V value;
	private double score;
}
