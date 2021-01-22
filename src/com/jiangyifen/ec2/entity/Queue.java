package com.jiangyifen.ec2.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.Size;

@Entity
@Table(name = "ec2_queue")
public class Queue {
	
	// 当客户打入队列后，在接通前给客户报放的提醒的方式：【无需报号、报服务分机、报服务工号】
	public static final String READ_DIGITS_TYPE_READ_NONE = "read_none";		// 无需报放
	public static final String READ_DIGITS_TYPE_READ_EXTEN = "read_service_exten";		// 报服务分机
	public static final String READ_DIGITS_TYPE_READ_EMPNO = "read_service_empno";		// 报服务工号
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "queue")
	@SequenceGenerator(name = "queue", sequenceName = "seq_ec2_queue_id", allocationSize = 1)
	private Long id; 
	
	@Size(min = 0, max = 128)
	@Column(columnDefinition="character varying(128) NOT NULL unique", unique = true, nullable = false)
	private String name;
	
	@Size(min = 0, max = 128)
	@Column(columnDefinition="character varying(128)")
	private String announce;
	
	@Size(min = 0, max = 128)
	@Column(columnDefinition="character varying(128)")
	private String context;
	
	@Column(columnDefinition="bigint")
	private Long timeout;

	@Column(columnDefinition="boolean")
	private Boolean monitor_join;
	
	@Size(min = 0, max = 128)
	@Column(columnDefinition="character varying(128)")
	private String monitor_format;

	@Size(min = 0, max = 128)
	@Column(columnDefinition="character varying(128)")
	private String queue_youarenext;
	
	@Size(min = 0, max = 128)
	@Column(columnDefinition="character varying(128)")
	private String queue_thereare;
	
	@Size(min = 0, max = 128)
	@Column(columnDefinition="character varying(128)")
	private String queue_callswaiting;
	
	@Size(min = 0, max = 128)
	@Column(columnDefinition="character varying(128)")
	private String queue_holdtime;
	
	@Size(min = 0, max = 128)
	@Column(columnDefinition="character varying(128)")
	private String queue_minutes;
	
	@Size(min = 0, max = 128)
	@Column(columnDefinition="character varying(128)")
	private String queue_seconds;
	
	@Size(min = 0, max = 128)
	@Column(columnDefinition="character varying(128)")
	private String queue_lessthan;
	
	@Size(min = 0, max = 128)
	@Column(columnDefinition="character varying(128)")
	private String queue_thankyou;
	
	@Size(min = 0, max = 128)
	@Column(columnDefinition="character varying(128)")
	private String queue_reporthold;
	
	@Column(columnDefinition="bigint")
	private Long announce_frequency;
	
	@Column(columnDefinition="bigint")
	private Long announce_round_seconds;
	
	@Size(min = 0, max = 128)
	@Column(columnDefinition="character varying(128)")
	private String announce_holdtime;
	
	@Column(columnDefinition="bigint")
	private Long retry;
	
	@Column(columnDefinition="bigint")
	private Long wrapuptime;	// 冷却时长
	
	@Column(columnDefinition="bigint")
	private Long maxlen;
	
	@Column(columnDefinition="bigint")
	private Long servicelevel;
	
	@Size(min = 0, max = 128)
	@Column(columnDefinition="character varying(128)")
	private String strategy;
	
	/**
	 * 允许呼入空的队列
	 * yes		主叫可以进入无 members 或只有 unavailable members 的队列中
	 * no		主叫不可以进入无 members 的队列中
	 * strict	主叫不可以进入无 members 或只有 unavailable members 的队列中
	 * loose 同 strict，但 paused queue members 不会作为 unavaliable members 统计
	 */
	@Size(min = 0, max = 128)
	@Column(columnDefinition="character varying(128)")
	private String joinempty;
	
	/**
	 * 在新主叫无法加入队列时， remove callers from queue，参考 joinempty，如果你使用 agents 作为 queue members，总是把该参数置为 strict
	 * 控制访客是否可以在队列中保留
	 */
	@Size(min = 0, max = 128)
	@Column(columnDefinition="character varying(128)")
	private String leavewhenempty;
	
	@Column(columnDefinition="boolean")
	private Boolean eventmemberstatus;
	
	@Column(columnDefinition="boolean")
	private Boolean eventwhencalled;
	
	@Column(columnDefinition="boolean")
	private Boolean reportholdtime;
	
	@Column(columnDefinition="bigint")
	private Long memberdelay;
	
	@Column(columnDefinition="bigint")
	private Long weight;
	
	@Column(columnDefinition="boolean")
	private Boolean timeoutrestart;
	
	@Column(columnDefinition="boolean")
	private Boolean setinterfacevar;
	
	@Size(min = 0, max = 128)
	@Column(columnDefinition="character varying(128)")
	private String autopause;	// 漏接置忙
	
	@Column(columnDefinition="bigint")
	private Long periodic_announce_frequency;

	@Column(columnDefinition="bigint")
	private Long queue_periodic_announce;
	
	@Column(columnDefinition="boolean")
	private Boolean dynamicmember;
	
	@Size(min = 0, max = 128)
	@Column(columnDefinition="character varying(128)")
	private String description;
	
	// 是否为域范围内的默认队列，默认false 表示不是
	@Column(columnDefinition="boolean not null default false", nullable = false)
	private Boolean isDefaultQueue = false; 

	// 是否为可修改队列，默认 true 表示修改 （如果不可修改，一般是自动外呼任务自动生成的队列）
	@Column(columnDefinition="boolean not null default true", nullable = false)
	private Boolean isModifyable = true; 

	@Size(min = 0, max = 128)
	@Column(columnDefinition="character varying(128) default 'read_none'")
	private String readDigitsType = READ_DIGITS_TYPE_READ_NONE;		// 客户呼入接通后，报报的数字类型【无(默认)，报分机、工号等】

	// 是否开启提醒客户录音提醒
	@Column(columnDefinition="boolean not null default false", nullable = false)
	private Boolean isrecordSoundNotice = false;
	
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = MusicOnHold.class)
	@JoinColumn(name = "musiconhold_id")
	private MusicOnHold musiconhold;
	
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = Domain.class)
	@JoinColumn(name = "domain_id", nullable = false)
	private Domain domain;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAnnounce() {
		return announce;
	}

	public void setAnnounce(String announce) {
		this.announce = announce;
	}

	public String getContext() {
		return context;
	}

	public void setContext(String context) {
		this.context = context;
	}

	public Long getTimeout() {
		return timeout;
	}

	public void setTimeout(Long timeout) {
		this.timeout = timeout;
	}

	public Boolean getMonitor_join() {
		return monitor_join;
	}

	public void setMonitor_join(Boolean monitor_join) {
		this.monitor_join = monitor_join;
	}

	public Boolean getIsModifyable() {
		return isModifyable;
	}

	public void setIsModifyable(Boolean isModifyable) {
		this.isModifyable = isModifyable;
	}

	public String getMonitor_format() {
		return monitor_format;
	}

	public void setMonitor_format(String monitor_format) {
		this.monitor_format = monitor_format;
	}

	public String getQueue_youarenext() {
		return queue_youarenext;
	}

	public void setQueue_youarenext(String queue_youarenext) {
		this.queue_youarenext = queue_youarenext;
	}

	public String getQueue_thereare() {
		return queue_thereare;
	}

	public void setQueue_thereare(String queue_thereare) {
		this.queue_thereare = queue_thereare;
	}

	public String getQueue_callswaiting() {
		return queue_callswaiting;
	}

	public void setQueue_callswaiting(String queue_callswaiting) {
		this.queue_callswaiting = queue_callswaiting;
	}

	public String getQueue_holdtime() {
		return queue_holdtime;
	}

	public void setQueue_holdtime(String queue_holdtime) {
		this.queue_holdtime = queue_holdtime;
	}

	public String getQueue_minutes() {
		return queue_minutes;
	}

	public void setQueue_minutes(String queue_minutes) {
		this.queue_minutes = queue_minutes;
	}

	public String getQueue_seconds() {
		return queue_seconds;
	}

	public void setQueue_seconds(String queue_seconds) {
		this.queue_seconds = queue_seconds;
	}

	public String getQueue_lessthan() {
		return queue_lessthan;
	}

	public void setQueue_lessthan(String queue_lessthan) {
		this.queue_lessthan = queue_lessthan;
	}

	public String getQueue_thankyou() {
		return queue_thankyou;
	}

	public void setQueue_thankyou(String queue_thankyou) {
		this.queue_thankyou = queue_thankyou;
	}

	public String getQueue_reporthold() {
		return queue_reporthold;
	}

	public void setQueue_reporthold(String queue_reporthold) {
		this.queue_reporthold = queue_reporthold;
	}

	public Long getAnnounce_frequency() {
		return announce_frequency;
	}

	public void setAnnounce_frequency(Long announce_frequency) {
		this.announce_frequency = announce_frequency;
	}

	public Long getAnnounce_round_seconds() {
		return announce_round_seconds;
	}

	public void setAnnounce_round_seconds(Long announce_round_seconds) {
		this.announce_round_seconds = announce_round_seconds;
	}

	public String getAnnounce_holdtime() {
		return announce_holdtime;
	}

	public void setAnnounce_holdtime(String announce_holdtime) {
		this.announce_holdtime = announce_holdtime;
	}

	public Long getRetry() {
		return retry;
	}

	public void setRetry(Long retry) {
		this.retry = retry;
	}

	public Long getWrapuptime() {
		return wrapuptime;
	}

	public void setWrapuptime(Long wrapuptime) {
		this.wrapuptime = wrapuptime;
	}

	public Long getMaxlen() {
		return maxlen;
	}

	public void setMaxlen(Long maxlen) {
		this.maxlen = maxlen;
	}

	public Long getServicelevel() {
		return servicelevel;
	}

	public void setServicelevel(Long servicelevel) {
		this.servicelevel = servicelevel;
	}

	public String getStrategy() {
		return strategy;
	}

	public void setStrategy(String strategy) {
		this.strategy = strategy;
	}

	public String getJoinempty() {
		return joinempty;
	}

	public void setJoinempty(String joinempty) {
		this.joinempty = joinempty;
	}

	public String getLeavewhenempty() {
		return leavewhenempty;
	}

	public void setLeavewhenempty(String leavewhenempty) {
		this.leavewhenempty = leavewhenempty;
	}

	public Boolean getEventmemberstatus() {
		return eventmemberstatus;
	}

	public void setEventmemberstatus(Boolean eventmemberstatus) {
		this.eventmemberstatus = eventmemberstatus;
	}

	public Boolean getEventwhencalled() {
		return eventwhencalled;
	}

	public void setEventwhencalled(Boolean eventwhencalled) {
		this.eventwhencalled = eventwhencalled;
	}

	public Boolean getReportholdtime() {
		return reportholdtime;
	}

	public void setReportholdtime(Boolean reportholdtime) {
		this.reportholdtime = reportholdtime;
	}

	public Long getMemberdelay() {
		return memberdelay;
	}

	public void setMemberdelay(Long memberdelay) {
		this.memberdelay = memberdelay;
	}

	public Long getWeight() {
		return weight;
	}

	public void setWeight(Long weight) {
		this.weight = weight;
	}

	public Boolean getTimeoutrestart() {
		return timeoutrestart;
	}

	public void setTimeoutrestart(Boolean timeoutrestart) {
		this.timeoutrestart = timeoutrestart;
	}

	public Boolean getSetinterfacevar() {
		return setinterfacevar;
	}

	public void setSetinterfacevar(Boolean setinterfacevar) {
		this.setinterfacevar = setinterfacevar;
	}

	public String getAutopause() {
		return autopause;
	}

	public void setAutopause(String autopause) {
		this.autopause = autopause;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Long getPeriodic_announce_frequency() {
		return periodic_announce_frequency;
	}

	public void setPeriodic_announce_frequency(Long periodic_announce_frequency) {
		this.periodic_announce_frequency = periodic_announce_frequency;
	}

	public Long getQueue_periodic_announce() {
		return queue_periodic_announce;
	}

	public void setQueue_periodic_announce(Long queue_periodic_announce) {
		this.queue_periodic_announce = queue_periodic_announce;
	}

	public Boolean getDynamicmember() {
		return dynamicmember;
	}

	public void setDynamicmember(Boolean dynamicmember) {
		this.dynamicmember = dynamicmember;
	}

	public String getReadDigitsType() {
		return readDigitsType;
	}

	public void setReadDigitsType(String readDigitsType) {
		this.readDigitsType = readDigitsType;
	}

	public Boolean getIsrecordSoundNotice() {
		return isrecordSoundNotice;
	}

	public void setIsrecordSoundNotice(Boolean isrecordSoundNotice) {
		this.isrecordSoundNotice = isrecordSoundNotice;
	}

	public MusicOnHold getMusiconhold() {
		return musiconhold;
	}

	public void setMusiconhold(MusicOnHold musiconhold) {
		this.musiconhold = musiconhold;
	}

	public Boolean getIsDefaultQueue() {
		return isDefaultQueue;
	}

	public void setIsDefaultQueue(Boolean isDefaultQueue) {
		this.isDefaultQueue = isDefaultQueue;
	}

	public Domain getDomain() {
		return domain;
	}

	public void setDomain(Domain domain) {
		this.domain = domain;
	}

	public String getDescriptionAndName() {
		return name.toString()+"-"+description.toString();
	}
	
	@Override
	public String toString() {
		return description;
	}

}
