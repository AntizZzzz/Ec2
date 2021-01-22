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

import com.jiangyifen.ec2.bean.ChannelLifeCycle;
import com.jiangyifen.ec2.bean.ExtenStatus;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.service.eaoservice.UserService;
import com.jiangyifen.ec2.servlet.http.common.pojo.CommonRespBo;
import com.jiangyifen.ec2.servlet.http.common.pojo.UserCallStatus;
import com.jiangyifen.ec2.servlet.http.common.utils.AnalyzeIfaceJointUtil;
import com.jiangyifen.ec2.servlet.http.common.utils.GsonUtil;
import com.jiangyifen.ec2.utils.SpringContextHolder;

/**
 * @Description 描述：调用该接口，用于获取指定用户通话状态
 * 
 * 	坐席必须同时具备以下条件，方可获取指定用户通话状态成功：
 * 
 * 		1、用户存在（即要求用户名参数要正确）
 * 
 * 请求路径：http://{ec2_server_ip}:{ec2_server_port}/ec2/http/common/aquireCallStatus?accessId=xxx&accessKey=xxx&username=1001
 *
 * eg.
 * 	http://192.168.2.160:8080/ec2/http/common/acquireCallStatus?accessId=xxx&accessKey=xxx&username=1001
 * 
 * @author  JRH
 * @date    2014年8月12日 下午3:19:31
 */
@SuppressWarnings("serial")
public class AcquireCallStatusServlet extends HttpServlet {

	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private UserService userService = SpringContextHolder.getBean("userService");

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		CommonRespBo commonRespBo = new CommonRespBo();		// 响应信息
		commonRespBo.setCode("0");
		commonRespBo.setMessage("获取指定用户通话状态成功！");

		try {
			
			Long domainId = (Long) request.getAttribute("domainId");		// 通过com.jiangyifen.ec2.servlet.http.common.filter.HttpCommonFilter 获得
			String username = StringUtils.trimToEmpty(request.getParameter("username"));

			if("".equals(username)) {
				commonRespBo.setCode("-1");
				commonRespBo.setMessage("失败，用户名不能为空！");
				logger.warn("JRH - IFACE 获取指定用户通话状态失败，原因：请求参数中用户名username 值为空！");
				operateResponse(response, commonRespBo);
				return;
			}

			List<User> userLs = userService.getUsersByUsername(username, domainId);
			if (userLs == null || userLs.size() == 0) {
				commonRespBo.setCode("-1");
				commonRespBo.setMessage("失败，用户不存在，用户名有误！");
				logger.warn("JRH - IFACE 获取指定用户通话状态失败，原因：请求参数中用户名有误，租户编号"+domainId+"下，用户"+username+"不存在！");
				operateResponse(response, commonRespBo);
				return;
			}
			
			UserCallStatus userCallStatus = new UserCallStatus();
			userCallStatus.setUsername(username); 
			commonRespBo.setResults(userCallStatus);
			
			User loginUser = userLs.get(0);
			String exten = ShareData.userToExten.get(loginUser.getId());
			if(exten == null) {
				userCallStatus.setStatusCode(-1);
				userCallStatus.setDestcription("用户不在线");
				operateResponse(response, commonRespBo);
				return;
			}

			/****************************** 分机是否注册正常  ***********************************/
			ExtenStatus extenStatus = ShareData.extenStatusMap.get(exten);
			boolean isregisted = true;
			if(extenStatus == null) {	// 分机状态维护信息不存在，则表明无法呼叫该分机
				isregisted = false;
			} else if(extenStatus.getRegisterStatus() == null) {	// 分机没注册上，则表明无法呼叫该分机
				isregisted = false;
			} else if(!extenStatus.getRegisterStatus().contains("OK") && !extenStatus.getRegisterStatus().contains("ok")) {	// 分机虽然注册了，但状态不可用，则表明无法呼叫该分机
				isregisted = false;
			}
			
			if(!isregisted) {
				userCallStatus.setStatusCode(-2);
				userCallStatus.setDestcription("分机不可用");
				operateResponse(response, commonRespBo);
				return;
			}
			
			Set<String> channels = ShareData.peernameAndChannels.get(exten);
			if(channels != null && channels.size() == 0){
				userCallStatus.setStatusCode(3);
				userCallStatus.setDestcription("无通话");
				operateResponse(response, commonRespBo);
				return;
			}

			String chanel = "";
			for(String ch : channels) {
				chanel = ch; break;
			}
			ChannelLifeCycle clc = ShareData.channelToChannelLifeCycle.get(chanel);
			logger.info(clc + "");
			
			if(clc != null) {
				String destlinenum = clc.getDestlinenum();
				String connectedlinenum = clc.getConnectedlinenum();

				Long downStateTime = clc.getDownStateTime().getTime();		// 通道产生的时间点
				Integer duration = (int) ((System.currentTimeMillis() - downStateTime + 500) / 1000);
				userCallStatus.setDuration(duration); 

				List<String> domainExtLs = ShareData.domainToExts.get(domainId);

				if(clc.getOriginateDialTime() != null && destlinenum != null && !domainExtLs.contains(destlinenum)) {	// 坐席是主叫
					userCallStatus.setDirection("outgoing");
					userCallStatus.setSrcNum(exten);
					userCallStatus.setDestNum(destlinenum);

				} else if(clc.getDestlinenum() == null && connectedlinenum != null && !domainExtLs.contains(connectedlinenum)) {	// 坐席是被叫，客户呼入	
					userCallStatus.setDirection("incoming");
					userCallStatus.setSrcNum(connectedlinenum);
					userCallStatus.setDestNum(exten);

				} else {
					userCallStatus.setDirection("inner");
					userCallStatus.setSrcNum(connectedlinenum);	// 这种情况代表 exten 即当前坐席是被呼叫者
					userCallStatus.setDestNum(exten);
					if(destlinenum != null && !destlinenum.equals(connectedlinenum)) {	// 这种情况代表exten 即当前坐席是呼叫发起者
						userCallStatus.setSrcNum(exten);
						userCallStatus.setDestNum(destlinenum);
					}
				}
				
				if(clc.getBridgedChannel() == null) {
					userCallStatus.setStatusCode(1);
					userCallStatus.setDestcription("呼叫中");
				} else {
					userCallStatus.setStatusCode(2);
					userCallStatus.setDestcription("通话中");
				}
				
				operateResponse(response, commonRespBo);
				return;
			} 
			
			userCallStatus.setStatusCode(-100);
			userCallStatus.setDestcription("未知状态 ");		// 正常不会出现该情况
			operateResponse(response, commonRespBo);
		} catch (Exception e) {
			commonRespBo.setCode("-1");
			commonRespBo.setMessage("失败，未知错误！！");
			operateResponse(response, commonRespBo);

			logger.error("JRH - IFACE 获取指定用户通话状态, 出现异常！"+e.getMessage(), e);
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
