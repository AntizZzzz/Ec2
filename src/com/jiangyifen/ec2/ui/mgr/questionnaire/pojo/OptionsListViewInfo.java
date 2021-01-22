package com.jiangyifen.ec2.ui.mgr.questionnaire.pojo;

import com.jiangyifen.ec2.entity.CustomerQuestionnaire;

/**
 * 窗体与选项组件通信对象
 * @author lxy
 *
 */
public class OptionsListViewInfo {

	private int initCode;
	private CustomerQuestionnaire customerQuestionnaire;
	
	public int getInitCode() {
		return initCode;
	}
	public void setInitCode(int initCode) {
		this.initCode = initCode;
	}
	public CustomerQuestionnaire getCustomerQuestionnaire() {
		return customerQuestionnaire;
	}
	public void setCustomerQuestionnaire(CustomerQuestionnaire customerQuestionnaire) {
		this.customerQuestionnaire = customerQuestionnaire;
	}
	
}
