package com.eeefff.hasentinel.demo.spring.webmvc.handler;

import java.util.Objects;

import com.alibaba.csp.sentinel.adapter.spring.webmvc.callback.UrlCleaner;

public class TestUrlCleaner implements UrlCleaner {

	@Override
	public String clean(String originUrl) {
		if(Objects.isNull(originUrl)) {
			return originUrl;
		}
		if(originUrl.startsWith("/uri/xx")) {
			return "/uri/xx";
		}
		return originUrl;
	}

}
