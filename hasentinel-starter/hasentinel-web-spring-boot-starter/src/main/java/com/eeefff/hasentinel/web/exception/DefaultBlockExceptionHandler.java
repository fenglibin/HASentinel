package com.eeefff.hasentinel.web.exception;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.csp.sentinel.adapter.spring.webmvc.callback.BlockExceptionHandler;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.util.StringUtil;
/**
 * 
 * @author fenglibin
 *
 */
public class DefaultBlockExceptionHandler implements BlockExceptionHandler{

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response, BlockException e) throws Exception {
		
		// Return 429 (Too Many Requests) by default.
        response.setStatus(429);

        StringBuffer url = request.getRequestURL();

        if ("GET".equals(request.getMethod()) && StringUtil.isNotBlank(request.getQueryString())) {
            url.append("?").append(request.getQueryString());
        }

        PrintWriter out = response.getWriter();
        out.print("[LD]You request are blocked. Come back later.");
        out.flush();
        out.close();
		
	}

}
