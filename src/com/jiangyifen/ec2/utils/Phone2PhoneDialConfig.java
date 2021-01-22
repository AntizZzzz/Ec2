package com.jiangyifen.ec2.utils;

import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 外转外配置转接方式 超时 时间设定工具类
 * @author jrh
 *
 */
public class Phone2PhoneDialConfig {
	// assigned timeout seconds when csr don't have any redirect plan
	public static final String TIMEOUT_COMMON="timeout_common"; 

	// assigned timeout seconds when redirect plan type is noanswer
	public static final String TIMEOUT_NOANSWER="timeout_noanswer"; 
	
	// assigned timeout seconds when redirect plan type is busy
	public static final String TIMEOUT_BUSY="timeout_busy"; 
	
	// assigned timeout seconds when redirect plan type is unonline
	public static final String TIMEOUT_UNONLINE="timeout_unonline"; 
	
	//other config 
	private static Logger logger;
	public static Properties props = null;
	public static InputStream inputStream = null;
	
	static{
		try {
			logger = LoggerFactory.getLogger(Phone2PhoneDialConfig.class);
			props = new Properties();
			inputStream= Phone2PhoneDialConfig.class.getClassLoader().getResourceAsStream("propertiefiles/phone2phone_dial.properties");
			props.load(inputStream);
		} catch (Exception e) {
			logger.error("phone2phone_dial config file not found or cannot read!", e);
		}
	}

//	TODO
	public static void main(String[] args) {
		System.out.println(props.getProperty(TIMEOUT_COMMON));
		System.out.println(props.getProperty(TIMEOUT_NOANSWER));
	}
}
