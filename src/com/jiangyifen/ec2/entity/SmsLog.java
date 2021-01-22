package com.jiangyifen.ec2.entity;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Enumerated;
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

import com.jiangyifen.ec2.entity.enumtype.SmsStatus;
@Entity
@Table(name = "ec2_smslog")
public class SmsLog {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "smslog")
	@SequenceGenerator(name = "smslog", sequenceName = "seq_ec2_smslog_id", allocationSize = 1)
	private Long id;
	
	//发送日期
	@Temporal(TemporalType.TIMESTAMP)
	private Date sendDate;

	//发送人
	@NotNull 
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = User.class)
	private User user;
	
	//发送分机
	private String extenNumber;
	
	//目标电话
	private String destPhoneNumber;
	
	//发送内容
	private String content;

	@Enumerated
	private SmsStatus smsStatus;
	
	// 域不能为空
	@NotNull
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = Domain.class)
	private Domain domain;

	
	//Getter and Setter 
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Date getSendDate() {
		return sendDate;
	}

	public void setSendDate(Date sendDate) {
		this.sendDate = sendDate;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public String getExtenNumber() {
		return extenNumber;
	}

	public void setExtenNumber(String extenNumber) {
		this.extenNumber = extenNumber;
	}

	public String getDestPhoneNumber() {
		return destPhoneNumber;
	}

	public void setDestPhoneNumber(String destPhoneNumber) {
		this.destPhoneNumber = destPhoneNumber;
	}

	public SmsStatus getSmsStatus() {
		return smsStatus;
	}

	public void setSmsStatus(SmsStatus smsStatus) {
		this.smsStatus = smsStatus;
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
