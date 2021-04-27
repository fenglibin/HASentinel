package com.eeefff.hasentinel.common.util;

/**
 * 
 * @author fenglibin
 *
 */
public class StringUtil {
	public static final String PATH_PATTERN = "[a-zA-Z1-9_\\-]*";
	public static final String EMPTY_STRING = "";

	/**
	 * 将Restful Url替换为正则表达式代替URL中点位的URL，示例：<br>
	 * /{p1}/machine.json 替换为 /PATH_PATTERN/machine.json<br>
	 * /{p1}/{p2}/machine.json 替换为 /PATH_PATTERN/PATH_PATTERN/machine.json<br>
	 * /{p1}/xx/{p2}/machine.json 替换为 /PATH_PATTERN/xx/PATH_PATTERN/machine.json<br>
	 * /{p1}/xx/{p2} 替换为 /PATH_PATTERN/xx/PATH_PATTERN<br>
	 * /xx/{p1}/{p2} 替换为 /xx/PATH_PATTERN/PATH_PATTERN<br>
	 * /xx/{p1}/yy/{p2} 替换为 /xx/PATH_PATTERN/yy/PATH_PATTERN<br>
	 * 
	 * @param restUrl
	 * @return
	 */
	public static String restUrlToRegixUrl(String restUrl) {
		StringBuilder result = new StringBuilder();
		int index_start = -1;
		int index_end = -1;
		while ((index_start = restUrl.indexOf("{")) >= 0) {
			index_end = restUrl.indexOf("}");
			result.append(restUrl.substring(0, index_start));
			result.append(PATH_PATTERN);
			restUrl = restUrl.substring(index_end + 1);
		}
		result.append(restUrl);
		return result.toString();
	}
}
