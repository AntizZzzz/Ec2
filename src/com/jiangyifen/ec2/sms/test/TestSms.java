package com.jiangyifen.ec2.sms.test;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import sun.misc.BASE64Encoder;

import com.jiangyifen.ec2.entity.SmsInfo;
import com.jiangyifen.ec2.sms.Dx10086SmsSend;
import com.jiangyifen.ec2.sms.ShsixunSmsSend;
import com.jiangyifen.ec2.sms.UnicomSmsSend;
import com.jiangyifen.ec2.utils.MD5Encrypt;
import com.jiangyifen.ec2.utils.SmsConfig;

public class TestSms {
	
	public static void main(String[] args) {
		
//		System.out.println(qxtongSms());
		
//		System.out.println(igooappSms());
		
//		System.out.println(iacsSms());
		 
//		System.out.println(unicomSmsSend());
		
//		System.out.println(dx10086SmsSend());
		
		System.out.println(shsixunSmsSend());
	}

	/**
	 * @Description 描述：自动生成插入数据库的SQL
	 *
	 * @author  JRH
	 * @date    2014年8月26日 上午11:34:42
	 * @param companyname		短信通道的公司名称，或者用于做为发送短信的签名使用
	 * @param description		描述信息
	 * @param password			密码
	 * @param smschannelname	通道名称
	 * @param username			账号
	 * @param domain_id 		租户编号
	 */
	private static void generateSmsSql(String companyname, String description, String password,
			String smschannelname, String username, String domain_id) {
		StringBuffer strBf = new StringBuffer();
		strBf.append("\r\n");
		strBf.append("SELECT last_value FROM seq_ec2_smsinfo_id;");
		strBf.append("\r\n");
		strBf.append("\r\n");
		strBf.append("INSERT INTO ec2_smsinfo (\"id\", \"companyname\", \"description\", \"password\", \"smschannelname\", \"username\", \"domain_id\") ");
		strBf.append(" VALUES ((SELECT nextval('seq_ec2_smsinfo_id')), '"+companyname+"', '"+description+"', '"+password+"', '"+smschannelname+"', '"+username+"', "+domain_id+");");
		strBf.append("\r\n");
		strBf.append("\r\n");
		System.out.println(strBf);
	}
	
	/**
	 * @Description 描述：思讯通短信接口测试
	 *
	 * @author  JRH
	 * @date    2014年8月18日 下午3:35:40
	 * @return String
	 */
	private static String shsixunSmsSend() { 
		String companyname = "银来财富";	// 这里是短息的公司签名，必须跟思讯通要求的签名一致，而不能简单的写成公司名字，不然无法发送成功
		String description = "上海梵志网络科技有限公司，注意：companyname的值为短息的公司签名，必须跟思讯通要求的签名一致，而不能简单的写成公司名字，不然无法发送成功";
		String password = "chuandao";
		String smschannelname = "shsixunSmsSend";
		String username = "yinlai";
		String domain_id = "1";
//		String companyname = "桂银投资";	// 这里是短息的公司签名，必须跟思讯通要求的签名一致，而不能简单的写成公司名字，不然无法发送成功
//		String description = "上海梵志网络科技有限公司，注意：companyname的值为短息的公司签名，必须跟思讯通要求的签名一致，而不能简单的写成公司名字，不然无法发送成功";
//		String password = "chuandao";
//		String smschannelname = "shsixunSmsSend";
//		String username = "shguiyin";
//		String domain_id = "1";
		
		generateSmsSql(companyname, description, password, smschannelname, username, domain_id);
		
		boolean issendMsg = false;
		if(issendMsg) {
			SmsInfo smsInfo = new SmsInfo();
			smsInfo.setUserName("shguiyin");
			smsInfo.setPassword("chuandao"); // userid = 8280 
			smsInfo.setCompanyName("桂银投资");
			
			List<String> toList = new ArrayList<String>();
			toList.add("13816760398");
//		toList.add("15861692130");
//		toList.add("15601796063");
			
			String content = "JRH Test 短信。》》"+System.currentTimeMillis()+"签名：";
			
			ShsixunSmsSend ssx = new ShsixunSmsSend();
			HashMap<String, String> result = ssx.sendSMS(smsInfo, toList, content);
			String r = StringUtils.join(result.values(), ",");
			return r;
		}
		
		return "";
	}


