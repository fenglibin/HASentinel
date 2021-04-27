package com.alibaba.csp.sentinel.dashboard.util;

import org.junit.Assert;
import org.junit.Test;

import com.alibaba.csp.sentinel.dashboard.domain.vo.MetricType;

public class DubboUtilTest {
	@Test
	public void testCheckResourceIsDubboRequestRequest_1() {
		String resource = "com.xx.aa";
		boolean result = DubboUtil.checkResourceIsDubboRequestRequest(resource);
		Assert.assertTrue(result);
	}

	@Test
	public void testCheckResourceIsDubboRequestRequest_2() {
		String resource = "cn.xx.aa";
		boolean result = DubboUtil.checkResourceIsDubboRequestRequest(resource);
		Assert.assertTrue(result);
	}

	@Test
	public void testCheckResourceIsDubboRequestRequest_3() {
		String resource = "cn.xx";
		boolean result = DubboUtil.checkResourceIsDubboRequestRequest(resource);
		Assert.assertFalse(result);
	}

	@Test
	public void other1() {
		System.out.println(MetricType.WEB.name());
	}
}
