package com.jiangyifen.ec2.ui.mgr.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.utils.Config;
import com.jiangyifen.ec2.utils.SpringContextHolder;

/**
 * 
 * <p>获取录音的基础路径</p>
 * 
 * <p>不要在线程中使用，Session不允许再线程中访问</p>
 *
 * @version $Id: BaseUrlUtils.java 2014-6-9 上午11:24:34 chenhb $
 *
 */
public class BaseUrlUtils {
	private static Logger logger = LoggerFactory.getLogger(Config.class);
	
	//录音url的路径问题
	public static String getBaseUrl(){
		
		try {
			if(SpringContextHolder.getHttpSession() == null || SpringContextHolder.getHttpSession().getAttribute("baseurl") == null) {
				return Config.props.getProperty(Config.REC_URL_PERFIX);
			}
		} catch (Exception e) {
			return Config.props.getProperty(Config.REC_URL_PERFIX);
		}
		
		String baseUrl="";
		//自动所有，默认用80端口
		if("autoall".equals(Config.props.getProperty(Config.REC_MODEL))){
			baseUrl=(String)SpringContextHolder.getHttpSession().getAttribute("baseurl");
			
			baseUrl=baseUrl.replace("${port}", "80");
			
		//自动IP，用配置的端口
		}else if("autoip".equals(Config.props.getProperty(Config.REC_MODEL))){
			baseUrl=(String)SpringContextHolder.getHttpSession().getAttribute("baseurl");
			String configPort=Config.props.getProperty(Config.REC_PORT);
			configPort=configPort.trim();
			if(StringUtils.isNumeric(configPort)){
				baseUrl=baseUrl.replace("${port}", configPort);
			}else{
				baseUrl=baseUrl.replace("${port}", "80");
				logger.info("chenhb: "+Config.REC_PORT+" config "+configPort+" error ,default to port 80");
			}
			
		//用配置项来识别IP
		}else if("configall".equals(Config.props.getProperty(Config.REC_MODEL))){
			baseUrl=Config.props.getProperty(Config.REC_URL_PERFIX);
		}else{
			logger.info("chenhb: "+Config.REC_MODEL+" config "+Config.props.getProperty(Config.REC_MODEL)+" unknown config");
		}
		
		//如果任然为空
		if(StringUtils.isBlank(baseUrl)){
			baseUrl=Config.props.getProperty(Config.REC_URL_PERFIX);
			logger.info("chenhb: BaseUrl is blank , default to blank");
		}
		
		return baseUrl;
		
	}
}
