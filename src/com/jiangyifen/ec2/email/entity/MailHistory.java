package com.jiangyifen.ec2.email.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.User;

/**
 * 邮件发送的历史记录实体类 
 */
@Entity
@Table(name = "ec2_mail_history")
public class MailHistory {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "mail_history")
	@SequenceGenerator(name = "mail_history", sequenceName = "seq_mail_history_id", allocationSize = 1)
	private Long id;

	//who
	@Size(min = 0, max = 255)
	@Column(columnDefinition="character varying(255)")
	private String fromAddress;		// 邮件发送者的地址

	@Size(min = 0, max = 255)
	@Column(columnDefinition="character varying(255)")
	private String senderName;			// 邮件发送者的昵称

	// when
	@Temporal(TemporalType.TIMESTAMP)
	private Date sendTimeDate;			// 邮件发送的时间
	
	//send what
	@Size(min = 0, max = 255)
	@Column(columnDefinition="character varying(255)")
	private String subject;			// 邮件发送的主题

	@Size(min = 0, max = 4096)
	@Column(columnDefinition="character varying(4096)")
	private String content;			// 邮件发送的内容
	
	/**
	 * 邮箱发送附件，如果有多个，中间用","隔开
	 * 在给邮件添加附件时，需要先把附件上传到服务器
	 * 功能待拓展
	 */
	@Size(min = 0, max = 1024)
	@Column(columnDefinition="character varying(1024)")
	private String attachFiles; // multi separate by ,

	/**
	 * 邮箱接收人，如果有多个，中间用","隔开
	 */
	private String toAddresses; // multi separate by ,
	
	public MailHistory() {
	}
	// =================对应关系=================//

	// 多个创建的邮件发送的历史记录n-->1个用户
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = User.class)
	private User user;
	
	// 当开启了全局使用统一的邮箱地址发送邮件时多个创建的邮件发送的历史记录 n--> 1个用户
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = User.class)
	private User loginUser;

	//域的概念 邮件发送的历史记录n-->1域
	//域不能为空
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

	public String getFromAddress() {
		return fromAddress;
	}

	public void setFromAddress(String fromAddress) {
		this.fromAddress = fromAddress;
	}

	public String getSenderName() {
		return senderName;
	}

	public void setSenderName(String senderName) {
		this.senderName = senderName;
	}

	public Date getSendTimeDate() {
		return sendTimeDate;
	}

	public void setSendTimeDate(Date sendTimeDate) {
		this.sendTimeDate = sendTimeDate;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getAttachFiles() {
		return attachFiles;
	}

	public void setAttachFiles(String attachFiles) {
		this.attachFiles = attachFiles;
	}

	public String getToAddresses() {
		return toAddresses;
	}

	public void setToAddresses(String toAddresses) {
		this.toAddresses = toAddresses;
	}

	public Domain getDomain() {
		return domain;
	}

	public void setDomain(Domain domain) {
		this.domain = domain;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public User getLoginUser() {
		return loginUser;
	}

	public void setLoginUser(User loginUser) {
		this.loginUser = loginUser;
	}

}

