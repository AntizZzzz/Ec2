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
import com.jiangyifen.ec2.service.csr.ami.DialService;
import com.jiangyifen.ec2.service.csr.ami.HangupService;
import com.jiangyifen.ec2.service.eaoservice.UserService;
import com.jiangyifen.ec2.servlet.http.common.pojo.CommonRespBo;
import com.jiangyifen.ec2.servlet.http.common.utils.AnalyzeIfaceJointUtil;
import com.jiangyifen.ec2.servlet.http.common.utils.GsonUtil;
import com.jiangyifen.ec2.utils.SpringContextHolder;

/**
 * @Description 描述：调用该接口，用于监听指定坐席的通话
 * 
 * 	坐席必须同时具备以下条件，方可进行监听：
 * 
 * 		假设：管理员使用分机800008要监听正在使用分机800001的坐席1001的通话
 * 
 * 		1、管理员用于监听的分机800008，暂时没有任何坐席与之绑定 
 * 		1、坐席1001跟存在（即要求用户名参数要正确）
 * 		2、坐席1001已经与分机800001建立了绑定关系（即相当于登录了EC2系统）
 * 		3、坐席使用的分机800001存在对应的通道，即存在单通，或者已经接通的电话
 * 
 * 请求路径：http://{ec2_server_ip}:{ec2_server_port}/ec2/http/common/spyUser?accessId=xxx&accessKey=xxx&destUsername=1001&mgrExten=800008
 *
 * eg.
 * 	http://192.168.2.160:8080/ec2/http/common/spyUser?accessId=xxx&accessKey=xxx&destUsername=1001&mgrExten=800008
 * 
 * @author  JRH
 * @date    2014年8月12日 上午09:30:01
 */
@SuppressWarnings("serial")
public class SpyUserServlet extends HttpServlet {

	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private UserService userService = SpringContextHolder.getBean("userService");
	private HangupService hangupService = SpringContextHolder.getBean("hangupService");
	private DialService dialService = SpringContextHolder.getBean("dialService");

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		CommonRespBo commonRespBo = new CommonRespBo();		// 响应信息
		commonRespBo.setCode("0");
		commonRespBo.setMessage("监听成功！");

		try {
			
			Long domainId = (Long) request.getAttribute("domainId");		// 通过com.jiangyifen.ec2.servlet.http.common.filter.HttpCommonFilter 获得
			String destUsername = StringUtils.trimToEmpty(request.getParameter("destUsername"));
			String mgrExten = StringUtils.trimToEmpty(request.getParameter("mgrExten"));

			if("".equals(destUsername) || "".equals(mgrExten)) {
				commonRespBo.setCode("-1");
				commonRespBo.setMessage("失败，目标用户名与分机号均不能为空！");
				logger.warn("JRH - IFACE 监听失败，原因：请求参数中目标用户名destUsername 或 分机号exten 值为空！");
				operateResponse(response, commonRespBo);
				return;
			}
			
			Long userId = ShareData.extenToUser.get(mgrExten);
			if(userId != null) {	// 检查分机被占用情况
				commonRespBo.setCode("-1");
				commonRespBo.setMessage("失败，当前分机已被坐席占用！");
				logger.warn("JRH - IFACE 监听失败，原因：当前分机已被坐席占用！");
				operateResponse(response, commonRespBo);
				return;
			}
			
			List<User> destUserLs = userService.getUsersByUsername(destUsername, domainId);
			if (destUserLs == null || destUserLs.size() == 0) {
				commonRespBo.setCode("-1");
				commonRespBo.setMessage("失败，目标用户不存在，目标用户名有误！");
				logger.warn("JRH - IFACE 监听失败，原因：请求参数中用户名有误，租户编号"+domainId+"下，目标用户"+destUsername+"不存在！");
				operateResponse(response, commonRespBo);
				return;
			}
					
			User destUser = destUserLs.get(0);
			String csrExten = ShareData.userToExten.get(destUser.getId());
			if(csrExten == null) {	// 检查在线情况
				commonRespBo.setCode("-1");
				commonRespBo.setMessage("失败，目标用户尚未绑定分机！");
				logger.warn("JRH - IFACE 监听失败，原因：目标用户"+destUsername+"尚未绑定分机！");
				operateResponse(response, commonRespBo);
				return;
			}
			
			Set<String> csrChannels = ShareData.peernameAndChannels.get(csrExten);
			if(csrChannels != null && csrChannels.size() == 0){
				commonRespBo.setCode("-1");
				commonRespBo.setMessage("失败，目标用户当前没有通话");
				logger.warn("JRH - IFACE 监听失败，原因：目标用户"+destUsername+"当前没有通话！");
				operateResponse(response, commonRespBo);
				return;
			}
			
			// 强拆mgrExten的所有Channel
			Set<String> mgrChannels = ShareData.peernameAndChannels.get(mgrExten);
			if(mgrChannels != null) {
				for(String mgrChannel : mgrChannels){
					hangupService.hangup(mgrChannel);
				}
			}
			
			dialService.dial(mgrExten, "555"+csrExten);

			operateResponse(response, commonRespBo);
		} catch (Exception e) {
			commonRespBo.setCode("-1");
			commonRespBo.setMessage("失败，未知错误！！");
			operateResponse(response, commonRespBo);

			logger.error("JRH - IFACE 监听, 出现异常！"+e.getMessage(), e);
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
