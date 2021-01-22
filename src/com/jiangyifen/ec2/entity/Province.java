package com.jiangyifen.ec2.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.Size;

@Entity
@Table(name = "ec2_province")
public class Province {
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "province")
	@SequenceGenerator(name = "province", sequenceName = "seq_ec2_province_id", allocationSize = 1)
	private Long id;
	
	@Size(min = 0, max = 64)
	@Column(nullable = false,columnDefinition="character varying(64) NOT NULL")
	private String name;
	
	public Province() {
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
	
	public String toString() {
		return name;
	}
}
