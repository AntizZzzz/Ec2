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
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sun.misc.BASE64Encoder;

import com.jiangyifen.ec2.entity.SmsInfo;
import com.jiangyifen.ec2.utils.SmsConfig;

/**
 * 企信通短信平台的短信发送通道 【慧亮代理】
 * 		因为短信通道返回："success"开头		--	短信发送成功！  
 *  	因为短信通道返回： "failure"		--	短信发送失败，后面跟失败原因(如："failure;账号余额不足！")！
 * 		因为短信通道返回： http请求是出现异常，可能是通道已经被供应商停掉了	--	"可能是授权已过期"
 * @author jrh
 *
 */
public class QxtongSmsSend implements SmsSendIface {
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
	        	String partUrl = SmsConfig.props.getProperty(SmsConfig.QXTONG_SMS_ROUTE);
	        	URL url = new URL(partUrl);
	            String 	contentEncoded = URLEncoder.encode(content, "GBK");
				String pw = "";
				if(smsInfo.getPassword() != null) {
					pw = new BASE64Encoder().encode(smsInfo.getPassword().getBytes());
				}
			    String params = "&username="+smsInfo.getUserName()+"&password="+pw+"&smstype=0&mobile="+receiver+"&content="+contentEncoded;
	        	
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
						         httpURLConnection.getInputStream(), "GBK"));
		    	StringBuffer stringBuffer = new StringBuffer();
				int ch;
				while ((ch = bufferedReader.read()) > -1) {
					stringBuffer.append((char) ch);
				}
				bufferedReader.close();
				
				//将StringBuffer中的内容输出
				String xmlResult = StringUtils.trimToEmpty(stringBuffer.toString());
				if(xmlResult.startsWith("success")) {
					resultMap.put(receiver, "短信发送成功");
				} else if(xmlResult.startsWith("failure")) {
					int startIndex = xmlResult.indexOf("failure") + 7;
					String subResult = xmlResult.substring(startIndex);
					while(subResult.startsWith(";")) {
						subResult = subResult.substring(1);
					}
					resultMap.put(receiver, subResult);
				} else {
					resultMap.put(receiver, "未知错误,短信发送状态不明，请检查下发内容及号码等信息！");
				}
			} catch (MalformedURLException e) {
				e.printStackTrace();
				logger.error("jrh 客户使用企信通短信平台-慧亮代理的短信通道发送短信失败，可能是授权已过期-->"+e.getMessage(), e);
				resultMap.put(receiver, "可能是授权已过期");
			} catch (ProtocolException e) {
				e.printStackTrace();
				logger.error("jrh 客户使用企信通短信平台-慧亮代理的短信通道发送短信失败，可能是授权已过期-->"+e.getMessage(), e);
				resultMap.put(receiver, "可能是授权已过期");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				logger.error("jrh 客户使用企信通短信平台-慧亮代理的短信通道发送短信失败，可能是授权已过期-->"+e.getMessage(), e);
				resultMap.put(receiver, "可能是授权已过期");
			} catch (IOException e) {
				e.printStackTrace();
				logger.error("jrh 客户使用企信通短信平台-慧亮代理的短信通道发送短信失败，可能是授权已过期-->"+e.getMessage(), e);
				resultMap.put(receiver, "可能是授权已过期");
			}
		}
		
		return resultMap;
	}
	
}
