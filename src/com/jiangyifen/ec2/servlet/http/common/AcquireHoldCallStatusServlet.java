package com.jiangyifen.ec2.servlet.http.common;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.service.eaoservice.UserService;
import com.jiangyifen.ec2.servlet.http.common.pojo.CommonRespBo;
import com.jiangyifen.ec2.servlet.http.common.utils.AnalyzeIfaceJointUtil;
import com.jiangyifen.ec2.servlet.http.common.utils.GsonUtil;
import com.jiangyifen.ec2.utils.SpringContextHolder;

/**
 * @Description 描述信息: 获取呼叫保持状态的 Servlet 
 * 
 * 获取呼叫保持状态, 首先根据用户名来查询, 如果用户名为空, 则根据分机号进行查询.
 * 
 * 示例: 
 * 	http://192.168.1.160:8081/ec2/http/common/acquireHoldCallStatus?accessId=2014072586956690HTDM1&accessKey=6F8906ED473C34D5D62CF65D9597DBB0&username=1001
 * 	http://192.168.1.160:8081/ec2/http/common/acquireHoldCallStatus?accessId=2014072586956690HTDM1&accessKey=6F8906ED473C34D5D62CF65D9597DBB0&exten=800001
 *
 * @auther jinht
 *
 * @date 2016-2-1 上午9:20:05
 */
@SuppressWarnings("serial")
public class AcquireHoldCallStatusServlet extends HttpServlet {
	// 日志工具类
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	// 业务类
	private UserService userService = SpringContextHolder.getBean("userService");
	
	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		CommonRespBo commonRespBo = new CommonRespBo();		// 相应信息
		commonRespBo.setCode("0");
		commonRespBo.setMessage("获取呼叫保持状态成功!");
		
		try {
			Long domainId = (Long) request.getAttribute("domainId");
			String username = StringUtils.trimToEmpty(request.getParameter("username"));
			String exten = StringUtils.trimToEmpty(request.getParameter("exten"));
			
			if(!"".equals(username)) {	// 如果用户名不为空, 则首先根据用户名信息进行查询
				List<User> users = userService.getUsersByUsername(username, domainId);
				if(users == null || users.size() == 0) {
					commonRespBo.setCode("-1");
					commonRespBo.setMessage("失败, 用户不存在, 用户名有误!");
					operateResponse(response, commonRespBo);
					logger.warn("jinht - IFACE 获取呼叫保持状态失败, 原因: 请求参数中的用户名有误, 租户编号" + domainId + "下, 用户" + username + "不存在!");
					return;
				}
				
				User loginUser = users.get(0);
				exten = ShareData.userToExten.get(loginUser.getId());
				if(exten == null || "".equals(exten)) {
					commonRespBo.setCode("-1");
					commonRespBo.setMessage("失败, 用户尚未绑定分机!");
					logger.warn("jinht - IFACE 获取呼叫保持状态失败, 原因: 用户(" + username + ")尚未绑定分机!");
					operateResponse(response, commonRespBo);
					return;
				}
				
				List<String> holdOnCallerChannels = ShareData.userExtenToHoldOnCallerChannels.get(exten);
				if(holdOnCallerChannels == null || holdOnCallerChannels.size() == 0) {
					commonRespBo.setCode("-1");
					commonRespBo.setMessage("失败, 当前用户没有客户处于呼叫保持中!");
					logger.warn("jinht - IFACE 当前用户(" + username + ")没有客户处于呼叫保持中!");
					operateResponse(response, commonRespBo);
					return;
				}
				
				commonRespBo.setResults(holdOnCallerChannels);
				operateResponse(response, commonRespBo);
				return;
			}
			
			if(!"".equals(exten)) {
				List<String> holdOnCallerChannels = ShareData.userExtenToHoldOnCallerChannels.get(exten);
				if(holdOnCallerChannels == null || holdOnCallerChannels.size() == 0) {
					commonRespBo.setCode("-1");
					commonRespBo.setMessage("失败, 当前用户没有客户处于呼叫保持中!");
					logger.warn("jinht - IFACE 当前分机(" + exten + ")没有客户处于呼叫保持中!");
					operateResponse(response, commonRespBo);
					return;
				}
				
				commonRespBo.setResults(holdOnCallerChannels);
				operateResponse(response, commonRespBo);
				return;
			}
			
			commonRespBo.setCode("-1");
			commonRespBo.setMessage("失败, 用户名或分机不能为空!");
			operateResponse(response, commonRespBo);
			logger.warn("jinht - IFACE 获取呼叫保持状态失败, 用户名或分机不能为空!");
			return;
			
		} catch (Exception e) {
			commonRespBo.setCode("-1");
			commonRespBo.setMessage("失败, 未知错误!");
			operateResponse(response, commonRespBo);
			logger.error("jinht - IFACE 获取呼叫保持失败, 原因: " + e.getMessage(), e);
			return;
		}
		
		
	}
	
	/**
	 * @Description 描述：返回操作的反馈信息
	 *
	 * @author  JRH
	 * @date    2014年8月8日 下午12:43:21
	 * @param response			HttpServletResponse
	 * @param commonRespBo		响应信息
	 * @throws IOException 
	 */
	private void operateResponse(HttpServletResponse response, CommonRespBo commonRespBo) throws IOException {
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();
		if("false".equals(AnalyzeIfaceJointUtil.WHETHER_SUPPORT_CORS)) {
			response.setContentType("text/plain");
			out.println(""+GsonUtil.toJson(commonRespBo));
		} else {
			response.setContentType("application/json");
			out.println("callback(" + GsonUtil.toJson(commonRespBo) + ")");
		}
		out.close();
	}

}
