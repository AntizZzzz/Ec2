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
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
@Table(name = "ec2_kbinfo_type")
public class KbInfoType {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ec2_kbinfo_type")
	@SequenceGenerator(name = "ec2_kbinfo_type", sequenceName = "seq_ec2_kbinfo_type_id", allocationSize = 1)
	private Long id;
	
	@Size(min = 0, max = 20)
	@Column(columnDefinition="character varying(100) NOT NULL ", nullable = false)
	private String name;
	
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = KbInfoType.class)
	private KbInfoType parenetType;

	@NotNull
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = Domain.class)
	@JoinColumn(name = "domain_id", nullable = false)
	private Domain domain;	//所属公司
	
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

	public KbInfoType getParenetType() {
		return parenetType;
	}

	public void setParenetType(KbInfoType parenetType) {
		this.parenetType = parenetType;
	}

	public Domain getDomain() {
		return domain;
	}

	public void setDomain(Domain domain) {
		this.domain = domain;
	}
	
	
	
}
