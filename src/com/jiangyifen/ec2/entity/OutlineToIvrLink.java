package com.jiangyifen.ec2.entity;

import java.text.SimpleDateFormat;
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

/**
 * 
 * @Description 描述：外线与IVR 的关联关系
 * 
 * @author  jrh
 * @date    2014年3月5日 下午5:21:46
 * @version v1.0.0
 */
@Entity
@Table(name = "ec2_outline_to_ivr_link")
public class OutlineToIvrLink {

	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ivr_menu_to_outline_link")
	@SequenceGenerator(name = "ivr_menu_to_outline_link", sequenceName = "seq_ec2_ivr_menu_to_outline_link_id", allocationSize = 1)
	private Long id;

	// IVRMenu编号
	@Column(columnDefinition="bigint NOT NULL", nullable=false)
	private Long ivrMenuId;

	// 外线编号
	@Column(columnDefinition="bigint NOT NULL", nullable=false)
	private Long outlineId;

	// 创建时间
	@Temporal(TemporalType.TIMESTAMP)
	@Column(columnDefinition = "TIMESTAMP WITH TIME ZONE NOT NULL", nullable = false)
	private Date createDate;
	
	@Column(columnDefinition="integer NOT NULL", nullable=false)
	private Integer priority;		// 优先级

	@Temporal(TemporalType.TIMESTAMP)
	@Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
	private Date effectDate;		// 起效日期

	@Temporal(TemporalType.TIMESTAMP)
	@Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
	private Date expireDate;		// 失效日期
	
	@Column(columnDefinition="boolean not null default false", nullable = false)
	private Boolean isSunEffect = false;		// 星期日是否生效，

	@Column(columnDefinition="boolean not null default false", nullable = false)
	private Boolean isMonEffect = false;		// 星期一是否生效，

	@Column(columnDefinition="boolean not null default false", nullable = false)
	private Boolean isTueEffect = false;		// 星期二是否生效，
	
	@Column(columnDefinition="boolean not null default false", nullable = false)
	private Boolean isWedEffect = false;		// 星期三是否生效，
	
	@Column(columnDefinition="boolean not null default false", nullable = false)
	private Boolean isThuEffect = false;		// 星期四是否生效，
	
	@Column(columnDefinition="boolean not null default false", nullable = false)
	private Boolean isFriEffect = false;		// 星期五是否生效，
	
	@Column(columnDefinition="boolean not null default false", nullable = false)
	private Boolean isSatEffect = false;		// 星期六是否生效，

	@Column(columnDefinition="integer NOT NULL default 0")
	private Integer startHour = 0;	// 开始 生效的时间起始小时 
	
	@Column(columnDefinition="integer NOT NULL default 0")
	private Integer startMin = 0;	// 开始 生效的时间起始分钟 

	@Column(columnDefinition="integer NOT NULL default 23")
	private Integer stopHour = 23;	// 终止IVR的小时 
	
	@Column(columnDefinition="integer NOT NULL default 59")
	private Integer stopMin = 59;	// 终止IVR的分钟 

	@Column(columnDefinition="boolean not null default true", nullable = true)
	private Boolean isUseable = true;	// 可用状态
	
	// 所属域的编号
	@Column(columnDefinition="bigint NOT NULL", nullable=false)
	private Long domainId;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getIvrMenuId() {
		return ivrMenuId;
	}

	public void setIvrMenuId(Long ivrMenuId) {
		this.ivrMenuId = ivrMenuId;
	}

	public Long getOutlineId() {
		return outlineId;
	}

