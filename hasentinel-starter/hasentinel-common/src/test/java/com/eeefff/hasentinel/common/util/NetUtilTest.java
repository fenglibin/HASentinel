package com.eeefff.hasentinel.common.util;

import org.junit.Assert;
import org.junit.Test;

import com.eeefff.hasentinel.common.AbstractTest;
import com.eeefff.hasentinel.common.util.NetUtil;

public class NetUtilTest extends AbstractTest{
	@Test
	public void testIsLoclePortUsing() {
		boolean isUsing = NetUtil.isLoclePortUsing(8810);
		Assert.assertTrue(isUsing);
	}
	@Test
	public void testGetLinuxLocalIp() {
		String ipaddress = NetUtil.getLinuxLocalIp();
		System.out.println(ipaddress);
	}
}
