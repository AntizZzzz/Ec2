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

@Entity
@Table(name = "ec2_noticeitem")
public class NoticeItem {
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "noticeitem")
	@SequenceGenerator(name = "noticeitem", sequenceName = "seq_ec2_noticeitem_id", allocationSize = 1)
	private Long id;
	
	// 是否已读	默认是未读
	@Column(columnDefinition="boolean not null default false", nullable = false)
	private boolean hasReaded = false;
	
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = User.class)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;	// 该Item对应的用户
	
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = Notice.class)
	@JoinColumn(name = "notice_id", nullable = false)
	private Notice notice;	// 该Item对应的Notice
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public boolean isHasReaded() {
		return hasReaded;
	}

	public void setHasReaded(boolean hasReaded) {
		this.hasReaded = hasReaded;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Notice getNotice() {
		return notice;
	}

	public void setNotice(Notice notice) {
		this.notice = notice;
	}
	
}
