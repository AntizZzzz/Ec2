package com.jiangyifen.ec2.ui.mgr.questionnaire;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.entity.Question;
import com.jiangyifen.ec2.entity.QuestionOptions;
import com.jiangyifen.ec2.entity.QuestionType;
import com.jiangyifen.ec2.entity.Questionnaire;
import com.jiangyifen.ec2.service.eaoservice.QuestionTypeService;
import com.jiangyifen.ec2.service.eaoservice.QuestionnaireService;
import com.jiangyifen.ec2.ui.mgr.questionnaire.pojo.QuestionAndOptions;
import com.jiangyifen.ec2.ui.mgr.questionnaire.utils.WorkUIUtils;
import com.jiangyifen.ec2.ui.mgr.questionnaire.utils.ui.HiddenTextField;
import com.jiangyifen.ec2.ui.mgr.tabsheet.QuestionnaireManagement;
import com.jiangyifen.ec2.ui.mgr.util.StyleConfig;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

//TODO lxy	1305
/**
* 问卷调查添加问卷
* 
* 
* 注入：QuestionnaireEao questionnaireEao
* 
* @author lxy
*
*/
public class AddQuestion extends Window implements ClickListener {

	
	private static final long serialVersionUID = 3542356129862831719L;
	// 日志工具
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	//=================================页面初始化start=================================================
	//页面展示初始化
	private QuestionnaireManagement questionnaireManagement;
	//本控件持有对象
	private BeanItemContainer<QuestionType> questionTypeContainer;
		
 	//页面服务层初始化
	private Button saveNext;		//保存下一题
	private Button saveClose;		//保存关闭
	private Button cancel;		//取消按钮
	private Label lb_msg;		//提示信息
 	private Label lb_title ;
	private TextArea tf_title;
	
	private Label lb_type;
	private ComboBox typeSelect;//题目类型
	
	private Label lb_MainTitle;
	private Label lb_Subtitle;
	private Label lb_QuestionnaireCount;
	private Label lb_Question;
	
 	private HiddenTextField hidden_questionnaire; //问卷隐藏域
	
	private Button bt_addQuestionItem;
	private Button bt_delQuestionItem;
	
	private List<QuestionItemLayout> itemList = new ArrayList<QuestionItemLayout>();
 	
	private VerticalLayout item = null;
	
	//业务元素
	//private Domain domain;		//当前用户
	private Questionnaire questionnaire;
	private QuestionnaireService questionnaireService;	 
	private QuestionTypeService questionTypeService;
	
	
	public AddQuestion (QuestionnaireManagement questionnaireManagement){
		this.questionnaireManagement = questionnaireManagement;
		initThis();
		initHidden();
		initSpringContext();
		initCompanent();
	}
	
	private void initThis(){
		this.center();
		this.setModal(true);
		this.setResizable(false);
		this.setCaption("添加问题");
		this.setWidth("500px");
		this.setHeight("450px");
	}
	
	private void initHidden(){
		 hidden_questionnaire = new HiddenTextField();
	}
	
	/**初始化Spring上写相关系想你 */
	private void initSpringContext(){
		questionnaireService = SpringContextHolder.getBean("questionnaireService");
		questionTypeService = SpringContextHolder.getBean("questionTypeService");
	}
	/**初始化控件布局和组件 */
	private void initCompanent() {
		//添加Window内最大的Layout
		VerticalLayout 		windowContent 		= 	createWindowContentComponents();	//竖直布局
		GridLayout 			questionnaireLayout = 	createQuestionnaireComponents();	//问卷信息栏
		GridLayout 			questionLayout 		= 	createQuestionLayoutComponents();	//问题信息栏
		HorizontalLayout 	toolLayout	 		= 	createToolLayoutComponents();		//工具栏
		VerticalLayout 	 	itemLayout 			= 	createItemComponents();				//动态选项
		HorizontalLayout 	bottomLayout 		= 	createBottonComponents();			//底部操作栏
		
		windowContent.addComponent(questionnaireLayout);
		windowContent.addComponent(questionLayout);
		windowContent.addComponent(toolLayout);
		windowContent.addComponent(itemLayout);
		windowContent.addComponent(bottomLayout);
		
		this.addComponent(windowContent);
		
		addQuestionItem();
		addQuestionItem();
		addQuestionItem();
		addQuestionItem();			
	}
	
