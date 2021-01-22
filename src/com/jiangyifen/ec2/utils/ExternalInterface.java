package com.jiangyifen.ec2.utils;

import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 外部接口调用的属性配置工具类
 *
 * @author jinht
 *
 * @date 2015-6-17 下午1:19:08 
 *
 */
public class ExternalInterface {

	/**
	 * 呼入弾屏接口是否启用调用, true 表示开启状态, false 表示关闭状态, 默认为关闭状态
	 */
	public static String INCOMING_ELASTIC_SCREEN_INTERFACE_IS_OPEN = "incoming_elastic_screen_interface_is_open";
	
	/**
	 * 呼入弾屏接口调用的地址
	 */
	public static String INCOMING_ELASTIC_SCREEN_INTERFACE_URL = "incoming_elastic_screen_interface_url";
	
	/**
	 * 呼入弾屏接口调用地址后面跟着的参数: 被叫用户Id, 被叫用户名, 被叫外线, 被叫分机, 主叫号码
	 */
	public static String INCOMING_ELASTIC_SCREEN_INTERFACE_URL_PARAMS = "incoming_elastic_screen_interface_url_params";
	
	/**
	 * 呼出弾屏接口是否启用调用, true 表示开启状态, false 表示关闭装呗, 默认为关闭状态
	 */
	public static String OUTGOING_ELASTIC_SCREEN_INTERFACE_IS_OPEN = "outgoing_elastic_screen_interface_is_open";
	
	/**
	 * 呼出弾屏接口调用的地址
	 */
	public static String OUTGOING_ELASTIC_SCREEN_INTERFACE_URL = "outgoing_elastic_screen_interface_url";
	
	/**
	 * 呼出弾屏接口调用地址后面跟着的参数: 被叫号码, 主叫分机, 主叫用户Id, 主叫用户名
	 */
	public static String OUTGOING_ELASTIC_SCREEN_INTERFACE_URL_PARAMS = "outgoing_elastic_screen_interface_url_params";
	
	/**
	 * 是否开启推送 CDR 话单到第三方系统, true 表示开启状态, false 表示关闭状态,  默认为关闭状态
	 */
	public static String PUSH_CDR_TO_THIRD_PARTY_SYSTEM_IS_OPEN = "push_cdr_to_third_party_system_is_open";
	
	/**
	 * 第三方系 CDR 话单的推送地址
	 */
	public static String PUSH_CDR_TO_THIRD_PARTY_SYSTEM_URL = "push_cdr_to_third_party_system_url";
	
	/**
	 * 是否开启这个监控接听事件推送信息
	 */
	public static String LISTENER_BRIDGE_EVENT_IS_OPEN = "listener_bridge_event_is_open";
	
	/**
	 * 监控接听事件推送信息的URL地址
	 */
	public static String LISTENER_BRIDGE_EVENT_URL = "listener_bridge_event_url";
	
	// other config
	private static Logger logger;
	public static Properties props = null;
	public static InputStream inputStream = null;
	
	static {
		try {
			logger = LoggerFactory.getLogger(Phone2PhoneDialConfig.class);
			props = new Properties();
			inputStream = ExternalInterface.class.getClassLoader().getResourceAsStream("propertiefiles/external_interface.properties");
			props.load(inputStream);
			
			INCOMING_ELASTIC_SCREEN_INTERFACE_IS_OPEN = props.getProperty(INCOMING_ELASTIC_SCREEN_INTERFACE_IS_OPEN);
			INCOMING_ELASTIC_SCREEN_INTERFACE_URL = props.getProperty(INCOMING_ELASTIC_SCREEN_INTERFACE_URL);
			INCOMING_ELASTIC_SCREEN_INTERFACE_URL_PARAMS = props.getProperty(INCOMING_ELASTIC_SCREEN_INTERFACE_URL_PARAMS);
			OUTGOING_ELASTIC_SCREEN_INTERFACE_IS_OPEN = props.getProperty(OUTGOING_ELASTIC_SCREEN_INTERFACE_IS_OPEN);
			OUTGOING_ELASTIC_SCREEN_INTERFACE_URL = props.getProperty(OUTGOING_ELASTIC_SCREEN_INTERFACE_URL);
			OUTGOING_ELASTIC_SCREEN_INTERFACE_URL_PARAMS = props.getProperty(OUTGOING_ELASTIC_SCREEN_INTERFACE_URL_PARAMS);
			PUSH_CDR_TO_THIRD_PARTY_SYSTEM_IS_OPEN = props.getProperty(PUSH_CDR_TO_THIRD_PARTY_SYSTEM_IS_OPEN);
			PUSH_CDR_TO_THIRD_PARTY_SYSTEM_URL = props.getProperty(PUSH_CDR_TO_THIRD_PARTY_SYSTEM_URL);
			LISTENER_BRIDGE_EVENT_IS_OPEN = props.getProperty(LISTENER_BRIDGE_EVENT_IS_OPEN);
			LISTENER_BRIDGE_EVENT_URL = props.getProperty(LISTENER_BRIDGE_EVENT_URL);
		} catch (Exception e) {
			logger.error("external_interface config file not found or cannot read!", e);
		}
	}
	
	public static void main(String[] args) {
		System.out.println(INCOMING_ELASTIC_SCREEN_INTERFACE_IS_OPEN);
		System.out.println(INCOMING_ELASTIC_SCREEN_INTERFACE_URL);
		System.out.println(INCOMING_ELASTIC_SCREEN_INTERFACE_URL_PARAMS);
//		System.out.println(INCOMING_ELASTIC_SCREEN_INTERFACE_URL_PARAMS.replace("{0}", "13131313").replace("{1}", "1001"));
		System.out.println(MessageFormat.format(INCOMING_ELASTIC_SCREEN_INTERFACE_URL_PARAMS, "13131313", "1001"));
		System.out.println(PUSH_CDR_TO_THIRD_PARTY_SYSTEM_IS_OPEN);
		System.out.println(PUSH_CDR_TO_THIRD_PARTY_SYSTEM_URL);
	}
	
}
