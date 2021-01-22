package com.jiangyifen.ec2.servlet.http.common.pojo;

/**
 * @Description 描述：用户与分机的绑定信息
 *
 * @author  JRH
 * @date    2014年8月12日 下午1:21:25
 */
public class UserBindingInfo {
	
	private String username;	// 用户名
	
	private String exten;		// 绑定的分机

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getExten() {
		return exten;
	}

	public void setExten(String exten) {
		this.exten = exten;
	}

	@Override
	public String toString() {
		return "UserBindingInfo [username=" + username + ", exten=" + exten + "]";
	}
	
}
