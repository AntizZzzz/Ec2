package com.jiangyifen.ec2.entity;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
/**
 * 发送短信的相关信息 
 * @author chb
 */
@Entity
@Table(name = "ec2_smsinfo")
public class SmsInfo {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "smsinfo")
	@SequenceGenerator(name = "smsinfo", sequenceName = "seq_ec2_smsinfo_id", allocationSize = 1)
	private Long id;
	
	private String smsChannelName = ""; //所使用的发短息的模块名称，就是Spring配置中对应的实现发送短信接口的单例
	private String companyName = ""; 
	private String userName = "";
	private String password = "";
	private String description = "";

	@ManyToOne(fetch = FetchType.LAZY, targetEntity = Domain.class)
	@JoinColumn(columnDefinition="NOT NULL", name = "domain_id")
	private Domain domain; // 所属公司

	//Getter and Setter
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getSmsChannelName() {
		return smsChannelName;
	}

	public void setSmsChannelName(String smsChannelName) {
		this.smsChannelName = smsChannelName;
	}

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Domain getDomain() {
		return domain;
	}

	public void setDomain(Domain domain) {
		this.domain = domain;
	}

}
