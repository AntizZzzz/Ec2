package com.jiangyifen.ec2.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.Size;

@Entity
@Table(name = "ec2_timers")
public class Timers {
	
	public final static int NEXT_POP_MIN = 5;	// 当坐席关闭定时提醒窗口时，自动设置下次弹屏提醒的默认时间  为 5分钟后
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "timers")
	@SequenceGenerator(name = "timers", sequenceName = "seq_ec2_timers_id", allocationSize = 1)
	private Long id;	// 用户ID
	
	@Size(min = 1,max = 128)
	@Column(columnDefinition= "character varying(128) NOT NULL", nullable = false)
	private String title;	// 标题
	
	@Column(columnDefinition= "TEXT NOT NULL", nullable = false)
	private String content;	// 内容

	@Column(columnDefinition="bigint")
	private Long customerId;	// 要回访客户编号

	@Size(min = 0, max = 64)
	@Column(columnDefinition="character varying(64)")
	private String customerPhoneNum; // 要回访客户电话号码 13520105465 ， 135****5465

	@Column(columnDefinition="boolean not null default false", nullable = false)
	private Boolean isDialInLastPop = false;	// 坐席是否在最后一次预约弹屏中联系了预约的客户， 默认为没有
	
	@Column(columnDefinition="integer")
	private Integer dialCount = 0;	// 坐席呼叫客户的次数
	
	@Column(columnDefinition= "TIMESTAMP WITH TIME ZONE")
	@Temporal(TemporalType.TIMESTAMP)
	private Date lastDialTime;	// 最近呼叫时间
	
	@Column(columnDefinition= "TIMESTAMP WITH TIME ZONE NOT NULL", nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date createTime;	// 创建时间
	
	@Column(columnDefinition= "TIMESTAMP WITH TIME ZONE NOT NULL", nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date firstRespTime;	// 首次响应时间
	
	@Column(columnDefinition= "TIMESTAMP WITH TIME ZONE NOT NULL", nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date responseTime;	// 响应时间
	
	@Column(columnDefinition= "TIMESTAMP WITH TIME ZONE")
	@Temporal(TemporalType.TIMESTAMP)
	private Date forbidRespTime;	// 坐席点击禁止提醒的时间

	@Column(columnDefinition="integer")
	private Integer popCount = 0;	// 系统提醒坐席的次数

	@Column(columnDefinition="boolean not null default false", nullable = false)
	private Boolean isCsrForbidPop = false;	// 是否为坐席禁止了定时器再次提醒， 默认为没有
	
	@Size(min = 1,max = 32)
	@Column(columnDefinition= "character varying(32) NOT NULL", nullable = false)
	private String type;	// 响应类型（一次、每天、每周、 每年、每月、每年、自定义）
	
	@JoinColumn(columnDefinition="NOT NULL", name = "creator_id", nullable = false)
	private User creator;	// 定时器的拥有者
	
	@JoinColumn(columnDefinition="NOT NULL", name = "domain_id", nullable = false)
	private Domain domain;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Long getCustomerId() {
		return customerId;
	}

	public void setCustomerId(Long customerId) {
		this.customerId = customerId;
	}

	public String getCustomerPhoneNum() {
		return customerPhoneNum;
	}

	public void setCustomerPhoneNum(String customerPhoneNum) {
		this.customerPhoneNum = customerPhoneNum;
	}

	public Boolean getIsDialInLastPop() {
		return isDialInLastPop;
	}

	public void setIsDialInLastPop(Boolean isDialInLastPop) {
		this.isDialInLastPop = isDialInLastPop;
	}

	public Integer getDialCount() {
		return dialCount;
	}

	public void setDialCount(Integer dialCount) {
		this.dialCount = dialCount;
	}

	public Date getLastDialTime() {
		return lastDialTime;
	}

	public void setLastDialTime(Date lastDialTime) {
		this.lastDialTime = lastDialTime;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Date getFirstRespTime() {
		return firstRespTime;
	}

	public void setFirstRespTime(Date firstRespTime) {
		this.firstRespTime = firstRespTime;
	}

	public Date getResponseTime() {
		return responseTime;
	}

	public void setResponseTime(Date responseTime) {
		this.responseTime = responseTime;
	}

	public Date getForbidRespTime() {
		return forbidRespTime;
	}

	public void setForbidRespTime(Date forbidRespTime) {
		this.forbidRespTime = forbidRespTime;
	}

	public Integer getPopCount() {
		return popCount;
	}

	public void setPopCount(Integer popCount) {
		this.popCount = popCount;
	}

	public Boolean getIsCsrForbidPop() {
		return isCsrForbidPop;
	}

	public void setIsCsrForbidPop(Boolean isCsrForbidPop) {
		this.isCsrForbidPop = isCsrForbidPop;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
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

}
