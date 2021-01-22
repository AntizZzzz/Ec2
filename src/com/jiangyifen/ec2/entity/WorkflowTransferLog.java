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
import javax.validation.constraints.Size;

/**
 * 2013-06-13
 * 		工作流迁移日志  (为山西焦炭而作)
 * @author jrh
 *
 */
@Entity
@Table(name = "ec2_workflow_transfer_log")
public class WorkflowTransferLog {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "workflow_transfer_log")
	@SequenceGenerator(name = "workflow_transfer_log", sequenceName = "seq_ec2_workflow_transfer_log_id", allocationSize = 1)
	private Long id;

	//---------------------  关联的资源的各种基础信息    ------------------//
	
	@Column(columnDefinition="bigint NOT NULL", nullable=false)
	private Long customerResourceId;	// 客户id

	@Size(min = 0, max = 64)
	@Column(columnDefinition="character varying(64)")
	private String customerName;		// 客户姓名

	@Size(min = 0, max = 64)
	@Column(columnDefinition="character varying(64) NOT NULL", nullable=false)
	private String phoneNo;				// 客户电话
	
	@Column(columnDefinition= "TIMESTAMP WITH TIME ZONE")
	@Temporal(TemporalType.TIMESTAMP)
	private Date importCustomerTime;	// 原始数据导入时间
	
	@Column(columnDefinition="bigint")
	private Long importorId;			// 导入者Id
	
	@Size(min = 0, max = 32)
	@Column(columnDefinition="character varying(32)")
	private String importorEmpNo;		// 导入者工号
	
	@Size(min = 0, max = 32)
	@Column(columnDefinition="character varying(32)")
	private String importorName;		// 导入者姓名
	
	@Column(columnDefinition="bigint")
	private Long importorDeptId;		// 导入者部门Id

	@Size(min = 0, max = 64)
	@Column(columnDefinition="character varying(64)")
	private String importorDeptName;	// 导入者部门名称
	
	
	//---------------------  第一工作流下话务员获取任务处理任务等信息    ------------------//
	
	@Column(columnDefinition="bigint")
	private Long workflow1ProjectId;		// 土豆项目编号(第一工作流所用项目编号)
	
	@Size(min = 0, max = 64)
	@Column(columnDefinition="character varying(64)")
	private String workflow1ProjectName;		// 土豆项目名称(第一工作流所用项目名称)

	@Column(columnDefinition= "TIMESTAMP WITH TIME ZONE")
	@Temporal(TemporalType.TIMESTAMP)
	private Date migrateWorkflow1Time;		// 转入土豆池时间(转入第一工作流的时间)
	
	@Column(columnDefinition= "TIMESTAMP WITH TIME ZONE")
	@Temporal(TemporalType.TIMESTAMP)
	private Date workflow1PickTaskTime;		// 土豆获取时间(第一工作流话务员获取任务时间)
	
	@Column(columnDefinition="bigint")
	private Long workflow1CsrId;			// 土豆获取人编号 (第一工作流话务员编号)

	@Size(min = 0, max = 32)
	@Column(columnDefinition="character varying(32)")
	private String workflow1CsrEmpNo;		// 土豆获取人工号 (第一工作流话务员工号)

	@Size(min = 0, max = 64)
	@Column(columnDefinition="character varying(64)")
	private String workflow1CsrName;		// 土豆获取人姓名 (第一工作流话务员姓名)
	
	@Size(min = 0, max = 64)
	@Column(columnDefinition="character varying(64)")
	private String workflow1CsrUsername;	// 土豆获取人用户名 (第一工作流话务员用户名)

	@Column(columnDefinition="bigint")
	private Long workflow1CsrDeptId;		// 土豆获取人部门编号 (第一工作流话务员部门编号)

	@Size(min = 0, max = 64)
	@Column(columnDefinition="character varying(64)")
	private String workflow1CsrDeptName;	// 土豆获取人部门名称 (第一工作流话务员部门名称)


	//---------------------  第二工作流下话务员获取任务处理任务等信息    ------------------//

	@Column(columnDefinition="bigint")
	private Long workflow2ProjectId;		// 一销项目编号(第二工作流所用项目编号)

	@Size(min = 0, max = 64)
	@Column(columnDefinition="character varying(64)")
	private String workflow2ProjectName;	// 一销项目名称(第二工作流所用项目名称)
	
	@Column(columnDefinition= "TIMESTAMP WITH TIME ZONE")
	@Temporal(TemporalType.TIMESTAMP)
	private Date migrateWorkflow2Time;		// 转入一销池时间(转入第二工作流的时间)
	
	@Column(columnDefinition= "TIMESTAMP WITH TIME ZONE")
	@Temporal(TemporalType.TIMESTAMP)
	private Date workflow2PickTaskTime;		// 一销获取时间(第二工作流话务员获取任务时间)
	
	@Column(columnDefinition="bigint")
	private Long workflow2CsrId;			// 一销获取人编号 (第二工作流话务员编号)

	@Size(min = 0, max = 32)
	@Column(columnDefinition="character varying(32)")
	private String workflow2CsrEmpNo;		// 一销获取人工号 (第二工作流话务员工号)
	
	@Size(min = 0, max = 64)
	@Column(columnDefinition="character varying(64)")
	private String workflow2CsrName;		// 一销获取人姓名 (第二工作流话务员姓名)
	
	@Size(min = 0, max = 64)
	@Column(columnDefinition="character varying(64)")
	private String workflow2CsrUsername;	// 一销获取人用户名 (第二工作流话务员用户名)

	@Column(columnDefinition="bigint")
	private Long workflow2CsrDeptId;		// 一销获取人部门编号 (第二工作流话务员部门编号)

	@Size(min = 0, max = 64)
	@Column(columnDefinition="character varying(64)")
	private String workflow2CsrDeptName;	// 一销获取人部门名称 (第二工作流话务员部门名称)

	
	//---------------------  第三工作流下话务员获取任务处理任务等信息    ------------------//

	@Column(columnDefinition="bigint")
	private Long workflow3ProjectId;		// 二销项目编号(第三工作流所用项目编号)

	@Size(min = 0, max = 64)
	@Column(columnDefinition="character varying(64)")
	private String workflow3ProjectName;	// 二销项目名称(第三工作流所用项目名称)
	
	@Column(columnDefinition= "TIMESTAMP WITH TIME ZONE")
	@Temporal(TemporalType.TIMESTAMP)
	private Date migrateWorkflow3Time;		// 转入二销池时间(转入第三工作流的时间)
	
	@Column(columnDefinition= "TIMESTAMP WITH TIME ZONE")
	@Temporal(TemporalType.TIMESTAMP)
	private Date workflow3PickTaskTime;		// 二销获取时间(第三工作流话务员获取任务时间)
	
	@Column(columnDefinition="bigint")
	private Long workflow3CsrId;			// 二销获取人编号 (第三工作流话务员编号)

	@Size(min = 0, max = 32)
	@Column(columnDefinition="character varying(32)")
	private String workflow3CsrEmpNo;		// 二销获取人工号 (第三工作流话务员工号)
	
	@Size(min = 0, max = 64)
	@Column(columnDefinition="character varying(64)")
	private String workflow3CsrName;		// 二销获取人姓名 (第三工作流话务员姓名)
	
	@Size(min = 0, max = 64)
	@Column(columnDefinition="character varying(64)")
	private String workflow3CsrUsername;	// 二销获取人用户名 (第三工作流话务员用户名)

	@Column(columnDefinition="bigint")
	private Long workflow3CsrDeptId;		// 二销获取人部门编号 (第三工作流话务员部门编号)

	@Size(min = 0, max = 64)
	@Column(columnDefinition="character varying(64)")
	private String workflow3CsrDeptName;	// 二销获取人部门名称 (第三工作流话务员部门名称)

	@Size(min = 0, max = 512)
	@Column(columnDefinition="character varying(512)")
	private String description;				// 操作描述，暂时不用
	
	//域的概念 批次n-->1域
	@Column(columnDefinition="bigint NOT NULL")
	private Long domainId;					// 当前记录所属域

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getCustomerResourceId() {
		return customerResourceId;
	}

	public void setCustomerResourceId(Long customerResourceId) {
		this.customerResourceId = customerResourceId;
	}

	public String getCustomerName() {
		return customerName;
	}

	public void setCustomerName(String customerName) {
		this.customerName = customerName;
	}

	public String getPhoneNo() {
		return phoneNo;
	}

	public void setPhoneNo(String phoneNo) {
		this.phoneNo = phoneNo;
	}

	public Date getImportCustomerTime() {
		return importCustomerTime;
	}

	public void setImportCustomerTime(Date importCustomerTime) {
		this.importCustomerTime = importCustomerTime;
	}

	public Long getImportorId() {
		return importorId;
	}

	public void setImportorId(Long importorId) {
		this.importorId = importorId;
	}

	public String getImportorEmpNo() {
		return importorEmpNo;
	}

	public void setImportorEmpNo(String importorEmpNo) {
		this.importorEmpNo = importorEmpNo;
	}

	public String getImportorName() {
		return importorName;
	}

	public void setImportorName(String importorName) {
		this.importorName = importorName;
	}

	public Long getImportorDeptId() {
		return importorDeptId;
	}

	public void setImportorDeptId(Long importorDeptId) {
		this.importorDeptId = importorDeptId;
	}

	public String getImportorDeptName() {
		return importorDeptName;
	}

	public void setImportorDeptName(String importorDeptName) {
		this.importorDeptName = importorDeptName;
	}

	public Long getWorkflow1ProjectId() {
		return workflow1ProjectId;
	}

	public void setWorkflow1ProjectId(Long workflow1ProjectId) {
		this.workflow1ProjectId = workflow1ProjectId;
	}

	public String getWorkflow1ProjectName() {
		return workflow1ProjectName;
	}

	public void setWorkflow1ProjectName(String workflow1ProjectName) {
		this.workflow1ProjectName = workflow1ProjectName;
	}

	public Date getMigrateWorkflow1Time() {
		return migrateWorkflow1Time;
	}

	public void setMigrateWorkflow1Time(Date migrateWorkflow1Time) {
		this.migrateWorkflow1Time = migrateWorkflow1Time;
	}

	public Date getWorkflow1PickTaskTime() {
		return workflow1PickTaskTime;
	}

	public void setWorkflow1PickTaskTime(Date workflow1PickTaskTime) {
		this.workflow1PickTaskTime = workflow1PickTaskTime;
	}

	public Long getWorkflow1CsrId() {
		return workflow1CsrId;
	}

	public void setWorkflow1CsrId(Long workflow1CsrId) {
		this.workflow1CsrId = workflow1CsrId;
	}

	public String getWorkflow1CsrEmpNo() {
		return workflow1CsrEmpNo;
	}

	public void setWorkflow1CsrEmpNo(String workflow1CsrEmpNo) {
		this.workflow1CsrEmpNo = workflow1CsrEmpNo;
	}

	public String getWorkflow1CsrName() {
		return workflow1CsrName;
	}

	public void setWorkflow1CsrName(String workflow1CsrName) {
		this.workflow1CsrName = workflow1CsrName;
	}

	public String getWorkflow1CsrUsername() {
		return workflow1CsrUsername;
	}

	public void setWorkflow1CsrUsername(String workflow1CsrUsername) {
		this.workflow1CsrUsername = workflow1CsrUsername;
	}

	public Long getWorkflow1CsrDeptId() {
		return workflow1CsrDeptId;
	}

	public void setWorkflow1CsrDeptId(Long workflow1CsrDeptId) {
		this.workflow1CsrDeptId = workflow1CsrDeptId;
	}

	public String getWorkflow1CsrDeptName() {
		return workflow1CsrDeptName;
	}

	public void setWorkflow1CsrDeptName(String workflow1CsrDeptName) {
		this.workflow1CsrDeptName = workflow1CsrDeptName;
	}

	public Long getWorkflow2ProjectId() {
		return workflow2ProjectId;
	}

	public void setWorkflow2ProjectId(Long workflow2ProjectId) {
		this.workflow2ProjectId = workflow2ProjectId;
	}

	public String getWorkflow2ProjectName() {
		return workflow2ProjectName;
	}

	public void setWorkflow2ProjectName(String workflow2ProjectName) {
		this.workflow2ProjectName = workflow2ProjectName;
	}

	public Date getMigrateWorkflow2Time() {
		return migrateWorkflow2Time;
	}

	public void setMigrateWorkflow2Time(Date migrateWorkflow2Time) {
		this.migrateWorkflow2Time = migrateWorkflow2Time;
	}

	public Date getWorkflow2PickTaskTime() {
		return workflow2PickTaskTime;
	}

	public void setWorkflow2PickTaskTime(Date workflow2PickTaskTime) {
		this.workflow2PickTaskTime = workflow2PickTaskTime;
	}

	public Long getWorkflow2CsrId() {
		return workflow2CsrId;
	}

	public void setWorkflow2CsrId(Long workflow2CsrId) {
		this.workflow2CsrId = workflow2CsrId;
	}

	public String getWorkflow2CsrEmpNo() {
		return workflow2CsrEmpNo;
	}

	public void setWorkflow2CsrEmpNo(String workflow2CsrEmpNo) {
		this.workflow2CsrEmpNo = workflow2CsrEmpNo;
	}

	public String getWorkflow2CsrName() {
		return workflow2CsrName;
	}

	public void setWorkflow2CsrName(String workflow2CsrName) {
		this.workflow2CsrName = workflow2CsrName;
	}

	public String getWorkflow2CsrUsername() {
		return workflow2CsrUsername;
	}

	public void setWorkflow2CsrUsername(String workflow2CsrUsername) {
		this.workflow2CsrUsername = workflow2CsrUsername;
	}

	public Long getWorkflow2CsrDeptId() {
		return workflow2CsrDeptId;
	}

	public void setWorkflow2CsrDeptId(Long workflow2CsrDeptId) {
		this.workflow2CsrDeptId = workflow2CsrDeptId;
	}

	public String getWorkflow2CsrDeptName() {
		return workflow2CsrDeptName;
	}

	public void setWorkflow2CsrDeptName(String workflow2CsrDeptName) {
		this.workflow2CsrDeptName = workflow2CsrDeptName;
	}

	public Long getWorkflow3ProjectId() {
		return workflow3ProjectId;
	}

	public void setWorkflow3ProjectId(Long workflow3ProjectId) {
		this.workflow3ProjectId = workflow3ProjectId;
	}

	public String getWorkflow3ProjectName() {
		return workflow3ProjectName;
	}

	public void setWorkflow3ProjectName(String workflow3ProjectName) {
		this.workflow3ProjectName = workflow3ProjectName;
	}

	public Date getMigrateWorkflow3Time() {
		return migrateWorkflow3Time;
	}

	public void setMigrateWorkflow3Time(Date migrateWorkflow3Time) {
		this.migrateWorkflow3Time = migrateWorkflow3Time;
	}

	public Date getWorkflow3PickTaskTime() {
		return workflow3PickTaskTime;
	}

	public void setWorkflow3PickTaskTime(Date workflow3PickTaskTime) {
		this.workflow3PickTaskTime = workflow3PickTaskTime;
	}

	public Long getWorkflow3CsrId() {
		return workflow3CsrId;
	}

	public void setWorkflow3CsrId(Long workflow3CsrId) {
		this.workflow3CsrId = workflow3CsrId;
	}

	public String getWorkflow3CsrEmpNo() {
		return workflow3CsrEmpNo;
	}

	public void setWorkflow3CsrEmpNo(String workflow3CsrEmpNo) {
		this.workflow3CsrEmpNo = workflow3CsrEmpNo;
	}

	public String getWorkflow3CsrName() {
		return workflow3CsrName;
	}

	public void setWorkflow3CsrName(String workflow3CsrName) {
		this.workflow3CsrName = workflow3CsrName;
	}

	public String getWorkflow3CsrUsername() {
		return workflow3CsrUsername;
	}

	public void setWorkflow3CsrUsername(String workflow3CsrUsername) {
		this.workflow3CsrUsername = workflow3CsrUsername;
	}

	public Long getWorkflow3CsrDeptId() {
		return workflow3CsrDeptId;
	}

	public void setWorkflow3CsrDeptId(Long workflow3CsrDeptId) {
		this.workflow3CsrDeptId = workflow3CsrDeptId;
	}

	public String getWorkflow3CsrDeptName() {
		return workflow3CsrDeptName;
	}

	public void setWorkflow3CsrDeptName(String workflow3CsrDeptName) {
		this.workflow3CsrDeptName = workflow3CsrDeptName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Long getDomainId() {
		return domainId;
	}

	public void setDomainId(Long domainId) {
		this.domainId = domainId;
	}

	@Override
	public String toString() {
		return "WorkflowTransferLog [id=" + id + ", customerResourceId="
				+ customerResourceId + ", phoneNo=" + phoneNo
				+ ", workflow1ProjectId=" + workflow1ProjectId
				+ ", workflow1ProjectName=" + workflow1ProjectName
				+ ", workflow2ProjectId=" + workflow2ProjectId
				+ ", workflow2ProjectName=" + workflow2ProjectName
				+ ", workflow3ProjectId=" + workflow3ProjectId
				+ ", workflow3ProjectName=" + workflow3ProjectName + "]";
	}

}
