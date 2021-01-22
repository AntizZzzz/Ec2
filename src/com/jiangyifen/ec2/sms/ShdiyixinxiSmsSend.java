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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.entity.SmsInfo;
import com.jiangyifen.ec2.utils.SmsConfig;

/**
 * 
 * ShdiyixinxiSmsSend 上海第翼信息科技有限公司
 * 
 * @Description 描述：上海第翼信息短信平台的短信发送通道【万米财富】
 *              	0	提交成功
 *              	1	含有敏感词汇
 *              	2	余额不足
 *              	3	没有号码
 *              	4	包含sql语句
 *              	10	账号不存在
 *              	11	账号注销
 *              	12	账号停用
 *              	13	IP鉴权失败
 *              	14	格式错误
 *              	-1	系统异常
 *              
 * 	接口中的登录名，可使用网页版注册时的登录名、手机号、QQ号。如登陆名是中文，请使用手机号，防止中文乱码。
 * 	接口密码请登陆网页版从 管理中心--基本资料中 复制。
 * 	如修改网页版的登录密码，接口密码也会随之变化，请及时修改。
 * 	通道支持时，平台可以post状态和回复短信给您，具体说明看接口说明；注意是post方式，不是get方式
 * 	提供2个小工具，供您测试使用，填写您的地址，点击推送可往您的地址上post数据，以便您调试程序。
 *
 * 示例：
 *	http://web.1xinxi.cn/asmx/smsservice.aspx?name=test&pwd=112345&content=testmsg&mobile=13566677777,18655555555&stime=2012-08-01 8:20:23&sign=testsign&type=pt&extno=123
 *
 * @author jinht
 *
 * @date 2015-5-12 下午1:18:41 
 *
 */
public class ShdiyixinxiSmsSend implements SmsSendIface {

	// 日志工具
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Override
	public HashMap<String, String> sendSMS(SmsInfo smsInfo, List<String> toList, String content) {
		
		HashMap<String, String> resultMap = new HashMap<String, String>();
		
		for(String receiver : toList) {
			
			try {
				// 建立指定 url 的 http 链接
				String partUrl = SmsConfig.props.getProperty(SmsConfig.SHDIYIXINXI_SMS_ROUTE);
				URL url = new URL(partUrl);
				
				// 内容格式转换为 UTF-8 的编码格式
				String contentEncoded = URLEncoder.encode(content, "UTF-8");
				StringBuffer sbParams = new StringBuffer();
				sbParams.append("name="+smsInfo.getUserName());						// 必填参数，用户账号
				sbParams.append("&pwd="+smsInfo.getPassword());						// 必填参数，WEB平台：基本资料中的接口密码
				sbParams.append("&content="+contentEncoded);						// 必填参数，发送内容(1-500个汉字)UTF-8编码
				sbParams.append("&mobile="+receiver);								// 必填参数。手机号码。多个以英文都好隔开
//				sbParams.append("&stime="+sdf.format(new Date()));					// 可选参数。发送时间，填写时已填写的时间发送，不填写时为当前时间发送
				sbParams.append("&sign="+smsInfo.getCompanyName());					// 可选参数，用户签名
				sbParams.append("&type=pt");										// 必填参数，固定值 pt
//				sbParams.append("&extno=123");										// 可选参数，扩展码，用户定义扩展吗，只能为数字
				
				HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
				httpURLConnection.setRequestMethod("POST");
				httpURLConnection.setDoOutput(true);

				// 取得输出流并写入请求参数
				OutputStream outputStream = httpURLConnection.getOutputStream();
				outputStream.write(sbParams.toString().getBytes("UTF-8"));
				outputStream.flush();
				outputStream.close();
				
				// 读取返回的内容到 StringBuffer
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream(), "UTF-8"));
				StringBuffer stringBuffer = new StringBuffer();
				int ch ;
				while((ch = bufferedReader.read()) > -1) {
					stringBuffer.append((char) ch);
				}
				bufferedReader.close();
				
				// 将 StringBuffered 的内容输出
				String[] args = stringBuffer.toString().split(",");

				if(args != null) {
					
					int i = 0;
					try {
						i = Integer.parseInt(args[0]);
					} catch (Exception e) {
						resultMap.put(receiver, "短信提供商回复错误，原因未知("+args[0]+")");
						e.printStackTrace();
						continue;
					}
					
					if(i==0) {
						resultMap.put(receiver, args[5] != null ? args[5] : "提交成功(0)");
					} else if(i == 1) {
						resultMap.put(receiver, args[1] != null ? args[1] : "含有敏感词汇(1)");
					} else if(i == 2) {
						resultMap.put(receiver, args[1] != null ? args[1] : "余额不足(2)");
					} else if(i == 3) {
						resultMap.put(receiver, args[1] != null ? args[1] : "没有号码(3)");
					} else if(i == 4) {
						resultMap.put(receiver, args[1] != null ? args[1] : "包含sql语句(4)");
					} else if(i == 10) {
						resultMap.put(receiver, args[1] != null ? args[1] : "账号不存在(10)");
					} else if(i == 11) {
						resultMap.put(receiver, args[1] != null ? args[1] : "账号注销(11)");
					} else if(i == 12) {
						resultMap.put(receiver, args[1] != null ? args[1] : "账号停用(12)");
					} else if(i == 13) {
						resultMap.put(receiver, args[1] != null ? args[1] : "IP鉴权失败(13)");
					} else if(i == 14) {
						resultMap.put(receiver, args[1] != null ? args[1] : "格式错误(14)");
					} else if(i == -1) {
						resultMap.put(receiver, args[1] != null ? args[1] : "系统异常(-1)");
					} else {
						resultMap.put(receiver, "短信有可能没有发送成功，原因未知("+i+")");
					}
					
				} else {
					resultMap.put(receiver, "短信有可能没有发送成功，原因未知");
				}
				
			} catch (Exception e) {
				logger.error("jinht -->> 发送短信时出现异常："+e.getMessage(), e);
			}
			
		}
		
		return resultMap;
	}
	
	public static void main(String[] args) {
		
		SmsInfo smsInfo = new SmsInfo();
		smsInfo.setCompanyName("万米财富");
		smsInfo.setDescription("");
		smsInfo.setUserName("万米金融远程事业部");
		smsInfo.setPassword("5516DA9B0B951C4310A7E0D14E9C");
		List<String> toList = new ArrayList<String>();
		toList.add("aa");
		
		System.out.println(new ShdiyixinxiSmsSend().sendSMS(smsInfo, toList, "hello, My I help you? 诶S诶【万福】"));
		
	}

}
