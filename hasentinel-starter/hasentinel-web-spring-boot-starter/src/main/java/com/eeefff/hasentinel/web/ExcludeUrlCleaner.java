package com.eeefff.hasentinel.web;

import com.alibaba.csp.sentinel.adapter.spring.webmvc.callback.UrlCleaner;

import lombok.extern.slf4j.Slf4j;

/**
* 用于排除不需要统计的URL
* @author fenglibin
*/
@Slf4j
public class ExcludeUrlCleaner implements UrlCleaner {
	private String[] excludeUrls = null;

	public ExcludeUrlCleaner(String excludeUrl) {
		if (excludeUrl != null && excludeUrl.trim().length() > 0) {
			excludeUrls = excludeUrl.split(",");
		}
		log.info("Sentinel exclude URL prefixs:" + excludeUrl);
	}

	@Override
	public String clean(String originUrl) {
		if (excludeUrls == null || originUrl == null) {
			return originUrl;
		}
		for (String restUrl : excludeUrls) {
			if (originUrl.startsWith(restUrl)) {
				// 不统计
				return null;
			}
		}
		return originUrl;
	}

}
