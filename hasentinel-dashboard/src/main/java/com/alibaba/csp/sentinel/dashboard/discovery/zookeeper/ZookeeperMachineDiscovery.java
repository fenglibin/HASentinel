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
package com.alibaba.csp.sentinel.dashboard.discovery.zookeeper;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException.NoNodeException;
import org.apache.zookeeper.data.Stat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.alibaba.csp.sentinel.dashboard.config.RedisConfig.CacheTimeConfig;
import com.alibaba.csp.sentinel.dashboard.discovery.AppInfo;
import com.alibaba.csp.sentinel.dashboard.discovery.MachineDiscovery;
import com.alibaba.csp.sentinel.dashboard.discovery.MachineInfo;
import com.alibaba.csp.sentinel.dashboard.rule.zookeeper.ZookeeperConfigUtils;
import com.alibaba.csp.sentinel.dashboard.wrapper.redis.RedisTemplateWrapper;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.util.AssertUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * @author fenglibin
 */
@Slf4j
@Component("zookeeperMachineDiscovery")
public class ZookeeperMachineDiscovery implements MachineDiscovery {

	@Autowired
	private CuratorFramework zkClient;

	@Autowired
	private Converter<String, AppInfo> decodeConverter;
	@Autowired
	private Converter<AppInfo, String> encodeConverter;

	// 单个App的Resource的缓存时间，单位为秒，默认为300秒
	@Autowired
	private CacheTimeConfig cacheTimeConfig;

	@Override
	public long addMachine(MachineInfo machineInfo) {
		AssertUtil.notNull(machineInfo, "machineInfo cannot be null");
		AppInfo appInfo = getDetailApp(machineInfo.getApp());
		if (appInfo == null) {
			appInfo = new AppInfo(machineInfo.getApp(), machineInfo.getAppType());
		}
		/*
		 * else { Optional<MachineInfo> machine =
		 * appInfo.getMachine(machineInfo.getIp(), machineInfo.getPort()); //
		 * 判断当前服务器节点是否已经存在了，存在则不增加了 if (machine.isPresent()) { return 0; } }
		 */
		appInfo.addMachine(machineInfo);

		boolean saveResult = saveData(machineInfo.getApp(), appInfo);
		if (saveResult) {
			return 1;
		}
		return 0;
	}

	@Override
	public boolean removeMachine(String app, String ip, int port) {
		AssertUtil.assertNotBlank(app, "app name cannot be blank");
		AppInfo appInfo = getDetailApp(app);
		if (appInfo != null) {
			Optional<MachineInfo> machineInfo = appInfo.getMachine(ip, port);
			if (machineInfo.isPresent()) {
				boolean removeResult = appInfo.removeMachine(ip, port);
				if (removeResult) {
					// 将删除后的结果回写的ZK
					boolean saveResult = saveData(app, appInfo);
					if (!saveResult) {
						// 如果保存失败，再加回去
						appInfo.addMachine(machineInfo.get());
						return false;
					} else {
						return true;
					}
				} else {
					return false;
				}
			} else {
				return false;
			}
		}
		return false;
	}

	@Override
	public List<String> getAppNames() {
		List<String> childRenList;
		try {
			childRenList = zkClient.getChildren().forPath(ZookeeperConfigUtils.getAppMachinesConfigZkPath());
			return childRenList;
		} catch (Exception e) {
			log.error("从ZK中获取所有的应用列表发生异常：" + e.getMessage(), e);
		}
		return new ArrayList<>();
	}

	@Override
	public AppInfo getDetailApp(String app) {
		AssertUtil.assertNotBlank(app, "app name cannot be blank");
		byte[] data = null;
		try {
			data = zkClient.getData().forPath(ZookeeperConfigUtils.getMachineDiscoveryZkPath(app));
			if (data == null || data.length == 0) {
				return null;
			}
			AppInfo appInfo = decodeConverter.convert(new String(data, StandardCharsets.UTF_8));
			int appInfoSize = appInfo.getMachines().size();
			// 删除过期的服务器
			Set<MachineInfo> machines = appInfo.getMachines();
			for (MachineInfo machine : machines) {
				if (machine.isDead()) {
					appInfo.removeMachine(machine.getIp(), machine.getPort());
				}
			}
			if (appInfoSize != appInfo.getMachines().size()) {
				saveData(appInfo.getApp(), appInfo);
			}
			return appInfo;
		} catch (NoNodeException e) {
			// 查询时节点不存在不打异常
		} catch (Exception e) {
			log.error("从ZK获取app:" + app + "的服务器列表发生异常:" + e.getMessage(), e);
		}
		return null;
	}

	@Override
	public Set<AppInfo> getBriefApps() {
		final Set<AppInfo> appSet = new HashSet<AppInfo>();
		try {
			String key = "briefApps";
			RedisTemplateWrapper.getAndConsumeValue(key, v -> {
				Optional.ofNullable(v).ifPresent(v1 -> {
					appSet.addAll((Set<AppInfo>) v1);
				});
			});
			if (!CollectionUtils.isEmpty(appSet)) {
				return appSet;
			}
			Stat stat = zkClient.checkExists().forPath(ZookeeperConfigUtils.getAppMachinesConfigZkPath());
			if (stat == null) {
				return new HashSet<>();
			}
			List<String> childRenList = zkClient.getChildren()
					.forPath(ZookeeperConfigUtils.getAppMachinesConfigZkPath());
			childRenList.forEach(a -> {
				appSet.add(getDetailApp(a));
			});
			RedisTemplateWrapper.set(key, appSet, cacheTimeConfig.getAppResource(), TimeUnit.SECONDS);
			return appSet;
		} catch (Exception e) {
			log.error("从ZK中获取所有的应用信息发生异常：" + e.getMessage(), e);
		}
		return new HashSet<>();
	}

	@Override
	public void removeApp(String app) {
		AssertUtil.assertNotBlank(app, "app name cannot be blank");
		String zkPath = ZookeeperConfigUtils.getMachineDiscoveryZkPath(app);
		try {
			zkClient.delete().forPath(zkPath);
		} catch (Exception e) {
			log.error("从ZK中删除app：" + app + "的信息发生异常：" + e.getMessage(), e);
		}
	}

	/**
	 * 将app机器列表保存到ZK中
	 * 
	 * @param app
	 * @param appInfo
	 */
	private boolean saveData(String app, AppInfo appInfo) {
		String zkPath = ZookeeperConfigUtils.getMachineDiscoveryZkPath(app);
		byte[] data = null;
		data = encodeConverter.convert(appInfo).getBytes(StandardCharsets.UTF_8);
		try {
			Stat stat = zkClient.checkExists().forPath(zkPath);
			if (stat == null) {
				zkClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(zkPath, null);
			}
			zkClient.setData().forPath(zkPath, data);
			return true;
		} catch (Exception e) {
			log.error("将app:" + app + "的机器列表保存到ZK中发生异常：" + e.getMessage(), e);
		}
		return false;
	}

}
