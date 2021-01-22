package com.jiangyifen.ec2.ui.mgr.questionnaire;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.entity.CustomerQuestionOptions;
import com.jiangyifen.ec2.entity.CustomerQuestionnaire;
import com.jiangyifen.ec2.entity.Question;
import com.jiangyifen.ec2.entity.QuestionOptions;
import com.jiangyifen.ec2.entity.Questionnaire;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.service.eaoservice.QuestionService;
import com.jiangyifen.ec2.service.eaoservice.QuestionnaireService;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.Notification;

public class DoQuestionnaireQuestion extends VerticalLayout implements ClickListener{
		
		//静态
		private static final long serialVersionUID = 223952958705792239L;
		private final Logger logger = LoggerFactory.getLogger(this.getClass());		// 日志工具
		
		//持有UI对象
		//本控件持有对象
		private Questionnaire questionnaire;
		private Question question;
		private CustomerQuestionnaire customerQuestionnaire;
		
		//页面布局组件
		private	VerticalLayout optionsContent;
		
		//页面控件
		private Label lb_msg;
		
  		private Button bt_next;
		private OptionGroup questionSelect ;
		//业务必须对象 
		
		//业务本页对象
		private Long customerid;
		private User loginUser;
		//业务注入对象
		private QuestionnaireService questionnaireService;
		private QuestionService questionService;
		
		public DoQuestionnaireQuestion(User loginUser){
			this.loginUser = loginUser;
			
			initCompanent();
		}
		
		private void initCompanent(){
			this.setSpacing(true);
			this.setWidth("100%");
			initSpringContext();
								optionsContent 		= 	createOptionsContentComponents();	//竖直布局
			HorizontalLayout 	toolLayout	 		= 	createToolLayoutComponents();		//工具栏
			this.addComponent(optionsContent);
			this.addComponent(toolLayout);
			createMsg();
			this.addComponent(lb_msg);
		}
		
		private Label createMsg() {
			lb_msg = new Label("");
			lb_msg.setWidth("-1px");
			lb_msg.setContentMode(Label.CONTENT_XHTML);
			return lb_msg;
		}
		
		public void refreshComponentInfo(Questionnaire questionnaire, Long customerid) {
			this.questionnaire = questionnaire;
			this.customerid = customerid;
			
			if(initLoadInfo()){
	 			updateQuestionOptionsUI();
	 			bt_next.setVisible(true);
			}else{
				this.bt_next.setEnabled(false);
				//保存失败
				showWindowMsgInfo("数据初始化异常，请重试!"); 
			}
		}
		
		private boolean initLoadInfo(){
			if(null != questionnaire){
				question  =  questionService.getFirstQuestionByQuestionnaireId(questionnaire.getId());
				if(null != question){
					customerQuestionnaire = new CustomerQuestionnaire();
					customerQuestionnaire.setCustomerid(customerid);
					customerQuestionnaire.setQuestionnaire(questionnaire);
					customerQuestionnaire.setUserid(loginUser.getId());
					questionService.saveCustomerQuestionnaire(customerQuestionnaire);
				}else{
					return false;
				}
			}else{
				return false;
			}
			return true;
		}

		private void enableNextButton(){
			this.bt_next.setEnabled(false);
		}
		
		 
		private void initSpringContext(){
			//初始化spring相关属性
			questionnaireService = SpringContextHolder.getBean("questionnaireService");
			questionService = SpringContextHolder.getBean("questionService");
		}
		
		private HorizontalLayout createToolLayoutComponents() {
			HorizontalLayout toolLayout_rt = new HorizontalLayout();
			toolLayout_rt.setSizeUndefined();
			toolLayout_rt.setMargin(false, true, true, false);
			toolLayout_rt.setSpacing(true);
			
			bt_next = new Button("下一题",this);
			bt_next.setStyleName("default");
			bt_next.setVisible(false);
			toolLayout_rt.addComponent(bt_next);
			
			return toolLayout_rt;
		}
		
		private VerticalLayout createOptionsContentComponents(){
			VerticalLayout optionsContent_rt = new VerticalLayout();
			
			return optionsContent_rt;
		}
		
