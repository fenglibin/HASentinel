package com.eeefff.hasentinel.dubbo.config;

import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;

import com.alibaba.csp.sentinel.adapter.dubbo.origin.DefaultDubboOriginParser;
import com.alibaba.csp.sentinel.adapter.dubbo.origin.DubboOriginParser;

/**
 * Dubbo名称解析实现类
 * 
 * @author fenglibin
 *
 */
public class HADubboOriginParser implements DubboOriginParser {
	private static DubboOriginParser dubboOriginParser = new DefaultDubboOriginParser();

	@Override
	public String parse(Invoker<?> invoker, Invocation invocation) {
		if (DubboNameParserManager.getDubboOriginParser() != null) {
			return DubboNameParserManager.getDubboOriginParser().parse(invoker, invocation);
		} else {
			return dubboOriginParser.parse(invoker, invocation);
		}
	}

}
