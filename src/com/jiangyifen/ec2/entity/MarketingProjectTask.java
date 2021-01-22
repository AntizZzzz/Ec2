package com.jiangyifen.ec2.entity;

import java.util.Date;

import javax.persistence.Column;
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
import javax.validation.constraints.Size;

import com.jiangyifen.ec2.entity.enumtype.CustomerQuestionnaireFinishStatus;
import com.jiangyifen.ec2.entity.enumtype.MarketingProjectTaskType;

@Entity
@Table(name = "ec2_marketing_project_task")
public class MarketingProjectTask {
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "marketing_project_task")
	@SequenceGenerator(name = "marketing_project_task", sequenceName = "seq_ec2_marketing_project_task_id", allocationSize = 1)
	private Long id;

	//任务状态
	@Size(min = 0, max = 32)
	@Column(columnDefinition="character varying(32)")
	private String lastStatus;
	
	// 呼叫是否完成   默认 false 未完成
	// 注释中设置的默认值 用于直接用 SQL语句向数据库 中插入数据时 自动生成默认值
	@Column(columnDefinition="boolean not null default false", nullable = false)
	private Boolean isFinished = false;		// 这里设默认值，是用于在系统界面保存生成的数据时，使得插入的数据默认值为false
	
	// 呼叫是否接通   默认 false 为应答
	@Column(columnDefinition="boolean not null default false", nullable = false)
	private Boolean isAnswered = false;
	
	// 完成任务的最新时间
	@Temporal(TemporalType.TIMESTAMP)
	@Column(columnDefinition= "TIMESTAMP WITH TIME ZONE")
	private Date lastUpdateDate;

	// 完成任务的最新时间
	@Temporal(TemporalType.TIMESTAMP)
	@Column(columnDefinition= "TIMESTAMP WITH TIME ZONE")
	private Date distributeTime;

	//自动外呼的Id值
	private Long autodialId;
	
	//自动外呼名称
	@Size(min = 0, max = 64)
	@Column(columnDefinition="character varying(64)")
	private String autodialName;
	
	//自动外呼是否接通
	private Boolean autodialIsAnswered;

	//自动外呼坐席是否接起
	private Boolean autodialIsCsrPickup;
	
	//--------------------------- chb 通过批次对Task的可用性全局配置 2013-6-24 -----------------------//
	//该Task是否可用
	private Boolean isUseable = true;
	//记录批次的Id值
	private Long batchId;
	
	
	// 自动外呼发起呼叫时间
	@Temporal(TemporalType.TIMESTAMP)
	@Column(columnDefinition= "TIMESTAMP WITH TIME ZONE")
	private Date autodialTime;

	// 自动外呼客户应答时间
	@Temporal(TemporalType.TIMESTAMP)
	@Column(columnDefinition= "TIMESTAMP WITH TIME ZONE")
	private Date autodialAnsweredTime;

	// 自动外呼客服接起电话时间
	@Temporal(TemporalType.TIMESTAMP)
	@Column(columnDefinition= "TIMESTAMP WITH TIME ZONE")
	private Date autodialPickupTime;

	//--------------------------- jrh 为山西焦炭设定 2013-6-17 -----------------------//
	// 预约时间 
	@Temporal(TemporalType.TIMESTAMP)
	@Column(columnDefinition= "TIMESTAMP WITH TIME ZONE")
	private Date orderTime;

	// 预约备注
	@Size(min = 0,max = 1024)
	@Column(columnDefinition="character varying(1024)")
	private String orderNote;	
	
	//--------------------------- jrh 为问卷而设定 2013-6-18 -----------------------//
	
	// 客户完成问卷任务的状态
	@Enumerated
	private CustomerQuestionnaireFinishStatus customerQuestionnaireFinishStatus;
	
	//--------------------------- jrh 为完善功能而加 2013-6-17 -----------------------//
	// 任务类型 ，目前任务类型跟类型相同 	默认值为：营销任务
	@Enumerated 
	private MarketingProjectTaskType marketingProjectTaskType = MarketingProjectTaskType.MARKETING;
	
	
	//--------------------------- jrh 为实现工作流迁移日志而加【山西焦炭】 2013-7-30 -----------------------//
	// 迁移日志的编号	
	@Column(columnDefinition="bigint")
	private Long workflowTransferLogId;

	//--------------------------- jrh 普遍适用【山西焦炭】 2013-7-30 -----------------------//

	// 任务生成时间
	@Temporal(TemporalType.TIMESTAMP)
	@Column(columnDefinition= "TIMESTAMP WITH TIME ZONE")
	private Date createTime;
	
	public MarketingProjectTask() {
	}
	
	@Override
	public int hashCode() {
		return id.intValue();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MarketingProjectTask other = (MarketingProjectTask) obj;
		if (id == null||other.id == null) {
			return false;
		}
		return id.intValue()==other.id.intValue();
	}

	//=================对应关系=================//

	//任务n-->1用户   多个任务由一个用户完成
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = User.class)
	private User user;
	
	//任务n-->1项目   多个任务属于同一个项目
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = CustomerResource.class)
	private CustomerResource customerResource;
	
	//任务n-->1资源   同一资源可能被重复利用
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = MarketingProject.class)
	private MarketingProject marketingProject;
	
	//域的概念 批次1-->n域
	//域不能为空
	@NotNull
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = Domain.class)
	private Domain domain;

	//================= getter 和  setter 方法=================//
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getLastStatus() {
		return lastStatus;
	}

	public void setLastStatus(String lastStatus) {
		this.lastStatus = lastStatus;
	}

	public Date getLastUpdateDate() {
		return lastUpdateDate;
	}

	public void setLastUpdateDate(Date lastUpdateDate) {
		this.lastUpdateDate = lastUpdateDate;
	}
	
	public Date getDistributeTime() {
		return distributeTime;
	}

	public void setDistributeTime(Date distributeTime) {
		this.distributeTime = distributeTime;
	}

	public CustomerQuestionnaireFinishStatus getCustomerQuestionnaireFinishStatus() {
		return customerQuestionnaireFinishStatus;
	}

	public void setCustomerQuestionnaireFinishStatus(
			CustomerQuestionnaireFinishStatus customerQuestionnaireFinishStatus) {
		this.customerQuestionnaireFinishStatus = customerQuestionnaireFinishStatus;
	}

	public MarketingProjectTaskType getMarketingProjectTaskType() {
		return marketingProjectTaskType;
	}

	public void setMarketingProjectTaskType(
			MarketingProjectTaskType marketingProjectTaskType) {
		this.marketingProjectTaskType = marketingProjectTaskType;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
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

	public Domain getDomain() {
		return domain;
	}

	public void setDomain(Domain domain) {
		this.domain = domain;
	}

	public Boolean getIsFinished() {
		return isFinished;
	}

	public void setIsFinished(Boolean isFinished) {
		this.isFinished = isFinished;
	}

	public Boolean getIsAnswered() {
		return isAnswered;
	}

	public void setIsAnswered(Boolean isAnswered) {
		this.isAnswered = isAnswered;
	}

	public Long getAutodialId() {
		return autodialId;
	}

	public void setAutodialId(Long autodialId) {
		this.autodialId = autodialId;
	}

	public String getAutodialName() {
		return autodialName;
	}

	public void setAutodialName(String autodialName) {
		this.autodialName = autodialName;
	}

	public Boolean getAutodialIsAnswered() {
		return autodialIsAnswered;
	}

	public void setAutodialIsAnswered(Boolean autodialIsAnswered) {
		this.autodialIsAnswered = autodialIsAnswered;
	}

	public Boolean getAutodialIsCsrPickup() {
		return autodialIsCsrPickup;
	}

	public void setAutodialIsCsrPickup(Boolean autodialIsCsrPickup) {
		this.autodialIsCsrPickup = autodialIsCsrPickup;
	}

	public Long getBatchId() {
		return batchId;
	}

	public void setBatchId(Long batchId) {
		this.batchId = batchId;
	}

	public Date getAutodialTime() {
		return autodialTime;
	}

	public void setAutodialTime(Date autodialTime) {
		this.autodialTime = autodialTime;
	}

	public Date getAutodialAnsweredTime() {
		return autodialAnsweredTime;
	}

	public void setAutodialAnsweredTime(Date autodialAnsweredTime) {
		this.autodialAnsweredTime = autodialAnsweredTime;
	}

	public Date getAutodialPickupTime() {
		return autodialPickupTime;
	}

	public void setAutodialPickupTime(Date autodialPickupTime) {
		this.autodialPickupTime = autodialPickupTime;
	}

	
	public Boolean getIsUseable() {
		return isUseable;
	}

	public void setIsUseable(Boolean isUseable) {
		this.isUseable = isUseable;
	}

	public Date getOrderTime() {
		return orderTime;
	}

	public void setOrderTime(Date orderTime) {
		this.orderTime = orderTime;
	}

	public String getOrderNote() {
		return orderNote;
	}

	public void setOrderNote(String orderNote) {
		this.orderNote = orderNote;
	}

	public Long getWorkflowTransferLogId() {
		return workflowTransferLogId;
	}

	public void setWorkflowTransferLogId(Long workflowTransferLogId) {
		this.workflowTransferLogId = workflowTransferLogId;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	@Override
	public String toString() {
		return "MarketingProjectTask [id=" + id + ", isFinished=" + isFinished
				+ ", isUseable=" + isUseable + ", orderTime=" + orderTime
				+ ", marketingProjectTaskType=" + marketingProjectTaskType
				+ ", marketingProject=" + marketingProject + "]";
	}

}