		private void updateQuestionOptionsUI() {
			if (null != question) {
				List<QuestionOptions> optionsList = questionService.getQuestionOptionsListByQuestionId(question.getId());
				BeanItemContainer<QuestionOptions> beanItemContainer = new BeanItemContainer<QuestionOptions>(QuestionOptions.class);
				beanItemContainer.addAll(optionsList);
				questionSelect = new OptionGroup(question.getTitle(),beanItemContainer);
				if("checkbox".equals(question.getQuestionType().getValue())){
					questionSelect.setMultiSelect(true);
				}

				questionSelect.setNullSelectionAllowed(false);
				questionSelect.setItemCaptionPropertyId("name");
				questionSelect.setImmediate(true);
				questionSelect.setWidth("100%");
				optionsContent.setWidth("100%");
				optionsContent.removeAllComponents();
				optionsContent.addComponent(questionSelect);
			} else {
				enableNextButton();
			}
		}

		@SuppressWarnings("unchecked")
		@Override
		public void buttonClick(ClickEvent event) {
			Button source = event.getButton();
			if(source == bt_next){
				Set<QuestionOptions> questionOptionsSet =   new HashSet<QuestionOptions>();
				if("checkbox".equals(question.getQuestionType().getValue())){
					Object selectValue = questionSelect.getValue();
					questionOptionsSet = (Set<QuestionOptions>) selectValue;
				}else{
					Object selectValue = questionSelect.getValue();
					QuestionOptions questionOptions = (QuestionOptions)selectValue;
					if(null!=questionOptions){
						questionOptionsSet.add(questionOptions);
					}
 				}
 				if(questionOptionsSet.size()<1){
					lb_msg.setValue("<font color='red'>请您选择!</font>");
				}else{
					lb_msg.setValue("");
					if(excute_saveUserOptions(questionOptionsSet)){//保存
						nextOrSkipQuestionUpdate(questionOptionsSet);
					}else{
						showWindowMsgInfo("保存问题选项失败"); 
					}
				}
			}
		}

		private boolean excute_saveUserOptions(Set<QuestionOptions> questionOptions) {
			boolean excute = false;
			try {
				List<CustomerQuestionOptions> uqos = new ArrayList<CustomerQuestionOptions>();
				CustomerQuestionOptions customerQuestionOptions;
	
				Iterator<QuestionOptions> it = questionOptions.iterator();
				while (it.hasNext()) {
					QuestionOptions o = it.next();
					customerQuestionOptions = new CustomerQuestionOptions();
					customerQuestionOptions.setQuestion(question);
					customerQuestionOptions.setCustomerQuestionnaire(customerQuestionnaire);
					customerQuestionOptions.setQuestionOptions(o);
					uqos.add(customerQuestionOptions);
				}
				questionnaireService.saveCustomerQuestionOptions(uqos);
				excute = true;
			} catch (Exception e) {
				e.printStackTrace();
				logger.error("保存用户问卷选项异常_excute_saveUserOptions", e);
			}
			return excute;
		}

		private void nextOrSkipQuestionUpdate(Set<QuestionOptions> questionOptions) {
			Question new_question;
			Question skip = null;
			
			Iterator<QuestionOptions> it = questionOptions.iterator();
			while (it.hasNext()) {
				QuestionOptions o = it.next();
				if(null!=o && null != o.getSkip()){
					skip = o.getSkip();
					break;
				}
			}
		 
			if(null!=skip){
				new_question = skip;
			}else{
				new_question = questionService.getNextQuestionByOrdernumber(question);
			}
			if(null!=new_question){
				question = new_question;
	 			updateQuestionOptionsUI();
			}else{
				updateCustomerQuestionnaire();
 				optionsContent.removeAllComponents();
 				String caption = "<font color='red'>"+questionnaire.getMainTitle()+"</font><br/>";
				optionsContent.addComponent(new Label(caption+"问卷结束，谢谢!", Label.CONTENT_XHTML));
	 			bt_next.setVisible(false);
			} 
		}
		
		private void updateCustomerQuestionnaire() {
			if(null != customerQuestionnaire){
 				questionService.updateCustomerQuestionnaire(customerQuestionnaire);
			}
		}

		/**
		 * 显示
		 * @param msg
		 */
		private void showWindowMsgInfo(String msg){
			this.getApplication().getMainWindow().showNotification(msg, Notification.TYPE_WARNING_MESSAGE);
		}

		public Questionnaire getQuestionnaire() {
			return questionnaire;
		}

		public void setQuestionnaire(Questionnaire questionnaire) {
			this.questionnaire = questionnaire;
		}
		
}
