/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.eeefff.hasentinel.common.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * @author leyou
 */
public class MetricEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	private Long id;
	private Date gmtCreate;
	private Date gmtModified;
	// 应用归属团队
	private String team;
	// 应用名称
	private String app;
	private String appName;
	/**
	 * 监控信息的时间戳
	 */
	private Date timestamp;
	private String resource;
	private String resourceName;
	private Long passQps;
	private Long successQps;
	private Long blockQps;
	private Long exceptionQps;

	/**
	 * summary rt of all success exit qps.
	 */
	private double rt;

	/**
	 * 本次聚合的总条数
	 */
	private int count;

	private int resourceCode;

	private double avgRt;

	public static MetricEntity copyOf(MetricEntity oldEntity) {
		MetricEntity entity = new MetricEntity();
		entity.setId(oldEntity.getId());
		entity.setGmtCreate(oldEntity.getGmtCreate());
		entity.setGmtModified(oldEntity.getGmtModified());
		entity.setTeam(oldEntity.getTeam());
		entity.setApp(oldEntity.getApp());
		entity.setTimestamp(oldEntity.getTimestamp());
		entity.setResource(oldEntity.getResource());
		entity.setPassQps(oldEntity.getPassQps());
		entity.setBlockQps(oldEntity.getBlockQps());
		entity.setSuccessQps(oldEntity.getSuccessQps());
		entity.setExceptionQps(oldEntity.getExceptionQps());
		entity.setRt(oldEntity.getRt());
		entity.setCount(oldEntity.getCount());
		entity.setResource(oldEntity.getResource());
		entity.setAvgRt(oldEntity.getAvgRt());
		return entity;
	}

	public synchronized void addPassQps(Long passQps) {
		this.passQps += passQps;
	}

	public synchronized void addBlockQps(Long blockQps) {
		this.blockQps += blockQps;
	}

	public synchronized void addExceptionQps(Long exceptionQps) {
		this.exceptionQps += exceptionQps;
	}

	public synchronized void addCount(int count) {
		this.count += count;
	}

	public synchronized void addRtAndSuccessQps(double avgRt, Long successQps) {
		this.rt += avgRt * successQps;
		this.successQps += successQps;
	}

	/**
	 * {@link #rt} = {@code avgRt * successQps}
	 *
	 * @param avgRt      average rt of {@code successQps}
	 * @param successQps
	 */
	public synchronized void setRtAndSuccessQps(double avgRt, Long successQps) {
		this.rt = avgRt * successQps;
		this.successQps = successQps;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Date getGmtCreate() {
		return gmtCreate;
	}

	public void setGmtCreate(Date gmtCreate) {
		this.gmtCreate = gmtCreate;
	}

	public Date getGmtModified() {
		return gmtModified;
	}

	public void setGmtModified(Date gmtModified) {
		this.gmtModified = gmtModified;
	}

	public String getTeam() {
		return team;
	}

	public void setTeam(String team) {
		this.team = team;
	}

	public String getApp() {
		return app;
	}

	public void setApp(String app) {
		this.app = app;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public String getResource() {
		return resource;
	}

	public void setResource(String resource) {
		this.resource = resource;
		this.resourceCode = resource.hashCode();
	}

	public Long getPassQps() {
		return passQps;
	}

	public void setPassQps(Long passQps) {
		this.passQps = passQps;
	}

	public Long getBlockQps() {
		return blockQps;
	}

	public void setBlockQps(Long blockQps) {
		this.blockQps = blockQps;
	}

	public Long getExceptionQps() {
		return exceptionQps;
	}

	public void setExceptionQps(Long exceptionQps) {
		this.exceptionQps = exceptionQps;
	}

	public double getRt() {
		return rt;
	}

	public void setRt(double rt) {
		this.rt = rt;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public int getResourceCode() {
		return resourceCode;
	}

	public Long getSuccessQps() {
		return successQps;
	}

	public void setSuccessQps(Long successQps) {
		this.successQps = successQps;
	}

	public void setAvgRt(double avgRt) {
		this.avgRt = avgRt;
	}

	/**
	 * 获取平均响应时间
	 * 
	 * @return
	 */
	public double getAvgRt() {
		return getSuccessQps() > 0 ? getRt() / getSuccessQps() : getRt();
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public String getResourceName() {
		return resourceName;
	}

	public void setResourceName(String resourceName) {
		this.resourceName = resourceName;
	}
	
	@Override
	public String toString() {
		return "MetricEntity{" + "id=" + id + ", gmtCreate=" + gmtCreate + ", gmtModified=" + gmtModified + ", team='"
				+ team + '\'' + ", app='" + app + '\'' + ", appName='" + appName + '\'' + ", timestamp=" + timestamp
				+ ", resource='" + resource + '\'' + ", passQps=" + passQps + ", blockQps=" + blockQps + ", successQps="
				+ successQps + ", exceptionQps=" + exceptionQps + ", rt=" + rt + ", count=" + count + ", resourceCode="
				+ resourceCode + '}';

	}

	@Override
	public int hashCode() {
		return team.hashCode() + app.hashCode() + resource.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof MetricEntity)) {
			return false;
		}
		MetricEntity metric = (MetricEntity) obj;
		if (metric.getTeam().equalsIgnoreCase(team) && metric.getApp().equalsIgnoreCase(app)
				&& metric.getResource().equalsIgnoreCase(resource)) {
			return true;
		}
		return false;
	}

}
