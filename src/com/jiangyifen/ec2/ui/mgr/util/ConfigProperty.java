package com.jiangyifen.ec2.ui.mgr.util;

import com.jiangyifen.ec2.utils.SpringContextHolder;

public class ConfigProperty {
	public static int ASSIGN_RESOURCE_NUMBER = 1000;
	public static String PATH = "";
	public static String PATH_EXPORT = "";
	public static String ASTERISK_SOUNDFILE_PATH="/var/lib/asterisk/sounds/";
	public static String ACTIVEMQ_ROOT;
	static {
		PATH = SpringContextHolder.getHttpSession().getServletContext()
				.getRealPath("/upload");

		PATH_EXPORT = SpringContextHolder.getHttpSession().getServletContext()
				.getRealPath("/export");
		ACTIVEMQ_ROOT = "D:\\apache-activemq-5.6.0";
	}

}
