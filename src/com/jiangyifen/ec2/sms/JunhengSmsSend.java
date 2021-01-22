package com.jiangyifen.ec2.sms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.entity.SmsInfo;
import com.jiangyifen.ec2.utils.SmsConfig;

/**
 * JunhengSmsSend 君恒代理短信通道
 * 
 *
 * @author chenhb
 * 
 */

/*
1、配置文件propertiefiles/sms.properties
	junheng_sms_route = http://sdk2.entinfo.cn/z_send.aspx

2、配置文件applicationContextSms.xml中添加
	<!-- junhengSmsSend 【君恒代理通道】 -->
	<bean id="junhengSmsSend" class="com.jiangyifen.ec2.sms.JunhengSmsSend"></bean>

3、数据库插入sql
	insert into ec2_smsinfo(id,companyname,username,password,smschannelname,domain_id) values(1,'','SDK-JHK-010-00001','204063','junhengSmsSend',1);
	注：companyname为可选签名字段 ，如果以#开头，则将#后面的字段添加到签名，如果以$开头，则将$后面的字段添加到签名头部,其它开头识别为标识字段，不用做签名用途
	       如#【汉天】,则签名【汉天】到短信末尾
	
4、SmsConfig 更改
	// 【君恒代理短信通道】
	public static String JUNHENG_SMS_ROUTE = "junheng_sms_route";
	
5、完成短信业务逻辑
	添加类com.jiangyifen.ec2.sms.Client
	添加类com.jiangyifen.ec2.sms.Demo_Client 可选
	完善JunhengSmsSend 这个业务类
 */
public class JunhengSmsSend implements SmsSendIface {
	// 日志工具
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	 

//	 1	没有需要取得的数据	取用户回复就出现1的返回值,表示没有回复数据
//	-2 	帐号/密码不正确	1.序列号未注册2.密码加密不正确3.密码已被修改
//	-4	余额不足	直接调用查询看是否余额为0或不足
//	-5	数据格式错误	
//	-6	参数有误	看参数传的是否均正常,请调试程序查看各参数
//	-7	权限受限	该序列号是否已经开通了调用该方法的权限
//	-8	流量控制错误	
//	-9	扩展码权限错误	该序列号是否已经开通了扩展子号的权限
//	-10	内容长度长	短信内容过长
//	-11	内部数据库错误	
//	-12	序列号状态错误	序列号是否被禁用
//	-13	没有提交增值内容	
//	-14	服务器写文件失败	
//	-15	文件内容base64编码错误	
//	-16	返回报告库参数错误	
//	-17	没有权限	
//	-18	上次提交没有等待返回不能继续提交	
//	-19	禁止同时使用多个接口地址	每个序列号提交只能使用一个接口地址
	
	private static final Map<String,String> codeToResult=new HashMap<String, String>();
	static{
		codeToResult.put("1", "没有需要取得的数据");
		codeToResult.put("-2", "帐号或密码不正确");
		codeToResult.put("-4", "余额不足");
		codeToResult.put("-5", "数据格式错误");
		codeToResult.put("-6", "参数有误");
		codeToResult.put("-7", "权限受限");
		codeToResult.put("-8", "流量控制错误");
		codeToResult.put("-9", "扩展码权限错误");
		codeToResult.put("-10", "内容长度长");
		codeToResult.put("-11", "内部数据库错误");
		codeToResult.put("-12", "序列号状态错误");
		codeToResult.put("-13", "没有提交增值内容");
		codeToResult.put("-14", "服务器写文件失败");
		codeToResult.put("-15", "文件内容base64编码错误");
		codeToResult.put("-16", "返回报告库参数错误");
		codeToResult.put("-17", "没有权限");
		codeToResult.put("-18", "上次提交没有等待返回不能继续提交");
		codeToResult.put("-19", "禁止同时使用多个接口地址");	
	}
	
	public static void main(String[] args) throws Exception {
		
		SmsInfo smsInfo=new SmsInfo();
//		smsInfo.setCompanyName("【ccc】");
//		smsInfo.setDescription("");
//		smsInfo.setDomain(null);
//		smsInfo.setId(null);
//		smsInfo.setUserName("SDK-JHK-010-00002");
		smsInfo.setUserName("SDK-JHK-010-00004");
		smsInfo.setPassword("546612");
//		smsInfo.setPassword("524924");
//		smsInfo.setSmsChannelName("");
		List<String> toList=new ArrayList<String>();
		toList.add("13816760398");
		
		Client client = new Client(smsInfo.getUserName(), smsInfo.getPassword());
//		String content = new String("Hi,What a u doing?[上海君恒科技发展有限公司]".getBytes(), "GB2312");
//		System.out.println(content);
//		String result_mt = client.mt(StringUtils.join(toList, ","), content, "", "", "");
		String result_mt = client.mt(StringUtils.join(toList, ","), "Hi,What a u doing?你好[上金集团]", "", "", "");
		System.out.print(result_mt);
	
//		您好，感谢您在诺诺镑客注册，我是理财顾问谢挺，我的手机号18616349901，QQ2478818939，如有疑问可随时联系，祝生活愉快。
	}
	
	@Override
	public HashMap<String,String> sendSMS(SmsInfo smsInfo, List<String> toList, String content) {
		HashMap<String,String> resultMap = new HashMap<String,String>();
		
		//可签名字段 ，如果以#开头，则将#后面的字段添加到签名末尾，如果以$开头，则将$后面的字段添加到签名头部
		//如#【汉天】,则签名【汉天】到短信
		if(smsInfo.getCompanyName().startsWith("#")){
			content+="["+smsInfo.getCompanyName().substring(1)+"]";
		}else if(smsInfo.getCompanyName().startsWith("$")){
			content="["+smsInfo.getCompanyName().substring(1)+"]"+content;
		}
		
		try {
			Client client = new Client(smsInfo.getUserName(), smsInfo.getPassword());
			//设置地址，默认为http://sdk2.entinfo.cn/webservice.asmx
			client.setServiceURL(SmsConfig.props.getProperty(SmsConfig.JUNHENG_SMS_ROUTE));
			String result_mt = client.mt(StringUtils.join(toList, ","), content, "", "", "");
//			System.out.print(result_mt);
			
			//结果封装到map中
			String resultStr=codeToResult.get(result_mt);
			if(StringUtils.isBlank(result_mt)) {
				for(String phone:toList){
					resultMap.put(phone,"短信服务器错误("+result_mt+")");
				}
				logger.info("chenhb: JunhengSmsSend send sms ->"+content+"<- to ->"+toList+"<- unkown failed, result_num-"+result_mt);
			} else if(StringUtils.isBlank(resultStr)){
				for(String phone:toList){
					resultMap.put(phone,"发送成功("+result_mt+")");
				}
				logger.info("chenhb: JunhengSmsSend send sms ->"+content+"<- to ->"+toList+"<- success, result_num-"+result_mt);
			}else{
				for(String phone:toList){
					resultMap.put(phone,resultStr+"("+result_mt+")");
				}
				logger.info("chenhb: JunhengSmsSend send sms ->"+content+"<- to ->"+toList+"<- failed, result_num-"+result_mt+" reason-"+resultStr);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return resultMap;
	}
}
