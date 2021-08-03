package com.eeeffff.hasentinel.cloud.gateway.filter;

import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.ResourceTypeConstants;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.adapter.spring.webmvc.AbstractSentinelInterceptor;
import com.alibaba.csp.sentinel.adapter.spring.webmvc.callback.UrlCleaner;
import com.alibaba.csp.sentinel.adapter.spring.webmvc.config.SentinelWebMvcConfig;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.util.StringUtil;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * 对Spring Gateway定义的Controller对外的接口进行拦截统计
 * 
 * @author fenglibin
 * @date 2021年8月3日 上午10:46:56
 *
 */
@Slf4j
public class HASentinelGatewayWebFilter implements WebFilter,Ordered {
	public HASentinelGatewayWebFilter() {

	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

		// 后端应用的路由请求及Gateway本身提供的Controller接口请求，都会经过该Filter，此处需要区别对待
		Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
		if (route != null) {
			return chain.filter(exchange);
		}

		log.info("Enter into web filter.");
		SentinelWebMvcConfig config = new SentinelWebMvcConfig();
		config.setHttpMethodSpecify(true);

		String origin = null;

		String resourceName = getResourceName(config, exchange);
		String contextName = getContextName(config, exchange);

		ContextUtil.enter(contextName, origin);
		// Sentinel父类中默认没有将请求参数带入，热点参数规则需要传入参数，否则热点参数规则不生效
		// 热点参数校验方法：com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowSlot.checkFlow
		try {
			Entry entry = SphU.entry(resourceName, ResourceTypeConstants.COMMON_WEB, EntryType.IN, null);
			exchange.getAttributes().put(config.getRequestAttributeName(), entry);
			return chain.filter(exchange);
		} catch (BlockException e) {
			log.error(e.getMessage(), e);
			ServerHttpResponse response = exchange.getResponse();
			DataBuffer buffer = response.bufferFactory().wrap("Access blocked by system.".getBytes());
			return response.writeWith(Mono.just(buffer));
		}
	}

	/**
	 * 获取请求的资源路径
	 * 
	 * @param config
	 * @param exchange
	 * @return
	 */
	protected String getResourceName(SentinelWebMvcConfig config, ServerWebExchange exchange) {
		String resourceName = exchange.getRequest().getPath().value();
		UrlCleaner urlCleaner = config.getUrlCleaner();
		if (urlCleaner != null) {
			resourceName = urlCleaner.clean(resourceName);
		}
		// Add method specification if necessary
		if (StringUtil.isNotEmpty(resourceName) && config.isHttpMethodSpecify()) {
			resourceName = exchange.getRequest().getMethodValue().toUpperCase() + ":" + resourceName;
		}
		return resourceName;
	}

	protected String getContextName(SentinelWebMvcConfig config, ServerWebExchange exchange) {
		if (config.isWebContextUnify()) {
			return AbstractSentinelInterceptor.SENTINEL_SPRING_WEB_CONTEXT_NAME;
		}

		return getResourceName(config, exchange);
	}

	@Override
	public int getOrder() {
		return 0;
	}
}
