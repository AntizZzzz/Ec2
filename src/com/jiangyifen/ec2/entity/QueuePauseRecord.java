package com.jiangyifen.ec2.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;


@Entity
@Table(name = "ec2_queue_member_pause_event_log")
public class QueuePauseRecord {
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "queue_member_pause_event_log")
	@SequenceGenerator(name = "queue_member_pause_event_log", sequenceName = "seq_ec2_queue_member_pause_event_log_id", allocationSize = 1)
	private Long id;	// ID
	
	@Size(min = 0, max = 64)
	@Column(nullable = false, columnDefinition="character varying(64) NOT NULL")
	private String username;	// 用户名
	
	@Size(min = 0, max = 64)
	@Column(nullable = false, columnDefinition="character varying(64) NOT NULL")
	private String queue;		// 队列名
	
	@Size(min = 0, max = 64)
	@Column(nullable = false, columnDefinition="character varying(64) NOT NULL")
	private String sipname;		// IP电话名
	
	@Size(min = 0, max = 128)
	@Column(columnDefinition="character varying(128)")
	private String reason;		// 置忙原因
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(columnDefinition="TIMESTAMP WITH TIME ZONE")
	private Date pauseDate;		// 置忙时间
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(columnDefinition="TIMESTAMP WITH TIME ZONE")
	private Date unpauseDate;	// 置闲时间
	
//TODO 添加飞空约束
//	@NotNull
//	@Column(nullable = false, columnDefinition="bigint NOT NULL")
	@Column(columnDefinition="bigint")
	private Long deptId;		// 部门id
	
	@Size(min = 0, max = 64)
	@Column(columnDefinition="character varying(64)")
//	@NotNull
//	@Size(min = 0, max = 64)
//	@Column(nullable = false,columnDefinition="character varying(64) NOT NULL")
	private String deptName; 	// 部门名称

	@NotNull
	@Column(columnDefinition="bigint NOT NULL", nullable = false)
	private Long domainId;		// 域的id号
	
	public Long getId() {
		return id;
	}

	public String getUsername() {
		return username;
	}

	public String getQueue() {
		return queue;
	}

	public String getSipname() {
		return sipname;
	}

	public Date getPauseDate() {
		return pauseDate;
	}

	public Date getUnpauseDate() {
		return unpauseDate;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setQueue(String queue) {
		this.queue = queue;
	}

	public void setSipname(String sipname) {
		this.sipname = sipname;
	}

	public void setPauseDate(Date pauseDate) {
		this.pauseDate = pauseDate;
	}

	public void setUnpauseDate(Date unpauseDate) {
		this.unpauseDate = unpauseDate;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public Long getDeptId() {
		return deptId;
	}

	public void setDeptId(Long deptId) {
		this.deptId = deptId;
	}

	public String getDeptName() {
		return deptName;
	}

	public void setDeptName(String deptName) {
		this.deptName = deptName;
	}

	public Long getDomainId() {
		return domainId;
	}

	public void setDomainId(Long domainId) {
		this.domainId = domainId;
	}
	
}
