package com.jiangyifen.ec2.servlet.http.common.meeting;

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

import com.jiangyifen.ec2.bean.ExtenStatus;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.service.eaoservice.UserService;
import com.jiangyifen.ec2.servlet.http.common.pojo.CommonRespBo;
import com.jiangyifen.ec2.servlet.http.common.utils.AnalyzeIfaceJointUtil;
import com.jiangyifen.ec2.servlet.http.common.utils.GsonUtil;
import com.jiangyifen.ec2.servlet.http.common.utils.MeetingRoomUtil;
import com.jiangyifen.ec2.utils.SpringContextHolder;

/**
 * @Description 描述: 调用该接口, 用于邀请他人(他人表示分机/手机/座席用户名)加入会议室
 *  
 * 该接口可以邀请 座席用户名/分机号/手机或固话. 当满足以下条件时可以邀请成功:
 *   1. 邀请座席用户名 (username)
 *    该座席必须登录, 并绑定了有效的分机.
 *   2. 邀请分机号 (exten)
 *    该分机号必须是在系统中存在的, 并且该分机已经成功注册.
 *   3. 邀请手机或固话 (phone)
 *    该手机或固话号码必须为 7 - 13 位的数字(包括 7 和 13).
 *   4. 根据发起邀请的分机号进行结束会议室(stopMeetingRoom)
 *   
 * 请求路径: http://{ec2_server_ip}:{ec2_server_port}/ec2/http/common/meeting/inviteJoinMeetingRoom?accessId=xxx&accessKey=xxx&domainId=x&inviteType=xxx&originateExten=xxxx&inviteExten=xxxx&phoneNumber=xxxxxxxxxxx&username=xxxx
 *  
 * eq.
 *  发起邀请分机的接口示例: http://192.168.1.160:8080/ec2/http/common/meeting/inviteJoinMeetingRoom?accessKey=6F8906ED473C34D5D62CF65D9597DBB0&accessId=2014072586956690HTDM1&domainId=1&inviteType=exten&originateExten=800001&inviteExten=800002
 *  发起邀请手机的接口示例: http://192.168.1.160:8080/ec2/http/common/meeting/inviteJoinMeetingRoom?accessKey=6F8906ED473C34D5D62CF65D9597DBB0&accessId=2014072586956690HTDM1&domainId=1&inviteType=phone&originateExten=800001&phoneNumber=0158247xxxxx
 *  发起邀请用户的接口示例: http://192.168.1.160:8080/ec2/http/common/meeting/inviteJoinMeetingRoom?accessKey=6F8906ED473C34D5D62CF65D9597DBB0&accessId=2014072586956690HTDM1&domainId=1&inviteType=username&originateExten=800001&username=1002
 *  结束会议室的接口示例:   http://192.168.1.160:8080/ec2/http/common/meeting/inviteJoinMeetingRoom?accessKey=6F8906ED473C34D5D62CF65D9597DBB0&accessId=2014072586956690HTDM1&domainId=1&inviteType=stopMeetingRoom&originateExten=800001
 *  
 * @author jht
 * 
 * @date 2015年9月24日 13:30:30
 */
@SuppressWarnings("serial")
public class InviteJoinMettingRoomServlet extends HttpServlet {

	private Logger logger = LoggerFactory.getLogger(this.getClass());
	private UserService userService = SpringContextHolder.getBean("userService");
	
	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		CommonRespBo commonRespBo = new CommonRespBo();
		commonRespBo.setCode("0");
		commonRespBo.setMessage("邀请成功!");
		
		Long domainId = (Long) request.getAttribute("domainId");		// 通过com.jiangyifen.ec2.servlet.http.common.filter.HttpCommonFilter 获得
		String originateExten = StringUtils.trimToEmpty(request.getParameter("originateExten"));	// 发起呼叫的分机
		originateExten = MeetingRoomUtil.pickOutPhoneNo(originateExten);
		
