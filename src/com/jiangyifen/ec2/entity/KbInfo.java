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
/**
 * 
 * @author lxy
 *
 */
@Entity
@Table(name = "ec2_kbinfo")
public class KbInfo {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ec2_kbinfo")
	@SequenceGenerator(name = "ec2_kbinfo", sequenceName = "seq_ec2_kbinfo_id", allocationSize = 1)
	private Long id; 
	
	@Column(columnDefinition="character varying(500) NOT NULL", nullable = false)
	private String title;

	@Column(columnDefinition = "TEXT")
  	private String content;
	
	//创建时间
	@Temporal(TemporalType.TIMESTAMP)
	@Column(columnDefinition= "TIMESTAMP WITH TIME ZONE")
	private Date createDate;
	
	//最后更新时间
	@Temporal(TemporalType.TIMESTAMP)
	@Column(columnDefinition= "TIMESTAMP WITH TIME ZONE")
	private Date lastUpdateDate;
	
	@Column(columnDefinition="character varying(100)")
	private String key;
	
	@Column
	private int feedbacks;
	
	@Column
	private long looks;
	
	@Column
	private long uses;
	
	@Column
	private int level;
	
	@Column
	private int status;
	
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = KbInfoType.class)
	private KbInfoType kbInfoType;
	
	@NotNull
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = Domain.class)
	@JoinColumn(name = "domain_id", nullable = false)
	private Domain domain;	//所属公司
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public Date getLastUpdateDate() {
		return lastUpdateDate;
	}

	public void setLastUpdateDate(Date lastUpdateDate) {
		this.lastUpdateDate = lastUpdateDate;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public int getFeedbacks() {
		return feedbacks;
	}

	public void setFeedbacks(int feedbacks) {
		this.feedbacks = feedbacks;
	}

	public long getLooks() {
		return looks;
	}

	public void setLooks(long looks) {
		this.looks = looks;
	}

	public long getUses() {
		return uses;
	}

	public void setUses(long uses) {
		this.uses = uses;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public KbInfoType getKbInfoType() {
		return kbInfoType;
	}

	public void setKbInfoType(KbInfoType kbInfoType) {
		this.kbInfoType = kbInfoType;
	}

	public Domain getDomain() {
		return domain;
	}

	public void setDomain(Domain domain) {
		this.domain = domain;
	}
	
	
}
