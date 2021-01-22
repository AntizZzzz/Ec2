package com.jiangyifen.ec2.entity;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.jiangyifen.ec2.bean.AutoDialoutTaskStatus;
import com.jiangyifen.ec2.utils.AutoDialConfig;

@Entity
@Table(name = "ec2_auto_dialout_task")
public class AutoDialoutTask {
	public static String SOUND_DIALOUT="语音群发";
	public static String AUTO_DIALOUT="自动外呼";
	public static String PLAY_PRE_AUDIO="播放";
	public static String NOT_PLAY_PRE_AUDIO="不播放";
	public static String SYSTEM_AJUST="自动";
	public static String NOT_SYSTEM_AJUST="固定";
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "auto_dialout_task")
	@SequenceGenerator(name = "auto_dialout_task", sequenceName = "seq_ec2_auto_dialout_task_id", allocationSize = 1)
	private Long id;
	
	//自动外呼名字
	@Size(min = 0, max = 64)
	@Column(nullable = false,columnDefinition="character varying(64) NOT NULL")
	private String autoDialoutTaskName;
	
	
	//备注信息
	@Size(min = 0, max = 512)
	@Column(nullable = false,columnDefinition="character varying(512)")
	private String note;
	
	//系数
	private Double ratio=Double.parseDouble(AutoDialConfig.props.getProperty(AutoDialConfig.DEFAULT_RATIO));

	//指定百分比系数
	private Integer percentageDepth=Integer.parseInt(AutoDialConfig.props.getProperty(AutoDialConfig.DEFAULT_PERCENTAGE_DEPTH));

	//手动指定固定值,期望队列中的Caller数量
	private Integer staticExpectedCallers=Integer.parseInt(AutoDialConfig.props.getProperty(AutoDialConfig.DEFAULT_STATIC_EXPECTED_CALLERS));
	
	//是否开启系统自动调整
	private String isSystemAjust=SYSTEM_AJUST;
	
	//并发上限
	private Integer concurrentLimit;
	
	//状态（新建、开始、结束）
	//项目n-->1项目状态   项目的状态信息
	@Enumerated
	private AutoDialoutTaskStatus autoDialoutTaskStatus;

	//是不是在呼叫前播放音乐
	@Size(min = 1, max = 16)
	@Column(columnDefinition="character varying(16)")
	private String preAudioPlay=NOT_PLAY_PRE_AUDIO;
	
	//DialTask创建日期
	@Temporal(TemporalType.TIMESTAMP)
	private Date createDate;
	
	//类型：“自动外呼” 或   “语音群发”
	@Size(min = 1, max = 16)
	@Column(columnDefinition="character varying(16)")
	private String dialoutType;

	// jrh 自动外呼的手机成员对象
	@Basic(fetch = FetchType.LAZY)
	private Set<String> phoneNos = new HashSet<String>(); 
	
	//域的概念 批次1-->n域
	//域不能为空
	@NotNull
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = Domain.class)
	private Domain domain;
	
	//=============================  对应关系  ================================//
	//和项目的 n-->1 关系
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = MarketingProject.class)
	private MarketingProject marketingProject;

	//和语音文件的 n-->1 关系
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = SoundFile.class)
	private SoundFile soundFile;
	
	//和队列的一对一关系
	//客户1-->1地址 默认地址
	@OneToOne(fetch = FetchType.LAZY, targetEntity = Queue.class)
	private Queue queue;
	
	//和CSR的一对多关系
	@OneToMany(targetEntity = User.class,fetch=FetchType.LAZY)
	private Set<User> users;
	
	//多个项目对应于一个用户
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = User.class)
	private User creator;

	//====================================Getter and Setter========================//
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Double getRatio() {
		return ratio;
	}

	public void setRatio(Double ratio) {
		this.ratio = ratio;
	}

	public Integer getConcurrentLimit() {
		return concurrentLimit;
	}

	public void setConcurrentLimit(Integer concurrentLimit) {
		this.concurrentLimit = concurrentLimit;
	}

	public AutoDialoutTaskStatus getAutoDialoutTaskStatus() {
		return autoDialoutTaskStatus;
	}

	public void setAutoDialoutTaskStatus(AutoDialoutTaskStatus autoDialoutTaskStatus) {
		this.autoDialoutTaskStatus = autoDialoutTaskStatus;
	}

	public Integer getStaticExpectedCallers() {
		return staticExpectedCallers;
	}

	public void setStaticExpectedCallers(Integer staticExpectedCallers) {
		this.staticExpectedCallers = staticExpectedCallers;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public MarketingProject getMarketingProject() {
		return marketingProject;
	}

	public void setMarketingProject(MarketingProject marketingProject) {
		this.marketingProject = marketingProject;
	}

	public Queue getQueue() {
		return queue;
	}

	public void setQueue(Queue queue) {
		this.queue = queue;
	}

	public Set<User> getUsers() {
		return users;
	}

	public void setUsers(Set<User> users) {
		this.users = users;
	}
	

	public String getAutoDialoutTaskName() {
		return autoDialoutTaskName;
	}

	public void setAutoDialoutTaskName(String autoDialoutTaskName) {
		this.autoDialoutTaskName = autoDialoutTaskName;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}
	
	public SoundFile getSoundFile() {
		return soundFile;
	}

	public void setSoundFile(SoundFile soundFile) {
		this.soundFile = soundFile;
	}

	public String getPreAudioPlay() {
		return preAudioPlay;
	}

	public void setPreAudioPlay(String preAudioPlay) {
		this.preAudioPlay = preAudioPlay;
	}

	public String getDialoutType() {
		return dialoutType;
	}

	public void setDialoutType(String dialoutType) {
		this.dialoutType = dialoutType;
	}

	public Set<String> getPhoneNos() {
		return phoneNos;
	}

	public void setPhoneNos(Set<String> phoneNos) {
		this.phoneNos = phoneNos;
	}

	public Integer getPercentageDepth() {
		return percentageDepth;
	}

	public void setPercentageDepth(Integer percentageDepth) {
		this.percentageDepth = percentageDepth;
	}

	public String getIsSystemAjust() {
		return isSystemAjust;
	}

	public void setIsSystemAjust(String isSystemAjust) {
		this.isSystemAjust = isSystemAjust;
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
