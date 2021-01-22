package com.jiangyifen.ec2.entity;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;


@Entity
@Table(name = "ec2_notice")
public class Notice {
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "notice")
	@SequenceGenerator(name = "notice", sequenceName = "seq_ec2_notice_id", allocationSize = 1)
	private Long id;	// ID号
	
	@NotNull
	@Size(min = 1,max = 64)
	@Column(columnDefinition="character varying(64)")
	private String type;	// 类型
	
	@NotNull
	@Size(min = 1,max = 128)
	@Column(columnDefinition="character varying(128)")
	private String title;	// 标题
	
	@Size(min = 1)
	@Column(columnDefinition= "TEXT")
	private String content;	// 内容
	
	@Override
	public int hashCode() {
		return id.intValue();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Notice other = (Notice) obj;
		if (id == null||other.id == null) {
			return false;
		}
		return id.intValue()==other.id.intValue();
	}
	
	@Column(nullable = false, columnDefinition= "TIMESTAMP WITH TIME ZONE NOT NULL")
	@Temporal(TemporalType.TIMESTAMP)
	private Date sendDate;	// 发送时间
	
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = User.class)
	@JoinColumn(name = "sender_id", nullable = false)
	private User sender;	// 发送者

    @NotNull
	@ManyToMany(fetch = FetchType.LAZY, targetEntity = User.class)
	@JoinTable(name = "ec2_notice_receiver_user_link",
				joinColumns = @JoinColumn(name = "notice_id"), 
				inverseJoinColumns = @JoinColumn(name = "user_id")
			)
	private Set<User> receivers = new HashSet<User>();	// 接收者用户
    
    @ManyToOne(fetch = FetchType.LAZY, targetEntity = Domain.class)
	@JoinColumn(name = "domain_id", nullable = false)
    private Domain domain;
    
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
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

	public Date getSendDate() {
		return sendDate;
	}

	public void setSendDate(Date sendDate) {
		this.sendDate = sendDate;
	}

	public User getSender() {
		return sender;
	}

	public void setSender(User sender) {
		this.sender = sender;
	}
	
	public Set<User> getReceivers() {
		return receivers;
	}

	public void setReceivers(Set<User> receivers) {
		this.receivers = receivers;
	}

	public Domain getDomain() {
		return domain;
	}

	public void setDomain(Domain domain) {
		this.domain = domain;
	}
	
}
