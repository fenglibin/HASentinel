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
package com.alibaba.csp.sentinel.dashboard.datasource.entity.rule;

import java.util.Date;
import java.util.List;

import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowClusterConfig;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowItem;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule;
import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author fenglibin
 * @since 0.2.1
 */
public class ParamFlowRuleEntityV2 {

	protected String id;
	protected String app;
	protected String ip;
	protected Integer port;

	protected ParamFlowRule rule;

	private Date gmtCreate;
	private Date gmtModified;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getApp() {
		return app;
	}

	public ParamFlowRuleEntityV2 setApp(String app) {
		this.app = app;
		return this;
	}

	public String getIp() {
		return ip;
	}

	public ParamFlowRuleEntityV2 setIp(String ip) {
		this.ip = ip;
		return this;
	}

	public Integer getPort() {
		return port;
	}

	public ParamFlowRuleEntityV2 setPort(Integer port) {
		this.port = port;
		return this;
	}

	public ParamFlowRule getRule() {
		return rule;
	}

	public ParamFlowRuleEntityV2 setRule(ParamFlowRule rule) {
		this.rule = rule;
		return this;
	}

	public Date getGmtCreate() {
		return gmtCreate;
	}

	public ParamFlowRuleEntityV2 setGmtCreate(Date gmtCreate) {
		this.gmtCreate = gmtCreate;
		return this;
	}

	public Date getGmtModified() {
		return gmtModified;
	}

	public ParamFlowRuleEntityV2 setGmtModified(Date gmtModified) {
		this.gmtModified = gmtModified;
		return this;
	}

	public ParamFlowRule toRule() {
		return rule;
	}

	@JsonIgnore
	@JSONField(serialize = false)
	public String getLimitApp() {
		return rule.getLimitApp();
	}

	@JsonIgnore
	@JSONField(serialize = false)
	public String getResource() {
		return rule.getResource();
	}

	@JsonIgnore
	@JSONField(serialize = false)
	public int getGrade() {
		return rule.getGrade();
	}

	@JsonIgnore
	@JSONField(serialize = false)
	public Integer getParamIdx() {
		return rule.getParamIdx();
	}

	@JsonIgnore
	@JSONField(serialize = false)
	public double getCount() {
		return rule.getCount();
	}

	@JsonIgnore
	@JSONField(serialize = false)
	public List<ParamFlowItem> getParamFlowItemList() {
		return rule.getParamFlowItemList();
	}

	@JsonIgnore
	@JSONField(serialize = false)
	public int getControlBehavior() {
		return rule.getControlBehavior();
	}

	@JsonIgnore
	@JSONField(serialize = false)
	public int getMaxQueueingTimeMs() {
		return rule.getMaxQueueingTimeMs();
	}

	@JsonIgnore
	@JSONField(serialize = false)
	public int getBurstCount() {
		return rule.getBurstCount();
	}

	@JsonIgnore
	@JSONField(serialize = false)
	public long getDurationInSec() {
		return rule.getDurationInSec();
	}

	@JsonIgnore
	@JSONField(serialize = false)
	public boolean isClusterMode() {
		return rule.isClusterMode();
	}

	@JsonIgnore
	@JSONField(serialize = false)
	public ParamFlowClusterConfig getClusterConfig() {
		return rule.getClusterConfig();
	}
}
