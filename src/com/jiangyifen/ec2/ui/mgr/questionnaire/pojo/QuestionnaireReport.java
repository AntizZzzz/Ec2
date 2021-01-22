package com.jiangyifen.ec2.ui.mgr.questionnaire.pojo;

import java.util.List;

import com.jiangyifen.ec2.entity.Questionnaire;

public class QuestionnaireReport {

	private Questionnaire questionnaire;
	private int doCount;
	private int effectiveCount;
	private List<QuestionReport> questionReports;
	
	public Questionnaire getQuestionnaire() {
		return questionnaire;
	}
	public void setQuestionnaire(Questionnaire questionnaire) {
		this.questionnaire = questionnaire;
	}
	public List<QuestionReport> getQuestionReports() {
		return questionReports;
	}
	public void setQuestionReports(List<QuestionReport> questionReports) {
		this.questionReports = questionReports;
	}
	public int getDoCount() {
		return doCount;
	}
	public void setDoCount(int doCount) {
		this.doCount = doCount;
	}
	public int getEffectiveCount() {
		return effectiveCount;
	}
	public void setEffectiveCount(int effectiveCount) {
		this.effectiveCount = effectiveCount;
	}
	
}
