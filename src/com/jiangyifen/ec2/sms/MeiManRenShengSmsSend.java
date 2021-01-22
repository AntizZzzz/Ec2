package com.jiangyifen.ec2.sms;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.entity.SmsInfo;
import com.jiangyifen.ec2.utils.SmsConfig;

public class MeiManRenShengSmsSend implements SmsSendIface {
	/*
	 * 1、配置文件propertiefiles/sms.properties 
	 * 
	 * 2、配置文件applicationContextSms.xml中添加 <!-- meiManRenShengSmsSend 短信通道 --> <bean
	 * id="meiManRenShengSmsSend"
	 * class="com.jiangyifen.ec2.sms.ShjianzhouSmsSend"></bean>
	 * 
	 * 3、数据库插入sql insert into ec2_smsinfo(id,companyname,username,password,smschannelname,domain) values(1,'美满人生','mmrs-181818','KN0E0bzO','meiManRenShengSmsSend',1);
	 * 
	 * 4、完成短信业务逻辑 MeiManRenShengSmsSend 这个业务类
	 */

	// 日志工具
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	public static void main(String[] args) {
		SmsInfo smsInfo = new SmsInfo();
		smsInfo.setCompanyName("美满人生");
		smsInfo.setDescription("");
		// smsInfo.setDomain(null);
		// smsInfo.setId(null);
		/*smsInfo.setUserName("mmrs-181818");
		smsInfo.setPassword("KN0E0bzO");*/
		smsInfo.setUserName("mmrs-181818");
		smsInfo.setPassword("Hyy123456");
		// smsInfo.setSmsChannelName("");
		List<String> toList = new ArrayList<String>();
		toList.add("18017058409");
		new MeiManRenShengSmsSend().sendSMS(smsInfo, toList, "亲爱的会员，您好，冬天天气干燥，请注意多多喝水，注意上火【美满人生】");
	}

	@SuppressWarnings("static-access")
	@Override
	public HashMap<String, String> sendSMS(SmsInfo smsInfo, List<String> toList, String content) {
		HashMap<String, String> resultMap = new HashMap<String, String>();

		for (String receiver : toList) {
			// 超速提交(一般为每秒一次提交)
			try {
				Thread.currentThread().sleep(1000L);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}

			try {
				// 建立指定url的Http连接
				String partUrl = SmsConfig.props.getProperty(SmsConfig.MEIMANRENSHENG_SMS_ROUTE);
				URL url = new URL(partUrl);
				String [] usernameAndId=smsInfo.getUserName().split("-");
				String username = usernameAndId[0];
				String productid=usernameAndId[1];
				String password = smsInfo.getPassword();
				String params = "username=" + username + "&password=" + password + "&mobile=" + receiver + "&content=" + content+"&dstime="+"&productid="+productid+"&xh=";
				HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
				httpURLConnection.setRequestMethod("POST");
				httpURLConnection.setDoOutput(true);

				// 取得输出流并写入请求参数
				OutputStream outputStream = httpURLConnection.getOutputStream();
				outputStream.write(params.getBytes("UTF-8"));
				outputStream.flush();
				outputStream.close();

				// 读取返回的内容到StringBuffer
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream(), "UTF-8"));
				StringBuffer stringBuffer = new StringBuffer();
				int ch;
				while ((ch = bufferedReader.read()) > -1) {
					stringBuffer.append((char) ch);
				}
				bufferedReader.close();

				// 将StringBuffer中的内容输出
				String sendResult = StringUtils.trimToEmpty(stringBuffer.toString());
				logger.info("XKP 美满人生短信发送结果（meimanrensheng sms send result）——>"+sendResult);
				String [] sendResults=sendResult.split(",");
				// 结果解析
				
				/*-1	用户名或者密码不正确或用户禁用
				1,xxxxxxxx	1代表发送短信成功,xxxxxxxx代表消息编号
				0,xxxxxxxx	0发送短信失败,xxxxxxxx代表消息编号
				2	余额不够或扣费错误
				3	扣费失败异常（请联系客服）
				5,xxxxxxxx	短信定时成功, xxxxxxxx代表消息编号
				6	有效号码为空
				7	短信内容为空
				8	无签名，必须，格式：【签名】
				9	没有Url提交权限
				10	发送号码过多,最多支持200个号码
				11	产品ID异常或产品禁用
				12	参数异常
				13	30分种重复提交
				14	用户名或密码不正确，产品余额为0，禁止提交，联系客服
				15	Ip验证失败
				19	短信内容过长，最多支持500个
				20	定时时间不正确：格式：20130202120212(14位数字)*/
				int i = 0;
				try {
					i = Integer.parseInt(sendResults[0]);
				} catch (Exception e) {
					resultMap.put(receiver, "短信提供商回复错误，原因未知(" + sendResult + ")");
					e.printStackTrace();
					continue;
				}

				if (i == 1) {
					resultMap.put(receiver, "短信发送成功(" + i + ")");
				} else if (i == 2) {
					resultMap.put(receiver, "余额不够或扣费错误(2)");
				} else if (i == 3) {
					resultMap.put(receiver, "扣费失败异常（请联系客服）(3)");
				} else if (i == 5) {
					resultMap.put(receiver, "短信定时成功(5)");
				} else if (i == 6) {
					resultMap.put(receiver, "有效号码为空(6)");
				} else if (i == 7) {
					resultMap.put(receiver, "短信内容为空(7)");
				} else if (i == 8) {
					resultMap.put(receiver, "无签名，必须，格式：【签名】(8)");
				} else if (i == 9) {
					resultMap.put(receiver, "没有Url提交权限(9)");
				} else if (i == 10) {
					resultMap.put(receiver, "发送号码过多,最多支持200个号码(10)");
				} else if (i == 11) {
					resultMap.put(receiver, "产品ID异常或产品禁用(11)");
				} else if (i == 12) {
					resultMap.put(receiver, "参数异常(12)");
				} else if (i == 13) {
					resultMap.put(receiver, "30分种重复提交(13)");
				} else if (i == 14) {
					resultMap.put(receiver, "用户名或密码不正确，产品余额为0，禁止提交，联系客服(14)");
				} else if (i == 15) {
					resultMap.put(receiver, "Ip验证失败(15)");
				} else if (i == 19) {
					resultMap.put(receiver, "短信内容过长，最多支持500个(19)");
				} else if (i == 20) {
					resultMap.put(receiver, "定时时间不正确：格式：20130202120212(14位数字)(20)");
				} else if(i ==0){
					resultMap.put(receiver, "发送短信失败(0)");
				} else {
					resultMap.put(receiver, "短信有可能没有发送成功，原因未知(" + i + ")");
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return resultMap;
	}
}
