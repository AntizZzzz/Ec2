package com.jiangyifen.ec2.sms;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.entity.SmsInfo;
import com.jiangyifen.ec2.utils.SmsConfig;

/**
 * biztoAllSmsSend 短信平台的短信发送通道 
 *
 * @author chenhb
 * 
 */
public class BiztoAllSmsSend implements SmsSendIface {
	// 日志工具
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	public static void main(String[] args) {
		SmsInfo smsInfo=new SmsInfo();
		smsInfo.setCompanyName("汉天");
		smsInfo.setDescription("");
		smsInfo.setDomain(null);
		smsInfo.setId(null);
		smsInfo.setUserName("ronggu");
		smsInfo.setPassword("rg123!");
		smsInfo.setSmsChannelName("");
		List<String> toList=new ArrayList<String>();
		toList.add("15021157421");
		new BiztoAllSmsSend().sendSMS(smsInfo, toList, "Hello");
	}
	
	@Override
	public HashMap<String,String> sendSMS(SmsInfo smsInfo, List<String> toList, String content) {
		HashMap<String,String> resultMap = new HashMap<String,String>();
		
	 
		
		for(String receiver : toList) {
			try {
//				// 编辑短信接收人
//	        	String receivers = StringUtils.join(toList,'\r\n');
//				http://sms3.biztoall.net:8088/smshttp/infoSend?account=ronggu&password=rg123!&content=Hello&sendtime=20141110150000&phonelist=15888888888,15999999999&taskId=username_20111110123122_http_122121
				
				//建立指定url的Http连接
	        	String partUrl = SmsConfig.props.getProperty(SmsConfig.BIZTOALL_SMS_ROUTE);
	        	
	        	URL url = new URL(partUrl);

	        	//参数
	        	String account = smsInfo.getUserName();
	        	String password = smsInfo.getPassword();
	        	String sendtime=new SimpleDateFormat("yyyyMMddHHss").format(new Date());
	        	String taskId = smsInfo.getUserName()+"_" + new SimpleDateFormat("yyyyMMddHHss").format(new Date())+"_http_"+ Math.round((Math.random()) * 100000);
	        	//拼装		
			    String params = "account="+account+"&password="+password+"&content="+content+" 【"+smsInfo.getCompanyName()+"】"+"&sendtime="+sendtime+"&phonelist="+receiver+"&taskId="+taskId;
			    
logger.info("chenhb: params "+params);

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
						         httpURLConnection.getInputStream(), "GBK"));
		    	StringBuffer stringBuffer = new StringBuffer();
				int ch;
				while ((ch = bufferedReader.read()) > -1) {
					stringBuffer.append((char) ch);
				}
				bufferedReader.close();
				
				//将StringBuffer中的内容输出
				String resultString = StringUtils.trimToEmpty(stringBuffer.toString());
System.err.println("resultString:"+resultString );
				String[] resultArray = resultString.split(",");
				String resultCode="";
				String resultMsg="";
				if(resultArray.length<2){
					resultMap.put(receiver, "短信发送返回参数错误");
				}else{
					resultCode=resultArray[0];
					if(resultCode.equals("0")&&resultArray.length>=3){
						resultMsg+="发送成功，已发送"+resultArray[1]+"条短信，剩余"+resultArray[2]+"条短信";
System.err.println("resultMsg: "+resultMsg);
					}
				}
				
				if("-1".equals(resultCode)) {
					resultMap.put(receiver, "账号不存在:"+resultMsg);
				} else if("-2".equals(resultCode)) {
					resultMap.put(receiver, "密码错误:"+resultMsg);
				}else if("-3".equals(resultCode)) {
					resultMap.put(receiver, "账号被锁:"+resultMsg);
				}else if("-4".equals(resultCode)) {
					resultMap.put(receiver, "账号余额为0:"+resultMsg);
				}else if("-5".equals(resultCode)) {
					resultMap.put(receiver, "短信内容含有关键字:"+resultMsg);
				}else if("-6".equals(resultCode)) {
					resultMap.put(receiver, "标示号taskId有误:"+resultMsg);
				}else if("-7".equals(resultCode)) {
					resultMap.put(receiver, "短信内容前没加后缀:"+resultMsg);
				}else if("-8".equals(resultCode)) {
					resultMap.put(receiver, "提交的短信号码低于账号最低配置:"+resultMsg);
				}else if("-9".equals(resultCode)) {
					resultMap.put(receiver, "短信发送量超出当日规定数量:"+resultMsg);
				}else if("0".equals(resultCode)) {
					resultMap.put(receiver, "短信发送成功:"+resultMsg);
				}else if("1".equals(resultCode)) {
					resultMap.put(receiver, "其他错误:"+resultMsg);
				}else if("2".equals(resultCode)) {
					resultMap.put(receiver, "账号信息有误:"+resultMsg);
				}else if("3".equals(resultCode)) {
					resultMap.put(receiver, "短信内容或号码为空:"+resultMsg);
				}else if("4".equals(resultCode)) {
					resultMap.put(receiver, "账号余额不够发送当前短信:"+resultMsg);
				}else if("5".equals(resultCode)) {
					resultMap.put(receiver, "发送短信过程中出错:"+resultMsg);
				}else{
					resultMap.put(receiver, "短信通道未知返回结果:"+resultMsg);
				}
				
			} catch (Exception e) {
				e.printStackTrace();
				resultMap.put(receiver, "短信通道异常");
			}
		}
		
		return resultMap;
	}
}
