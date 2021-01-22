package com.jiangyifen.ec2.report.entity;

public class Customer {

	private String customerEmpno;// 客户编号
	private String projectName;// 项目名
	private String potatoSource;// 土豆来源
	private String username;// 工号
	private String realName;// 姓名
	private String dept;// 部门
	private String telephone;
	private String createDate;// 创建日期
	private String orderDate;// 预约日期
	private String firstCallDate;// 首次拨打时间
	private String secondCallDate;// 第二次拨打时间
	private String thirdCallDate;// 第三次拨打时间
	private String customerStatus;// 客户状态

	public String getCustomerEmpno() {
		return customerEmpno;
	}

	public void setCustomerEmpno(String customerEmpno) {
		this.customerEmpno = customerEmpno;
	}

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public String getPotatoSource() {
		return potatoSource;
	}

	public void setPotatoSource(String potatoSource) {
		this.potatoSource = potatoSource;
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

	public String getDept() {
		return dept;
	}

	public void setDept(String dept) {
		this.dept = dept;
	}

	public String getTelephone() {
		return telephone;
	}

	public void setTelephone(String telephone) {
		this.telephone = telephone;
	}

	public String getCreateDate() {
		return createDate;
	}

	public void setCreateDate(String createDate) {
		this.createDate = createDate;
	}

	public String getOrderDate() {
		return orderDate;
	}

	public void setOrderDate(String orderDate) {
		this.orderDate = orderDate;
	}

	public String getFirstCallDate() {
		return firstCallDate;
	}

	public void setFirstCallDate(String firstCallDate) {
		this.firstCallDate = firstCallDate;
	}

	public String getSecondCallDate() {
		return secondCallDate;
	}

	public void setSecondCallDate(String secondCallDate) {
		this.secondCallDate = secondCallDate;
	}

	public String getThirdCallDate() {
		return thirdCallDate;
	}

	public void setThirdCallDate(String thirdCallDate) {
		this.thirdCallDate = thirdCallDate;
	}

	public String getCustomerStatus() {
		return customerStatus;
	}

	public void setCustomerStatus(String customerStatus) {
		this.customerStatus = customerStatus;
	}

}
