package com.jiangyifen.ec2.entity;

import java.util.Date;

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
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

import com.jiangyifen.ec2.entity.enumtype.MessageType;

/**
 * 短信实体
 *
 */
@Entity
@Table(name="ec2_message")
public class Message {
	
	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="message")
	@SequenceGenerator(name="message", sequenceName="seq_ec2_message_id" ,allocationSize = 1)
	private long id; //短信ID

	@Column(columnDefinition="text NOT NULL")
	private String phoneNumber; //手机号码

	
	@Column(columnDefinition="text")
	private String content; 	//内容
	
	@Column(columnDefinition="TIMESTAMP WITH TIME ZONE NOT NULL")
	@Temporal(TemporalType.TIMESTAMP)
	private Date time; 			//时间
	
	@Enumerated
	private MessageType messageType;
	
	//========================     关联关系                ===============================//
	@ManyToOne(fetch = FetchType.LAZY, targetEntity=User.class)
	@JoinColumn(nullable = false)
	private User user; //短信----->用户     n------>1
	
	
	
	@NotNull
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = Domain.class)
	@JoinColumn(name = "domain_id", nullable = false)
	private Domain domain;	   //所属 域


	//========================     Getter and Setter方法        ====================//
	public long getId() {
		return id;
	}
	
	public void setId(long id) {
		this.id = id;
	}
	
	public MessageType getMessageType() {
		return messageType;
	}

	public void setMessageType(MessageType messageType) {
		this.messageType = messageType;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getContent() {
		return content;
	}
	
	public void setContent(String content) {
		this.content = content;
	}
	
	public Date getTime() {
		return time;
	}
	
	public void setTime(Date time) {
		this.time = time;
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

	@Override
	public String toString() {
		return "Message [id=" + id + ", phoneNumber=" + phoneNumber + ", time="
				+ time + ", content=" + content + ", messageType="+
				 ", user=" + user + "]";
	}
	
	
}