	@SuppressWarnings("unused")
	private static String dx10086SmsSend() {
		SmsInfo smsInfo = new SmsInfo();
		smsInfo.setUserName("m1002");
		smsInfo.setPassword("456987"); // userid = 8227
		smsInfo.setSmsChannelName("dx10086SmsSend");
		
		smsInfo.setUserName("FZ201405");
		smsInfo.setPassword("LPW2643016"); // userid = 8280
		smsInfo.setSmsChannelName("dx10086SmsSend");
		
		List<String> toList = new ArrayList<String>();
		toList.add("13816760398");
		toList.add("15601796063");
		
		String content = "Test 测ceshi试短信：谢谢您的来电";
		
		Dx10086SmsSend uss = new Dx10086SmsSend();
		HashMap<String, String> result = uss.sendSMS(smsInfo, toList, content);
		String r = StringUtils.join(result.values(), ",");
		return r;
	}
	
	@SuppressWarnings("unused")
	private static String unicomSmsSend() {
		SmsInfo smsInfo = new SmsInfo();
		smsInfo.setUserName("admin");
		smsInfo.setPassword("DjqEm8");
		smsInfo.setSmsChannelName("unicomSmsSend");
		
		List<String> toList = new ArrayList<String>();
		toList.add("13816760398");
		
		String content = "谢谢您的来电";
		
		UnicomSmsSend uss = new UnicomSmsSend();
		HashMap<String, String> result = uss.sendSMS(smsInfo, toList, content);
		String r = StringUtils.join(result.values(), ",");
		return r;
	}


