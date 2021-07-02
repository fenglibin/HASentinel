package com.eeeffff.hasentinel.dubbo.exception;

import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;

import com.alibaba.csp.sentinel.adapter.dubbo.fallback.DubboFallback;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.SentinelRpcException;

import lombok.extern.slf4j.Slf4j;
/**
 * 
 * @author fenglibin
 *
 */
@Slf4j
public class DefaultDubboFallback implements DubboFallback {

	@Override
	public Result handle(Invoker<?> invoker, Invocation invocation, BlockException ex) {
		log.error("[Default]LD dubbo request blocked.");
		throw new SentinelRpcException(ex);
	}

}
