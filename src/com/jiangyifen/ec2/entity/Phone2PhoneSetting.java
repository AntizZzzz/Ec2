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
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.jiangyifen.ec2.bean.DayOfWeek;

/**
 * 外转外配置项
 * @author jrh
 */
@Entity
@Table(name = "ec2_phone2phone_setting")
public class Phone2PhoneSetting  {
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "phone2phone_setting")
	@SequenceGenerator(name = "phone2phone_setting", sequenceName = "seq_ec2_phone2phone_setting_id", allocationSize = 1)
	private Long id;	// 配置项ID
	
	@Size(min = 0, max = 32)
	@Column(nullable = false, columnDefinition="character varying(32) NOT NULL")
	private String startTime;	// 开始 呼叫手机时间 18:00
	
	@Size(min = 0, max = 32)
	@Column(nullable = false, columnDefinition="character varying(32) NOT NULL")
	private String stopTime;	// 停止呼叫手机时间 23:59
	
	@Size(min = 0, max = 32)
	@Column(nullable = false, columnDefinition="character varying(32) NOT NULL")
	private String dayOfWeekType; // 转接日期类型 weekday, weekend, custom(工作日、周末、自定义)

	@Basic(fetch = FetchType.LAZY)
	private Set<DayOfWeek> daysOfWeek = new HashSet<DayOfWeek>();	// 日期选择类型

	@Basic(fetch = FetchType.LAZY)
	@Column(nullable = false)
	private Set<String> redirectTypes = new HashSet<String>(); 		// 哪些情况需要转呼手机noanswer(无人接听)、unonline(不在线)、busy(用户忙)、force(强制直接呼手机)
	
	@Column(columnDefinition="integer NOT NULL default 10")
	private Integer noanswerTimeout = 10;		// 配置多长时间内没有话务员接听电话，转接手机
	
	@Column(columnDefinition="boolean not null default false", nullable = false)
	private Boolean isStartedRedirect = false;	// 是否开始呼手机，默认为没有

	@Column(columnDefinition="boolean not null default false", nullable = false)
	private Boolean isGlobalSetting = false;	// 是否全局配置，默认为不是
	
	/*
	 *  只有管理员可以进行的配置项
	 */
	@Column(columnDefinition="boolean")
	private Boolean isLicensed2Csr;	// 是否授权给话务员进行自定义设置	（这个是给管理员设置用的，话务员界面不操作该字段）

	@Column(columnDefinition="boolean")
	private Boolean isSpecifiedPhones;	// 客户呼入时，是否指定呼某些手机号,如果不是，则转呼到话务员自己拥有的手机

	@Basic(fetch = FetchType.LAZY)
	private Set<String> specifiedPhones = new HashSet<String>();	// 强制规定要转呼的手机号
	
	/*
	 * 关联对象 
	 */
	@OneToOne(fetch = FetchType.LAZY, targetEntity = User.class)
	@JoinColumn(name = "user_id", nullable = false)
	private User creator;	// 创建者
	
    @NotNull
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = Domain.class)
	@JoinColumn(name = "domain_id", nullable = false)
	private Domain domain;	//所属公司
    
	/*
	 *  只有管理员可以进行的配置项
	 */
	@OneToMany(fetch = FetchType.LAZY, targetEntity = User.class)
	@JoinTable(name = "ec2_phone2phone_setting_user_link",
				joinColumns = @JoinColumn(name = "phone2phone_setting_id"), 
				inverseJoinColumns = @JoinColumn(name = "user_id")
			)
	private Set<User> specifiedCsrs = new HashSet<User>();	// 客户呼入时，被强制指定需要转呼手机的话务员（该字段的设计是由管理员设计的，话务员不操作）

	public Phone2PhoneSetting() {	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getStopTime() {
		return stopTime;
	}

	public void setStopTime(String stopTime) {
		this.stopTime = stopTime;
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

	public Set<String> getRedirectTypes() {
		return redirectTypes;
	}

	public void setRedirectTypes(Set<String> redirectTypes) {
		this.redirectTypes = redirectTypes;
	}

	public Integer getNoanswerTimeout() {
		return noanswerTimeout;
	}

	public void setNoanswerTimeout(Integer noanswerTimeout) {
		this.noanswerTimeout = noanswerTimeout;
	}

	public Boolean getIsStartedRedirect() {
		return isStartedRedirect;
	}

	public void setIsStartedRedirect(Boolean isStartedRedirect) {
		this.isStartedRedirect = isStartedRedirect;
	}

	public Boolean getIsGlobalSetting() {
		return isGlobalSetting;
	}

	public void setIsGlobalSetting(Boolean isGlobalSetting) {
		this.isGlobalSetting = isGlobalSetting;
	}

	public Boolean getIsLicensed2Csr() {
		return isLicensed2Csr;
	}

	public void setIsLicensed2Csr(Boolean isLicensed2Csr) {
		this.isLicensed2Csr = isLicensed2Csr;
	}

	public Boolean getIsSpecifiedPhones() {
		return isSpecifiedPhones;
	}

	public void setIsSpecifiedPhones(Boolean isSpecifiedPhones) {
		this.isSpecifiedPhones = isSpecifiedPhones;
	}

	public Set<String> getSpecifiedPhones() {
		return specifiedPhones;
	}

	public void setSpecifiedPhones(Set<String> specifiedPhones) {
		this.specifiedPhones = specifiedPhones;
	}

	public User getCreator() {
		return creator;
	}

	public void setCreator(User creator) {
		this.creator = creator;
	}

	public Domain getDomain() {
		return domain;
	}

	public void setDomain(Domain domain) {
		this.domain = domain;
	}

	public Set<User> getSpecifiedCsrs() {
		return specifiedCsrs;
	}

	public void setSpecifiedCsrs(Set<User> specifiedCsrs) {
		this.specifiedCsrs = specifiedCsrs;
	}

	public String toLoggerString() {
		return "Phone2PhoneSetting [id=" + id + ", startTime=" + startTime
				+ ", stopTime=" + stopTime + ", dayOfWeekType=" + dayOfWeekType
				+ ", redirectTypes=" + redirectTypes + ", isStartedRedirect="
				+ isStartedRedirect + ", isGlobalSetting=" + isGlobalSetting
				+ ", isLicensed2Csr=" + isLicensed2Csr + ", isSpecifiedPhones="
				+ isSpecifiedPhones + ", creator=" + creator + ", domain="
				+ domain + "]";
	}

}
