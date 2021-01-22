package com.jiangyifen.ec2.sms;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.entity.SmsInfo;
import com.jiangyifen.ec2.utils.SmsConfig;

/**
 * duanxin10086 短信平台的短信发送通道 
 *
	<?xml version="1.0" encoding="utf-8" ?>
	<returnsms>
		<returnstatus>status</returnstatus> 		 --------- 返回状态值：成功返回 Success 失败返回：Faild
		<message>message</message> 					 --------- 返回信息：见下表
		<remainpoint>remainpoint</remainpoint>  	 --------- 返回余额（剩余可发送条数）
		<taskID>taskID</taskID>  					 --------- 返回本次任务的序列ID
		<successCounts>successCounts</successCounts> --------- 成功短信数：当成功后返回提交成功短信数
	</returnsms>

	返回信息提示(returnstatus 的值)		说明
	------------------------------------
	ok								提交成功
	用户名或密码不能为空					提交的用户名或密码为空
	发送内容包含sql注入字符				包含sql注入字符
	用户名或密码错误						表示用户名或密码错误
	短信号码不能为空						提交的被叫号码为空
	短信内容不能为空						发送内容为空
	包含非法字符：						表示检查到不允许发送的非法字符
	对不起，您当前要发送的量大于您当前余额		当支付方式为预付费是，检查到账户余额不足
	其他错误							其他数据库操作方面的错误

 * @author jrh
 * 
 */
public class Dx10086SmsSend implements SmsSendIface {
	// 日志工具
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Override
	public HashMap<String,String> sendSMS(SmsInfo smsInfo, List<String> toList, String content) {
		HashMap<String,String> resultMap = new HashMap<String,String>();
		
	 
		
		for(String receiver : toList) {
			try {
//				// 编辑短信接收人
//	        	String receivers = StringUtils.join(toList,'\r\n');
				
				//建立指定url的Http连接
	        	String partUrl = SmsConfig.props.getProperty(SmsConfig.DUANXIN10086_SMS_ROUTE);
	        	
	        	URL url = new URL(partUrl);
	        	String pw = smsInfo.getPassword();
			    String params = "&userid=8280&account="+smsInfo.getUserName()+"&password="+pw+"&mobile="+receiver+"&content="+content;
			    
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
				String xmlResult = StringUtils.trimToEmpty(stringBuffer.toString());
				
				// 提取代表发送成功的状态值
				String returnstatus = xmlResult.substring(xmlResult.indexOf("<returnstatus>")+14, xmlResult.indexOf("</returnstatus>"));
				logger.info("客户使用duanxin10086-短信平台的短信发送通道 发送短信------返回状态值为(returnstatus)："+returnstatus);
				
				if("Success".equals(returnstatus)) {
					resultMap.put(receiver, "短信发送成功");
				} else {
					String message = xmlResult.substring(xmlResult.indexOf("<message>")+9, xmlResult.indexOf("</message>"));
					resultMap.put(receiver, message);
					logger.info("客户使用duanxin10086-短信平台的短信发送通道 发送短信失败------失败原因为(message)："+message);
				}
				
			} catch (MalformedURLException e) {
				e.printStackTrace();
				logger.error("jrh 客户使用duanxin10086-短信平台的短信发送通道 发送短信失败，可能是授权已过期-->"+e.getMessage(), e);
				resultMap.put(receiver, "可能是授权已过期");
			} catch (ProtocolException e) {
				e.printStackTrace();
				logger.error("jrh 客户使用duanxin10086-短信平台的短信发送通道 发送短信失败，可能是授权已过期-->"+e.getMessage(), e);
				resultMap.put(receiver, "可能是授权已过期");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				logger.error("jrh 客户使用duanxin10086-短信平台的短信发送通道 发送短信失败，可能是授权已过期-->"+e.getMessage(), e);
				resultMap.put(receiver, "可能是授权已过期");
			} catch (IOException e) {
				e.printStackTrace();
				logger.error("jrh 客户使用duanxin10086-短信平台的短信发送通道 发送短信失败，可能是授权已过期-->"+e.getMessage(), e);
				resultMap.put(receiver, "可能是授权已过期");
			}
		}
		
		return resultMap;
	}
	
//	public static void main(String[] args) {
//		
//		String xmlResult = "<?xml version='1.0' encoding='utf-8' ?><returnsms>"
//				+ "<returnstatus>Faild</returnstatus>"
//				+ "<message>用户名或密码错误</message>"
//				+ "<remainpoint>0</remainpoint>"
//				+ "<taskID>0</taskID>"
//				+ "<successCounts>0</successCounts></returnsms>";
//		
//		xmlResult = "<?xml version='1.0' encoding='utf-8' ?><returnsms>"
//				+ "<returnstatus>Success</returnstatus>"
//				+ "<message>ok</message>"
//				+ "<remainpoint>4</remainpoint>"
//				+ "<taskID>691593</taskID>"
//				+ "<successCounts>1</successCounts></returnsms>";
//		
//		String returnstatus = xmlResult.substring(xmlResult.indexOf("<returnstatus>")+14, xmlResult.indexOf("</returnstatus>"));
//		
//		String message = xmlResult.substring(xmlResult.indexOf("<message>")+9, xmlResult.indexOf("</message>"));
//	
//		System.out.println(returnstatus);
//		System.out.println(message);
//		
//	}
	
}
