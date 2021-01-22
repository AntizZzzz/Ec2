package com.jiangyifen.ec2.email.entity;

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

import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.User;

/**
 * 邮件发送配置信息实体类
 * character varying [(n)] 变长字符串
 * character [(n)] 定长字符串
 */
@Entity
@Table(name = "ec2_mail_config")
public class MailConfig {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "mail_config")
	@SequenceGenerator(name = "mail_config", sequenceName = "seq_mail_config_id", allocationSize = 1)
	private Long id;

	@Size(min = 0, max = 255)
	@Column(columnDefinition="character varying(255)")
	private String smtpHost;		// 发送邮件服务器的IP
	
	@Size(min = 0, max = 255)
	@Column(columnDefinition="character varying(255)")
	private String smtpPort;		// 发送邮件服务器的端口
	
	@Size(min = 0, max = 255)
	@Column(columnDefinition="character varying(255)")
	private String smtpAuth;		// 是否需要身份验证
	
	@Size(min = 0, max = 255)
	@Column(nullable = false,unique = true,columnDefinition="character varying(255) NOT NULL unique")
	private String fromAddress;	// 邮件发送者的邮箱地址

	@Size(min = 0, max = 255)
	@Column(columnDefinition="character varying(255)")
	private String senderName;		// 邮件发送者的邮箱昵称
	
	@Size(min = 0, max = 255)
	@Column(columnDefinition="character varying(255)")
	private String senderPassword;	// 邮件发送者的邮箱密码

	/**
	 * 是否记住邮箱的密码
	 */
//	@Size(min = 0, max = 255)
//	@Column(columnDefinition="character varying(255)")
//	private String isRememberPassword;
	
	/**
	 * 是否为系统某一域下面的统一使用邮箱
	 * 	如果一个域下面有多个默认邮箱的话，则在每次发送邮件的时候都进行随机取得某一个邮箱信息进行发送邮件
	 * 	功能有待扩展
	 */
	@Size(min = 0, max = 255)
	@Column(columnDefinition="character varying(255)")
	private String isDefault;		

	public MailConfig() {
	}
	// =================对应关系=================//

	// 多个邮箱信息配置n-->1个用户
	@ManyToOne(fetch = FetchType.EAGER, targetEntity = User.class)
	private User user;

	//域的概念 邮箱信息配置n-->1域
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

	public String getSmtpHost() {
		return smtpHost;
	}

	public void setSmtpHost(String smtpHost) {
		this.smtpHost = smtpHost;
	}

	public String getSmtpPort() {
		return smtpPort;
	}

	public void setSmtpPort(String smtpPort) {
		this.smtpPort = smtpPort;
	}

	public String getSmtpAuth() {
		return smtpAuth;
	}

	public void setSmtpAuth(String smtpAuth) {
		this.smtpAuth = smtpAuth;
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

	public String getSenderPassword() {
		return senderPassword;
	}

	public void setSenderPassword(String senderPassword) {
		this.senderPassword = senderPassword;
	}
	
	public String getIsDefault() {
		return isDefault;
	}

	public void setIsDefault(String isDefault) {
		this.isDefault = isDefault;
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
	
}

