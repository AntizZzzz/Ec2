package com.jiangyifen.ec2.utils;

import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Config{
	// ami config
	public static final String AMI_USERNAME="amiusername"; 
	public static final String AMI_PWD="amipwd"; 
	public static final String AMI_IP="amiip"; 
	
	
	// asterisk config path
	public static final String DEFAULT_MOH_PATH="defaultmohpath";			// musiconhold 的默认语音文件存放路径
	public static final String CUSTOMER_MOH_PATH="customermohpath";			// musiconhold 的自定义语音文件存放路径
	public static final String CONF_FILE_PATH="conffilepath";				// asterisk 的配置文件存放路径
	public static final String CONF_BACKUP_FILE_PATH="confbackupfilepath";	// asterisk 的配置文件的备份路径
	
	
	// chart config
	public static final String CHART_WIDTH="chartwidth";
	public static final String  CHART_HEIGHT="chartheight";
	
	// recore file path prefix  chenhb: changed 20140604
	//录音路径配置模式  
//	autoall - auto detect ip and use 80 port;  
//	autoip - auto detect ip  
//	configall - use config ip and port
	public static final String REC_MODEL="rec_model";
	public static final String REC_PORT="rec_port";
	public static final String REC_URL_PERFIX="rec_url_perfix";
	public static final String REC_DIR_PATH="rec_dir_path";
	// 录音文件的临时存放位置,在挂断电话之后会把这个录音文件移入到 REC_DIR_PATH 目录下的指定位置
	public static final String REC_LOCAL_MEM_PATH = "rec_local_mem_path";
	// 三方会议的录音文件
	public static final String REC_MR_LOCAL_DISK_PATH="rec_mr_local_disk_path";
	
	public static final String  DELETE_TASK_MAX_NUM  = "delete_task_max_num";
	public static final String  PERMIT_EXECUTE_DELETE_HOUR  = "permit_execute_delete_hour";
			
			
	//other config 
	private static Logger logger;
	public static Properties props = null;
	public static InputStream inputStream = null;
	
	public static final String WORK_ORDER_PATH = "work_order_path";
	public static final String SNMP_IP = "snmp_ip";
	
	static{
		try {
			logger = LoggerFactory.getLogger(Config.class);
			props = new Properties();
			inputStream= Config.class.getClassLoader().getResourceAsStream("config.properties");
			props.load(inputStream);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}
	
//	/**
//	 * 获取录音url前缀
//	 * @return
//	 */
//	public static String getRecUrlPerfix() {
//		String baseUrl="";
//		//自动所有，默认用80端口
//		if("autoall".equals(Config.props.getProperty(Config.REC_MODEL))){
//			baseUrl=(String)SpringContextHolder.getHttpSession().getAttribute("baseurl");
//			baseUrl=baseUrl.replace("${port}", "80");
//			
//		//自动IP，用配置的端口
//		}else if("autoip".equals(Config.props.getProperty(Config.REC_MODEL))){
//			baseUrl=(String)SpringContextHolder.getHttpSession().getAttribute("baseurl");
//			String configPort=Config.props.getProperty(Config.REC_PORT);
//			configPort=configPort.trim();
//			if(StringUtils.isNumeric(configPort)){
//				baseUrl=baseUrl.replace("${port}", configPort);
//			}else{
//				baseUrl=baseUrl.replace("${port}", "80");
//				logger.info("chenhb: "+Config.REC_PORT+" config "+configPort+" error ,default to port 80");
//			}
//			
//		//用配置项来识别IP
//		}else if("configall".equals(Config.props.getProperty(Config.REC_MODEL))){
//			baseUrl=Config.props.getProperty(Config.REC_URL_PERFIX);
//		}else{
//			logger.info("chenhb: "+Config.REC_MODEL+" config "+Config.props.getProperty(Config.REC_MODEL)+" unknown config");
//		}
//		
//		//如果任然为空
//		if(StringUtils.isBlank(baseUrl)){
//			baseUrl=Config.props.getProperty(Config.REC_URL_PERFIX);
//			logger.info("chenhb: BaseUrl is blank , default to blank");
//		}
//		
//		return baseUrl;
//	}
}
