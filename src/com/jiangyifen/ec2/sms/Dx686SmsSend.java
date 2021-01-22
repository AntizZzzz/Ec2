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
 * 
 * @Description 描述：目前只能给中道公司用，因为其中有个“SpCode=000001 企业编号”字段，以后如果要给其他公司用，则往smsinfo 实体里加一个企业编号字段即可 
 * 
 * @author  lc
 */
public class Dx686SmsSend implements SmsSendIface {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public HashMap<String,String> sendSMS(SmsInfo smsInfo, List<String> toList, String content) {
		HashMap<String,String> resultMap = new HashMap<String,String>();
		
	 
		
		for(String receiver : toList) {
			try {
//				// 编辑短信接收人
//	        	String receivers = StringUtils.join(toList,'\r\n');

				// 建立指定url的Http连接
				String partUrl = SmsConfig.props
						.getProperty(SmsConfig.DX686_SMS_ROUTE);
				URL url = new URL(partUrl);
				 
				String contentEncoded= URLEncoder.encode(content, "GB2312");
			 
				String params = "zh="+smsInfo.getUserName()+"&mm="+smsInfo.getPassword()+"&hm="+receiver+"&nr="+contentEncoded+"&dxlbid=13";

				HttpURLConnection httpURLConnection = (HttpURLConnection) url
						.openConnection();
				httpURLConnection.setRequestMethod("GET");
				httpURLConnection.setDoOutput(true);

				// 取得输出流并写入请求参数
				OutputStream outputStream = httpURLConnection.getOutputStream();
				outputStream.write(params.getBytes("GB2312"));
				outputStream.flush();
				outputStream.close();

				// 读取返回的内容到StringBuffer
				BufferedReader bufferedReader = new BufferedReader(
						new InputStreamReader(httpURLConnection.getInputStream(),
								"GB2312"));
				StringBuffer stringBuffer = new StringBuffer();
				int ch;
				while ((ch = bufferedReader.read()) > -1) {
					stringBuffer.append((char) ch);
				}
				bufferedReader.close();

				// 将StringBuffer中的内容输出
				String xmlResult = StringUtils.trimToEmpty(stringBuffer.toString());

				logger.info("xmlResult: " + xmlResult);

//				// 取出指定坐标的字符
//				String resultNum = "-1";
//				try {
//					resultNum = xmlResult.substring(xmlResult.indexOf("=")+1, xmlResult.indexOf("&"));
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//				
//				String s = String.valueOf(resultNum);

				int i = Integer.parseInt(xmlResult);

				if (i == 0) {
					resultMap.put(receiver, "短信被服务器接纳并已进入队列或执行的操作成功");
				} else if (i == -1) {
					resultMap.put(receiver, "参数不全（某参数为空或参数数据类型不正确）");
				} else if (i == -2) {
					resultMap.put(receiver, "用户名或密码验证错误");
				} else if (i == -3) {
					resultMap.put(receiver, "发送短信余额不足（账户中必须有存款大于1条）");
				} else if (i == -4) {
					resultMap.put(receiver, "没有手机号码或手机号码超过100个");
				} else if (i == -5) {
					resultMap.put(receiver, "发送手机里含有错误号码");
				} else if (i == -6) {
					resultMap.put(receiver, "内容超长");
				} else if (i == -7) {
					resultMap.put(receiver, "短信中含有非法字符或非法词汇（内容含被过滤的关键字）");
				} else if (i == -8) {
					resultMap.put(receiver, "未开放HTTP接口");
				} else if (i == -9) {
					resultMap.put(receiver, "IP地址认证失败");
				} else if (i == -10) {
					resultMap.put(receiver, "发送时间限制");
				} else if (i == -11) {
					resultMap.put(receiver, "短信类别ID不存在或不正确");
				} else if (i == -12) {
					resultMap.put(receiver, "提交的短信条数不正确"); 
				} else {
					resultMap.put(receiver, "短信有可能没有发送成功，原因未知");
				}
			} catch (MalformedURLException e) {
				e.printStackTrace();
				logger.error(
						"lc 客户使用dx686短信平台-姜吉祥自找通道发送短信失败，可能是授权已过期-->" + e.getMessage(),
						e);
				resultMap.put(receiver, "发送失败，可能是授权已过期");
			} catch (ProtocolException e) {
				e.printStackTrace();
				logger.error(
						"lc 客户使用dx686短信平台-姜吉祥自找通道发送短信失败，可能是授权已过期-->" + e.getMessage(),
						e);
				resultMap.put(receiver, "发送失败，可能是授权已过期");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				logger.error(
						"lc 客户使用dx686短信平台-姜吉祥自找通道发送短信失败，可能是授权已过期-->" + e.getMessage(),
						e);
				resultMap.put(receiver, "发送失败，可能是授权已过期");
			} catch (IOException e) {
				e.printStackTrace();
				logger.error(
						"lc 客户使用dx686短信平台-姜吉祥自找通道发送短信失败，可能是授权已过期-->" + e.getMessage(),
						e);
				resultMap.put(receiver, "发送失败，可能是授权已过期");
			}
		}
		
		return resultMap;
	}

}
