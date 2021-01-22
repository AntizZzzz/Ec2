package com.jiangyifen.ec2.entity;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 * 用户与外线的静态对应关系
 * 	一个用户只能对应一条外线
 * @author jrh
 */
@Entity
@Table( name="ec2_user_outline")
public class UserOutline {
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_outline")
	@SequenceGenerator(name = "user_outline", sequenceName = "seq_ec2_user_outline_id", allocationSize = 1)
	private Long id; 
	
	@OneToOne(fetch = FetchType.LAZY, targetEntity = User.class)
	@JoinColumn(columnDefinition = "not null unique", name = "user_id", nullable = false, unique = true)
	private User user;	// 对应用户
	
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = SipConfig.class)
	@JoinColumn(name = "sip_id", nullable = false)
	private SipConfig sip;
    
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = Domain.class)
	@JoinColumn(name = "domain_id", nullable = false)
	private Domain domain;

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

	public SipConfig getSip() {
		return sip;
	}

	public void setSip(SipConfig sip) {
		this.sip = sip;
	}

	public Domain getDomain() {
		return domain;
	}

	public void setDomain(Domain domain) {
		this.domain = domain;
	}

}
