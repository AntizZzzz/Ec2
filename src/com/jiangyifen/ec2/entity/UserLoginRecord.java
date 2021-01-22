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
@Table(name = "ec2_user_login_record")
public class UserLoginRecord {
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_login_record")
	@SequenceGenerator(name = "user_login_record", sequenceName = "seq_ec2_user_login_record_id", allocationSize = 1)
	private Long id;	// 记录ID
	
	@Size(min = 0, max = 64)
	@Column(nullable = false,columnDefinition="character varying(64)")
	private String username;	// 用户昵称
	
	@Size(min = 0, max = 64)
	@Column(columnDefinition="character varying(64)")
	private String realName;	// 用户真实姓名
	
	@Size(min = 0, max = 80)
	@Column(columnDefinition="character varying(80)")
	private String exten;		// 分机号
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(columnDefinition="TIMESTAMP WITH TIME ZONE")
	private Date loginDate;		// 登录日期
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(columnDefinition="TIMESTAMP WITH TIME ZONE")
	private Date logoutDate;	// 退出日期
	
	@Size(min = 0, max = 64)
	@Column(columnDefinition="character varying(64)")
	private String ip;			// 登陆时使用的Ip 

	@Column(columnDefinition="bigint")
	private Long deptId;		// 部门id
	
	@Size(min = 0, max = 64)
	@Column(columnDefinition="character varying(64)")
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

	public String getRealName() {
		return realName;
	}

	public String getExten() {
		return exten;
	}

	public Date getLoginDate() {
		return loginDate;
	}

	public Date getLogoutDate() {
		return logoutDate;
	}

	public String getIp() {
		return ip;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setRealName(String realName) {
		this.realName = realName;
	}

	public void setExten(String exten) {
		this.exten = exten;
	}

	public void setLoginDate(Date loginDate) {
		this.loginDate = loginDate;
	}

	public void setLogoutDate(Date logoutDate) {
		this.logoutDate = logoutDate;
	}

	public void setIp(String ip) {
		this.ip = ip;
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
