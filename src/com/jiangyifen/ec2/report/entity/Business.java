package com.jiangyifen.ec2.report.entity;

public class Business {

	private String projectName;// 项目名
	private String username;// 用户名
	private String name;// 姓名
	private String dept;// 部门
	private int projectTaskCount;// 接收土豆数
	private int finishedTaskCount;// 呼出土豆数
	private int answeredTaskCount;// 接通土豆数
	private int unansweredTaskCount;// 未接通土豆数
	private String answeredTaskRate;// 土豆接通率
	private int unfinishedTaskCount;// 未处理土豆数
	private int refuseTaskCount;// 拒绝数
	private int orderTrackCount;// 预约跟踪数
	private int orderCustomerCount;// 预约开户数
	private int successCustomerCount;// 成功开户数
	private int successPotatoCount;// 成功土豆数
	private int cdrConnectCount;// cdr中接通的数量
	private String conversionRate;// 转化率

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDept() {
		return dept;
	}

	public void setDept(String dept) {
		this.dept = dept;
	}

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public int getProjectTaskCount() {
		return projectTaskCount;
	}

	public void setProjectTaskCount(int projectTaskCount) {
		this.projectTaskCount = projectTaskCount;
	}

	public int getFinishedTaskCount() {
		return finishedTaskCount;
	}

	public void setFinishedTaskCount(int finishedTaskCount) {
		this.finishedTaskCount = finishedTaskCount;
	}

	public int getAnsweredTaskCount() {
		return answeredTaskCount;
	}

	public void setAnsweredTaskCount(int answeredTaskCount) {
		this.answeredTaskCount = answeredTaskCount;
	}

	public int getUnansweredTaskCount() {
		return unansweredTaskCount;
	}

	public void setUnansweredTaskCount(int unansweredTaskCount) {
		this.unansweredTaskCount = unansweredTaskCount;
	}

	public String getAnsweredTaskRate() {
		return answeredTaskRate;
	}

	public void setAnsweredTaskRate(String answeredTaskRate) {
		this.answeredTaskRate = answeredTaskRate;
	}

	public int getUnfinishedTaskCount() {
		return unfinishedTaskCount;
	}

	public void setUnfinishedTaskCount(int unfinishedTaskCount) {
		this.unfinishedTaskCount = unfinishedTaskCount;
	}

	public int getRefuseTaskCount() {
		return refuseTaskCount;
	}

	public void setRefuseTaskCount(int refuseTaskCount) {
		this.refuseTaskCount = refuseTaskCount;
	}

	public int getOrderTrackCount() {
		return orderTrackCount;
	}

	public void setOrderTrackCount(int orderTrackCount) {
		this.orderTrackCount = orderTrackCount;
	}

	public int getOrderCustomerCount() {
		return orderCustomerCount;
	}

	public void setOrderCustomerCount(int orderCustomerCount) {
		this.orderCustomerCount = orderCustomerCount;
	}

	public int getSuccessCustomerCount() {
		return successCustomerCount;
	}

	public void setSuccessCustomerCount(int successCustomerCount) {
		this.successCustomerCount = successCustomerCount;
	}

	public int getSuccessPotatoCount() {
		return successPotatoCount;
	}

	public void setSuccessPotatoCount(int successPotatoCount) {
		this.successPotatoCount = successPotatoCount;
	}

	public String getConversionRate() {
		return conversionRate;
	}

	public void setConversionRate(String conversionRate) {
		this.conversionRate = conversionRate;
	}

	public int getCdrConnectCount() {
		return cdrConnectCount;
	}

	public void setCdrConnectCount(int cdrConnectCount) {
		this.cdrConnectCount = cdrConnectCount;
	}

}
