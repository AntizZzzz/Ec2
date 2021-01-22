package com.jiangyifen.ec2.entity;

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

@Entity
@Table(name = "ec2_customer_complaint_record")
public class CustomerComplaintRecord {
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "customer_complaint")
	@SequenceGenerator(name = "customer_complaint", sequenceName = "seq_ec2_customer_complaint_record_id", allocationSize = 1)
	private Long id;
	
	//记录的标题
	@Size(min = 0, max = 128)
	@Column(nullable = false,columnDefinition="character varying(128) NOT NULL")
	private String title;
	
	//记录的创建时间
	@Temporal(TemporalType.TIMESTAMP)
	@Column(columnDefinition= "TIMESTAMP WITH TIME ZONE")
	private Date createDate;

	//记录的内容
	@Column(columnDefinition = "TEXT")
	private String recordContent;
	
	public CustomerComplaintRecord() {
	}
	
	//=================对应关系=================//

	//域的概念 批次n-->1域
	//域不能为空
	@NotNull
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = Domain.class)
	private Domain domain;

	//记录n-->1处理者 记录的最终处理者
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = User.class)
	private User owner;

	//记录n-->1输入者 记录输入者
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = User.class)
	private User creator;
	
	//记录n-->1资源  针对哪条资源的记录
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = CustomerResource.class)
	private CustomerResource customerResource;
	
	//记录n-->1项目 针对哪个项目的记录
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = MarketingProject.class)
	private MarketingProject marketingProject;
	
	//记录n-->1状态 针对具体状态的记录
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = CustomerComplaintRecordStatus.class)
	private CustomerComplaintRecordStatus customerComplaintRecordStatus;
	
	//================= getter 和  setter 方法=================//

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getRecordContent() {
		return recordContent;
	}

	public void setRecordContent(String recordContent) {
		this.recordContent = recordContent;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public Domain getDomain() {
		return domain;
	}

	public void setDomain(Domain domain) {
		this.domain = domain;
	}

	public User getOwner() {
		return owner;
	}

	public void setOwner(User owner) {
		this.owner = owner;
	}

	public User getCreator() {
		return creator;
	}

	public void setCreator(User creator) {
		this.creator = creator;
	}

	public CustomerResource getCustomerResource() {
		return customerResource;
	}

	public void setCustomerResource(CustomerResource customerResource) {
		this.customerResource = customerResource;
	}

	public MarketingProject getMarketingProject() {
		return marketingProject;
	}

	public void setMarketingProject(MarketingProject marketingProject) {
		this.marketingProject = marketingProject;
	}

	public CustomerComplaintRecordStatus getCustomerComplaintRecordStatus() {
		return customerComplaintRecordStatus;
	}

	public void setCustomerComplaintRecordStatus(CustomerComplaintRecordStatus customerComplaintRecordStatus) {
		this.customerComplaintRecordStatus = customerComplaintRecordStatus;
	}

	public String toLoggerString() {
		return "CustomerComplaintRecord [id=" + id
				+ ", createDate=" + createDate
				+ ", domain=" + domain
				+ ", creator=" + creator
				+ ", marketingProject=" + marketingProject
				+ "]";
	}
	
}
