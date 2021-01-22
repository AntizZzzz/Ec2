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
 * 苏沃科技短信平台的短信发送通道【呼太代理】
 * 		 成功				因为短信通道返回："001";   
 *  	用户名为空			因为短信通道返回： "-001";
 * 		密码为空 			因为短信通道返回："-002";
 * 		用户名或密码错误 		因为短信通道返回："-003";
 * 		编码方式有误 		因为短信通道返回："-004";
 * 		无二次接口权限 		因为短信通道返回："-005";
 * 		未知错误 			因为短信通道返回："-006";
 * 		"可能是授权已过期"; 	因为短信通道返回： http请求是出现异常，可能是通道已经被供应商停掉了
 * @author jrh
 *
 */
public class SuwoitSmsSend implements SmsSendIface {
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
	        	String partUrl = SmsConfig.props.getProperty(SmsConfig.SUWOIT_SMS_ROUTE);
	        	URL url = new URL(partUrl);
			    String params = "username="+smsInfo.getUserName()+"&password="+smsInfo.getPassword()+"&content="+content+"&receivePhone="+receiver;
			    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
			    httpURLConnection.setRequestMethod( "POST");
			    httpURLConnection.setDoOutput( true);
			    
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
				String result = StringUtils.trimToEmpty(stringBuffer.toString());
				
				if("001".equals(result)) {
					resultMap.put(receiver, "发送成功");
				} else if("-001".equals(result)) {
					resultMap.put(receiver, "用户名为空");
				} else if("-002".equals(result)) {
					resultMap.put(receiver, "密码为空");
				} else if("-003".equals(result)) {
					resultMap.put(receiver, "用户名或密码错误");
				} else if("-004".equals(result)) {
					resultMap.put(receiver, "编码方式有误");
				} else if("-005".equals(result)) {
					resultMap.put(receiver, "无二次接口权限");
				} else if("-006".equals(result)) {
					resultMap.put(receiver, "未知错误,短信发送状态不明，请检查下发内容及号码等信息！");
				}
				resultMap.put(receiver, result);
			} catch (MalformedURLException e) {
				e.printStackTrace();
				logger.error("jrh 客户使用苏沃科技短信平台-呼太代理的短信通道发送短信失败，可能是授权已过期-->"+e.getMessage(), e);
				resultMap.put(receiver, "可能是授权已过期");
			} catch (ProtocolException e) {
				e.printStackTrace();
				logger.error("jrh 客户使用苏沃科技短信平台-呼太代理的短信通道发送短信失败，可能是授权已过期-->"+e.getMessage(), e);
				resultMap.put(receiver, "可能是授权已过期");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				logger.error("jrh 客户使用苏沃科技短信平台-呼太代理的短信通道发送短信失败，可能是授权已过期-->"+e.getMessage(), e);
				resultMap.put(receiver, "可能是授权已过期");
			} catch (IOException e) {
				e.printStackTrace();
				logger.error("jrh 客户使用苏沃科技短信平台-呼太代理的短信通道发送短信失败，可能是授权已过期-->"+e.getMessage(), e);
				resultMap.put(receiver, "可能是授权已过期");
			}
		}
		
		return resultMap;
	}
	
}
