package com.alibaba.csp.sentinel.dashboard.repository.metric;

import java.util.concurrent.atomic.AtomicLong;

public class MetricCount {
	// 处理的数量
	private AtomicLong nums = new AtomicLong(0);
	// 写入数据花费的总时间（毫秒）
	private AtomicLong times = new AtomicLong(0);
	// 待处理队列中的数量
	private int queueNums;
	// 线程池中排队待处理的数量
	private int threadPoolQueueNums;
	// 最大处理线程
	private int threads;
	// 当前活跃的处理线程
	private int activeThreads;

	/**
	 * 处理的数量
	 * 
	 * @return
	 */
	public long getNums() {
		return nums.get();
	}

	public void incrNums() {
		nums.incrementAndGet();
	}

	public void addTimes(long delta) {
		times.addAndGet(delta);
	}

	/**
	 * 获取写入数据花费的总时间，单位为秒
	 * 
	 * @return
	 */
	public long getTimes() {
		return times.get() / 1000;
	}

	/**
	 * 获取每批数据写入花费的平均时间 tps
	 * 
	 * @return
	 */
	public long getAvgTps() {
		long handTime = getTimes();
		handTime = handTime == 0 ? 1 : handTime;
		return nums.get() / handTime;
	}

	public int getQueueNums() {
		return queueNums;
	}

	public void setQueueNums(int queueNums) {
		this.queueNums = queueNums;
	}

	public int getThreads() {
		return threads;
	}

	public void setThreads(int threads) {
		this.threads = threads;
	}

	public int getActiveThreads() {
		return activeThreads;
	}

	public void setActiveThreads(int activeThreads) {
		this.activeThreads = activeThreads;
	}

	public int getThreadPoolQueueNums() {
		return threadPoolQueueNums;
	}

	public void setThreadPoolQueueNums(int threadPoolQueueNums) {
		this.threadPoolQueueNums = threadPoolQueueNums;
	}

}
