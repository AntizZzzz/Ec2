package com.jiangyifen.ec2.sms;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.jiangyifen.ec2.entity.SmsInfo;
import com.jiangyifen.ec2.utils.SmsConfig;

/**
 * ShjianzhouSmsSend 上海建周电子商务
 * 
 *
 * @author chenhb
 * 
 */
public class ShjianzhouSmsSend implements SmsSendIface {
	/*
1、配置文件propertiefiles/sms.properties
	shjianzhou（上海建周）  cus:ShanghaiWeixinDianziShangwu
	shjianzhou_sms_route = http://www.jianzhou.sh.cn/JianzhouSMSWSServer/http/sendBatchMessage

2、配置文件applicationContextSms.xml中添加
	<!-- shjianzhouSmsSend 短信通道 -->
	<bean id="shjianzhouSmsSend" class="com.jiangyifen.ec2.sms.ShjianzhouSmsSend"></bean>

3、数据库插入sql
	insert into ec2_smsinfo(id,companyname,username,password,smschannelname,domain) values(1,'上海建周','cjz306','31268545','shjianzhouSmsSend',1);

4、完成短信业务逻辑
	ShjianzhouSmsSend 这个业务类
	 */
	
	// 日志工具
//	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	public static void main(String[] args) {
		SmsInfo smsInfo=new SmsInfo();
		smsInfo.setCompanyName("建周科技");
		smsInfo.setDescription("");
//		smsInfo.setDomain(null);
//		smsInfo.setId(null);
		smsInfo.setUserName("cjz306");
		smsInfo.setPassword("31268545");
//		smsInfo.setSmsChannelName("");
		List<String> toList=new ArrayList<String>();
		toList.add("15021157421");
		new ShjianzhouSmsSend().sendSMS(smsInfo, toList, "下班回家吃饭喽！【建周科技】");
	}
	
	@SuppressWarnings("static-access")
	@Override
	public HashMap<String,String> sendSMS(SmsInfo smsInfo, List<String> toList, String content) {
		HashMap<String,String> resultMap = new HashMap<String,String>();
		
		for(String receiver : toList) {
			//超速提交(一般为每秒一次提交)
			//=============================每秒1次提交,此处浪费1s时间，添加在开头查看方便
			try {
				Thread.currentThread().sleep(1000L);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			//=============================
			
			
			try {
				//建立指定url的Http连接
	        	String partUrl = SmsConfig.props.getProperty(SmsConfig.SHJIANZHOU_SMS_ROUTE);
	        	
	        	URL url = new URL(partUrl);
	        	String username=smsInfo.getUserName();
	        	String password = smsInfo.getPassword();
			    String params = "account="+username+"&password="+password+"&destmobile="+receiver+"&msgText="+content;
			    
			    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
			    httpURLConnection.setRequestMethod("POST");
			    httpURLConnection.setDoOutput(true);
			    
			    //取得输出流并写入请求参数
			    OutputStream outputStream = httpURLConnection.getOutputStream();
			    outputStream.write(params.getBytes("UTF-8"));
			    outputStream.flush();
			    outputStream.close();
			    
			    //读取返回的内容到StringBuffer
			    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(
						         httpURLConnection.getInputStream(), "UTF-8"));
		    	StringBuffer stringBuffer = new StringBuffer();
				int ch;
				while ((ch = bufferedReader.read()) > -1) {
					stringBuffer.append((char) ch);
				}
				bufferedReader.close();
				
				//将StringBuffer中的内容输出
				String sendResult = StringUtils.trimToEmpty(stringBuffer.toString());
				
//				返回结果：
//				返回值	解释
//				大于0	提交成功，该数字为本批次的任务ID，提交成功后请自行保存发送记录。
//				-1	余额不足
//				-2	帐号或密码错误
//				-3	连接服务商失败
//				-4	超时
//				-5	其他错误，一般为网络问题，IP受限等
//				-6	短信内容为空
//				-7	目标号码为空
//				-8	用户通道设置不对，需要设置三个通道
//				-9	捕获未知异常
//				-10	超过最大定时时间限制
//				-11	目标号码在黑名单里
//				-12	消息内容包含禁用词语
//				-13	没有权限使用该网关
//				-14	找不到对应的Channel ID
//				-17	没有提交权限，客户端帐号无法使用接口提交
//				-18	提交参数名称不正确或确实参数
//				-19	必须为POST提交
//				-20	超速提交(一般为每秒一次提交)

				//结果解析
				int i = 0;
				try {
					i=Integer.parseInt(sendResult);
				} catch (Exception e) {
					resultMap.put(receiver, "短信提供商回复错误，原因未知("+sendResult+")");
					e.printStackTrace();
					continue;
				}
				
				
				if (i > 0) {
					resultMap.put(receiver, "提交成功("+i+")");
				} else if (i == -1) {
					resultMap.put(receiver, "余额不足(-1)");
				} else if (i == -2) {
					resultMap.put(receiver, "帐号或密码错误(-2)");
				} else if (i == -3) {
					resultMap.put(receiver, "连接服务商失败(-3)");
				} else if (i == -4) {
					resultMap.put(receiver, "超时(-4)");
				} else if (i == -5) {
					resultMap.put(receiver, "其他错误，一般为网络问题，IP受限等(-5)");
				} else if (i == -6) {
					resultMap.put(receiver, "短信内容为空(-6)");
				} else if (i == -7) {
					resultMap.put(receiver, "目标号码为空(-7)");
				} else if (i == -8) {
					resultMap.put(receiver, "用户通道设置不对，需要设置三个通道(-8)");
				} else if (i == -9) {
					resultMap.put(receiver, "捕获未知异常(-9)");
				} else if (i == -10) {
					resultMap.put(receiver, "超过最大定时时间限制(-10)");
				} else if (i == -11) {
					resultMap.put(receiver, "目标号码在黑名单里(-11)");
				} else if (i == -12) {
					resultMap.put(receiver, "消息内容包含禁用词语(-12)"); 
				} else if (i == -13) {
					resultMap.put(receiver, "没有权限使用该网关(-13)"); 
				} else if (i == -14) {
					resultMap.put(receiver, "找不到对应的Channel ID(-14)"); 
				} else if (i == -17) {
					resultMap.put(receiver, "没有提交权限，客户端帐号无法使用接口提交(-17)"); 
				} else if (i == -18) {
					resultMap.put(receiver, "提交参数名称不正确或确实参数(-18)"); 
				} else if (i == -19) {
					resultMap.put(receiver, "必须为POST提交(-19)"); 
				} else if (i == -20) {
					resultMap.put(receiver, "超速提交(一般为每秒一次提交)(-20)"); 
				} else {
					resultMap.put(receiver, "短信有可能没有发送成功，原因未知("+i+")");
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return resultMap;
	}
}
