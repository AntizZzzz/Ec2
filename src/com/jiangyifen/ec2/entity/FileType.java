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
import javax.validation.constraints.Size;

/**
 * 工单附件
 * @author lxy
 *
 */
@Entity
@Table(name = "ec2_file_type")
public class FileType {

		@Id
		@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "file_type")
		@SequenceGenerator(name = "file_type", sequenceName = "seq_ec2_file_type_id", allocationSize = 1)
		private Long id;
		
		@Column
		@Size(min = 0, max = 20)
		private String name;			//附件名称
		
		@Column
		@Size(min = 0, max = 20)
		private String dotName;
		
		@Column
		@Size(min = 0, max = 20)
		private String typeOne;			//附件名称
		
		@Column
		@Size(min = 0, max = 20)
		private String typeTwo;			//附件名称
		
		@Column
		@Size(min = 0, max = 20)
		private String typeThree;			//附件名称
		
		@Column
		private String describe;			//附件名称
		
		@Column
		private String able;				//可用性
		
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

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getDotName() {
			return dotName;
		}

		public void setDotName(String dotName) {
			this.dotName = dotName;
		}

		public String getTypeOne() {
			return typeOne;
		}

		public void setTypeOne(String typeOne) {
			this.typeOne = typeOne;
		}

		public String getTypeTwo() {
			return typeTwo;
		}

		public void setTypeTwo(String typeTwo) {
			this.typeTwo = typeTwo;
		}

		public String getTypeThree() {
			return typeThree;
		}

		public void setTypeThree(String typeThree) {
			this.typeThree = typeThree;
		}

		public String getDescribe() {
			return describe;
		}

		public void setDescribe(String describe) {
			this.describe = describe;
		}

		public String getAble() {
			return able;
		}

		public void setAble(String able) {
			this.able = able;
		}

		public Domain getDomain() {
			return domain;
		}

		public void setDomain(Domain domain) {
			this.domain = domain;
		}
		
}
