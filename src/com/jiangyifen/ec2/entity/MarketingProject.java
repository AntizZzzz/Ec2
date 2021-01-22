package com.jiangyifen.ec2.entity;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.jiangyifen.ec2.bean.MarketingProjectStatus;
import com.jiangyifen.ec2.entity.enumtype.MarketingProjectType;

@Entity
@Table(name = "ec2_markering_project")
public class MarketingProject {
	public static String COMMISSIONER_ROUTING_ON="启用"; 
	public static String COMMISSIONER_ROUTING_OFF="不启用"; 
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "markering_project")
	@SequenceGenerator(name = "markering_project", sequenceName = "seq_ec2_markering_project_id", allocationSize = 1)
	private Long id;
	
	//项目名字
	@Size(min = 0, max = 64)
	@Column(nullable = false,columnDefinition="character varying(64) NOT NULL")
	private String projectName;
	
	//备注信息
	@Size(min = 0, max = 512)
	@Column(columnDefinition="character varying(512)") 
	private String note;
	
	//专员路由字段
	@Size(min = 0, max = 64)
	@Column(columnDefinition="character varying(64) NOT NULL")
	private String commissionerRouting=COMMISSIONER_ROUTING_OFF;
			
	//项目创建日期（是否需要结束日期？）
	@Temporal(TemporalType.TIMESTAMP)
	private Date createDate;
	
	//--------------------------- jrh 为完善功能而加 2013-6-17 -----------------------//
	// 任务类型 ，目前与任务类型相同 	默认项目类型为：营销项目
	@Enumerated 
	private MarketingProjectType marketingProjectType = MarketingProjectType.MARKETING;
	
	//--------------------------- jrh 为完善功能而加 2013-6-17 -----------------------//
	
	
	//--------------------------- jrh 为完善功能而加 2013-8-28 -----------------------//
	// 话务员最大未完成任务数
	@Column(columnDefinition="integer NOT NULL default 100", nullable = false)
	private Integer csrMaxUnfinishedTaskCount = 100;

	// 话务员每次最大获取任务数量
	@Column(columnDefinition="integer NOT NULL default 10", nullable = false)
	private Integer csrOnceMaxPickTaskCount = 10;
	
	//--------------------------- jrh 为完善功能而加 2013-8-28 -----------------------//
	
	public MarketingProject() {
	
	}

	//=================对应关系=================//
	//项目n-->1项目状态   项目的状态信息
	@Enumerated
	private MarketingProjectStatus marketingProjectStatus;
	
	//域的概念 批次1-->n域
	//域不能为空
	@NotNull
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = Domain.class)
	private Domain domain;
	
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = Queue.class)
	private Queue queue;
	
	//多个项目对应于一个用户
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = User.class)
	private User creater;
	
	//项目n-->n用户     一个项目由多个用户来做
	@ManyToMany(targetEntity = User.class,fetch=FetchType.LAZY)
	private Set<User> users;

	//项目n-->n用户     一个项目由多个用户来做
	@ManyToMany(targetEntity = CustomerResourceBatch.class,fetch=FetchType.LAZY)
	private Set<CustomerResourceBatch> batches;
	
	//项目n-->n问卷     一个项目对应对个问卷 jrh
	@ManyToMany(targetEntity = Questionnaire.class,fetch=FetchType.LAZY)
	@JoinTable(name = "ec2_marketing_project_questionnaire_link",
			joinColumns = @JoinColumn(name = "project_id"), 
			inverseJoinColumns = @JoinColumn(name = "questionnaire_id")
		)
	private Set<Questionnaire> questionnaires = new HashSet<Questionnaire>();

	//项目1-->1外线     一个项目可以使用一条外线
	@OneToOne(targetEntity = SipConfig.class,fetch=FetchType.LAZY)
	private SipConfig sip;
	
	//================= getter 和  setter 方法=================//

	public Long getId() {
		return id;
	}

	public String getProjectName() {
		return projectName;
	}

	public String getNote() {
		return note;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public MarketingProjectStatus getMarketingProjectStatus() {
		return marketingProjectStatus;
	}

	public MarketingProjectType getMarketingProjectType() {
		return marketingProjectType;
	}

	public void setMarketingProjectType(MarketingProjectType marketingProjectType) {
		this.marketingProjectType = marketingProjectType;
	}

	public Domain getDomain() {
		return domain;
	}

	public User getCreater() {
		return creater;
	}

	public Set<User> getUsers() {
		return users;
	}

	public Set<CustomerResourceBatch> getBatches() {
		return batches;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public void setMarketingProjectStatus(
			MarketingProjectStatus marketingProjectStatus) {
		this.marketingProjectStatus = marketingProjectStatus;
	}

	public void setDomain(Domain domain) {
		this.domain = domain;
	}

	public void setCreater(User creater) {
		this.creater = creater;
	}

	public void setUsers(Set<User> users) {
		this.users = users;
	}

	public void setBatches(Set<CustomerResourceBatch> batches) {
		this.batches = batches;
	}
	
	public String getCommissionerRouting() {
		return commissionerRouting;
	}

	public void setCommissionerRouting(String commissionerRouting) {
		this.commissionerRouting = commissionerRouting;
	}

	public Queue getQueue() {
		return queue;
	}

	public void setQueue(Queue queue) {
		this.queue = queue;
	}

	public SipConfig getSip() {
		return sip;
	}

	public void setSip(SipConfig sip) {
		this.sip = sip;
	}

	public Set<Questionnaire> getQuestionnaires() {
		return questionnaires;
	}

	public void setQuestionnaires(Set<Questionnaire> questionnaires) {
		this.questionnaires = questionnaires;
	}

	public Integer getCsrMaxUnfinishedTaskCount() {
		return csrMaxUnfinishedTaskCount;
	}

	public void setCsrMaxUnfinishedTaskCount(Integer csrMaxUnfinishedTaskCount) {
		this.csrMaxUnfinishedTaskCount = csrMaxUnfinishedTaskCount;
	}

	public Integer getCsrOnceMaxPickTaskCount() {
		return csrOnceMaxPickTaskCount;
	}

	public void setCsrOnceMaxPickTaskCount(Integer csrOnceMaxPickTaskCount) {
		this.csrOnceMaxPickTaskCount = csrOnceMaxPickTaskCount;
	}

	//-----------jrh 用于显示项目名称和姓名类型的组合内容  ----------------// 
	public String getProjectNameAndType() {
		return projectName +" - "+ marketingProjectType.getName();
	}
	
	public String toString() {
		return projectName;
	}
}
