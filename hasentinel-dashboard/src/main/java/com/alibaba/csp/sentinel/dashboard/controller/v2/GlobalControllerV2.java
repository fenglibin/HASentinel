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
package com.alibaba.csp.sentinel.dashboard.controller.v2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.csp.sentinel.dashboard.auth.AuthAction;
import com.alibaba.csp.sentinel.dashboard.auth.AuthService.PrivilegeType;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.SystemRuleEntity;
import com.alibaba.csp.sentinel.dashboard.domain.Result;
import com.alibaba.csp.sentinel.dashboard.rule.zookeeper.system.ZookeeperSystemRuleStore;

/**
 * @author fenglibin
 */
@RestController
@RequestMapping("/global")
public class GlobalControllerV2 {

	private final Logger logger = LoggerFactory.getLogger(GlobalControllerV2.class);

	@Autowired
	private ZookeeperSystemRuleStore<SystemRuleEntity> repository;

	/**
	 * 增加全部规则，如果对应用key的规则不存在则增加，存在则覆盖
	 * @param key
	 * @param value
	 * @return
	 */
	@GetMapping("/save.json")
	@AuthAction(PrivilegeType.WRITE_RULE)
	public Result<Void> addRule(String key, String value) {
	
		return Result.ofSuccess(null);
	}

}
