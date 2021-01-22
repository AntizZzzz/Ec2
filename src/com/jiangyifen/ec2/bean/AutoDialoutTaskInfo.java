package com.jiangyifen.ec2.bean;

public class AutoDialoutTaskInfo {
	//项目信息
	private String projectName;
	//自动外呼信息
	private String autodialoutName;
	//外线信息
	private String outline;
	private Integer outlineCocurrent;
	private Integer outlineCapability;
	//CSR信息
	private Integer csrAvailable;
	private Integer csrLoggedIn;
	//队列信息
	private String queueName;
	private Integer callersCount;
	private Integer queueDepth;
	
	public AutoDialoutTaskInfo() {
	}
	
	public String getProjectName() {
		return projectName;
	}
	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}
	public String getAutodialoutName() {
		return autodialoutName;
	}
	public void setAutodialoutName(String autodialoutName) {
		this.autodialoutName = autodialoutName;
	}
	public String getOutline() {
		return outline;
	}
	public void setOutline(String outline) {
		this.outline = outline;
	}
	public Integer getOutlineCocurrent() {
		return outlineCocurrent;
	}
	public void setOutlineCocurrent(Integer outlineCocurrent) {
		this.outlineCocurrent = outlineCocurrent;
	}
	
	public Integer getOutlineCapability() {
		return outlineCapability;
	}

	public void setOutlineCapability(Integer outlineCapability) {
		this.outlineCapability = outlineCapability;
	}

	public Integer getCsrAvailable() {
		return csrAvailable;
	}
	public void setCsrAvailable(Integer csrAvailable) {
		this.csrAvailable = csrAvailable;
	}
	public Integer getCsrLoggedIn() {
		return csrLoggedIn;
	}
	public void setCsrLoggedIn(Integer csrLoggedIn) {
		this.csrLoggedIn = csrLoggedIn;
	}
	public String getQueueName() {
		return queueName;
	}
	public void setQueueName(String queueName) {
		this.queueName = queueName;
	}
	public Integer getCallersCount() {
		return callersCount;
	}
	public void setCallersCount(Integer callersCount) {
		this.callersCount = callersCount;
	}
	public Integer getQueueDepth() {
		return queueDepth;
	}
	public void setQueueDepth(Integer queueDepth) {
		this.queueDepth = queueDepth;
	}
}
