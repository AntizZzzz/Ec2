package com.jiangyifen.ec2.report.pojo;

public class AutoDialDetailPo {

	private String dept;                        //部门
	private String empno;                       //工号
	private String name;                        //姓名
	private int connectCount;                   //接通数量
	private String callTimeLength;              //通话时长
	private String avgCallTimeLength;           //平均通话时长 
	private String answerTimeLength;            //接电话用时
	private String avgAnswerTimeLength;         //平均接电话用时
	public String getDept() {
		return dept;
	}
	public void setDept(String dept) {
		this.dept = dept;
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
	public int getConnectCount() {
		return connectCount;
	}
	public void setConnectCount(int connectCount) {
		this.connectCount = connectCount;
	}
	public String getCallTimeLength() {
		return callTimeLength;
	}
	public void setCallTimeLength(String callTimeLength) {
		this.callTimeLength = callTimeLength;
	}
	public String getAvgCallTimeLength() {
		return avgCallTimeLength;
	}
	public void setAvgCallTimeLength(String avgCallTimeLength) {
		this.avgCallTimeLength = avgCallTimeLength;
	}
	public String getAnswerTimeLength() {
		return answerTimeLength;
	}
	public void setAnswerTimeLength(String answerTimeLength) {
		this.answerTimeLength = answerTimeLength;
	}
	public String getAvgAnswerTimeLength() {
		return avgAnswerTimeLength;
	}
	public void setAvgAnswerTimeLength(String avgAnswerTimeLength) {
		this.avgAnswerTimeLength = avgAnswerTimeLength;
	}
	
	
}
