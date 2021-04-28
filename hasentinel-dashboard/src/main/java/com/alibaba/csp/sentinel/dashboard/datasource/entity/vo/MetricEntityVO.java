package com.alibaba.csp.sentinel.dashboard.datasource.entity.vo;

import javax.validation.constraints.NotNull;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Tolerate;

@Data
@Builder
public class MetricEntityVO {
	@NotNull
	private String app;
	@NotNull
	private String resource;
	
	@Tolerate
	public MetricEntityVO() {
		
	}

	@Override
	public String toString() {
		return app + resource;
	}

	@Override
	public int hashCode() {
		return app.hashCode() + resource.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof MetricEntityVO)) {
			return false;
		}
		MetricEntityVO metric = (MetricEntityVO) obj;
		if (metric.getApp().equalsIgnoreCase(app) && metric.getResource().equalsIgnoreCase(resource)) {
			return true;
		}
		return false;
	}
}