	@SuppressWarnings("unused")
	private static String iacsSms() {
		List<String> toList = new ArrayList<String>();
		toList.add("13816760398");
//		toList.add("60398");
//		toList.add("15861692130");
    	String receivers = "";
    	for(String receiver : toList) {
    		receivers += receiver + ","; 
    	}
    	while(receivers.endsWith(",")) {
    		receivers = receivers.substring(0, receivers.lastIndexOf(","));
    	}

		try {
			Long startTime = System. currentTimeMillis();

			String[] parms = new String[] {"账户","000000","13816760398","研发","15"};
//			String[] parms = new String[] {"惠顺商贸","000000","13816760398","研发","15"};
			parms[2] = receivers;
			
			for(int i = 0; i< parms.length; i++) {
				parms[i] = URLEncoder.encode(parms[i], "GB2312");
			}

//			String content = "jrh研发：一眨眼就到了今天的这个时候了。";
//			content = URLEncoder.encode(content, "GBK");
        	
			//建立指定url的Http连接
//        	String partUrl = SmsConfig.props.getProperty(SmsConfig.IGOOAPP_SMS_ROUTE);
        	String partUrl = "http://223.4.97.247/smsComputer/smsComputersend.asp?";
        	String params = "zh="+parms[0]+"&mm="+parms[1]+"&hm="+parms[2]+"&nr="+parms[3]+"&dxlbid="+parms[4];
        	
        	System.out.println();
        	System.out.println(partUrl+params);
        	System.out.println();
        	System.out.println();
        	
		    URL url = new URL(partUrl+params);
		    
		    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
		    httpURLConnection.setRequestMethod("POST");
		    httpURLConnection.setDoOutput(true);
		    
		    //取得输出流并写入请求参数
		    OutputStream outputStream = httpURLConnection.getOutputStream();
//		    outputStream.write(params.getBytes("GB2312"));
		    outputStream.flush();
		    outputStream.close();
		    
		    //读取返回的内容到StringBuffer
		    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(
					         httpURLConnection.getInputStream(), "GB2312"));
	    	StringBuffer stringBuffer = new StringBuffer();
			int ch;
			while ((ch = bufferedReader.read()) > -1) {
				stringBuffer.append((char) ch);
				System.out.println((char)ch);
			}
			bufferedReader.close();
			
			
			System.out.println(stringBuffer.toString());
			
			
			//将StringBuffer中的内容输出
			String xmlResult = StringUtils.trimToEmpty(stringBuffer.toString());
			
			Long endTime = System. currentTimeMillis();
			System. out.println( "发送 短信 耗时：" + (endTime - startTime));
			
			System.out.println("xmlResult---------->  "+xmlResult);
			
			if("0".endsWith(xmlResult)) {
				return "短信发送成功";
			} else if("-1".endsWith(xmlResult)) {
				return "参数不全（某参数为空或参数数据类型不正确";
			} else if("-2".endsWith(xmlResult)) {
				return "用户名或密码验证错误";
			} else if("-3".endsWith(xmlResult)) {
				return "发送短信余额不足（账户中必须有存款大于1条）";
			} else if("-4".endsWith(xmlResult)) {
				return "没有手机号码";
			} else if("-5".endsWith(xmlResult)) {
				return "发送手机里含有错误号码";
			} else if("-6".endsWith(xmlResult)) {
				return "短信内容超长";
			} else if("-7".endsWith(xmlResult)) {
				return "短信中含有非法字符或非法词汇（内容含被过滤的关键字）";
			} else if("-8".endsWith(xmlResult)) {
				return "未开放HTTP接口";
			} else if("-9".endsWith(xmlResult)) {
				return "IP地址认证失败";
			} else if("-10".endsWith(xmlResult)) {
				return "发送时间限制";
			} else if("-11".endsWith(xmlResult)) {
				return "短信类别ID不存在或不正确";
			} else if("-12".endsWith(xmlResult)) {
				return "提交的短信条数不正确";
			} else {
				return "短信发送状态不明，可能是短信中含有非法字符或非法词汇";
			}

		} catch (MalformedURLException e) {
			e.printStackTrace();
			return "可能是授权已过期";
		} catch (ProtocolException e) {
			e.printStackTrace();
			return "可能是授权已过期";
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return "可能是授权已过期";
		} catch (IOException e) {
			e.printStackTrace();
			return "可能是授权已过期";
		}
	}


	/**
	 * 企信通短信通道
	 * @return
	 */
	public static String qxtongSms() {
		List<String> toList = new ArrayList<String>();
//		toList.add("13816760398");
		toList.add("60398");
//		toList.add("15861692130");
    	String receivers = "";
    	for(String receiver : toList) {
    		receivers += receiver + ","; 
    	}
    	while(receivers.endsWith(",")) {
    		receivers = receivers.substring(0, receivers.lastIndexOf(","));
    	}

		try {
			Long startTime = System. currentTimeMillis();

			String content = "jrh 研发：一眨眼就到了今天的这个时候了。";
			content = URLEncoder.encode(content, "GBK");
        	
			//建立指定url的Http连接
//        	String partUrl = SmsConfig.props.getProperty(SmsConfig.IGOOAPP_SMS_ROUTE);
        	String partUrl = "http://139.159.32.10:8888/servlet/UserServiceAPI?method=sendSMS&isLongSms=1";
        	String pw = new BASE64Encoder().encode("888888".getBytes());
        	System.out.println(pw);
        	String params = "&username=agenthuiliang&password="+pw+"&smstype=10&mobile="+receivers+"&content="+content;
        	
        	
        	System.out.println(partUrl+params);
        	System.out.println();
        	System.out.println();
        	System.out.println();
        	
		    URL url = new URL(partUrl);
		    
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
			
			Long endTime = System. currentTimeMillis();
			System. out.println( "发送 短信 耗时：" + (endTime - startTime));
			
			System.out.println("xmlResult---------->  "+xmlResult);
			
			if(xmlResult.startsWith("success")) {
				return "短信发送成功";
			} else if(xmlResult.startsWith("failure")) {
				int startIndex = xmlResult.indexOf("failure") + 7;
				String subResult = xmlResult.substring(startIndex);
				while(subResult.startsWith(";")) {
					subResult = subResult.substring(1);
				}
				return subResult;
			} else {
				return "短信发送状态不明！";
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return "可能是授权已过期";
		} catch (ProtocolException e) {
			e.printStackTrace();
			return "可能是授权已过期";
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return "可能是授权已过期";
		} catch (IOException e) {
			e.printStackTrace();
			return "可能是授权已过期";
		}
	}
	
	/**
	 * 测试爱谷果短信通道
	 * @return
	 */
	public static String igooappSms() {
		List<String> toList = new ArrayList<String>();
		toList.add("13816760398");
//		toList.add("60398");
//		toList.add("15861692130");
    	String receivers = "";
    	for(String receiver : toList) {
    		receivers += receiver + ","; 
    	}
    	while(receivers.endsWith(",")) {
    		receivers = receivers.substring(0, receivers.lastIndexOf(","));
    	}

		try {
			Long startTime = System. currentTimeMillis();

			String content = "一眨眼就到了今天的这个时候了。";
			content = URLEncoder.encode(content, "UTF-8");
        	
			//建立指定url的Http连接
        	String partUrl = SmsConfig.props.getProperty(SmsConfig.IGOOAPP_SMS_ROUTE);
//        	String partUrl = "http://61.152.145.23:8080/http/SendSms";
        	String pw = MD5Encrypt.getMD5Str("88138805", "UTF-8");
//        	String pw = MD5Encrypt.getMD5Str("12212", "UTF-8");
        	String params = "Account=88138805&Password="+pw+"&Phone="+receivers+"&Content="+content+"&SubCode=&SendTime=";
        	
        	System.out.println(partUrl+params);
        	System.out.println();
        	System.out.println();
        	System.out.println();
        	
		    URL url = new URL(partUrl);
		    
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
			String xmlResult = StringUtils.trimToEmpty(stringBuffer.toString());
			
			Long endTime = System. currentTimeMillis();
			System. out.println( "发送 短信 耗时：" + (endTime - startTime));
			
			System.out.println(xmlResult);
			
			// 解析发送结果 
			int startIndex = xmlResult.indexOf("<response>") + 10;
			int endIndex = xmlResult.indexOf("</response>");
			String responseStr = xmlResult.substring(startIndex, endIndex);
			int resultNum = Integer.parseInt(responseStr);
			if(resultNum > 0) {
				return "成功发送 " +resultNum+ "条短信！";
			} else if(resultNum == -1) {
				return "账号不存在，请检查用户名或密码是否正确！";
			} else if(resultNum == -2) {
				return "账户余额不足！";
			} else if(resultNum == -3) {
				return "账号已被禁用！";
			} else if(resultNum == -4) {
				return "ip 鉴权失败！";
			} else if(resultNum == -8) {
				return "缺少请求参数或参数不正确(请检查下发内容是否为空，下发号码是否大于100个等)！";
			} else if(resultNum == -9) {
				return "短信内容不合法，请修正后重试！";
			} else if(resultNum == -10) {
				return "账户当日发送短信已经超过允许的每日最大发送量！";
			} else if(resultNum == -11) {
				return "用户接入方式错误！";
			} else if(resultNum == -12) {
				return "号码个数小于最小限制！";
			}
			return "短信发送失败，请检查下发内容及号码等信息！";
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return "可能是授权已过期";
		} catch (ProtocolException e) {
			e.printStackTrace();
			return "可能是授权已过期";
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return "可能是授权已过期";
		} catch (IOException e) {
			e.printStackTrace();
			return "可能是授权已过期";
		}
	}
	
}
