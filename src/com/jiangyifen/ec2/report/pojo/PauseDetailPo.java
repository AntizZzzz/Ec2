package com.jiangyifen.ec2.report.pojo;


public class PauseDetailPo {
	
	private String dept;//部门
	private String username;//用户名
	private String queue;//队列名
	private Long  pauseTimeTotal;// 置忙总时间
	
	public String getDept() {
		return dept;
	}
	public void setDept(String dept) {
		this.dept = dept;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getQueue() {
		return queue;
	}
	public void setQueue(String queue) {
		this.queue = queue;
	}
	public Long getPauseTimeTotal() {
		return pauseTimeTotal;
	}
	public void setPauseTimeTotal(Long pauseTimeTotal) {
		this.pauseTimeTotal = pauseTimeTotal;
	}
	@Override
	public String toString() {
		return "PauseDetailPo [dept=" + dept + ", username=" + username + ", queue=" + queue
				+ ", pauseTimeTotal=" + pauseTimeTotal + "]";
	}
	
}
