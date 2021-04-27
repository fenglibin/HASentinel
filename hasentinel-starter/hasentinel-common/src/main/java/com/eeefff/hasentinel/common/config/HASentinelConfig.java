package com.eeefff.hasentinel.common.config;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.alibaba.csp.sentinel.init.InitExecutor;
import com.eeefff.hasentinel.common.datasource.ZookeeperConfigProperties;
import com.eeefff.hasentinel.common.util.NetUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * @author fenglibin
 */
@Slf4j
@Configuration
@EnableConfigurationProperties({ HASentineConfigProperties.class, ZookeeperConfigProperties.class })
public class HASentinelConfig {

	@Autowired
	private HASentineConfigProperties sentineConfigProperties;
	private static HASentineConfigProperties staticSentineConfigProperties;
	@Autowired
	private ZookeeperConfigProperties zookeeperConfigProperties;
	private static ZookeeperConfigProperties staticZookeeperConfigProperties;

	// 当前应用归属的团队
	@Value("${project.team:default}")
	private String projectTeam;
	
	// 项目的名称
	@Value("${project.name:#{null}}")
	private String projectName;
	// 项目的名称（用于兼容现有的项目名称）
	@Value("${spring.application.name:#{null}}")
	private String applicationName;
	
	// 用于表示项目启动时，是否初使化往Sentinel控制台的注册连接
	@Value("${project.init.connection:#{true}}")
	private boolean projectInitConnection;
	
	

	public HASentinelConfig() {
		log.info("============== Laidian Sentinel Config loaded ===================");
	}

	@PostConstruct
	public void init() {
		staticZookeeperConfigProperties = zookeeperConfigProperties;
		staticSentineConfigProperties = sentineConfigProperties;
		if (applicationName != null) {
			System.setProperty("project.name", applicationName);
		} else if (projectName != null) {
			System.setProperty("project.name", projectName);
		}
		if (sentineConfigProperties.getSentinelServer() != null) {
			System.setProperty("csp.sentinel.dashboard.server", sentineConfigProperties.getSentinelServer());
		}
		boolean isPortUsing = NetUtil.isLoclePortUsing(sentineConfigProperties.getApiPort());
		if (isPortUsing) {
			sentineConfigProperties.setApiPort(sentineConfigProperties.getApiRandomPort());
		}
		System.setProperty("csp.sentinel.api.port", String.valueOf(sentineConfigProperties.getApiPort()));
		System.setProperty("csp.sentinel.log.dir", sentineConfigProperties.getLogDir());
		System.setProperty("csp.sentinel.log.use.pid", sentineConfigProperties.getLogUsePid());
		System.setProperty("csp.sentinel.heartbeat.interval.ms",
				String.valueOf(sentineConfigProperties.getHeartbeatIntervalMS()));
		if (sentineConfigProperties.getHeartBeatClientHost() != null && sentineConfigProperties.getHeartBeatClientHost().trim().length() > 0) {
			System.setProperty("csp.sentinel.heartbeat.client.ip", sentineConfigProperties.getHeartBeatClientHost());
		}
		if (projectInitConnection) {
			/**
			 * 初使化Sentinel相关资源的连接，如初使化ZK连接、自动往Sentinel控制台发起注册等
			 */
			new Thread(() -> InitExecutor.doInit()).start();
		}
	}

	/**
	 * 获取ZK的配置
	 * 
	 * @return
	 */
	public static ZookeeperConfigProperties getZookeeperConfigProperties() {
		return staticZookeeperConfigProperties;
	}

	public static void setZookeeperConfigProperties(ZookeeperConfigProperties staticZookeeperConfigProperties) {
		HASentinelConfig.staticZookeeperConfigProperties = staticZookeeperConfigProperties;
	}

	/**
	 * 获取Sentinel的配置
	 * 
	 * @return
	 */
	public static HASentineConfigProperties getSentineConfigProperties() {
		return staticSentineConfigProperties;
	}

	public static void setSentineConfigProperties(HASentineConfigProperties staticSentineConfigProperties) {
		HASentinelConfig.staticSentineConfigProperties = staticSentineConfigProperties;
	}

	@Bean
	public MetricReporter getMetricReporter() {
		MetricReporter metricReporter = new MetricReporter();
		return metricReporter;
	}
	
	public String getProjectTeam() {
		return projectTeam;
	}

	/**
	 * 获取应用名称
	 * @return
	 */
	public String getProjectName() {
		return projectName;
	}
	
	
}
