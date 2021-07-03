package com.alibaba.csp.sentinel.dashboard.rule.zookeeper;

import java.util.List;
import java.util.Map;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.AuthorityRuleEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.DegradeRuleEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.FlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.ParamFlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.SystemRuleEntity;
import com.alibaba.csp.sentinel.dashboard.discovery.AppInfo;
import com.alibaba.csp.sentinel.dashboard.rule.zookeeper.authority.AuthorityRuleZookeeperProvider;
import com.alibaba.csp.sentinel.dashboard.rule.zookeeper.authority.AuthorityRuleZookeeperPublisher;
import com.alibaba.csp.sentinel.dashboard.rule.zookeeper.flow.FlowRuleZookeeperProvider;
import com.alibaba.csp.sentinel.dashboard.rule.zookeeper.flow.FlowRuleZookeeperPublisher;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

import lombok.extern.slf4j.Slf4j;

/**
 * @author rodbate
 * @author fenglibin
 * @since 2019/04/20 15:02
 */
@Slf4j
@ConditionalOnProperty(value = "dynamic.rules.source.type", havingValue = "zookeeper")
@Configuration
@EnableConfigurationProperties(ZookeeperConfigProperties.class)
public class ZookeeperConfig {

	private static final int DEFAULT_ZK_SESSION_TIMEOUT = 30000;
	private static final int DEFAULT_ZK_CONNECTION_TIMEOUT = 10000;
	private static final int RETRY_TIMES = 3;
	private static final int SLEEP_TIME = 1000;

	public ZookeeperConfig() {
		log.info("============== Use Zookeeper Dynamic Rules Source ===================");
	}

	/**
	 * flow rule entity encoder
	 *
	 * @return <pre>{@code Converter<List < FlowRuleEntity>, String>}</pre>
	 */
	@Bean
	public Converter<List<FlowRuleEntity>, String> flowRuleEntityEncoder() {
		return JSON::toJSONString;
	}

	/**
	 * flow rule entity decoder
	 *
	 * @return <pre>{@code Converter<String, List < FlowRuleEntity>>}</pre>
	 */
	@Bean
	public Converter<String, List<FlowRuleEntity>> flowRuleEntityDecoder() {
		return s -> JSON.parseArray(s, FlowRuleEntity.class);
	}

	/**
	 * authority rule entity encoder
	 *
	 * @return <pre>{@code Converter<List < AuthorityRuleEntity>, String>}</pre>
	 */
	@Bean
	public Converter<List<AuthorityRuleEntity>, String> authorityRuleEntityEncoder() {
		return JSON::toJSONString;
	}

	/**
	 * authority rule entity decoder
	 *
	 * @return <pre>{@code Converter<String, List < AuthorityRuleEntity>>}</pre>
	 */
	@Bean
	public Converter<String, List<AuthorityRuleEntity>> authorityRuleEntityDecoder() {
		return s -> JSON.parseArray(s, AuthorityRuleEntity.class);
	}

	/**
	 * 应用所有服务器的实体编码器
	 *
	 * @return <pre>{@code Converter<AppInfo, String>}</pre>
	 */
	@Bean
	public Converter<AppInfo, String> appMachinesEntityEncoder() {
		return JSON::toJSONString;
	}

	/**
	 * 应用所有服务器的实体解码器
	 *
	 * @return <pre>{@code Converter<String, AppInfo>}</pre>
	 */
	@Bean
	public Converter<String, AppInfo> appMachinesEntityDecoder() {
		return s -> JSON.parseObject(s, AppInfo.class);
	}

	/**
	 * 应用降级实体编码器
	 *
	 * @return <pre>{@code Converter<DegradeRuleEntity, String>}</pre>
	 */
	@Bean
	public Converter<DegradeRuleEntity, String> appDegradeEntityEncoder() {
		return JSON::toJSONString;
	}

	/**
	 * 应用降级实体解码器，原始对象为单个的DegradeRuleEntity对象
	 *
	 * @return <pre>{@code Converter<String, DegradeRuleEntity>}</pre>
	 */
	@Bean
	public Converter<String, DegradeRuleEntity> appDegradeEntityDecoder() {
		return s -> JSON.parseObject(s, DegradeRuleEntity.class);
	}

	/**
	 * 应用降级实体编码器
	 *
	 * @return <pre>{@code Converter<DegradeRuleEntity, String>}</pre>
	 */
	@Bean
	public Converter<Map<Long, DegradeRuleEntity>, String> appMapDegradeEntityEncoder() {
		return JSON::toJSONString;
	}

	/**
	 * 应用降级实体解码器,
	 *
	 * @return <pre>{@code Converter<String, Map<Long, DegradeRuleEntity>>}</pre>
	 */
	@Bean
	public Converter<String, Map<Long, DegradeRuleEntity>> appMapDegradeEntityDecoder() {
		return s -> JSON.parseObject(s, new TypeReference<Map<Long, DegradeRuleEntity>>() {
		});
	}

