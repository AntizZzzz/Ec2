package com.jiangyifen.ec2.ui.mgr.questionnaire;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.entity.CustomerQuestionnaire;
import com.jiangyifen.ec2.entity.Question;
import com.jiangyifen.ec2.entity.QuestionOptions;
import com.jiangyifen.ec2.entity.Questionnaire;
import com.jiangyifen.ec2.entity.RecordFile;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.service.eaoservice.QuestionService;
import com.jiangyifen.ec2.ui.mgr.questionnaire.pojo.OptionsListViewInfo;
import com.jiangyifen.ec2.ui.mgr.questionnaire.pojo.ui.QuestionOptionsListView;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.terminal.gwt.server.WebApplicationContext;
import com.vaadin.terminal.gwt.server.WebBrowser;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class StartDoQuestionnaireShowAll extends Window  implements ClickListener{
	
	//静态
	private static final long serialVersionUID = -6939714061686048094L;
	private final Logger logger = LoggerFactory.getLogger(this.getClass()); // 日志工具

	// 持有UI对象
	
	// 本控件持有对象
	//private Questionnaire questionnaire;
	 

	// 页面布局组件
	private HorizontalLayout contentLayout;
	private VerticalLayout leftContent;
	private VerticalLayout rightContent;
	private HorizontalLayout optLayout;
	private HorizontalLayout buttonLayout;
	private HorizontalLayout panelLayout;
	// 页面控件
	private Label lb_msg;

	private Button bt_save_question;
	private Button bt_cancel;
	private OptionGroup og_success;

	private Panel pl_note;
	
	private List<QuestionOptionsListView> listViews = new ArrayList<QuestionOptionsListView>();
	
	// 业务必须对象
	
	
	
	// 业务本页对象
	private Long customerid;
	private User loginUser;
	private RecordFile recordFile;
	private int doType;
	private CustomerQuestionnaire customerQuestionnaire;
	private Questionnaire questionnaire;
	
	// 业务注入对象
	private QuestionService questionService;
	
	
	public StartDoQuestionnaireShowAll(User loginUser){
		this.center();
		this.setModal(false);
		this.setResizable(false);
		this.loginUser = loginUser;
		
		initlayout();
		initSpringContext();
	}

	@Override
	public void attach() {
		initWindowSize();
	}
	
	private void initlayout(){
 		buildContentLayout();
 		buildOptLayout();
		buildBottomLayout();	
		buildPanelLayout();
		
		this.addComponent(contentLayout);
		this.addComponent(optLayout);
		this.addComponent(new Label(" ",Label.CONTENT_XHTML));
		this.addComponent(buttonLayout);
		this.addComponent(panelLayout);	
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
		
		og_success = new OptionGroup();
		og_success.setMultiSelect(true);
		og_success.addItem("end");
		og_success.setHtmlContentAllowed(true);
		og_success.setItemCaption("end", "<b><font color='#F47920'>是成功问卷</font></b>");
		optLayout.addComponent(og_success);
		
	}
	
	private void buildBottomLayout(){
		buttonLayout = new HorizontalLayout();
		buttonLayout.setSpacing(true);
		buttonLayout.setMargin(true,true,true,true);
		
		bt_save_question = new Button("保存问卷",this);
		bt_save_question.setStyleName("default");
		buttonLayout.addComponent(bt_save_question);
		
		bt_cancel = new Button("关闭",this);
		buttonLayout.addComponent(bt_cancel);
	}
	
	private void buildPanelLayout(){
		panelLayout = new HorizontalLayout();
	}
	
	private void initWindowSize() {//多线程下Session数据，
		WebApplicationContext context = (WebApplicationContext) this.getApplication().getContext();
		WebBrowser webBrowser = context.getBrowser();
		int width = webBrowser.getScreenWidth();
		int height = webBrowser.getScreenHeight();
		if(width >= 1366) {
			this.setWidth("1190px");
		} else {
			this.setWidth("940px");
		}

		if(height > 768) {
			this.setHeight("630px");
		} else if(height == 768) {
			this.setHeight("520px");
		} else {
			this.setHeight("480px");
		}
	}

	/**初始化Spring上写相关系想你 */
	private void initSpringContext(){
// 		loginUser = SpringContextHolder.getLoginUser();
 		questionService = SpringContextHolder.getBean("questionService");
	}
	@Override
	@SuppressWarnings("unchecked")
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if(source == bt_save_question) {
			try {
				String finish = "start";
				Set<String> sets = (Set<String>) og_success.getValue();
				if(sets != null){
					if(sets.contains("end")){
						finish = "end";
					}
				}
				customerQuestionnaire.setFinish(finish);
				questionService.updateCustomerQuestionnaireOnly(customerQuestionnaire);
				
				if(1 == doType){//新开始一次
					questionService.saveCustomerQuestionnaireAndChannelShowAll(customerQuestionnaire, recordFile,listViews);	
				}else if(2 == doType){//第二次从新做
					questionService.saveCustomerQuestionnaireAndChannelShowAll(customerQuestionnaire, recordFile,listViews);	
				}else if(3 == doType){//第二次接上次做
					questionService.saveCustomerQuestionnaireAndChannelShowAllClearOptions(customerQuestionnaire, recordFile,listViews);	
				}
				 showWindowMsgInfo("问卷保存成功");
				 this.getApplication().getMainWindow().removeWindow(this);
			} catch (Exception e) {
				e.printStackTrace();
				logger.error("问卷保存成功_buttonClick_LLXXYY", e);
				getApplication()
						.getMainWindow()
						.showNotification("问卷保存失败!",
								Notification.TYPE_ERROR_MESSAGE);
			}
		 
		} else if(source == bt_cancel){
			this.getApplication().getMainWindow().removeWindow(this);
		}
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
		this.doType = doType;
		this.questionnaire = questionnaire;
		this.customerid = customerid;
		try {
			if(initLoadInfo(recordFilePath)){
				updateQuestionnaireUI();
	 			updateQuestionOptionsUI();
	 			 
			}else{
				showWindowMsgInfo("数据初始化异常，请重试!"); 
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void updateQuestionnaireUI() {
		String finish = customerQuestionnaire.getFinish();
		if("end".equals(finish)){
			Set<String> set = new HashSet<String>();
			set.add("end");
			og_success.setValue(set);
		}
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

	/** 保存问卷 客户 CSR等关联信息*/
	private boolean initLoadInfo(String recordFilePath){
		if(null != questionnaire){
			// 创建录音文件对象
			recordFile = new RecordFile();
			recordFile.setDomain(loginUser.getDomain());
			recordFile.setCreateTime(new Date());
			recordFile.setCustomerId(customerid);
			recordFile.setUserId(loginUser.getId());
			recordFile.setDescription("做问卷的呼叫记录的录音文件");
			recordFile.setDownloadPath(recordFilePath);
			try {
				if(1 == doType){//  1:第一次做，2：第二次从新做，3：第二次接上次做
					//1.读取该问卷的第一个问题
					//2.保存客户问卷关系  保存客户问卷-通道语音关系
					customerQuestionnaire = new CustomerQuestionnaire();
					customerQuestionnaire.setCustomerid(customerid);
					customerQuestionnaire.setQuestionnaire(questionnaire);
					customerQuestionnaire.setUserid(loginUser.getId());
					questionService.saveCustomerQuestionnaireAndChannel(customerQuestionnaire, recordFile);//2.保存客户问卷关系  保存客户问卷-通道语音关系
					return true;
				}else if(2 == doType){//2：第二次从新做
					//1.删除客户问卷-通道语音关系(待定)。应该是逻辑删除，以防止以后查询历史记录
					//2.删除客户问卷，客户问卷选项
					//3.读取该问卷的第一个问题
					//4.保存客户问卷关系 保存客户问卷-通道语音关系
					Set<RecordFile> recordFiles = questionService.deleteCustomerQuestionnaireAndChannel(questionnaire,customerid,loginUser);
					customerQuestionnaire = new CustomerQuestionnaire();
					customerQuestionnaire.setCustomerid(customerid);
					customerQuestionnaire.setQuestionnaire(questionnaire);
					customerQuestionnaire.setUserid(loginUser.getId());
					customerQuestionnaire.setRecordFiles(recordFiles);
					questionService.saveCustomerQuestionnaireAndChannel(customerQuestionnaire, recordFile); //2.保存客户问卷关系  保存客户问卷-通道语音关系
					return true; 
					
				}else if(3 == doType){//3：第二次接上次做
					//读取该问卷的接上一次的问题
					//获得该客户问卷关系对象
					customerQuestionnaire  =  questionService.getCustomerQuestionnaireByCustomeridQuestionnaireUserid(questionnaire,customerid,loginUser);
					questionService.updateCustomerQuestionnaireAndChannel(customerQuestionnaire, recordFile);
					if(null != customerQuestionnaire){
						return true;
					}else{
						//没有客户问卷关系回到类型 doType = 1 异常
						lb_msg.setValue("<font color='red'>没有发现没有客户问卷，数据异常!</font>");
						return false;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				logger.error("保存问卷 客户 CSR等关联信息异常_initLoadInfo_LLXXYY", e);
				this.getApplication()
						.getMainWindow()
						.showNotification("保存问卷 客户 CSR等关联信息异常!",
								Notification.TYPE_ERROR_MESSAGE);
				lb_msg.setValue("<font color='red'>保存问卷客户关联信息异常，数据异常!</font>");
				return false;
			}
			
			 
		}else{
			return false;
		}
		return true;
	}
	
	
	private void updateQuestionOptionsUI(){
		if(null != questionnaire){
			this.setCaption(questionnaire.getMainTitle());
			List<Question> questionList =  questionService.getQuestionListByQuestionnaireId(questionnaire.getId());
			leftContent.removeAllComponents();
			rightContent.removeAllComponents();
			listViews.clear();
			int quesCount = questionList.size();
			int avg = (quesCount+1)/2;
			
			for (int i = 0; i < questionList.size(); i++) {
				if(i < avg){
					buildLeftContentQuestionUI(questionList.get(i));
				}else{
					buildRightContentQuestionUI(questionList.get(i));
				}
			}
		}else{
			// TODO 没有问卷
		}
	}
	 
	
	private void  buildLeftContentQuestionUI(Question question){
		List<QuestionOptions> optionsList = questionService.getQuestionOptionsListByQuestionId(question.getId());
		int index = questionService.getIndexByQuestionnaireAndQuestionOrder(questionnaire.getId(),question.getOrdernumber());
		OptionsListViewInfo optionsListViewInfo = null;
		if(1 == doType){//新开始一次
			
		}else if(2 == doType){//第二次从新做
			
		}else if(3 == doType){//第二次接上次做
			optionsListViewInfo = new OptionsListViewInfo();
			optionsListViewInfo.setInitCode(1);
			optionsListViewInfo.setCustomerQuestionnaire(customerQuestionnaire);
		}
		QuestionOptionsListView listView = new QuestionOptionsListView(question, optionsList, index+".",optionsListViewInfo);
		leftContent.addComponent(listView);
		leftContent.addComponent(new Label("<hr>",Label.CONTENT_XHTML));
		listViews.add(listView);
	}
	
	private void  buildRightContentQuestionUI(Question question){
		List<QuestionOptions> optionsList = questionService.getQuestionOptionsListByQuestionId(question.getId());
		int index = questionService.getIndexByQuestionnaireAndQuestionOrder(questionnaire.getId(),question.getOrdernumber());
		OptionsListViewInfo optionsListViewInfo = null;
		if(1 == doType){//新开始一次
			
		}else if(2 == doType){//第二次从新做
			
		}else if(3 == doType){//第二次接上次做
			optionsListViewInfo = new OptionsListViewInfo();
			optionsListViewInfo.setInitCode(1);
			optionsListViewInfo.setCustomerQuestionnaire(customerQuestionnaire);
		}
		QuestionOptionsListView listView = new QuestionOptionsListView(question, optionsList, index+".",optionsListViewInfo);
		rightContent.addComponent(listView);
		rightContent.addComponent(new Label("<hr>",Label.CONTENT_XHTML));
		listViews.add(listView);
	}
	/** 显示  msg */
	private void showWindowMsgInfo(String msg){
		this.getApplication().getMainWindow().showNotification(msg, Notification.TYPE_WARNING_MESSAGE);
	}
}
