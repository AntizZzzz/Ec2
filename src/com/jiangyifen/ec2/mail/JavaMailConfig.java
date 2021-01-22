package com.jiangyifen.ec2.mail;

import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 获取配置信息的唯一入口
 */
public class JavaMailConfig {

	private static String default_config = "./javamail.properties";

	private static Properties config;

	private static Logger logger = LoggerFactory.getLogger(JavaMailConfig.class);

	static {
		config = new Properties();
		try {
			InputStream is = JavaMailConfig.class.getResourceAsStream(default_config);
			config.load(is);
		} catch (Exception e) {
			if (logger.isErrorEnabled()) {
				logger.error("初始化配置失败：" + e.getCause());
			}
		}
	}

	private JavaMailConfig() {
	}

	/**
	 * 获取属性值
	 * 
	 * @param 属性键值
	 * @return 属性值, 未找到则返回null
	 */
	public static String getProperty(String key) {
		String value = config.getProperty(key);
		return value == null ? value : value.trim();
	}

	/**
	 * 获取属性值
	 * 
	 * @param 属性键值
	 * @param 未找到时的默认值
	 * @return 属性值，未找到则返回默认值
	 */
	public static String getProperty(String key, String defaultValue) {
		String value = config.getProperty(key);
		if (value == null)
			return defaultValue;
		return value.trim();
	}

	/**
	 * 获取boolean类型的属性值，不存在则为false
	 */
	public static boolean getBooleanProperty(String name) {
		return getBooleanProperty(name, false);
	}

	/**
	 * 获取boolean类型的属性值，不存在则为提供的默认值
	 */
	public static boolean getBooleanProperty(String name, boolean defaultValue) {
		// get the value first, then convert
		String value = JavaMailConfig.getProperty(name);
		if (value == null)
			return defaultValue;
		return (new Boolean(value)).booleanValue();
	}

	/**
	 * 获取int类型的属性值，不存在则为0
	 */
	public static int getIntProperty(String name) {
		return getIntProperty(name, 0);
	}

	/**
	 * 获取int类型的属性值，不存在则为提供的默认值
	 */
	public static int getIntProperty(String name, int defaultValue) {
		// get the value first, then convert
		String value = JavaMailConfig.getProperty(name);
		if (value == null)
			return defaultValue;
		return (new Integer(value)).intValue();
	}

	/**
	 * 获取所有键值
	 * 
	 * @return Enumeration 所有键值的集合
	 */
	@SuppressWarnings("rawtypes")
	public static Enumeration keys() {
		return config.keys();
	}

	/**
	 * 获取键值为startingWith开始的属性
	 */
	@SuppressWarnings("rawtypes")
	public static Properties getPropertiesStartingWith(String startingWith) {
		Properties props = new Properties();
		for (Enumeration it = config.keys(); it.hasMoreElements();) {
			String key = (String) it.nextElement();
			if(key.startsWith(startingWith))
				props.put(key, config.get(key));
		}
		return props;
	}

	public static void setUploadsDir(String path) {
		// only do this if the user wants to use the webapp context
		if ("${webapp.context}".equals(config.getProperty("uploads.dir"))) {
			config.setProperty("uploads.dir", path);
		}
	}

	public static void setTempDir(String path) {
		// only do this if the user wants to use the webapp context
		if ("${webapp.context}".equals(config.getProperty("temp.dir"))) {
			config.setProperty("temp.dir", path);
		}
	}
	
	public static Properties getProperties(){
		return config;
	}
}
