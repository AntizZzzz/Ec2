package com.jiangyifen.ec2.servlet.http.common;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.bean.ChannelSession;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.service.csr.ami.ChannelRedirectService;
import com.jiangyifen.ec2.service.eaoservice.UserService;
import com.jiangyifen.ec2.servlet.http.common.pojo.CommonRespBo;
import com.jiangyifen.ec2.servlet.http.common.utils.AnalyzeIfaceJointUtil;
import com.jiangyifen.ec2.servlet.http.common.utils.GsonUtil;
import com.jiangyifen.ec2.utils.SpringContextHolder;

/**
 * @Description 描述：调用该接口，用于让坐席将当期通话进行呼叫保持
 * 
 * 	坐席必须同时具备以下条件，方可发起呼叫保持：
 * 
 * 		假设：当前坐席 1001 坐席正在与客户13816760988进行通话
 * 
 * 		1、坐席1001存在（即要求用户名参数要正确）
 * 		2、坐席1001已经与分机建立了绑定关系（即相当于登录了EC2系统）
 * 		3、坐席1001当前存在未挂断的电话
 *  
 * 请求路径：http://{ec2_server_ip}:{ec2_server_port}/ec2/http/common/holdCall?accessId=xxx&accessKey=xxx&username=1001
 *
 * eg.
 * 	http://192.168.2.160:8080/ec2/http/common/holdCall?accessId=xxx&accessKey=xxx&username=1001
 * 
 * @author  JRH
 * @date    2014年8月11日 下午3:00:45
 */
@SuppressWarnings("serial")
public class HoldCallServlet extends HttpServlet {

	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private UserService userService = SpringContextHolder.getBean("userService");
	private ChannelRedirectService channelRedirectService=SpringContextHolder.getBean("channelRedirectService");

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		CommonRespBo commonRespBo = new CommonRespBo();		// 响应信息
		commonRespBo.setCode("0");
		commonRespBo.setMessage("呼叫保持成功！");

		try {
			
			Long domainId = (Long) request.getAttribute("domainId");		// 通过com.jiangyifen.ec2.servlet.http.common.filter.HttpCommonFilter 获得
			String username = StringUtils.trimToEmpty(request.getParameter("username"));
			
			if("".equals(username)) {
				commonRespBo.setCode("-1");
				commonRespBo.setMessage("失败，用户名不能为空！");
				logger.warn("JRH - IFACE 呼叫保持失败，原因：请求参数中用户名username 值为空！");
				operateResponse(response, commonRespBo);
				return;
			}

			List<User> userLs = userService.getUsersByUsername(username, domainId);
			if (userLs == null || userLs.size() == 0) {
				commonRespBo.setCode("-1");
				commonRespBo.setMessage("失败，用户不存在，用户名有误！");
				logger.warn("JRH - IFACE 呼叫保持失败，原因：请求参数中用户名有误，租户编号"+domainId+"下，用户"+username+"不存在！");
				operateResponse(response, commonRespBo);
				return;
			}
			
			User loginUser = userLs.get(0);
			String exten = ShareData.userToExten.get(loginUser.getId());
			if(exten == null) {	// 检查在线情况
				commonRespBo.setCode("-1");
				commonRespBo.setMessage("失败，用户尚未绑定分机！");
				logger.warn("JRH - IFACE 呼叫保持失败，原因：用户"+username+"尚未绑定分机！");
				operateResponse(response, commonRespBo);
				return;
			}
			
			Set<String> srcBridgedChannels = this.getAllBridgedChannel(exten);		// 源用户端，必须获取与其建立通话的另一方通道，如果客户电话对应的通道, 必须存在接通的电话
			if(srcBridgedChannels == null || srcBridgedChannels.size() == 0){
				commonRespBo.setCode("-1");
				commonRespBo.setMessage("失败，用户当前没有接通的通话！");
				logger.warn("JRH - IFACE 呼叫保持失败，原因：用户"+username+"当前没有接通的通话！");
				operateResponse(response, commonRespBo);
				return;
			} 

			for(String srcBridgedChannel : srcBridgedChannels){		// 将与自己建立通话的通道转接给目标手机, 正常也只会有一通
				channelRedirectService.redirectQueue(srcBridgedChannel, "900000");

				// 将呼入的客户通道存放到当前坐席对应的内存中去，以便其待会接回【用以回复呼叫保持中的通话】
				List<String> holdOnCallerChannels = ShareData.userExtenToHoldOnCallerChannels.get(exten);
				if(holdOnCallerChannels == null) {
					holdOnCallerChannels = new ArrayList<String>();
					ShareData.userExtenToHoldOnCallerChannels.put(exten, holdOnCallerChannels);
				}
				if(!holdOnCallerChannels.contains(srcBridgedChannel)) {
					holdOnCallerChannels.add(srcBridgedChannel);
				}
				
				break;
			}

			operateResponse(response, commonRespBo);
		} catch (Exception e) {
			commonRespBo.setCode("-1");
			commonRespBo.setMessage("失败，未知错误！！");
			operateResponse(response, commonRespBo);

			logger.error("JRH - IFACE 呼叫保持, 出现异常！"+e.getMessage(), e);
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

	/**
	 * 取得所有处于Bridged状态的Channel
	 * 
	 * @return
	 */
	private Set<String> getAllBridgedChannel(String exten) {
		Set<String> allBridgedChannel = new HashSet<String>();
		Set<String> channels = ShareData.peernameAndChannels.get(exten);
		if(channels != null) {
			for(String channel : channels) {
				ChannelSession channelSession = ShareData.channelAndChannelSession.get(channel);
				if (channelSession != null) {
					String brigdedChannel = channelSession.getBridgedChannel();
					if (brigdedChannel != null && !"".equals(brigdedChannel)) {
						allBridgedChannel.add(brigdedChannel);
					}
				}
			}
		}
		
		return allBridgedChannel;
	}
	
}
