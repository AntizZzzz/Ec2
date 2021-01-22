package com.jiangyifen.ec2.servlet.http.common.pojo;

/**
 * @Description 描述: 弾屏信息
 *  
 * @author jht
 * @date 2015年10月26日 10:53:29
 */
public class PopupIncomingVo {

	private String phoneNumber;		// 电话号码
	private String outline;			// 外线
	private long createTime;			// 创建时间
	private String exten;				// 分机
	private String username;			// 座席用户名
	
	// getter and setter method
	
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
	
	// toString method
	
	@Override
	public String toString() {
		return "PopupIncomingVo [phoneNumber=" + phoneNumber + ", outline="
				+ outline + ", createTime=" + createTime + ", exten=" + exten
				+ ", username=" + username + "]";
	}
	
}
