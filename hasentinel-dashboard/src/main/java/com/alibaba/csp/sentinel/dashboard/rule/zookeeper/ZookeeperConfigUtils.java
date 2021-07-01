package com.alibaba.csp.sentinel.dashboard.rule.zookeeper;

/**
 * @author rodbate
 * @author fenglibin
 * @since 2019/04/20 15:22
 */
public final class ZookeeperConfigUtils {

	public static final String GROUP_ID = "SENTINEL-GROUP";
	private static final String ZK_PATH_SEPARATOR = "/";
	public static final String APP_MACHINES = "APP-MACHINES";
	public static final String FLOW_RULES = "FLOW-RULES";
	public static final String AUTHORITY_RULES = "AUTHORITY-RULES";
	public static final String DEGRADE_RULES = "DEGRADE-RULES";
	public static final String HOT_RULES = "HOT-RULES";
	public static final String SYSTEM_RULES = "SYSTEM-RULES";
	public static final String GLOBAL_RULES = "GLOBAL-RULES";
	// 限流规则的ZK路径后缀
	private static final String FLOW_RULE_DATA_ID_POSTFIX = "-flow-rules";
	// 授权规则的ZK路径后缀
	private static final String AUTHORITY_RULE_DATA_ID_POSTFIX = "-authority-rules";
	// 机器发现的ZK路径后缀
	private static final String MACHINE_DISCOVERY_DATA_ID_POSTFIX = "-machine-discovery";
	// 降级规则的ZK路径后缀
	private static final String DEGRADE_RULE_DATA_ID_POSTFIX = "-degrade-rules";
	// 热点规则的ZK路径后缀
	private static final String HOT_RULE_DATA_ID_POSTFIX = "-hot-rules";
	// 系统规则的ZK路径后缀
	private static final String SYSTEM_RULE_DATA_ID_POSTFIX = "-system-rules";
	// 系统规则的ZK路径后缀
	private static final String GLOBAL_RULE_DATA_ID = "global-system-rules";

	private ZookeeperConfigUtils() {
	}

	/**
	 * 获取Sentinel根配置的ZK路径
	 * 
	 * @return
	 */
	public static String getConfigRootZkPath() {
		return ZK_PATH_SEPARATOR + GROUP_ID;
	}

	/**
	 * 获取限流规则Sentinel限流配置的ZK路径
	 * 
	 * @return
	 */
	public static String getFlowRulesConfigZkPath() {
		return getConfigRootZkPath() + ZK_PATH_SEPARATOR + FLOW_RULES;
	}

	/**
	 * 获取降级规则Sentinel限流配置的ZK路径
	 * 
	 * @return
	 */
	public static String getDegradeRulesConfigZkPath() {
		return getConfigRootZkPath() + ZK_PATH_SEPARATOR + DEGRADE_RULES;
	}

	/**
	 * 获取应用服务器Sentinel机器列表配置的ZK路径
	 * 
	 * @return
	 */
	public static String getAppMachinesConfigZkPath() {
		return getConfigRootZkPath() + ZK_PATH_SEPARATOR + APP_MACHINES;
	}

	/**
	 * 获取热点规则Sentinel限流配置的ZK路径
	 * 
	 * @return
	 */
	public static String getHotRulesConfigZkPath() {
		return getConfigRootZkPath() + ZK_PATH_SEPARATOR + HOT_RULES;
	}

	/**
	 * 获取系统规则Sentinel限流配置的ZK路径
	 * 
	 * @return
	 */
	public static String getSystemRulesConfigZkPath() {
		return getConfigRootZkPath() + ZK_PATH_SEPARATOR + SYSTEM_RULES;
	}
	
	/**
	 * 获取系统规则全局限流配置的ZK路径，如统一设置默认的最高QPS
	 * 
	 * @return
	 */
	public static String getGlobalRulesConfigZkPath() {
		return getConfigRootZkPath() + ZK_PATH_SEPARATOR + GLOBAL_RULES;
	}

	/**
	 * 获授权控规则在ZK中的路径，如：/groupId/dataId
	 *
	 * @param app 应用名称
	 * @return 授权规则在ZK中的路径
	 */
	public static String getAuthorityRuleZkPath(String app) {
		return getConfigRootZkPath() + ZK_PATH_SEPARATOR + AUTHORITY_RULES + ZK_PATH_SEPARATOR + app
				+ AUTHORITY_RULE_DATA_ID_POSTFIX;
	}

	/**
	 * 获取流控规则在ZK中的路径，如：/groupId/dataId
	 *
	 * @param app 应用名称
	 * @return 流控规则在ZK中的路径
	 */
	public static String getFlowRuleZkPath(String app) {
		return getFlowRulesConfigZkPath() + ZK_PATH_SEPARATOR + app + FLOW_RULE_DATA_ID_POSTFIX;
	}

	/**
	 * 获取降级规则在ZK中的路径，如：/groupId/dataId
	 *
	 * @param app app name
	 * @return zk path
	 */
	public static String getDegradeRuleZkPath(String app) {
		return getDegradeRulesConfigZkPath() + ZK_PATH_SEPARATOR + app + DEGRADE_RULE_DATA_ID_POSTFIX;
	}

	/**
	 * 获取热点规则在ZK中的路径，如：/groupId/dataId
	 *
	 * @param app app name
	 * @return zk path
	 */
	public static String getHotRuleZkPath(String app) {
		return getHotRulesConfigZkPath() + ZK_PATH_SEPARATOR + app + HOT_RULE_DATA_ID_POSTFIX;
	}

	/**
	 * 获取系统规则在ZK中的路径，如：/groupId/dataId
	 *
	 * @param app app name
	 * @return zk path
	 */
	public static String getSystemRuleZkPath(String app) {
		return getSystemRulesConfigZkPath() + ZK_PATH_SEPARATOR + app + SYSTEM_RULE_DATA_ID_POSTFIX;
	}
	
	/**
	 * 获取全局规则（如全局接口的默认ＱＰＳ设置）在ZK中的路径，如：/groupId/dataId
	 *
	 * @return zk path
	 */
	public static String getGlobalRuleZkPath() {
		return getSystemRulesConfigZkPath() + ZK_PATH_SEPARATOR + GLOBAL_RULE_DATA_ID;
	}
	
	

	/**
	 * 获取Machine Discovery时调用的ZK PATH，其保存的是当前应用对应的所有服务器信息
	 * 
	 * @param app
	 * @return
	 */
	public static String getMachineDiscoveryZkPath(String app) {
		if (app.endsWith(MACHINE_DISCOVERY_DATA_ID_POSTFIX)) {
			return getAppMachinesConfigZkPath() + ZK_PATH_SEPARATOR + app;
		}
		return getAppMachinesConfigZkPath() + ZK_PATH_SEPARATOR + app + MACHINE_DISCOVERY_DATA_ID_POSTFIX;
	}

}
