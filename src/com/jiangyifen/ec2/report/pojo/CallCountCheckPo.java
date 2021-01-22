package com.jiangyifen.ec2.report.pojo;

public class CallCountCheckPo {

	private String empno;// 工号
	private String name;// 姓名
	private String dept;// 部门
	private int callTotalCount;// 呼叫总数量
	private int callConnectCount;// 呼叫接通数量
	private int callUnconnectCount;// 呼叫未接通数量

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

	public String getDept() {
		return dept;
	}

	public void setDept(String dept) {
		this.dept = dept;
	}

	public int getCallTotalCount() {
		return callTotalCount;
	}

	public void setCallTotalCount(int callTotalCount) {
		this.callTotalCount = callTotalCount;
	}

	public int getCallConnectCount() {
		return callConnectCount;
	}

	public void setCallConnectCount(int callConnectCount) {
		this.callConnectCount = callConnectCount;
	}

	public int getCallUnconnectCount() {
		return callUnconnectCount;
	}

	public void setCallUnconnectCount(int callUnconnectCount) {
		this.callUnconnectCount = callUnconnectCount;
	}

}
