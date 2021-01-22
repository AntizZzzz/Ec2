package com.jiangyifen.ec2.ui.mgr.questionnaire;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.entity.CustomerQuestionOptions;
import com.jiangyifen.ec2.entity.CustomerQuestionnaire;
import com.jiangyifen.ec2.entity.Question;
import com.jiangyifen.ec2.entity.QuestionOptions;
import com.jiangyifen.ec2.entity.Questionnaire;
import com.jiangyifen.ec2.entity.RecordFile;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.service.eaoservice.QuestionService;
import com.jiangyifen.ec2.service.eaoservice.QuestionnaireService;
import com.jiangyifen.ec2.ui.mgr.questionnaire.pojo.BusinessValidate;
import com.jiangyifen.ec2.ui.mgr.questionnaire.pojo.QuestionOptionsFormValue;
import com.jiangyifen.ec2.ui.mgr.questionnaire.pojo.ui.QuestionOptionsListView;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.Notification;

public class StartDoQuestionnaire extends VerticalLayout implements ClickListener{
		
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
  		
 		private QuestionOptionsListView listView;
		//业务必须对象 
		
		//业务本页对象
		private Long customerid;
		private User loginUser;
		//业务注入对象
		private QuestionnaireService questionnaireService;
		private QuestionService questionService;
		
