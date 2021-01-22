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
@Table(name = "ec2_report_autodial_detail")
public class AutodialDetail {
	/**
	 * id
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "report_autodial_detail")
	@SequenceGenerator(name = "report_autodial_detail", sequenceName = "seq_ec2_report_autodial_detail_id", allocationSize = 1)
	private Long id;
	/**
	 * 日期(yyyy-MM-dd)
	 */
	@Column(nullable = false, columnDefinition = "date")
	@Temporal(TemporalType.DATE)
	private Date date;
	/**
	 * 所在域id
	 */
	@NotNull
	@Size(min = 1, max = 64)
	private String domainId;
	/**
	 * 部门名称
	 */
	@NotNull
	@Size(min = 1, max = 64)
	private String deptName;
	/**
	 * 自动外呼名称
	 */
	@NotNull
	@Size(min = 1, max = 64)
	private String autodialName;
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
	private String realName;// 姓名
	/**
	 * 接通数量
	 */
	@NotNull
	@Size(min = 1, max = 64)
	private int connect_count;
	/**
	 * 接通时长
	 */
	@NotNull
	@Size(min = 1, max = 64)
	private int connect_time_length;
	/**
	 * 平均通话时长
	 */
	@NotNull
	@Size(min = 1, max = 64)
	private int average_call_time_length;
	/**
	 * 接电话用时
	 */
	@NotNull
	@Size(min = 1, max = 64)
	private int pickup_time_length;
	/**
	 * 平均接电话用时
	 */
	@NotNull
	@Size(min = 1, max = 64)
	private int average_pickup_time_length;

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

	public String getAutodialName() {
		return autodialName;
	}

	public void setAutodialName(String autodialName) {
		this.autodialName = autodialName;
	}

	public int getConnect_count() {
		return connect_count;
	}

	public void setConnect_count(int connect_count) {
		this.connect_count = connect_count;
	}

	public int getConnect_time_length() {
		return connect_time_length;
	}

	public void setConnect_time_length(int connect_time_length) {
		this.connect_time_length = connect_time_length;
	}

	public int getAverage_call_time_length() {
		return average_call_time_length;
	}

	public void setAverage_call_time_length(int average_call_time_length) {
		this.average_call_time_length = average_call_time_length;
	}

	public int getPickup_time_length() {
		return pickup_time_length;
	}

	public void setPickup_time_length(int pickup_time_length) {
		this.pickup_time_length = pickup_time_length;
	}

	public int getAverage_pickup_time_length() {
		return average_pickup_time_length;
	}

	public void setAverage_pickup_time_length(int average_pickup_time_length) {
		this.average_pickup_time_length = average_pickup_time_length;
	}

}
