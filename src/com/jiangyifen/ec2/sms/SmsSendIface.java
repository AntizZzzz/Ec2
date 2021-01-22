package com.jiangyifen.ec2.sms;

import java.util.HashMap;
import java.util.List;

import com.jiangyifen.ec2.entity.SmsInfo;

/**
 * 短信发送接口
 * @author chb
 *
 */
public interface SmsSendIface {
	/**
	 * 下发短信方法入口 
	 * @param smsInfo 短信账号密码等信息
	 * @param to 发到哪个手机号
	 * @param context 短信内容
	 * @return String 发送是否成功  【jrh 将Boolean 值改成String】
	 * 				 成功				因为短信通道返回："001";   
	 *  			 用户名为空			因为短信通道返回： "-001";
	 * 				 密码为空 			因为短信通道返回："-002";
	 * 				 用户名或密码错误 		因为短信通道返回："-003";
	 * 				 编码方式有误 		因为短信通道返回："-004";
	 * 			 	 无二次接口权限 		因为短信通道返回："-005";
	 * 			 	 未知错误 			因为短信通道返回："-006";
	 * 				"可能是授权已过期"; 	因为短信通道返回： http请求是出现异常，可能是通道已经被供应商停掉了
	 */
	public HashMap<String,String> sendSMS(SmsInfo smsInfo,List<String> toList, String content);
}
