package com.jiangyifen.ec2.entity;

import java.util.Date;

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
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

//声音文件
@Entity
@Table(name = "ec2_sound_file")
public class SoundFile {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sound_file")
	@SequenceGenerator(name = "sound_file", sequenceName = "seq_ec2_sound_file_id", allocationSize = 1)
	private Long id;
	
	//语音名称
	@Size(min = 0, max = 64)
	@Column(nullable = false,columnDefinition="character varying(64)")
	private String storeName;
		
	
	//描述信息
	@Size(min = 0, max = 32)
	@Column(columnDefinition="character varying(32)")
	private String descName;
	
	//语音上传时间
	@Temporal(TemporalType.TIMESTAMP)
	private Date importDate;
	
	/**
	 * 对应关系
	 */
	//多个创建的批次n-->1个用户
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = User.class)
	private User user; //上传者
	
	//域
	@NotNull
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = Domain.class)
	@JoinColumn(name = "domain_id", nullable = false)
	private Domain domain;

	@Override
	public String toString() {
		return descName;
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getStoreName() {
		return storeName;
	}

	public void setStoreName(String storeName) {
		this.storeName = storeName;
	}

	public String getDescName() {
		return descName;
	}

	public void setDescName(String descName) {
		this.descName = descName;
	}

	public Date getImportDate() {
		return importDate;
	}

	public void setImportDate(Date importDate) {
		this.importDate = importDate;
	}
	
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Domain getDomain() {
		return domain;
	}

	public void setDomain(Domain domain) {
		this.domain = domain;
	}
	
}
