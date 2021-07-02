package com.eeeffff.hasentinel.web;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.alibaba.csp.sentinel.adapter.spring.webmvc.callback.UrlCleaner;
import com.eeeffff.hasentinel.common.util.StringUtil;

import lombok.extern.slf4j.Slf4j;

/**
* SentinelWebInterceptor会获取请求URL的原始Rest URL做为Key，不需要特殊处理；只有使用CommonFilter时，才需要自己处理Rest URL。<br>
* 在使用SentinelWebInterceptor的场景中，UrlCleaner的功能退化为用于过滤掉不需要统计的Url，针对这些URL返回为空，Sentinel就不会统计。<br>
* if (WebConfigManager.getUrlCleaner() == null) { <br>
* 	WebConfigManager.setUrlCleaner(new DefaultRestfulUrlCleaner(sentineConfigProperties.getRestUrlPrefix(), <br>
*			WebConfigManager.getRestUrls())); <br>
* }
* 
* @author fenglibin
*/
@Slf4j
public class DefaultRestfulUrlCleaner implements UrlCleaner {
	private String[] restUrls = null;
	private Map<String, Pattern> patternMap = new HashMap<String, Pattern>();
	private Map<String, String> displayUrlMap = new HashMap<String, String>();

	public DefaultRestfulUrlCleaner(String restUrl) {
		this(restUrl, null);
	}

	public DefaultRestfulUrlCleaner(String restUrl, Set<String> _restUrls) {
		if (_restUrls == null) {
			_restUrls = new HashSet<String>();
		}
		if (restUrl != null && restUrl.trim().length() > 0) {
			String[] urls = restUrl.split(",");
			for (String url : urls) {
				_restUrls.add(url);
			}
		}
		restUrls = new String[_restUrls.size()];
		restUrls = _restUrls.toArray(restUrls);
		for (String url : restUrls) {
			if (url.indexOf(StringUtil.PATH_PATTERN) > 0) { // UR包括正则表达式
				patternMap.put(url, Pattern.compile(url));
				displayUrlMap.put(url, url.replace(StringUtil.PATH_PATTERN, "*"));
			}
		}
		log.info("Rest URLs:" + _restUrls.toString().replace(StringUtil.PATH_PATTERN, "*"));
	}

	@Override
	public String clean(String originUrl) {
		if (restUrls == null || originUrl == null) {
			return originUrl;
		}
		for (String restUrl : restUrls) {
			if (restUrl.indexOf(StringUtil.PATH_PATTERN) > 0) { // UR包括正则表达式
				Pattern p = patternMap.get(restUrl);
				Matcher m = p.matcher(originUrl);
				if (m.matches()) {
					return displayUrlMap.get(restUrl);
				}
			} else if (originUrl.startsWith(restUrl)) {
				return restUrl;
			}
		}
		return originUrl;
	}

}
