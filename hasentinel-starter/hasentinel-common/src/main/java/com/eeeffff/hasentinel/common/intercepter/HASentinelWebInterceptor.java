package com.eeeffff.hasentinel.common.intercepter;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.ResourceTypeConstants;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.adapter.spring.webmvc.SentinelWebInterceptor;
import com.alibaba.csp.sentinel.adapter.spring.webmvc.config.SentinelWebMvcConfig;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.util.StringUtil;
/**
 * Spring Web MVC interceptor that integrates with Sentinel.
 * @author fenglibin
 *
 */
public class HASentinelWebInterceptor extends SentinelWebInterceptor {

	private final SentinelWebMvcConfig config;

	public HASentinelWebInterceptor(SentinelWebMvcConfig config) {
		super(config);
		if (config == null) {
			this.config = new SentinelWebMvcConfig();
		} else {
			this.config = config;
		}

	}

	public HASentinelWebInterceptor() {
		this(new SentinelWebMvcConfig());
	}
	
	@Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
        throws Exception {
        try {
            String resourceName = getResourceName(request);

            if (StringUtil.isEmpty(resourceName)) {
                return true;
            }
            
            if (increaseReferece(request, this.config.getRequestRefName(), 1) != 1) {
                return true;
            }
            
            // Parse the request origin using registered origin parser.
            // 需要自定义获取origin的实现，授权规则才会生效，否则默认授权规则不生效，
            // 规则实现需继承com.alibaba.csp.sentinel.adapter.spring.webmvc.callback.RequestOriginParser
            // 实现实例：com.eeeffff.hasentinel.demo.spring.webmvc.handler.WhiteAppRequestOriginParser
            String origin = parseOrigin(request);
            String contextName = getContextName(request);
            ContextUtil.enter(contextName, origin);
            // Sentinel父类中默认没有将请求参数带入，热点参数规则需要传入参数，否则热点参数规则不生效
            // 热点参数校验方法：com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowSlot.checkFlow
            Entry entry = SphU.entry(resourceName, ResourceTypeConstants.COMMON_WEB, EntryType.IN,getParamValues(request));
            request.setAttribute(this.config.getRequestAttributeName(), entry);
            return true;
        } catch (BlockException e) {
            try {
                handleBlockException(request, response, e);
            } finally {
                ContextUtil.exit();
            }
            return false;
        }
    }
	/**
     * @param request
     * @param rcKey
     * @param step
     * @return reference count after increasing (initial value as zero to be increased) 
     */
    private Integer increaseReferece(HttpServletRequest request, String rcKey, int step) {
        Object obj = request.getAttribute(rcKey);
        
        if (obj == null) {
            // initial
            obj = Integer.valueOf(0);
        }
        
        Integer newRc = (Integer)obj + step;
        request.setAttribute(rcKey, newRc);
        return newRc;
    }
	/**
	 * 获取请求中所有的参数值
	 * 
	 * @param request
	 * @return 请求中所有的参数值的数组
	 */
	private Object[] getParamValues(HttpServletRequest request) {
		Object[] values = new Object[request.getParameterMap().size()];
		int index = 0;
		for (Map.Entry<String, String[]> param : request.getParameterMap().entrySet()) {
			String[] value = param.getValue();
			if (value != null && value.length > 0) {
				values[index] = value[0];
			}
			index++;
		}
		return values;
	}
}
