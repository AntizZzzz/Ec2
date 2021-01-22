package com.jiangyifen.ec2.sms;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.entity.SmsInfo;
import com.jiangyifen.ec2.utils.SmsConfig;

/**
 * 信和汇金 定制短信对接接口
 *
 * 北京天信博易科技有限公司
 *
 * @author jinht
 *
 * @date 2015-7-22 上午9:20:53 
 *
 */
public class BjTianxinSmsSend implements SmsSendIface{

	// 日志工具
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@SuppressWarnings("static-access")
	@Override
	public HashMap<String, String> sendSMS(SmsInfo smsInfo, List<String> toList, String content) {

		HashMap<String, String> resultMap = new HashMap<String, String>();
		
		String partUrl = SmsConfig.props.getProperty(SmsConfig.BJTIANXIN_SMS_ROUTE);
		String contentParseGBK = "";
		try {
			contentParseGBK = URLEncoder.encode(content + "【"+smsInfo.getCompanyName()+"】", "gbk");
			contentParseGBK = URLEncoder.encode(contentParseGBK, "gbk");
		} catch (Exception e) {
			logger.error("jinht -->> 信和汇金发送短信时, 内容转换为 GBK 格式时出现异常! " + e.getMessage(), e);
		}
		
		for(String receiver : toList) {
			// 超速提交(一般为每秒提交一次)
			try {
				Thread.currentThread().sleep(1000L);
			} catch(InterruptedException e) {
				e.printStackTrace();
			}
			
			try {
				URL url = new URL(partUrl);
				String params = "cpName="+smsInfo.getUserName() + "&cpPwd=" + smsInfo.getPassword() + "&phones=" + receiver + "&msg=" + contentParseGBK;
				HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
				httpURLConnection.setRequestMethod("POST");
				httpURLConnection.setDoOutput(true);
				
				// 取得输出流并写入请求参数
				OutputStream outputStream = httpURLConnection.getOutputStream();
				outputStream.write(params.getBytes("UTF-8"));
				outputStream.flush();
				outputStream.close();
				
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream(), "gbk"));
				StringBuffer stringBuffer = new StringBuffer();
				int ch;
				while((ch = bufferedReader.read()) > -1) {
					stringBuffer.append((char) ch);
				}
				bufferedReader.close();
				
				// 将 StringBuffer 中的内容输出
				String sendResult = StringUtils.trimToEmpty(stringBuffer.toString());
				if("0".equals(sendResult)) {
					logger.info("jinht -->> 信和汇金短信发送结果 --> 发送成功!");
					resultMap.put(receiver, "短信发送结果: 发送成功!");
				} else {
					logger.info("jinht -->> 信和汇金短信发送结果 --> " + sendResult);
					resultMap.put(receiver, "短信发送结果: " + sendResult);
				}
				
			} catch (Exception e) {
				logger.error("jinht -->> 信和汇金短信发送出现异常! " + e.getMessage(), e);
			}
			
		}
		
		return resultMap;
	}

	public static void main(String[] args) {
		SmsInfo smsInfo = new SmsInfo();
		smsInfo.setUserName("xinhehuijin");
		smsInfo.setPassword("775225");
		smsInfo.setCompanyName("信和汇金");
		smsInfo.setDescription("信和汇金-短信对接");
		
		List<String> toList = new ArrayList<String>();
		toList.add("15824735464");
		new BjTianxinSmsSend().sendSMS(smsInfo, toList, "您好, 发送这条短信时为了进行测试, 看您有没有收到此信息, hello!");
	}
	
}
