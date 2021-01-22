package com.jiangyifen.ec2.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 * 外线与黑名单对应关系[按外线区分黑名单]
 * @author jrh
 *  2013-7-25
 */
@Entity
@Table(name = "ec2_outline_2_blacklist_item_link")
public class Outline2BlacklistItemLink {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "outline_2_blacklist_item_link")
	@SequenceGenerator(name = "outline_2_blacklist_item_link", sequenceName = "seq_ec2_outline_2_blacklist_item_link_id", allocationSize = 1)
	private Long id;
	
	// 外线Id
	@Column(columnDefinition="bigint NOT NULL", nullable=false)
	private Long outlineId;
	
	// 对应黑名单Id
	@Column(columnDefinition="bigint NOT NULL", nullable=false)
	private Long blacklistItemId;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	/** 获取外线Id */
	public Long getOutlineId() {
		return outlineId;
	}

	public void setOutlineId(Long outlineId) {
		this.outlineId = outlineId;
	}
	
	/** 获取黑名单Id */
	public Long getBlacklistItemId() {
		return blacklistItemId;
	}

	public void setBlacklistItemId(Long blacklistItemId) {
		this.blacklistItemId = blacklistItemId;
	}

}
