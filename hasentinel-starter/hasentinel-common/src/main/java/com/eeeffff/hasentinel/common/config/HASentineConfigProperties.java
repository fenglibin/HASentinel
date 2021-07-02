package com.eeeffff.hasentinel.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

/**
 * Sentinel配置项属性文件
 * 
 * @author fenglibin
 */

@ConfigurationProperties(prefix = "sentinel")
public class HASentineConfigProperties {

	// Sentinel控制台地址
	@Setter
	private String sentinelServer;
	private static final String sentinelServerLocal = "localhost:20440";

	// Zookeeper地址
	@Setter
	private String zookeeperAddress;
	private static final String zookeeperAddressLocal = "localhost:2181";

	@Setter
	@Getter
	// 当前应用提供metric数据的api端口，如果未设置，则默认使用54321做为端口
	private int apiPort = 54321;

	@Setter
	@Getter
	// 当前应用提供metric数据的api端口，如果默认端口12345已经被占用了，则默认使用30000~40000的随机端口
	private int apiRandomPort = 30000 + (int) (10000 * Math.random());

	@Setter
	@Getter
	// 日志输出路径，Sentinel默认会将日志输出到用户目录{@code ${user.home}/logs/csp/}下
	private String logDir = "logs/csp";

	@Setter
	@Getter
	// 默认的日志文件名没有包括PID，但如果在同一台服务器上运行了多个实例，
	// 可能需要在日志文件中加上PID用于区分不同的日志文件。
	private String logUsePid = "true";

	@Setter
	@Getter
	// 应用端与控制台的心台检测时间，默认为50秒钟
	private int heartbeatIntervalMS = 50_000;

	@Setter
	@Getter
	// 需要排除Sentinel统计的URL的前缀，多个Rest URL以英文逗号分隔
	private String excludeUrlPrefix = null;

	@Setter
	@Getter
	// 是否开启Dubbo consumer
	// filter:com.alibaba.csp.sentinel.adapter.dubbo.SentinelDubboConsumerFilter
	private boolean enableConsumerFilter = true;

	@Setter
	@Getter
	// 指定客户端ＩＰ地址，在有些多网卡的场景，获取的ＩＰ不正确，可以通过手动指定ＩＰ的方式
	private String heartBeatClientHost = null;

	/**
	 * 获取Sentinel控制台的地址
	 * 
	 * @return
	 */
	public String getSentinelServer() {
		// 如果有配置，则使用配置的值
		if (sentinelServer != null && sentinelServer.trim().length() > 0) {
			return sentinelServer;
		}
		return sentinelServerLocal;
	}

	public String getZookeeperAddress() {
		// 如果有配置，则使用配置的值
		if (zookeeperAddress != null && zookeeperAddress.trim().length() > 0) {
			return zookeeperAddress;
		}
		return zookeeperAddressLocal;
	}

}