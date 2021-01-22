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

/**
 * Entity:工单
 * 
 * @author lxy
 * 
 */
@Entity
@Table(name = "ec2_workorder")
public class WorkOrder {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "workorder")
	@SequenceGenerator(name = "workorder", sequenceName = "seq_ec2_workorder_id", allocationSize = 1)
	private Long id;

	@Column
	private String title; // 工单标题

	@Column
	private String content; // 工单内容

	@Column
	private String labels; // 工单标签

	@ManyToOne(fetch = FetchType.LAZY, targetEntity = User.class)
	// 创建者
	private User createUser;

	@ManyToOne(fetch = FetchType.LAZY, targetEntity = User.class)
	// 处理者
	private User handleUser;

	@ManyToOne(fetch = FetchType.LAZY, targetEntity = WorkOrderType.class)
	// 工单类型
	private WorkOrderType workOrderType;

	@ManyToOne(fetch = FetchType.LAZY, targetEntity = WorkOrderStatus.class)
	// 工单状态
	private WorkOrderStatus workOrderStatus;

	@ManyToOne(fetch = FetchType.LAZY, targetEntity = WorkOrderPriority.class)
	// 工单优先级
	private WorkOrderPriority workOrderPriority;

	@ManyToOne(fetch = FetchType.LAZY, targetEntity = WorkOrderHandleResult.class)
	// 工单处理结果
	private WorkOrderHandleResult workOrderHandleResult;

	@ManyToOne(fetch = FetchType.LAZY, targetEntity = MarketingProject.class)
	// 工单所属项目
	private MarketingProject marketingProject;

	@NotNull
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = Domain.class)
	// 工单所属域-公司
	private Domain domain;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
	private Date dueTime; // 预计时间
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
	private Date handleTime; // 处理时间--关闭时时间

	@Temporal(TemporalType.TIMESTAMP)
	@Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
	private Date createTime; // 创建时间

	@Temporal(TemporalType.TIMESTAMP)
	@Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
	private Date lastUpdateTime; // 最后更新时间

	/**
	 * 主键
	 * 
	 * @return
	 */
	public Long getId() {
		return id;
	}

	/**
	 * 主键
	 * 
	 * @param id
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * 工单标题
	 * 
	 * @return
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * 工单标题
	 * 
	 * @param title
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * 工单内容
	 * 
	 * @return
	 */
	public String getContent() {
		return content;
	}

	/**
	 * 工单内容
	 * 
	 * @param content
	 */
	public void setContent(String content) {
		this.content = content;
	}

	/**
	 * 工单标签
	 * 
	 * @return
	 */
	public String getLabels() {
		return labels;
	}

	/**
	 * 工单标签
	 * 
	 * @param labels
	 */
	public void setLabels(String labels) {
		this.labels = labels;
	}

	/**
	 * 创建者
	 * 
	 * @return
	 */
	public User getCreateUser() {
		return createUser;
	}

	/**
	 * 创建者
	 * 
	 * @param createUser
	 */
	public void setCreateUser(User createUser) {
		this.createUser = createUser;
	}

	/**
	 * 处理者
	 * 
	 * @return
	 */
	public User getHandleUser() {
		return handleUser;
	}

	/**
	 * 处理者
	 * 
	 * @param handleUser
	 */
	public void setHandleUser(User handleUser) {
		this.handleUser = handleUser;
	}

	/**
	 * 工单类型
	 * 
	 * @return
	 */
	public WorkOrderType getWorkOrderType() {
		return workOrderType;
	}

	/**
	 * 工单类型
	 * 
	 * @param workOrderType
	 */
	public void setWorkOrderType(WorkOrderType workOrderType) {
		this.workOrderType = workOrderType;
	}

	/**
	 * 工单状态
	 * 
	 * @return
	 */
	public WorkOrderStatus getWorkOrderStatus() {
		return workOrderStatus;
	}

	/**
	 * 工单状态
	 * 
	 * @param workOrderStatus
	 */
	public void setWorkOrderStatus(WorkOrderStatus workOrderStatus) {
		this.workOrderStatus = workOrderStatus;
	}

	/**
	 * 工单优先级
	 * 
	 * @return
	 */
	public WorkOrderPriority getWorkOrderPriority() {
		return workOrderPriority;
	}

	/**
	 * 工单优先级
	 * 
	 * @param workOrderPriority
	 */
	public void setWorkOrderPriority(WorkOrderPriority workOrderPriority) {
		this.workOrderPriority = workOrderPriority;
	}

	/**
	 * 工单处理结果
	 * 
	 * @return
	 */
	public WorkOrderHandleResult getWorkOrderHandleResult() {
		return workOrderHandleResult;
	}

	/**
	 * 工单处理结果
	 * 
	 * @param workOrderHandleResult
	 */
	public void setWorkOrderHandleResult(
			WorkOrderHandleResult workOrderHandleResult) {
		this.workOrderHandleResult = workOrderHandleResult;
	}

	/**
	 * 工单所属项目
	 * 
	 * @return
	 */
	public MarketingProject getMarketingProject() {
		return marketingProject;
	}

	/**
	 * 工单所属项目
	 * 
	 * @param marketingProject
	 */
	public void setMarketingProject(MarketingProject marketingProject) {
		this.marketingProject = marketingProject;
	}

	/**
	 * 工单所属域-公司
	 * 
	 * @return
	 */
	public Domain getDomain() {
		return domain;
	}

	/**
	 * 工单所属域-公司
	 * 
	 * @param domain
	 */
	public void setDomain(Domain domain) {
		this.domain = domain;
	}
	/**
	 * 预计时间
	 * @return
	 */
	public Date getDueTime() {
		return dueTime;
	}
	/**
	 * 预计时间
	 * @param dueTime
	 */
	public void setDueTime(Date dueTime) {
		this.dueTime = dueTime;
	}

	/**
	 * 处理时间--关闭时时间
	 * 
	 * @return
	 */
	public Date getHandleTime() {
		return handleTime;
	}

	/**
	 * 处理时间--关闭时时间
	 * 
	 * @param handleTime
	 */
	public void setHandleTime(Date handleTime) {
		this.handleTime = handleTime;
	}

	/**
	 * 创建时间
	 * 
	 * @return
	 */
	public Date getCreateTime() {
		return createTime;
	}

	/**
	 * 创建时间
	 * 
	 * @param createTime
	 */
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	/**
	 * 最后更新时间
	 * 
	 * @return
	 */
	public Date getLastUpdateTime() {
		return lastUpdateTime;
	}

	/**
	 * 最后更新时间
	 * 
	 * @param lastUpdateTime
	 */
	public void setLastUpdateTime(Date lastUpdateTime) {
		this.lastUpdateTime = lastUpdateTime;
	}
	 
	@Override
	public String toString() {
 		return "Class:["+this.getClass().getName()+"]	"+"[id:"+this.id+"]"+"[title:"+this.title+"]"+"[content:"+this.content+"]"+"[labels:"+this.labels+"]"+"{createUser:"+this.createUser+"}"+"{handleUser:"+this.handleUser+"}"+"{workOrderType:"+this.workOrderType+"}"+"{workOrderStatus:"+this.workOrderStatus+"}"+"{workOrderPriority:"+this.workOrderPriority+"}"+"{workOrderHandleResult:"+this.workOrderHandleResult+"}"+"{marketingProject:"+this.marketingProject+"}"+"{domain:"+this.domain+"}"+"[handleTime:"+this.handleTime+"]"+"[createTime:"+this.createTime+"]"+"[lastUpdateTime:"+this.lastUpdateTime+"]";
	}
	
}
