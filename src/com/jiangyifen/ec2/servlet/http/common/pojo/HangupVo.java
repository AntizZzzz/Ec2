package com.jiangyifen.ec2.servlet.http.common.pojo;

/**
 * @Description 描述信息: 挂断信息 
 *
 * @auther jinht
 *
 * @date 2015-11-25 下午2:34:37
 */
public class HangupVo {

	private String phoneNumber;			// 电话号码
	private String outline;				// 外线
	private long createTime;				// 挂断时间
	private String exten;					// 分机
	private String username;				// 座席用户名
	private String destination;			// 呼叫方向
	private String uniqueId;				// 录音唯一标示
	
	
	public String getPhoneNumber() {
		return phoneNumber;
	}
	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}
	public String getOutline() {
		return outline;
	}
	public void setOutline(String outline) {
		this.outline = outline;
	}
	public long getCreateTime() {
		return createTime;
	}
	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}
	public String getExten() {
		return exten;
	}
	public void setExten(String exten) {
		this.exten = exten;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getDestination() {
		return destination;
	}
	public void setDestination(String destination) {
		this.destination = destination;
	}
	public String getUniqueId() {
		return uniqueId;
	}
	public void setUniqueId(String uniqueId) {
		this.uniqueId = uniqueId;
	}
	
	@Override
	public String toString() {
		return "HangupVo [phoneNumber=" + phoneNumber + ", outline=" + outline
				+ ", createTime=" + createTime + ", exten=" + exten
				+ ", username=" + username + ", destination=" + destination
				+ ", uniqueId=" + uniqueId + "]";
	}
	
}
