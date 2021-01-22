package com.jiangyifen.ec2.report.pojo;

import java.util.Date;

public class EmployeeCheckPo {

	private String deptName;// department name
	private String empno;// the empno of csr
	private String name;// the name of csr
	private Date date;// date
	private String busyTimeLength;// how long the csr is busy
	private String loginTimeLength;// how long the csr login
	private String freeTimeLength;// how long the csr is free

	public String getDeptName() {
		return deptName;
	}

	public void setDeptName(String deptName) {
		this.deptName = deptName;
	}

	public String getEmpno() {
		return empno;
	}

	public void setEmpno(String empno) {
		this.empno = empno;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getBusyTimeLength() {
		return busyTimeLength;
	}

	public void setBusyTimeLength(String busyTimeLength) {
		this.busyTimeLength = busyTimeLength;
	}

	public String getLoginTimeLength() {
		return loginTimeLength;
	}

	public void setLoginTimeLength(String loginTimeLength) {
		this.loginTimeLength = loginTimeLength;
	}

	public String getFreeTimeLength() {
		return freeTimeLength;
	}

	public void setFreeTimeLength(String freeTimeLength) {
		this.freeTimeLength = freeTimeLength;
	}

	@Override
	public String toString() {
		return "EmployeeCheckPo [deptName=" + deptName + ", empno=" + empno
				+ ", name=" + name + ", date=" + date + ", busyTimeLength="
				+ busyTimeLength + ", loginTimeLength=" + loginTimeLength
				+ ", freeTimeLength=" + freeTimeLength + "]";
	}

}
