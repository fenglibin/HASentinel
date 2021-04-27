package com.eeefff.hasentinel.common.util;

import java.util.regex.Pattern;

import org.junit.Test;

import com.eeefff.hasentinel.common.AbstractTest;
import com.eeefff.hasentinel.common.util.StringUtil;

public class StringUtilTest extends AbstractTest {
	
	@Test
	public void testRestUrlToRegixUrl_1() {
		String url = "/{p1}/machine.json";
		System.out.println("原始URL："+url+"，处理后的URL："+StringUtil.restUrlToRegixUrl(url));
	}
	@Test
	public void testRestUrlToRegixUrl_2() {
		String url = "/{p1}/{p2}/machine.json";
		System.out.println("原始URL："+url+"，处理后的URL："+StringUtil.restUrlToRegixUrl(url));
	}
	@Test
	public void testRestUrlToRegixUrl_3() {
		String url = "/{p1}/xx/{p2}/machine.json";
		System.out.println("原始URL："+url+"，处理后的URL："+StringUtil.restUrlToRegixUrl(url));
	}
	@Test
	public void testRestUrlToRegixUrl_4() {
		String url = "/{p1}/xx/{p2}";
		System.out.println("原始URL："+url+"，处理后的URL："+StringUtil.restUrlToRegixUrl(url));
	}
	@Test
	public void testRestUrlToRegixUrl_5() {
		String url = "/xx/{p1}/{p2}";
		System.out.println("原始URL："+url+"，处理后的URL："+StringUtil.restUrlToRegixUrl(url));
	}
	@Test
	public void testRestUrlToRegixUrl_6() {
		String url = "/xx/{p1}/yy/{p2}";
		System.out.println("原始URL："+url+"，处理后的URL："+StringUtil.restUrlToRegixUrl(url));
	}
	@Test
	public void testRegex_1() {
		String url = "/xx/aaa/yy/bb";
		String regex = StringUtil.restUrlToRegixUrl("/xx/{p1}/yy/{p2}");
		boolean isMatch = Pattern.matches(regex, url);
		System.out.println("url:"+url+" match result "+isMatch);
	}
	@Test
	public void testRegex_2() {
		String url = "/xx/aaa/xxxxxxx/yy/bb";
		String regex = StringUtil.restUrlToRegixUrl("/xx/{p1}/yy/{p2}");
		boolean isMatch = Pattern.matches(regex, url);
		System.out.println("url:"+url+" match result "+isMatch);
	}
	@Test
	public void testRegex_3() {
		String url = "/xx/aaa-123/yy/bb";
		String regex = StringUtil.restUrlToRegixUrl("/xx/{p1}/yy/{p2}");
		boolean isMatch = Pattern.matches(regex, url);
		System.out.println("url:"+url+" match result "+isMatch);
	}
	@Test
	public void testRegex_4() {
		String url = "/xx/aaa_123/yy/bb";
		String regex = StringUtil.restUrlToRegixUrl("/xx/{p1}/yy/{p2}");
		boolean isMatch = Pattern.matches(regex, url);
		System.out.println("url:"+url+" match result "+isMatch);
	}
	@Test
	public void testRegex_5() {
		String url = "/xx/aaa_1-23/yy/bb";
		String regex = StringUtil.restUrlToRegixUrl("/xx/{p1}/yy/{p2}");
		boolean isMatch = Pattern.matches(regex, url);
		System.out.println("url:"+url+" match result "+isMatch);
	}
	
	@Test
	public void test_6() {
		String str = "/[a-zA-Z1-9_\\-]*/isit.json";
		System.out.println(str.indexOf(StringUtil.PATH_PATTERN));
		str = str.replace(StringUtil.PATH_PATTERN, "");
		System.out.println(str);
	}
}
