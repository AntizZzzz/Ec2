package com.jiangyifen.ec2.utils;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class EncodingFilter implements Filter {
	
	private FilterConfig filterConfig;
	
	@Override
	public void destroy() {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		String encoding=filterConfig.getInitParameter("encoding");
		
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;
		
		httpRequest.setCharacterEncoding(encoding);
		chain.doFilter(httpRequest, httpResponse);
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		this.filterConfig=filterConfig;
	}

}
