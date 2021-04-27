package com.eeefff.hasentinel.dubbo.exception;

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

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author fenglibin
 *
 */
@Slf4j
public class HADubboConsumerFallback implements DubboFallback {

	@Override
	public Result handle(Invoker<?> invoker, Invocation invocation, BlockException e) {
		log.error("BlockException happened.");
		if (e instanceof AuthorityException) {
			return DubboBlockExeptionManager.DubboConsumerFallbackConfig.getAuthorityDubboFallback().handle(invoker, invocation, e);
		} else if (e instanceof DegradeException) {
			return DubboBlockExeptionManager.DubboConsumerFallbackConfig.getDegradeDubboFallback().handle(invoker, invocation, e);
		} else if (e instanceof FlowException) {
			return DubboBlockExeptionManager.DubboConsumerFallbackConfig.getFlowDubboFallback().handle(invoker, invocation, e);
		} else if (e instanceof ParamFlowException) {
			return DubboBlockExeptionManager.DubboConsumerFallbackConfig.getParamFlowDubboFallback().handle(invoker, invocation, e);
		} else if (e instanceof SystemBlockException) {
			return DubboBlockExeptionManager.DubboConsumerFallbackConfig.getSystemDubboFallback().handle(invoker, invocation, e);
		} else {
			return DubboBlockExeptionManager.DubboConsumerFallbackConfig.getDefaultDubboFallback().handle(invoker, invocation, e);
		}
	}

}
