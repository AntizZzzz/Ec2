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
@Table(name = "ec2_county")
public class County {
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "county")
	@SequenceGenerator(name = "county", sequenceName = "seq_ec2_county_id", allocationSize = 1)
	private Long id;
	
	@Size(min = 1, max = 64)
	@Column(nullable = false,columnDefinition="character varying(64) NOT NULL")
	private String name;
	
	public County() {
	}
	
	@NotNull
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = City.class)
	private City city;
	
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


	public City getCity() {
		return city;
	}


	public void setCity(City city) {
		this.city = city;
	}
	@Override
	public String toString() {
		return name;
	}
	//country唯一,返回name即可
	public String toLoggerString() {
		return name;
	}
}
