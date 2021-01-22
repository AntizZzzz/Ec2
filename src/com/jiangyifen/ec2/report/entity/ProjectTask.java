package com.jiangyifen.ec2.report.entity;

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
import javax.validation.constraints.Size;
/**
 * 关于Task表的报表统计用表 
 * @author chb
 */
@Entity
@Table(name = "ec2_project_task")
// 呼入电话数量统计
public class ProjectTask {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "project_task_count")
	@SequenceGenerator(name = "project_task", sequenceName = "seq_ec2_project_task_id", allocationSize = 1)
	private Long id;
	
	@Column(nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date date; // 日期
	
	//任务状态
	@Size(min = 0, max = 32)
	@Column(columnDefinition="character varying(32)")
	private String status;
	
	// 呼叫是否接通   默认 false 为应答
	@Column(columnDefinition="boolean not null default false", nullable = false)
	private Boolean isFinished;

	// 呼叫是否接通   默认 false 为应答
	@Column(columnDefinition="boolean not null default false", nullable = false)
	private Boolean isAnswered;
	
	//用户Id
	private Long user_id;

	//条数
	private Long count;
	
	private Long domain_id; // 域
	
	//构造器
	public ProjectTask() {
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public Boolean getIsFinished() {
		return isFinished;
	}

	public void setIsFinished(Boolean isFinished) {
		this.isFinished = isFinished;
	}

	public Long getUser_id() {
		return user_id;
	}

	public void setUser_id(Long user_id) {
		this.user_id = user_id;
	}

	public Long getCount() {
		return count;
	}

	public void setCount(Long count) {
		this.count = count;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Boolean getIsAnswered() {
		return isAnswered;
	}

	public void setIsAnswered(Boolean isAnswered) {
		this.isAnswered = isAnswered;
	}

	public Long getDomain_id() {
		return domain_id;
	}

	public void setDomain_id(Long domain_id) {
		this.domain_id = domain_id;
	}
}
