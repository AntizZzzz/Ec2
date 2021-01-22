package com.jiangyifen.ec2.utils;

import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 短息发送通道，http 固定请求路径配置文件
 * @author jrh
 *
 */
public class SmsConfig {
	// 【君恒代理短信通道】
	public static String JUNHENG_SMS_ROUTE = "junheng_sms_route";
	
	// jrh 苏沃科技短信平台的短信发送通道【呼太代理】
	public static String SUWOIT_SMS_ROUTE = "suwoit_sms_route"; 
	
	// jrh 爱谷果传媒短信平台的短信发送通道 【慧亮代理】
	public static String IGOOAPP_SMS_ROUTE = "igooapp_sms_route"; 
	
	// jrh 企信通短信平台的短信发送通道 【慧亮代理】
	public static String QXTONG_SMS_ROUTE = "qxtong_sms_route"; 
	
	// jrh IACS短信平台的短信发送通道 【惠顺商贸自找通道】
	public static String IACS_SMS_ROUTE = "iacs_sms_route"; 
	
	// jrh 中国联通短信平台发送通道【中道】
	public static String UNICOM_SMS_ROUTE = "unicom_sms_route";
	
	//lc dx686 短信平台发送通道【姜吉祥】
	public static String DX686_SMS_ROUTE = "dx686_sms_route";
	
	//lc sioo短信平台发送通道
	public static String SIOO_SMS_ROUTE = "sioo_sms_route";
	
	// jrh duanxin10086 短信通道
	public static String DUANXIN10086_SMS_ROUTE = "duanxin10086_sms_route";
	
	// chenhb biztoall 短信通道
	public static String BIZTOALL_SMS_ROUTE = "biztoall_sms_route";
	
	// jrh shsixun[思讯通] 短信通道
	public static String SHSIXUN_SMS_ROUTE = "shsixun_sms_route";
	
	// chenhb shjianzhou 【上海为信电子商务】
	public static String SHJIANZHOU_SMS_ROUTE = "shjianzhou_sms_route";
	
	// xuankp meimanrensheng [美满人生]
	public static String MEIMANRENSHENG_SMS_ROUTE="meiManRenSheng_sms_route";
	
	// jinht waimicaifu [万米财富] 短信通道
	public static String SHDIYIXINXI_SMS_ROUTE = "shdiyixinxi_sms_route";
	
	// jinht bjtianxin [诺诺磅客] 短信通道
	public static String BJTIANXIN_SMS_ROUTE = "bjtianxin_sms_route";
		
	//other config 
	private static Logger logger;
	public static Properties props = null;
	public static InputStream inputStream = null;
	
	static{
		try {
			logger = LoggerFactory.getLogger(SmsConfig.class);
			props = new Properties();
			inputStream = SmsConfig.class.getClassLoader().getResourceAsStream("propertiefiles/sms.properties");
			props.load(inputStream);
		} catch (Exception e) {
			logger.error("Can't find or can't read the configuration which for sending message!", e);
		}
	}
	
//	TODO
	public static void main(String[] args) {
		System.out.println(props.getProperty(SUWOIT_SMS_ROUTE));
		System.out.println(props.getProperty(IGOOAPP_SMS_ROUTE));
	}
}
