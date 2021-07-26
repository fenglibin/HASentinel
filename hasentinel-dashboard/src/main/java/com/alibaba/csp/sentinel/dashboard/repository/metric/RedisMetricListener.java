package com.alibaba.csp.sentinel.dashboard.repository.metric;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.MetricEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.vo.MetricEntityVO;
import com.alibaba.csp.sentinel.dashboard.domain.vo.MetricType;
import com.alibaba.csp.sentinel.dashboard.util.MD5Util;
import com.alibaba.csp.sentinel.dashboard.util.MetricUtil;
import com.alibaba.csp.sentinel.dashboard.wrapper.redis.LastResourceRedisKey;
import com.alibaba.csp.sentinel.dashboard.wrapper.redis.RedisTemplateWrapper;

import lombok.extern.slf4j.Slf4j;

/**
 * 将Metric写入Redis中，使用事件监控，减少直接调用的耦合
 * 
 * @author fenglibin
 * @date 2021年7月26日 下午7:36:34
 *
 */
@Slf4j
@Component
public class RedisMetricListener {

	/**
	 * 将Metric加入到Redis中
	 * 
	 * @param metric
	 */
	@Async
	@EventListener
	public void addLastResourceMetric(MetricEntity metric) {
		log.info("将Metric写入到Redis，更新指定Endpoint最后一次的访问时间.");
		String type = MetricUtil.getRequestTypeByResource(metric.getResource());
		// 将MetricEntityVO写入到redis的zset中，按耗时多少进行排序
		RedisTemplateWrapper.zSetAdd(LastResourceRedisKey.LAST_RESOURCE_SORTED_METRIC_KEY.getValue(), MetricEntityVO.builder().app(metric.getApp()).resource(metric.getResource()).build(),
				metric.getSuccessQps() > 0 ? metric.getRt() / metric.getSuccessQps() : metric.getRt());
		RedisTemplateWrapper.zSetAdd(LastResourceRedisKey.LAST_RESOURCE_SORTED_METRIC_KEY_NOEXPIRE.getValue(), MetricEntityVO.builder().app(metric.getApp()).resource(metric.getResource()).build(),
				metric.getSuccessQps() > 0 ? metric.getRt() / metric.getSuccessQps() : metric.getRt());
		if (MetricType.WEB.name().equals(type)) {
			RedisTemplateWrapper.zSetAdd(LastResourceRedisKey.LAST_RESOURCE_SORTED_METRIC_KEY_WEB.getValue(), MetricEntityVO.builder().app(metric.getApp()).resource(metric.getResource()).build(),
					metric.getSuccessQps() > 0 ? metric.getRt() / metric.getSuccessQps() : metric.getRt());
			RedisTemplateWrapper.zSetAdd(LastResourceRedisKey.LAST_RESOURCE_SORTED_METRIC_KEY_WEB_NOEXPIRE.getValue(), MetricEntityVO.builder().app(metric.getApp()).resource(metric.getResource()).build(),
					metric.getSuccessQps() > 0 ? metric.getRt() / metric.getSuccessQps() : metric.getRt());
		} else if (MetricType.DUBBO.name().equals(type)) {
			RedisTemplateWrapper.zSetAdd(LastResourceRedisKey.LAST_RESOURCE_SORTED_METRIC_KEY_DUBBO.getValue(), MetricEntityVO.builder().app(metric.getApp()).resource(metric.getResource()).build(),
					metric.getSuccessQps() > 0 ? metric.getRt() / metric.getSuccessQps() : metric.getRt());
			RedisTemplateWrapper.zSetAdd(LastResourceRedisKey.LAST_RESOURCE_SORTED_METRIC_KEY_DUBBO_NOEXPIRE.getValue(), MetricEntityVO.builder().app(metric.getApp()).resource(metric.getResource()).build(),
					metric.getSuccessQps() > 0 ? metric.getRt() / metric.getSuccessQps() : metric.getRt());
		} else if (MetricType.OTHER.name().equals(type)) {
			RedisTemplateWrapper.zSetAdd(LastResourceRedisKey.LAST_RESOURCE_SORTED_METRIC_KEY_OTHER.getValue(), MetricEntityVO.builder().app(metric.getApp()).resource(metric.getResource()).build(),
					metric.getSuccessQps() > 0 ? metric.getRt() / metric.getSuccessQps() : metric.getRt());
			RedisTemplateWrapper.zSetAdd(LastResourceRedisKey.LAST_RESOURCE_SORTED_METRIC_KEY_OTHER_NOEXPIRE.getValue(), MetricEntityVO.builder().app(metric.getApp()).resource(metric.getResource()).build(),
					metric.getSuccessQps() > 0 ? metric.getRt() / metric.getSuccessQps() : metric.getRt());
		}
		// 将每一份MetricEntiry都放入到HashSet中，以app+resource为Key，相同的MetricEntity为覆盖原来已经存在的
		RedisTemplateWrapper.hSetMetric(LastResourceRedisKey.LAST_RESOURCE_ALL_METRIC_KEY.getValue(), MD5Util.md5Of32(metric.getApp() + metric.getResource()), metric);
		RedisTemplateWrapper.hSetMetric(LastResourceRedisKey.LAST_RESOURCE_ALL_METRIC_KEY_NOEXPIRE.getValue(), MD5Util.md5Of32(metric.getApp() + metric.getResource()), metric);
	}
}
