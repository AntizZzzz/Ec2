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
@Table(name = "ec2_musiconhold")
public class MusicOnHold {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "musiconhold")
	@SequenceGenerator(name = "musiconhold", sequenceName = "seq_ec2_musiconhold_id", allocationSize = 1)
	private Long id;
	
	@Size(min = 0, max = 64)
	@Column(columnDefinition="character varying(64) NOT NULL", nullable = true)
	private String name;
	
	@Size(min = 0, max = 64)
	@Column(columnDefinition="character varying(64) NOT NULL", nullable = true)
	private String mode;
	
	@Size(min = 0, max = 255)
	@Column(columnDefinition="character varying(255) NOT NULL", nullable = true)
	private String directory;
	
	@Size(min = 0, max = 128)
	@Column(columnDefinition="character varying(128)")
	private String description;
	
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = Domain.class)
	@JoinColumn(columnDefinition="NOT NULL", name = "domain_id", nullable = false)
	private Domain domain; // 所属公司

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

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public String getDirectory() {
		return directory;
	}

	public void setDirectory(String directory) {
		this.directory = directory;
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

	public String toString() {
		return name+"-"+description;
	}
	
}
