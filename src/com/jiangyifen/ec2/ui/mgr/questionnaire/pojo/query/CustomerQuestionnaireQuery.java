package com.jiangyifen.ec2.ui.mgr.questionnaire.pojo.query;

import java.util.Date;
import java.util.List;

public class CustomerQuestionnaireQuery{

	private Long id;
	private Long csrId;
 	private String csrRealName;
	private String csrUserName;
	private String csrEmpNo;
	
	private Long cresId;
	private String cresName;
	
	private String tel_number;
	private Long projectId;
	private String projectName;
	private String questionnaireName;
	private String questionnaireFinish;
	private Date questionnaireEndTime;
	private Date questionnaireStartTime;
	
	private List<String> ans;
	
	public Long getCsrId() {
		return csrId;
	}
	public void setCsrId(Long csrId) {
		this.csrId = csrId;
	}
	public String getCsrRealName() {
		return csrRealName;
	}
	public void setCsrRealName(String csrRealName) {
		this.csrRealName = csrRealName;
	}
	public String getCsrUserName() {
		return csrUserName;
	}
	public void setCsrUserName(String csrUserName) {
		this.csrUserName = csrUserName;
	}
	public String getCsrEmpNo() {
		return csrEmpNo;
	}
	public void setCsrEmpNo(String csrEmpNo) {
		this.csrEmpNo = csrEmpNo;
	}
	public Long getCresId() {
		return cresId;
	}
	public void setCresId(Long cresId) {
		this.cresId = cresId;
	}
	public String getCresName() {
		return cresName;
	}
	public void setCresName(String cresName) {
		this.cresName = cresName;
	}
	public String getTel_number() {
		return tel_number;
	}
	public void setTel_number(String tel_number) {
		this.tel_number = tel_number;
	}
	public String getProjectName() {
		return projectName;
	}
	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}
	public String getQuestionnaireName() {
		return questionnaireName;
	}
	public void setQuestionnaireName(String questionnaireName) {
		this.questionnaireName = questionnaireName;
	}
	public String getQuestionnaireFinish() {
		return questionnaireFinish;
	}
	public void setQuestionnaireFinish(String questionnaireFinish) {
		this.questionnaireFinish = questionnaireFinish;
	}
	public Date getQuestionnaireEndTime() {
		return questionnaireEndTime;
	}
	public void setQuestionnaireEndTime(Date questionnaireEndTime) {
		this.questionnaireEndTime = questionnaireEndTime;
	}
	public Date getQuestionnaireStartTime() {
		return questionnaireStartTime;
	}
	public void setQuestionnaireStartTime(Date questionnaireStartTime) {
		this.questionnaireStartTime = questionnaireStartTime;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getProjectId() {
		return projectId;
	}
	public void setProjectId(Long projectId) {
		this.projectId = projectId;
	}
	public List<String> getAns() {
		return ans;
	}
	public void setAns(List<String> ans) {
		this.ans = ans;
	}
	
	 
	 
}
