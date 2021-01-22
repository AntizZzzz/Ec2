package com.jiangyifen.ec2.ui.admin.tableinfo.pojo.vo;

/**
 * 线程显示信息视图对象
 * @author JHT
 */
public class ThreadViewVo{

	private Long id;				// 线程的ID
	private String name;			// 线程的名称
	private String state;			// 线程的运行状态
	private String isDaemon;		// 是否为后台线程
	
 	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public String getIsDaemon() {
		return isDaemon;
	}
	public void setIsDaemon(String isDaemon) {
		this.isDaemon = isDaemon;
	}
	
}
