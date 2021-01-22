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
import javax.persistence.UniqueConstraint;

@Entity
@Table(name = "ec2_project_customer", uniqueConstraints={
		@UniqueConstraint(columnNames={"account_project_id","customer_resource_id", "account_manager_id"})})
public class ProjectCustomer {
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "project_customer")
	@SequenceGenerator(name = "project_customer", sequenceName = "seq_ec2_project_customer_id", allocationSize = 1)
	private Long id;
	
	@Column(columnDefinition= "TIMESTAMP WITH TIME ZONE NOT NULL", nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date signDate;
	
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = MarketingProject.class)
	@JoinColumn(columnDefinition = "NOT NULL", name = "account_project_id", nullable = false)
	private MarketingProject accountProject;	// 资源成为了哪一个项目中的客户
	
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = CustomerResource.class)
	@JoinColumn(columnDefinition = "NOT NULL", name = "customer_resource_id", nullable = false)
	private CustomerResource customerResource;	// 成为客户的资源对象
	
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = User.class)
	@JoinColumn(columnDefinition = "NOT NULL", name = "account_manager_id", nullable = false)
	private User accountManager;				// 客户经理

	@ManyToOne(fetch = FetchType.LAZY, targetEntity = Domain.class)
	@JoinColumn(columnDefinition="NOT NULL", name = "domain_id", nullable = false)
	private Domain domain;	//所属域
	
	public Long getId() {
		return id;
	}

	public Date getSignDate() {
		return signDate;
	}

	public MarketingProject getAccountProject() {
		return accountProject;
	}

	public CustomerResource getCustomerResource() {
		return customerResource;
	}

	public User getAccountManager() {
		return accountManager;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setSignDate(Date signDate) {
		this.signDate = signDate;
	}

	public void setAccountProject(MarketingProject accountProject) {
		this.accountProject = accountProject;
	}

	public void setCustomerResource(CustomerResource customerResource) {
		this.customerResource = customerResource;
	}

	public void setAccountManager(User accountManager) {
		this.accountManager = accountManager;
	}

	public Domain getDomain() {
		return domain;
	}

	public void setDomain(Domain domain) {
		this.domain = domain;
	}
	
}
