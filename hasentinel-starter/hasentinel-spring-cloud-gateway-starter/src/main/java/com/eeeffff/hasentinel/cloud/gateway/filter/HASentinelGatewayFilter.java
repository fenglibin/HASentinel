package com.eeeffff.hasentinel.cloud.gateway.filter;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.web.server.ServerWebExchange;

import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.ResourceTypeConstants;
import com.alibaba.csp.sentinel.adapter.gateway.common.SentinelGatewayConstants;
import com.alibaba.csp.sentinel.adapter.gateway.common.param.GatewayParamParser;
import com.alibaba.csp.sentinel.adapter.gateway.sc.ServerWebExchangeItemParser;
import com.alibaba.csp.sentinel.adapter.gateway.sc.api.GatewayApiMatcherManager;
import com.alibaba.csp.sentinel.adapter.gateway.sc.api.matcher.WebExchangeApiMatcher;
import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.GatewayCallbackManager;
import com.alibaba.csp.sentinel.adapter.reactor.ContextConfig;
import com.alibaba.csp.sentinel.adapter.reactor.EntryConfig;
import com.alibaba.csp.sentinel.adapter.reactor.SentinelReactorTransformer;
import com.alibaba.csp.sentinel.context.HAContextUtil;

import ch.qos.logback.core.util.ContextUtil;
import reactor.core.publisher.Mono;

/**
 * 该类Copy自com.alibaba.csp.sentinel.adapter.gateway.sc.SentinelGatewayFilter，
 * 对原来传入EntryConfig中的routeId修改为path，
 * 原来为：
 * new EntryConfig(routeId, ResourceTypeConstants.COMMON_API_GATEWAY, EntryType.IN, 1, params, new ContextConfig(contextName(routeId), origin))
 * 修改为：
 * new EntryConfig(path, ResourceTypeConstants.COMMON_API_GATEWAY, EntryType.IN, 1, params, new ContextConfig(contextName(path), origin))
 * 因为routeId不能够获取到完整的路径。
 * 
 * @author fenglibin
 * @date 2021年8月2日 下午4:48:55
 *
 */
public class HASentinelGatewayFilter implements GatewayFilter, GlobalFilter, Ordered {
	private final int order;

	public HASentinelGatewayFilter() {
		this(Ordered.HIGHEST_PRECEDENCE);
	}

	public HASentinelGatewayFilter(int order) {
		this.order = order;
	}

	private final GatewayParamParser<ServerWebExchange> paramParser = new GatewayParamParser<>(new ServerWebExchangeItemParser());

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);

		String path = exchange.getRequest().getMethodValue().toUpperCase() + ":" + exchange.getRequest().getPath().value();

		Mono<Void> asyncResult = chain.filter(exchange);
		if (route != null) {
			String routeId = route.getId();
			Object[] params = paramParser.parseParameterFor(routeId, exchange, r -> r.getResourceMode() == SentinelGatewayConstants.RESOURCE_MODE_ROUTE_ID);
			String origin = Optional.ofNullable(GatewayCallbackManager.getRequestOriginParser()).map(f -> f.apply(exchange)).orElse("");
			asyncResult = asyncResult.transform(new SentinelReactorTransformer<>(new EntryConfig(path, ResourceTypeConstants.COMMON_API_GATEWAY, EntryType.IN, 1, params, new ContextConfig(contextName(routeId), origin))));
		}

		Set<String> matchingApis = pickMatchingApiDefinitions(exchange);
		for (String apiName : matchingApis) {
			Object[] params = paramParser.parseParameterFor(apiName, exchange, r -> r.getResourceMode() == SentinelGatewayConstants.RESOURCE_MODE_CUSTOM_API_NAME);
			asyncResult = asyncResult.transform(new SentinelReactorTransformer<>(new EntryConfig(apiName, ResourceTypeConstants.COMMON_API_GATEWAY, EntryType.IN, 1, params)));
		}

		return asyncResult;
	}

	protected Set<String> pickMatchingApiDefinitions(ServerWebExchange exchange) {
		return GatewayApiMatcherManager.getApiMatcherMap().values().stream().filter(m -> m.test(exchange)).map(WebExchangeApiMatcher::getApiName).collect(Collectors.toSet());
	}

	protected String contextName(String route) {
		return SentinelGatewayConstants.GATEWAY_CONTEXT_ROUTE_PREFIX + route;
	}

	@Override
	public int getOrder() {
		return order;
	}
}