		public StartDoQuestionnaire(User loginUser){
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
		
		/**
		 * 刷新控件，被调查使用的问卷，客户编号 问题显示类型， 通道和语音
		 * @param questionnaire 被调查使用的问卷
		 * @param customerid	客户编号
		 * @param doType  <br>1：第一次做，<br>2：第二次重新做，<br>3：第二次接上次做
		 * @param recordFilePath 录音文件存储路径
		 * 
		 */
		public void refreshComponentInfo(Questionnaire questionnaire, Long customerid,int doType,String recordFilePath) {
			this.questionnaire = questionnaire;
			this.customerid = customerid;
			try {
				lb_msg.setValue("");
				if(initLoadInfo(doType,recordFilePath)){
		 			updateQuestionOptionsUI();
		 			bt_next.setVisible(true);
				}else{
					bt_next.setEnabled(false);
					showWindowMsgInfo("数据初始化异常，请重试!"); 
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		/** 保存问卷 客户 CSR等关联信息*/
		private boolean initLoadInfo(int doType,String recordFilePath){
			if(null != questionnaire){
				// 创建录音文件对象
				RecordFile recordFile = new RecordFile();
				recordFile.setDomain(loginUser.getDomain());
				recordFile.setCreateTime(new Date());
				recordFile.setCustomerId(customerid);
				recordFile.setUserId(loginUser.getId());
				recordFile.setDescription("做问卷的呼叫记录的录音文件");
				recordFile.setDownloadPath(recordFilePath);

				if(1 == doType){//  1:第一次做，2：第二次从新做，3：第二次接上次做
					//1.读取该问卷的第一个问题
					//2.保存客户问卷关系  保存客户问卷-通道语音关系
					question  =  questionService.getFirstQuestionByQuestionnaireId(questionnaire.getId());	//1.读取该问卷的第一个问题
					if(null != question){
						customerQuestionnaire = new CustomerQuestionnaire();
						customerQuestionnaire.setCustomerid(customerid);
						customerQuestionnaire.setQuestionnaire(questionnaire);
						customerQuestionnaire.setUserid(loginUser.getId());
						
						questionService.saveCustomerQuestionnaireAndChannel(customerQuestionnaire, recordFile);//2.保存客户问卷关系  保存客户问卷-通道语音关系
						return true;
					}else{
						return false;
					}
				}else if(2 == doType){//2：第二次从新做
					//1.删除客户问卷-通道语音关系(待定)。应该是逻辑删除，以防止以后查询历史记录
					//2.删除客户问卷，客户问卷选项
					//3.读取该问卷的第一个问题
					//4.保存客户问卷关系 保存客户问卷-通道语音关系
					 Set<RecordFile> recordFiles = questionService.deleteCustomerQuestionnaireAndChannel(questionnaire,customerid,loginUser);
					 question  =  questionService.getFirstQuestionByQuestionnaireId(questionnaire.getId());	//1.读取该问卷的第一个问题
						if(null != question){
							customerQuestionnaire = new CustomerQuestionnaire();
							customerQuestionnaire.setCustomerid(customerid);
							customerQuestionnaire.setQuestionnaire(questionnaire);
							customerQuestionnaire.setUserid(loginUser.getId());
							customerQuestionnaire.setRecordFiles(recordFiles);
//							TODO
							questionService.saveCustomerQuestionnaireAndChannel(customerQuestionnaire, recordFile); //2.保存客户问卷关系  保存客户问卷-通道语音关系
							return true;
						}else{
							return false;
						}
					
				}else if(3 == doType){//3：第二次接上次做
					//读取该问卷的接上一次的问题
					//获得该客户问卷关系对象
					//TODO 未做
					customerQuestionnaire  =  questionService.getCustomerQuestionnaireByCustomeridQuestionnaireUserid(questionnaire,customerid,loginUser);
					questionService.updateCustomerQuestionnaireAndChannel(customerQuestionnaire, recordFile);
					if(null != customerQuestionnaire){
						question  =  questionService.getRightQuestion(customerQuestionnaire);	//1.读取该问卷的第一个问题
						if(null == question){
							lb_msg.setValue("<font color='red'>问卷已经完成,谢谢!</font>");
						}
						return true;
					}else{
						//没有客户问卷关系回到类型 doType = 1 异常
						lb_msg.setValue("<font color='red'>没有发现客户问卷，数据异常!</font>");
						return false;
					}
				}
				
				/*question  =  questionService.getFirstQuestionByQuestionnaireId(questionnaire.getId());	//获得该问卷的第一个问题
				if(null != question){
					customerQuestionnaire = new CustomerQuestionnaire();
					customerQuestionnaire.setCustomerid(customerid);
					customerQuestionnaire.setQuestionnaire(questionnaire);
					customerQuestionnaire.setUserid(loginUser.getId());
					questionService.saveCustomerQuestionnaireAndChannel(customerQuestionnaire);
				}else{
					return false;
				}*/
			}else{
				return false;
			}
			return true;
		}

		private void enableNextButton(){
			this.bt_next.setEnabled(false);
		}
		/**初始化spring相关属性*/
		private void initSpringContext(){
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
		
		/**更新问题*/
		private void updateQuestionOptionsUI() {
			if (null != question) {
				buildQuestionUI(question);
			} else {
				enableNextButton();
			}
		}

		//TODO 开始 新添加
		/**根据问题构建问题选项模块*/
		private void  buildQuestionUI(Question question){
			List<QuestionOptions> optionsList = questionService.getQuestionOptionsListByQuestionId(question.getId());
			int index = questionService.getIndexByQuestionnaireAndQuestionOrder(questionnaire.getId(),question.getOrdernumber());
			listView = new QuestionOptionsListView(question, optionsList, index+".",null);//null 是不填充组件选项信息
			optionsContent.removeAllComponents();
			optionsContent.addComponent(listView);
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
		//TODO 结束 新添加
		
 		@Override
		public void buttonClick(ClickEvent event) {
			Button source = event.getButton();
			if(source == bt_next){
				BusinessValidate businessValidate = listView.validateFormValue();
				if(businessValidate.isBtf()){//校验成功
					lb_msg.setValue("");
					List<QuestionOptionsFormValue> formValues = listView.getFormValue();
					if(excute_saveUserOptions(formValues)){//保存
						nextOrSkipQuestionUpdate(formValues);
					}else{
						showWindowMsgInfo("保存问题选项失败"); 
					}
				}else{//校验未通过
					lb_msg.setValue("<font color='red'>"+businessValidate.getBmsg()+"!</font>");
				}
			}
		}

		/** 保存用户问卷选项 */
		private boolean excute_saveUserOptions(List<QuestionOptionsFormValue> formValues) {
			boolean excute = false;
			try {
				List<CustomerQuestionOptions> uqos = new ArrayList<CustomerQuestionOptions>();
				CustomerQuestionOptions customerQuestionOptions;
				if((null != formValues)&&(formValues.size() > 0)){
					for (QuestionOptionsFormValue questionOptionsFormValue : formValues) {
						customerQuestionOptions = new CustomerQuestionOptions();
						customerQuestionOptions.setQuestion(question);
						customerQuestionOptions.setCustomerQuestionnaire(customerQuestionnaire);
						customerQuestionOptions.setQuestionOptions(questionOptionsFormValue.getOptions());
						customerQuestionOptions.setText(questionOptionsFormValue.getOptionText());
						uqos.add(customerQuestionOptions);
					}
					questionnaireService.saveCustomerQuestionOptions(uqos);
					excute = true;
				}else{
					excute = false;
				}
			} catch (Exception e) {
				e.printStackTrace();
				logger.error("保存用户问卷选项异常_excute_saveUserOptions", e);
			}
			return excute;
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
				updateCustomerQuestionnaire();
				clearQuestionUI();
				lb_msg.setValue("<font color='red'>问卷已经完成,谢谢!</font>");
	 			bt_next.setVisible(false);
			} 
		}
		
		/** 更新用户问卷*/
		private void updateCustomerQuestionnaire() {
			if(null != customerQuestionnaire){
 				questionService.updateCustomerQuestionnaire(customerQuestionnaire);
			}
		}

		/** 显示  msg */
		private void showWindowMsgInfo(String msg){
			this.getApplication().getMainWindow().showNotification(msg, Notification.TYPE_WARNING_MESSAGE);
		}
}
