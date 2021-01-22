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

@Entity
@Table(name = "ec2_customer_service_record_status")
public class CustomerServiceRecordStatus {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "customer_service_record_status")
	@SequenceGenerator(name = "customer_service_record_status", sequenceName = "seq_ec2_customer_service_record_status_id", allocationSize = 1)
	private Long id;

	// 特定类型的状态名称
	@Size(min = 0, max = 32)
	@Column(nullable = false,columnDefinition="character varying(32) NOT NULL")
	private String statusName;
	
	// 当前状态“暗示”，是接通、还是未接通    默认 false 表示未接通
	@Column(columnDefinition="boolean not null default false", nullable = false)
	private Boolean isAnswered = false;
	
	//该状态是否可用，便于管理员进行控制 		默认 true 表示可用
	@Column(columnDefinition="boolean not null default true", nullable = false)
	private Boolean enabled = true;
	
	// 呼叫方向，outgoing 表示呼出，incoming 呼入 默认 ''
	@Size(min = 0, max = 64)
	@Column(columnDefinition="character varying(64) not null default ''", nullable = false)
	private String direction;
	
	// 该记录状态是否表示将对应的客户资源置为Vip客户
	@Column(columnDefinition="boolean not null default false", nullable = false)
	private Boolean isMeanVipCustomer = false;

	// =================对应关系=================//

	// 域的概念 批次1-->n域
	// 域不能为空
	@NotNull
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = Domain.class)
	private Domain domain;
	
	// ================= getter 和 setter 方法=================//
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getStatusName() {
		return statusName;
	}

	public void setStatusName(String statusName) {
		this.statusName = statusName;
	}

	
	public Boolean getIsAnswered() {
		return isAnswered;
	}

	public void setIsAnswered(Boolean isAnswered) {
		this.isAnswered = isAnswered;
	}

	public Boolean getEnabled() {
		return enabled;
	}

	public String getDirection() {
		return direction;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

	public void setDirection(String direction) {
		this.direction = direction;
	}

	public Domain getDomain() {
		return domain;
	}

	public void setDomain(Domain domain) {
		this.domain = domain;
	}
	
	public Boolean getIsMeanVipCustomer() {
		return isMeanVipCustomer;
	}

	public void setIsMeanVipCustomer(Boolean isMeanVipCustomer) {
		this.isMeanVipCustomer = isMeanVipCustomer;
	}

	public String toString() {
		return statusName;
	}
	
	
	public String getMigrateRecordStatus(){
		String direction_EN= " ";
		String enabled_EN = " ";
		if(direction.equals("incoming")){
			direction_EN = "呼入";
		} else if(direction.equals("outgoing")){
			direction_EN = "呼出";
		}
		
		if(enabled){
			enabled_EN = "可用";
		} else {
			enabled_EN = "禁用";
		}
		return statusName+"-"+direction_EN+"-"+enabled_EN;
	}
	
}
