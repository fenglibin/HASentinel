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
package com.alibaba.csp.sentinel.dashboard.rule.zookeeper.degrade;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.DegradeRuleEntity;
import com.alibaba.csp.sentinel.dashboard.discovery.MachineInfo;
import com.alibaba.csp.sentinel.dashboard.repository.rule.RuleRepository;
import com.alibaba.csp.sentinel.dashboard.rule.zookeeper.ZookeeperConfigUtils;
import com.alibaba.csp.sentinel.dashboard.uniqueid.IdGenerator;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.util.AssertUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * @author fenglibin
 */
@Slf4j
@Component
public class ZookeeperDegradeRuleStore<T extends DegradeRuleEntity> implements RuleRepository<T, Long> {

	@Autowired
	private CuratorFramework zkClient;
	@Autowired
	private Converter<DegradeRuleEntity, String> encodeConverter;
	@Autowired
	private Converter<String, DegradeRuleEntity> decodeConverter;
	@Autowired
	private Converter<Map<Long, DegradeRuleEntity>, String> mapEncodeConverter;
	@Autowired
	private Converter<String, Map<Long, DegradeRuleEntity>> mapDecodeConverter;

	@Autowired
	private IdGenerator<Long> idGenerator;

	@SuppressWarnings("unchecked")
	@Override
	public T save(T entity) {
		if (entity.getId() == null) {
			entity.setId(nextId());
		}
		T processedEntity = preProcess(entity);
		if (processedEntity != null) {
			String zkPathId = ZookeeperConfigUtils.getDegradeRuleZkPath(String.valueOf(entity.getId()));
			String zkPathApp = ZookeeperConfigUtils.getDegradeRuleZkPath(entity.getApp());
			byte[] data = null;
			data = encodeConverter.convert(processedEntity).getBytes(StandardCharsets.UTF_8);
			try {

				Stat stat = zkClient.checkExists().forPath(zkPathId);
				if (stat == null) {
					zkClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(zkPathId, null);
				}
				stat = zkClient.checkExists().forPath(zkPathApp);
				if (stat == null) {
					zkClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(zkPathApp,
							null);
				}

				Map<Long, DegradeRuleEntity> entities = (Map<Long, DegradeRuleEntity>) findBy(entity.getApp());
				if (entities == null) {
					entities = new ConcurrentHashMap<>(32);
				}
				entities.put(entity.getId(), entity);
				byte[] appData = mapEncodeConverter.convert(entities).getBytes(StandardCharsets.UTF_8);

				zkClient.setData().forPath(zkPathId, data);
				zkClient.setData().forPath(zkPathApp, appData);
			} catch (Exception e) {
				log.error("???app:" + entity.getApp() + " ID:" + entity.getId() + "????????????????????????ZK??????????????????" + e.getMessage(), e);
			}
		}

		return processedEntity;
	}

	@Override
	public List<T> saveAll(List<T> rules) {

		if (CollectionUtils.isEmpty(rules)) {
			return null;
		}
		List<T> savedRules = new ArrayList<>(rules.size());
		for (T rule : rules) {
			savedRules.add(save(rule));
		}
		return savedRules;
	}

	@SuppressWarnings("unchecked")
	@Override
	public T delete(Long id) {
		String zkPathId = ZookeeperConfigUtils.getDegradeRuleZkPath(String.valueOf(id));
		try {
			byte[] data = zkClient.getData().forPath(zkPathId);
			if (data == null || data.length == 0) {
				return null;
			}
			T entity = (T) decodeConverter.convert(new String(data, StandardCharsets.UTF_8));

			String zkPathApp = ZookeeperConfigUtils.getDegradeRuleZkPath(entity.getApp());

			Map<Long, DegradeRuleEntity> appEntities = (Map<Long, DegradeRuleEntity>) findBy(entity.getApp());
			if (appEntities != null) {
				appEntities.remove(entity.getId());
			}

			zkClient.delete().forPath(zkPathId);
			zkClient.setData().forPath(zkPathApp,
					mapEncodeConverter.convert(appEntities).getBytes(StandardCharsets.UTF_8));
			return entity;
		} catch (Exception e) {
			log.error("??????ID???" + id + " ?????????????????????????????????" + e.getMessage(), e);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public T findById(Long id) {
		String zkPathId = ZookeeperConfigUtils.getDegradeRuleZkPath(String.valueOf(id));
		try {
			Stat stat = zkClient.checkExists().forPath(zkPathId);
			if (stat == null) {
				return null;
			}
			byte[] data = zkClient.getData().forPath(zkPathId);
			if (data == null || data.length == 0) {
				return null;
			}
			return (T) decodeConverter.convert(new String(data, StandardCharsets.UTF_8));
		} catch (Exception e) {
			log.error("??????ID???" + id + " ?????????????????????????????????" + e.getMessage(), e);
		}
		return null;
	}

	@Override
	public List<T> findAllByMachine(MachineInfo machineInfo) {
		AssertUtil.notNull(machineInfo, "machineInfo cannot be null");
		AssertUtil.notEmpty(machineInfo.getApp(), "app cannot be empty");

		return findAllByApp(machineInfo.getApp());
	}

	@Override
	public List<T> findAllByApp(String appName) {
		AssertUtil.notEmpty(appName, "appName cannot be empty");
		Map<Long, T> entities = (Map<Long, T>) findBy(appName);
		if (entities == null) {
			return new ArrayList<>();
		}
		return new ArrayList<>(entities.values());
	}

	/**
	 * ??????appName??????????????????????????????ZK???????????????????????????????????????????????????map???key????????????????????????ID???value??????????????????????????????
	 * 
	 * @param app
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private Map<Long, T> findBy(String app) {
		AssertUtil.notEmpty(app, "appName cannot be empty");
		String zkPath = ZookeeperConfigUtils.getDegradeRuleZkPath(String.valueOf(app));
		try {
			Stat stat = zkClient.checkExists().forPath(zkPath);
			if (stat == null) {
				return null;
			}
			byte[] data = zkClient.getData().forPath(zkPath);
			if (data == null || data.length == 0) {
				return null;
			}
			return (Map<Long, T>) mapDecodeConverter.convert(new String(data, StandardCharsets.UTF_8));
		} catch (Exception e) {
			log.error("??????zkPath???" + zkPath + " ?????????????????????????????????" + e.getMessage(), e);
		}
		return null;
	}

	public void clearAll() {
		String degradeRuleRootPath = ZookeeperConfigUtils.getDegradeRulesConfigZkPath();
		try {
			zkClient.delete().deletingChildrenIfNeeded().forPath(degradeRuleRootPath);
		} catch (Exception e) {
			log.error("???ZK?????????" + degradeRuleRootPath + " ??????????????????????????????????????????" + e.getMessage(), e);
		}
	}

	protected T preProcess(T entity) {
		return entity;
	}

	/**
	 * Get next unused id.
	 *
	 * @return next unused id
	 */
	protected long nextId() {
		return idGenerator.nextId();
	}
}
