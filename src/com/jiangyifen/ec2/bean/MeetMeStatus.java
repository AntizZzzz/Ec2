package com.jiangyifen.ec2.bean;

/**
 * chb copy from ec
 * @author chb
 *
 */
public class MeetMeStatus {
	
	private int position;
	private String callerIdNum;
	private String callerIdName;
	private String channel;
	private Boolean isMute;
	private String roverStatus;
	private String role;
	private String time;
	
	private String name;
	
	
	public int getPosition() {
		return position;
	}
	public void setPosition(int position) {
		this.position = position;
	}
	public String getCallerIdNum() {
		return callerIdNum;
	}
	public void setCallerIdNum(String callerIdNum) {
		this.callerIdNum = callerIdNum;
	}
	public String getCallerIdName() {
		return callerIdName;
	}
	public void setCallerIdName(String callerIdName) {
		this.callerIdName = callerIdName;
	}
	public String getChannel() {
		return channel;
	}
	public void setChannel(String channel) {
		this.channel = channel;
	}
	public Boolean getIsMute() {
		return isMute;
	}
	public void setIsMute(Boolean isMute) {
		this.isMute = isMute;
	}
	public String getRoverStatus() {
		return roverStatus;
	}
	public void setRoverStatus(String roverStatus) {
		this.roverStatus = roverStatus;
	}
	public String getRole() {
		return role;
	}
	public void setRole(String role) {
		this.role = role;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getName() {
		return name;
	}
}