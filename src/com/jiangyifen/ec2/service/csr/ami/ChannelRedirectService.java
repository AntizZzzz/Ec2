package com.jiangyifen.ec2.service.csr.ami;

/**
 * 电话转接服务接口
 */
public interface ChannelRedirectService {

	/**
	 * 电话转接到分机
	 * @param channel	当前被呼叫分机拥有的通道
	 * @param exten		要转接到的分机号
	 * @return boolean  如果转接成功，则返回true
	 */
	public boolean redirectExten(String channel, String exten);
	
	/**
	 * 电话转接到队列
	 * @param channel	当前被呼叫分机拥有的通道
	 * @param queueName	要转接到的队列名称
	 * @return boolean  如果转接成功，则返回true
	 */
	public boolean redirectQueue(String channel, String queueName);
	
	/**
	 * 电话转接到手机
	 * @param channel	当前被呼叫分机拥有的通道
	 * @param telephone	要转接到的手机号
	 * @return boolean  如果转接成功，则返回true
	 */
	public boolean redirectTelephone(String channel, String telephone);
	
	/**
	 * 电话转接到extension
	 * @param channel	当前被呼叫分机拥有的通道
	 * @param extension	要转接到的extension
	 * @return boolean  如果转接成功，则返回true
	 */
	public boolean redirectCommonExtension(String channel, String extension);

	/**
	 * 一次性将两个通道转接到指定的extension【一般用于将两个建立通话的通道转接到会议室】
	 * 
	 * *************************************************************************************************
	 * 注意：这里发起转接时--																   				  *
	 *    	[如果处理的是 呼出 的情况]，在接通后，将两个通道进行redirect 到会议室，那么转接传递的两个通道顺序可以随意放 				  *
	 *	   	[如果处理的书 呼入 的情况]，在接通后，两个通道的传递顺序不能修改，第一个通道只能是被叫分机对应的通道，而第二个通道必须是主叫对应的通道  *
	 *						        如果呼入不按上面的要求进行传递通道参数，在两个通道都进入会议室后，大概在25秒后，被叫分机将自动挂断                *
	 *	另外：该功能目前只能应用asterisk-1.8.25及其以上版本，对于上面呼入的情况，应该是asterisk-1.8.25的一个bug。                                       *
	 *		如果呼入，并且使用的asterisk 版本低于V1.8.25，那无论怎么传递通道，在两个通道进入会议室后，过25秒，都会出现自动挂断的情况            *
	 *																								  * 
	 * *************************************************************************************************
	 *
	 * @param channel		通道1
	 * @param extraChannel	通道2
	 * @param extension		要转接到的extension
	 * @return boolean  	如果转接成功，则返回true
	 */
	public boolean redirectDoubleChannels(String channel, String extraChannel, String extension);
	
}
