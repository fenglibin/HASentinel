/*
 * Copyright 2006-2015 Transsion.com All right reserved. This software is the confidential and proprietary information
 * of Transsion.com ("Confidential Information"). You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into with Transsion.com.
 */
package com.eeeffff.hasentinel.influxdb.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

/**
 * 
 * ClassName CrossSiteFilter.java 跨域调用处理
 * @author fenglibin
 * @Blog http://xiake6.net
 * @Date 2019年11月28日
 * 
 * Description
 */
public class CrossSiteFilter implements Filter {

    private ServletContext ctx;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        ctx = filterConfig.getServletContext();

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
                                                                                              ServletException {

        HttpServletResponse res = (HttpServletResponse) response;

        /* 是否允许Json跨域调用，加上以下的配置表示允许跨域调用 */
        res.setHeader("Access-Control-Allow-Origin", "*");
        //res.setHeader("Access-Control-Allow-Credentials", "true");
        res.setHeader("Access-Control-Allow-Headers", "Accept, Accept-Encoding, Authorization, Content-Length, Content-Type, X-CSRF-Token, X-HTTP-Method-Override");
        res.setHeader("Access-Control-Allow-Methods", "DELETE, GET, OPTIONS, POST, PUT");
        res.setHeader("Access-Control-Expose-Headers", "Date, X-InfluxDB-Version, X-InfluxDB-Build");
        res.setHeader("Content-Type", "application/json");
        //res.setHeader("Content-Encoding", "gzip");

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // TODO Auto-generated method stub

    }

}
