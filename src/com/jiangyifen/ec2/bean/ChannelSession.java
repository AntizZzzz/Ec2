package com.jiangyifen.ec2.bean;

import java.util.Date;


public class ChannelSession {

	private String channel;				// 当前通道
	private String status;				// 当前通道的状态
	private String callerIdNum;			// channel 对应的主叫显示号码
	private String bridgedChannel;		// 与当前通道建立通话的另一通道
	private String connectedlinenum;	// bridgedChannel 对应的手机或分机号
	private boolean isBridged;			// 是否已经打通
	/*************************************************************************************
	 *  asterisk 的 StatusEvent 只能获取到主叫通道的持续时长  statusEvent.getSeconds()
	 *  而被叫的通道的   StatusEvent 如果发生，则获取不到持续时长  
	 *  所以，如果是被叫通道就不能用 seconds 来获取通道持续时长，而用 createDate 来替换
	*************************************************************************************/
	private Integer seconds;			// 持续时长
	// jrh 20130621
	private String channelUniqueId;		// channelUniqueId 的uniqueid
	private String bridgedUniqueId;		// bridgedChannel 的uniqueid
	// jrh 20140416
	private Date createDate;	// channelSession 的创建时间
	
	/*
	 * 对于呼入呼出时，在接通后，将话务员的id存的对方 channelSession 中（目前客户满意度调查需要使用该字段）
	 */
	private Long bridgedUserId;		
	
	public ChannelSession() {
		this.createDate = new Date();
	}
	
	//=============================  Getter and Setter  ========================//
	
	public String getChannel() {
		return channel;
	}
	public void setChannel(String channel) {
		this.channel = channel;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getCallerIdNum() {
		return callerIdNum;
	}
	public void setCallerIdNum(String callerIdNum) {
		this.callerIdNum = callerIdNum;
	}
	public String getBridgedChannel() {
		return bridgedChannel;
	}
	public void setBridgedChannel(String bridgedChannel) {
		this.bridgedChannel = bridgedChannel;
	}
	public String getConnectedlinenum() {
		return connectedlinenum;
	}
	public void setConnectedlinenum(String connectedlinenum) {
		this.connectedlinenum = connectedlinenum;
	}

	public Integer getSeconds() {
		return seconds;
	}
//	TODO
	public void setSeconds(Integer seconds) {
		this.seconds = seconds;
	}
	public boolean isBridged() {
		return isBridged;
	}
	public void setBridged(boolean isBridged) {
		this.isBridged = isBridged;
	}
	
	public Long getBridgedUserId() {
		return bridgedUserId;
	}
	public void setBridgedUserId(Long bridgedUserId) {
		this.bridgedUserId = bridgedUserId;
	}
	
	public String getChannelUniqueId() {
		return channelUniqueId;
	}
	public void setChannelUniqueId(String channelUniqueId) {
		this.channelUniqueId = channelUniqueId;
	}
	public Date getCreateDate() {
		return createDate;
	}
	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}
	public String getBridgedUniqueId() {
		return bridgedUniqueId;
	}
	public void setBridgedUniqueId(String bridgedUniqueId) {
		this.bridgedUniqueId = bridgedUniqueId;
	}

	@Override
	public String toString() {
		return "ChannelSession [channel=" + channel + ", status=" + status
				+ ", callerIdNum=" + callerIdNum + ", bridgedChannel="
				+ bridgedChannel + ", connectedlinenum=" + connectedlinenum
				+ ", isBridged=" + isBridged + ", seconds=" + seconds
				+ ", channelUniqueId=" + channelUniqueId + ", bridgedUniqueId="
				+ bridgedUniqueId + ", createDate=" + createDate
				+ ", bridgedUserId=" + bridgedUserId + "]";
	}
	
}
