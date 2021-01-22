package com.jiangyifen.ec2.report.pojo;

public class CallCheckPo_bak_zhongdao {

	private String empno;
	private String name;
	private String dept;
	private int callTotalCount;// 呼叫总数
	private int callTotalTimeLength;// 呼叫总时长
	private int callAvgTimeLength;// 呼叫平均时长
	private int callConnectCount;// 呼叫接通数量
	private int callConnectTimeLength;// 呼叫接通时长
	private int callAvgConnectTimeLength;// 呼叫平均接通时长
	private int callUnconnectCount;// 呼叫未接通数量
	private int callUnconnectTimeLength;// 呼叫未接通时长;
	private int callRingTimeLength;// 振铃时长
	private int callAvgRingTimeLength;// 平均振铃时长

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

	public int getCallTotalTimeLength() {
		return callTotalTimeLength;
	}

	public void setCallTotalTimeLength(int callTotalTimeLength) {
		this.callTotalTimeLength = callTotalTimeLength;
	}

	public int getCallAvgTimeLength() {
		return callAvgTimeLength;
	}

	public void setCallAvgTimeLength(int callAvgTimeLength) {
		this.callAvgTimeLength = callAvgTimeLength;
	}

	public int getCallConnectCount() {
		return callConnectCount;
	}

	public void setCallConnectCount(int callConnectCount) {
		this.callConnectCount = callConnectCount;
	}

	public int getCallConnectTimeLength() {
		return callConnectTimeLength;
	}

	public void setCallConnectTimeLength(int callConnectTimeLength) {
		this.callConnectTimeLength = callConnectTimeLength;
	}

	public int getCallAvgConnectTimeLength() {
		return callAvgConnectTimeLength;
	}

	public void setCallAvgConnectTimeLength(int callAvgConnectTimeLength) {
		this.callAvgConnectTimeLength = callAvgConnectTimeLength;
	}

	public int getCallUnconnectCount() {
		return callUnconnectCount;
	}

	public void setCallUnconnectCount(int callUnconnectCount) {
		this.callUnconnectCount = callUnconnectCount;
	}

	public int getCallUnconnectTimeLength() {
		return callUnconnectTimeLength;
	}

	public void setCallUnconnectTimeLength(int callUnconnectTimeLength) {
		this.callUnconnectTimeLength = callUnconnectTimeLength;
	}

	public int getCallRingTimeLength() {
		return callRingTimeLength;
	}

	public void setCallRingTimeLength(int callRingTimeLength) {
		this.callRingTimeLength = callRingTimeLength;
	}

	public int getCallAvgRingTimeLength() {
		return callAvgRingTimeLength;
	}

	public void setCallAvgRingTimeLength(int callAvgRingTimeLength) {
		this.callAvgRingTimeLength = callAvgRingTimeLength;
	}

}
