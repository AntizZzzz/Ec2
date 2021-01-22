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

import com.jiangyifen.ec2.entity.SmsInfo;
import com.jiangyifen.ec2.utils.SmsConfig;

/**
 * IACS短信平台 【惠顺商贸自找通道】
 * 		因为短信通道返回： "0" 		--	短信发送成功
 *  	因为短信通道返回： "-1"		--	参数不全（某参数为空或参数数据类型不正确
 * 		因为短信通道返回："-2"		--	用户名或密码验证错误
 * 		因为短信通道返回："-3"		--	发送短信余额不足（账户中必须有存款大于1条）
 * 		因为短信通道返回："-4"		--  没有手机号码
 * 		因为短信通道返回："-5"		--	发送手机里含有错误号码
 * 		因为短信通道返回："-6"		--	短信内容超长
 * 		因为短信通道返回："-7"		--	短信中含有非法字符或非法词汇（内容含被过滤的关键字）
 * 		因为短信通道返回："-8"		--	未开放HTTP接口！
 * 		因为短信通道返回："-9"		--	IP地址认证失败
 * 		因为短信通道返回："-10"		--	发送时间限制
 * 		因为短信通道返回："-11"		--	短信类别ID不存在或不正确
 * 		因为短信通道返回："-12"		--	提交的短信条数不正确
 * 		因为短信通道返回    其他			--	短信发送状态不明，可能是短信中含有非法字符或非法词汇
 * 		因为短信通道返回： http请求是出现异常，可能是通道已经被供应商停掉了	--	"可能是授权已过期"
 * 
 * 不采用个短信通道供应商提供的短信群发功能，而是采用逐条发送的方式：
 * 	原因：如果采用短信群发方法，只发送一次请求，这样如果一次性给100个号码发送了短信，其中99条失败了，只有一条成功，
 * 	那返回的结果也是成功，这样在写发送短信日志的时候就会有错了
 * 
 * @author jrh
 *  2013-9-3
 */
public class IacsSmsSend implements SmsSendIface {
	// 日志工具
		private final Logger logger = LoggerFactory.getLogger(this.getClass());
		
		@Override
		public HashMap<String,String> sendSMS(SmsInfo smsInfo, List<String> toList, String content) {
			HashMap<String,String> resultMap = new HashMap<String,String>();
			
		 
			
			for(String receiver : toList) {
				try {
//					// 编辑短信接收人
//		        	String receivers = StringUtils.join(toList,'\r\n');
					
	        		// 加密参数
	        		String username = URLEncoder.encode(smsInfo.getUserName(), "GB2312");
	        		String pw = "";
	        		if(smsInfo.getPassword() != null) {
	        			pw = URLEncoder.encode(smsInfo.getPassword(), "GB2312");
	        		}
	        		receiver = URLEncoder.encode(receiver, "GB2312");

					
					//建立指定url的Http连接
					String partUrl = SmsConfig.props.getProperty(SmsConfig.IACS_SMS_ROUTE);
					URL url = new URL(partUrl);
					
					String contentEncoded = URLEncoder.encode(content, "UTF-8");
					String params = "zh="+username+"&mm="+pw+"&hm="+receiver+"&nr="+contentEncoded+"&dxlbid=15";

				    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
				    httpURLConnection.setRequestMethod("POST");
				    httpURLConnection.setDoOutput(true);
				    
				    //取得输出流并写入请求参数
				    OutputStream outputStream = httpURLConnection.getOutputStream();
				    outputStream.write(params.getBytes("GB2312"));
				    outputStream.flush();
				    outputStream.close();
				    
				    //读取返回的内容到StringBuffer
				    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(
							         httpURLConnection.getInputStream(), "GB2312"));
			    	StringBuffer stringBuffer = new StringBuffer();
					int ch;
					while ((ch = bufferedReader.read()) > -1) {
						stringBuffer.append((char) ch);
					}
					bufferedReader.close();
					
					//将StringBuffer中的内容输出
					String xmlResult = StringUtils.trimToEmpty(stringBuffer.toString());
					
					if("0".endsWith(xmlResult)) {
						resultMap.put(receiver, "短信发送成功");
					} else if("-1".endsWith(xmlResult)) {
						resultMap.put(receiver, "参数不全（某参数为空或参数数据类型不正确");
					} else if("-2".endsWith(xmlResult)) {
						resultMap.put(receiver, "用户名或密码验证错误");
					} else if("-3".endsWith(xmlResult)) {
						resultMap.put(receiver, "发送短信余额不足（账户中必须有存款大于1条）");
					} else if("-4".endsWith(xmlResult)) {
						resultMap.put(receiver, "没有手机号码,或存在错误号码");
					} else if("-5".endsWith(xmlResult)) {
						resultMap.put(receiver, "发送手机里含有错误号码");
					} else if("-6".endsWith(xmlResult)) {
						resultMap.put(receiver, "短信内容超长");
					} else if("-7".endsWith(xmlResult)) {
						resultMap.put(receiver, "短信中含有非法字符或非法词汇（内容含被过滤的关键字）");
					} else if("-8".endsWith(xmlResult)) {
						resultMap.put(receiver, "未开放HTTP接口");
					} else if("-9".endsWith(xmlResult)) {
						resultMap.put(receiver, "IP地址认证失败");
					} else if("-10".endsWith(xmlResult)) {
						resultMap.put(receiver, "发送时间限制");
					} else if("-11".endsWith(xmlResult)) {
						resultMap.put(receiver, "短信类别ID不存在或不正确");
					} else if("-12".endsWith(xmlResult)) {
						resultMap.put(receiver, "提交的短信条数不正确");
					} else {
						resultMap.put(receiver, "短信发送状态不明，可能是短信中含有非法字符或非法词汇");
					}
				} catch (MalformedURLException e) {
					e.printStackTrace();
					logger.error("jrh 客户使用IACS短信平台-惠顺商贸自找通道发送短信失败，可能是授权已过期-->"+e.getMessage(), e);
					resultMap.put(receiver, "发送失败，可能是授权已过期");
				} catch (ProtocolException e) {
					e.printStackTrace();
					logger.error("jrh 客户使用IACS短信平台-惠顺商贸自找通道发送短信失败，可能是授权已过期-->"+e.getMessage(), e);
					resultMap.put(receiver, "发送失败，可能是授权已过期");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
					logger.error("jrh 客户使用IACS短信平台-惠顺商贸自找通道发送短信失败，可能是授权已过期-->"+e.getMessage(), e);
					resultMap.put(receiver, "发送失败，可能是授权已过期");
				} catch (IOException e) {
					e.printStackTrace();
					logger.error("jrh 客户使用IACS短信平台-惠顺商贸自找通道发送短信失败，可能是授权已过期-->"+e.getMessage(), e);
					resultMap.put(receiver, "发送失败，可能是授权已过期");
				}
        	}
			
			return resultMap;
		}
		
}
