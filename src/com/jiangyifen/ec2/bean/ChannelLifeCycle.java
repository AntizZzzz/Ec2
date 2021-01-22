package com.jiangyifen.ec2.bean;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 记录通道的整个生命周期达到各个状态的时间
 *	channel state
 *
 * @author jrh
 *  2013-8-30
 */
public class ChannelLifeCycle {
	
	private String selfChannel;				// 自身通道
	private String selfUniqueid;			// 自身通道唯一标识
	
	private String bridgedChannel;			// 建立连接后对方通道
	private String bridgedUniqueid;			// 建立连接后对方通道唯一标识
	
	// 不需要建立通话就能获取的信息
	private String connectedlinenum;		// 在NewStateEvent产生时赋值
	private String connectedlinename;		// 在NewStateEvent产生时赋值
	private String connectedChannel;		// 如果没有接通，目前获取不到，用于备用【如果接通=bridgedChannel】
	private String connectedUniqueid;		// 如果没有接通，目前获取不到，用于备用【如果接通=bridgedUniqueid】

	// 被叫号码
	private String destlinenum;				// 被叫号码： 目前只用来解决呼出时的被叫号码
	
	/* 当前通道变成指定状态的时间  */
	// 目前关注点
	private Date originateDialTime;			// 当前通道发起呼叫的时间  jrh 自己通过agi 统计		// 这个时间点甚至可以考虑不计，因为该时间跟 通道产生的时间几乎一致 downStateTime
	private Date ringStateTime;
	private Date ringingStateTime; 
	private Date upStateTime;
	private Date downStateTime;			// new channel event 发生是，通道的状态就是down
	private Date bridgedTime;			// 当前通道与另一个通道建立通话的时间
	
	// 备用
	private Date dialingStateTime;
	private Date busyStateTime;
	private Date dialingOffhookStateTime;
	private Date preRingStateTime;
	private Date rsrvdStateTime;
	private Date offHookStateTime;
	
	public ChannelLifeCycle(String channel) {
		this.selfChannel = channel;
	}
	
	/**  获取通道信息   */
	public String getSelfChannel() {
		return selfChannel;
	}
	
	/**  设置通道信息   */
	public void setSelfChannel(String channel) {
		this.selfChannel = channel;
	}

	public String getSelfUniqueid() {
		return selfUniqueid;
	}

	public void setSelfUniqueid(String uniqueid) {
		this.selfUniqueid = uniqueid;
	}

	public String getBridgedChannel() {
		return bridgedChannel;
	}

	public void setBridgedChannel(String bridgedChannel) {
		this.bridgedChannel = bridgedChannel;
	}

	public String getBridgedUniqueid() {
		return bridgedUniqueid;
	}

	public void setBridgedUniqueid(String bridgedUniqueid) {
		this.bridgedUniqueid = bridgedUniqueid;
	}

	public String getConnectedlinenum() {
		return connectedlinenum;
	}

	public void setConnectedlinenum(String connectedlinenum) {
		this.connectedlinenum = connectedlinenum;
	}

	public String getConnectedlinename() {
		return connectedlinename;
	}

	public void setConnectedlinename(String connectedlinename) {
		this.connectedlinename = connectedlinename;
	}

	public String getConnectedChannel() {
		return connectedChannel;
	}

	public void setConnectedChannel(String connectedChannel) {
		this.connectedChannel = connectedChannel;
	}

	public String getConnectedUniqueid() {
		return connectedUniqueid;
	}

	public void setConnectedUniqueid(String connectedUniqueid) {
		this.connectedUniqueid = connectedUniqueid;
	}

	public String getDestlinenum() {
		return destlinenum;
	}

	public void setDestlinenum(String destlinenum) {
		this.destlinenum = destlinenum;
	}

	public Date getOriginateDialTime() {
		return originateDialTime;
	}

	public void setOriginateDialTime(Date originateDialTime) {
		this.originateDialTime = originateDialTime;
	}

	public Date getRingStateTime() {
		return ringStateTime;
	}

	public void setRingStateTime(Date ringStateTime) {
		this.ringStateTime = ringStateTime;
	}

	public Date getRingingStateTime() {
		return ringingStateTime;
	}

	public void setRingingStateTime(Date ringingStateTime) {
		this.ringingStateTime = ringingStateTime;
	}

	public Date getUpStateTime() {
		return upStateTime;
	}

	public void setUpStateTime(Date upStateTime) {
		this.upStateTime = upStateTime;
	}

	public Date getDownStateTime() {
		return downStateTime;
	}

	public void setDownStateTime(Date downStateTime) {
		this.downStateTime = downStateTime;
	}
	
	public Date getBridgedTime() {
		return bridgedTime;
	}

	public void setBridgedTime(Date bridgedTime) {
		this.bridgedTime = bridgedTime;
	}

	public Date getRsrvdStateTime() {
		return rsrvdStateTime;
	}

	public void setRsrvdStateTime(Date rsrvdStateTime) {
		this.rsrvdStateTime = rsrvdStateTime;
	}

	public Date getOffHookStateTime() {
		return offHookStateTime;
	}

	public void setOffHookStateTime(Date offHookStateTime) {
		this.offHookStateTime = offHookStateTime;
	}

	public Date getDialingStateTime() {
		return dialingStateTime;
	}

	public void setDialingStateTime(Date dialingStateTime) {
		this.dialingStateTime = dialingStateTime;
	}

	public Date getBusyStateTime() {
		return busyStateTime;
	}

	public void setBusyStateTime(Date busyStateTime) {
		this.busyStateTime = busyStateTime;
	}

	public Date getDialingOffhookStateTime() {
		return dialingOffhookStateTime;
	}

	public void setDialingOffhookStateTime(Date dialingOffhookStateTime) {
		this.dialingOffhookStateTime = dialingOffhookStateTime;
	}

	public Date getPreRingStateTime() {
		return preRingStateTime;
	}

	public void setPreRingStateTime(Date preRingStateTime) {
		this.preRingStateTime = preRingStateTime;
	}

	@Override
	public String toString() {
		return "ChannelLifeCycle [selfChannel=" + selfChannel 
				+ ", selfUniqueid=" + selfUniqueid 
				+ ", bridgedChannel=" + bridgedChannel 
				+ ", bridgedUniqueid=" + bridgedUniqueid 
				+ ", connectedlinenum=" + connectedlinenum
				+ ", connectedlinename=" + connectedlinename
				+ ", connectedChannel=" + connectedChannel
				+ ", connectedUniqueid=" + connectedUniqueid
				+ ", destlinenum=" + destlinenum
				+ ", originateDialTime=" + fd(originateDialTime)
				+ ", ringStateTime=" + fd(ringStateTime)
				+ ", ringingStateTime=" + fd(ringingStateTime) 
				+ ", upStateTime=" + fd(upStateTime) 
				+ ", bridgedTime=" + fd(bridgedTime) 
				+ ", downStateTime=" + fd(downStateTime)
				+ ", rsrvdStateTime=" + fd(rsrvdStateTime) 
				+ ", offHookStateTime=" + fd(offHookStateTime) 
				+ ", dialingStateTime=" + fd(dialingStateTime)
				+ ", busyStateTime=" + fd(busyStateTime)
				+ ", dialingOffhookStateTime=" + fd(dialingOffhookStateTime)
				+ ", preRingStateTime=" + fd(preRingStateTime) + "]";
	}
	
	// TODO delte it
	public String fd(Date date) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		if(date != null) {
			return sdf.format(date);
		}
		return null;
	}

}
