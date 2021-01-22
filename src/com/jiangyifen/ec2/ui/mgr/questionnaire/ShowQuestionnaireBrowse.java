package com.jiangyifen.ec2.ui.mgr.questionnaire;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.entity.Question;
import com.jiangyifen.ec2.entity.QuestionOptions;
import com.jiangyifen.ec2.entity.Questionnaire;
import com.jiangyifen.ec2.service.eaoservice.QuestionService;
import com.jiangyifen.ec2.service.eaoservice.QuestionnaireService;
import com.jiangyifen.ec2.ui.mgr.questionnaire.pojo.BusinessValidate;
import com.jiangyifen.ec2.ui.mgr.questionnaire.pojo.QuestionOptionsFormValue;
import com.jiangyifen.ec2.ui.mgr.questionnaire.pojo.ui.QuestionOptionsListView;
import com.jiangyifen.ec2.ui.mgr.tabsheet.QuestionnaireManagement;
import com.jiangyifen.ec2.ui.mgr.util.StyleConfig;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class ShowQuestionnaireBrowse  extends Window implements ClickListener{
		
		//静态
		private static final long serialVersionUID = 223952958705792239L;
		private final Logger logger = LoggerFactory.getLogger(this.getClass());		// 日志工具
		
		//持有UI对象
		private QuestionnaireManagement questionnaireManagement;
 		//本控件持有对象
		private Questionnaire questionnaire;
		private Question question;
 		
		//页面布局组件
		private	VerticalLayout optionsContent;
		
		//页面控件
		private Label lb_msg;
		
  		private Button bt_next;
		private Button bt_do_over;
		private Button bt_excute;
		private Button bt_cancel;
		private Button bt_excute_save;
		
		private QuestionOptionsListView listView;
		//业务必须对象
		
		//业务本页对象
 
		//业务注入对象
		private QuestionnaireService questionnaireService;
		private QuestionService questionService;
		
		public ShowQuestionnaireBrowse(QuestionnaireManagement questionnaireManagement){
			this.questionnaireManagement = questionnaireManagement;
			initCompanent();
		}
		
		private void initCompanent(){
			this.center();
			this.setModal(true);
			this.setResizable(false);
			this.setCaption("欢迎你的到来!");
			this.setSizeFull();
			this.setWidth("500px");
			this.setHeight("380px");
			initSpringContext();
			VerticalLayout 		windowContent 		= 	createWindowContentComponents();	//竖直布局
								optionsContent 		= 	createOptionsContentComponents();	//竖直布局
			HorizontalLayout 	toolLayout	 		= 	createToolLayoutComponents();		//工具栏
			windowContent.addComponent(optionsContent);
			windowContent.addComponent(toolLayout);
			windowContent.setSizeFull();
			createMsg();
			windowContent.addComponent(lb_msg);
			
			this.addComponent(windowContent);
		}
		
		private Label createMsg() {
			lb_msg = new Label("");
			lb_msg.setWidth("-1px");
			lb_msg.setContentMode(Label.CONTENT_XHTML);
			return lb_msg;
		}

		 
		public void refreshComponentInfo(Questionnaire questionnaire) {
			this.lb_msg.setValue("");
			if(null != questionnaire){
				this.bt_next.setEnabled(true);
				this.questionnaire = questionnaire;
				question  =  questionService.getFirstQuestionByQuestionnaireId(questionnaire.getId());
 	 			updateQuestionnaireUI();
	 			updateQuestionOptionsUI();
	 			updateButtons();
			}else{
				this.bt_next.setEnabled(false);
 				showWindowMsgInfo("数据初始化异常，请重试!"); 
			}
		}

		private void updateButtons() {
			this.bt_do_over.setVisible(true);
			this.bt_next.setVisible(true);
			this.bt_excute.setVisible(true);
			this.bt_cancel.setVisible(true);
			this.bt_excute_save.setVisible(false);
		}

		private void enableNextButton(){
			this.bt_next.setEnabled(false);
		}
		
		 
		private void initSpringContext(){
			//初始化spring相关属性
			questionnaireService = SpringContextHolder.getBean("questionnaireService");
			questionService = SpringContextHolder.getBean("questionService");
		}

		private VerticalLayout createWindowContentComponents() {
			VerticalLayout windowContent_rt = new VerticalLayout();
			windowContent_rt.setSizeUndefined();
			windowContent_rt.setMargin(false, true, true, false);
			windowContent_rt.setSpacing(true);
			windowContent_rt.setStyleName(StyleConfig.VERTICAL_STYLE);
			return windowContent_rt;
		}
		
		private HorizontalLayout createToolLayoutComponents() {
			HorizontalLayout toolLayout_rt = new HorizontalLayout();
			toolLayout_rt.setSizeUndefined();
			toolLayout_rt.setMargin(false, true, true, false);
			toolLayout_rt.setSpacing(true);
			
			bt_do_over = new Button("重新开始",this);
			bt_do_over.setStyleName("default");
	 		toolLayout_rt.addComponent(bt_do_over);
	 		
			bt_next = new Button("下一题",this);
			bt_next.setStyleName("default");
			toolLayout_rt.addComponent(bt_next);
			
	 		bt_excute = new Button("执行问卷",this);
	 		bt_excute.setStyleName("default");
	 		toolLayout_rt.addComponent(bt_excute);
	 		 
	 		bt_excute_save = new Button("确定执行",this);
	 		bt_excute_save.setStyleName("default");
	 		toolLayout_rt.addComponent(bt_excute_save);
	 		
	 		bt_cancel = new Button("关闭窗口",this);
	 		toolLayout_rt.addComponent(bt_cancel);
	 		
	 		 
			toolLayout_rt.setSpacing(true);
			return toolLayout_rt;
		}
		
		private VerticalLayout createOptionsContentComponents(){
			VerticalLayout optionsContent_rt = new VerticalLayout();
			optionsContent_rt.setSizeFull();
			return optionsContent_rt;
		}
		
		private void updateQuestionOptionsUI() {
			if (null != question) {
				List<QuestionOptions> optionsList = questionService.getQuestionOptionsListByQuestionId(question.getId());
				int index = questionService.getIndexByQuestionnaireAndQuestionOrder(questionnaire.getId(),question.getOrdernumber());
				listView = new QuestionOptionsListView(question, optionsList, index+".",null);//null 是不填充组件选项信息
				optionsContent.removeAllComponents();
				optionsContent.addComponent(listView);
			} else {
				optionsContent.removeAllComponents();
				this.lb_msg.setValue("<font color='red'>抱歉，未找到问题，请添加问题!</font>");
				enableNextButton();
			}
		}

		private void updateQuestionnaireUI(){
			String tem_MainTitle = questionnaire.getMainTitle();
			this.setCaption("问卷:: "+tem_MainTitle);
		}
 
		@Override
		public void buttonClick(ClickEvent event) {
			Button source = event.getButton();
			if(source == bt_next){
				BusinessValidate businessValidate = listView.validateFormValue();
				if(businessValidate.isBtf()){//校验成功
					lb_msg.setValue("");
					List<QuestionOptionsFormValue> formValues = listView.getFormValue();
					
					nextOrSkipQuestionUpdate(formValues);
					 
				}else{//校验未通过
					lb_msg.setValue("<font color='red'>"+businessValidate.getBmsg()+"!</font>");
				}
			}
			else if(source == bt_do_over){//重做
				question  =  questionService.getFirstQuestionByQuestionnaireId(questionnaire.getId());
				if(null != questionnaire){
 					question  =  questionService.getFirstQuestionByQuestionnaireId(questionnaire.getId());
	 	 			updateQuestionnaireUI();
		 			updateQuestionOptionsUI();
		 			updateButtons();
				}else{
					this.bt_next.setEnabled(false);
	 				showWindowMsgInfo("数据初始化异常，请重试!"); 
				}
			}
			else if(source == bt_excute){//执行
				this.bt_do_over.setVisible(false);
				this.bt_next.setVisible(false);
				this.bt_excute.setVisible(false);
				this.bt_cancel.setVisible(true);
				this.bt_excute_save.setVisible(true);
				optionsContent.removeAllComponents();
				optionsContent.addComponent(new Label("<font color='red'>在执行状态下的问卷不能修改，您确定要执行问卷吗？</font>",Label.CONTENT_XHTML));
 			}
			else if(source == bt_excute_save){//确认执行
				stateCodeQuestionnaire();
				questionnaireManagement.updateTable(false);
 				this.getApplication().getMainWindow().removeWindow(this);
 			}
			else if(source == bt_cancel){//取消按钮
				this.getApplication().getMainWindow().removeWindow(this);
			}
		}

		/** 下一题 */
		private void nextOrSkipQuestionUpdate(List<QuestionOptionsFormValue> formValues) {
			Question newQuestion;
			Question skipQuestion = null;
			if((null != formValues)&&(formValues.size() > 0)){
				for (QuestionOptionsFormValue questionOptionsFormValue : formValues) {
					QuestionOptions questionOptions = questionOptionsFormValue.getOptions();
					if((null != questionOptions)&&(null != questionOptions.getSkip())){
						skipQuestion = questionOptions.getSkip();
						break;
					}
				}
			}else{
  	 			bt_next.setVisible(false);
			}
			if(null!=skipQuestion){
				newQuestion = skipQuestion;
			}else{
				newQuestion = questionService.getNextQuestionByOrdernumber(question);
			}
			if (null != newQuestion) {
				question = newQuestion;
				clearQuestionUI();
	 			updateQuestionOptionsUI();
			}else{//完成问卷
				clearQuestionUI();
				lb_msg.setValue("<font color='red'>问卷已经完成,谢谢!</font>");
	 			bt_next.setVisible(false);
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
		
		/**清空问题选项模块*/
		private void clearQuestionUI(){
			if(null != listView){
				try {
					optionsContent.removeComponent(listView);
				} catch (Exception e) {
					e.printStackTrace();//TODO lxy  需要处理
				}
			}
		}
		/**显示*/
		private void showWindowMsgInfo(String msg){
			this.getApplication().getMainWindow().showNotification(msg, Notification.TYPE_WARNING_MESSAGE);
		}

		public Questionnaire getQuestionnaire() {
			return questionnaire;
		}

		public void setQuestionnaire(Questionnaire questionnaire) {
			this.questionnaire = questionnaire;
		}
		
		public void cliseSelf(){
			this.getApplication().getMainWindow().removeWindow(this);
		}
		
}
