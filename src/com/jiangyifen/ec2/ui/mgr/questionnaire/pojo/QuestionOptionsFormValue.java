package com.jiangyifen.ec2.ui.mgr.questionnaire.pojo;

import com.jiangyifen.ec2.entity.QuestionOptions;

/**
 * 选项表获取值对象
 * 问题选项对象
 * 问题是否回答了文本
 * 
 * @author lxy
 *
 */
public class QuestionOptionsFormValue {

	private QuestionOptions options;
	
	private boolean textTF;
	
	private String optionText;

	public QuestionOptions getOptions() {
		return options;
	}

	public void setOptions(QuestionOptions options) {
		this.options = options;
	}

	public boolean isTextTF() {
		return textTF;
	}

	public void setTextTF(boolean textTF) {
		this.textTF = textTF;
	}

	public String getOptionText() {
		return optionText;
	}

	public void setOptionText(String optionText) {
		this.optionText = optionText;
	}
	
	
}
