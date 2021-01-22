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
 * @Description 描述：
 *
 * @author  jrh
 * @date    2014年2月19日 下午1:56:44
 * @version v1.0.0
 */
@Entity
@Table(name = "ec2_ivr_log")
public class IVRLog {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ivr_log")
	@SequenceGenerator(name = "ivr_log", sequenceName = "seq_ec2_ivr_log_id", allocationSize = 1)
	private Long id;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(columnDefinition = "TIMESTAMP WITH TIME ZONE NOT NULL", nullable = false)
	private Date createDate;

	@Size(min = 0, max = 64)
	@Column(columnDefinition="character varying(64) default ''")
	private String ivrId = "";

	@Size(min = 0, max = 64)
	@Column(columnDefinition="character varying(64) default ''")
	private String actionId = "";

	@Size(min = 0, max = 64)
	@Column(columnDefinition="character varying(64) default ''")
	private String optionId = "";

	@Size(min = 0, max = 64)
	@Column(columnDefinition="character varying(64) default ''")
	private String pressNumber = "";

	@Size(min = 0, max = 64)
	@Column(columnDefinition="character varying(64) default ''")
	private String channel = "";

	@Size(min = 0, max = 64)
	@Column(columnDefinition="character varying(64) default ''")
	private String uniqueid = "";

	@Size(min = 0, max = 128)
	@Column(columnDefinition="character varying(128) default ''")
	private String context = "";

	@Size(min = 0, max = 64)
	@Column(columnDefinition="character varying(64) default ''")
	private String extension = "";	// 走的dialplan ，如 toqueue

	@Size(min = 0, max = 64)
	@Column(columnDefinition="character varying(64) default ''")
	private String callerIdNumber = "";

	@Size(min = 0, max = 64)
	@Column(columnDefinition="character varying(64) default ''")
	private String callerIdName = "";
	
	@Size(min = 0, max = 64)
	@Column(columnDefinition="character varying(64) default ''")
	private String dest = "";	// 被叫的外线号码
	
	@Size(min = 0, max = 128)
	@Column(columnDefinition="character varying(128) default ''")
	private String node = "";

	@Column(columnDefinition="bigint", nullable = false)
	private Long domainId=0L;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public String getIvrId() {
		return ivrId;
	}

	public void setIvrId(String ivrId) {
		this.ivrId = ivrId;
	}

	public String getActionId() {
		return actionId;
	}

	public void setActionId(String actionId) {
		this.actionId = actionId;
	}

	public String getOptionId() {
		return optionId;
	}

	public void setOptionId(String optionId) {
		this.optionId = optionId;
	}

	public String getPressNumber() {
		return pressNumber;
	}

	public void setPressNumber(String pressNumber) {
		this.pressNumber = pressNumber;
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public String getUniqueid() {
		return uniqueid;
	}

	public void setUniqueid(String uniqueid) {
		this.uniqueid = uniqueid;
	}

	public String getContext() {
		return context;
	}

	public void setContext(String context) {
		this.context = context;
	}

	public String getExtension() {
		return extension;
	}

	public void setExtension(String extension) {
		this.extension = extension;
	}

	public String getCallerIdNumber() {
		return callerIdNumber;
	}

	public void setCallerIdNumber(String callerIdNumber) {
		this.callerIdNumber = callerIdNumber;
	}

	public String getCallerIdName() {
		return callerIdName;
	}

	public void setCallerIdName(String callerIdName) {
		this.callerIdName = callerIdName;
	}

	public String getDest() {
		return dest;
	}

	public void setDest(String dest) {
		this.dest = dest;
	}

	public String getNode() {
		return node;
	}

	public void setNode(String node) {
		this.node = node;
	}

	public Long getDomainId() {
		return domainId;
	}

	public void setDomainId(Long domainId) {
		this.domainId = domainId;
	}
	
}
