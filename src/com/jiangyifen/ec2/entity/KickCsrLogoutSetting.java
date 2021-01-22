package com.jiangyifen.ec2.entity;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.Size;

import com.jiangyifen.ec2.bean.DayOfWeek;

/**
 * 
 * @Description 描述：定时强制坐席退出系统的配置项
 * 
 * @author  jrh
 * @date    2013年12月19日 下午2:38:51
 * @version v1.0.0
 */
@Entity
@Table(name = "ec2_kick_csr_logout_setting")
public class KickCsrLogoutSetting {
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "kick_csr_logout_setting")
	@SequenceGenerator(name = "kick_csr_logout_setting", sequenceName = "seq_ec2_kick_csr_logout_setting_id", allocationSize = 1)
	private Long id;	// 配置项ID

	@Column(columnDefinition="boolean not null default false", nullable = false)
	private Boolean isLaunch = false;	// 是否执行开启定时任务，默认为没有
	
	@Size(min = 0, max = 32)
	@Column(columnDefinition="character varying(32) NOT NULL", nullable = false)
	private String dayOfWeekType; // 转接日期类型 weekday, weekend, custom(工作日、周末、自定义)

	@Basic(fetch = FetchType.LAZY)
	private Set<DayOfWeek> daysOfWeek = new HashSet<DayOfWeek>();	// 日期选择类型

	@Column(columnDefinition="integer NOT NULL", nullable = false)	// 启动的小时
	private Integer launchHour;

	@Column(columnDefinition="integer NOT NULL", nullable = false)	// 启动的分钟
	private Integer launchMinute;
	
	@Column(columnDefinition="bigint", nullable = false)
	private Long creatorId;		// 创建人
	
	@Size(min = 0, max = 64)
	@Column(columnDefinition="character varying(64)")
	private String realName;	// 真实姓名

	@Size(min = 0, max = 64)
	@Column(nullable = false,unique = true,columnDefinition="character varying(64)")
	private String username;	// 账号名称

	@Size(min = 0, max = 32)
	@Column(nullable = false, columnDefinition="character varying(32)")
	private String empNo;		// 工号

	@Column(columnDefinition="bigint", nullable = false)
	private Long domainId;

	public KickCsrLogoutSetting() {	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Boolean getIsLaunch() {
		return isLaunch;
	}

	public void setIsLaunch(Boolean isLaunch) {
		this.isLaunch = isLaunch;
	}

	public String getDayOfWeekType() {
		return dayOfWeekType;
	}

	public void setDayOfWeekType(String dayOfWeekType) {
		this.dayOfWeekType = dayOfWeekType;
	}

	public Set<DayOfWeek> getDaysOfWeek() {
		return daysOfWeek;
	}

	public void setDaysOfWeek(Set<DayOfWeek> daysOfWeek) {
		this.daysOfWeek = daysOfWeek;
	}

	public Integer getLaunchHour() {
		return launchHour;
	}

	public void setLaunchHour(Integer launchHour) {
		this.launchHour = launchHour;
	}

	public Integer getLaunchMinute() {
		return launchMinute;
	}

	public void setLaunchMinute(Integer launchMinute) {
		this.launchMinute = launchMinute;
	}

	public Long getCreatorId() {
		return creatorId;
	}

	public void setCreatorId(Long creatorId) {
		this.creatorId = creatorId;
	}

	public String getRealName() {
		return realName;
	}

	public void setRealName(String realName) {
		this.realName = realName;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getEmpNo() {
		return empNo;
	}

	public void setEmpNo(String empNo) {
		this.empNo = empNo;
	}

	public Long getDomainId() {
		return domainId;
	}

	public void setDomainId(Long domainId) {
		this.domainId = domainId;
	}

	@Override
	public String toString() {
		return "KickCsrLogoutSetting [id=" + id + ", isLaunch=" + isLaunch
				+ ", dayOfWeekType=" + dayOfWeekType + ", daysOfWeek="
				+ daysOfWeek + ", launchHour=" + launchHour + ", launchMinute="
				+ launchMinute + ", creatorId=" + creatorId + ", realName="
				+ realName + ", username=" + username + ", empNo=" + empNo
				+ ", domainId=" + domainId + "]";
	}

}
