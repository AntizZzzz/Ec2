package com.jiangyifen.ec2.ui.mgr.questionnaire;

import java.util.ArrayList;
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
import com.jiangyifen.ec2.service.eaoservice.CustomerQuestionnaireService;
import com.jiangyifen.ec2.service.eaoservice.QuestionService;
import com.jiangyifen.ec2.ui.mgr.questionnaire.pojo.OptionsListViewInfo;
import com.jiangyifen.ec2.ui.mgr.questionnaire.pojo.ui.QuestionOptionsListView;
import com.jiangyifen.ec2.ui.mgr.questionnaire.utils.WorkUIUtils;
import com.jiangyifen.ec2.ui.mgr.tabsheet.QuestionnaireManagementEdit;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class EditCustomerQuestionnaire extends Window  implements ClickListener{
	
	//静态
	private static final long serialVersionUID = -6939714061686048094L;
	private final Logger logger = LoggerFactory.getLogger(this.getClass()); // 日志工具

	// 持有UI对象
	private QuestionnaireManagementEdit questionnaireManagementEdit;
	// 本控件持有对象
	//private Questionnaire questionnaire;
	 

	// 页面布局组件
	private HorizontalLayout contentLayout;
	private VerticalLayout leftContent;
	private VerticalLayout rightContent;
	private HorizontalLayout optLayout;
	private HorizontalLayout buttonLayout;
	
	// 页面控件
	private Label lb_msg;

	private Button bt_save_question;
	private Button bt_cancel;
	private OptionGroup og_success;
	
	private List<QuestionOptionsListView> listViews = new ArrayList<QuestionOptionsListView>();
	
	// 业务必须对象
	
	
	
	// 业务本页对象
	private Integer[] screenResolution;							// 屏幕分辨率
	private RecordFile recordFile;
	private CustomerQuestionnaire customerQuestionnaire;
	private Questionnaire questionnaire;
	
	// 业务注入对象
	private QuestionService questionService;
	private CustomerQuestionnaireService customerQuestionnaireService;
	
	public EditCustomerQuestionnaire(QuestionnaireManagementEdit questionnaireManagementEdit){
		this.center();
		this.setModal(false);
		this.setResizable(false);
		this.questionnaireManagementEdit = questionnaireManagementEdit;
		initWindowSize();
		initlayout();
		initSpringContext();
	}

	private void initlayout(){
 		buildContentLayout();
 		buildOptLayout();
		buildBottomLayout();	 
		this.addComponent(contentLayout);
		this.addComponent(optLayout);
		this.addComponent(new Label(" ",Label.CONTENT_XHTML));
		this.addComponent(buttonLayout);
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
 		customerQuestionnaireService = SpringContextHolder.getBean("customerQuestionnaireService");
 		
	}
	
	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if(source == bt_save_question) {
			excuteEditQuestion();
			questionnaireManagementEdit.updateTable(false);
			this.getApplication().getMainWindow().removeWindow(this);
		} else if(source == bt_cancel){
			this.getApplication().getMainWindow().removeWindow(this);
		}
	}

	@SuppressWarnings("unchecked")
	private void excuteEditQuestion(){
		try {
			String finish = "start";
			Set<String> sets = (Set<String>) og_success.getValue();
			if(sets != null){
				if(sets.contains("end")){
					finish = "end";
				}
			}
			customerQuestionnaire.setFinish(finish);
			questionService.updateCustomerQuestionnaireOnlyFinish(customerQuestionnaire);
			questionService.saveCustomerQuestionnaireAndChannelShowAllClearOptions(customerQuestionnaire, recordFile,listViews);
			showWindowMsgInfo("修改成功");
			
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("保存失败_excuteEditQuestion_LLXXYY", e);
			this.getApplication()
					.getMainWindow()
					.showNotification("保存异常!",
							Notification.TYPE_ERROR_MESSAGE); 
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
	public void refreshComponentInfo( Long customerQuestionnaireId,int doType,String recordFilePath) {
		customerQuestionnaire = customerQuestionnaireService.get(customerQuestionnaireId);
		if (null != customerQuestionnaire){
			questionnaire = customerQuestionnaire.getQuestionnaire();
		}else{
			this.lb_msg.setValue(WorkUIUtils.fontColorHtmlString("没有找到给条客户问卷"));
		}
		try {
			updateQuestionnaireUI();
	 		updateQuestionOptionsUI();
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
		OptionsListViewInfo optionsListViewInfo = new OptionsListViewInfo();
		optionsListViewInfo.setInitCode(1);
		optionsListViewInfo.setCustomerQuestionnaire(customerQuestionnaire);
		QuestionOptionsListView listView = new QuestionOptionsListView(question, optionsList, index+".",optionsListViewInfo);
		leftContent.addComponent(listView);
		/*leftContent.addComponent(new Label("<hr>",Label.CONTENT_XHTML));*/
		listViews.add(listView);
	}
	
	private void  buildRightContentQuestionUI(Question question){
		List<QuestionOptions> optionsList = questionService.getQuestionOptionsListByQuestionId(question.getId());
		int index = questionService.getIndexByQuestionnaireAndQuestionOrder(questionnaire.getId(),question.getOrdernumber());
		OptionsListViewInfo optionsListViewInfo = new OptionsListViewInfo();
		optionsListViewInfo.setInitCode(1);
		optionsListViewInfo.setCustomerQuestionnaire(customerQuestionnaire);
		QuestionOptionsListView listView = new QuestionOptionsListView(question, optionsList, index+".",optionsListViewInfo);
		rightContent.addComponent(listView);
		/*rightContent.addComponent(new Label("<hr>",Label.CONTENT_XHTML));*/
		listViews.add(listView);
	}
	
	/**
	 * 显示
	 * @param msg
	 */
	private void showWindowMsgInfo(String msg){
		this.getApplication().getMainWindow().showNotification(msg, Notification.TYPE_HUMANIZED_MESSAGE);
	}
	
}
