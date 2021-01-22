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
import javax.validation.constraints.Size;

import com.jiangyifen.ec2.entity.enumtype.SmsPhoneNumberType;

/**
 * 存储短信电话号码的实体
 * @author chb
 *
 */
@Entity
@Table(name="ec2_smsphonenumber")
public class SmsPhoneNumber {
	
	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="smsphonenumber")
	@SequenceGenerator(name="smsphonenumber", sequenceName="seq_ec2_smsphonenumber_id" ,allocationSize = 1)
	private long id; //短信ID

	@Column(columnDefinition="text NOT NULL")
	private String phoneNumber; //手机号码

	@Column(columnDefinition="TIMESTAMP WITH TIME ZONE NOT NULL")
	@Temporal(TemporalType.TIMESTAMP)
	private Date time; //时间
	
	@Size(min = 0, max = 64)
	private String name; //用户名
	
	@Enumerated
	private SmsPhoneNumberType smsPhoneNumberType;
	
	//========================     关联关系                ===============================//
	
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

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	
	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	public SmsPhoneNumberType getSmsPhoneNumberType() {
		return smsPhoneNumberType;
	}

	public void setSmsPhoneNumberType(SmsPhoneNumberType smsPhoneNumberType) {
		this.smsPhoneNumberType = smsPhoneNumberType;
	}


	public Domain getDomain() {
		return domain;
	}

	public void setDomain(Domain domain) {
		this.domain = domain;
	}
	
	@Override
	public String toString() {
		return phoneNumber+"("+name+")";
	}
	
}
