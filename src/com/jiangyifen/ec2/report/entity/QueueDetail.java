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
@Table(name = "ec2_report_queue_detail")
public class QueueDetail {
	/**
	 * id
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "report_queue_detail")
	@SequenceGenerator(name = "report_queue_detail", sequenceName = "seq_ec2_report_queue_detail_id", allocationSize = 1)
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
	 * 队列名
	 */
	@NotNull
	@Size(min = 1, max = 64)
	private String queueName;
	/**
	 * 队列进入次数
	 */
	@NotNull
	@Size(min = 1, max = 64)
	private int into_queue_count;
	/**
	 * 队列放弃次数
	 */
	@NotNull
	@Size(min = 1, max = 64)
	private int abandon_queue_count;
	/**
	 * 接通等待时长
	 */
	@NotNull
	@Size(min = 1, max = 64)
	private int connect_wait_time_length;
	/**
	 * 未接通等待时长
	 */
	@NotNull
	@Size(min = 1, max = 64)
	private int unconnect_wait_time_length;

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

	public String getQueueName() {
		return queueName;
	}

	public void setQueueName(String queueName) {
		this.queueName = queueName;
	}

	public int getInto_queue_count() {
		return into_queue_count;
	}

	public void setInto_queue_count(int into_queue_count) {
		this.into_queue_count = into_queue_count;
	}

	public int getAbandon_queue_count() {
		return abandon_queue_count;
	}

	public void setAbandon_queue_count(int abandon_queue_count) {
		this.abandon_queue_count = abandon_queue_count;
	}

	public int getConnect_wait_time_length() {
		return connect_wait_time_length;
	}

	public void setConnect_wait_time_length(int connect_wait_time_length) {
		this.connect_wait_time_length = connect_wait_time_length;
	}

	public int getUnconnect_wait_time_length() {
		return unconnect_wait_time_length;
	}

	public void setUnconnect_wait_time_length(int unconnect_wait_time_length) {
		this.unconnect_wait_time_length = unconnect_wait_time_length;
	}

}
