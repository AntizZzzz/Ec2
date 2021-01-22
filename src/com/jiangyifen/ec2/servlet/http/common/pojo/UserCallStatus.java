package com.jiangyifen.ec2.servlet.http.common.pojo;

/**
 * @Description 描述：坐席呼叫状态包装类
 *
 * @author  JRH
 * @date    2014年8月12日 下午2:52:14
 */
public class UserCallStatus {

	private String username;
	
	/**
	 * 状态码：
	 * 
	 * 	statusCode = 1 ： 呼叫中
	 * 	statusCode = 2 ：通话中
	 * 	statusCode = 3 ：无通话
	 * 	statusCode = -1 ：用户不在线 
	 * 	statusCode = -2 ：分机不可用 
	 * 	statusCode = -100 ：未知状态 
	 */
	private Integer statusCode;
	
	private String destcription;// 描述信息
	
	private String srcNum;		// 主叫号码
	
	private String destNum;		// 被叫号码
	
	private String direction;	// 呼叫方向
	
	private Integer duration; 	// 【从通道产生开始计时】如果是界面呼出：包括摘机这段时间

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public Integer getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(Integer statusCode) {
		this.statusCode = statusCode;
	}

	public String getDestcription() {
		return destcription;
	}

	public void setDestcription(String destcription) {
		this.destcription = destcription;
	}

	public String getSrcNum() {
		return srcNum;
	}

	public void setSrcNum(String srcNum) {
		this.srcNum = srcNum;
	}

	public String getDestNum() {
		return destNum;
	}

	public void setDestNum(String destNum) {
		this.destNum = destNum;
	}

	public String getDirection() {
		return direction;
	}

	public void setDirection(String direction) {
		this.direction = direction;
	}

	public Integer getDuration() {
		return duration;
	}

	public void setDuration(Integer duration) {
		this.duration = duration;
	}

	@Override
	public String toString() {
		return "UserCallStatus [username=" + username + ", statusCode=" + statusCode + ", destcription=" + destcription + ", srcNum=" + srcNum + ", destNum=" + destNum + ", direction=" + direction + ", duration=" + duration + "]";
	}
	
}