	private VerticalLayout createWindowContentComponents(){
		VerticalLayout windowContent_rt = new VerticalLayout();
		windowContent_rt.setSizeUndefined();
		windowContent_rt.setMargin(false, true, true, false);
		windowContent_rt.setSpacing(true);
		windowContent_rt.setStyleName(StyleConfig.VERTICAL_STYLE);
		return windowContent_rt;
	}
	
	
	//问卷信息栏
	private GridLayout createQuestionnaireComponents(){
		
		GridLayout  questionnaireLt_rt = new GridLayout(2,4);
		questionnaireLt_rt.setSpacing(true);
		questionnaireLt_rt.setWidth("100%");
		questionnaire = (Questionnaire)questionnaireManagement.getTable().getValue();
 		if(null != questionnaire){
 			hidden_questionnaire.setData(questionnaire);
			String mainTitle = questionnaire.getMainTitle();
			String mainTitleDesc = questionnaire.getMainTitle();
			if(null!=mainTitle){
				if(mainTitle.trim().length()>28){
					mainTitle = mainTitle.substring(0,28)+"..";
				}
			}else{
				mainTitle = "";
				mainTitleDesc="";
			}
			lb_MainTitle = new Label(mainTitle) ;
			lb_MainTitle.setDescription(mainTitleDesc);
			questionnaireLt_rt.addComponent(new Label("问卷主标题："));
			questionnaireLt_rt.addComponent(lb_MainTitle);
			
			 
			String subtitle = questionnaire.getSubtitle();
			String subtitleDesc = questionnaire.getSubtitle();;
			if(null!=subtitle){
				if(subtitle.trim().length()>28){
					subtitle = subtitle.substring(0,28)+"..";
				}
			}else{
				subtitle = "";
				subtitleDesc="";
			}
			lb_Subtitle = new Label(subtitle);
			lb_Subtitle.setDescription(subtitleDesc);
			questionnaireLt_rt.addComponent(new Label("问卷副标题："));
			questionnaireLt_rt.addComponent(lb_Subtitle); 
			
			int questionCount = 0;
			questionCount  = questionnaireService.getQuestionCountByQuestionnaireId(questionnaire.getId());
			lb_QuestionnaireCount = new Label("[ "+questionCount+" ]题");
 			questionnaireLt_rt.addComponent(new Label("题目总数：")); 
			questionnaireLt_rt.addComponent(lb_QuestionnaireCount);
			
			Question qinfo  =  questionnaireService.getUpQuestionQuestionnaireId(questionnaire.getId());
			String qinfoTitle = "";
			String qinfoTitleDesc = "";
			if(null!=qinfo){
				qinfoTitle = qinfo.getTitle();
				qinfoTitleDesc =  qinfo.getTitle();
				if(null!=qinfoTitle){
					if(qinfoTitle.trim().length()>28){
						qinfoTitle = qinfoTitle.substring(0,28)+"..";
					}
				}else{
					qinfoTitle = "";
					qinfoTitleDesc="";
				}
			} 
			
			lb_Question = new Label(qinfoTitle);
			lb_Question.setDescription(qinfoTitleDesc);
			questionnaireLt_rt.addComponent(new Label("前一问题：")); 
			questionnaireLt_rt.addComponent(lb_Question);
			
			return questionnaireLt_rt;
		}else{
			questionnaireLt_rt.addComponent(new Label(""));
			questionnaireLt_rt.addComponent(new Label("<font color='red'>选中对象不存在</font>",Label.CONTENT_XHTML));
			return questionnaireLt_rt;
		}
		
	}

