package com.eeefff.hasentinel.common.datasource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.alibaba.csp.sentinel.datasource.ReadableDataSource;
import com.alibaba.csp.sentinel.datasource.zookeeper.ZookeeperDataSource;
import com.alibaba.csp.sentinel.init.InitFunc;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityRule;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityRuleManager;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRuleManager;
import com.alibaba.csp.sentinel.slots.system.SystemRule;
import com.alibaba.csp.sentinel.slots.system.SystemRuleManager;
import com.alibaba.csp.sentinel.util.AppNameUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.eeefff.hasentinel.common.config.HASentineConfigProperties;
import com.eeefff.hasentinel.common.config.HASentinelConfig;
import com.eeefff.hasentinel.common.entity.AuthorityRuleEntity;
import com.eeefff.hasentinel.common.entity.ParamFlowRuleEntity;
import com.eeefff.hasentinel.common.entity.SystemRuleEntity;

import lombok.extern.slf4j.Slf4j;

/**
 * Zookeeper配置数据源(Sentinel通过SPI的方式加载该实现)
 * 
 * @author fenglibin
 *
 */
@Slf4j
public class HAZookeeperDataSource implements InitFunc {

	@Override
	public void init() throws Exception {
		new Thread() {
			public void run() {
				try {
					initDatasource();
				} catch (Exception e) {
					log.error("初使化数据源发生异常:" + e.getMessage(), e);
				}
			}
		}.start();

	}

	public void initDatasource() throws Exception {
		HASentineConfigProperties sentineConfigProperties = null;
		int index = 1;
		while (index <= 60) {
			sentineConfigProperties = HASentinelConfig.getSentineConfigProperties();
			log.info("第" + index + "次获取sentineConfigProperties的值为：" + sentineConfigProperties);
			if (sentineConfigProperties == null) {
				Thread.sleep(1000);
				index++;
			} else {
				break;
			}
		}
		final String remoteAddress = HASentinelConfig.getSentineConfigProperties().getZookeeperAddress();
		final String flowRuleZkPath = ZookeeperConfigUtils.getFlowRuleZkPath(AppNameUtil.getAppName());
		final String authorityRuleZkPath = ZookeeperConfigUtils.getAuthorityRuleZkPath(AppNameUtil.getAppName());
		final String degradeRuleZkPath = ZookeeperConfigUtils.getDegradeRuleZkPath(AppNameUtil.getAppName());
		final String paramFlowRuleZkPath = ZookeeperConfigUtils.getHotRuleZkPath(AppNameUtil.getAppName());
		final String systemRuleZkPath = ZookeeperConfigUtils.getSystemRuleZkPath(AppNameUtil.getAppName());

		ReadableDataSource<String, List<FlowRule>> flowRuleDataSource = new ZookeeperDataSource<>(remoteAddress,
				flowRuleZkPath, source -> {
					log.info("流控规则发生变化，flowRuleZkPath:" + flowRuleZkPath + "，规则内容：" + source);
					return JSON.parseObject(source, new TypeReference<List<FlowRule>>() {
					});
				});
		ReadableDataSource<String, List<DegradeRule>> degradeRuleDataSource = new ZookeeperDataSource<>(remoteAddress,
				degradeRuleZkPath, source -> {
					Map<Long, DegradeRule> rules = JSON.parseObject(source,
							new TypeReference<Map<Long, DegradeRule>>() {
							});
					if (rules == null || rules.isEmpty()) {
						return new ArrayList<>();
					}
					List<DegradeRule> list = new ArrayList<>(rules.values());
					log.info("降级规则发生变化，degradeRuleZkPath:" + degradeRuleZkPath + "，规则内容：" + JSON.toJSONString(list));
					return list;
				});
		ReadableDataSource<String, List<AuthorityRule>> authorityRuleDataSource = new ZookeeperDataSource<>(
				remoteAddress, authorityRuleZkPath, source -> {
					final List<AuthorityRule> authorityRule = new ArrayList<AuthorityRule>();
					List<AuthorityRuleEntity> authorityRuleEntityList = JSON.parseObject(source,
							new TypeReference<List<AuthorityRuleEntity>>() {
							});
					if (authorityRuleEntityList == null || authorityRuleEntityList.size() == 0) {
						return authorityRule;
					}
					authorityRuleEntityList.forEach(r -> {
						authorityRule.add(r.getRule());
					});
					log.info("授权规则发生变化，authorityRuleZkPath:" + authorityRuleZkPath + "，规则内容："
							+ JSON.toJSONString(authorityRule));
					return authorityRule;
				});
		ReadableDataSource<String, List<ParamFlowRule>> paramFlowRuleDataSource = new ZookeeperDataSource<>(
				remoteAddress, paramFlowRuleZkPath, source -> {
					final List<ParamFlowRule> ruleList = new ArrayList<ParamFlowRule>();
					Map<Long, ParamFlowRuleEntity> rules = JSON.parseObject(source,
							new TypeReference<Map<Long, ParamFlowRuleEntity>>() {
							});
					if (rules == null || rules.size() == 0) {
						return ruleList;
					}
					rules.forEach((k, v) -> {
						ruleList.add(v.getRule());
					});
					log.info("参数规则发生变化，paramFlowRuleZkPath:" + paramFlowRuleZkPath + "，规则内容："
							+ JSON.toJSONString(ruleList));
					return ruleList;
				});
		ReadableDataSource<String, List<SystemRule>> systemRuleDataSource = new ZookeeperDataSource<>(remoteAddress,
				systemRuleZkPath, source -> {
					final List<SystemRule> ruleList = new ArrayList<SystemRule>();
					Map<Long, SystemRuleEntity> rules = JSON.parseObject(source,
							new TypeReference<Map<Long, SystemRuleEntity>>() {
							});
					if (rules == null || rules.size() == 0) {
						return ruleList;
					}
					rules.forEach((k, v) -> {
						ruleList.add(v.getRule());
					});
					log.info("系统规则发生变化，systemRuleZkPath:" + systemRuleZkPath + "，规则内容：" + JSON.toJSONString(ruleList));
					return ruleList;
				});

		FlowRuleManager.register2Property(flowRuleDataSource.getProperty());
		DegradeRuleManager.register2Property(degradeRuleDataSource.getProperty());
		AuthorityRuleManager.register2Property(authorityRuleDataSource.getProperty());
		ParamFlowRuleManager.register2Property(paramFlowRuleDataSource.getProperty());
		SystemRuleManager.register2Property(systemRuleDataSource.getProperty());

	}

}
