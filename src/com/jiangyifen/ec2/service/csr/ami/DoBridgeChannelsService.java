package com.jiangyifen.ec2.service.csr.ami;

/**
 * 直接让两个通道建立通话
 * @author jrh
 *  2013-7-10
 */
public interface DoBridgeChannelsService {

	/**
	 * 让两个通道建立连接
	 * @param channel1		通道1
	 * @param channel2		通道2
	 */
	public boolean doBridgeChannels(String channel1, String channel2);
	
	/**
	 * 让两个通道建立连接
	 * @param channel1		通道1
	 * @param channel2		通道2
	 * @param tone			给通道2发起友好的接通音
	 */
	public boolean doBridgeChannels(String channel1, String channel2, Boolean tone);
	
}
