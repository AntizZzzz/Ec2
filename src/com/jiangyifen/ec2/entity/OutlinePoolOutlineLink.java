package com.jiangyifen.ec2.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "ec2_outline_pool_outline_link")
public class OutlinePoolOutlineLink {


	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "outline_pool_outline_link")
	@SequenceGenerator(name = "outline_pool_outline_link", sequenceName = "seq_ec2_outline_pool_outline_link_id", allocationSize = 1)
	private Long id;
	
	// 号码池编号
	@Column(columnDefinition="bigint NOT NULL", nullable=false)
	private Long poolId;

	// 外线编号
	@Column(columnDefinition="bigint NOT NULL", nullable=false)
	private Long outlineId;

	public OutlinePoolOutlineLink() {
	}

	
	
	public OutlinePoolOutlineLink(Long poolId, Long outlineId) {
		this.poolId = poolId;
		this.outlineId = outlineId;
	}



	public Long getPoolId() {
		return poolId;
	}

	public void setPoolId(Long poolId) {
		this.poolId = poolId;
	}

	public Long getOutlineId() {
		return outlineId;
	}

	public void setOutlineId(Long outlineId) {
		this.outlineId = outlineId;
	}
	
	
}
