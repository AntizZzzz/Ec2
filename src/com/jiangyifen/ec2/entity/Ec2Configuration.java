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

// 全局配置
@Entity
@Table(name = "ec2_config")
public class Ec2Configuration {
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "config")
	@SequenceGenerator(name = "config", sequenceName = "seq_ec2_config_id", allocationSize = 1)
	private Long id;
	
	@Size(min = 0, max = 255)
	@Column(nullable = false,columnDefinition="character varying(255) NOT NULL")
	private String key;
	
	@Column(nullable = false)
	private Boolean value;
	
//	TODO 重构时将value 字段改成字符串类型，因为全局配置中的配置项可能是多方面选择
//	@Size(min = 0, max = 64)
//	@Column(columnDefinition="character varying(64)")
//	private String valueStr;
	
	// 加上描述字段 , 如果key ='mobile_num_secret' , value = 'false' ,那么写描述信息的格式建议为如下所示：
	// value值为 false 表示电话号码不加密，否则加密
	@Size(min = 0, max = 255)
	@Column(columnDefinition="character varying(255)")
	private String description;
	
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = Domain.class)
	@JoinColumn(columnDefinition="NOT NULL", name = "domain_id", nullable=false)
	private Domain domain;
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public Boolean getValue() {
		return value;
	}

	public void setValue(Boolean value) {
		this.value = value;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Domain getDomain() {
		return domain;
	}

	public void setDomain(Domain domain) {
		this.domain = domain;
	}
	
}
