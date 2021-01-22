package com.jiangyifen.ec2.utils.proputils;

import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @Description 描述：为 【上海利安达贵金属经营有限公司】 专用的Agi
 * 
 * 		根据呼入客户的要求，呼叫到指定的坐席，或队列
 * 		
 * 		客户的每一个按键代表要呼叫到指定的坐席，坐席的用户名=INPUT_NUMBER0的对应值
 * 
 * @author  jrh
 * @date    2014年2月12日 下午4:40:28
 * @version v1.1.1
 */
public class LiAnDaIncomingConfig {

	// 用户输入的按键为 "0"
	public static final String INPUT_NUMBER0 = "input_number0"; 

	// 用户输入的按键为 "1"
	public static final String INPUT_NUMBER1 = "input_number1"; 
	
	// 用户输入的按键为 "2"
	public static final String INPUT_NUMBER2 = "input_number2"; 
	
	// 用户输入的按键为 "3"
	public static final String INPUT_NUMBER3 = "input_number3"; 
	
	// 用户输入的按键为 "4"
	public static final String INPUT_NUMBER4 = "input_number4"; 
	
	// 用户输入的按键为 "5"
	public static final String INPUT_NUMBER5 = "input_number5"; 
	
	// 用户输入的按键为 "6"
	public static final String INPUT_NUMBER6 = "input_number6"; 
	
	// 用户输入的按键为 "7"
	public static final String INPUT_NUMBER7 = "input_number7"; 
	
	// 用户输入的按键为 "8"
	public static final String INPUT_NUMBER8 = "input_number8"; 
	
	// 用户输入的按键为 "9"
	public static final String INPUT_NUMBER9 = "input_number9"; 
	
	// 当用户想要联系某个指定的坐席，但坐席不在线，则呼叫到指定的队列中
	public static final String SPECIFIED_QUEUE = "specified_queue"; 

	//other config 
	private static Logger logger;
	public static Properties props = null;
	public static InputStream inputStream = null;
	
	static{
		try {
			logger = LoggerFactory.getLogger(LiAnDaIncomingConfig.class);
			props = new Properties();
			inputStream= LiAnDaIncomingConfig.class.getClassLoader().getResourceAsStream("propertiefiles/andali.properties");
			props.load(inputStream);
		} catch (Exception e) {
			logger.error("phone2phone_dial config file not found or cannot read!", e);
		}
	}

//	TODO
	public static void main(String[] args) {
		System.out.println(props.getProperty(INPUT_NUMBER9));
		System.out.println(props.getProperty(SPECIFIED_QUEUE));
	}
}
