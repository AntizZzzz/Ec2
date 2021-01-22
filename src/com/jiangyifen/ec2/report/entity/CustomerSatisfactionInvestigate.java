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
@Table(name = "ec2_report_customer_satisfaction_investigate")
// 呼入电话数量统计
public class CustomerSatisfactionInvestigate {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "report_call_check")
	@SequenceGenerator(name = "report_customer_satisfaction_investigate", sequenceName = "seq_ec2_report_customer_satisfaction_investigate_id", allocationSize = 1)
	private Long id;
	/**
	 * 日期(yyyy-MM-dd)
	 */
	@Column(nullable = false, columnDefinition = "date")
	@Temporal(TemporalType.DATE)
	private Date date;
	/**
	 * 时段(24小时进制)
	 */
	@NotNull
	@Size(min = 1, max = 64)
	private int hour;
	/**
	 * 所在域id
	 */
	@NotNull
	@Size(min = 1, max = 64)
	private String domainId;
	/**
	 * 所在部门
	 */
	@NotNull
	@Size(min = 1, max = 64)
	private String deptName;
	/**
	 * 用户名
	 */
	@NotNull
	@Size(min = 1, max = 64)
	private String username;
	/**
	 * 姓名
	 */
	@NotNull
	@Size(min = 1, max = 64)
	private String realName;

	/**
	 * 总成绩
	 */
	private int sumGrade;

	/**
	 * 被打分的次数
	 */
	private int count;

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

	public int getHour() {
		return hour;
	}

	public void setHour(int hour) {
		this.hour = hour;
	}

	public String getDomainId() {
		return domainId;
	}

	public void setDomainId(String domainId) {
		this.domainId = domainId;
	}

	public String getDeptName() {
		return deptName;
	}

	public void setDeptName(String deptName) {
		this.deptName = deptName;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getRealName() {
		return realName;
	}

	public void setRealName(String realName) {
		this.realName = realName;
	}

	public int getSumGrade() {
		return sumGrade;
	}

	public void setSumGrade(int sumGrade) {
		this.sumGrade = sumGrade;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

}
