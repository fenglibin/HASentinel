package com.eeeffff.hasentinel.common.vo.config;

import com.alibaba.fastjson.JSONObject;

import lombok.Getter;
import lombok.Setter;

/**
 * @author fenglibin
 */
public class ResultWrapper {

	@Getter
	@Setter
    private Integer code;
	@Getter
	@Setter
    private String message;

    public ResultWrapper(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
    
    public static ResultWrapper blocked() {
        return new ResultWrapper(-1, "Blocked by Sentinel");
    }

    public String toJsonString() {
        return JSONObject.toJSONString(this);
    }
}
