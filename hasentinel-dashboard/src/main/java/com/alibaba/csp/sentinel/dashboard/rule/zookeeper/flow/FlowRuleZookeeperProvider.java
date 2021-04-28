package com.alibaba.csp.sentinel.dashboard.rule.zookeeper.flow;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.KeeperException.NoNodeException;
import org.apache.zookeeper.data.Stat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.FlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRuleProvider;
import com.alibaba.csp.sentinel.dashboard.rule.zookeeper.ZookeeperConfigUtils;
import com.alibaba.csp.sentinel.datasource.Converter;

/**
 * @author rodbate
 * @since 2019/04/20 15:40
 */
@Component("flowRuleZookeeperProvider")
public class FlowRuleZookeeperProvider implements DynamicRuleProvider<List<FlowRuleEntity>> {

	@Autowired
	private CuratorFramework zkClient;

	@Autowired
	private Converter<String, List<FlowRuleEntity>> converter;

	@Override
	public List<FlowRuleEntity> getRules(String appName) throws Exception {
		byte[] data = null;
		try {
			String zkPath = ZookeeperConfigUtils.getFlowRuleZkPath(appName);
			Stat stat = zkClient.checkExists().forPath(zkPath);
	        if(stat == null){
	            return new ArrayList<>(0);
	        }
			data = zkClient.getData().forPath(zkPath);
			if (data == null || data.length == 0) {
				return new ArrayList<>();
			}
			return converter.convert(new String(data, StandardCharsets.UTF_8));
		} catch (NoNodeException e) {
			// 查询时节点不存在不打异常
		}
		return null;
	}
}
