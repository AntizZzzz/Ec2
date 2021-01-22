package com.jiangyifen.ec2.servlet.http.common.utils;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.asteriskjava.manager.action.CommandAction;
import org.asteriskjava.manager.action.OriginateAction;

import com.jiangyifen.ec2.ami.AmiManagerThread;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.service.eaoservice.UserService;
import com.jiangyifen.ec2.utils.SpringContextHolder;

/**
 * @Description 会议室功能实体类
 * 
 * @author jht
 *
 * @date 2015-9-24 10:23:13
 */
public class MeetingRoomUtil {

	private static UserService userService = SpringContextHolder.getBean("userService");
	
	/**
	 * 邀请座席用户名的方法
	 * @param userEmpno		被邀请用户名
	 * @param exten			当前发起会议室功能的分机号
	 * @return Boolean
	 */
	public static Boolean executeInviteCsr(String username, String exten) {
		List<User> usersList = userService.getUsersByUsername(username);
		
		if(usersList.size() == 0) {		// 系统中不存在该用户名
			return false;
		}
		
		User user = usersList.get(0);
		String extenNumber = ShareData.userToExten.get(user.getId());
		if(extenNumber == null) {		// 该用户当前没有登录, 或者该用户没有绑定分机
			return false;
		}
		
		return invite("SIP/" + extenNumber, extenNumber, exten);
	}
	
	/**
	 * 邀请分机的方法
	 * @param inviteExten		被邀请的分机号
	 * @param exten				当前发起会议室功能的分机号
	 * @return Boolean
	 */
	public static Boolean executeInviteExten(String inviteExten, String exten) {
		if(inviteExten == null || "".equals(inviteExten)) {			// 如果被邀请的分机号为空, 或者为空字符串, 则返回 false
			return false;
		}
		return invite("SIP/" + inviteExten, inviteExten, exten);
	}
	
	/**
	 * 邀请手机的方法
	 * @param phoneNumber		被邀请的手机号
	 * @param exten				当前发起会议室功能的分机号
	 * @param domainId			当前用户所在的域
	 * @return Boolean
	 */
	public static Boolean executeInvitePhoneNumber(String phoneNumber, String exten, Long domainId) {
		// 检查输入是否合法
		if(phoneNumber.equals("") || !StringUtils.isNumeric(phoneNumber) || phoneNumber.length() < 7 || phoneNumber.length() > 13){
			return false;
		}
		
		// 发起 action 定位到会议室
		String outline = ShareData.domainToDefaultOutline.get(domainId);
		return invite("SIP/" + phoneNumber + "@" + outline, phoneNumber, exten);
	}
	
	/**
	 * 发起邀请的方法
	 * @param channel		通道
	 * @param callerId		被邀请的号码
	 * @param exten			当前使用的分机号
	 * @return Boolean		true 邀请成功; false 邀请失败, 出现异常
	 */
	private static Boolean invite(String channel, String callerId, String exten) {
		try {
			// 呼叫 Action
			OriginateAction originateAction = new OriginateAction();
			originateAction.setTimeout(5 * 60 * 1000L);
			originateAction.setChannel(channel);
			originateAction.setCallerId(callerId);
			originateAction.setExten("998" + exten);
			originateAction.setContext("outgoing");
			originateAction.setPriority(1);
			originateAction.setAsync(true);
			AmiManagerThread.sendAction(originateAction);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	/**
	 * 终止会议室的方法
	 * @param exten		要终止的发起呼叫的分机号
	 * @return Boolean	true 终止成功; false 终止失败
	 */
	public static Boolean stopMeetingRoom(String exten) {
		try {
			CommandAction commadAction = new CommandAction("meetme kick " + exten + " all");
			AmiManagerThread.sendAction(commadAction);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	/**
     * 取出文件中的电话号码，如原始数据位 ‘ 135&%￥1676jrh0398  ’, 执行后返回 13516760398
     * @param originalData     原始数据
     * @return
     */
	public static String pickOutPhoneNo(String originalData) {
		StringBuffer phoneNoStrBf = new StringBuffer();
		for (int i = 0; i < originalData.length(); i++) {
			char c = originalData.charAt(i);
			int assiiCode = (int) c;
			if (assiiCode >= 48 && assiiCode <= 57) {
				phoneNoStrBf.append(c);
			}
		}
		return phoneNoStrBf.toString();
	}
	
}
