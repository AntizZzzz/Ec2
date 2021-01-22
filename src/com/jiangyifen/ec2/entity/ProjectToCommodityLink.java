package com.jiangyifen.ec2.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 * 
 * @Description 描述：手动维护的项目于商品之间的对应关系 中间表
 * 
 * @author  jrh
 * @date    2013年12月5日 下午8:13:58
 * @version v1.0.0
 */
@Entity
@Table(name = "ec2_project_to_commodity_link")
public class ProjectToCommodityLink {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "project_to_commodity_link")
	@SequenceGenerator(name = "project_to_commodity_link", sequenceName = "seq_ec2_project_to_commodity_link_id", allocationSize = 1)
	private Long id;
	
	// 项目编号
	@Column(columnDefinition="bigint NOT NULL", nullable=false)
	private Long projectId;
	
	// 商品编号
	@Column(columnDefinition="bigint NOT NULL", nullable=false)
	private Long commodityId;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getProjectId() {
		return projectId;
	}

	public void setProjectId(Long projectId) {
		this.projectId = projectId;
	}

	public Long getCommodityId() {
		return commodityId;
	}

	public void setCommodityId(Long commodityId) {
		this.commodityId = commodityId;
	}

}
