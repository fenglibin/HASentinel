package com.eeeffff.hasentinel.demo.spring.webmvc.vo;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;

@Data
@Builder
public class Result {
	@Default
	private int code=0;
	@Default
	private String result="";
	
}
