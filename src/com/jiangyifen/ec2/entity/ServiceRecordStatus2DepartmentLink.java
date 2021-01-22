package com.jiangyifen.ec2.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 * 客服记录状态与部门的关系中间表
 * 
 * @author jrh
 *
 */
@Entity
@Table(name = "ec2_service_record_status_2_department_link")
public class ServiceRecordStatus2DepartmentLink {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "service_record_status_2_department_link")
	@SequenceGenerator(name = "service_record_status_2_department_link", sequenceName = "seq_ec2_service_record_status_2_department_link_id", allocationSize = 1)
	private Long id;
	
	@Column(columnDefinition="bigint NOT NULL", nullable=false)
	private Long serviceRecordStatusId;

	@Column(columnDefinition="bigint NOT NULL", nullable=false)
	private Long departmentId;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getServiceRecordStatusId() {
		return serviceRecordStatusId;
	}

	public void setServiceRecordStatusId(Long serviceRecordStatusId) {
		this.serviceRecordStatusId = serviceRecordStatusId;
	}

	public Long getDepartmentId() {
		return departmentId;
	}

	public void setDepartmentId(Long departmentId) {
		this.departmentId = departmentId;
	}

}
