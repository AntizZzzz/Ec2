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
import com.jiangyifen.ec2.utils.MD5Encrypt;
import com.jiangyifen.ec2.utils.SmsConfig;

/**
 * 爱谷果传媒短信平台的短信发送通道 【慧亮代理】
 * 		因为短信通道返回： >0 成功条数	--	成功发送多少条！  
 *  	因为短信通道返回： "-1"		--	账号不存在，请检查用户名或密码是否正确！
 * 		因为短信通道返回："-2"		--	账户余额不足！
 * 		因为短信通道返回："-3"		--	账号已被禁用！
 * 		因为短信通道返回："-4"		--	ip 鉴权失败！
 * 		因为短信通道返回："-8"		--	缺少请求参数或参数不正确(请检查下发内容是否为空，下发号码是否大于100个等)！
 * 		因为短信通道返回："-9"		--	短信内容不合法，请修正后重试！
 * 		因为短信通道返回："-10"		--	账户当日发送短信已经超过允许的每日最大发送量！
 * 		因为短信通道返回："-11"		--	用户接入方式错误！
 * 		因为短信通道返回："-12"		--	号码个数小于最小限制！
 * 		因为短信通道返回："-99"或者其他	--	短信发送失败，请检查下发内容及号码等信息！
 * 		因为短信通道返回： http请求是出现异常，可能是通道已经被供应商停掉了	--	"可能是授权已过期"
 * 
 * 不采用个短信通道供应商提供的短信群发功能，而是采用逐条发送的方式：
 * 	原因：如果采用短信群发方法，只发送一次请求，这样如果一次性给100个号码发送了短信，其中99条失败了，只有一条成功，
 * 	那返回的结果也是成功，这样在写发送短信日志的时候就会有错了
 * 
 * @author jrh
 *
 */
