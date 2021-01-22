package com.jiangyifen.ec2.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.service.csr.ami.DialService;
import com.jiangyifen.ec2.service.eaoservice.UserService;
import com.jiangyifen.ec2.utils.SpringContextHolder;

@SuppressWarnings("serial")
public class HttpIfaceServlet extends HttpServlet{
	@Override
	protected void service(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
			throws ServletException, IOException {
		//super.service(httpServletRequest, httpServletResponse);
		PrintWriter out = httpServletResponse.getWriter();
		
		try {
			String username = httpServletRequest.getParameter("username");
			String connectedLineNum = httpServletRequest.getParameter("dstnum");

//			System.out.println("dial: useExten="+username+"  dstnum="+connectedLineNum);
//			System.out.println("".equals(username));
//			System.out.println("".equals(connectedLineNum));
			
			// 检查客户输入的信息是否有误，如果信息有误，则直接返回
			if(username == null || connectedLineNum == null || "".equals(username) || "".equals(connectedLineNum)) {
				out.append("Failed Caused by: username and dstnum can not be empty!");
				out.close();
				return;
			} 
			
			// 如果输入的信息正确，则进一步检查坐席是否存在，如果不存在，则直接返回
			UserService userService = SpringContextHolder.getBean("userService");
			List<User> csrs = userService.getUsersByUsername(username);
			if(csrs.size() == 0) {
				out.append("Failed Caused by: csr not exist!");
				out.close();
				return;
			} 
			
			// 如果坐席存在，则检查坐席当前是否在线，如果不在线直接返回
			User csrCaller = csrs.get(0);
			String useExten = ShareData.userToExten.get(csrCaller.getId());
			if(useExten == null) {
				out.append("Failed Caused by: csr not login!");
				out.close();
				return;
			}
			
			// 如果以上条件都满足，则发起呼叫
			DialService dialService = SpringContextHolder.getBean("dialService");
			dialService.dial(useExten, connectedLineNum, null, null);

//			System.out.println("dial: useExten="+useExten+"  dstnum="+connectedLineNum);
			
		} catch (Exception e) {
			e.printStackTrace();
			out.println("failed");
			out.close();
		}
		
		//======================向客户端返回结果============================//
		out.println("success");
		out.close();
	}
}
