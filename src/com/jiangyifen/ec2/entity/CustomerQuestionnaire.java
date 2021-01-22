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
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

//TODO lxy	1305
/**
* 问卷调查
* @author lxy
*
*/
@Entity
@Table(name = "ec2_customer_questionnaire")
public class CustomerQuestionnaire {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ec2_customer_questionnaire")
	@SequenceGenerator(name = "ec2_customer_questionnaire", sequenceName = "seq_ec2_customer_questionnaire_id", allocationSize = 1)
	private Long id; 
	
	@Column 
	private Long userid;
	
	@Column
	private Long customerid;
	
	@Column
	private String finish;		// 文件完成情况 ，两个状态：start 部分完成或开始， end 已完成
	
	//创建时间
	@Temporal(TemporalType.TIMESTAMP)
	@Column(columnDefinition= "TIMESTAMP WITH TIME ZONE")
	private Date startTime;
	
	//创建时间  
	@Temporal(TemporalType.TIMESTAMP)
	@Column(columnDefinition= "TIMESTAMP WITH TIME ZONE")
	private Date endTime;	
	
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = Questionnaire.class)
	private Questionnaire questionnaire;

	// 与录音文件的关联关系
	@ManyToMany(fetch = FetchType.LAZY, targetEntity = RecordFile.class)
	@JoinTable(name = "ec2_customer_questionnaire_record_file_link",
				joinColumns = @JoinColumn(name = "customer_questionnaire_id"), 
				inverseJoinColumns = @JoinColumn(name = "record_file_id")
			)
	private Set<RecordFile> recordFiles = new HashSet<RecordFile>();	// 用户角色
    
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getUserid() {
		return userid;
	}

	public void setUserid(Long userid) {
		this.userid = userid;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public Questionnaire getQuestionnaire() {
		return questionnaire;
	}

	public void setQuestionnaire(Questionnaire questionnaire) {
		this.questionnaire = questionnaire;
	}

	public Long getCustomerid() {
		return customerid;
	}

	public void setCustomerid(Long customerid) {
		this.customerid = customerid;
	}

	/**
	 *  文件完成情况 ，两个状态 <br>start 部分完成或开始 <br> end 已完成
	 * @return
	 */
	public String getFinish() {
		return finish;
	}

	/**
	 * //start 没有完成，end已经完成
	 * @param finish
	 */
	public void setFinish(String finish) {
		this.finish = finish;
	}

	public Set<RecordFile> getRecordFiles() {
		return recordFiles;
	}

	public void setRecordFiles(Set<RecordFile> recordFiles) {
		this.recordFiles = recordFiles;
	}
	
}