	//问题信息栏
	private GridLayout createQuestionLayoutComponents() {
 
		GridLayout questionGridLayout_rt = new GridLayout(3,2);
		questionGridLayout_rt.setSpacing(true);
		questionGridLayout_rt.setWidth("100%");
		lb_title = new Label("问题标题：");
		tf_title = new TextArea();
		tf_title.setRows(2);
		tf_title.setColumns(30);
		tf_title.setMaxLength(200);
 		tf_title.setInputPrompt("请填写问题标题");
		 
 		questionGridLayout_rt.addComponent(lb_title);
 		questionGridLayout_rt.addComponent(tf_title);
 		questionGridLayout_rt.addComponent(new Label("<font color='red'>*</font>",Label.CONTENT_XHTML));
		
		lb_type = new Label("问题类型：");
		
		List<QuestionType> userList = questionTypeService.getAllQuestionTypeList();
		questionTypeContainer = new BeanItemContainer<QuestionType>(QuestionType.class);
		questionTypeContainer.addAll(userList);
		typeSelect = new ComboBox();
		typeSelect.setNullSelectionAllowed(false);
		typeSelect.setInputPrompt("请选择问题类型");
		typeSelect.setImmediate(true);
		typeSelect.setWidth("100%");
		typeSelect.setContainerDataSource(questionTypeContainer);
		typeSelect.setItemCaptionPropertyId("name");
		
		questionGridLayout_rt.addComponent(lb_type);
		questionGridLayout_rt.addComponent(typeSelect);
		questionGridLayout_rt.addComponent(new Label("<font color='red'>*</font>",Label.CONTENT_XHTML));
		
		return questionGridLayout_rt;
		
	}
	//工具栏
	public HorizontalLayout createToolLayoutComponents(){
		HorizontalLayout tool = new HorizontalLayout();
		tool.setSpacing(true);
		tool.setWidth("100%");
		
		bt_addQuestionItem = new Button("加");
		bt_addQuestionItem.addListener(this);
		
		bt_delQuestionItem = new Button("减");
		bt_delQuestionItem.addListener(this);
		tool.addComponent(bt_addQuestionItem);
		tool.addComponent(bt_delQuestionItem);
	 
		return tool;
	}
	//动态
	private QuestionItemLayout  buildQuesttionItem(){
		int itemLength = itemList.size();
		return new QuestionItemLayout(itemLength,1);
		
	}
	//动态选项栏
	private VerticalLayout  createItemComponents(){
		item = new VerticalLayout();
		item.setSpacing(true);
		item.setWidth("100%");
		return item;
	}
	//底部操作栏
	private HorizontalLayout createBottonComponents() {
		HorizontalLayout bottonLt = new HorizontalLayout();
		
		bottonLt.setSpacing(true);
		bottonLt.setWidth("100%");

		//保存下一题
		saveNext = new Button("保存/下一题", this);
		saveNext.setStyleName("default");
		bottonLt.addComponent(saveNext);
		
		//保存关闭
		saveClose = new Button("保存/关闭", this);
		saveClose.setStyleName("default");
		bottonLt.addComponent(saveClose);
		
		// 取消按钮
		cancel = new Button("取消", this);
		bottonLt.addComponent(cancel);
		
		lb_msg = new Label("");
		lb_msg.setWidth("-1px");
		lb_msg.setContentMode(Label.CONTENT_XHTML);
		bottonLt.addComponent(lb_msg);
		return bottonLt;
	}
	
	
	/**
	 * attach 方法
	 */
	@Override
	public void attach() {
		super.attach();
		// 简单编辑分机的各字段的值
		//createTypicalSipConfig();
	}

