package com.eeefff.hasentinel.influxdb.log;

import org.apache.log4j.DailyRollingFileAppender;
import org.apache.log4j.Priority;

/**
 * Log4j默认会输出日志级别大于等于当前设置的日志级别到日志文件中，此处需要对其进行控制，将指定日志级别的日志输出到指定的文件中，<br>
 * 即将INFO级别的日志输出到记录INFO级别日志的日志文件中，将WARN级别的日志输出到记录WARN级别日志的日志文件中，<br>
 * 将ERROR级别的日志输出到记录ERROR级别日志的日志文件中等，而不需要将其它大于当前日志级别输出到该日志文件中，如WARN和ERROR的日志输出<br>
 * 到INFO级别的日志文件中，将ERROR级别的日志输出到WARN日志文件中等。<br>
 * 
 * @author fenglibin
 *
 */
public class CustomPriorityDailyRollingFileAppender extends DailyRollingFileAppender {

	/**
	 * 输出指定日志级别的日志到对应的日志文件中
	 */
	@Override
	public boolean isAsSevereAsThreshold(Priority priority) {
		// 只判断是否相等，而不判断优先级
		return ((threshold == null) || this.getThreshold().equals(priority));
	}
}
