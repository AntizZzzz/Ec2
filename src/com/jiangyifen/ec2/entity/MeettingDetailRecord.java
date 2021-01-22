package com.jiangyifen.ec2.entity;

import java.io.File;
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
import javax.validation.constraints.Size;

import com.jiangyifen.ec2.utils.Config;

/**
 * 
 * @Description 描述：多方通话的记录详情
 * 
 * @author  jrh
 * @date    2013年12月23日 下午8:32:14
 * @version v1.0.0
 */
@Entity
@Table(name="ec2_meetting_detail_record")
public class MeettingDetailRecord {
	
	private final static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd");
	
	// id 标识 会议记录详单
	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="meetting_detail_record")
	@SequenceGenerator(name="meetting_detail_record", sequenceName="seq_ec2_meetting_detail_record_id" ,allocationSize = 1)
	private Long id; //短信ID

	// 会议室房间号
	@Size(min = 1,max = 64)
	@Column(columnDefinition="character varying(64) not null", nullable=false)
	private String meettingRoom;

	// 会议唯一标识 用来标识单次会议的唯一标识
	@Size(min = 1,max = 64)
	@Column(columnDefinition="character varying(64) not null", nullable=false)
	private String meettingUniqueId;

	// 与会成员的通道信息
	@Size(min = 1,max = 64)
	@Column(columnDefinition="character varying(64) not null", nullable=false)
	private String channel;

	// 与会成员的通道唯一标识
	@Size(min = 1,max = 64)
	@Column(columnDefinition="character varying(64) not null", nullable=false)
	private String channelUniqueId;

	// 与会成员号码
	@Size(min = 1,max = 64)
	@Column(columnDefinition="character varying(64)")
	private String joinMemberNum;
	
	// 与会成员在会议室中的序号，排在哪一位
	@Column(columnDefinition="integer NOT NULL", nullable = false)
	private Integer memberIndex;

	// 与会成员在会议室中待了多长时间
	@Column(columnDefinition="bigint NOT NULL", nullable = false)
	private Long duration;

	// 与会成员进入会议室的时间
	@Column(columnDefinition="TIMESTAMP WITH TIME ZONE NOT NULL", nullable=false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date joinDate;

	// 与会成员退出会议室的时间
	@Column(columnDefinition="TIMESTAMP WITH TIME ZONE NOT NULL", nullable=false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date leaveDate;

	// 与会成员标识名称
	@Size(min = 1,max = 64)
	@Column(columnDefinition="character varying(64)")
	private String callerIdName;

	// 与会成员标识号码
	@Size(min = 1,max = 64)
	@Column(columnDefinition="character varying(64)")
	private String callerIdNum;

	// 与会成员沟通对象的名称
	@Size(min = 1,max = 64)
	@Column(columnDefinition="character varying(64)")
	private String connectedLineName;

	// 与会成员沟通对象的号码
	@Size(min = 1,max = 64)
	@Column(columnDefinition="character varying(64)")
	private String connectedLineNum;

	// asterisk 服务端的信息
	@Size(min = 1,max = 1024)
	@Column(columnDefinition="character varying(1024)")
	private String source;
	
	// 会议发起人的编号
	@Column(columnDefinition="bigint")
	private Long originatorId;

	// 与会人的编号
	@Column(columnDefinition="bigint")
	private Long joinMemberId;

	// 与会成员是否是第一个进入会议室的人
	@Column(columnDefinition="boolean not null default false", nullable = false)
	private boolean isFirstJoin = false;
	
	// 是否已经下载过
	@Column(columnDefinition="boolean not null default false", nullable = false)
	private Boolean isDownloaded = false;

	// domainId 属于 会议记录详单
	@Column(columnDefinition="bigint NOT NULL", nullable = false)
	private Long domainId;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getMeettingRoom() {
		return meettingRoom;
	}

	public void setMeettingRoom(String meettingRoom) {
		this.meettingRoom = meettingRoom;
	}

	public String getMeettingUniqueId() {
		return meettingUniqueId;
	}

	public void setMeettingUniqueId(String meettingUniqueId) {
		this.meettingUniqueId = meettingUniqueId;
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public String getChannelUniqueId() {
		return channelUniqueId;
	}

	public void setChannelUniqueId(String channelUniqueId) {
		this.channelUniqueId = channelUniqueId;
	}

	public Integer getMemberIndex() {
		return memberIndex;
	}

	public void setMemberIndex(Integer memberIndex) {
		this.memberIndex = memberIndex;
	}

	public Long getDuration() {
		return duration;
	}

	public void setDuration(Long duration) {
		this.duration = duration;
	}

	public Date getJoinDate() {
		return joinDate;
	}

	public void setJoinDate(Date joinDate) {
		this.joinDate = joinDate;
	}

	public Date getLeaveDate() {
		return leaveDate;
	}

	public void setLeaveDate(Date leaveDate) {
		this.leaveDate = leaveDate;
	}

	public String getCallerIdName() {
		return callerIdName;
	}

	public void setCallerIdName(String callerIdName) {
		this.callerIdName = callerIdName;
	}

	public String getCallerIdNum() {
		return callerIdNum;
	}

	public void setCallerIdNum(String callerIdNum) {
		this.callerIdNum = callerIdNum;
	}
	
	public String getJoinMemberNum() {
		return joinMemberNum;
	}

	public void setJoinMemberNum(String joinMemberNum) {
		this.joinMemberNum = joinMemberNum;
	}

	public boolean isFirstJoin() {
		return isFirstJoin;
	}

	public void setFirstJoin(boolean isFirstJoin) {
		this.isFirstJoin = isFirstJoin;
	}

	/**
	 * 录音文件路径
	 */
	public String getRecordFileUrl(String baseUrl) {
		String dateStr = DATE_FORMAT.format(joinDate);
		String dynamicPath = "meetme/"+dateStr+"/"+dateStr+"-"+meettingUniqueId+"-"+meettingRoom+"-meetme-record.wav";
		String filePath = Config.props.getProperty(Config.REC_DIR_PATH)+dynamicPath;
		File file = new File(filePath);
		if(file.exists()) {
			return baseUrl+dynamicPath;
		}
		return null;
	}

	public String getConnectedLineName() {
		return connectedLineName;
	}

	public void setConnectedLineName(String connectedLineName) {
		this.connectedLineName = connectedLineName;
	}

	public String getConnectedLineNum() {
		return connectedLineNum;
	}

	public void setConnectedLineNum(String connectedLineNum) {
		this.connectedLineNum = connectedLineNum;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public Long getOriginatorId() {
		return originatorId;
	}

	public void setOriginatorId(Long originatorId) {
		this.originatorId = originatorId;
	}

	public Long getJoinMemberId() {
		return joinMemberId;
	}

	public void setJoinMemberId(Long joinMemberId) {
		this.joinMemberId = joinMemberId;
	}

	public Boolean getIsDownloaded() {
		return isDownloaded;
	}

	public void setIsDownloaded(Boolean isDownloaded) {
		this.isDownloaded = isDownloaded;
	}

	public Long getDomainId() {
		return domainId;
	}

	public void setDomainId(Long domainId) {
		this.domainId = domainId;
	}

	@Override
	public String toString() {
		return "MeettingDetailRecord [id=" + id + ", meettingRoom=" + meettingRoom + ", meettingUniqueId=" + meettingUniqueId + ", channel=" + channel + ", channelUniqueId=" + channelUniqueId + ", joinMemberNum=" + joinMemberNum + ", memberIndex=" + memberIndex + ", duration=" + duration + ", joinDate=" + joinDate + ", leaveDate=" + leaveDate + ", callerIdName=" + callerIdName + ", callerIdNum=" + callerIdNum + ", connectedLineName=" + connectedLineName + ", connectedLineNum=" + connectedLineNum + ", source=" + source + ", originatorId=" + originatorId + ", joinMemberId=" + joinMemberId + ", isFirstJoin=" + isFirstJoin + ", isDownloaded=" + isDownloaded + ", domainId=" + domainId + "]";
	}

}
