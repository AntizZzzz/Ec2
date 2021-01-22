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
@Table(name = "ec2_report_employee_busy")
public class EmployeeBusy {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "report_employee_busy")
	@SequenceGenerator(name = "report_employee_busy", sequenceName = "seq_ec2_report_employee_busy_id", allocationSize = 1)
	private Long id;
	@Column(nullable = false, columnDefinition = "date")
	@Temporal(TemporalType.DATE)
	private Date date; // 日期
	@NotNull
	@Size(min = 1, max = 64)
	private int hour;// 时段(24小时进制)
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
	private String queueName;// 队列名
	@NotNull
	@Size(min = 1, max = 64)
	private int busyTimeLength;// 置忙时长

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

	public String getQueueName() {
		return queueName;
	}

	public void setQueueName(String queueName) {
		this.queueName = queueName;
	}

	public int getBusyTimeLength() {
		return busyTimeLength;
	}

	public void setBusyTimeLength(int busyTimeLength) {
		this.busyTimeLength = busyTimeLength;
	}

	@Override
	public String toString() {
		return "EmployeeBusy [id=" + id + ", date=" + date + ", hour=" + hour
				+ ", domainId=" + domainId + ", department=" + department
				+ ", username=" + username + ", name=" + name + ", queueName="
				+ queueName + ", busyTimeLength=" + busyTimeLength + "]";
	}

}
