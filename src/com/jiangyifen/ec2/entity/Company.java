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

@Entity
@Table(name="ec2_company")
public class Company {
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "company")
	@SequenceGenerator(name = "company", sequenceName = "seq_ec2_company_id", allocationSize = 1)
	private Long id; 
	
	@Size(min = 0, max = 255)
	@Column(columnDefinition="character varying(255) NOT NULL", nullable = false)
	private String name;
	
	@Size(min = 0, max = 255)
	@Column(columnDefinition="character varying(255)")
	private String address;
	
	@Size(min = 0, max = 32)
	@Column(columnDefinition="character varying(32)")
	private String telephone;
	
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = Domain.class)
	@JoinColumn(name = "domain_id", nullable = false)
	private Domain domain;

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getAddress() {
		return address;
	}

	public String getTelephone() {
		return telephone;
	}

	public Domain getDomain() {
		return domain;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public void setTelephone(String telephone) {
		this.telephone = telephone;
	}

	public void setDomain(Domain domain) {
		this.domain = domain;
	}

	public String toString() {
		return name;
	}
	
}
