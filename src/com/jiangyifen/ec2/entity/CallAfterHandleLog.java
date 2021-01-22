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

/**
 * @Description 描述：话后处理日志
 *
 * @author  JRH
 * @date    2014年6月24日 上午10:58:19
 * @version v1.0.0
 */
@Entity
@Table(name = "ec2_call_after_handle_log")
public class CallAfterHandleLog {
	
	public static final String DIRECTION_OUT = "outgoing";
	
	public static final String DIRECTION_IN = "incoming";
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "call_after_handle_log")
	@SequenceGenerator(name = "call_after_handle_log", sequenceName = "seq_ec2_call_after_handle_log_id", allocationSize = 1)
	private Long id;			// ID

	@Column(columnDefinition="bigint")
	private Long userId;		// 用户编号
	
	@Size(min = 0, max = 64)
	@Column(nullable = false, columnDefinition="character varying(64) NOT NULL")
	private String username;	// 用户名

	@Column(columnDefinition="bigint")
	private Long deptId;		// 部门id
	
	@Size(min = 0, max = 64)
	@Column(columnDefinition="character varying(64)")
	private String deptName; 	// 部门名称

	@NotNull
	@Column(columnDefinition="bigint")
	private Long domainId;		// 域的id号
	
	@Size(min = 0, max = 64)
	@Column(nullable = false, columnDefinition="character varying(64) NOT NULL")
	private String exten;		// 分机
	
	@Size(min = 0, max = 128)
	@Column(columnDefinition="character varying(128)")
	private String direction;	// 话后处理的方向
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(columnDefinition="TIMESTAMP WITH TIME ZONE")
	private Date startDate;		// 开始时间
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(columnDefinition="TIMESTAMP WITH TIME ZONE")
	private Date finishDate;	// 结束时间

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
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

	public String getExten() {
		return exten;
	}

	public void setExten(String exten) {
		this.exten = exten;
	}

	public String getDirection() {
		return direction;
	}

	public void setDirection(String direction) {
		this.direction = direction;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getFinishDate() {
		return finishDate;
	}

	public void setFinishDate(Date finishDate) {
		this.finishDate = finishDate;
	}
	
}
