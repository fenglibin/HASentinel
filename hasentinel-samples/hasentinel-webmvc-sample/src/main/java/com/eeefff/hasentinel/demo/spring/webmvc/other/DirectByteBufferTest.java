package com.eeefff.hasentinel.demo.spring.webmvc.other;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

import com.alibaba.csp.sentinel.util.StringUtil;


public class DirectByteBufferTest {
	@Test
	public void test1() {
		String str = "this is test string";
		ByteBuffer bb = ByteBuffer.allocateDirect(10 * 1024 * 1024);
		bb.put(str.getBytes());
		byte[] bt = new byte[str.length()];
		bb.flip();
		/*
		for (int i = 0; i < str.length(); i++) {
			bt[i] = bb.get();
		}
		*/
		bb.get(bt);

		System.out.print(new String(bt));
		bb.clear();
	}

	@Test
	public void test2() {
		String cities = "1,2,3,4,5,6";
		List<String> list = Arrays.stream(cities.split(",")).map(Integer::parseInt).map(e -> {
			if (e % 2 == 0) {
				return null;
			}
			return String.valueOf(e);
		}).filter(StringUtil::isNotEmpty).collect(Collectors.toList());
		System.out.println(list);
	}
}