	/**
	 * 应用热点参数实体编码器
	 *
	 * @return <pre>{@code Converter<ParamFlowRuleEntity, String>}</pre>
	 */
	@Bean
	public Converter<ParamFlowRuleEntity, String> appParamEntityEncoder() {
		return JSON::toJSONString;
	}

	/**
	 * 应用热点参数实体解码器，原始对象为单个的DegradeRuleEntity对象
	 *
	 * @return <pre>{@code Converter<String, ParamFlowRuleEntity>}</pre>
	 */
	@Bean
	public Converter<String, ParamFlowRuleEntity> appParamEntityDecoder() {
		return s -> JSON.parseObject(s, ParamFlowRuleEntity.class);
	}

	/**
	 * 应用热点参数实体编码器
	 *
	 * @return <pre>{@code Converter<ParamFlowRuleEntity, String>}</pre>
	 */
	@Bean
	public Converter<Map<Long, ParamFlowRuleEntity>, String> appMapParamEntityEncoder() {
		return JSON::toJSONString;
	}

	/**
	 * 应用热点参数实体解码器,
	 *
	 * @return <pre>{@code Converter<String, Map<Long, ParamFlowRuleEntity>>}</pre>
	 */
	@Bean
	public Converter<String, Map<Long, ParamFlowRuleEntity>> appMapParamEntityDecoder() {
		return s -> JSON.parseObject(s, new TypeReference<Map<Long, ParamFlowRuleEntity>>() {
		});
	}

	/**
	 * 应用热点参数实体编码器
	 *
	 * @return <pre>{@code Converter<SystemRuleEntity, String>}</pre>
	 */
	@Bean
	public Converter<SystemRuleEntity, String> appSystemEntityEncoder() {
		return JSON::toJSONString;
	}

	/**
	 * 应用热点参数实体解码器，原始对象为单个的DegradeRuleEntity对象
	 *
	 * @return <pre>{@code Converter<String, SystemRuleEntity>}</pre>
	 */
	@Bean
	public Converter<String, SystemRuleEntity> appSystemEntityDecoder() {
		return s -> JSON.parseObject(s, SystemRuleEntity.class);
	}

	/**
	 * 应用热点参数实体编码器
	 *
	 * @return <pre>{@code Converter<ParamFlowRuleEntity, String>}</pre>
	 */
	@Bean
	public Converter<Map<Long, SystemRuleEntity>, String> appMapSystemEntityEncoder() {
		return JSON::toJSONString;
	}

	/**
	 * 应用热点参数实体解码器,
	 *
	 * @return <pre>{@code Converter<String, Map<Long, ParamFlowRuleEntity>>}</pre>
	 */
	@Bean
	public Converter<String, Map<Long, SystemRuleEntity>> appMapSystemEntityDecoder() {
		return s -> JSON.parseObject(s, new TypeReference<Map<Long, SystemRuleEntity>>() {
		});
	}

	/**
	 * zk client
	 *
	 * @param properties zk properties
	 * @return zk client
	 */
	@Bean(destroyMethod = "close")
	public CuratorFramework zkClient(ZookeeperConfigProperties properties) {
		String connectString = properties.getConnectString();
		int sessionTimeout = DEFAULT_ZK_SESSION_TIMEOUT;
		int connectionTimeout = DEFAULT_ZK_CONNECTION_TIMEOUT;
		if (properties.getSessionTimeout() > 0) {
			sessionTimeout = properties.getSessionTimeout();
		}
		if (properties.getConnectionTimeout() > 0) {
			connectionTimeout = properties.getConnectionTimeout();
		}

		CuratorFramework zkClient = CuratorFrameworkFactory.newClient(connectString, sessionTimeout, connectionTimeout,
				new ExponentialBackoffRetry(SLEEP_TIME, RETRY_TIMES));
		zkClient.start();

		log.info(
				"Initialize zk client CuratorFramework, connectString={}, sessionTimeout={}, connectionTimeout={}, retry=[sleepTime={}, retryTime={}]",
				connectString, sessionTimeout, connectionTimeout, SLEEP_TIME, RETRY_TIMES);
		return zkClient;
	}

	@Bean
	public AuthorityRuleZookeeperProvider authorityRuleZookeeperProvider() {
		return new AuthorityRuleZookeeperProvider();
	}

	@Bean
	public AuthorityRuleZookeeperPublisher authorityRuleZookeeperPublisher() {
		return new AuthorityRuleZookeeperPublisher();
	}

	@Bean
	public FlowRuleZookeeperProvider flowRuleZookeeperProvider() {
		return new FlowRuleZookeeperProvider();
	}

	@Bean
	public FlowRuleZookeeperPublisher flowRuleZookeeperPublisher() {
		return new FlowRuleZookeeperPublisher();
	}
}
