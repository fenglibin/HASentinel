package com.eeeffff.hasentinel.web.exception;

import com.alibaba.csp.sentinel.adapter.spring.webmvc.callback.BlockExceptionHandler;

/**
 * Web异常处理管理器，提供用户针对指定的异常类型，自定义异常的处理
 * 
 * @author fenglibin
 *
 */
public class WebBlockExeptionManager {
	/**
	 * 默认异常处理器
	 */
	private static BlockExceptionHandler defaultExceptionHandler = new DefaultBlockExceptionHandler();
	/**
	 * 授权异常处理器
	 */
	private static BlockExceptionHandler authorityExceptionHandler = new AuthorityExceptionHandler();
	/**
	 * 降级异常处理器
	 */
	private static BlockExceptionHandler degradeExceptionHandler = new DegradeExceptionHandler();
	/**
	 * 限流异常处理器
	 */
	private static BlockExceptionHandler flowExceptionHandler = new FlowExceptionHandler();
	/**
	 * 热点参数异常处理器
	 */
	private static BlockExceptionHandler paramFlowExceptionHandler = new ParamFlowExceptionHandler();
	/**
	 * 系统异常处理器
	 */
	private static BlockExceptionHandler systemBlockExceptionHandler = new SystemBlockExceptionHandler();

	/**
	 * 获取授权异常的处理器
	 * 
	 * @return
	 */
	public static BlockExceptionHandler getAuthorityExceptionHandler() {
		return authorityExceptionHandler;
	}

	/**
	 * 设置授权异常的处理器
	 * 
	 * @param authorityExceptionHandler
	 */
	public static void setAuthorityExceptionHandler(BlockExceptionHandler authorityExceptionHandler) {
		WebBlockExeptionManager.authorityExceptionHandler = authorityExceptionHandler;
	}

	/**
	 * 获取默认异常的处理器
	 * 
	 * @return
	 */
	public static BlockExceptionHandler getDefaultExceptionHandler() {
		return defaultExceptionHandler;
	}

	/**
	 * 设置默认异常的处理器，所有的限流异常都会使用该异常处理器
	 * 
	 * @param defaultExceptionHandler
	 */
	public static void setDefaultExceptionHandler(BlockExceptionHandler defaultExceptionHandler) {
		WebBlockExeptionManager.defaultExceptionHandler = defaultExceptionHandler;
		WebBlockExeptionManager.authorityExceptionHandler = defaultExceptionHandler;
		WebBlockExeptionManager.degradeExceptionHandler = defaultExceptionHandler;
		WebBlockExeptionManager.flowExceptionHandler = defaultExceptionHandler;
		WebBlockExeptionManager.paramFlowExceptionHandler = defaultExceptionHandler;
		WebBlockExeptionManager.systemBlockExceptionHandler = defaultExceptionHandler;
	}

	/**
	 * 获取降级异常的处理器
	 * 
	 * @return
	 */
	public static BlockExceptionHandler getDegradeExceptionHandler() {
		return degradeExceptionHandler;
	}

	/**
	 * 设置降级异常的处理器
	 * 
	 * @param degradeExceptionHandler
	 */
	public static void setDegradeExceptionHandler(BlockExceptionHandler degradeExceptionHandler) {
		WebBlockExeptionManager.degradeExceptionHandler = degradeExceptionHandler;
	}

	/**
	 * 获取限流异常的处理器
	 * 
	 * @return
	 */
	public static BlockExceptionHandler getFlowExceptionHandler() {
		return flowExceptionHandler;
	}

	/**
	 * 设置限流异常的处理器
	 * 
	 * @param flowExceptionHandler
	 */
	public static void setFlowExceptionHandler(BlockExceptionHandler flowExceptionHandler) {
		WebBlockExeptionManager.flowExceptionHandler = flowExceptionHandler;
	}

	/**
	 * 获取热点参数异常的处理器
	 * 
	 * @return
	 */
	public static BlockExceptionHandler getParamFlowExceptionHandler() {
		return paramFlowExceptionHandler;
	}

	/**
	 * 设置热点参数异常的处理器
	 * 
	 * @param paramFlowExceptionHandler
	 */
	public static void setParamFlowExceptionHandler(BlockExceptionHandler paramFlowExceptionHandler) {
		WebBlockExeptionManager.paramFlowExceptionHandler = paramFlowExceptionHandler;
	}

	/**
	 * 获取系统异常的处理器
	 * 
	 * @return
	 */
	public static BlockExceptionHandler getSystemBlockExceptionHandler() {
		return systemBlockExceptionHandler;
	}

	/**
	 * 设置系统异常的处理器
	 * 
	 * @param systemBlockExceptionHandler
	 */
	public static void setSystemBlockExceptionHandler(BlockExceptionHandler systemBlockExceptionHandler) {
		WebBlockExeptionManager.systemBlockExceptionHandler = systemBlockExceptionHandler;
	}

}
