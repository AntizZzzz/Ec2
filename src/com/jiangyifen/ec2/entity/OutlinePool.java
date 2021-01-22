package com.jiangyifen.ec2.entity;

import java.util.Date;

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
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
@Table(name = "ec2_outline_pool")
public class OutlinePool {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "outline_pool")
	@SequenceGenerator(name = "outline_pool", sequenceName = "seq_ec2_outline_pool_id", allocationSize = 1)
	private Long id; // 号码池ID
	
	@NotNull
	@Size(min = 0, max = 32)
	@Column(nullable = false,columnDefinition="character varying(64) NOT NULL")
	private String poolNum; // 号码池编号
	
	@NotNull
	@Size(min = 0, max = 64)
	@Column(nullable = false,columnDefinition="character varying(64) NOT NULL")
	private String name; // 号码池名称

	@Size(min = 0, max = 64)
	@Column(columnDefinition="character varying(64)")
	private String description; // 描述
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(columnDefinition= "TIMESTAMP WITH TIME ZONE")
	private Date createTime;
	
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = Domain.class)
	@JoinColumn(columnDefinition="NOT NULL", name = "domain_id", nullable = false)
	private Domain domain; // 所属公司

	
	
	public OutlinePool() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Domain getDomain() {
		return domain;
	}

	public void setDomain(Domain domain) {
		this.domain = domain;
	}

	public String getPoolNum() {
		return poolNum;
	}

	public void setPoolNum(String poolNum) {
		this.poolNum = poolNum;
	}
	
}
