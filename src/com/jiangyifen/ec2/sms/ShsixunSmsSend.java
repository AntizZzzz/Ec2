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
 * @Description 描述： 上海思讯通短信平台的短信发送通道
 * 
 * 		 成功				因为短信通道返回："1,msgid";   
 *  	失败原因			因为短信通道返回： "0,失败原因";
 * 		"可能是授权已过期"; 	因为短信通道返回： http请求是出现异常，可能是通道已经被供应商停掉了
 *
 * @author  JRH
 * @date    2014年8月18日 下午3:00:57
 */
public class ShsixunSmsSend implements SmsSendIface {
	// 日志工具
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Override
	public HashMap<String,String> sendSMS(SmsInfo smsInfo, List<String> toList, String content) {
		HashMap<String,String> resultMap = new HashMap<String,String>();
		
		for(String receiver : toList) {
			try {
				String companyName = smsInfo.getCompanyName();
				String newContent = content;
				if(companyName != null && !"".equals(companyName)) {	// 思讯通的短信通道必须要有签名才能发送成功
					newContent += "【"+smsInfo.getCompanyName()+"】";
				}
				
				//建立指定url的Http连接
	        	String partUrl = SmsConfig.props.getProperty(SmsConfig.SHSIXUN_SMS_ROUTE);
	        	URL url = new URL(partUrl);
			    String params = "method=SendSms&username="+smsInfo.getUserName()+"&password="+smsInfo.getPassword()+"&phonelist="+receiver+"&msg="+newContent+"&SendDatetime=";
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
				logger.info("JRH 思讯通短信发送结果（sixuntong sms send result）——>"+result);
				
				String[] rsArr = result.split(",");
				int arrLen = rsArr.length;
				String rsCode = "";
				String rsMsg= "";
				if(arrLen > 0) {
					rsCode = StringUtils.trimToEmpty(rsArr[0]);
				}
				
				if(arrLen > 1) {
					rsMsg = StringUtils.trimToEmpty(rsArr[1]);
				}
				
				if("1".equals(rsCode)) {
					resultMap.put(receiver, "发送成功");
				} else if("0".equals(rsCode)) {
					resultMap.put(receiver, rsMsg);
				} else {
					resultMap.put(receiver, "未知错误,短信发送状态不明，请检查下发内容及号码等信息！");
				}
			} catch (MalformedURLException e) {
				e.printStackTrace();
				logger.error("jrh 客户使用思讯通短信平台-发送短信失败，可能是授权已过期-->"+e.getMessage(), e);
				resultMap.put(receiver, "可能是授权已过期");
			} catch (ProtocolException e) {
				e.printStackTrace();
				logger.error("jrh 客户使用思讯通短信平台-发送短信失败，可能是授权已过期-->"+e.getMessage(), e);
				resultMap.put(receiver, "可能是授权已过期");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				logger.error("jrh 客户使用思讯通短信平台-发送短信失败，可能是授权已过期-->"+e.getMessage(), e);
				resultMap.put(receiver, "可能是授权已过期");
			} catch (IOException e) {
				e.printStackTrace();
				logger.error("jrh 客户使用思讯通短信平台-发送短信失败，可能是授权已过期-->"+e.getMessage(), e);
				resultMap.put(receiver, "可能是授权已过期");
			}
		}
		
		return resultMap;
	}
	
}
