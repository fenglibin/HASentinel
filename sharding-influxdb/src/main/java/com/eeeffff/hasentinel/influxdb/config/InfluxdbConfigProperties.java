package com.eeeffff.hasentinel.influxdb.config;

import javax.annotation.PostConstruct;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@ConfigurationProperties(prefix = "influxdb")
public class InfluxdbConfigProperties {
	@Getter
	@Setter
	// url可能是多个url，以英文的逗号","分隔
	private String urls;
	@Getter
	@Setter
	private String username;
	@Getter
	@Setter
	private String password;
	private String[] urlArr;

	public String[] getUrlArr() {
		return urlArr;
	}

	@PostConstruct
	public void checkParams() {
		if (username == null || username.trim().length() == 0) {
			throw new RuntimeException("influxdb.username属性没有设置");
		}
		if (password == null || password.trim().length() == 0) {
			throw new RuntimeException("influxdb.password属性没有设置");
		}
		if (urls == null || urls.trim().length() == 0) {
			throw new RuntimeException("influxdb.urls属性没有设置");
		}
		urlArr = urls.split(",");
	}
}
