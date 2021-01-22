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
@Table(name="ec2_telephone")
public class Telephone {
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "telephone")
	@SequenceGenerator(name = "telephone", sequenceName = "seq_ec2_telephone_id", allocationSize = 1)
	private Long id; 
	
	@Size(min = 0, max = 64)
	@Column(columnDefinition="character varying(64) NOT NULL", nullable=false)
	private String number;
	
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = CustomerResource.class)
	@JoinColumn(columnDefinition="NOT NULL", nullable = false)
	private CustomerResource customerResource;

	@ManyToOne(fetch = FetchType.LAZY, targetEntity = Domain.class)
	@JoinColumn(name = "domain_id", nullable = false)
	private Domain domain;

	public Long getId() {
		return id;
	}

	public String getNumber() {
		return number;
	}

	public CustomerResource getCustomerResource() {
		return customerResource;
	}

	public Domain getDomain() {
		return domain;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public void setCustomerResource(CustomerResource customerResource) {
		this.customerResource = customerResource;
	}

	public void setDomain(Domain domain) {
		this.domain = domain;
	}
	/**
	 * chb 
	 * 为了在客服记录中电话能够正确显示，重写toString方法
	 */
	@Override
	public String toString() {
		return number;
	}
}
