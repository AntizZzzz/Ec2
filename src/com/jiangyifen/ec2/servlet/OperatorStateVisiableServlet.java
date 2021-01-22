package com.jiangyifen.ec2.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.jiangyifen.ec2.ui.admin.tableinfo.pojo.vo.StateVisiableVaraible;

/**
 * 设置操作员查看按钮是否显示
 * 
 * 192.168.1.160::8088/ec2/open
 * 
 * @author JHT
 * Servlet implementation class OperatorStateVisiableServlet
 */
@SuppressWarnings("serial")
public class OperatorStateVisiableServlet extends HttpServlet {
	
//	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
//    private static final String PERMIT = "t";		// 判断值
	
    /**
	 * @see 用来显示和隐藏系统信息查看按钮
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PrintWriter out = response.getWriter();
		out.print("<h4>T</h4>");
		if(StateVisiableVaraible.OPERATOR_SHOW_STATE == true) {
			StateVisiableVaraible.OPERATOR_SHOW_STATE = false;
		} else {
			StateVisiableVaraible.OPERATOR_SHOW_STATE = true;
		}
		/*String permit = request.getParameter("p");						// 用来接收是否允许访问权限
		String msg = request.getParameter("m");							// 用来接收是否激活访问权限
		if(permit != null && permit.trim().length() > 0 && permit.equals(PERMIT)){
			if(msg != null && msg.trim().length() > 0  && msg.equals(sdf.format(new Date()))){
				StateVisiableVaraible.OPERATOR_SHOW_STATE = true;		// 显示系统信息查看按钮
				out.print("<h4>T</h4>");
			} else {
				StateVisiableVaraible.OPERATOR_SHOW_STATE = false;		// 隐藏系统信息查看按钮
				out.print("<h4>F</h4>");
			}
		} else{
			StateVisiableVaraible.OPERATOR_SHOW_STATE = false;			// 隐藏系统信息查看按钮
			out.print("<h4>F</h4>");
		}*/
	}

}
