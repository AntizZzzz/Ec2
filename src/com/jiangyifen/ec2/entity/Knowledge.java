package com.jiangyifen.ec2.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.jiangyifen.ec2.entity.enumtype.KnowledgeStatus;


@Entity
@Table(name = "ec2_knowledge")
public class Knowledge {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "knowledge")
	@SequenceGenerator(name = "knowledge", sequenceName = "seq_ec2_knowledge_id", allocationSize = 1)
	private Long id;
	
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = User.class)
	@JoinColumn(columnDefinition = "NOT NULL", name = "creator_id", nullable = false)
	private User user;             //创建者
	
	//知识标题
	@Size(min = 0, max = 64)
	@Column(nullable = false, columnDefinition = "character varying(64) NOT NULL")
	private String title;
	
	@NotNull
	@Column(columnDefinition="text")
	private String content;      //知识内容
	
	@Enumerated
	private KnowledgeStatus knowledgeStatus;
	
	@NotNull
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = Domain.class)
	@JoinColumn(name = "domain_id", nullable = false)
	private Domain domain;	   //所属 域

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public KnowledgeStatus getKnowledgeStatus() {
		return knowledgeStatus;
	}

	public void setKnowledgeStatus(KnowledgeStatus knowledgeStatus) {
		this.knowledgeStatus = knowledgeStatus;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Domain getDomain() {
		return domain;
	}

	public void setDomain(Domain domain) {
		this.domain = domain;
	}

}
