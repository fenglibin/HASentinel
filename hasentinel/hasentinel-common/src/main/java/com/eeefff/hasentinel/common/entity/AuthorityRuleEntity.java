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

import java.util.Date;

import com.alibaba.csp.sentinel.slots.block.authority.AuthorityRule;
import com.alibaba.csp.sentinel.util.AssertUtil;
import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author fenglibin
 * @since 0.2.1
 */
public class AuthorityRuleEntity {

    protected String id;

    protected String app;
    protected String ip;
    protected Integer port;

    protected AuthorityRule rule;

    private Date gmtCreate;
    private Date gmtModified;
    
    
    public AuthorityRuleEntity() {
    }

    public AuthorityRuleEntity(AuthorityRule authorityRule) {
        AssertUtil.notNull(authorityRule, "Authority rule should not be null");
        this.rule = authorityRule;
    }

    public static AuthorityRuleEntity fromAuthorityRule(String app, String ip, Integer port, AuthorityRule rule) {
    	AuthorityRuleEntity entity = new AuthorityRuleEntity(rule);
        entity.setApp(app);
        entity.setIp(ip);
        entity.setPort(port);
        return entity;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getApp() {
        return app;
    }

    public AuthorityRuleEntity setApp(String app) {
        this.app = app;
        return this;
    }

    public String getIp() {
        return ip;
    }

    public AuthorityRuleEntity setIp(String ip) {
        this.ip = ip;
        return this;
    }

    public Integer getPort() {
        return port;
    }

    public AuthorityRuleEntity setPort(Integer port) {
        this.port = port;
        return this;
    }

    public AuthorityRule getRule() {
        return rule;
    }

    public AuthorityRuleEntity setRule(AuthorityRule rule) {
        this.rule = rule;
        return this;
    }

    public Date getGmtCreate() {
        return gmtCreate;
    }

    public AuthorityRuleEntity setGmtCreate(Date gmtCreate) {
        this.gmtCreate = gmtCreate;
        return this;
    }

    public Date getGmtModified() {
        return gmtModified;
    }

    public AuthorityRuleEntity setGmtModified(Date gmtModified) {
        this.gmtModified = gmtModified;
        return this;
    }

    public AuthorityRule toRule() {
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
    public int getStrategy() {
        return rule.getStrategy();
    }
}
