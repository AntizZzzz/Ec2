package com.jiangyifen.ec2.entity;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

//TODO lxy	1305
/**
* 问卷调查
* @author lxy
*
*/
@Entity
@Table(name = "ec2_questionnaire")
public class Questionnaire {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ec2_questionnaire")
	@SequenceGenerator(name = "ec2_questionnaire", sequenceName = "seq_ec2_questionnaire_id", allocationSize = 1)
	private Long id; 
	
	@Size(min = 0, max = 200)
	@Column(columnDefinition="character varying(200) NOT NULL", nullable = false)
	private String mainTitle;
	
	@Size(min = 0, max = 500)
	@Column(columnDefinition="character varying(500)")
	private String subtitle;
	
	@Size(min = 0, max = 1)
	@Column(columnDefinition="character varying(1)")
	private String stateCode;
	
	//创建时间
	@Temporal(TemporalType.TIMESTAMP)
	@Column(columnDefinition= "TIMESTAMP WITH TIME ZONE")
	private Date createTime;	

	// 项目n-->n问卷     一个项目对应对个问卷 jrh
	@ManyToMany(mappedBy = "questionnaires", fetch = FetchType.LAZY, targetEntity = MarketingProject.class)
	private Set<MarketingProject> marketingProjects = new HashSet<MarketingProject>();
	
    @NotNull
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = Domain.class)
	@JoinColumn(name = "domain_id", nullable = false)
	private Domain domain;	//所属公司
	
    //lxy 2013-07-15 问卷类型 
    @Column
	private int showcode;
    
    //lxy 2013-07-23 记录的内容
  	@Column(columnDefinition = "TEXT")
  	private String note;
  	
	/**
	 * 主键
	 * @return
	 */
	public Long getId() {
		return id;
	}
	/*
	 * 主键
	 */
	public void setId(Long id) {
		this.id = id;
	}

	public String getMainTitle() {
		return mainTitle;
	}
	/*
	 * 主标题
	 */
	public void setMainTitle(String mainTitle) {
		this.mainTitle = mainTitle;
	}
	/**
	 * 副标题
	 * @return
	 */
	public String getSubtitle() {
		return subtitle;
	}
	/*
	 * 副标题
	 */
	public void setSubtitle(String subtitle) {
		this.subtitle = subtitle;
	}
	/**
	 * 创建时间
	 * @return
	 */
	public Date getCreateTime() {
		return createTime;
	}
	/**
	 * 创建时间
	 * @param createTime
	 */
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public String getStateCode() {
		return stateCode;
	}
	
	public void setStateCode(String stateCode) {
		this.stateCode = stateCode;
	}
	
	public Set<MarketingProject> getMarketingProjects() {
		return marketingProjects;
	}
	
	public void setMarketingProjects(Set<MarketingProject> marketingProjects) {
		this.marketingProjects = marketingProjects;
	}
	
	public Domain getDomain() {
		return domain;
	}
	
	public void setDomain(Domain domain) {
		this.domain = domain;
	}
	
	/**
	 * 问卷类型 1、是跳转问卷  2、是展示全部问卷
	 * @return
	 */
	public int getShowcode() {
		return showcode;
	}
	
	/**
	 * 问卷类型 1、是跳转问卷  2、是展示全部问卷
	 * @param showcode
	 */
	public void setShowcode(int showcode) {
		this.showcode = showcode;
	}
	
	/**
	 * 备注信息
	 * @return
	 */
	public String getNote() {
		return note;
	}
	
	/**
	 * 备注信息
	 * @param remark
	 */
	public void setNote(String note) {
		this.note = note;
	}
 
	
}
