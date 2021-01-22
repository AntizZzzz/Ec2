package com.jiangyifen.ec2.ui.report.tabsheet.advkpi.pojo;

import java.util.Arrays;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.Size;

/**
 * @Description 描述：该实体用来维护高级KPI报表显示表格中 能看到那些属性列，并且各属性列对应的表格头，
 * 
 *              哪些统计信息需要考察【基础信息、登陆信息、置忙信息、
 *              话务总统计详情、呼出话务统计详情、内部话务统计详情、呼入话务统计详情、呼入漏接统计详情】
 *
 * @author JRH
 * @date 2014年5月21日 下午3:20:43
 * @version v1.0.0
 */
@Entity
@Table(name = "ec2_kpi_col")
public class KpiTableColVo {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ec2_kpi_col")
	@SequenceGenerator(name = "ec2_kpi_col", sequenceName = "ec2_kpi_col_id", allocationSize = 1)
	private Long id;
	private Object[] visibleCols = new Object[] {}; // 可见列属性
	private String[] visibleHeaders = new String[] {}; // 可见列表头
//	@Size(min = 0, max = 3000)
//	@Column(columnDefinition = "character varying(3000)")
//	private String visibleColsStr = "";
//	@Size(min = 0, max = 3000)
//	@Column(columnDefinition = "character varying(3000)")
//	private String visibleHeadersStr = "";
	@Size(min = 0, max = 64)
	@Column(columnDefinition = "character varying(64)")
	private boolean baseVisible = false; // 基础信息是否可见
	@Size(min = 0, max = 64)
	@Column(columnDefinition = "character varying(64)")
	private boolean loginVisible = false; // 登陆信息是否可见
	@Size(min = 0, max = 64)
	@Column(columnDefinition = "character varying(64)")
	private boolean pauseVisible = false; // 置忙信息是否可见
	@Size(min = 0, max = 64)
	@Column(columnDefinition = "character varying(64)")
	private boolean totalCallVisible = false; // 话务总统计详情是否可见
	@Size(min = 0, max = 64)
	@Column(columnDefinition = "character varying(64)")
	private boolean callOutVisible = false; // 呼出话务统计详情是否可见
	@Size(min = 0, max = 64)
	@Column(columnDefinition = "character varying(64)")
	private boolean callInnerVisible = false; // 内部话务统计详情是否可见
	@Size(min = 0, max = 64)
	@Column(columnDefinition = "character varying(64)")
	private boolean callInVisible = false; // 呼入话务统计详情是否可见
	@Size(min = 0, max = 64)
	@Column(columnDefinition = "character varying(64)")
	private boolean missCallInVisible = false; // 呼入漏接统计详情是否可见

	public Object[] getVisibleCols() {
		return visibleCols;
	}

	public void setVisibleCols(Object[] visibleCols) {
		this.visibleCols = visibleCols;
	}

	public String[] getVisibleHeaders() {
		return visibleHeaders;
	}

	public void setVisibleHeaders(String[] visibleHeaders) {
		this.visibleHeaders = visibleHeaders;
	}

	public boolean isBaseVisible() {
		return baseVisible;
	}

	public void setBaseVisible(boolean baseVisible) {
		this.baseVisible = baseVisible;
	}

	public boolean isLoginVisible() {
		return loginVisible;
	}

	public void setLoginVisible(boolean loginVisible) {
		this.loginVisible = loginVisible;
	}

	public boolean isPauseVisible() {
		return pauseVisible;
	}

	public void setPauseVisible(boolean pauseVisible) {
		this.pauseVisible = pauseVisible;
	}

	public boolean isTotalCallVisible() {
		return totalCallVisible;
	}

	public void setTotalCallVisible(boolean totalCallVisible) {
		this.totalCallVisible = totalCallVisible;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

//	public String getVisibleColsStr() {
//		return visibleColsStr;
//	}
//
//	public void setVisibleColsStr(String visibleColsStr) {
//		this.visibleColsStr = visibleColsStr;
//	}
//
//	public String getVisibleHeadersStr() {
//		return visibleHeadersStr;
//	}
//
//	public void setVisibleHeadersStr(String visibleHeadersStr) {
//		this.visibleHeadersStr = visibleHeadersStr;
//	}

	public boolean isCallOutVisible() {
		return callOutVisible;
	}

	public void setCallOutVisible(boolean callOutVisible) {
		this.callOutVisible = callOutVisible;
	}

	public boolean isCallInnerVisible() {
		return callInnerVisible;
	}

	public void setCallInnerVisible(boolean callInnerVisible) {
		this.callInnerVisible = callInnerVisible;
	}

	public boolean isCallInVisible() {
		return callInVisible;
	}

	public void setCallInVisible(boolean callInVisible) {
		this.callInVisible = callInVisible;
	}

	public boolean isMissCallInVisible() {
		return missCallInVisible;
	}

	public void setMissCallInVisible(boolean missCallInVisible) {
		this.missCallInVisible = missCallInVisible;
	}

	public String toLoggerString() {
		return "\r\n KpiTableColVo [visibleCols=" + Arrays.toString(visibleCols) + "\r\n, visibleHeaders="
				+ Arrays.toString(visibleHeaders) + "\r\n, baseVisible=" + baseVisible + ", loginVisible="
				+ loginVisible + ", pauseVisible=" + pauseVisible + ", totalCallVisible=" + totalCallVisible
				+ ", callOutVisible=" + callOutVisible + ", callInnerVisible=" + callInnerVisible + ", callInVisible="
				+ callInVisible + "]";
	}

	@Override
	public String toString() {
		return "KpiTableColVo [id=" + id + ", visibleCols=" + Arrays.toString(visibleCols) + ", visibleHeaders="
				+ Arrays.toString(visibleHeaders) + ", baseVisible=" + baseVisible + ", loginVisible=" + loginVisible
				+ ", pauseVisible=" + pauseVisible + ", totalCallVisible=" + totalCallVisible + ", callOutVisible="
				+ callOutVisible + ", callInnerVisible=" + callInnerVisible + ", callInVisible=" + callInVisible
				+ ", missCallInVisible=" + missCallInVisible + "]";
	}

}
