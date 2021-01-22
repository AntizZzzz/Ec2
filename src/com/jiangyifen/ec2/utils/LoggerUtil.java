package com.jiangyifen.ec2.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * 记录 日志工具类
 * 不知对性能有没有影响
 * @author chb
 */
public class LoggerUtil {
	/**
	 * 记录Info级别日志
	 * @param instance
	 * @param logInfo
	 */
	public static void logInfo(Object object,String logInfo){
		Logger logger =LoggerFactory.getLogger(object.getClass());
		logger.info(logInfo);
	}

	/**
	 * 记录Warn级别日志
	 * @param instance
	 * @param logInfo
	 */
	public static void logWarn(Object object,String logInfo){
		Logger logger =LoggerFactory.getLogger(object.getClass());
		logger.info(logInfo);
		
		
	}
	
	/**
	 * 记录Error级别日志
	 * @param instance
	 * @param logInfo
	 */
	public static void logError(Object object,String logInfo){
		Logger logger =LoggerFactory.getLogger(object.getClass());
		logger.info(logInfo);
		
		
	}
	
	/**
	 * 记录Debug级别日志
	 * @param instance
	 * @param logInfo
	 */
	public static void logDebug(Object object,String logInfo){
		Logger logger = LoggerFactory.getLogger(object.getClass());
		logger.info(logInfo);
	}
}
