package com.alibaba.csp.sentinel.dashboard.controller;

import java.util.List;

import org.junit.Test;

import com.alibaba.csp.sentinel.command.vo.NodeVo;
import com.alibaba.csp.sentinel.dashboard.client.SentinelApiClient;
import com.alibaba.csp.sentinel.dashboard.domain.ResourceTreeNode;
import com.alibaba.csp.sentinel.dashboard.domain.Result;
import com.alibaba.csp.sentinel.dashboard.domain.vo.ResourceVo;

public class ResourceControllerTest {

	private SentinelApiClient httpFetcher = new SentinelApiClient();

	@Test
	public void test1() {
		Result<?> result = null;
		String ip = "192.168.30.41";
		Integer port = 54321;
		String type = null;
		String searchKey = "";
		List<NodeVo> nodeVos = httpFetcher.fetchResourceOfMachine(ip, port, type);
		if (nodeVos == null) {
			result = Result.ofSuccess(null);
		}
		ResourceTreeNode treeNode = ResourceTreeNode.fromNodeVoList(nodeVos);
		treeNode.searchIgnoreCase(searchKey);
		result = Result.ofSuccess(ResourceVo.fromResourceTreeNode(treeNode));
		System.out.println(result.toString());
	}
}
