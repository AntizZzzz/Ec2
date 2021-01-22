package com.jiangyifen.ec2.utils;

import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AutoDialConfig{
	//producer
	public static final String PRODUCER_MIN="producer_min"; 
	public static final String PRODUCER_STEP="producer_step";
	public static final String PRODUCER_SLEEP_TIME="producer_sleep_time";
	//consumer
	public static final String CONSUMER_SLEEP_TIME="consumer_sleep_time";
	public static final String CONSUMER_USERID="consumer_userid";
	//queue
	public static final String QUEUE_MAX="queue_max";

	//default autodialout ratio
	public static final String DEFAULT_RATIO="default_ratio";
	
	//#default autodialout percentageDepth
	public static final String DEFAULT_PERCENTAGE_DEPTH="default_percentage_depth";

	//#default autodialout manual specified callers count
	public static final String DEFAULT_STATIC_EXPECTED_CALLERS="default_static_expected_callers";

	
	public static final String FILENAME="autodial_arg.properties"; 
	//other config 
	private static Logger logger;
	public static Properties props = null;
	public static InputStream inputStream = null;
	static{
		try {
			logger = LoggerFactory.getLogger(AutoDialConfig.class);
			props = new Properties();
			inputStream= AutoDialConfig.class.getClassLoader().getResourceAsStream(FILENAME);
			props.load(inputStream);
		} catch (Exception e) {
			logger.error("autoDial config file not found or cannot read!", e);
		}
	}
}
