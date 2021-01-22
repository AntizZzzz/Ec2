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
import javax.persistence.Transient;
import javax.validation.constraints.Size;

/**
 * 语音留言实体类
 *
 * @author jinht
 *
 * @date 2015-6-15 下午4:11:43 
 *
 */
@Entity
@Table(name = "ec2_voicemail")
public class Voicemail {

//	private static String BASE_PATH="/var/spool/asterisk/voicemail/";
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "voicemail")
	@SequenceGenerator(name = "voicemail", sequenceName = "seq_ec2_voicemail_id", allocationSize = 1)
	private Long id;
	
	private Long domainid; // 域的id
	
	private Long userid; // 用户的id

	@Transient
	private String username; // 用户的名称
	
	private String partFilename; // 文件名称  ${domainid}/${userid}/INBOX/${filename}.wav
	
	@Size(min = 0, max = 64)
	private String cusnum; // 客户呼入的手机号码

	@Size(min = 0, max = 64)
	private String fromOutlineChannel; // 客户呼入的外线channel
	
	@Size(min = 0, max = 64)
	private String fromOutline; // 从channel中截取的外线
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
	private Date origtimeDatetime; // 发起语音留言的时间
	
	private Integer duration; // 语音留言的持续时长
	
	public Voicemail() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public Long getDomainid() {
		return domainid;
	}

	public void setDomainid(Long domainid) {
		this.domainid = domainid;
	}

	public String getPartFilename() {
		return partFilename;
	}

	public void setPartFilename(String partFilename) {
		this.partFilename = partFilename;
	}

	public Long getUserid() {
		return userid;
	}

	public void setUserid(Long userid) {
		this.userid = userid;
	}

	public String getCusnum() {
		return cusnum;
	}

	public void setCusnum(String cusnum) {
		this.cusnum = cusnum;
	}

	public String getFromOutlineChannel() {
		return fromOutlineChannel;
	}

	public void setFromOutlineChannel(String fromOutlineChannel) {
		this.fromOutlineChannel = fromOutlineChannel;
	}

	public String getFromOutline() {
		return fromOutline;
	}

	public void setFromOutline(String fromOutline) {
		this.fromOutline = fromOutline;
	}

	public Date getOrigtimeDatetime() {
		return origtimeDatetime;
	}

	public void setOrigtimeDatetime(Date origtimeDatetime) {
		this.origtimeDatetime = origtimeDatetime;
	}

	public Integer getDuration() {
		return duration;
	}

	public void setDuration(Integer duration) {
		this.duration = duration;
	}
	
}
