package com.jiangyifen.ec2.entity;

import java.util.Date;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.jiangyifen.ec2.entity.enumtype.BatchStatus;

@Entity
@Table(name = "ec2_customer_resource_batch")
public class CustomerResourceBatch {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "customer_resource_batch")
	@SequenceGenerator(name = "customer_resource_batch", sequenceName = "seq_ec2_customer_resource_batch_id", allocationSize = 1)
	private Long id;

	// 批次名称
	// 批次名称可以重复
	@Size(min = 0, max = 64)
	@Column(nullable = false, columnDefinition = "character varying(64) NOT NULL")
	private String batchName;

	// 备注信息
	@Size(min = 0, max = 512)
	@Column(nullable = false, columnDefinition = "character varying(512) NOT NULL")
	private String note;

	// 创建批次的时间
	@Temporal(TemporalType.TIMESTAMP)
	@Column(columnDefinition = "TIMESTAMP WITH TIME ZONE NOT NULL", nullable = false)
	private Date createDate;

	//批次的停用和启用状态
	@Enumerated 
	private BatchStatus batchStatus;
	
	// 记录批次中资源的的数量
	private Long count;

	public CustomerResourceBatch() {
	}

	// =================对应关系=================//

	// 域的概念 批次n-->1域
	// 域不能为空
	@NotNull
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = Domain.class)
	private Domain domain;

	// 批次n-->n客户资源 一个批次对应多条客户资源数据
	@ManyToMany(targetEntity = CustomerResource.class, fetch = FetchType.LAZY, mappedBy = "customerResourceBatches")
	private Set<CustomerResource> customerResources;

	// 多个创建的批次n-->1个用户
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = User.class)
	private User user;

	// ================= getter 和 setter 方法=================//

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getBatchName() {
		return batchName;
	}

	public void setBatchName(String batchName) {
		this.batchName = batchName;
	}

	public Domain getDomain() {
		return domain;
	}

	public void setDomain(Domain domain) {
		this.domain = domain;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public Set<CustomerResource> getCustomerResources() {
		return customerResources;
	}

	public void setCustomerResources(Set<CustomerResource> customerResources) {
		this.customerResources = customerResources;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Long getCount() {
		return count;
	}
	
	public BatchStatus getBatchStatus() {
		return batchStatus;
	}

	public void setBatchStatus(BatchStatus batchStatus) {
		this.batchStatus = batchStatus;
	}

	/** 记录批次中资源的的数量 */
	public void setCount(Long count) {
		this.count = count;
	}

	public String toLoggerString() {
		return "CustomerResourceBatch [id=" + id + ", batchName=" + batchName
				+ ", domain=" + domain + ", user=" + user.getUsername() + "]";
	}

	@Override
	public String toString() {

		return batchName;
	}

}
