package com.jiangyifen.ec2.service.csr.ami;

/**
 * 电话挂断服务接口
 */
public interface HangupService {

	/**
	 * 挂断电话[通过HangupAction]
	 * @param channel 需挂断的通道
	 */
	public void hangup(String channel);
	
	/**
	 * 挂断电话[通过CommandAction]
	 * @param channel 需挂断的通道
	 */
	public void hangupByCommand(String channel);
	
}
