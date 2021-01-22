package com.jiangyifen.ec2.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.Size;

@Entity
@Table(name = "ec2_customer_satisfaction_investigation_log")
public class CustomerSatisfactionInvestigationLog {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "customer_satisfaction_investigation_log")
	@SequenceGenerator(name = "customer_satisfaction_investigation_log", sequenceName = "seq_customer_satisfaction_investigation_log_id", allocationSize = 1)
	private Long id;
	
	@Column(nullable = false, columnDefinition= "TIMESTAMP WITH TIME ZONE NOT NULL")
	@Temporal(TemporalType.TIMESTAMP)
	private Date date = new Date();
	
	/*
	 * 被调查的客户电话号
	 */
	@Size(min = 0, max = 64)
	@Column(columnDefinition="character varying(64)")
	private String customerPhoneNum;
	
	/*
	 * 被评分的话务员当时使用的分机
	 */
	@Size(min = 0, max = 80)
	@Column(columnDefinition="character varying(80)")
	private String exten;
	
	/*
	 * 被评分的话务员当时使用的分机
	 */
	@Size(min = 0, max = 128)
	@Column(columnDefinition="character varying(128)")
	private String direction;
	
	/*
	 * 被评分的话务员id号
	 */
	@Column(columnDefinition="bigint")
	private Long csrId;
	
	/*
	 * 通道标示
	 */
	@Size(min = 0, max = 64)
	@Column(columnDefinition="character varying(64)")
	private String uniqueid;
	
	/*
	 * 评分值
	 */
	@Size(min = 0, max = 64)
	@Column(columnDefinition="character varying(64)")
	private String grade;

	/**
	 * 所属域
	 */
	@Column(columnDefinition="bigint")
    private Long domainId;
    
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getCustomerPhoneNum() {
		return customerPhoneNum;
	}

	public void setCustomerPhoneNum(String customerPhoneNum) {
		this.customerPhoneNum = customerPhoneNum;
	}

	public String getExten() {
		return exten;
	}

	public void setExten(String exten) {
		this.exten = exten;
	}

	public String getDirection() {
		return direction;
	}

	public void setDirection(String direction) {
		this.direction = direction;
	}

	public Long getCsrId() {
		return csrId;
	}

	public void setCsrId(Long csrId) {
		this.csrId = csrId;
	}

	public String getUniqueid() {
		return uniqueid;
	}

	public void setUniqueid(String uniqueid) {
		this.uniqueid = uniqueid;
	}

	public String getGrade() {
		return grade;
	}

	public void setGrade(String grade) {
		this.grade = grade;
	}

	public Long getDomainId() {
		return domainId;
	}

	public void setDomainId(Long domainId) {
		this.domainId = domainId;
	}

}
