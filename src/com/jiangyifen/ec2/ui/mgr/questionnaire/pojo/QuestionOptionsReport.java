package com.jiangyifen.ec2.ui.mgr.questionnaire.pojo;

import com.jiangyifen.ec2.entity.QuestionOptions;

public class QuestionOptionsReport {
	
	private QuestionOptions options;
	private int doCount;
	private int effectiveCount;
	private double doRatio;
	private double effectiveRatio;
	public QuestionOptions getOptions() {
		return options;
	}
	public void setOptions(QuestionOptions options) {
		this.options = options;
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
	public double getDoRatio() {
		return doRatio;
	}
	public void setDoRatio(double doRatio) {
		this.doRatio = doRatio;
	}
	public double getEffectiveRatio() {
		return effectiveRatio;
	}
	public void setEffectiveRatio(double effectiveRatio) {
		this.effectiveRatio = effectiveRatio;
	}
	
	 
}
