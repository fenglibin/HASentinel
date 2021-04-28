package com.alibaba.csp.sentinel.dashboard.rule.zookeeper.authority;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.AuthorityRuleEntity;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRulePublisher;
import com.alibaba.csp.sentinel.dashboard.rule.zookeeper.ZookeeperConfigUtils;
import com.alibaba.csp.sentinel.datasource.Converter;

/**
 * @author rodbate
 * @since 2019/04/20 15:31
 */
public class AuthorityRuleZookeeperPublisher implements DynamicRulePublisher<List<AuthorityRuleEntity>> {

	@Autowired
	private CuratorFramework zkClient;

	@Autowired
	private Converter<List<AuthorityRuleEntity>, String> converter;

	@Override
	public void publish(String app, List<AuthorityRuleEntity> rules) throws Exception {
		String zkPath = ZookeeperConfigUtils.getAuthorityRuleZkPath(app);
		Stat stat = zkClient.checkExists().forPath(zkPath);
		if (stat == null) {
			zkClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(zkPath, null);
		}
		byte[] data = null;
		if (!CollectionUtils.isEmpty(rules)) {
			data = converter.convert(rules).getBytes(StandardCharsets.UTF_8);
		}
		zkClient.setData().forPath(zkPath, data);
	}

}
