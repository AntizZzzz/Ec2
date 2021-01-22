package com.jiangyifen.ec2.bean;

public class EmployeeStatusEntity {
	
	private Long userId;
	public static final String USERID_HEADER = "用户Id";
	
	private String username = "";		// 用户名
	public static final String USERNAME_HEADER = "用户名";
	
	private String realName = "";		// 姓名
	public static final String REALNAME_HEADER = "姓名";
	
	private String empNo = "";			// 工号
	public static final String EMPNO_HEADER = "工号";
	
	private Boolean isLogin;			// sip 号
	public static final String ISLOGIN_HEADER = "登陆状态";
	
	private String interfaze = "";		// sip 号
	public static final String INTERFAZE_HEADER = "分机号";
	
	private String interfazeRegisterStatus = "";
	public static final String INTERFAZE_REGISTER_STATUS_HEADER = "分机状态";
	
	private String callStatus = "";		// 通话状态
	public static final String CALLSTATUS_HEADER = "通话状态";
	
	private String callerIdNum = "";	// 主叫号码
	public static final String CALLERIDNUM_HEADER = "主叫号码";
	
	private String connectedLineNum = "";// 被叫号码
	public static final String CONNECTEDLINENUM_HEADER = "被叫号码";
	
	private Integer callSeconds;		// 通话时长
	public static final String CALLSECONDS_HEADER = "通话时长(秒)";
	
	private Long departmentId;			// 所属部门id值
	public static final String DEPARTMENTID_HEADER = "部门id";
	
	private String deptName = "";		// 部门名称
	public static final String DEPTNAME_HEADER = "部门名称";
	
	public Long getUserId() {
		return userId;
	}
	public void setUserId(Long userId) {
		this.userId = userId;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getRealName() {
		return realName;
	}
	public void setRealName(String realName) {
		this.realName = realName;
	}
	public String getEmpNo() {
		return empNo;
	}
	public void setEmpNo(String empNo) {
		this.empNo = empNo;
	}
	public Boolean getIsLogin() {
		return isLogin;
	}
	public void setIsLogin(Boolean isLogin) {
		this.isLogin = isLogin;
	}
	public String getInterfaze() {
		return interfaze;
	}
	public void setInterfaze(String interfaze) {
		this.interfaze = interfaze;
	}
	public String getInterfazeRegisterStatus() {
		return interfazeRegisterStatus;
	}
	public void setInterfazeRegisterStatus(String interfazeRegisterStatus) {
		this.interfazeRegisterStatus = interfazeRegisterStatus;
	}
	public String getCallStatus() {
		return callStatus;
	}
	public void setCallStatus(String callStatus) {
		this.callStatus = callStatus;
	}
	public String getCallerIdNum() {
		return callerIdNum;
	}
	public void setCallerIdNum(String callerIdNum) {
		this.callerIdNum = callerIdNum;
	}
	public String getConnectedLineNum() {
		return connectedLineNum;
	}
	public void setConnectedLineNum(String connectedLineNum) {
		this.connectedLineNum = connectedLineNum;
	}
	public Integer getCallSeconds() {
		return callSeconds;
	}
	public void setCallSeconds(Integer callSeconds) {
		this.callSeconds = callSeconds;
	}
	public Long getDepartmentId() {
		return departmentId;
	}
	public void setDepartmentId(Long departmentId) {
		this.departmentId = departmentId;
	}
	public String getDeptName() {
		return deptName;
	}
	public void setDeptName(String deptName) {
		this.deptName = deptName;
	}
	@Override
	public String toString() {
		return "EmployeeStatusEntity [userId=" + userId + ", username="
				+ username + ", realName=" + realName + ", empNo=" + empNo
				+ ", interfaze=" + interfaze + ", callStatus=" + callStatus
				+ ", callerIdNum=" + callerIdNum + ", connectedLineNum="
				+ connectedLineNum + ", callSeconds=" + callSeconds
				+ ", departmentId=" + departmentId + ", deptName=" + deptName
				+ "]";
	}
	
}
