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

/**
 * 工单状态
 * @author lxy
 *
 */
@Entity
@Table(name = "ec2_workorder_status")
public class WorkOrderStatus {

 
		@Id
		@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "workorder_status")
		@SequenceGenerator(name = "workorder_status", sequenceName = "seq_ec2_workorder_status_id", allocationSize = 1)
		private Long id;
		
		@Column(nullable = false)
		private String name;	//名称
		
		@Column
		private String describe;//描述
		
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
		 * 名称
		 * @return
		 */
		public String getName() {
			return name;
		}

		/**
		 * 名称
		 * @param name
		 */
		public void setName(String name) {
			this.name = name;
		}

		/**
		 * 描述:少于255
		 * @return
		 */
		public String getDescribe() {
			return describe;
		}

		/**
		 * 描述:少于255
		 * @param describe
		 */
		public void setDescribe(String describe) {
			this.describe = describe;
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
