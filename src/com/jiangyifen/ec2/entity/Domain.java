package com.jiangyifen.ec2.entity;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.Size;

@Entity
@Table(name = "ec2_domain")
public class Domain implements java.io.Serializable {

	private static final long serialVersionUID = 5354091405790212809L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "domain")
	@SequenceGenerator(name = "domain", sequenceName = "seq_ec2_domain_id", allocationSize = 1)
	private Long id;

	/*
	 * 注意域名必须为英文字母或数字，而且最好符合java命名规范
	 */
	@Size(min = 0, max = 64)
	@Column(unique = true, nullable = false, columnDefinition="character varying(64) NOT NULL unique")
	private String name;
	
	@Size(min = 0, max = 128)
	@Column(columnDefinition="character varying(128)")
	private String description;
	
	@OneToMany(mappedBy = "domain", fetch = FetchType.LAZY, targetEntity = Ec2Configuration.class)
	private Set<Ec2Configuration> configs = new HashSet<Ec2Configuration>();

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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Set<Ec2Configuration> getConfigs() {
		return configs;
	}

	public void setConfigs(Set<Ec2Configuration> configs) {
		this.configs = configs;
	}

	@Override
	public String toString() {
		return name;
	}
	
	public String toLoggerString() {
		return "Domain [id=" + id + ", name=" + name + "]";
	}

}
