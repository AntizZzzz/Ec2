package com.jiangyifen.ec2.report.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import javax.validation.constraints.Size;

import com.sun.istack.internal.NotNull;

@Entity
@Table(name = "ec2_report_project_pool")
// 呼入电话数量统计
public class ProjectPool {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "report_project_pool")
	@SequenceGenerator(name = "report_project_pool", sequenceName = "seq_ec2_report_project_pool_id", allocationSize = 1)
	private Long id;

	/**
	 * 项目名
	 */
	@NotNull
	@Size(min = 1, max = 64)
	private String projectName;
	/**
	 * 任务数
	 */
	@NotNull
	@Size(min = 1, max = 64)
	private int projectTaskCount;
	/**
	 * 接通数
	 */
	@NotNull
	@Size(min = 1, max = 64)
	private int connectCount;
	/**
	 * 接通率
	 */
	@NotNull
	@Size(min = 1, max = 64)
	private double connectRate;
	/**
	 * 任务转移到的项目的名称
	 */
	@NotNull
	@Size(min = 1, max = 64)
	private int moveToProjectName;
	/**
	 * 转移任务的数量
	 */
	@NotNull
	@Size(min = 1, max = 64)
	private int moveToProjectCount;
	/**
	 * 转化率（转移数量/任务总数）
	 */
	@NotNull
	@Size(min = 1, max = 64)
	private double moveRate;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public int getProjectTaskCount() {
		return projectTaskCount;
	}

	public void setProjectTaskCount(int projectTaskCount) {
		this.projectTaskCount = projectTaskCount;
	}

	public int getConnectCount() {
		return connectCount;
	}

	public void setConnectCount(int connectCount) {
		this.connectCount = connectCount;
	}

	public double getConnectRate() {
		return connectRate;
	}

	public void setConnectRate(double connectRate) {
		this.connectRate = connectRate;
	}

	public int getMoveToProjectName() {
		return moveToProjectName;
	}

	public void setMoveToProjectName(int moveToProjectName) {
		this.moveToProjectName = moveToProjectName;
	}

	public int getMoveToProjectCount() {
		return moveToProjectCount;
	}

	public void setMoveToProjectCount(int moveToProjectCount) {
		this.moveToProjectCount = moveToProjectCount;
	}

	public double getMoveRate() {
		return moveRate;
	}

	public void setMoveRate(double moveRate) {
		this.moveRate = moveRate;
	}

}
