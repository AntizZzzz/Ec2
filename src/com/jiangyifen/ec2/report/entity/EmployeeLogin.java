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

import com.sun.istack.internal.NotNull;

@Entity
@Table(name = "ec2_report_employee_login")
// 呼入电话数量统计
public class EmployeeLogin {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "report_employee_login")
	@SequenceGenerator(name = "report_employee_login", sequenceName = "seq_ec2_report_employee_login_id", allocationSize = 1)
	private Long id;
	@NotNull
	@Size(min = 1, max = 64)
	private String domainId;
	@NotNull
	@Size(min = 1, max = 64)
	private String department;// 所在部门
	@NotNull
	@Size(min = 1, max = 64)
	private String username;// 工号
	@NotNull
	@Size(min = 1, max = 64)
	private String name;// 姓名
	@NotNull
	@Size(min = 1, max = 64)
	private String fenji;// 分机号
	@NotNull
	@Size(min = 1, max = 64)
	private String ip;// ip
	@Temporal(TemporalType.TIMESTAMP)
	@Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
	private Date loginDate;// 登入日期
	@Temporal(TemporalType.TIMESTAMP)
	@Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
	private Date logoutDate;// 登出日期
	@NotNull
	@Size(min = 1, max = 64)
	private int login_time_length;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getDomainId() {
		return domainId;
	}

	public void setDomainId(String domainId) {
		this.domainId = domainId;
	}

	public String getDepartment() {
		return department;
	}

	public void setDepartment(String department) {
		this.department = department;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getFenji() {
		return fenji;
	}

	public void setFenji(String fenji) {
		this.fenji = fenji;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public Date getLoginDate() {
		return loginDate;
	}

	public void setLoginDate(Date loginDate) {
		this.loginDate = loginDate;
	}

	public int getLogin_time_length() {
		return login_time_length;
	}

	public void setLogin_time_length(int login_time_length) {
		this.login_time_length = login_time_length;
	}

	public Date getLogoutDate() {
		return logoutDate;
	}

	public void setLogoutDate(Date logoutDate) {
		this.logoutDate = logoutDate;
	}

}