		if("".equals(originateExten)) {
			commonRespBo.setCode("-1");
			commonRespBo.setMessage("失败, 发起会议室邀请的分机号不能为空, 并且发起呼叫的号必须为数字!");
			logger.warn("jinht - IFACE 发送邀请加入会议室失败, 原因: 请求参数中发起会议室邀请的分机号不能为空, 并且分机号必须为数字!");
			operateResponse(response, commonRespBo);
			return;
		}
		
		Long userId = ShareData.extenToUser.get(originateExten);
		if(userId == null) {
			commonRespBo.setCode("-2");
			commonRespBo.setMessage("失败, 发起会议室邀请的分机没有和登录用户进行绑定!");
			logger.warn("jinht - IFACE 发送邀请加入会议室失败, 原因: 请求参数中发起会议室邀请的分机没有和登录用户进行绑定!");
			operateResponse(response, commonRespBo);
			return;
		}
		
		ExtenStatus originateExtenStatus = ShareData.extenStatusMap.get(originateExten);
		boolean originateIsregisted = true;
		if(originateExtenStatus == null) {	// 分机状态维护信息不存在，则表明无法呼叫该分机
			originateIsregisted = false;
		} else if(originateExtenStatus.getRegisterStatus() == null) {	// 分机没注册上，则表明无法呼叫该分机
			originateIsregisted = false;
		} else if(!originateExtenStatus.getRegisterStatus().contains("OK") && !originateExtenStatus.getRegisterStatus().contains("ok")) {	// 分机虽然注册了，但状态不可用，则表明无法呼叫该分机
			originateIsregisted = false;
		}
		if(!originateIsregisted) {
			commonRespBo.setCode("-3");
			commonRespBo.setMessage("失败, 发起会议室邀请的分机状态不可用!");
			logger.warn("jinht - IFACE 发送邀请加入会议室失败, 原因: 请求参数中发起会议室邀请的分机状态不可用!");
			operateResponse(response, commonRespBo);
			return;
		}
		
		/**
		 * 这里应该对其新增以下功能:
		 *  1. 如果座席想要把当前接通的电话一并转入到会议室, 然后邀请第三个人进来的解决方案.
		 *  2. 如果座席不需要把当前通话转入会议室, 等待当前通话挂后再创建会议室的解决方案.
		 */
		/*Set<String> originateChannels = ShareData.peernameAndChannels.get(originateExten);
		if(originateChannels != null && originateChannels.size() > 0){
			commonRespBo.setCode(-4);
			commonRespBo.setMessage("失败，发起会议室邀请的分机尚有通话未挂断！");
			logger.warn("jinht - IFACE 发送邀请加入会议室失败, 原因: 请求参数中发起会议室邀请的分机尚有通话未挂断！");
			operateResponse(response, commonRespBo);
			return;
		}*/
		
