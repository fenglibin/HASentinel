package com.eeefff.hasentinel.influxdb.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.eeefff.hasentinel.influxdb.filter.CrossSiteFilter;

@Configuration
public class FilterConfig {
    @Bean
    public FilterRegistrationBean<CrossSiteFilter> filterRegistrationBean(){
        FilterRegistrationBean<CrossSiteFilter> bean = new FilterRegistrationBean<CrossSiteFilter>();
        bean.setFilter(new CrossSiteFilter());
        bean.addUrlPatterns("/*");
        return bean;
    }
}