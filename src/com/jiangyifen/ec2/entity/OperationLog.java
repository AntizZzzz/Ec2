package com.jiangyifen.ec2.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.jiangyifen.ec2.entity.enumtype.OperationStatus;

@Entity
@Table(name = "ec2_operationlog")
public class OperationLog { //操作日志
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "operationlog")
	@SequenceGenerator(name = "operationlog", sequenceName = "seq_ec2_operationlog_id", allocationSize = 1)
	private Long id; // Id

	@Size(min = 0, max = 128)
	@Column(columnDefinition="character varying(128)")
	private String username;	// 用户名

	@Size(min = 0, max = 128)
	@Column(columnDefinition="character varying(128)")
	private String realName;	// 真实姓名

	@Size(min = 0, max = 128)
	@Column(columnDefinition="character varying(128)")
	private String ip;	//IP地址
	
	//============操作时间===============//
	@Temporal(TemporalType.TIMESTAMP)
	@Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
	private Date operateDate; // 操作时间
		
	@Enumerated
	private OperationStatus operationStatus; // 目前只有是导出操作还是导入操作

	@Size(min = 0, max = 512)
	@Column(columnDefinition="character varying(512)")
	private String description; // 对于导的是哪里的数据的描述

	@Size(min = 0, max = 2048)
	@Column(columnDefinition="character varying(4096)")
	private String programmerSee; //我们维护时查看的字段
	
	@Size(min = 0, max = 512)
	@Column(columnDefinition="character varying(512)")
	private String filePath;	// 文件路径
	
	
	//域的概念 批次n-->1域
	//域不能为空
	@NotNull
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = Domain.class)
	private Domain domain;
	
	// ================= getter 和 setter 方法=================//
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getRealName() {
		return realName;
	}

	public void setRealName(String realName) {
		this.realName = realName;
	}

	public Date getOperateDate() {
		return operateDate;
	}

	public void setOperateDate(Date operateDate) {
		this.operateDate = operateDate;
	}

	public OperationStatus getOperationStatus() {
		return operationStatus;
	}

	public void setOperationStatus(OperationStatus operationStatus) {
		this.operationStatus = operationStatus;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public Domain getDomain() {
		return domain;
	}

	public void setDomain(Domain domain) {
		this.domain = domain;
	}

	public void setIp(String ip) {
		this.ip=ip;
	}

	public String getProgrammerSee() {
		return programmerSee;
	}

	public void setProgrammerSee(String programmerSee) {
		this.programmerSee = programmerSee;
	}

	public String getIp() {
		return ip;
	}
	

}
