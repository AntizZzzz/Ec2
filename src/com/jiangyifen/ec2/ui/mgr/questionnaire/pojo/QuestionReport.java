package com.jiangyifen.ec2.ui.mgr.questionnaire.pojo;

import java.util.List;

import com.jiangyifen.ec2.entity.Question;

public class QuestionReport {

	private Question question;
	private List<QuestionOptionsReport> optionsReports;
	
	public Question getQuestion() {
		return question;
	}
	public void setQuestion(Question question) {
		this.question = question;
	}
	public List<QuestionOptionsReport> getOptionsReports() {
		return optionsReports;
	}
	public void setOptionsReports(List<QuestionOptionsReport> optionsReports) {
		this.optionsReports = optionsReports;
	}
	
	
}
