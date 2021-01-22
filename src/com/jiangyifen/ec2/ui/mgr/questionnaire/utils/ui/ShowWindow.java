package com.jiangyifen.ec2.ui.mgr.questionnaire.utils.ui;

import com.jiangyifen.ec2.ui.mgr.tabsheet.QuestionnaireManagement;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class ShowWindow extends Window implements Window.CloseListener,ClickListener{
	/**
	 * 
	 */
	private static final long serialVersionUID = 2042314386006193205L;

	private QuestionnaireManagement questionnaireManagement;
	
	private Button btn_ok;		//保存关闭
	private Button btn_ccl;		//取消按钮
	
	private String methodName;
	
	public ShowWindow(QuestionnaireManagement questionnaireManagement,String methodName,String title,String width,String height){
		this.center();
		this.setModal(true);
		this.setWidth(width);
		this.setHeight(height);
		this.setCaption("系统提示");
		
		this.questionnaireManagement = questionnaireManagement;
		this.methodName = methodName;
		
		VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.setSpacing(true);
		Label lb_msg = new Label(title);
		lb_msg.setSizeFull();
		mainLayout.addComponent(lb_msg);
		
		HorizontalLayout foolter = new HorizontalLayout();
		foolter.setSpacing(true);
		foolter.setSizeFull();
		btn_ok = new Button("确定");
		btn_ok.addListener(this);
		btn_ok.setStyleName("default");
		foolter.addComponent(btn_ok);
		
		btn_ccl = new Button("取消",this);
		btn_ccl.addListener(this);
		foolter.addComponent(btn_ccl);
		
		mainLayout.addComponent(foolter);
		this.addComponent(mainLayout);
	}
	
	@Override
	public void windowClose(CloseEvent e) {
		if("deleteQuestionnaire".equals(methodName)){
			questionnaireManagement.deleteQuestionnaire(false);	
		}
		if("closeQuestionnaire".equals(methodName)){
			questionnaireManagement.closeQuestionnaire(false);
		}
		closeWindow();
	}

	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if(source == btn_ok){
			if("deleteQuestionnaire".equals(methodName)){
				questionnaireManagement.deleteQuestionnaire(true);	
			}
			if("closeQuestionnaire".equals(methodName)){
				questionnaireManagement.closeQuestionnaire(true);
			}
		}
		if(source == btn_ccl){
			if("deleteQuestionnaire".equals(methodName)){
				questionnaireManagement.deleteQuestionnaire(false);	
			}
			if("closeQuestionnaire".equals(methodName)){
				questionnaireManagement.closeQuestionnaire(false);
			}
		}
		closeWindow();
	}
	
	public void closeWindow(){
		this.getApplication().getMainWindow().removeWindow(this);
	}
}
