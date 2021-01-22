package com.jiangyifen.ec2.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Size;

@Entity
@Table(name="ec2_static_queue_member", uniqueConstraints={@UniqueConstraint(columnNames={"queueName","sipname", "domain_id"})})
public class StaticQueueMember {
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "static_queue_member")
	@SequenceGenerator(name = "static_queue_member", sequenceName = "seq_ec2_static_queue_member_id", allocationSize = 1)
	private Long id;
	
	@Size(min = 0, max = 128)
	@Column(columnDefinition="character varying(128) NOT NULL", nullable = false)
	private String queueName;
	
	@Size(min = 0, max = 128)
	@Column(columnDefinition="character varying(128) NOT NULL", nullable = false)
	private String sipname;
	
	@Column(columnDefinition="integer")
	private Integer priority;

	@ManyToOne(fetch = FetchType.LAZY, targetEntity = Domain.class)
	@JoinColumn(name = "domain_id", nullable = false)
	private Domain domain;

	public Long getId() {
		return id;
	}

	public String getQueueName() {
		return queueName;
	}

	public String getSipname() {
		return sipname;
	}

	public Integer getPriority() {
		return priority;
	}

	public Domain getDomain() {
		return domain;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setQueueName(String queueName) {
		this.queueName = queueName;
	}

	public void setSipname(String sipname) {
		this.sipname = sipname;
	}

	public void setPriority(Integer priority) {
		this.priority = priority;
	}

	public void setDomain(Domain domain) {
		this.domain = domain;
	}

}
