package com.eeeffff.hasentinel.dubbo.exception;

import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;

import com.alibaba.csp.sentinel.adapter.dubbo.fallback.DubboFallback;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowException;
import com.alibaba.csp.sentinel.slots.system.SystemBlockException;

/**
 * 
 * @author fenglibin
 *
 */
public class HADubboProviderFallback implements DubboFallback {

	@Override
	public Result handle(Invoker<?> invoker, Invocation invocation, BlockException e) {
		if (e instanceof AuthorityException) {
			return DubboBlockExeptionManager.DubboProviderFallbackConfig.getAuthorityDubboFallback().handle(invoker, invocation, e);
		} else if (e instanceof DegradeException) {
			return DubboBlockExeptionManager.DubboProviderFallbackConfig.getDegradeDubboFallback().handle(invoker, invocation, e);
		} else if (e instanceof FlowException) {
			return DubboBlockExeptionManager.DubboProviderFallbackConfig.getFlowDubboFallback().handle(invoker, invocation, e);
		} else if (e instanceof ParamFlowException) {
			return DubboBlockExeptionManager.DubboProviderFallbackConfig.getParamFlowDubboFallback().handle(invoker, invocation, e);
		} else if (e instanceof SystemBlockException) {
			return DubboBlockExeptionManager.DubboProviderFallbackConfig.getSystemDubboFallback().handle(invoker, invocation, e);
		} else {
			return DubboBlockExeptionManager.DubboProviderFallbackConfig.getDefaultDubboFallback().handle(invoker, invocation, e);
		}
	}

}
