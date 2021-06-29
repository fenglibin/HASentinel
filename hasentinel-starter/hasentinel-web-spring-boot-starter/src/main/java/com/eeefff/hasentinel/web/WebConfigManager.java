package com.eeefff.hasentinel.web;

import java.util.HashSet;
import java.util.Set;

import com.alibaba.csp.sentinel.adapter.spring.webmvc.callback.RequestOriginParser;
import com.alibaba.csp.sentinel.adapter.spring.webmvc.callback.UrlCleaner;

/**
 * Sentinel
 * Web配置管理器，用于让应用自定义一些实现，如{@link UrlCleaner}、{@link RequestOriginParser}等
 * 
 * @author fenglibin
 *
 */
public class WebConfigManager {
	/**
	 * Url清理实现，主要作用是将REST类型的URL标识为相同的URL（默认值为null，即不会对URL进行处理），<br>
	 * 实现类需要实现接口：com.alibaba.csp.sentinel.adapter.spring.webmvc.callback.UrlCleaner
	 */
	private static UrlCleaner urlCleaner;
	/**
	 * 用于解析请求的来源，应用于授权规则，授权规则用于判断来源是否对应流控应用中配置的应用，并对其实行白名单或黑名单控制，<br>
	 * 实现类需要实现接口：com.alibaba.csp.sentinel.adapter.spring.webmvc.callback.RequestOriginParser<br>
	 * 默认值为null，即授权规则中配置的任何规则都不会有效果，需要自己实现接口
	 */
	private static RequestOriginParser originParser;

	// 通过BeanPostProcess自动从Controller中解析出来的Rest URL
	private static Set<String> restUrls = new HashSet<String>();

	/**
	 * 获取UrlCleaner，主要作用是将REST类型的URL标识为相同的URL，否则有可能会造成内存溢出
	 * 
	 * @return
	 */
	public static UrlCleaner getUrlCleaner() {
		return urlCleaner;
	}

	/**
	 * 设置UrlCleaner，主要作用是将REST类型的URL标识为相同的URL，否则有可能会造成内存溢出
	 * 
	 * @param urlCleaner
	 */
	public static void setUrlCleaner(UrlCleaner urlCleaner) {
		WebConfigManager.urlCleaner = urlCleaner;
	}

	/**
	 * 获取WEB请求来源的解析，来源可以是IP、定义的某个参数等，这个要配置Sentinel控制后台的授权规则一起使用
	 * 
	 * @return
	 */
	public static RequestOriginParser getOriginParser() {
		return originParser;
	}

	/**
	 * 设置WEB请求来源的解析，来源可以是IP、定义的某个参数等，这个要配置Sentinel控制后台的授权规则一起使用
	 * 
	 * @param originParser
	 */
	public static void setOriginParser(RequestOriginParser originParser) {
		WebConfigManager.originParser = originParser;
	}
	
	public static Set<String> getRestUrls() {
		return restUrls;
	}

	public static void setRestUrls(Set<String> restUrls) {
		WebConfigManager.restUrls = restUrls;
	}

}
