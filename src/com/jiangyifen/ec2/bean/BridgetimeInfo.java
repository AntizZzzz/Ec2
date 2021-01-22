package com.jiangyifen.ec2.bean;

import java.util.Date;
/**
 * 为了修改BillSeconds，存储一个对象
 * jrh 可以删掉了 用 ChannelLifeCycle 代替
 * @author chb
 */
public class BridgetimeInfo {
	private String channel;
	private Date bridgeTime;

	/**
	 * 构造器
	 */
	public BridgetimeInfo() {
	}
	
	/**
	 * 重载构造器
	 * @param channel
	 * @param bridgeTime
	 */
	public BridgetimeInfo(String channel,Date bridgeTime) {
		this.channel=channel;
		this.bridgeTime=bridgeTime;
	}
	
	public String getChannel() {
		return channel;
	}
	public void setChannel(String channel) {
		this.channel = channel;
	}
	public Date getBridgeTime() {
		return bridgeTime;
	}
	public void setBridgeTime(Date bridgeTime) {
		this.bridgeTime = bridgeTime;
	}

	@Override
	public String toString() {
		return "BridgetimeInfo [channel=" + channel + ", bridgeTime="
				+ bridgeTime + "]";
	}
}
