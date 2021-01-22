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

//TODO lxy	1305
/**
* 问卷调查
* @author lxy
*
*/
@Entity
@Table(name = "ec2_customer_question_options")
public class CustomerQuestionOptions {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ec2_customer_question_options")
	@SequenceGenerator(name = "ec2_customer_question_options", sequenceName = "seq_ec2_customer_question_options_id", allocationSize = 1)
	private Long id;
	
	@Column(columnDefinition="text")
	private String text;
	
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = CustomerQuestionnaire.class)
	private CustomerQuestionnaire customerQuestionnaire;

	@ManyToOne(fetch = FetchType.LAZY, targetEntity = Question.class)
	private Question question;
	
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = QuestionOptions.class)
	private QuestionOptions questionOptions;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	public CustomerQuestionnaire getCustomerQuestionnaire() {
		return customerQuestionnaire;
	}

	public void setCustomerQuestionnaire(CustomerQuestionnaire customerQuestionnaire) {
		this.customerQuestionnaire = customerQuestionnaire;
	}

	public Question getQuestion() {
		return question;
	}

	public void setQuestion(Question question) {
		this.question = question;
	}

	public QuestionOptions getQuestionOptions() {
		return questionOptions;
	}

	public void setQuestionOptions(QuestionOptions questionOptions) {
		this.questionOptions = questionOptions;
	}

	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
}
