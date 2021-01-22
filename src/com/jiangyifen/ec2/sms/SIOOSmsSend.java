package com.jiangyifen.ec2.sms;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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

import com.jiangyifen.ec2.entity.SmsInfo;
import com.jiangyifen.ec2.utils.SmsConfig;

/**
 * 
 * @Description 描述：目前只能给中道公司用，因为其中有个“SpCode=000001 企业编号”字段，以后如果要给其他公司用，则往smsinfo 实体里加一个企业编号字段即可 
 * 
 * @author  lc
 */
public class SIOOSmsSend implements SmsSendIface {

	private final static Logger logger = LoggerFactory.getLogger(SIOOSmsSend.class);

	@Override
	public HashMap<String,String> sendSMS(SmsInfo smsInfo, List<String> toList, String content) {
		HashMap<String,String> resultMap = new HashMap<String,String>();
		String partUrl= "";
	 
		
		for(String receiver : toList) {
			try {

			 
				// 建立指定url的Http连接
				 partUrl = SmsConfig.props
						.getProperty(SmsConfig.SIOO_SMS_ROUTE);
				
			
				String contentEncoded = URLEncoder.encode(content, "utf-8");
				
				String params = "uid="+smsInfo.getUserName()+"&auth="+smsInfo.getPassword()+"&mobile="+receiver+"&msg="+contentEncoded+"&expid=0&encode=utf-8";
				URL url = new URL(partUrl+params);
				//打印日志
				logger.info(url.getPath());
				
				HttpURLConnection httpURLConnection = (HttpURLConnection) url
						.openConnection();
				httpURLConnection.setRequestMethod("GET");
			//	httpURLConnection.setDoOutput(true);

				// 读取返回的内容到StringBuffer
				BufferedReader bufferedReader = new BufferedReader(
						new InputStreamReader(httpURLConnection.getInputStream(),
								"UTF-8"));
				StringBuffer stringBuffer = new StringBuffer();
				int ch;
				while ((ch = bufferedReader.read()) > -1) {
					stringBuffer.append((char) ch);
				}
				bufferedReader.close();

			 
				String xmlResult = StringUtils.trimToEmpty(stringBuffer.toString()).substring(0,1);

				logger.info("xmlResult: " + xmlResult);


				int i = Integer.parseInt(xmlResult);

				if (i == 0) {
					resultMap.put(receiver, "操作成功");
				} else if (i == -1) {
					resultMap.put(receiver, "签权失败");
				} else if (i == -2) {
					resultMap.put(receiver, "未检索到被叫号码");
				} else if (i == -3) {
					resultMap.put(receiver, "被叫号码过多");
				} else if (i == -4) {
					resultMap.put(receiver, "内容未签名");
				} else if (i == -5) {
					resultMap.put(receiver, "内容过长");
				} else if (i == -6) {
					resultMap.put(receiver, "余额不足");
				} else if (i == -7) {
					resultMap.put(receiver, "暂停发送");
				} else if (i == -8) {
					resultMap.put(receiver, "保留");
				} else if (i == -9) {
					resultMap.put(receiver, "定时发送时间格式错误");
				} else if (i == -10) {
					resultMap.put(receiver, "下发内容为空");
				} else if (i == -11) {
					resultMap.put(receiver, "账户无效");
				} else if (i == -12) {
					resultMap.put(receiver, "Ip地址非法"); 
				}else if(i == -13){
					resultMap.put(receiver, "操作频率快");
				}else if(i == -14){
					resultMap.put(receiver, "操作失败");
				}else if(i == -15){
					resultMap.put(receiver, "拓展码无效(1-999)");
				} else {
					resultMap.put(receiver, "短信有可能没有发送成功，原因未知");
				}
			} catch (MalformedURLException e) {
				e.printStackTrace();
				logger.error(
						"lc 客户使用sioo短信平台发送短信失败，可能是授权已过期-->" + e.getMessage(),
						e);
				resultMap.put(receiver, "发送失败，可能是授权已过期");
			} catch (ProtocolException e) {
				e.printStackTrace();
				logger.error(
						"lc 客户使用sioo短信平台发送短信失败，可能是授权已过期-->" + e.getMessage(),
						e);
				resultMap.put(receiver, "发送失败，可能是授权已过期");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				logger.error(
						"lc 客户使用sioo短信平台发送短信失败，可能是授权已过期-->" + e.getMessage(),
						e);
				resultMap.put(receiver, "发送失败，可能是授权已过期");
			} catch (IOException e) {
				e.printStackTrace();
				logger.error(
						"lc 客户使用sioo短信平台发送短信失败-->" + e.getMessage(),
						e);
				resultMap.put(receiver, "发送失败，可能是授权已过期");
			}
		}
		
		return resultMap;
	}
	
////	public static void main(String[] args) throws Exception {
////		String str ="%E5%88%98%E8%B6%85";
//////		URLEncoder.encode(content, "GBK");
////		System.out.println(URLEncoder.encode("刘超","utf-8"));
////	}
//	
//	public static void main(String[] args) {
//		//通过调用URL进行推送
//		HttpURLConnection httpURLConnection=null;
//		try {
//			String content = "超刘 test";
//			content = URLEncoder.encode(content, "utf-8");
//			//建立指定url的Http连接
//			URL url = new URL("http://210.5.158.31/hy/?uid=20016&auth=8827d737029bc0950a90cf9ce5946ce9&mobile=15800756944&msg="+content+"&expid=0&encode=utf-8");
////			String params="uid=20016&auth=8827d737029bc0950a90cf9ce5946ce9&mobile=15021157421&msg=%E5%88%98%E8%B6%85&expid=0&encode=utf-8";
////System.out.println(params);
//			httpURLConnection=(HttpURLConnection) url.openConnection();
//			httpURLConnection.setRequestMethod("GET");
////			httpURLConnection.setDoOutput( true);
//			
//			//取得输出流并写入请求参数
////			OutputStream outputStream=httpURLConnection.getOutputStream();
////			outputStream.write(params.getBytes( "UTF-8"));
////			outputStream.flush();
////			outputStream.close();
//			
//			//读取返回的内容到StringBuffer
//			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(
//					httpURLConnection.getInputStream(), "UTF-8"));
//			StringBuffer stringBuffer = new StringBuffer();
//			int ch;
//			while ((ch = bufferedReader.read()) > -1) {
//				stringBuffer.append((char) ch);
//			}
//			bufferedReader.close();
//			String pushReturn=stringBuffer.toString();
//			logger.info("chb: Push return '"+pushReturn+"'");
//		} catch (Exception e) {
//			logger.warn("chb: Push failed",e);
//			try {
//				Thread.sleep(30*1000);
//			} catch (InterruptedException e1) {
//				//Do nothing
//			}
//		}finally{
//			if (httpURLConnection != null) 
//				httpURLConnection.disconnect(); 
//		}
//		
//	}
}
