package com.alibaba.csp.sentinel.context;

import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.context.ContextUtil;

public class HAContextUtil extends ContextUtil {
	public static Context replaceContext(Context newContext) {
		return ContextUtil.replaceContext(newContext);
	}
}
