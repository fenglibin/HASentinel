package com.alibaba.csp.sentinel.dashboard.util;

/**
 * 
 * @author fenglibin
 *
 */
public class DubboUtil {
	/**
	 * 根据请求资源的路径，判断当前请求是否Dubbo表求
	 * 
	 * @param resource
	 * @return
	 */
	public static boolean checkResourceIsDubboRequestRequest(String resource) {

		if (resource == null || resource.trim().length() == 0) {
			return false;
		}
		if (resource.startsWith("com.") || resource.startsWith("org.")) {
			return true;
		}
		// 判断请求的资源是否包含两个以上的点
		int dotNum = 0;
		while (resource.indexOf(".") > 0) {
			dotNum++;
			resource = resource.substring(resource.indexOf(".") + 1);
		}
		if (dotNum >= 2) {
			return true;
		}
		return false;
	}
}
