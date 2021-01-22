package com.jiangyifen.ec2.servlet.http.common;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.service.csr.ami.HangupService;
import com.jiangyifen.ec2.service.eaoservice.UserService;
import com.jiangyifen.ec2.servlet.http.common.pojo.CommonRespBo;
import com.jiangyifen.ec2.servlet.http.common.utils.AnalyzeIfaceJointUtil;
import com.jiangyifen.ec2.servlet.http.common.utils.GsonUtil;
import com.jiangyifen.ec2.utils.SpringContextHolder;

/**
 * @Description 描述：调用该接口，用于挂断坐席的当前通话
 * 
 * 	坐席必须同时具备以下条件，方可挂断成功：
 * 
 * 		1、坐席存在（即要求用户名参数要正确）
 * 		3、坐席已经与分机建立了绑定关系（即相当于登录了EC2系统）
 * 		2、坐席使用的分机存在对应的通道，即存在单通，或者已经接通的电话
 * 
 * 请求路径：http://{ec2_server_ip}:{ec2_server_port}/ec2/http/common/hangup?accessId=xxx&accessKey=xxx&username=1001
 *
 * eg.
 * 	http://192.168.2.160:8080/ec2/http/common/hangup?accessId=xxx&accessKey=xxx&username=1001
 * 
 * @author  JRH
 * @date    2014年8月11日 上午10:49:01
 */
@SuppressWarnings("serial")
public class HangupServlet extends HttpServlet {

	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private UserService userService = SpringContextHolder.getBean("userService");
	private HangupService hangupService = SpringContextHolder.getBean("hangupService");

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		CommonRespBo commonRespBo = new CommonRespBo();		// 响应信息
		commonRespBo.setCode("0");
		commonRespBo.setMessage("挂断成功！");

		try {
			
			Long domainId = (Long) request.getAttribute("domainId");		// 通过com.jiangyifen.ec2.servlet.http.common.filter.HttpCommonFilter 获得
			String username = StringUtils.trimToEmpty(request.getParameter("username"));

			if("".equals(username)) {
				commonRespBo.setCode("-1");
				commonRespBo.setMessage("失败，用户名不能为空！");
				logger.warn("JRH - IFACE 挂断失败，原因：请求参数中用户名username 值为空！");
				operateResponse(response, commonRespBo);
				return;
			}

			List<User> userLs = userService.getUsersByUsername(username, domainId);
			if (userLs == null || userLs.size() == 0) {
				commonRespBo.setCode("-1");
				commonRespBo.setMessage("失败，用户不存在，用户名有误！");
				logger.warn("JRH - IFACE 挂断失败，原因：请求参数中用户名有误，租户编号"+domainId+"下，用户"+username+"不存在！");
				operateResponse(response, commonRespBo);
				return;
			}
					
			User loginUser = userLs.get(0);
			String exten = ShareData.userToExten.get(loginUser.getId());
			if(exten == null) {	// 检查在线情况
				commonRespBo.setCode("-1");
				commonRespBo.setMessage("失败，用户尚未绑定分机！");
				logger.warn("JRH - IFACE 挂断失败，原因：用户"+username+"尚未绑定分机！");
				operateResponse(response, commonRespBo);
				return;
			}
			
			Set<String> channels = ShareData.peernameAndChannels.get(exten);
			if(channels != null && channels.size() == 0){
				commonRespBo.setCode("-1");
				commonRespBo.setMessage("失败，用户当前没有通话！");
				logger.warn("JRH - IFACE 挂断失败，原因：用户"+username+"当前没有通话！");
				operateResponse(response, commonRespBo);
				return;
			}
			
			try {
				for(String channel : channels){
					hangupService.hangup(channel);
				}
			} catch (Exception e) {
				commonRespBo.setCode("-1");
				commonRespBo.setMessage("失败，未知错误！");
				logger.error("JRH - IFACE 挂断失败，原因：调用hangupService.hangup(...), 出现异常！"+e.getMessage(), e);
			}

			operateResponse(response, commonRespBo);
		} catch (Exception e) {
			commonRespBo.setCode("-1");
			commonRespBo.setMessage("失败，未知错误！！");
			operateResponse(response, commonRespBo);

			logger.error("JRH - IFACE 挂断, 出现异常！"+e.getMessage(), e);
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
			response.setContentType(AnalyzeIfaceJointUtil.RESPONSE_CONTENT_TYPE);
			out.println("callback(" + GsonUtil.toJson(commonRespBo) + ");");
		}
		out.close();
	}

}
