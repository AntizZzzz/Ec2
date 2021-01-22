package com.jiangyifen.ec2.ui.mgr.resourcemanage;

import java.util.Date;

import com.jiangyifen.ec2.entity.Domain;
 
public class Task {
	
	private Long id;
	
	private String name;
	
	private String type;
	
		
	private String listSql;
	
	private String countSql;
	
	private Long countNum;
	
	private String status;
	
	private String resultMsg;
	
	private Date createDate;	

	private Long createUserId;
	
	private Domain domain;
	
	private String jpqlParams;

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

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getListSql() {
		return listSql;
	}

	public void setListSql(String listSql) {
		this.listSql = listSql;
	}

	public String getCountSql() {
		return countSql;
	}

	public void setCountSql(String countSql) {
		this.countSql = countSql;
	}

	public Long getCountNum() {
		return countNum;
	}

	public void setCountNum(Long countNum) {
		this.countNum = countNum;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getResultMsg() {
		return resultMsg;
	}

	public void setResultMsg(String resultMsg) {
		this.resultMsg = resultMsg;
	}


	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public Long getCreateUserId() {
		return createUserId;
	}

	public void setCreateUserId(Long createUserId) {
		this.createUserId = createUserId;
	}

	public Domain getDomain() {
		return domain;
	}

	public void setDomain(Domain domain) {
		this.domain = domain;
	}

	public String getJpqlParams() {
		return jpqlParams;
	}

	public void setJpqlParams(String jpqlParams) {
		this.jpqlParams = jpqlParams;
	}

	@Override
	public String toString() {
		return "Task [id=" + id + ", name=" + name + ", type=" + type + ", listSql=" + listSql + ", countSql=" + countSql + ", countNum=" + countNum + ", status=" + status + ", resultMsg="
				+ resultMsg + ", createDate=" + createDate + ", createUserId=" + createUserId + ", domain=" + domain + ", jpqlParams=" + jpqlParams + "]";
	}
	
	
}
