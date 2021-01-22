package com.jiangyifen.ec2.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
@Table(name = "ec2_customer_complaint_record_status")
public class CustomerComplaintRecordStatus {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "customer_complaint_record_status")
	@SequenceGenerator(name = "customer_complaint_record_status", sequenceName = "seq_ec2_customer_complaint_record_status_id", allocationSize = 1)
	private Long id;

	// 特定类型的状态名称
	@Size(min = 0, max = 32)
	@Column(nullable = false,columnDefinition="character varying(32) NOT NULL")
	private String statusName;

	 // 当前状态“暗示”，是接通、还是未接通  默认 false 表示未接通
	@Column(columnDefinition="boolean not null default false", nullable = false)
	private Boolean implication = false;
	
	public CustomerComplaintRecordStatus() {
	}
	// =================对应关系=================//

	// 域的概念 批次1-->n域
	// 域不能为空
	@NotNull
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = Domain.class)
	private Domain domain;
	
	// ================= getter 和 setter 方法=================//
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getStatusName() {
		return statusName;
	}

	public void setStatusName(String statusName) {
		this.statusName = statusName;
	}

	public Boolean getImplication() {
		return implication;
	}
	public void setImplication(Boolean implication) {
		this.implication = implication;
	}
	public Domain getDomain() {
		return domain;
	}

	public void setDomain(Domain domain) {
		this.domain = domain;
	}

	public String toString() {
		return statusName;
	}
	
}
