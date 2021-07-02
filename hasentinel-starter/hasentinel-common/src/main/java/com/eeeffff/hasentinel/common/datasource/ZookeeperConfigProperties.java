package com.eeeffff.hasentinel.common.datasource;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * ZK配置项属性文件
 * 
 * @author fenglibin
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "zookeeper.config")
public class ZookeeperConfigProperties {
	private String connectString;
	private int sessionTimeout;
	private int connectionTimeout;
}
