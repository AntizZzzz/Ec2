package com.jiangyifen.ec2.servlet.http.common.utils;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Description 描述：解析接口对接时需要的配置文件
 *
 * @author  JRH
 * @date    2014年8月7日 下午6:10:52
 */
public class AnalyzeIfaceJointUtil {
	
	public static String IP_FILTERABLE = "ip_filterable";							// 是否创建置忙置闲记录

	public static String AUTHORIZE_IPS = "authorize_ips";							// 允许调用呼叫中心EC2 的接口的IP

	public static String JOINT_LICENSE_NECESSARY = "joint_license_necessary";		// 第三方系统调用EC2的接口是否需要License 验证（即：accessId 跟 accessKey 验证）
	
	public static String PAUSE_LOG_CREATEABLE = "pause_log_createable";			// 是否创建置忙置闲记录
	
	public static String PUSH_RESOURCE_IS_CREATE_BATCH = "push_resource_is_create_batch";				// 推送过来的数据是否按照每天一个批次进行创建保存数据
	public static String PUSH_RESOURCE_CREATE_BATCH_DATE = "push_resource_create_batch_date";			// 推送过来的数据如果创建批次, 则可以配置按照时间范围创建批次
	public static String PUSH_RESOURCE_CREATE_BATCH_END_NAME = "push_resource_create_batch_end_name";	// 推送数据创建批次的结尾名称
	
	public static String WHETHER_SUPPORT_CORS = "whether_support_cors";								// 接口是否支持跨域请求, true 表示接口支持跨域请求, false 表示接口不支持跨域请求, 默认为 false
	public static String RESPONSE_CONTENT_TYPE = "response_content_type";
	
	//other config 
	private static Logger logger;
	public static Properties props = null;
	public static InputStream inputStream = null;

	public static HashSet<String> AUTHORIZE_IPS_SET = new HashSet<String>();		// 存储可以访问EC2接口的ip集合
	public static boolean PAUSE_LOG_CREATEABLE_VALUE = true;						// 默认是创建置忙置闲记录
	public static boolean JOINT_LICENSE_NECESSARY_VALUE = true;						// 默认是进行IP过滤的
	public static boolean IP_FILTERABLE_VALUE = true;								// 默认是进行IP过滤的
	
	static{
		try {
			logger = LoggerFactory.getLogger(AnalyzeIfaceJointUtil.class);
			props = new Properties();
			inputStream = AnalyzeIfaceJointUtil.class.getClassLoader().getResourceAsStream("com/jiangyifen/ec2/servlet/http/common/utils/iface_joint.properties");
			props.load(inputStream);
			
			String ip_filterable_str = AnalyzeIfaceJointUtil.props.getProperty(IP_FILTERABLE);
			if("true".equals(ip_filterable_str)) {
				IP_FILTERABLE_VALUE = true;
			} else {
				IP_FILTERABLE_VALUE = false;
			}

			String authorize_ips_str = AnalyzeIfaceJointUtil.props.getProperty(AUTHORIZE_IPS);
			if(authorize_ips_str != null && !"".equals(authorize_ips_str)) {
				for(String ip : authorize_ips_str.split(",")) {
					ip = ip.replace(" ", "");	// 去掉其中的空格
					if(!"".equals(ip)) {
						AUTHORIZE_IPS_SET.add(ip);
					}
				}
			}
			
			String joint_license_necessary_str = AnalyzeIfaceJointUtil.props.getProperty(JOINT_LICENSE_NECESSARY);
			if("true".equals(joint_license_necessary_str)) {
				JOINT_LICENSE_NECESSARY_VALUE = true;
			} else {
				JOINT_LICENSE_NECESSARY_VALUE = false;
			}

			String pause_log_createable_str = AnalyzeIfaceJointUtil.props.getProperty(PAUSE_LOG_CREATEABLE);
			if("true".equals(pause_log_createable_str)) {
				PAUSE_LOG_CREATEABLE_VALUE = true;
			} else {
				PAUSE_LOG_CREATEABLE_VALUE = false;
			}
			
			PUSH_RESOURCE_IS_CREATE_BATCH = AnalyzeIfaceJointUtil.props.getProperty(PUSH_RESOURCE_IS_CREATE_BATCH);
			PUSH_RESOURCE_CREATE_BATCH_DATE = AnalyzeIfaceJointUtil.props.getProperty(PUSH_RESOURCE_CREATE_BATCH_DATE);
			PUSH_RESOURCE_CREATE_BATCH_END_NAME = AnalyzeIfaceJointUtil.props.getProperty(PUSH_RESOURCE_CREATE_BATCH_END_NAME);
			
			WHETHER_SUPPORT_CORS = AnalyzeIfaceJointUtil.props.getProperty(WHETHER_SUPPORT_CORS);
			RESPONSE_CONTENT_TYPE = AnalyzeIfaceJointUtil.props.getProperty(RESPONSE_CONTENT_TYPE);
			
		} catch (Exception e) {
			logger.error("Can't find or can't read the configuration which for system butt joint!", e);
		}
	}
	
	/**
	 * @Description 描述：获取可以访问EC2接口的ip集合
	 *
	 * @author  JRH
	 * @date    2014年8月7日 下午7:15:10
	 * @return HashSet<String>
	 */
	public static HashSet<String> getAuthorizeIps() {
		HashSet<String> authorizeIpsSet = new HashSet<String>();
		
		String authorize_ips_str = props.getProperty(AUTHORIZE_IPS);
		
		if(authorize_ips_str != null && !"".equals(authorize_ips_str)) {
			for(String ip : authorize_ips_str.split(",")) {
				authorizeIpsSet.add(ip);
			}
		}
		
		return authorizeIpsSet;
	}
	
//	TODO
	public static void main(String[] args) {
//		System.out.println(props.getProperty(AUTHORIZE_IPS));
//		System.out.println(props.getProperty(PAUSE_LOG_CREATEABLE));
		System.out.println(IP_FILTERABLE_VALUE);
		System.out.println("-----"+StringUtils.join(AUTHORIZE_IPS_SET, ","));
		System.out.println(JOINT_LICENSE_NECESSARY_VALUE);
		System.out.println(PAUSE_LOG_CREATEABLE_VALUE);
		System.out.println(PUSH_RESOURCE_IS_CREATE_BATCH);
		System.out.println(PUSH_RESOURCE_CREATE_BATCH_DATE);
		System.out.println(PUSH_RESOURCE_CREATE_BATCH_END_NAME);
		System.out.println(WHETHER_SUPPORT_CORS);
		System.out.println(RESPONSE_CONTENT_TYPE);
	}
}
