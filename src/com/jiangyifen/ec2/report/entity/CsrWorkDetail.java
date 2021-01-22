package com.jiangyifen.ec2.report.entity;

public class CsrWorkDetail {

	private String exten;// 分机号
	private String empno;// 员工工号
	private String name;// 员工姓名
	private String callDirection;// 呼叫方向
	private String callNumber;// 主叫号码
	private String calledNumber;// 被叫号码
	private int ringTimeLength;// 振铃时长
	private String ringDate;// 开始拨打电话振铃的时间
	private String callDate;// 开始通话时间
	private String hungupDate;// 挂断电话的时间

	public String getExten() {
		return exten;
	}

	public void setExten(String exten) {
		this.exten = exten;
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

	public String getCallDirection() {
		return callDirection;
	}

	public void setCallDirection(String callDirection) {
		this.callDirection = callDirection;
	}

	public String getCallNumber() {
		return callNumber;
	}

	public void setCallNumber(String callNumber) {
		this.callNumber = callNumber;
	}

	public String getCalledNumber() {
		return calledNumber;
	}

	public void setCalledNumber(String calledNumber) {
		this.calledNumber = calledNumber;
	}

	public int getRingTimeLength() {
		return ringTimeLength;
	}

	public void setRingTimeLength(int ringTimeLength) {
		this.ringTimeLength = ringTimeLength;
	}

	public String getRingDate() {
		return ringDate;
	}

	public void setRingDate(String ringDate) {
		this.ringDate = ringDate;
	}

	public String getCallDate() {
		return callDate;
	}

	public void setCallDate(String callDate) {
		this.callDate = callDate;
	}

	public String getHungupDate() {
		return hungupDate;
	}

	public void setHungupDate(String hungupDate) {
		this.hungupDate = hungupDate;
	}

}
