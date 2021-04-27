package com.alibaba.csp.sentinel.dashboard.rule.zookeeper;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author rodbate
 * @since 2019/04/20 14:58
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "zookeeper.config")
public class ZookeeperConfigProperties {
	private String connectString;
	private int sessionTimeout;
	private int connectionTimeout;
}
