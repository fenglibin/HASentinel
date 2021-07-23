package com.alibaba.csp.sentinel.dashboard.util;

import com.alibaba.csp.sentinel.dashboard.domain.vo.MetricType;

public class MetricUtil {
	/**
	 * 根据请求资源的路径，判断当前请求是否Web表求
	 * 
	 * @param resource
	 * @return
	 */
	public static boolean checkIsWebRequestByResource(String resource) {
		if (resource.startsWith("/") || resource.startsWith("http://") || resource.startsWith("https://") || resource.indexOf("/") > 0) {
			return true;
		}
		return false;
	}

	/**
	 * 根据请求资源的路径，判断当前请求是否Dubbo表求
	 * 
	 * @param resource
	 * @return
	 */
	public static boolean checkIsDubboRequestByResource(String resource) {
		return DubboUtil.checkResourceIsDubboRequestRequest(resource);
	}

	/**
	 * 获取请求的类型，以"/"开头表示WEB请求，其它默认为DUBBO请求
	 * 
	 * @param resource
	 * @return
	 */
	public static String getRequestTypeByResource(String resource) {
		if (checkIsWebRequestByResource(resource)) {
			return MetricType.WEB.name();
		} else if (checkIsDubboRequestByResource(resource)) {
			return MetricType.DUBBO.name();
		} else {
			return MetricType.OTHER.name();
		}
	}
}
