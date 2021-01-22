package com.jiangyifen.ec2.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
@Table(name = "ec2_concurrent_statics")
public class ConcurrentStatics {
	public static final String OUTLINE="外线";
	public static final String ALLSIP="所有分机";
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "concurrent_statics")
	@SequenceGenerator(name = "concurrent_statics", sequenceName = "seq_ec2_concurrent_statics_id", allocationSize = 1)
	private Long id;
	
	private Integer min;

	private Integer max;
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date dateTime; // 日期
	
	@Size(min = 0, max = 64)
	@Column(columnDefinition="character varying(64)")
	private String peer;
	
	@Size(min = 0, max = 64)
	@Column(columnDefinition="character varying(64)")
	private String type;
	
	//域的概念 批次n-->1域
	@NotNull
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = Domain.class)
	private Domain domain;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Integer getMin() {
		return min;
	}

	public void setMin(Integer min) {
		this.min = min;
	}

	public Integer getMax() {
		return max;
	}

	public void setMax(Integer max) {
		this.max = max;
	}

	public Date getDateTime() {
		return dateTime;
	}

	public void setDateTime(Date dateTime) {
		this.dateTime = dateTime;
	}

	public String getPeer() {
		return peer;
	}

	public void setPeer(String peer) {
		this.peer = peer;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Domain getDomain() {
		return domain;
	}

	public void setDomain(Domain domain) {
		this.domain = domain;
	}
}
