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
import javax.validation.constraints.Size;
/**
 * 此处---> 用户名和工号必须一致 TODO chb  用户名和工号必须一致 
 * @author Administrator
 */
@Entity
//@Table( name="ec2_user_queue", uniqueConstraints={@UniqueConstraint(columnNames={"username","queueName"})} )  解决自动外呼添加csr出现异常  --chb 20140318 13:41
@Table( name="ec2_user_queue")
public class UserQueue {
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_queue")
	@SequenceGenerator(name = "user_queue", sequenceName = "seq_ec2_user_queue_id", allocationSize = 1)
	private Long id; 
	
	@Size(min = 0, max = 64)
	@Column(columnDefinition="character varying(64) NOT NULL", nullable = false)
	private String username;	// 用户名称
	
	@Size(min = 0, max = 128)
	@Column(columnDefinition="character varying(128) NOT NULL", nullable = false)
	private String queueName;	// 队列名称
	
	@Column(columnDefinition="integer")
	private Integer priority;

	@ManyToOne(fetch = FetchType.LAZY, targetEntity = Domain.class)
	@JoinColumn(name = "domain_id", nullable = false)
	private Domain domain;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getQueueName() {
		return queueName;
	}

	public void setQueueName(String queueName) {
		this.queueName = queueName;
	}

	public Integer getPriority() {
		return priority;
	}

	public void setPriority(Integer priority) {
		this.priority = priority;
	}

	public Domain getDomain() {
		return domain;
	}

	public void setDomain(Domain domain) {
		this.domain = domain;
	}

}
