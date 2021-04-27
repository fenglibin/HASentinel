package com.alibaba.csp.sentinel.dashboard.util;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/*
 * 类CookieUtil.java的实现描述：TODO 类实现描述
 * @author ddp1j32 2015-2-28 下午2:32:10
 */
public abstract class CookieUtil {

	/**
	 * 将cookie转换成map的格式
	 * 
	 * @param req
	 * @return
	 */
	public static Map<String, String> getCookies(HttpServletRequest req) {
		Map<String, String> cookieMap = new HashMap<String, String>();
		Cookie[] cookies = req.getCookies();
		if (cookies != null && cookies.length > 0) {
			for (Cookie cookie : cookies) {
				cookieMap.put(cookie.getName(), cookie.getValue());
			}
		}
		return cookieMap;
	}

	public static void addCookie(HttpServletRequest request, HttpServletResponse response, String cookieName,
			String cookieValue) {
		Cookie cookie = CookieUtil.getCookieByCookieName(request, cookieName);
		if (null == cookie) {
			cookie = new Cookie(cookieName, cookieValue);
			// 一小时有效
			cookie.setMaxAge(2 * 60 * 60);
			response.addCookie(cookie);
		}
	}

	/**
	 * 根据传入的属性获取Cookie
	 * 
	 * @param cookieName
	 * @param cookeValue
	 * @param maxAge
	 * @param cookiePath
	 * @return
	 */
	public static Cookie getCookie(String cookieName, String cookeValue, int maxAge, String cookiePath) {
		Cookie cookie = new Cookie(cookieName, cookeValue);
		cookie.setMaxAge(maxAge);
		cookie.setPath(cookiePath);
		return cookie;
	}

	/**
	 * 从cooke获取值
	 * 
	 * @param req
	 * @param cookieName
	 * @return
	 */
	public static Cookie getCookieByCookieName(HttpServletRequest req, String cookieName) {
		Cookie[] cookies = req.getCookies();
		if (cookies != null && cookies.length > 0) {
			for (Cookie cookie : cookies) {
				if (cookie.getName().equals(cookieName)) {
					return cookie;
				}
			}
		}
		return null;
	}

	public static String getAfidByCookieName(HttpServletRequest req, String cookieName) {
		Cookie[] cookies = req.getCookies();
		if (cookies != null && cookies.length > 0) {
			for (Cookie cookie : cookies) {
				if (cookie.getName().equals(cookieName)) {
					return cookie.getValue();
				}
			}
		}
		return null;
	}

	public static void removeCookie(HttpServletRequest request, HttpServletResponse response, String cookieName) {
		Cookie cookie = CookieUtil.getCookieByCookieName(request, cookieName);
		if (null != cookie) {
			cookie.setValue("");
			cookie.setMaxAge(0);
			response.addCookie(cookie);
		}
	}
}