public class IgooappSmsSend implements SmsSendIface {
	// 日志工具
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Override
	public HashMap<String,String> sendSMS(SmsInfo smsInfo, List<String> toList, String content) {
		HashMap<String,String> resultMap = new HashMap<String,String>();
		
 
		
		for(String receiver : toList) {
			try {
				//建立指定url的Http连接
	        	String partUrl = SmsConfig.props.getProperty(SmsConfig.IGOOAPP_SMS_ROUTE);
	        	URL url = new URL(partUrl);
		    	String	contentEncoded = URLEncoder.encode(content, "UTF-8");
				String pw = "";
				if(smsInfo.getPassword() != null) {
					pw = MD5Encrypt.getMD5Str(smsInfo.getPassword(), "UTF-8");
				}
			    String params = "Account="+smsInfo.getUserName()+"&Password="+pw+"&Phone="+receiver+"&Content="+contentEncoded+"&SubCode=&SendTime=";

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
				
				// 解析发送结果 
				int startIndex = xmlResult.indexOf("<response>") + 10;
				int endIndex = xmlResult.indexOf("</response>");
				String responseStr = xmlResult.substring(startIndex, endIndex);
				int resultNum = Integer.parseInt(responseStr);
				
				if(resultNum > 0) {
					resultMap.put(receiver, "成功发送 " +resultNum+ "条短信！");
				} else if(resultNum == -1) {
					resultMap.put(receiver, "账号不存在，请检查用户名或密码是否正确！");
				} else if(resultNum == -2) {
					resultMap.put(receiver, "账户余额不足！");
				} else if(resultNum == -3) {
					resultMap.put(receiver, "账号已被禁用！");
				} else if(resultNum == -4) {
					resultMap.put(receiver, "ip 鉴权失败！");
				} else if(resultNum == -8) {
					resultMap.put(receiver, "缺少请求参数或参数不正确(请检查下发内容是否为空，下发号码是否大于100个等)！");
				} else if(resultNum == -9) {
					resultMap.put(receiver, "短信内容不合法，请修正后重试！");
				} else if(resultNum == -10) {
					resultMap.put(receiver, "账户当日发送短信已经超过允许的每日最大发送量！");
				} else if(resultNum == -11) {
					resultMap.put(receiver, "用户接入方式错误！");
				} else if(resultNum == -12) {
					resultMap.put(receiver, "号码个数小于最小限制！");
				}
				resultMap.put(receiver, "未知错误,短信发送状态不明，请检查下发内容及号码等信息！");
			} catch (MalformedURLException e) {
				e.printStackTrace();
				logger.error("jrh 客户使用爱谷果传媒短信平台-慧亮代理的短信通道发送短信失败，可能是授权已过期-->"+e.getMessage(), e);
				resultMap.put(receiver, "可能是授权已过期");
			} catch (ProtocolException e) {
				e.printStackTrace();
				logger.error("jrh 客户使用爱谷果传媒短信平台-慧亮代理的短信通道发送短信失败，可能是授权已过期-->"+e.getMessage(), e);
				resultMap.put(receiver, "可能是授权已过期");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				logger.error("jrh 客户使用爱谷果传媒短信平台-慧亮代理的短信通道发送短信失败，可能是授权已过期-->"+e.getMessage(), e);
				resultMap.put(receiver, "可能是授权已过期");
			} catch (IOException e) {
				e.printStackTrace();
				logger.error("jrh 客户使用爱谷果传媒短信平台-慧亮代理的短信通道发送短信失败，可能是授权已过期-->"+e.getMessage(), e);
				resultMap.put(receiver, "可能是授权已过期");
			}
		}

		return resultMap;
	}

/**
 * 可以支持短信供应商提供的短信群发功能，各号码之间用逗号隔开
 */
//	@Override
//	public String sendSMS(SmsInfo smsInfo, List<String> toList, String content) {
//		
//		try {
//			// 编辑短信接收人
//			String receivers = StringUtils.join(toList,'\r\n');
//			
//			//建立指定url的Http连接
//			String partUrl = SmsConfig.props.getProperty(SmsConfig.IGOOAPP_SMS_ROUTE);
//			URL url = new URL(partUrl);
//			
//			String pw = "";
//			if(smsInfo.getPassword() != null) {
//				pw = MD5Encrypt.getMD5Str(smsInfo.getPassword(), "UTF-8");
//			}
//			content = URLEncoder.encode(content, "UTF-8");
//			String params = "Account="+smsInfo.getUserName()+"&Password="+pw+"&Phone="+receivers+"&Content="+content+"&SubCode=&SendTime=";
//			
//			HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
//			httpURLConnection.setRequestMethod("POST");
//			httpURLConnection.setDoOutput(true);
//			
//			//取得输出流并写入请求参数
//			OutputStream outputStream = httpURLConnection.getOutputStream();
//			outputStream.write(params.getBytes("UTF-8"));
//			outputStream.flush();
//			outputStream.close();
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
//			
//			//将StringBuffer中的内容输出
//			String xmlResult = StringUtils.trimToEmpty(stringBuffer.toString());
//			
//			// 解析发送结果 
//			int startIndex = xmlResult.indexOf("<response>") + 10;
//			int endIndex = xmlResult.indexOf("</response>");
//			String responseStr = xmlResult.substring(startIndex, endIndex);
//			int resultNum = Integer.parseInt(responseStr);
//			
//			if(resultNum > 0) {
//				return "成功发送 " +resultNum+ "条短信！";
//			} else if(resultNum == -1) {
//				return "账号不存在，请检查用户名或密码是否正确！";
//			} else if(resultNum == -2) {
//				return "账户余额不足！";
//			} else if(resultNum == -3) {
//				return "账号已被禁用！";
//			} else if(resultNum == -4) {
//				return "ip 鉴权失败！";
//			} else if(resultNum == -8) {
//				return "缺少请求参数或参数不正确(请检查下发内容是否为空，下发号码是否大于100个等)！";
//			} else if(resultNum == -9) {
//				return "短信内容不合法，请修正后重试！";
//			} else if(resultNum == -10) {
//				return "账户当日发送短信已经超过允许的每日最大发送量！";
//			} else if(resultNum == -11) {
//				return "用户接入方式错误！";
//			} else if(resultNum == -12) {
//				return "号码个数小于最小限制！";
//			}
//			return "未知错误,短信发送状态不明，请检查下发内容及号码等信息！";
//		} catch (MalformedURLException e) {
//			e.printStackTrace();
//			logger.error("jrh 客户使用爱谷果传媒短信平台-慧亮代理的短信通道发送短信失败，可能是授权已过期-->"+e.getMessage(), e);
//			return "可能是授权已过期";
//		} catch (ProtocolException e) {
//			e.printStackTrace();
//			logger.error("jrh 客户使用爱谷果传媒短信平台-慧亮代理的短信通道发送短信失败，可能是授权已过期-->"+e.getMessage(), e);
//			return "可能是授权已过期";
//		} catch (UnsupportedEncodingException e) {
//			e.printStackTrace();
//			logger.error("jrh 客户使用爱谷果传媒短信平台-慧亮代理的短信通道发送短信失败，可能是授权已过期-->"+e.getMessage(), e);
//			return "可能是授权已过期";
//		} catch (IOException e) {
//			e.printStackTrace();
//			logger.error("jrh 客户使用爱谷果传媒短信平台-慧亮代理的短信通道发送短信失败，可能是授权已过期-->"+e.getMessage(), e);
//			return "可能是授权已过期";
//		}
//		
//	}
	
}