	//=================================页面初始化end=================================================
	//=================================接口实现start=================================================
	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if(source == saveNext){
			if(checkform()){
				excute_SaveQuestion();		//执行保存，跳转下一题
				questionnaireManagement.showNextAddQuestionWindow();
			}
		}
		if(source == saveClose){
			if(checkform()){
				excute_SaveQuestion();		//执行保存，跳转下一题
				questionnaireManagement.closeQuestionWindow();
			}
		}
		if(source == cancel){
			questionnaireManagement.closeQuestionWindow();
		}
		if(source == bt_addQuestionItem){
			addQuestionItem();
		}
		if(source == bt_delQuestionItem){
			deleteQuestionItem();
		}
	}
	
	/**
	 * 执行保存
	 */
	private void excute_SaveQuestion(){
		try {
			Questionnaire questionnaire = getHiddenQuestionnaire();
			
			if(null!=questionnaire){
 				QuestionType qt= (QuestionType)typeSelect.getValue();
				if(null!=qt){
					Question question = new Question();
					question.setCreateTime(new Date());
					question.setQuestionnaire(questionnaire);
					question.setQuestionType(qt);
					question.setTitle(this.tf_title.getValue().toString());
					
					List<QuestionOptions> questionOptions = new ArrayList<QuestionOptions>();
					String TTF = "TF";
					if("open".equals(qt.getValue())){
						TTF = "TT";
					}else{
						TTF = "TF";
					}
					for (int i = 0; i < itemList.size(); i++) {
						QuestionItemLayout qitem = itemList.get(i);
						QuestionOptions option = new QuestionOptions();
						option.setOrdernumber(i+1);
						option.setType(TTF);
						option.setName(qitem.getItem_title().getValue().toString());
						option.setCreateTime(new Date());
						questionOptions.add(option);
					}

					QuestionAndOptions qao = new QuestionAndOptions();
					qao.setQuestionnaire(questionnaire);
					qao.setQuestion(question);
					qao.setQuestionOptions(questionOptions);
					
					boolean opt = questionnaireService.saveQuestionAndOptions(qao);
					if(opt){
						//保存成功
						showWindowMsgInfo("保存问题成功");
					}else{
						//保存失败
						showWindowMsgInfo("保存问题失败");
					}
					
				}else{
					showMsg("未关联到问题类型,请尝试重新问题类型,或通知管理员");
				}
 			}else{
				showMsg("未关联到待查问卷,请尝试重新选择问卷,或通知管理员");
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("保存问题失败_excute_SaveQuestion_LLXXYY", e);
			this.getApplication()
					.getMainWindow()
					.showNotification("保存问题异常!",
							Notification.TYPE_ERROR_MESSAGE);
		}
	}
	
	private void addQuestionItem() {
			QuestionItemLayout h = buildQuesttionItem();
			itemList.add(h);
			item.addComponent(h);
	}
	
	private void deleteQuestionItem(){
		if(itemList.size()==1){
			
		}else{
			//HorizontalLayout  hy = itemList.get(itemList.size()-1)
			 item.removeComponent(itemList.get(itemList.size()-1));
			 itemList.remove(itemList.size()-1);
		}
	}
	//=================================接口实现end=================================================
	private boolean checkform() {
		boolean valiTF = false;
		if(WorkUIUtils.stringIsEmpty(this.tf_title.getValue())){
			lb_msg.setValue("<font color='red'>问题标题不能为空!</font>");
		}else if(WorkUIUtils.stringIsEmpty(this.typeSelect.getValue())){
			lb_msg.setValue("<font color='red'>问题类型不能为空!</font>");
			
		}else{
			/*boolean qitemTF = false;
			for (int i = 0; i < itemList.size(); i++) {
				QuestionItemLayout qitem = itemList.get(i);
				if(!QuestionnaireUtils.stringIsEmpty(qitem.getItem_title().getValue())){
					 qitemTF = true;
				}
			}
			if(qitemTF){
				valiTF = true;
			}else{
				lb_msg.setValue("<font color='red'>至少有一个问题选项!</font>");
			}*/
			if(itemList.size() < 1){
 				lb_msg.setValue("<font color='red'>至少有一个问题选项!</font>");
			}else{
				valiTF = true;
			}
		}
		return valiTF;
	}
	
	public boolean validateText(String value){
		if((null==value)||("".equals(value))||("".equals(value.trim()))){
			return false;
		}else{
			return true;
		}
	}
	
	//显示提示信息，并标红
	private void showMsg(String text){
		lb_msg.setValue("<font color='red'>"+text+"</font>");	
	}
	//=================================内部类实现end=================================================
	 
	//=================================内部类实现end=================================================
	
	private Questionnaire getHiddenQuestionnaire(){
		Object tvalue = hidden_questionnaire.getData();
		Questionnaire questionnaire = null;
		if(null != tvalue){
			questionnaire = (Questionnaire)tvalue;
		}
		return questionnaire;
	}
	/**
	 * 显示
	 * @param msg
	 */
	private void showWindowMsgInfo(String msg){
		this.getApplication().getMainWindow().showNotification(msg, Notification.TYPE_HUMANIZED_MESSAGE);
	}
	
}
