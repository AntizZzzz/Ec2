package com.jiangyifen.ec2.servlet.woke;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import com.jiangyifen.ec2.bean.ChannelSession;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.service.csr.ami.ChannelRedirectService;
import com.jiangyifen.ec2.utils.SpringContextHolder;

@SuppressWarnings("serial")
public class HttpTransferServlet extends HttpServlet{
	@Override
	protected void service(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
			throws ServletException, IOException {
		//super.service(httpServletRequest, httpServletResponse);
		PrintWriter out = httpServletResponse.getWriter();
		
		try {
			//获取参数
			String extennum = httpServletRequest.getParameter("extennum");
			String dstnum = httpServletRequest.getParameter("dstnum");

			//参数有效性判断
			if(StringUtils.isEmpty(extennum)) {
				out.append("failed");
				out.close();
				return;
			} 
			
			//接口调用
			ChannelRedirectService channelRedirectService= SpringContextHolder.getBean("channelRedirectService");
			Set<String> channelSet = ShareData.peernameAndChannels.get(extennum);
			if(channelSet.size()==0){
				out.append("failed");
				out.close();
				return;
			}
			//认为只有一路通话
			Iterator<String> iter = channelSet.iterator();
			if(iter.hasNext()){
				String channel=iter.next();
				ChannelSession channelSession=ShareData.channelAndChannelSession.get(channel);
				if(channelSession!=null){
					channelRedirectService.redirectExten(channelSession.getBridgedChannel(), dstnum);
				}
			}
			
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
