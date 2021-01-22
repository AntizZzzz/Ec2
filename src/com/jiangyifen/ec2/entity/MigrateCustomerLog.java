package com.jiangyifen.ec2.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;


/**
 *   客户转移操作日志
 * @author jrh
 */

@Entity
@Table(name = "ec2_migrate_customer_log")
public class MigrateCustomerLog {
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "migrate_customer_log")
	@SequenceGenerator(name = "migrate_customer_log", sequenceName = "seq_ec2_migrate_customer_log_id", allocationSize = 1)
	private Long id;	// 记录ID

	@Temporal(TemporalType.TIMESTAMP)
	@Column(nullable = false, columnDefinition="TIMESTAMP WITH TIME ZONE NOT NULL")
	private Date migratedDate;

	//----------------------- 操作者信息-管理员 ------------------//
	@NotNull
	@Column(nullable = false, columnDefinition="bigint NOT NULL")
	private Long operatorUserId;		// （管理员）操作者id
	
	@Size(min = 0, max = 64)
	@Column(nullable = false, columnDefinition="character varying(64) NOT NULL")
	private String operatorUsername;	// （管理员）操作者用户名
	
	@Size(min = 0, max = 32)
	@Column(nullable = false, columnDefinition="character varying(32) NOT NULL")
	private String operatorEmpNo;		// （管理员）操作者工号
	
	@Size(min = 0, max = 64)
	@Column(columnDefinition="character varying(64)")
	private String operatorRealName;	// （管理员）操作者真实姓名

	@NotNull
	@Column(nullable = false, columnDefinition="bigint NOT NULL")
	private Long operatorDeptId;		// （管理员）操作者部门id
	
	@NotNull
	@Size(min = 0, max = 64)
	@Column(nullable = false,columnDefinition="character varying(64) NOT NULL")
	private String operatorDeptName; 	// （管理员）操作者部门名称

	//----------------------- 原客户经理信息-CSR ------------------//
	@NotNull
	@Column(nullable = false, columnDefinition="bigint NOT NULL")
	private Long oldManagerUserId;			// 原客户经理id
	
	@Size(min = 0, max = 64)
	@Column(nullable = false, columnDefinition="character varying(64) NOT NULL")
	private String oldManagerUsername;	// 原客户经理用户名
	
	@Size(min = 0, max = 32)
	@Column(nullable = false, columnDefinition="character varying(32) NOT NULL")
	private String oldManagerEmpNo;		// 原客户经理工号
	
	@Size(min = 0, max = 64)
	@Column(columnDefinition="character varying(64)")
	private String oldManagerRealName;	// 原客户经理真实姓名

	@NotNull
	@Column(nullable = false, columnDefinition="bigint NOT NULL")
	private Long oldManagerDeptId;		// 原客户经理部门id
	
	@NotNull
	@Size(min = 0, max = 64)
	@Column(nullable = false,columnDefinition="character varying(64) NOT NULL")
	private String oldManagerDeptName; 	// 原客户经理部门名称

	//----------------------- 新的客户经理信息-CSR ------------------//
	@NotNull
	@Column(nullable = false, columnDefinition="bigint NOT NULL")
	private Long newManagerUserId;		// 新客户经理id
	
	@Size(min = 0, max = 64)
	@Column(nullable = false, columnDefinition="character varying(64)")
	private String newManagerUsername;	// 新客户经理用户名
	
	@Size(min = 0, max = 32)
	@Column(nullable = false, columnDefinition="character varying(32) NOT NULL")
	private String newManagerEmpNo;		// 新客户经理工号
	
	@Size(min = 0, max = 64)
	@Column(columnDefinition="character varying(64)")
	private String newManagerRealName;	// 新客户经理真实姓名

	@NotNull
	@Column(nullable = false, columnDefinition="bigint NOT NULL")
	private Long newManagerDeptId;		// 新客户经理部门id
	
	@NotNull
	@Size(min = 0, max = 64)
	@Column(nullable = false,columnDefinition="character varying(64) NOT NULL")
	private String newManagerDeptName; 	// 新客户经理部门名称

	//----------------------- 客户信息-CustomerResource ------------------//
	@NotNull
	@Column(nullable = false, columnDefinition="bigint NOT NULL")
	private Long customerId;			// 客户id
	
	@Size(min = 0, max = 64)
	@Column(columnDefinition="character varying(64)")
	private String customerName;		// 客户姓名

	@NotNull
	@Column(columnDefinition="character varying(64) NOT NULL", nullable = false)
	private String customerDefaultPhone;		// 客户电话号码

	@Size(min = 0, max = 255)
	@Column(columnDefinition="character varying(255)")
	private String customerCompanyName;	// 客户公司名称
	
	@NotNull
	@Column(columnDefinition="bigint", nullable = false)
	private Long domainId;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Date getMigratedDate() {
		return migratedDate;
	}

	public void setMigratedDate(Date migratedDate) {
		this.migratedDate = migratedDate;
	}

	public Long getOperatorUserId() {
		return operatorUserId;
	}

	public void setOperatorUserId(Long operatorUserId) {
		this.operatorUserId = operatorUserId;
	}

	public String getOperatorUsername() {
		return operatorUsername;
	}

	public void setOperatorUsername(String operatorUsername) {
		this.operatorUsername = operatorUsername;
	}

	public String getOperatorEmpNo() {
		return operatorEmpNo;
	}

	public void setOperatorEmpNo(String operatorEmpNo) {
		this.operatorEmpNo = operatorEmpNo;
	}

	public String getOperatorRealName() {
		return operatorRealName;
	}

	public void setOperatorRealName(String operatorRealName) {
		this.operatorRealName = operatorRealName;
	}

	public Long getOperatorDeptId() {
		return operatorDeptId;
	}

	public void setOperatorDeptId(Long operatorDeptId) {
		this.operatorDeptId = operatorDeptId;
	}

	public String getOperatorDeptName() {
		return operatorDeptName;
	}

	public void setOperatorDeptName(String operatorDeptName) {
		this.operatorDeptName = operatorDeptName;
	}

	public Long getOldManagerUserId() {
		return oldManagerUserId;
	}

	public void setOldManagerUserId(Long oldManagerUserId) {
		this.oldManagerUserId = oldManagerUserId;
	}

	public String getOldManagerUsername() {
		return oldManagerUsername;
	}

	public void setOldManagerUsername(String oldManagerUsername) {
		this.oldManagerUsername = oldManagerUsername;
	}

	public String getOldManagerEmpNo() {
		return oldManagerEmpNo;
	}

	public void setOldManagerEmpNo(String oldManagerEmpNo) {
		this.oldManagerEmpNo = oldManagerEmpNo;
	}

	public String getOldManagerRealName() {
		return oldManagerRealName;
	}

	public void setOldManagerRealName(String oldManagerRealName) {
		this.oldManagerRealName = oldManagerRealName;
	}

	public Long getOldManagerDeptId() {
		return oldManagerDeptId;
	}

	public void setOldManagerDeptId(Long oldManagerDeptId) {
		this.oldManagerDeptId = oldManagerDeptId;
	}

	public String getOldManagerDeptName() {
		return oldManagerDeptName;
	}

	public void setOldManagerDeptName(String oldManagerDeptName) {
		this.oldManagerDeptName = oldManagerDeptName;
	}

	public Long getNewManagerUserId() {
		return newManagerUserId;
	}

	public void setNewManagerUserId(Long newManagerUserId) {
		this.newManagerUserId = newManagerUserId;
	}

	public String getNewManagerUsername() {
		return newManagerUsername;
	}

	public void setNewManagerUsername(String newManagerUsername) {
		this.newManagerUsername = newManagerUsername;
	}

	public String getNewManagerEmpNo() {
		return newManagerEmpNo;
	}

	public void setNewManagerEmpNo(String newManagerEmpNo) {
		this.newManagerEmpNo = newManagerEmpNo;
	}

	public String getNewManagerRealName() {
		return newManagerRealName;
	}

	public void setNewManagerRealName(String newManagerRealName) {
		this.newManagerRealName = newManagerRealName;
	}

	public Long getNewManagerDeptId() {
		return newManagerDeptId;
	}

	public void setNewManagerDeptId(Long newManagerDeptId) {
		this.newManagerDeptId = newManagerDeptId;
	}

	public String getNewManagerDeptName() {
		return newManagerDeptName;
	}

	public void setNewManagerDeptName(String newManagerDeptName) {
		this.newManagerDeptName = newManagerDeptName;
	}

	public Long getCustomerId() {
		return customerId;
	}

	public void setCustomerId(Long customerId) {
		this.customerId = customerId;
	}

	public String getCustomerName() {
		return customerName;
	}

	public void setCustomerName(String customerName) {
		this.customerName = customerName;
	}

	public String getCustomerDefaultPhone() {
		return customerDefaultPhone;
	}

	public void setCustomerDefaultPhone(String customerDefaultPhone) {
		this.customerDefaultPhone = customerDefaultPhone;
	}

	public String getCustomerCompanyName() {
		return customerCompanyName;
	}

	public void setCustomerCompanyName(String customerCompanyName) {
		this.customerCompanyName = customerCompanyName;
	}

	public Long getDomainId() {
		return domainId;
	}

	public void setDomainId(Long domainId) {
		this.domainId = domainId;
	}
	
}
