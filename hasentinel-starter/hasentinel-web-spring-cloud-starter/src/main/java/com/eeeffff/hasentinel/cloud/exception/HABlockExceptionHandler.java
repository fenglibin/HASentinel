package com.eeeffff.hasentinel.cloud.exception;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.csp.sentinel.adapter.spring.webmvc.callback.BlockExceptionHandler;
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
public class HABlockExceptionHandler implements BlockExceptionHandler {

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response, BlockException e) throws Exception {
		if (e instanceof AuthorityException) {
			WebBlockExeptionManager.getAuthorityExceptionHandler().handle(request, response, e);
		} else if (e instanceof DegradeException) {
			WebBlockExeptionManager.getDegradeExceptionHandler().handle(request, response, e);
		} else if (e instanceof FlowException) {
			WebBlockExeptionManager.getFlowExceptionHandler().handle(request, response, e);
		} else if (e instanceof ParamFlowException) {
			WebBlockExeptionManager.getParamFlowExceptionHandler().handle(request, response, e);
		} else if (e instanceof SystemBlockException) {
			WebBlockExeptionManager.getSystemBlockExceptionHandler().handle(request, response, e);
		} else {
			WebBlockExeptionManager.getDefaultExceptionHandler().handle(request, response, e);
		}

	}

}
