package com.jiangyifen.ec2.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 客服记录状态导航键
 * @author JHT
 * @date 2014-11-18 上午9:13:45
 */
@Entity
@Table(name = "ec2_customer_service_record_status_navigation_key")
public class CustomerServiceRecordStatusNavigationKey {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "customer_service_record_status_navigation_key")
	@SequenceGenerator(name = "customer_service_record_status_navigation_key", sequenceName = "seq_ec2_customer_service_record_status_navigation_key_id", allocationSize = 1)
	private Long id;	// 主键唯一标识
	
	@Size(min = 0, max = 64)
	@Column(columnDefinition = "character varying(64) NOT NULL default ''", nullable = false)
	private String inputKey;				// 配置输入的按键
	
	@Column(columnDefinition = "boolean not null default false", nullable = false)
	private Boolean enabled = true;		// 状态是否可用， 默认为true表示可用
	
	@NotNull
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = CustomerServiceRecordStatus.class)
	private CustomerServiceRecordStatus serviceRecordStatus;	// 客服记录状态
	
	@NotNull
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = Domain.class)
	private Domain domain;					// 域

//	@SuppressWarnings("unused")
//	@Transient	
//	private String statusName;	// 字段不被映射到数据库表结构里面
//	@SuppressWarnings("unused")
//	@Transient
//	private String direction;	// 字段不被映射到数据库表结构里面
//	
//	
	// ================== Getter 和 Setter 方法 ================== //
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getInputKey() {
		return inputKey;
	}

	public void setInputKey(String inputKey) {
		this.inputKey = inputKey;
	}

	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

	public Domain getDomain() {
		return domain;
	}

	public void setDomain(Domain domain) {
		this.domain = domain;
	}

	public CustomerServiceRecordStatus getServiceRecordStatus() {
		return serviceRecordStatus;
	}

	public void setServiceRecordStatus(CustomerServiceRecordStatus serviceRecordStatus) {
		this.serviceRecordStatus = serviceRecordStatus;
	}

}
