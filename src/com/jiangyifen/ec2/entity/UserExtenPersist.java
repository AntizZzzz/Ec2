package com.jiangyifen.ec2.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.Size;

//对界面权限控制
@Entity
@Table(name = "ec2_user_exten_persist")
public class UserExtenPersist {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_exten_persist")
	@SequenceGenerator(name = "user_exten_persist", sequenceName = "seq_user_exten_persist_id", allocationSize = 1)
	private Long id;
	
	private Long userId;
	
	@Size(min = 0, max = 64)
	@Column(columnDefinition="character varying(64)")
	private String exten;
	
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public String getExten() {
		return exten;
	}

	public void setExten(String exten) {
		this.exten = exten;
	}
	
	
}
