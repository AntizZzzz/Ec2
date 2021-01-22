package com.jiangyifen.ec2.report.entity;

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

@Entity
@Table(name = "ec2_report_call_check")
// 呼入电话数量统计
public class CallCheck {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "report_call_check")
	@SequenceGenerator(name = "report_call_check", sequenceName = "seq_ec2_report_call_check_id", allocationSize = 1)
	private Long id;
	/**
	 * 日期(yyyy-MM-dd)
	 */
	@Column(nullable = false, columnDefinition = "date")
	@Temporal(TemporalType.DATE)
	private Date date;
	/**
	 * 时段(24小时进制)
	 */
	@NotNull
	@Size(min = 1, max = 64)
	private int hour;

	/**
	 * 所在域id
	 */
	@NotNull
	@Size(min = 1, max = 64)
	private String domainId;

	/**
	 * 所在项目
	 */
	@NotNull
	@Size(min = 1, max = 64)
	private int projectId;
	/**
	 * 所在部门
	 */
	@NotNull
	@Size(min = 1, max = 64)
	private String deptName;
	/**
	 * 用户名
	 */
	@NotNull
	@Size(min = 1, max = 64)
	private String username;
	/**
	 * 姓名
	 */
	@NotNull
	@Size(min = 1, max = 64)
	private String realName;
	/**
	 * 内部呼叫总数
	 */
	@NotNull
	@Size(min = 1, max = 64)
	private int e_to_e_total_count;
	/**
	 * 内部接通数量
	 */
	@NotNull
	@Size(min = 1, max = 64)
	private int e_to_e_connect_count;
	/**
	 * 内部接通时长
	 */
	@NotNull
	@Size(min = 1, max = 64)
	private int e_to_e_connect_time_length;
	/**
	 * 内部未接通数量
	 */
	@NotNull
	@Size(min = 1, max = 64)
	private int e_to_e_unconnect_count;
	/**
	 * 内部未接通时长
	 */
	@NotNull
	@Size(min = 1, max = 64)
	private int e_to_e_unconnect_time_length;
	/**
	 * 呼出总数
	 */
	@NotNull
	@Size(min = 1, max = 64)
	private int outgoing_total_count;

	/**
	 * 呼出置闲时长（一通电话结束距下通电话开始）
	 */
	@NotNull
	@Size(min = 1, max = 64)
	private int outgoing_free_time_length;

	/**
	 * 呼出总时长
	 */
	@NotNull
	@Size(min = 1, max = 64)
	private int outgoing_total_time_length;
	/**
	 * 呼出接通数量
	 */
	@NotNull
	@Size(min = 1, max = 64)
	private int outgoing_connect_count;
	/**
	 * 呼出接通时长
	 */
	@NotNull
	@Size(min = 1, max = 64)
	private int outgoing_connect_time_length;
	/**
	 * 呼出未接通数量
	 */
	@NotNull
	@Size(min = 1, max = 64)
	private int outgoing_unconnect_count;
	/**
	 * 呼出未接通时长
	 */
	@NotNull
	@Size(min = 1, max = 64)
	private int outgoing_unconnect_time_length;
	/**
	 * 呼入总数
	 */
	@NotNull
	@Size(min = 1, max = 64)
	private int incoming_total_count;

	/**
	 * 呼入时长
	 */
	@NotNull
	@Size(min = 1, max = 64)
	private int incoming_total_time_length;

	/**
	 * 呼入置闲时长
	 */
	@NotNull
	@Size(min = 1, max = 64)
	private int incoming_free_time_length;

	/**
	 * 呼入接通数量
	 */
	@NotNull
	@Size(min = 1, max = 64)
	private int incoming_connect_count;
	/**
	 * 呼入接通时长
	 */
	@NotNull
	@Size(min = 1, max = 64)
	private int incoming_connect_time_length;
	/**
	 * 呼入未接通数量
	 */
	@NotNull
	@Size(min = 1, max = 64)
	private int incoming_unconnect_count;
	/**
	 * 呼入未接通时长
	 */
	@NotNull
	@Size(min = 1, max = 64)
	private int incoming_unconnect_time_length;
	/**
	 * 外传外呼叫总数
	 */
	@NotNull
	@Size(min = 1, max = 64)
	private int o_to_o_total_count;
	/**
	 * 外传外接通数量
	 */
	@NotNull
	@Size(min = 1, max = 64)
	private int o_to_o_connect_count;
	/**
	 * 外传外接通时长
	 */
	@NotNull
	@Size(min = 1, max = 64)
	private int o_to_o_connect_time_length;
	/**
	 * 外传外未接通数量
	 */
	@NotNull
	@Size(min = 1, max = 64)
	private int o_to_o_unconnect_count;
	/**
	 * 外传外未接通时长
	 */
	@NotNull
	@Size(min = 1, max = 64)
	private int o_to_o_unconnect_time_length;
	/**
	 * 总通话数量（包括呼入，呼出，内部，外传外）
	 */
	@NotNull
	@Size(min = 1, max = 64)
	private int call_total_count;
	/**
	 * 总通话时长（包括呼入，呼出，内部，外传外）
	 */
	@NotNull
	@Size(min = 1, max = 64)
	private int call_total_time_length;
	/**
	 * 总接通数量（包括呼入，呼出，内部，外传外）
	 */
	@NotNull
	@Size(min = 1, max = 64)
	private int call_connect_count;
	/**
	 * 总接通时长（包括呼入，呼出，内部，外传外）
	 */
	@NotNull
	@Size(min = 1, max = 64)
	private int call_connect_time_length;
	/**
	 * 总未接通数量（包括呼入，呼出，内部，外传外）
	 */
	@NotNull
	@Size(min = 1, max = 64)
	private int call_unconnect_count;
	/**
	 * 总未接通时长（包括呼入，呼出，内部，外传外）
	 */
	@NotNull
	@Size(min = 1, max = 64)
	private int call_unconnect_time_length;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public int getHour() {
		return hour;
	}

	public void setHour(int hour) {
		this.hour = hour;
	}

	public String getDomainId() {
		return domainId;
	}

	public void setDomainId(String domainId) {
		this.domainId = domainId;
	}

	public int getProjectId() {
		return projectId;
	}

	public void setProjectId(int projectId) {
		this.projectId = projectId;
	}

	public String getDeptName() {
		return deptName;
	}

	public void setDeptName(String deptName) {
		this.deptName = deptName;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getRealName() {
		return realName;
	}

	public void setRealName(String realName) {
		this.realName = realName;
	}

	public int getE_to_e_total_count() {
		return e_to_e_total_count;
	}

	public void setE_to_e_total_count(int e_to_e_total_count) {
		this.e_to_e_total_count = e_to_e_total_count;
	}

	public int getE_to_e_connect_count() {
		return e_to_e_connect_count;
	}

	public void setE_to_e_connect_count(int e_to_e_connect_count) {
		this.e_to_e_connect_count = e_to_e_connect_count;
	}

	public int getE_to_e_connect_time_length() {
		return e_to_e_connect_time_length;
	}

	public void setE_to_e_connect_time_length(int e_to_e_connect_time_length) {
		this.e_to_e_connect_time_length = e_to_e_connect_time_length;
	}

	public int getE_to_e_unconnect_count() {
		return e_to_e_unconnect_count;
	}

	public void setE_to_e_unconnect_count(int e_to_e_unconnect_count) {
		this.e_to_e_unconnect_count = e_to_e_unconnect_count;
	}

	public int getE_to_e_unconnect_time_length() {
		return e_to_e_unconnect_time_length;
	}

	public void setE_to_e_unconnect_time_length(int e_to_e_unconnect_time_length) {
		this.e_to_e_unconnect_time_length = e_to_e_unconnect_time_length;
	}

	public int getOutgoing_total_count() {
		return outgoing_total_count;
	}

	public void setOutgoing_total_count(int outgoing_total_count) {
		this.outgoing_total_count = outgoing_total_count;
	}

	public int getOutgoing_connect_count() {
		return outgoing_connect_count;
	}

	public void setOutgoing_connect_count(int outgoing_connect_count) {
		this.outgoing_connect_count = outgoing_connect_count;
	}

	public int getOutgoing_connect_time_length() {
		return outgoing_connect_time_length;
	}

	public void setOutgoing_connect_time_length(int outgoing_connect_time_length) {
		this.outgoing_connect_time_length = outgoing_connect_time_length;
	}

	public int getOutgoing_unconnect_count() {
		return outgoing_unconnect_count;
	}

	public void setOutgoing_unconnect_count(int outgoing_unconnect_count) {
		this.outgoing_unconnect_count = outgoing_unconnect_count;
	}

	public int getOutgoing_unconnect_time_length() {
		return outgoing_unconnect_time_length;
	}

	public void setOutgoing_unconnect_time_length(
			int outgoing_unconnect_time_length) {
		this.outgoing_unconnect_time_length = outgoing_unconnect_time_length;
	}

	public int getIncoming_total_count() {
		return incoming_total_count;
	}

	public void setIncoming_total_count(int incoming_total_count) {
		this.incoming_total_count = incoming_total_count;
	}

	public int getIncoming_connect_count() {
		return incoming_connect_count;
	}

	public void setIncoming_connect_count(int incoming_connect_count) {
		this.incoming_connect_count = incoming_connect_count;
	}

	public int getIncoming_connect_time_length() {
		return incoming_connect_time_length;
	}

	public void setIncoming_connect_time_length(int incoming_connect_time_length) {
		this.incoming_connect_time_length = incoming_connect_time_length;
	}

	public int getIncoming_unconnect_count() {
		return incoming_unconnect_count;
	}

	public void setIncoming_unconnect_count(int incoming_unconnect_count) {
		this.incoming_unconnect_count = incoming_unconnect_count;
	}

	public int getIncoming_unconnect_time_length() {
		return incoming_unconnect_time_length;
	}

	public void setIncoming_unconnect_time_length(
			int incoming_unconnect_time_length) {
		this.incoming_unconnect_time_length = incoming_unconnect_time_length;
	}

	public int getO_to_o_total_count() {
		return o_to_o_total_count;
	}

	public void setO_to_o_total_count(int o_to_o_total_count) {
		this.o_to_o_total_count = o_to_o_total_count;
	}

	public int getO_to_o_connect_count() {
		return o_to_o_connect_count;
	}

	public void setO_to_o_connect_count(int o_to_o_connect_count) {
		this.o_to_o_connect_count = o_to_o_connect_count;
	}

	public int getO_to_o_connect_time_length() {
		return o_to_o_connect_time_length;
	}

	public void setO_to_o_connect_time_length(int o_to_o_connect_time_length) {
		this.o_to_o_connect_time_length = o_to_o_connect_time_length;
	}

	public int getO_to_o_unconnect_count() {
		return o_to_o_unconnect_count;
	}

	public void setO_to_o_unconnect_count(int o_to_o_unconnect_count) {
		this.o_to_o_unconnect_count = o_to_o_unconnect_count;
	}

	public int getO_to_o_unconnect_time_length() {
		return o_to_o_unconnect_time_length;
	}

	public void setO_to_o_unconnect_time_length(int o_to_o_unconnect_time_length) {
		this.o_to_o_unconnect_time_length = o_to_o_unconnect_time_length;
	}

	public int getOutgoing_total_time_length() {
		return outgoing_total_time_length;
	}

	public void setOutgoing_total_time_length(int outgoing_total_time_length) {
		this.outgoing_total_time_length = outgoing_total_time_length;
	}

	public int getOutgoing_free_time_length() {
		return outgoing_free_time_length;
	}

	public void setOutgoing_free_time_length(int outgoing_free_time_length) {
		this.outgoing_free_time_length = outgoing_free_time_length;
	}

	public int getCall_total_count() {
		return call_total_count;
	}

	public void setCall_total_count(int call_total_count) {
		this.call_total_count = call_total_count;
	}

	public int getCall_connect_count() {
		return call_connect_count;
	}

	public void setCall_connect_count(int call_connect_count) {
		this.call_connect_count = call_connect_count;
	}

	public int getCall_connect_time_length() {
		return call_connect_time_length;
	}

	public void setCall_connect_time_length(int call_connect_time_length) {
		this.call_connect_time_length = call_connect_time_length;
	}

	public int getCall_unconnect_count() {
		return call_unconnect_count;
	}

	public void setCall_unconnect_count(int call_unconnect_count) {
		this.call_unconnect_count = call_unconnect_count;
	}

	public int getCall_unconnect_time_length() {
		return call_unconnect_time_length;
	}

	public void setCall_unconnect_time_length(int call_unconnect_time_length) {
		this.call_unconnect_time_length = call_unconnect_time_length;
	}

	public int getCall_total_time_length() {
		return call_total_time_length;
	}

	public void setCall_total_time_length(int call_total_time_length) {
		this.call_total_time_length = call_total_time_length;
	}

	public int getIncoming_total_time_length() {
		return incoming_total_time_length;
	}

	public void setIncoming_total_time_length(int incoming_total_time_length) {
		this.incoming_total_time_length = incoming_total_time_length;
	}

	public int getIncoming_free_time_length() {
		return incoming_free_time_length;
	}

	public void setIncoming_free_time_length(int incoming_free_time_length) {
		this.incoming_free_time_length = incoming_free_time_length;
	}

	@Override
	public String toString() {
		return "CallCheck [id=" + id + ", date=" + date + ", hour=" + hour
				+ ", domainId=" + domainId + ", projectId=" + projectId
				+ ", deptName=" + deptName + ", username=" + username
				+ ", realName=" + realName + ", e_to_e_total_count="
				+ e_to_e_total_count + ", e_to_e_connect_count="
				+ e_to_e_connect_count + ", e_to_e_connect_time_length="
				+ e_to_e_connect_time_length + ", e_to_e_unconnect_count="
				+ e_to_e_unconnect_count + ", e_to_e_unconnect_time_length="
				+ e_to_e_unconnect_time_length + ", outgoing_total_count="
				+ outgoing_total_count + ", outgoing_free_time_length="
				+ outgoing_free_time_length + ", outgoing_total_time_length="
				+ outgoing_total_time_length + ", outgoing_connect_count="
				+ outgoing_connect_count + ", outgoing_connect_time_length="
				+ outgoing_connect_time_length + ", outgoing_unconnect_count="
				+ outgoing_unconnect_count
				+ ", outgoing_unconnect_time_length="
				+ outgoing_unconnect_time_length + ", incoming_total_count="
				+ incoming_total_count + ", incoming_total_time_length="
				+ incoming_total_time_length + ", incoming_free_time_length="
				+ incoming_free_time_length + ", incoming_connect_count="
				+ incoming_connect_count + ", incoming_connect_time_length="
				+ incoming_connect_time_length + ", incoming_unconnect_count="
				+ incoming_unconnect_count
				+ ", incoming_unconnect_time_length="
				+ incoming_unconnect_time_length + ", o_to_o_total_count="
				+ o_to_o_total_count + ", o_to_o_connect_count="
				+ o_to_o_connect_count + ", o_to_o_connect_time_length="
				+ o_to_o_connect_time_length + ", o_to_o_unconnect_count="
				+ o_to_o_unconnect_count + ", o_to_o_unconnect_time_length="
				+ o_to_o_unconnect_time_length + ", call_total_count="
				+ call_total_count + ", call_total_time_length="
				+ call_total_time_length + ", call_connect_count="
				+ call_connect_count + ", call_connect_time_length="
				+ call_connect_time_length + ", call_unconnect_count="
				+ call_unconnect_count + ", call_unconnect_time_length="
				+ call_unconnect_time_length + "]";
	}

}
