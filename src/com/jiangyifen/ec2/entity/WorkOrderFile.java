package com.jiangyifen.ec2.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 工单附件
 * @author lxy
 *
 */
@Entity
@Table(name = "ec2_workorder_file")
public class WorkOrderFile {

		@Id
		@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "workorder_file")
		@SequenceGenerator(name = "workorder_file", sequenceName = "seq_ec2_workorder_file_id", allocationSize = 1)
		private Long id;
		
		@Column
		private String name;			//附件名称
		
		@Size(min = 0, max = 20)
		@Column
		private String type; 			//附件类型
		
		@NotNull
		@ManyToOne(fetch = FetchType.LAZY, targetEntity = Domain.class)
		private Domain domain;// 所属域-公司
		
		@ManyToOne(fetch = FetchType.LAZY, targetEntity = WorkOrder.class)		//附件所属工单
		private WorkOrder workOrder;
		
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
		 * 附件名称
		 * @return
		 */
		public String getName() {
			return name;
		}

		/**
		 * 附件名称
		 * @param name
		 */
		public void setName(String name) {
			this.name = name;
		}

		/**
		 * 附件类型
		 * @return
		 */
		public String getType() {
			return type;
		}

		/**
		 * 附件类型
		 * @param type
		 */
		public void setType(String type) {
			this.type = type;
		}

		/**
		 * 附件所属工单
		 * @return
		 */
		public WorkOrder getWorkOrder() {
			return workOrder;
		}

		/**
		 * 附件所属工单
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
