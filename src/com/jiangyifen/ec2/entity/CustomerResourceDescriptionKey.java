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
@Table(name = "ec2_customer_resource_description_key")
public class CustomerResourceDescriptionKey {
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "customer_resource_description_key")
	@SequenceGenerator(name = "customer_resource_description_key", sequenceName = "seq_ec2_customer_resource_description_key_id", allocationSize = 1)
	private Long id; 
	
	//描述信息key值（如：备注 note、爱好 favorite、汽车 car、存款 deposit、职称 jobTitle）
	@Size(min = 0, max = 32)
	@Column(columnDefinition="character varying(32) NOT NULL", nullable = false)
	private String key;

	public Long getId() {
		return id;
	}

	public String getKey() {
		return key;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setKey(String key) {
		this.key = key;
	}
	
}
