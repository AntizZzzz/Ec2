package com.jiangyifen.ec2.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
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
@Table(name = "ec2_question")
public class Question {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ec2_question")
	@SequenceGenerator(name = "ec2_question", sequenceName = "seq_ec2_question_id", allocationSize = 1)
	private Long id; 
	
	@Column 
	private String title;
	
	@Column
	private int ordernumber;
	
	//创建时间
	@Temporal(TemporalType.TIMESTAMP)
	@Column(columnDefinition= "TIMESTAMP WITH TIME ZONE")
	private Date createTime;	
	
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = Questionnaire.class)
	private Questionnaire questionnaire;
	
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = QuestionType.class)
	private QuestionType questionType;
	
	/**
	 * 主键
	 * @return
	 */
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public int getOrdernumber() {
		return ordernumber;
	}
	public void setOrdernumber(int ordernumber) {
		this.ordernumber = ordernumber;
	}
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	public Questionnaire getQuestionnaire() {
		return questionnaire;
	}
	public void setQuestionnaire(Questionnaire questionnaire) {
		this.questionnaire = questionnaire;
	}
	public QuestionType getQuestionType() {
		return questionType;
	}
	public void setQuestionType(QuestionType questionType) {
		this.questionType = questionType;
	}
	@Override
	public String toString() {
		 
		return "title:"+this.title+"	createTime:"+this.createTime;
	}
	
}
