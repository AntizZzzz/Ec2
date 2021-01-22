package com.jiangyifen.ec2.report.entity;

public class Kpi {

	private String projectName;// 项目名
	private String deptName;// 部门
	private String username;// 工号
	private String realName;// 姓名
	private int outgoingTotalCount;// 外呼总数
	private int outgoingConnectCount;// 外呼接通数
	private String outgoingConnectRate;// 外呼接通率
	private int outgoingTotalTimeLength;// 外呼总时长
	private int outgoingAvgTimeLength;// 外呼平均时长
	private int loginTotalTimeLength;// 登入总时长
	private int busyTotalTimeLength;// 置忙总时长
	private int freeTotalTimeLength;// 置闲时长
	private int ringTotalTimeLength;// 振铃总时长
	private String workTotalTimeLengthRate;// 工作时长占比
	private String callTimeLengthRate;// 能聊时长率

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public String getDeptName() {
		return deptName;
	}

	public void setDeptName(String deptName) {
		this.deptName = deptName;
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

	public int getOutgoingTotalCount() {
		return outgoingTotalCount;
	}

	public void setOutgoingTotalCount(int outgoingTotalCount) {
		this.outgoingTotalCount = outgoingTotalCount;
	}

	public int getOutgoingConnectCount() {
		return outgoingConnectCount;
	}

	public void setOutgoingConnectCount(int outgoingConnectCount) {
		this.outgoingConnectCount = outgoingConnectCount;
	}

	public String getOutgoingConnectRate() {
		return outgoingConnectRate;
	}

	public void setOutgoingConnectRate(String outgoingConnectRate) {
		this.outgoingConnectRate = outgoingConnectRate;
	}

	public int getOutgoingTotalTimeLength() {
		return outgoingTotalTimeLength;
	}

	public void setOutgoingTotalTimeLength(int outgoingTotalTimeLength) {
		this.outgoingTotalTimeLength = outgoingTotalTimeLength;
	}

	public int getOutgoingAvgTimeLength() {
		return outgoingAvgTimeLength;
	}

	public void setOutgoingAvgTimeLength(int outgoingAvgTimeLength) {
		this.outgoingAvgTimeLength = outgoingAvgTimeLength;
	}

	public int getLoginTotalTimeLength() {
		return loginTotalTimeLength;
	}

	public void setLoginTotalTimeLength(int loginTotalTimeLength) {
		this.loginTotalTimeLength = loginTotalTimeLength;
	}

	public int getBusyTotalTimeLength() {
		return busyTotalTimeLength;
	}

	public void setBusyTotalTimeLength(int busyTotalTimeLength) {
		this.busyTotalTimeLength = busyTotalTimeLength;
	}

	public int getRingTotalTimeLength() {
		return ringTotalTimeLength;
	}

	public void setRingTotalTimeLength(int ringTotalTimeLength) {
		this.ringTotalTimeLength = ringTotalTimeLength;
	}

	public int getFreeTotalTimeLength() {
		return freeTotalTimeLength;
	}

	public void setFreeTotalTimeLength(int freeTotalTimeLength) {
		this.freeTotalTimeLength = freeTotalTimeLength;
	}

	public String getWorkTotalTimeLengthRate() {
		return workTotalTimeLengthRate;
	}

	public void setWorkTotalTimeLengthRate(String workTotalTimeLengthRate) {
		this.workTotalTimeLengthRate = workTotalTimeLengthRate;
	}

	public String getCallTimeLengthRate() {
		return callTimeLengthRate;
	}

	public void setCallTimeLengthRate(String callTimeLengthRate) {
		this.callTimeLengthRate = callTimeLengthRate;
	}

}
