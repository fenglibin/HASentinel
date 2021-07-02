package com.eeeffff.hasentinel.demo.spring.webmvc.handler;

import javax.servlet.http.HttpServletRequest;

import com.alibaba.csp.sentinel.adapter.spring.webmvc.callback.RequestOriginParser;

/**
 * 授权规则处理示例： <br>
 * 针对配置了授权规则的资源，仅允许在授权规则定义的白名单之内的APP进行访问，在黑名单之中的、或者没有定义的APP访问时，全部都拒绝
 * 
 * @author fenglibin
 *
 */
public class WhiteAppRequestOriginParser implements RequestOriginParser {
	private static final String BLACK = "BLACK";

	@Override
	public String parseOrigin(HttpServletRequest request) {
		String app = request.getParameter("app");
		if (app == null || app.trim().length() == 0) {
			return BLACK;
		}
		return app;
	}

}
