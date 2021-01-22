package com.jiangyifen.ec2.sms;

import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.jiangyifen.ec2.entity.SmsInfo;

public class SmsSendExample implements SmsSendIface{
	@Override
	public HashMap<String,String> sendSMS(SmsInfo smsInfo, List<String> toList, String content) {
		HashMap<String,String> resultMap = new HashMap<String,String>();
		System.out.println(toList);
		String toPhones=StringUtils.join(toList,',');
		System.out.println("http://xxxx-xxxx?username="+smsInfo.getUserName()+"&password="+smsInfo.getPassword()+"&to="+toPhones+"&content="+content);
		System.out.println("成功以通道"+this.getClass().getName()+"发送短信!");
		for(String phone : toList) {
			resultMap.put(phone, "成功");
		}
		return resultMap;
	}
	
}
