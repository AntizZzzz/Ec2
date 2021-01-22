package com.jiangyifen.ec2.bean;

public class SipStatusEntity {

	private String interfaze;
	public static final String INTERFAZE_HEADER = "分机号";

	private String usingOutline;
	public static final String USINGOUTLINE_HEADER = "使用外线";

	private String outlineSource;
	public static final String OUTLINESOURCE_HEADER = "外线来源";
	
	private String registerStatus;
	public static final String REGISTERSTATUS_HEADER = "注册状态";
	
	private String callStatus;
	public static final String CALLSTATUS_HEADER = "通话状态";
	
	private String registedIp;
	public static final String REGISTEDIP_HEADER = "注册ip";
	
	private String port;
	public static final String PORT_HEADER = "使用端口";
	
	private String loginStatus;
	public static final String LOGINSTATUS_HEADER = "使用状态";

	public String getInterfaze() {
		return interfaze;
	}

	public void setInterfaze(String interfaze) {
		this.interfaze = interfaze;
	}

	public String getRegisterStatus() {
		return registerStatus;
	}

	public String getUsingOutline() {
		return usingOutline;
	}

	
	public String getOutlineSource() {
		return outlineSource;
	}

	public void setOutlineSource(String outlineSource) {
		this.outlineSource = outlineSource;
	}

	public void setUsingOutline(String usingOutline) {
		this.usingOutline = usingOutline;
	}

	public void setRegisterStatus(String registerStatus) {
		this.registerStatus = registerStatus;
	}

	public String getCallStatus() {
		return callStatus;
	}

	public void setCallStatus(String callStatus) {
		this.callStatus = callStatus;
	}

	public String getRegistedIp() {
		return registedIp;
	}

	public void setRegistedIp(String registedIp) {
		this.registedIp = registedIp;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getLoginStatus() {
		return loginStatus;
	}

	public void setLoginStatus(String loginStatus) {
		this.loginStatus = loginStatus;
	}

	@Override
	public String toString() {
		return "SipStatusEntity [interfaze=" + interfaze + ", registerStatus="
				+ registerStatus + ", callStatus=" + callStatus
				+ ", registedIp=" + registedIp + ", port=" + port
				+ ", loginStatus=" + loginStatus + "]";
	}
	
}
