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
 * 
 * @Description 描述：电话漏接日志
 * 
 * @author  jrh
 * @date    2013年12月26日 下午1:59:50
 * @version v1.0.0
 */
@Entity
@Table(name="ec2_miss_call_log")
public class MissCallLog {

	// id 标识 会议记录详单
	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="miss_call_log")
	@SequenceGenerator(name="miss_call_log", sequenceName="seq_ec2_miss_call_log_id" ,allocationSize = 1)
	private Long id; //短信ID

	// 主叫通道
	@Size(min = 1,max = 64)
	@Column(columnDefinition="character varying(64)")
	private String srcChannel;

	// 主叫通道唯一标识
	@Size(min = 1,max = 64)
	@Column(columnDefinition="character varying(64)")
	private String srcUniqueId;
	
	// 主叫号码
	@Size(min = 1,max = 64)
	@Column(columnDefinition="character varying(64)")
	private String srcNum;
	
	// srcName
	@Size(min = 1,max = 64)
	@Column(columnDefinition="character varying(64)")
	private String srcName;

	// 被叫分机通道
	@Size(min = 1,max = 64)
	@Column(columnDefinition="character varying(64) not null", nullable=false)
	private String destChannel;
	
	// 被叫分机通道的唯一标识
	@Size(min = 1,max = 64)
	@Column(columnDefinition="character varying(64) not null", nullable=false)
	private String destUniqueId;
	
	// 被叫分机号
	@Size(min = 1,max = 64)
	@Column(columnDefinition="character varying(64)")
	private String destNum;
	
	// destName
	@Size(min = 1,max = 64)
	@Column(columnDefinition="character varying(64)")
	private String destName;

	// 被叫分机振铃时长
	@Column(columnDefinition="bigint NOT NULL", nullable = false)
	private Long ringingDuration;

	// 被叫分机开始振铃时间
	@Column(columnDefinition="TIMESTAMP WITH TIME ZONE NOT NULL", nullable=false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date ringingStateTime;
	
	// 被叫挂断时间
	@Column(columnDefinition="TIMESTAMP WITH TIME ZONE NOT NULL", nullable=false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date hangupTime;

	// 被叫通道生成时间
	@Column(columnDefinition="TIMESTAMP WITH TIME ZONE")
	@Temporal(TemporalType.TIMESTAMP)
	private Date channelCreateTime;

	// 被叫用户编号
	@Column(columnDefinition="bigint")
	private Long destUserId;
	
	// 被叫用户所属部门编号
	@Column(columnDefinition="bigint")
	private Long destUserDeptId;

	// 主叫用户编号(如果主叫是坐席的话)
	@Column(columnDefinition="bigint")
	private Long srcUserId;
	
	// 主叫用户所属部门编号
	@Column(columnDefinition="bigint")
	private Long srcUserDeptId;

	// 所属域对应的编号
	@Column(columnDefinition="bigint NOT NULL", nullable = false)
	private Long domainId;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getSrcChannel() {
		return srcChannel;
	}

	public void setSrcChannel(String srcChannel) {
		this.srcChannel = srcChannel;
	}

	public String getSrcUniqueId() {
		return srcUniqueId;
	}

	public void setSrcUniqueId(String srcUniqueId) {
		this.srcUniqueId = srcUniqueId;
	}

	public String getSrcNum() {
		return srcNum;
	}

	public void setSrcNum(String srcNum) {
		this.srcNum = srcNum;
	}

	public String getSrcName() {
		return srcName;
	}

	public void setSrcName(String srcName) {
		this.srcName = srcName;
	}

	public String getDestChannel() {
		return destChannel;
	}

	public void setDestChannel(String destChannel) {
		this.destChannel = destChannel;
	}

	public String getDestUniqueId() {
		return destUniqueId;
	}

	public void setDestUniqueId(String destUniqueId) {
		this.destUniqueId = destUniqueId;
	}

	public String getDestNum() {
		return destNum;
	}

	public void setDestNum(String destNum) {
		this.destNum = destNum;
	}

	public String getDestName() {
		return destName;
	}

	public void setDestName(String destName) {
		this.destName = destName;
	}

	public Long getRingingDuration() {
		return ringingDuration;
	}

	public void setRingingDuration(Long ringingDuration) {
		this.ringingDuration = ringingDuration;
	}

	public Date getRingingStateTime() {
		return ringingStateTime;
	}

	public void setRingingStateTime(Date ringingStateTime) {
		this.ringingStateTime = ringingStateTime;
	}

	public Date getHangupTime() {
		return hangupTime;
	}

	public void setHangupTime(Date hangupTime) {
		this.hangupTime = hangupTime;
	}

	public Date getChannelCreateTime() {
		return channelCreateTime;
	}

	public void setChannelCreateTime(Date channelCreateTime) {
		this.channelCreateTime = channelCreateTime;
	}

	public Long getDestUserId() {
		return destUserId;
	}

	public void setDestUserId(Long destUserId) {
		this.destUserId = destUserId;
	}

	public Long getDestUserDeptId() {
		return destUserDeptId;
	}

	public void setDestUserDeptId(Long destUserDeptId) {
		this.destUserDeptId = destUserDeptId;
	}

	public Long getSrcUserId() {
		return srcUserId;
	}

	public void setSrcUserId(Long srcUserId) {
		this.srcUserId = srcUserId;
	}

	public Long getSrcUserDeptId() {
		return srcUserDeptId;
	}

	public void setSrcUserDeptId(Long srcUserDeptId) {
		this.srcUserDeptId = srcUserDeptId;
	}

	public Long getDomainId() {
		return domainId;
	}

	public void setDomainId(Long domainId) {
		this.domainId = domainId;
	}

	@Override
	public String toString() {
		return "MissCallLog [id=" + id + ", srcChannel=" + srcChannel
				+ ", srcUniqueId=" + srcUniqueId + ", srcNum=" + srcNum
				+ ", srcName=" + srcName + ", destChannel=" + destChannel
				+ ", destUniqueId=" + destUniqueId + ", destNum=" + destNum
				+ ", destName=" + destName + ", ringingDuration="
				+ ringingDuration + ", ringingStateTime=" + ringingStateTime
				+ ", hangupTime=" + hangupTime + ", channelCreateTime="
				+ channelCreateTime + ", destUserId=" + destUserId
				+ ", destUserDeptId=" + destUserDeptId + ", srcUserId="
				+ srcUserId + ", srcUserDeptId=" + srcUserDeptId
				+ ", domainId=" + domainId + "]";
	}

}
