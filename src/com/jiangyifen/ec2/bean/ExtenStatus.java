package com.jiangyifen.ec2.bean;

import java.util.HashMap;

/**
 * @author Administrator
 *
 */
public class ExtenStatus {
	//分机名
	private String sipName;
	//IP
	private String ip;
	//端口
	private Integer port;
	//注册状态
	private String registerStatus="UNKNOWN";
	
	// jrh 置忙的队列，及置忙原因
	private HashMap<String, String> pauseQueues2Reason = new HashMap<String, String>();

	// jrh 置闲的队列，及置闲原因
	private HashMap<String, String> unpauseQueues2Reason = new HashMap<String, String>();
	
	public String getSipName() {
		return sipName;
	}
	public void setSipName(String sipName) {
		this.sipName = sipName;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public Integer getPort() {
		return port;
	}
	public void setPort(Integer port) {
		this.port = port;
	}
	public String getRegisterStatus() {
		return registerStatus;
	}
	public void setRegisterStatus(String registerStatus) {
		this.registerStatus = registerStatus;
	}
	public HashMap<String, String> getPauseQueues2Reason() {
		return pauseQueues2Reason;
	}
	public void setPauseQueues2Reason(HashMap<String, String> pauseQueues2Reason) {
		this.pauseQueues2Reason = pauseQueues2Reason;
	}
	public HashMap<String, String> getUnpauseQueues2Reason() {
		return unpauseQueues2Reason;
	}
	public void setUnpauseQueues2Reason(HashMap<String, String> unpauseQueues2Reason) {
		this.unpauseQueues2Reason = unpauseQueues2Reason;
	}
	@Override
	public String toString() {
		return "ExtenStatus [sipName=" + sipName + ", ip=" + ip + ", port="
				+ port + ", registerStatus=" + registerStatus
				+ ", pauseQueues2Reason=" + pauseQueues2Reason
				+ ", unpauseQueues2Reason=" + unpauseQueues2Reason + "]";
	}
	
}
