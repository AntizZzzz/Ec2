package com.jiangyifen.ec2.ui.mgr.questionnaire;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.entity.Question;
import com.jiangyifen.ec2.entity.QuestionOptions;
import com.jiangyifen.ec2.entity.Questionnaire;
import com.jiangyifen.ec2.service.eaoservice.QuestionService;
import com.jiangyifen.ec2.service.eaoservice.QuestionnaireService;
import com.jiangyifen.ec2.ui.mgr.questionnaire.pojo.ui.QuestionOptionsListView;
import com.jiangyifen.ec2.ui.mgr.tabsheet.QuestionnaireManagement;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class ShowQuestionnaireBrowseShowAll  extends Window implements ClickListener{
		//静态
		private static final long serialVersionUID = 223952958705792239L;
		private final Logger logger = LoggerFactory.getLogger(this.getClass());		// 日志工具
		
		// 持有UI对象
		private QuestionnaireManagement questionnaireManagement;
		// 本控件持有对象
		
		
		// 页面布局组件
		private HorizontalLayout contentExcuteLayout;
		private HorizontalLayout contentLayout;
		private VerticalLayout leftContent;
		private VerticalLayout rightContent;
		private HorizontalLayout optLayout;
		private HorizontalLayout buttonLayout;
		private HorizontalLayout panelLayout;
		// 页面控件
		private Label lb_msg;
		private Button bt_excute;
		private Button bt_excute_save;
		private Button bt_cancel;
  
		private Panel pl_note;
		// 业务必须对象
		
		
		// 业务本页对象
		private Integer[] screenResolution;							// 屏幕分辨率
 		private Questionnaire questionnaire;
		
		// 业务注入对象
 		private QuestionnaireService questionnaireService;
		private QuestionService questionService;
		
		public ShowQuestionnaireBrowseShowAll(QuestionnaireManagement questionnaireManagement){
			this.questionnaireManagement = questionnaireManagement;
			initMainWindow();
			initWindowSize();
			initlayout();
			initSpringContext();
		}

		private void initMainWindow(){
			this.center();
			this.setModal(false);
			this.setResizable(false);
			
		}
		
		private void initlayout(){
	 		buildContentLayout();
	 		buildContentExcuteLayout();
	 		buildOptLayout();
	 		buildBottomLayout();	
	 		buildPanelLayout();
	 		
			this.addComponent(contentLayout);
			this.addComponent(contentExcuteLayout);
			this.addComponent(optLayout);
			this.addComponent(new Label(" ",Label.CONTENT_XHTML));
			this.addComponent(buttonLayout);	
			this.addComponent(panelLayout);	
			
			lb_msg = new Label("0000000");
			lb_msg.setWidth("-1px");
			lb_msg.setContentMode(Label.CONTENT_XHTML);
			this.addComponent(lb_msg);
		}
		
		private void buildContentExcuteLayout() {
			contentExcuteLayout = new HorizontalLayout();
			contentExcuteLayout.setSizeFull();
			contentExcuteLayout.setMargin(true);
			
		}

		private void buildContentLayout(){
			contentLayout = new HorizontalLayout();
			contentLayout.setSizeFull();
			contentLayout.setSpacing(true);
			
			leftContent = new VerticalLayout();
			leftContent.setSizeFull();
			leftContent.setSpacing(false);
	 		 
			rightContent = new VerticalLayout();
			rightContent.setSizeFull();
			rightContent.setSpacing(false);
	 	 
			contentLayout.addComponent(leftContent);
			contentLayout.addComponent(rightContent);
			
		}
		private void buildOptLayout(){
			optLayout = new HorizontalLayout();
			optLayout.setSizeFull();
			optLayout.setSpacing(false);
			optLayout.setMargin(false,true,false,true);
		}
		
		private void buildBottomLayout(){
			buttonLayout = new HorizontalLayout();
			buttonLayout.setSpacing(true);
			buttonLayout.setMargin(true,true,true,true);
			
			bt_excute = new Button("执行问卷",this);
	 		bt_excute.setStyleName("default");
	 		buttonLayout.addComponent(bt_excute);
			
	 		bt_excute_save = new Button("确定执行",this);
	 		bt_excute_save.setStyleName("default");
	 		buttonLayout.addComponent(bt_excute_save);
	 		
			bt_cancel = new Button("关闭窗口",this);
			buttonLayout.addComponent(bt_cancel);
		}
		
		private void buildPanelLayout(){
			panelLayout = new HorizontalLayout();
		}
		
		private void initWindowSize() {
			 screenResolution = SpringContextHolder.getScreenResolution(); 
	 		 if(screenResolution[0] >= 1366) {
					this.setWidth("1090px");
				} else {
					this.setWidth("940px");
				}
				if(screenResolution[1] > 768) {
					this.setHeight("610px");//610px
				} else if(screenResolution[1] == 768) {
					this.setHeight("560px");
				} else {
					this.setHeight("510px");
				}
		}

		/**初始化Spring上写相关系想你 */
		private void initSpringContext(){
 	 		questionService = SpringContextHolder.getBean("questionService");
 	 		questionnaireService = SpringContextHolder.getBean("questionnaireService");
 	 		
		}
		 
		@Override
		public void buttonClick(ClickEvent event) {
			Button source = event.getButton();
			if(source == bt_excute){
  				this.bt_excute.setVisible(false);
				this.bt_cancel.setVisible(true);
				this.bt_excute_save.setVisible(true);
				contentLayout.setVisible(false);
				
				contentExcuteLayout.setVisible(true);
				contentExcuteLayout.removeAllComponents();
				contentExcuteLayout.addComponent(new Label("<font color='red'>在执行状态下的问卷不能修改，您确定要执行问卷吗？</font>",Label.CONTENT_XHTML));
			}else if(source == bt_excute_save){//取消按钮
				stateCodeQuestionnaire();
				questionnaireManagement.updateTable(false);
 				this.getApplication().getMainWindow().removeWindow(this);
			}
			else if(source == bt_cancel){//取消按钮
				this.getApplication().getMainWindow().removeWindow(this);
			}
		}

		public void stateCodeQuestionnaire() {
			try {
				if(questionnaire != null){
					questionnaireService.stateCodeQuestionnaireById(questionnaire.getId());
					this.getApplication().getMainWindow().showNotification("执行问卷成功！", Notification.TYPE_HUMANIZED_MESSAGE);
				}
			} catch (Exception e) {
				e.printStackTrace();
				logger.error("执行问卷异常_stateCodeQuestionnaire_LLXXYY", e);
				this.getApplication()
						.getMainWindow()
						.showNotification("执行问卷异常!",
								Notification.TYPE_ERROR_MESSAGE);
			}
		}
		
		public void refreshComponentInfo(Questionnaire target) {
			this.lb_msg.setValue("");
			this.bt_excute.setVisible(true);
			this.questionnaire = target;
			if(questionnaire != null){
				updateQuestionnaireUI();
	 			updateQuestionsUI();
	 			updateButtons();
			}else{
				this.bt_excute.setVisible(false);
				this.lb_msg.setValue("<font color='red'>抱歉，未找到问问卷，请添加问卷!</font>");
			}
			contentLayout.setVisible(true);
			contentExcuteLayout.setVisible(false);
		}
		

		private void updateQuestionsUI() {
			this.setCaption(questionnaire.getMainTitle());
			List<Question> questionList =  questionService.getQuestionListByQuestionnaireId(questionnaire.getId());
			leftContent.removeAllComponents();
			rightContent.removeAllComponents();
		
 			int quesCount = questionList.size();
			if(quesCount > 0){
				int avg = (quesCount+1)/2;
				for (int i = 0; i < questionList.size(); i++) {
					if(i < avg){
						buildLeftContentQuestionUI(questionList.get(i));
					}else{
						buildRightContentQuestionUI(questionList.get(i));
					}
				}
			}else{
				this.bt_excute.setVisible(false);
				this.lb_msg.setValue("<font color='red'>抱歉，未找到问题，请添加问题!</font>");
			}
 			
		}

		private void updateQuestionnaireUI() {
			this.setCaption("问卷::"+questionnaire.getMainTitle());
			panelLayout.removeAllComponents();
			if(null != questionnaire.getNote() && questionnaire.getNote().length()>0){
				pl_note = new Panel();
	 			TextArea ta_note = new TextArea();
				ta_note.setRows(4);
				ta_note.setColumns(79);
				ta_note.setMaxLength(2000);
				ta_note.setValue(questionnaire.getNote());
				ta_note.setReadOnly(true);
				pl_note.addComponent(ta_note);
				panelLayout.addComponent(pl_note);
			} 
		}
 
		private void updateButtons() {
 			this.bt_cancel.setVisible(true);
			this.bt_excute_save.setVisible(false);
		}
		private void  buildLeftContentQuestionUI(Question question){
			List<QuestionOptions> optionsList = questionService.getQuestionOptionsListByQuestionId(question.getId());
			int index = questionService.getIndexByQuestionnaireAndQuestionOrder(questionnaire.getId(),question.getOrdernumber());
 			QuestionOptionsListView listView = new QuestionOptionsListView(question, optionsList, index+".",null);
			leftContent.addComponent(listView);
			leftContent.addComponent(new Label("<hr>",Label.CONTENT_XHTML));
		}
		
		private void  buildRightContentQuestionUI(Question question){
			List<QuestionOptions> optionsList = questionService.getQuestionOptionsListByQuestionId(question.getId());
			int index = questionService.getIndexByQuestionnaireAndQuestionOrder(questionnaire.getId(),question.getOrdernumber());
			QuestionOptionsListView listView = new QuestionOptionsListView(question, optionsList, index+".",null);
			rightContent.addComponent(listView);
			rightContent.addComponent(new Label("<hr>",Label.CONTENT_XHTML));
		}
}