		String inviteType = StringUtils.trimToEmpty(request.getParameter("inviteType"));			// 邀请的类型. exten 分机; phone 手机; username 用户名.
		if("phone".equals(inviteType)) {
			String phoneNumber = StringUtils.trimToEmpty(request.getParameter("phoneNumber"));			// 被邀请的手机号
			phoneNumber = MeetingRoomUtil.pickOutPhoneNo(phoneNumber);
			if(phoneNumber.equals("") || !StringUtils.isNumeric(phoneNumber) || phoneNumber.length() < 7 || phoneNumber.length() > 13) {
				commonRespBo.setCode("-1");
				commonRespBo.setMessage("失败, 被邀请的手机号不能为空, 并且号码数字长度在 7-13 之间! ");
				logger.warn("jinht - IFACE 发送邀请加入会议室失败, 原因: 请求中被邀请的手机号不能为空, 并且号码数字长度在 7-13 之间!");
				operateResponse(response, commonRespBo);
				return;
			}
			
			Boolean isSuccess = MeetingRoomUtil.executeInvitePhoneNumber(phoneNumber, originateExten, domainId);	// 发起邀请手机号的请求
			if(!isSuccess) {
				commonRespBo.setCode("-5");
				// 失败, 在发送邀请手机号加入会议室时出现异常!
				commonRespBo.setMessage("失败, 失败原因未知! ");
				logger.warn("jinht - IFACE 发送邀请加入会议室失败, 原因: 失败原因未知!");
				operateResponse(response, commonRespBo);
				return;
			}
			
			commonRespBo.setCode("0");
			commonRespBo.setMessage("成功邀请手机号码加入会议室!");
			
		} else if("exten".equals(inviteType)) {
			String inviteExten = StringUtils.trimToEmpty(request.getParameter("inviteExten"));			// 被邀请的分机
			inviteExten = MeetingRoomUtil.pickOutPhoneNo(inviteExten);
			if("".equals(inviteExten)) {
				commonRespBo.setCode("-1");
				commonRespBo.setMessage("被邀请的分机号不能为空, 并且分机号必须为数字!");
				logger.warn("jinht - IFACE 发送邀请加入会议室失败, 原因: 请求参数中被邀请的分机不能为空, 并且分机号必须为数字!");
				operateResponse(response, commonRespBo);
				return;
			}
			
			Long inviteUserId = ShareData.extenToUser.get(inviteExten);
			if(inviteUserId == null) {
				commonRespBo.setCode("-2");
				commonRespBo.setMessage("失败, 被邀请的分机没有和用户进行绑定!");
				logger.warn("jinht - IFACE 发送邀请加入会议室失败, 原因: 请求参数中的被邀请的分机没有和用户进行绑定!");
				operateResponse(response, commonRespBo);
				return;
			}
			
			ExtenStatus inviteExtenStatus = ShareData.extenStatusMap.get(inviteExten);
			boolean extenIsRegisted = true;
			if(inviteExtenStatus == null) {		// 分机状态维护信息不存在, 则表明无法呼叫该分机
				extenIsRegisted = false;
			} else if(inviteExtenStatus.getRegisterStatus() == null) {		// 分机没有注册上, 则表明无法呼叫该分机
				extenIsRegisted = false;
			} else if(!inviteExtenStatus.getRegisterStatus().contains("OK") && !inviteExtenStatus.getRegisterStatus().contains("ok")) {		// 分机虽然注册了, 但是状态不可用, 则表名无法呼叫该分机
				extenIsRegisted = false;
			}
			
			if(!extenIsRegisted) {
				commonRespBo.setCode("-3");
				commonRespBo.setMessage("失败, 被邀请的分机状态不可用!");
				logger.warn("jinht - IFACE 发送邀请加入会议室失败, 原因: 请求参数中被邀请的分机状态不可用!");
				operateResponse(response, commonRespBo);
				return;
			}
			
			Set<String> inviteChannels = ShareData.peernameAndChannels.get(inviteExten);
			if(inviteChannels != null && inviteChannels.size() > 0) {
				commonRespBo.setCode("-4");
				commonRespBo.setMessage("失败, 被邀请的分机尚有通话为挂断!");
				logger.warn("jinht - IFACE 发送邀请加入会议室失败, 原因: 请求参数中被邀请的分机尚有通话为挂断!");
				operateResponse(response, commonRespBo);
				return;
			}
			
			boolean isSuccess = MeetingRoomUtil.executeInviteExten(inviteExten, originateExten);
			if(!isSuccess) {
				commonRespBo.setCode("-5");
				// 失败, 在发送邀请手机号加入会议室时出现异常!
				commonRespBo.setMessage("失败, 失败原因未知! ");
				logger.warn("jinht - IFACE 发送邀请加入会议室失败, 原因: 失败原因未知!");
				operateResponse(response, commonRespBo);
				return;
			}
			
			commonRespBo.setCode("0");
			commonRespBo.setMessage("成功邀请分机加入会议室!");
			
		} else if("username".equals(inviteType)) {
			String username = StringUtils.trimToEmpty(request.getParameter("username"));				// 被邀请的用户名
			if("".equals(username)) {
				commonRespBo.setCode("-1");
				commonRespBo.setMessage("失败, 被邀请的用户名不能为空或空字符串!");
				logger.warn("jinht - IFACE 发送邀请加入会议室失败, 原因: 请求参数中被邀请的用户名不能为空或空字符串!");
				operateResponse(response, commonRespBo);
				return;
			}
			
			List<User> usersList = userService.getUsersByUsername(username, domainId);
			if(usersList.size() == 0) {
				commonRespBo.setCode("-1");
				commonRespBo.setMessage("失败, 被邀请的用户名在系统中不存在!");
				logger.warn("jinht - IFACE 发送邀请加入会议室失败, 原因: 请求参数中被邀请的用户名在系统中不存在!");
				operateResponse(response, commonRespBo);
				return;
			}
			
			User user = usersList.get(0);
			String extenNumber = ShareData.userToExten.get(user.getId());
			if(extenNumber == null) {
				commonRespBo.setCode("-2");
				commonRespBo.setMessage("失败, 被邀请的用户名没有和分机进行绑定!");
				logger.warn("jinht - IFACE 发送邀请加入会议室失败, 原因: 请求参数中被邀请的用户名没有和分机进行绑定!");
				operateResponse(response, commonRespBo);
				return;
			}
			
			ExtenStatus extenNumberStatus = ShareData.extenStatusMap.get(extenNumber);
			boolean extenNumberIsRegisted = true;
			if(extenNumberStatus == null) {		// 分机状态维护信息不存在, 则表明无法呼叫该分机
				extenNumberIsRegisted = false;
			} else if(extenNumberStatus.getRegisterStatus() == null) {		// 分机没有注册上, 则表明无法呼叫该分机
				extenNumberIsRegisted = false;
			} else if(!extenNumberStatus.getRegisterStatus().contains("OK") && !extenNumberStatus.getRegisterStatus().contains("ok")) {		// 分机虽然注册了, 但是状态不可用, 则表名无法呼叫该分机
				extenNumberIsRegisted = false;
			}
			
			if(!extenNumberIsRegisted) {
				commonRespBo.setCode("-3");
				commonRespBo.setMessage("失败, 被邀请的用户绑定的分机状态不可用!");
				logger.warn("jinht - IFACE 发送邀请加入会议室失败, 原因: 请求参数中被邀请的用户绑定的分机状态不可用!");
				operateResponse(response, commonRespBo);
				return;
			}
			
			Set<String> extenNumberChannels = ShareData.peernameAndChannels.get(extenNumber);
			if(extenNumberChannels != null && extenNumberChannels.size() > 0) {
				commonRespBo.setCode("-4");
				commonRespBo.setMessage("失败, 被邀请的用户绑定的分机尚有通话为挂断!");
				logger.warn("jinht - IFACE 发送邀请加入会议室失败, 原因: 请求参数中被邀请的用户绑定的分机尚有通话为挂断!");
				operateResponse(response, commonRespBo);
				return;
			}
			
			boolean isSuccess = MeetingRoomUtil.executeInviteExten(extenNumber, originateExten);
			if(!isSuccess) {
				commonRespBo.setCode("-5");
				// 失败, 在发送邀请手机号加入会议室时出现异常!
				commonRespBo.setMessage("失败, 失败原因未知! ");
				logger.warn("jinht - IFACE 发送邀请加入会议室失败, 原因: 失败原因未知!");
				operateResponse(response, commonRespBo);
				return;
			}
			
			commonRespBo.setCode("0");
			commonRespBo.setMessage("成功邀请用户加入会议室!");
			
		} else if("stopMeetingRoom".equals(inviteType)) {
			boolean isSuccess = MeetingRoomUtil.stopMeetingRoom(originateExten);
			if(!isSuccess) {
				commonRespBo.setCode("-5");
				commonRespBo.setMessage("失败, 失败原因未知!");
				logger.warn("jinht - IFACE 结束会议室失败, 原因: 失败原因未知!");
				operateResponse(response, commonRespBo);
				return;
			}
			
			commonRespBo.setCode("0");
			commonRespBo.setMessage("成功结束本次会议室通话!");
			
		}
		
		operateResponse(response, commonRespBo);
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
