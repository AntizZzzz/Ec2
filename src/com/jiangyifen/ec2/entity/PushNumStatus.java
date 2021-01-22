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
@Table(name = "ec2_push_num_status")
public class PushNumStatus {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "push_num_status")
	@SequenceGenerator(name = "push_num_status", sequenceName = "seq_ec2_push_num_status_id", allocationSize = 1)
	private Long id;

	@Column(nullable = false, columnDefinition = "character varying(64)")
	private String number;// 按键

	@Size(min = 0, max = 64)
	@Column(nullable = false, columnDefinition = "character varying(64)")
	private String statusName;// 状态名

	@NotNull
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = Domain.class)
	private Domain domain;// 所在域

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public String getStatusName() {
		return statusName;
	}

	public void setStatusName(String statusName) {
		this.statusName = statusName;
	}

	public Domain getDomain() {
		return domain;
	}

	public void setDomain(Domain domain) {
		this.domain = domain;
	}

}
