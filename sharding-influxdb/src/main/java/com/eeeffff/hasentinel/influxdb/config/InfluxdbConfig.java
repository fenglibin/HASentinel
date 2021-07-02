package com.eeeffff.hasentinel.influxdb.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({InfluxdbConfigProperties.class})
public class InfluxdbConfig {
	
}

