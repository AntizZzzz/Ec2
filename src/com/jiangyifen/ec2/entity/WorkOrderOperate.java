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

/**
 * 工单操作--记录对工单操作的日志，包括评论与操作
 * @author lxy
 *
 */
@Entity
@Table(name = "ec2_workorder_operate")
public class WorkOrderOperate {

		@Id
		@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "workorder_operate")
		@SequenceGenerator(name = "workorder_operate", sequenceName = "seq_ec2_workorder_operate_id", allocationSize = 1)
		private Long id;
		
		@Size(min = 0, max = 20)
		@Column
		private String type; 			//类型
		
		@Column
		private String content;			//评论内容
		
		@Column
		private String isEdit;			//评论是否更新
		
		@Column
		private String operateName;		//操作名称
		
		@Column
		private String originalStatus;	//操作原始状态
		
		@Column
		private String nowStatus;		//操作现在状态
		
		//创建时间
		@Temporal(TemporalType.TIMESTAMP)
		@Column(columnDefinition= "TIMESTAMP WITH TIME ZONE")
		private Date createTime;
		
		//最后更新时间
		@Temporal(TemporalType.TIMESTAMP)
		@Column(columnDefinition= "TIMESTAMP WITH TIME ZONE")
		private Date lastUpdateTime;
		
		@ManyToOne(fetch = FetchType.LAZY, targetEntity = WorkOrder.class)		//所属工单
		private WorkOrder workOrder;
		
		@NotNull
		@ManyToOne(fetch = FetchType.LAZY, targetEntity = Domain.class)
		private Domain domain;// 所属域-公司
		
		/**
		 * 主键
		 * @return
		 */
		public Long getId() {
			return id;
		}

		/**
		 * 主键
		 * @param id
		 */
		public void setId(Long id) {
			this.id = id;
		}

		/**
		 * 类型
		 * @return
		 */
		public String getType() {
			return type;
		}

		/**
		 * 类型
		 * @param type
		 */
		public void setType(String type) {
			this.type = type;
		}

		/**
		 * 评论内容
		 * @return
		 */
		public String getContent() {
			return content;
		}

		/**
		 * 评论内容
		 * @param content
		 */
		public void setContent(String content) {
			this.content = content;
		}
		/**
		 * 评论是否更新
		 * @return
		 */
		public String getIsEdit() {
			return isEdit;
		}
		
		/**
		 * 评论是否更新
		 * @param isEdit
		 */
		public void setIsEdit(String isEdit) {
			this.isEdit = isEdit;
		}

		/**
		 * 操作名称
		 * @return
		 */
		public String getOperateName() {
			return operateName;
		}

		/**
		 * 操作名称
		 * @param operateName
		 */
		public void setOperateName(String operateName) {
			this.operateName = operateName;
		}

		/**
		 * 操作原始状态
		 * @return
		 */
		public String getOriginalStatus() {
			return originalStatus;
		}

		/**
		 * 操作原始状态
		 * @param originalStatus
		 */
		public void setOriginalStatus(String originalStatus) {
			this.originalStatus = originalStatus;
		}

		/**
		 * 操作现在状态
		 * @return
		 */
		public String getNowStatus() {
			return nowStatus;
		}
		
		/**
		 * 操作现在状态
		 * @param nowStatus
		 */
		public void setNowStatus(String nowStatus) {
			this.nowStatus = nowStatus;
		}

		/**
		 * 创建时间
		 * @return
		 */
		public Date getCreateTime() {
			return createTime;
		}

		/**
		 * 创建时间
		 * @param createTime
		 */
		public void setCreateTime(Date createTime) {
			this.createTime = createTime;
		}

		/**
		 * 最后更新时间
		 * @return
		 */
		public Date getLastUpdateTime() {
			return lastUpdateTime;
		}

		/**
		 * 最后更新时间
		 * @param lastUpdateTime
		 */
		public void setLastUpdateTime(Date lastUpdateTime) {
			this.lastUpdateTime = lastUpdateTime;
		}
		
		/**
		 * 所属工单
		 * @return
		 */
		public WorkOrder getWorkOrder() {
			return workOrder;
		}

		/**
		 * 所属工单
		 * @param workOrder
		 */
		public void setWorkOrder(WorkOrder workOrder) {
			this.workOrder = workOrder;
		}
		/**
		 * 所属域-公司
		 * @return
		 */
		public Domain getDomain() {
			return domain;
		}

		/**
		 * 所属域-公司
		 * @param domain
		 */
		public void setDomain(Domain domain) {
			this.domain = domain;
		}
}
