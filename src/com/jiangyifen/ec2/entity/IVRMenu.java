package com.jiangyifen.ec2.entity;

import java.util.Date;

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
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.jiangyifen.ec2.entity.enumtype.IVRActionType;
import com.jiangyifen.ec2.entity.enumtype.IVRMenuType;

/**
 * 
 * @Description 描述：呼入用的语音导航
 * 
 * @author  jrh
 * @date    2014年2月19日 下午1:56:50
 * @version v1.0.0
 */
@Entity
@Table(name = "ec2_ivr_menu")
public class IVRMenu {
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ivr_menu")
	@SequenceGenerator(name = "ivr_menu", sequenceName = "seq_ec2_ivr_menu_id", allocationSize = 1)
	private Long id;

	@Size(min = 0, max = 128)
	@Column(columnDefinition = "character varying(128) NOT NULL", nullable=false)
	private String ivrMenuName;		// 名称

	@Size(min = 0, max = 256)
	@Column(columnDefinition = "character varying(256)")
	private String description;		// 描述信息

	// 创建时间
	@Temporal(TemporalType.TIMESTAMP)
	@Column(columnDefinition = "TIMESTAMP WITH TIME ZONE NOT NULL", nullable = false)
	private Date createDate;
	
	@NotNull
	@Enumerated
	private IVRMenuType ivrMenuType = IVRMenuType.customize;		// 类型 【模板IVR、用户自定义IVR】

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
	private IVRActionType rootActionType;		// RootAction操作类型 【打分机、打队列、打手机、Playback语音、Read语音、播放语音根据按键到指定坐席】

	//和语音文件的 n-->1 关系		欢迎词
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = SoundFile.class)
	private SoundFile welcomeSoundFile;
	
	//和语音文件的 n-->1 关系		结束语
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = SoundFile.class)
	private SoundFile closeSoundFile;

	// 记录n-->1输入者 记录输入者
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = User.class)
	private User creator;
	
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = Domain.class)
	@JoinColumn(name = "domain_id", nullable = false)
	private Domain domain;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getIvrMenuName() {
		return ivrMenuName;
	}

	public void setIvrMenuName(String ivrMenuName) {
		this.ivrMenuName = ivrMenuName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public IVRMenuType getIvrMenuType() {
		return ivrMenuType;
	}

	public void setIvrMenuType(IVRMenuType ivrMenuType) {
		this.ivrMenuType = ivrMenuType;
	}

	public IVRActionType getRootActionType() {
		return rootActionType;
	}

	public void setRootActionType(IVRActionType rootActionType) {
		this.rootActionType = rootActionType;
	}

	public SoundFile getWelcomeSoundFile() {
		return welcomeSoundFile;
	}

	public void setWelcomeSoundFile(SoundFile welcomeSoundFile) {
		this.welcomeSoundFile = welcomeSoundFile;
	}

	public SoundFile getCloseSoundFile() {
		return closeSoundFile;
	}

	public void setCloseSoundFile(SoundFile closeSoundFile) {
		this.closeSoundFile = closeSoundFile;
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

	@Override
	public String toString() {
		return "编号："+id+" , 名称："+ivrMenuName;
	}

	public String toLoggerString() {
		return "IVRMenu [id=" + id + ", ivrMenuName=" + ivrMenuName
				+ ", description=" + description + ", createDate=" + createDate
				+ ", ivrMenuType=" + ivrMenuType + ", rootActionType="
				+ rootActionType + ", welcomeSoundFile=" + welcomeSoundFile
				+ ", closeSoundFile=" + closeSoundFile + ", creator=" + creator
				+ ", domain=" + domain + "]";
	}
	
}
