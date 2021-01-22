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
import javax.validation.constraints.Size;

/**
 * 队列请求详单，用于报表统计
 * 
 * @author chao
 * 
 */
@Entity
@Table(name = "ec2_queue_request_detail")
public class QueueRequestDetail {
	/**
	 * id
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "queue_request_detail")
	@SequenceGenerator(name = "queue_request_detail", sequenceName = "seq_ec2_queue_request_detail_id", allocationSize = 1)
	private Long id;
	/**
	 * 队列名
	 */
	@Size(min = 0, max = 64)
	@Column(columnDefinition = "character varying(64)")
	private String queueName;
	/**
	 * 电话打入队列时的channel
	 */
	@Size(min = 0, max = 64)
	@Column(columnDefinition = "character varying(64)")
	private String channel;
	/**
	 * 进入队列日期
	 */
	@Temporal(TemporalType.TIMESTAMP)
	private Date intoDate;
	/**
	 * 退出队列日期
	 */
	@Temporal(TemporalType.TIMESTAMP)
	private Date outDate;
	/**
	 * 是否接通，默认false(未接通)
	 */
	@Column(columnDefinition = "boolean not null default false", nullable = false)
	private Boolean isAnswered = false;
	/**
	 * 域的id
	 */
	@Column(columnDefinition = "bigint", nullable = false)
	private Long domainId = 0L;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getQueueName() {
		return queueName;
	}

	public void setQueueName(String queueName) {
		this.queueName = queueName;
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public Date getIntoDate() {
		return intoDate;
	}

	public void setIntoDate(Date intoDate) {
		this.intoDate = intoDate;
	}

	public Date getOutDate() {
		return outDate;
	}

	public void setOutDate(Date outDate) {
		this.outDate = outDate;
	}

	public Boolean getIsAnswered() {
		return isAnswered;
	}

	public void setIsAnswered(Boolean isAnswered) {
		this.isAnswered = isAnswered;
	}

	public Long getDomainId() {
		return domainId;
	}

	public void setDomainId(Long domainId) {
		this.domainId = domainId;
	}

	@Override
	public String toString() {
		return "QueueRequestDetail [id=" + id + ", queueName=" + queueName + ", channel=" + channel + ", intoDate=" + intoDate + ", outDate=" + outDate + ", isAnswered=" + isAnswered + ", domainId=" + domainId + "]";
	}

}
