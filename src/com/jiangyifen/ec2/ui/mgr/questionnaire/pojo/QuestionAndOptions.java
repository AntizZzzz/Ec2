package com.jiangyifen.ec2.ui.mgr.questionnaire.pojo;

import java.util.List;

import com.jiangyifen.ec2.entity.Question;
import com.jiangyifen.ec2.entity.QuestionOptions;
import com.jiangyifen.ec2.entity.Questionnaire;

public class QuestionAndOptions {

	private Questionnaire questionnaire;
	private Question question;
	private List<QuestionOptions> questionOptions;
	
	public Questionnaire getQuestionnaire() {
		return questionnaire;
	}
	public void setQuestionnaire(Questionnaire questionnaire) {
		this.questionnaire = questionnaire;
	}
	public Question getQuestion() {
		return question;
	}
	public void setQuestion(Question question) {
		this.question = question;
	}
	public List<QuestionOptions> getQuestionOptions() {
		return questionOptions;
	}
	public void setQuestionOptions(List<QuestionOptions> questionOptions) {
		this.questionOptions = questionOptions;
	}
}
