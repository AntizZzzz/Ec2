package com.jiangyifen.ec2.servlet.http.common;

import java.io.IOException;
import java.io.PrintWriter;
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
import com.jiangyifen.ec2.entity.Queue;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.service.csr.ami.ChannelRedirectService;
import com.jiangyifen.ec2.service.eaoservice.QueueService;
import com.jiangyifen.ec2.service.eaoservice.UserService;
import com.jiangyifen.ec2.servlet.http.common.pojo.CommonRespBo;
import com.jiangyifen.ec2.servlet.http.common.utils.AnalyzeIfaceJointUtil;
import com.jiangyifen.ec2.servlet.http.common.utils.GsonUtil;
import com.jiangyifen.ec2.utils.SpringContextHolder;

/**
 * @Description 描述：调用该接口，用于坐席发起电话转接到队列（即某个小组）
 * 
 * 	坐席必须同时具备以下条件，方可发起转接：
 * 
 * 		假设：当前坐席 1001 坐席将来电13816760988 转给 一个队列 900001
 * 
 * 		1、坐席1001存在（即要求用户名参数要正确）
 * 		2、坐席1001已经与分机建立了绑定关系（即相当于登录了EC2系统）
 * 		3、坐席1001当前存在未挂断的电话
 * 		4、队列900001存在
 *  
 * 请求路径：http://{ec2_server_ip}:{ec2_server_port}/ec2/http/common/transferCallQueue?accessId=xxx&accessKey=xxx&username=1001&queue=900001
 *
 * eg.
 * 	http://192.168.2.160:8080/ec2/http/common/transferCallQueue?accessId=xxx&accessKey=xxx&username=1001&queue=900001
 * 
 * @author  JRH
 * @date    2014年8月11日 下午4:10:45
 */
@SuppressWarnings("serial")
public class TransferCallQueueServlet extends HttpServlet {

	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private UserService userService = SpringContextHolder.getBean("userService");
	private QueueService queueService = SpringContextHolder.getBean("queueService");
	private ChannelRedirectService channelRedirectService=SpringContextHolder.getBean("channelRedirectService");

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		CommonRespBo commonRespBo = new CommonRespBo();		// 响应信息
		commonRespBo.setCode("0");
		commonRespBo.setMessage("转接到队列成功！");

		try {
			
			Long domainId = (Long) request.getAttribute("domainId");		// 通过com.jiangyifen.ec2.servlet.http.common.filter.HttpCommonFilter 获得
			String username = StringUtils.trimToEmpty(request.getParameter("username"));
			String queueName = StringUtils.trimToEmpty(request.getParameter("queue"));
			
			if("".equals(username) || "".equals(queueName)) {
				commonRespBo.setCode("-1");
				commonRespBo.setMessage("失败，用户名和队列不能为空！");
				logger.warn("JRH - IFACE 转接到队列失败，原因：请求参数中用户名username或目标队列queue 为空！");
				operateResponse(response, commonRespBo);
				return;
			}

			List<User> userLs = userService.getUsersByUsername(username, domainId);
			if (userLs == null || userLs.size() == 0) {
				commonRespBo.setCode("-1");
				commonRespBo.setMessage("失败，用户不存在，用户名有误！");
				logger.warn("JRH - IFACE 转接到队列失败，原因：请求参数中用户名有误，租户编号"+domainId+"下，用户"+username+"不存在！");
				operateResponse(response, commonRespBo);
				return;
			}
			
			User loginUser = userLs.get(0);
			String exten = ShareData.userToExten.get(loginUser.getId());
			if(exten == null) {	// 检查在线情况
				commonRespBo.setCode("-1");
				commonRespBo.setMessage("失败，用户尚未绑定分机！");
				logger.warn("JRH - IFACE 转接到队列失败，原因：用户"+username+"尚未绑定分机！");
				operateResponse(response, commonRespBo);
				return;
			}

			Set<String> srcBridgedChannels = this.getAllBridgedChannel(exten);		// 源用户端，必须获取与其建立通话的另一方通道，如果客户电话对应的通道, 必须存在接通的电话
			if(srcBridgedChannels == null || srcBridgedChannels.size() == 0){
				commonRespBo.setCode("-1");
				commonRespBo.setMessage("失败，用户当前没有接通的通话！");
				logger.warn("JRH - IFACE 转接到队列失败，原因：用户"+username+"当前没有接通的通话！");
				operateResponse(response, commonRespBo);
				return;
			} 
			
			Queue queue = queueService.getByName(queueName, domainId);
			if(queue == null) {
				commonRespBo.setCode("-1");
				commonRespBo.setMessage("失败，目标队列不存在！");
				logger.warn("JRH - IFACE 转接到队列失败，原因：租户编号"+domainId+"下，目标队列"+queueName+" 不存在！");
				operateResponse(response, commonRespBo);
				return;
			}
			
			for(String srcBridgedChannel : srcBridgedChannels){		// 将与自己建立通话的通道转接给目标队列, 正常也只会有一通
				channelRedirectService.redirectQueue(srcBridgedChannel, queueName);
			}

			operateResponse(response, commonRespBo);
		} catch (Exception e) {
			commonRespBo.setCode("-1");
			commonRespBo.setMessage("失败，未知错误！！");
			operateResponse(response, commonRespBo);

			logger.error("JRH - IFACE 转接到队列, 出现异常！"+e.getMessage(), e);
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
