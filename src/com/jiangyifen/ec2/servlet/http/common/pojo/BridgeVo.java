package com.jiangyifen.ec2.servlet.http.common.pojo;

/**
 * @Description 描述信息: 接起信息 
 *
 * @auther jinht
 *
 * @date 2015-11-25 下午2:35:02
 */
public class BridgeVo {

	private String phoneNumber;			// 电话号码
	private String outline;				// 外线
	private long createTime;				// 接起时间
	private String exten;					// 分机
	private String destination;			// 呼叫方向
	
	
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
	public String getDestination() {
		return destination;
	}
	public void setDestination(String destination) {
		this.destination = destination;
	}
	
	@Override
	public String toString() {
		return "BridgeVo [phoneNumber=" + phoneNumber + ", outline=" + outline
				+ ", createTime=" + createTime + ", exten=" + exten
				+ ", destination=" + destination
				+ "]";
	}
	
}
