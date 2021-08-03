package com.eeeffff.hasentinel.cloud.gateway;

import java.util.function.Function;

import org.springframework.web.server.ServerWebExchange;

import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.BlockRequestHandler;
import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.GatewayCallbackManager;

/**
 * 
 * @author fenglibin
 * @date 2021年8月2日 下午2:56:08
 *
 */
public class HAGatewayCallbackManager {
	public static void setBlockHandler(BlockRequestHandler blockHandler) {
		GatewayCallbackManager.setBlockHandler(blockHandler);
	}

	public static void setRequestOriginParser(Function<ServerWebExchange, String> requestOriginParser) {
		GatewayCallbackManager.setRequestOriginParser(requestOriginParser);
	}
}
