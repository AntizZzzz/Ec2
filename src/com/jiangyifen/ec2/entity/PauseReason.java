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
@Table(name = "ec2_pause_reason")
public class PauseReason {
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pause_reason")
	@SequenceGenerator(name = "pause_reason", sequenceName = "seq_ec2_pause_reason_id", allocationSize = 1)
	private Long id;
	
	@Size(min = 0, max = 128)
	@Column(columnDefinition="character varying(128)")
	private String reason;		// 置忙原因
	
	//该状态是否可用，便于管理员进行控制 		默认 true 表示可用
	@Column(columnDefinition="boolean not null default true", nullable = false)
	private boolean enabled = true;	// 是否可用
	
	// 域不能为空
	@NotNull
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = Domain.class)
	private Domain domain;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	public String getReason() {
		return reason;
	}
	
	public void setReason(String reason) {
		this.reason = reason;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public Domain getDomain() {
		return domain;
	}

	public void setDomain(Domain domain) {
		this.domain = domain;
	}
	
	public String toString() {
		return reason;
	}
	
}
