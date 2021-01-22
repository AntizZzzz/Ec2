package com.jiangyifen.ec2.globaldata;

 
public class GlobalVariable {
	/*
	 * 同时可以在线的坐席数 chb comment
	 */
//	public static int CONCURRENT_MAX_CSR = 120;
	
	/*
	 * 系统到期时间  chb 
	 */
//	1public static String EC2_EXPIRED_DATE = "2014-06-30";
	
	/*
	 * 客户呼入，如果直接转到队列，设置timeout
	 */
	public static String AGI_INCOMING_TO_QUEUE_TIMEOUT="1800";
	
	/*
	 * 客户呼入，如果直接转到分机，设置timeout
	 */
	public static String AGI_INCOMING_TO_EXTEN_TIMEOUT="1800";
	
	/*
	 * 客户呼入，如果直接转到外部手机，设置timeout
	 */
	public static String AGI_INCOMING_TO_MOBILE_TIMEOUT="1800";
	

	/**
	 * QueuePauseRecord 呼入话后处理置忙 原因		2014-06-24
	 */
	public static String AFTER_CALL_IN_PAUSE_EXTEN_REASON = "呼入话后";	
	
	/**
	 * QueuePauseRecord 呼出话后处理置忙 原因		2014-06-24
	 */
	public static String AFTER_CALL_OUT_PAUSE_EXTEN_REASON = "呼出话后";	
	
	// jrh 外加 3 种特殊的置忙原因
	/**
	 * 登陆后默认的置忙原因
	 */
	public static String DEFAULT_LOGIN_EXTEN_PAUSE_REASON = "登陆置忙";
	
	/** jrh 2014-06-24 将该原因由原来的‘呼我吧’ 改成 ‘空闲’
	 * //	public static String DEFAULT_UNPAUSE_EXTEN_REASON = "呼我吧";
	 * 
	 * QueuePauseRecord 新增的原因为 “空闲” 的置忙置闲记录
	 */
	public static String DEFAULT_UNPAUSE_EXTEN_REASON = "空闲";

	/**
	 * QueuePauseRecord  默认的通过接口调用时置忙的原因
	 */
	public static String DEFAULT_IFACE_PAUSE_EXTEN_REASON = "忙碌";


	/**
	 * QueuePauseRecord 默认的管理员置忙原因
	 */
	public static String MANAGER_FORCE_EXTEN_PAUSE_REASON = "管理员强制置忙";
	
	
	/**
	 * QueuePauseRecord 默认的管理员置闲原因
	 */
	public static String MANAGER_FORCE_EXTEN_UNPAUSE_REASON = "管理员强制空闲";

	/**
	 *  QueuePauseRecord 该信息用于 创建默认的置忙记录的原因，使用场景：如管理员强制置忙，
	 *  	此时针对asterisk 的置忙原因就是 ‘管理员强制置忙’， 但DB 创建置忙记录的原因就用这个 “忙碌”
	 */
	public static String DEFAULT_PAUSE_RECORD_REASON = "忙碌"	;		
	
	
	
	
	
	
	
	/************************************************************************************************/
	/**
	 * 该参数用来检查asterisk channel 残留的问题，即导致软电话无法继续拨打电话
	 * 如果该参数配置的域 GlobalData 中的 MAC_ADDRESS 相同，则会写日志文件 bug_channelRemnant.log ，
	 * 并且在管理员在通话状态置忙时，就会给 jrh 的邮箱 initialjiang@126.com 发送邮件，其附件就是日志文件  bug_channelRemnant.log
	 */
	public static String mac_asterisk_channel_remnant = "00:26:55:7B:AE:70";

}
