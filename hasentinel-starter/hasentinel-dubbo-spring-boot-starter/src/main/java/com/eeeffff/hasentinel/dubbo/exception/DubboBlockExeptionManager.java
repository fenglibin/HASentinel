package com.eeeffff.hasentinel.dubbo.exception;

import com.alibaba.csp.sentinel.adapter.dubbo.fallback.DubboFallback;

/**
 * Dubbo异常处理管理器，提供用户针对指定的异常类型，自定义异常的处理
 * 
 * @author fenglibin
 *
 */
public class DubboBlockExeptionManager {
	
	public static class DubboProviderFallbackConfig extends DefaultDubboFallbackConfig {
	}

	public static class DubboConsumerFallbackConfig extends DefaultDubboFallbackConfig {
	}

	static class DefaultDubboFallbackConfig {
		/**
		 * 默认异常处理器
		 */
		private static DubboFallback defaultDubboFallback = new DefaultDubboFallback();
		/**
		 * 授权异常处理器
		 */
		private static DubboFallback authorityDubboFallback = new AuthorityDubboFallback();
		/**
		 * 降级异常处理器
		 */
		private static DubboFallback degradeDubboFallback = new DegradeDubboFallback();
		/**
		 * 限流异常处理器
		 */
		private static DubboFallback flowDubboFallback = new FlowDubboFallback();
		/**
		 * 热点参数异常处理器
		 */
		private static DubboFallback paramFlowDubboFallback = new ParamFlowDubboFallback();
		/**
		 * 系统异常处理器
		 */
		private static DubboFallback systemDubboFallback = new SystemDubboFallback();

		/**
		 * 获取授权异常的处理器
		 * 
		 * @return
		 */
		public static DubboFallback getAuthorityDubboFallback() {
			return authorityDubboFallback;
		}

		/**
		 * 设置授权异常的处理器
		 * 
		 * @param authorityDubboFallback
		 */
		public static void setAuthorityDubboFallback(DubboFallback authorityDubboFallback) {
			DefaultDubboFallbackConfig.authorityDubboFallback = authorityDubboFallback;
		}

		/**
		 * 获取默认异常的处理器
		 * 
		 * @return
		 */
		public static DubboFallback getDefaultDubboFallback() {
			return defaultDubboFallback;
		}

		/**
		 * 设置默认异常的处理器，所有的Dubbo限流异常都会通过该处理器处理
		 * 
		 * @param defaultDubboFallback
		 */
		public static void setDefaultDubboFallback(DubboFallback defaultDubboFallback) {
			DefaultDubboFallbackConfig.defaultDubboFallback = defaultDubboFallback;
			DefaultDubboFallbackConfig.authorityDubboFallback = defaultDubboFallback;
			DefaultDubboFallbackConfig.degradeDubboFallback = defaultDubboFallback;
			DefaultDubboFallbackConfig.flowDubboFallback = defaultDubboFallback;
			DefaultDubboFallbackConfig.paramFlowDubboFallback = defaultDubboFallback;
			DefaultDubboFallbackConfig.systemDubboFallback = defaultDubboFallback;
		}

		/**
		 * 获取降级异常的处理器
		 * 
		 * @return
		 */
		public static DubboFallback getDegradeDubboFallback() {
			return degradeDubboFallback;
		}

		/**
		 * 设置降级异常的处理器
		 * 
		 * @param degradeDubboFallback
		 */
		public static void setDegradeDubboFallback(DubboFallback degradeDubboFallback) {
			DefaultDubboFallbackConfig.degradeDubboFallback = degradeDubboFallback;
		}

		/**
		 * 获取限流异常的处理器
		 * 
		 * @return
		 */
		public static DubboFallback getFlowDubboFallback() {
			return flowDubboFallback;
		}

		/**
		 * 设置限流异常的处理器
		 * 
		 * @param flowDubboFallback
		 */
		public static void setFlowDubboFallback(DubboFallback flowDubboFallback) {
			DefaultDubboFallbackConfig.flowDubboFallback = flowDubboFallback;
		}

		/**
		 * 获取热点参数异常的处理器
		 * 
		 * @return
		 */
		public static DubboFallback getParamFlowDubboFallback() {
			return paramFlowDubboFallback;
		}

		/**
		 * 设置热点参数异常的处理器
		 * 
		 * @param paramFlowDubboFallback
		 */
		public static void setParamFlowDubboFallback(DubboFallback paramFlowDubboFallback) {
			DefaultDubboFallbackConfig.paramFlowDubboFallback = paramFlowDubboFallback;
		}

		/**
		 * 获取系统异常的处理器
		 * 
		 * @return
		 */
		public static DubboFallback getSystemDubboFallback() {
			return systemDubboFallback;
		}

		/**
		 * 设置系统异常的处理器
		 * 
		 * @param systemDubboFallback
		 */
		public static void setSystemDubboFallback(DubboFallback systemDubboFallback) {
			DefaultDubboFallbackConfig.systemDubboFallback = systemDubboFallback;
		}
	}
}
