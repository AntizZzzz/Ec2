package com.jiangyifen.ec2.entity;

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

import com.jiangyifen.ec2.entity.enumtype.IVROptionType;

/**
 * 
 * @Description 描述：客户在呼入IVR 的流程中按键信息，及其针对该按键要做的处理
 * 
 * @author  jrh
 * @date    2014年2月19日 下午2:23:03
 * @version v1.0.0
 */
@Entity
@Table(name = "ec2_ivr_option")
public class IVROption {
	
//	public static String[] press_keys = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "*"};
	public static String[] press_keys = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "*", "#"};
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ivr_option")
	@SequenceGenerator(name = "ivr_option", sequenceName = "seq_ec2_ivr_option_id", allocationSize = 1)
	private Long id;

	@Size(min = 0, max = 128)
	@Column(columnDefinition = "character varying(128)")
	private String ivrOptionName;	// 名称
	
	@Size(min = 0, max = 256)
	@Column(columnDefinition = "character varying(256)")
	private String description;		// 描述信息

	@Size(min = 0, max = 64)
	@Column(columnDefinition = "character varying(64) NOT NULL", nullable=false)
	private String pressNumber;		// 客户按键

	@Column(columnDefinition="integer")
	private Integer layerNumber;	// 当前用户是处于IVR 的地几层按键

	/**
	 * 类型值有：
	 * 		1、呼叫分机：				 toExten 
	 * 		2、呼入队列: 				 toQueue
	 * 		3、转呼手机: 				 toMobile
	 * 		4、Playback语音: 		 	 toPlayback
	 * 		5、Read语音:		 		 toRead
	 * 		6、重听:		 		 	 toRepeat
	 * 		7、返回上一级:		 		 toReturnPre
	 * 		8、返回到主菜单:		 		 toReturnRoot
	 * 		9、Read语音根据按键到执行Agi:   toReadForAgi 	如-根据按键将呼入客户接通到指定坐席（该坐席是客户自己选择的）
	 */
	@NotNull
	@Enumerated
	private IVROptionType ivrOptionType;			// 功能类型

	@ManyToOne(fetch = FetchType.LAZY, targetEntity = IVRAction.class)
	private IVRAction currentIvrAction;	// 当前客户是处于哪一个action 下按的键
	
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = IVRAction.class)
	private IVRAction nextIvrAction;	// 当前用户按的键，代表着下一步要做什么处理

	@ManyToOne(fetch = FetchType.LAZY, targetEntity = IVRMenu.class)
	private IVRMenu ivrMenu;		// 所属IVR
	
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = Domain.class)
	@JoinColumn(name = "domain_id", nullable = false)
	private Domain domain;

	
	public IVRMenu getIvrMenu() {
		return ivrMenu;
	}

	public void setIvrMenu(IVRMenu ivrMenu) {
		this.ivrMenu = ivrMenu;
	}

	@Override
	public String toString() {
		return  ivrOptionName;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getIvrOptionName() {
		return ivrOptionName;
	}

	public void setIvrOptionName(String ivrOptionName) {
		this.ivrOptionName = ivrOptionName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getPressNumber() {
		return pressNumber;
	}

	public void setPressNumber(String pressNumber) {
		this.pressNumber = pressNumber;
	}

	public Integer getLayerNumber() {
		return layerNumber;
	}

	public void setLayerNumber(Integer layerNumber) {
		this.layerNumber = layerNumber;
	}

	public IVROptionType getIvrOptionType() {
		return ivrOptionType;
	}

	public void setIvrOptionType(IVROptionType ivrOptionType) {
		this.ivrOptionType = ivrOptionType;
	}

	public IVRAction getCurrentIvrAction() {
		return currentIvrAction;
	}

	public void setCurrentIvrAction(IVRAction currentIvrAction) {
		this.currentIvrAction = currentIvrAction;
	}

	public IVRAction getNextIvrAction() {
		return nextIvrAction;
	}

	public void setNextIvrAction(IVRAction nextIvrAction) {
		this.nextIvrAction = nextIvrAction;
	}

	public Domain getDomain() {
		return domain;
	}

	public void setDomain(Domain domain) {
		this.domain = domain;
	}
	
}
