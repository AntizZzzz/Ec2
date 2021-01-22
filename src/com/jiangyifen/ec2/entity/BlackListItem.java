package com.jiangyifen.ec2.entity;

import java.util.Date;

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
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
@Table(name = "ec2_black_list_item")
public class BlackListItem {
	public static final String TYPE_INCOMING = "incoming";
	public static final String TYPE_OUTGOING = "outgoing";
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "black_list_item")
	@SequenceGenerator(name = "black_list_item", sequenceName = "seq_ec2_black_list_item_id", allocationSize = 1)
	private Long id;

	//电话号码
	@Size(min = 0, max = 64)
	@Column(nullable = false, columnDefinition="character varying(64) NOT NULL")
	private String phoneNumber;
	
	//类型,考虑到黑名单的扩展用type，没有改为direction [incoming ：呼入黑名单； outgoing ： 呼出黑名单]
	@Size(min = 0, max = 64)
	@Column(nullable = false, columnDefinition="character varying(64) NOT NULL")
	private String type;
	
	//加入黑名单原因
	@Size(min = 0, max = 512)
	@Column(nullable = false, columnDefinition="character varying(512) NOT NULL")
	private String reason;
	
	@Column(nullable = false, columnDefinition= "TIMESTAMP WITH TIME ZONE NOT NULL")
	@Temporal(TemporalType.TIMESTAMP)
	private Date createTime;	// 加入日期
	
	//域
	@NotNull
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = Domain.class)
	@JoinColumn(name = "domain_id", nullable = false)
	private Domain domain;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getPhoneNumber() {
		return phoneNumber;
	}
	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}
	
	/** [incoming ：呼入黑名单； outgoing ： 呼出黑名单] */
	public String getType() {
		return type;
	}
	
	/** [incoming ：呼入黑名单； outgoing ： 呼出黑名单] */
	public void setType(String type) {
		this.type = type;
	}
	public String getReason() {
		return reason;
	}
	public void setReason(String reason) {
		this.reason = reason;
	}
	public Domain getDomain() {
		return domain;
	}
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	public void setDomain(Domain domain) {
		this.domain = domain;
	}
	
	

}
