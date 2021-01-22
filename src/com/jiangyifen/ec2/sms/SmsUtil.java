package com.jiangyifen.ec2.sms;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.jiangyifen.ec2.entity.Message;
import com.jiangyifen.ec2.entity.SmsInfo;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.entity.enumtype.MessageType;
import com.jiangyifen.ec2.globaldata.SmsShareData;
import com.jiangyifen.ec2.service.eaoservice.MessagesManageService;
import com.jiangyifen.ec2.utils.SpringContextHolder;
/**
 * 短信发送类
 * @author chb
 * 添加一个短信通道要做3件事：
 * 1、添加一个SmsSendIface的通道实现
 * 2、在applicationContextSms.xml中将配置一个通道的实现实例
 * 3、在数据库中插入一条SmsInfo的配置，其中smsChannelName的名字为spring中配置的单例Bean的名字
 * 
 */
public class SmsUtil {
	
	private static MessagesManageService messagesManageService;
	
	static{
		messagesManageService = SpringContextHolder.getBean("messagesManageService");
	}
	
	public static String sendSMS(User user,String extenNumber,Long domainId,List<String> toList, String content){
		SmsInfo smsInfo=SmsShareData.domainToSmsInfo.get(domainId);
		if(smsInfo==null){
			throw new RuntimeException("贵公司所属域 ID："+domainId+"  尚未开通短信通道，暂不能发送短信！");
		} 
		
		if(toList.size() == 0) {	// 这个一般不会存在，因为在界面就应该做判断
			throw new RuntimeException("短信接收者不能为空！");
		}
		
		//TODO 短信发送速度的调节,一次发送的数量不能超过100
		SmsSendIface smsSendIface = SpringContextHolder.getBean(smsInfo.getSmsChannelName());
		if(smsSendIface == null) {
			throw new RuntimeException("贵公司所属域 ID："+domainId+"  短信通道配置有误，暂不能发送短信！");
		}
		
		HashMap<String,String> resultMap = smsSendIface.sendSMS(smsInfo, toList, content);

		int successCount = 0;
		String result = "未知状态";
		
		//向数据库中记录短消息发送日志
		for(String toPhoneNumber : toList){
			
			// jrh 创建短信对象
			Message message = new Message();
			message.setContent(content);
			message.setDomain(user.getDomain());
			message.setPhoneNumber(toPhoneNumber);
			message.setTime(new Date());
			message.setUser(user);
			
			String sendResult = resultMap.get(toPhoneNumber);
			if(sendResult == null) {	// 这种情况一般不会出现
				sendResult = "未知状态";
			}
			if(sendResult.contains("成功")) {
				message.setMessageType(MessageType.SUCCESS);
				++successCount;
				result = sendResult;
			} else if(sendResult.contains("未知错误")) {
				message.setMessageType(MessageType.UNKNOWN);
				result = sendResult;
			} else {
				message.setMessageType(MessageType.FAILED);
				result = sendResult;
			}
			messagesManageService.updateMessage(message);
			
//			// chb 创建短信日志
//			SmsLog smsLog=new SmsLog();
//			smsLog.setContent(content);
//			smsLog.setDestPhoneNumber(toPhoneNumber);
//			smsLog.setDomain(smsInfo.getDomain());
//			smsLog.setExtenNumber(extenNumber);
//			smsLog.setSendDate(new Date());
//			smsLog.setUser(user);
//			if("成功".equals(sendResult)) {
//				smsLog.setSmsStatus(SmsStatus.SUCCESS);
//			} else if("未知错误".equals(sendResult)) {
//				smsLog.setSmsStatus(SmsStatus.UNKNOWN);
//			} else {
//				smsLog.setSmsStatus(SmsStatus.FAILED);
//			}
//			commonService.save(smsLog);
		}
		
		if(successCount > 0) {
			int failedCount = toList.size() - successCount;
			result = "成功发送 "+successCount+" 条！";
			if(failedCount > 0) {
				result += "失败"+failedCount+" 条！";
			}
		}

		return result;
	}
}



