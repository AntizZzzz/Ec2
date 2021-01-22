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
public class UnicomSmsSend implements SmsSendIface {

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
						.getProperty(SmsConfig.UNICOM_SMS_ROUTE);
				URL url = new URL(partUrl);
				String contentEncoded = URLEncoder.encode(content, "GB2312");
				// TODO SpCode=000001 企业编号，暂时写死
				// 老的测试账号为 username = sh_zd ,password = zd1234, 企业编号 = 201451
				String params = "SpCode=205459&LoginName="
						+ smsInfo.getUserName().trim() + "&Password="
						+ smsInfo.getPassword().trim() + "&UserNumber=" + receiver
						+ "&MessageContent=" + contentEncoded
						+ "&SerialNumber=&ScheduleTime=&f=1";

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

				// 取出指定坐标的字符
				String resultNum = "-1";
				try {
					resultNum = xmlResult.substring(xmlResult.indexOf("=")+1, xmlResult.indexOf("&"));
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				String s = String.valueOf(resultNum);

				int i = Integer.parseInt(s);

				if (i == 0) {
					resultMap.put(receiver, "短信发送成功");
				} else if (i == 1) {
					resultMap.put(receiver, "提交参数不能为空");
				} else if (i == 2) {
					resultMap.put(receiver, "帐号无效或未开户");
				} else if (i == 3) {
					resultMap.put(receiver, "帐号密码错误");
				} else if (i == 4) {
					resultMap.put(receiver, "预约发送时间无效");
				} else if (i == 5) {
					resultMap.put(receiver, "IP不合法");
				} else if (i == 6) {
					resultMap.put(receiver, "号码中含有无效号码或不在规定的号段,");
				} else if (i == 7) {
					resultMap.put(receiver, "中含有非法关键内容字");
				} else if (i == 8) {
					resultMap.put(receiver, "内容长度超过上限，最大402字符");
				} else if (i == 9) {
					resultMap.put(receiver, "接受号码过多，最大1000");
				} else if (i == 10) {
					resultMap.put(receiver, "黑名单用户");
				} else if (i == 11) {
					resultMap.put(receiver, "提交速度太快");
				} else if (i == 12) {
					resultMap.put(receiver, "您尚未订购[普通短信业务]，暂不能发送该类信息");
				} else if (i == 13) {
					resultMap.put(receiver, "您的[普通短信业务]剩余数量发送不足，暂不能发送该类信息");
				} else if (i == 14) {
					resultMap.put(receiver, "流水号格式不正确");
				} else if (i == 15) {
					resultMap.put(receiver, "流水号重复");
				} else if (i == 16) {
					resultMap.put(receiver, "超出发送上限（操作员帐户当日发送上限）");
				} else if (i == 17) {
					resultMap.put(receiver, "余额不足");
				} else if (i == 18) {
					resultMap.put(receiver, "扣费不成功");
				} else if (i == 20) {
					resultMap.put(receiver, "系统错误");
				} else if (i == 21) {
					resultMap.put(receiver, "您只能发送联通的手机号码，本次发送的手机号码中包含了非联通的手机号码");
				} else if (i == 22) {
					resultMap.put(receiver, "您只能发送移动的手机号码，本次发送的手机号码中包含了非移动的手机号码");
				} else if (i == 23) {
					resultMap.put(receiver, "您只能发送电信的手机号码，本次发送的手机号码中包含了非电信的手机号码");
				} else if (i == 24) {
					resultMap.put(receiver, "账户状态不正常");
				} else if (i == 25) {
					resultMap.put(receiver, "账户权限不足");
				} else if (i == 26) {
					resultMap.put(receiver, "需要人工审核");
				} else if (i == 28) {
					resultMap.put(receiver, "发送内容与模板不符");
				} else {
					resultMap.put(receiver, "短信有可能没有发送成功，原因未知");
				}
			} catch (MalformedURLException e) {
				e.printStackTrace();
				logger.error(
						"lc 客户使用中国联通短信平台-中道自找通道发送短信失败，可能是授权已过期-->" + e.getMessage(),
						e);
				resultMap.put(receiver, "发送失败，可能是授权已过期");
			} catch (ProtocolException e) {
				e.printStackTrace();
				logger.error(
						"lc 客户使用中国联通短信平台-中道自找通道发送短信失败，可能是授权已过期-->" + e.getMessage(),
						e);
				resultMap.put(receiver, "发送失败，可能是授权已过期");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				logger.error(
						"lc 客户使用中国联通短信平台-中道自找通道发送短信失败，可能是授权已过期-->" + e.getMessage(),
						e);
				resultMap.put(receiver, "发送失败，可能是授权已过期");
			} catch (IOException e) {
				e.printStackTrace();
				logger.error(
						"lc 客户使用中国联通短信平台-中道自找通道发送短信失败，可能是授权已过期-->" + e.getMessage(),
						e);
				resultMap.put(receiver, "发送失败，可能是授权已过期");
			}
		}
		
		return resultMap;
	}

}
