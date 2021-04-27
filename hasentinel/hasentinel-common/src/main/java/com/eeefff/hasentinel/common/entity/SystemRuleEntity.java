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

import com.alibaba.csp.sentinel.slots.system.SystemRule;
import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Date;

/**
 * @author fenglibin
 */
public class SystemRuleEntity {

    private String id;

    private String app;
    private String ip;
    private Integer port;
    private Double highestSystemLoad;
    private Long avgRt;
    private Long maxThread;
    private Double qps;
    private Double highestCpuUsage;

    private Date gmtCreate;
    private Date gmtModified;
    
    private SystemRule rule;
    
    @JsonIgnore
    @JSONField(serialize = false)
    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
    @JsonIgnore
    @JSONField(serialize = false)
    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }
    @JsonIgnore
    @JSONField(serialize = false)
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    @JsonIgnore
    @JSONField(serialize = false)
    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }
    @JsonIgnore
    @JSONField(serialize = false)
    public Double getHighestSystemLoad() {
        return highestSystemLoad;
    }

    public void setHighestSystemLoad(Double highestSystemLoad) {
        this.highestSystemLoad = highestSystemLoad;
    }
    @JsonIgnore
    @JSONField(serialize = false)
    public Long getAvgRt() {
        return avgRt;
    }

    public void setAvgRt(Long avgRt) {
        this.avgRt = avgRt;
    }
    @JsonIgnore
    @JSONField(serialize = false)
    public Long getMaxThread() {
        return maxThread;
    }

    public void setMaxThread(Long maxThread) {
        this.maxThread = maxThread;
    }
    @JsonIgnore
    @JSONField(serialize = false)
    public Double getQps() {
        return qps;
    }

    public void setQps(Double qps) {
        this.qps = qps;
    }
    @JsonIgnore
    @JSONField(serialize = false)
    public Double getHighestCpuUsage() {
        return highestCpuUsage;
    }

    public void setHighestCpuUsage(Double highestCpuUsage) {
        this.highestCpuUsage = highestCpuUsage;
    }
    @JsonIgnore
    @JSONField(serialize = false)
    public Date getGmtCreate() {
        return gmtCreate;
    }

    public void setGmtCreate(Date gmtCreate) {
        this.gmtCreate = gmtCreate;
    }
    @JsonIgnore
    @JSONField(serialize = false)
    public Date getGmtModified() {
        return gmtModified;
    }

    public void setGmtModified(Date gmtModified) {
        this.gmtModified = gmtModified;
    }

    public SystemRule getRule() {
        SystemRule rule = new SystemRule();
        rule.setHighestSystemLoad(highestSystemLoad);
        rule.setAvgRt(avgRt);
        rule.setMaxThread(maxThread);
        rule.setQps(qps);
        rule.setHighestCpuUsage(highestCpuUsage);
        return rule;
    }
}
