package com.jiangyifen.ec2.entity;

import java.util.HashMap;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.jiangyifen.ec2.entity.enumtype.IVRActionType;


/**
 * 
 * @Description 描述：
 * 
 * @author  jrh
 * @date    2014年2月19日 下午1:56:44
 * @version v1.0.0
 */
@Entity
@Table(name = "ec2_ivr_action")
public class IVRAction {

	public static final HashMap<String, String> AGI_NAMES_MAP = new HashMap<String, String>(); 
	
	static {
		AGI_NAMES_MAP.put("dialSpecifiedExtenByIvr.agi", "呼叫指定分机");
	}
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ivr_action")
	@SequenceGenerator(name = "ivr_action", sequenceName = "seq_ec2_ivr_action_id", allocationSize = 1)
	private Long id;

	@Size(min = 0, max = 128)
	@Column(columnDefinition = "character varying(128)")
	private String ivrActionName;	// 名称
	
	@Size(min = 0, max = 256)
	@Column(columnDefinition = "character varying(256)")
	private String description;		// 描述信息

	/**
	 * 类型值有：
	 * 		1、呼叫分机：				 toExten 
	 * 		2、呼入队列: 				 toQueue
	 * 		3、转呼手机: 				 toMobile
	 * 		4、Playback语音: 		 	 toPlayback
	 * 		5、Read语音:		 		 toRead
	 * 		6、Read语音根据按键到执行Agi:   toReadForAgi 	如-根据按键将呼入客户接通到指定坐席（该坐席是客户自己选择的）
	 */
	@NotNull
	@Enumerated
	private IVRActionType actionType;		// 操作类型 【打分机、打队列、打手机、Playback语音、Read语音、播放语音根据按键到指定坐席】

	@Size(min = 0, max = 80)
	@Column(columnDefinition="character varying(80)")
	private String extenName;		// 分机名称
	
	@Size(min = 0, max = 128)
	@Column(columnDefinition="character varying(128)")
	private String queueName;		// 队列名称

	@Size(min = 0, max = 64)
	@Column(columnDefinition="character varying(64)")
	private String mobileNumber;	// 手机号码
	
	@Size(min = 0, max = 80)
	@Column(columnDefinition="character varying(80)")
	private String outlineName;	// 外线名称

	@Column(columnDefinition="integer")
	private Integer errorOpportunity;	// 如果是 Read 类型，客户允许输错按键的次数。
	
	@Size(min = 0, max = 255)
	@Column(columnDefinition="character varying(255)")
	private String agiName;				// 读语音后要调用的agi, 如 blacklistItem.agi
	
	@Size(min = 0, max = 255)
	@Column(columnDefinition="character varying(255)")
	private String agiDescription;		// 读语音后要调用的agi的功能描述
	
	@Column(columnDefinition="boolean not null default false", nullable = false)
	private Boolean isRootAction = false;	// 是否为IVR 的第一个Action
	
	//和语音文件的 n-->1 关系		结束语
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = SoundFile.class)
	private SoundFile soundFile;	// 要Playback 或者 Read 的语音文件

	@ManyToOne(fetch = FetchType.LAZY, targetEntity = IVRMenu.class)
	private IVRMenu ivrMenu;		// 所属IVR

	@ManyToOne(fetch = FetchType.LAZY, targetEntity = Domain.class)
	@JoinColumn(name = "domain_id", nullable = false)
	private Domain domain;

	@Override
	public String toString() {
		return  ivrActionName+"、"+description;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getIvrActionName() {
		return ivrActionName;
	}

	public void setIvrActionName(String ivrActionName) {
		this.ivrActionName = ivrActionName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public IVRActionType getActionType() {
		return actionType;
	}

	public void setActionType(IVRActionType actionType) {
		this.actionType = actionType;
	}

	public String getExtenName() {
		return extenName;
	}

	public void setExtenName(String extenName) {
		this.extenName = extenName;
	}

	public String getQueueName() {
		return queueName;
	}

	public void setQueueName(String queueName) {
		this.queueName = queueName;
	}

	public String getMobileNumber() {
		return mobileNumber;
	}

	public void setMobileNumber(String mobileNumber) {
		this.mobileNumber = mobileNumber;
	}

	public String getOutlineName() {
		return outlineName;
	}

	public void setOutlineName(String outlineName) {
		this.outlineName = outlineName;
	}

	public Integer getErrorOpportunity() {
		return errorOpportunity;
	}

	public void setErrorOpportunity(Integer errorOpportunity) {
		this.errorOpportunity = errorOpportunity;
	}

	public String getAgiName() {
		return agiName;
	}

	public void setAgiName(String agiName) {
		this.agiName = agiName;
	}

	public String getAgiDescription() {
		return agiDescription;
	}

	public void setAgiDescription(String agiDescription) {
		this.agiDescription = agiDescription;
	}

	public Boolean getIsRootAction() {
		return isRootAction;
	}

	public void setIsRootAction(Boolean isRootAction) {
		this.isRootAction = isRootAction;
	}

	public SoundFile getSoundFile() {
		return soundFile;
	}

	public void setSoundFile(SoundFile soundFile) {
		this.soundFile = soundFile;
	}

	public IVRMenu getIvrMenu() {
		return ivrMenu;
	}

	public void setIvrMenu(IVRMenu ivrMenu) {
		this.ivrMenu = ivrMenu;
	}

	public Domain getDomain() {
		return domain;
	}

	public void setDomain(Domain domain) {
		this.domain = domain;
	}
	
}
