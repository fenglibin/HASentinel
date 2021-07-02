package com.eeeffff.hasentinel.dubbo.config;

import com.alibaba.csp.sentinel.adapter.dubbo.origin.DubboOriginParser;

public class DubboNameParserManager {
	private static DubboOriginParser dubboOriginParser = null;

	public static DubboOriginParser getDubboOriginParser() {
		return dubboOriginParser;
	}

	public static void setDubboOriginParser(DubboOriginParser dubboOriginParser) {
		DubboNameParserManager.dubboOriginParser = dubboOriginParser;
	}
	
}
