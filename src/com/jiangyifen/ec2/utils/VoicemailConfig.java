package com.jiangyifen.ec2.utils;

import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 语音留言配置
 *
 * @author jinht
 *
 * @date 2015-6-18 上午8:19:34 
 *
 */
public class VoicemailConfig {

	/**
	 * 分机没有接听时是否进行语音留言
	 */
	public static String VOICEMAIL_EXTEN_NO_ANSWER = "voicemail_exten_no_answer";
	
	/**
	 * 队列没有所有成员置忙或不在线时是否进行语音留言
	 */
	public static String VOICEMAIL_QUEUE_MEMBERS_BUSY = "voicemail_queue_members_busy";
	
	/**
	 * 开启专员路由后直接呼入客户经理的分机时是否开启进行语音留言
	 */
	public static String VOICEMAIL_COMMISSIONER_ROUTE = "voicemail_commissioner_route";
	
	// other config
	private static Logger logger;
	public static Properties props = null;
	public static InputStream inputStream = null;
	
	static {
		try {
			logger = LoggerFactory.getLogger(VoicemailConfig.class);
			props = new Properties();
			inputStream = VoicemailConfig.class.getClassLoader().getResourceAsStream("propertiefiles/voicemail.properties");
			props.load(inputStream);
			
			VOICEMAIL_EXTEN_NO_ANSWER = props.getProperty(VOICEMAIL_EXTEN_NO_ANSWER);
			VOICEMAIL_QUEUE_MEMBERS_BUSY = props.getProperty(VOICEMAIL_QUEUE_MEMBERS_BUSY);
			VOICEMAIL_COMMISSIONER_ROUTE = props.getProperty(VOICEMAIL_COMMISSIONER_ROUTE);
		} catch (Exception e) {
			logger.error("voicemail config file not found or cannot read!", e);
		}
	}
	
	public static void main(String[] args) {
		System.out.println(VOICEMAIL_EXTEN_NO_ANSWER);
		System.out.println(VOICEMAIL_QUEUE_MEMBERS_BUSY);
		System.out.println(VOICEMAIL_COMMISSIONER_ROUTE);
	}
	
}
