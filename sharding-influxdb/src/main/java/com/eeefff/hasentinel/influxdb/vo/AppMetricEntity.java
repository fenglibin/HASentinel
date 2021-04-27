package com.eeefff.hasentinel.influxdb.vo;

import javax.validation.constraints.NotNull;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Tolerate;

@Data
@Builder
public class AppMetricEntity {
	@NotNull
	private String app;
	@NotNull
	private String resource;
	@NotNull
	private double avgRt;

	@Tolerate
	public AppMetricEntity() {

	}
}