	public void setOutlineId(Long outlineId) {
		this.outlineId = outlineId;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public Integer getPriority() {
		return priority;
	}

	public void setPriority(Integer priority) {
		this.priority = priority;
	}

	public Date getEffectDate() {
		return effectDate;
	}

	public void setEffectDate(Date effectDate) {
		this.effectDate = effectDate;
	}

	public Date getExpireDate() {
		return expireDate;
	}

	public void setExpireDate(Date expireDate) {
		this.expireDate = expireDate;
	}

	public Boolean getIsSunEffect() {
		return isSunEffect;
	}

	public void setIsSunEffect(Boolean isSunEffect) {
		this.isSunEffect = isSunEffect;
	}

	public Boolean getIsMonEffect() {
		return isMonEffect;
	}

	public void setIsMonEffect(Boolean isMonEffect) {
		this.isMonEffect = isMonEffect;
	}

	public Boolean getIsTueEffect() {
		return isTueEffect;
	}

	public void setIsTueEffect(Boolean isTueEffect) {
		this.isTueEffect = isTueEffect;
	}

	public Boolean getIsWedEffect() {
		return isWedEffect;
	}

	public void setIsWedEffect(Boolean isWedEffect) {
		this.isWedEffect = isWedEffect;
	}

	public Boolean getIsThuEffect() {
		return isThuEffect;
	}

	public void setIsThuEffect(Boolean isThuEffect) {
		this.isThuEffect = isThuEffect;
	}

	public Boolean getIsFriEffect() {
		return isFriEffect;
	}

	public void setIsFriEffect(Boolean isFriEffect) {
		this.isFriEffect = isFriEffect;
	}

	public Boolean getIsSatEffect() {
		return isSatEffect;
	}

	public void setIsSatEffect(Boolean isSatEffect) {
		this.isSatEffect = isSatEffect;
	}

	public Integer getStartHour() {
		return startHour;
	}

	public void setStartHour(Integer startHour) {
		this.startHour = startHour;
	}

	public Integer getStartMin() {
		return startMin;
	}

	public void setStartMin(Integer startMin) {
		this.startMin = startMin;
	}

	public Integer getStopHour() {
		return stopHour;
	}

	public void setStopHour(Integer stopHour) {
		this.stopHour = stopHour;
	}

	public Integer getStopMin() {
		return stopMin;
	}

	public void setStopMin(Integer stopMin) {
		this.stopMin = stopMin;
	}

	public Boolean getIsUseable() {
		return isUseable;
	}

	public void setIsUseable(Boolean isUseable) {
		this.isUseable = isUseable;
	}

	public Long getDomainId() {
		return domainId;
	}

	public void setDomainId(Long domainId) {
		this.domainId = domainId;
	}

	/**
	 * 获取生效时段【用于组合显示当前IVR 的生效时段，主要用于显示在界面上，不做存储】
	 * @return
	 */
	public String getEffectTimeScope() {
		StringBuffer timeBf = new StringBuffer();
		
		// 日期
		timeBf.append(DATE_FORMAT.format(effectDate));
		timeBf.append(" 至 ");
		timeBf.append(DATE_FORMAT.format(expireDate));
		timeBf.append("  ");
		
		// 星期几
		timeBf.append(getEffectDays());
		timeBf.append("  ");
		
		// 时段
		if (startHour < 10) {
			timeBf.append("0");
		} 
		timeBf.append(startHour);
		timeBf.append(":");
		if(startMin < 10) {
			timeBf.append("0");
		}
		timeBf.append(startMin);
		timeBf.append(" - ");
		if (stopHour < 10) {
			timeBf.append("0");
		} 
		timeBf.append(stopHour);
		timeBf.append(":");
		if(stopMin < 10) {
			timeBf.append("0");
		}
		timeBf.append(stopMin);
		return timeBf.toString();
	}

	/**
	 * 生效日【用于组合显示当前IVR 的生效日，主要用于显示在界面上，不做存储】
	 * @return
	 */
	private String getEffectDays() {
		StringBuffer daysBf = new StringBuffer();
		daysBf.append("周 ");
		if(isMonEffect) {
			daysBf.append("一");
			daysBf.append("、");
		} 
		if(isTueEffect) {
			daysBf.append("二");
			daysBf.append("、");
		}
		if(isWedEffect) {
			daysBf.append("三");
			daysBf.append("、");
		}
		if(isThuEffect) {
			daysBf.append("四");
			daysBf.append("、");
		}
		if(isFriEffect) {
			daysBf.append("五");
			daysBf.append("、");
		}
		if(isSatEffect) {
			daysBf.append("六");
			daysBf.append("、");
		}
		if(isSunEffect) {
			daysBf.append("日");
			daysBf.append("、");
		}

		String days = daysBf.toString();
		if(!days.endsWith("、")) {
			daysBf.delete(0, daysBf.length());
			daysBf.append("没有生效日");
		} else {
			days = days.substring(0, days.length()-1);
		}
		return days;
	}

	public String toLoggerString() {
		return "OutlineToIvrLink [id=" + id + ", ivrMenuId=" + ivrMenuId
				+ ", priority=" + priority + ", effectDays=" + getEffectTimeScope()
				+ ", isUseable=" + isUseable + ", domainId=" + domainId + "]";
	}
	
}
