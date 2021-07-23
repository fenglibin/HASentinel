package com.alibaba.csp.sentinel.dashboard.util;

import java.util.UUID;
/**
 * 获取32位的唯一值
 * @author fenglibin
 * @date 2021年7月23日 下午5:46:07
 *
 */
public class UniqUtil {
	public static String getUniq32Key() {
		return MD5Util.md5Hex(UUID.randomUUID().toString());
	}
}
