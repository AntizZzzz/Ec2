package com.jiangyifen.ec2.servlet.woke;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import com.jiangyifen.ec2.service.csr.ami.DialService;
import com.jiangyifen.ec2.utils.SpringContextHolder;

@SuppressWarnings("serial")
public class HttpChannelspyServlet extends HttpServlet{
	@Override
	protected void service(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
			throws ServletException, IOException {
		//super.service(httpServletRequest, httpServletResponse);
		PrintWriter out = httpServletResponse.getWriter();
		
		try {
			//获取参数
			String spyedextennum = httpServletRequest.getParameter("spyedextennum");
			String spyextennum = httpServletRequest.getParameter("spyextennum");

			//参数有效性判断
			if(StringUtils.isEmpty(spyedextennum)||StringUtils.isEmpty(spyextennum)) {
				out.append("failed");
				out.close();
				return;
			} 
			
			//接口调用
			DialService dialService = SpringContextHolder.getBean("dialService");
			dialService.dial(spyextennum, "555"+spyedextennum);
			
			//返回结果
			out.println("success");
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
			out.println("failed");
			out.close();
		}
	}
}
