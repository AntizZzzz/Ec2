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
@Table(name = "ec2_city")
public class City {
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "city")
	@SequenceGenerator(name = "city", sequenceName = "seq_ec2_city_id", allocationSize = 1)
	private Long id;
	
	@Size(min = 0, max = 64)
	@Column(nullable = false,columnDefinition="character varying(64) NOT NULL")
	private String name;
	
	public City() {
	}
	
	@NotNull
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = Province.class)
	public Province province;
	
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


	public Province getProvince() {
		return province;
	}


	public void setProvince(Province province) {
		this.province = province;
	}

	@Override
	public String toString() {
		return name;
	}

	//city 唯一,返回name即可
	public String toLoggerString() {
		return name;
	}
	
}
