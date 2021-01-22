package com.jiangyifen.ec2.test;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
/*
 通过在web.xml中配置Filter 可以设置字符、配置用户登录路径 
 */
public class Common_LogOrNotTest implements javax.servlet.Filter {
	@SuppressWarnings("unused")
	private FilterConfig config;
	private String logon_page;
	private String home_page;

	public void destroy() {
		config = null;

	}

	public void init(FilterConfig filterconfig) throws ServletException {
		// 从部署描述符中获取登录页面和首页的URI
		config = filterconfig;
		logon_page = filterconfig.getInitParameter("LOGON_URI");
		home_page = filterconfig.getInitParameter("HOME_URI");
		System.out.println(home_page);
		if (null == logon_page || null == home_page) {
			throw new ServletException("没有找到登录页面或主页");
		}
	}

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) {
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse rpo = (HttpServletResponse) response;
		javax.servlet.http.HttpSession session = req.getSession();

		try {
			req.setCharacterEncoding("utf-8");
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		String userId = (String) session.getAttribute("UserId");
		String request_uri = req.getRequestURI().toUpperCase();// 得到用户请求的URI
//		String ctxPath = req.getContextPath();// 得到web应用程序的上下文路径
//		String uri = request_uri.substring(ctxPath.length()); // 去除上下文路径，得到剩余部分的路径
		try {
			if (request_uri.indexOf("LOGIN.JSP") == -1
					&& request_uri.indexOf("LOG.JSP") == -1 && userId == null) {
				rpo.sendRedirect(home_page + logon_page);
				System.out.print(home_page + logon_page);
				return;
			}

			else {
				chain.doFilter(request, response);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}