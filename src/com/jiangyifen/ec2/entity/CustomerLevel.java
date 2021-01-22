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
@Table(name = "ec2_customer_level")
public class CustomerLevel {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "customer_level")
	@SequenceGenerator(name = "customer_level", sequenceName = "seq_ec2_customer_level_id", allocationSize = 1)
	private Long id;

	// 等级名称
	@Size(min = 0, max = 32)
	@Column(nullable = false,columnDefinition="character varying(32) NOT NULL")
	private String levelName;
	
	//等级
	private Integer level;
	
	//保护期
	private Integer protectDay;
	
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

	public String getLevelName() {
		return levelName;
	}

	public void setLevelName(String levelName) {
		this.levelName = levelName;
	}

	public Integer getLevel() {
		return level;
	}

	public void setLevel(Integer level) {
		this.level = level;
	}

	public Integer getProtectDay() {
		return protectDay;
	}

	public void setProtectDay(Integer protectDay) {
		this.protectDay = protectDay;
	}

	public Domain getDomain() {
		return domain;
	}

	public void setDomain(Domain domain) {
		this.domain = domain;
	}

	@Override
	public String toString() {
		return levelName;
	}
	
}
