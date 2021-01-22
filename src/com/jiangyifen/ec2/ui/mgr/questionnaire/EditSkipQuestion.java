package com.jiangyifen.ec2.ui.mgr.questionnaire;

 import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.entity.Question;
import com.jiangyifen.ec2.entity.QuestionOptions;
import com.jiangyifen.ec2.entity.QuestionType;
import com.jiangyifen.ec2.entity.Questionnaire;
import com.jiangyifen.ec2.service.eaoservice.QuestionService;
import com.jiangyifen.ec2.service.eaoservice.QuestionTypeService;
import com.jiangyifen.ec2.service.eaoservice.QuestionnaireService;
import com.jiangyifen.ec2.ui.FlipOverTableComponent;
import com.jiangyifen.ec2.ui.mgr.questionnaire.pojo.QuestionAndOptions;
import com.jiangyifen.ec2.ui.mgr.questionnaire.utils.WorkUIUtils;
import com.jiangyifen.ec2.ui.mgr.questionnaire.utils.ui.HiddenTextField;
import com.jiangyifen.ec2.ui.mgr.tabsheet.QuestionnaireManagement;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class EditSkipQuestion extends Window implements ClickListener, ValueChangeListener{
	
	//静态
	private static final long serialVersionUID = 753549345674821827L;
	private final Logger logger = LoggerFactory.getLogger(this.getClass());	// 日志工具
	//---------------------------UI----------------------------------------
	//本控件持有对象
	private BeanItemContainer<QuestionType> questionTypeContainer;
		
	private static final int PAGE_LENGTH = 15;
	private final Object[] COL_PROPERTIES = new Object[] {"id","title"};
	private final String[] COL_HEADERS = new String[] { "编号","问题标题"};	
	
	private QuestionnaireManagement questionnaireManagement;
	
	private Label lb_title ;
	private TextArea tf_title;
	
	
	private Table table;
	private String countSql;
	private String searchSql;
	
	private Label lb_type;
	private ComboBox typeSelect;//题目类型
	
	private Label lb_MainTitle;
	private Label lb_Subtitle;
 	
	private Label lb_msg;
	
	private Button bt_addQuestionItem;
	private Button bt_delQuestionItem;
	
	private Button  save;
	private Button  saveClose;
	private Button 	cancel;
	private Button  delete;
	
	private VerticalLayout itemLayout;
	private VerticalLayout rightLayout;
	
 	private HiddenTextField hidden_questionnaire; //问卷隐藏域
 	
 	private List<QuestionItemLayoutSelect> itemList = new ArrayList<QuestionItemLayoutSelect>();
	//---------------------------OP----------------------------------------
	private FlipOverTableComponent<Question> flip;
	
	private Long questionnaireid = new Long(-1);
	
	private Questionnaire questionnaire; 
	private QuestionService questionService;
	private QuestionnaireService questionnaireService;
	private QuestionTypeService questionTypeService;
	
	public EditSkipQuestion(QuestionnaireManagement questionnaireManagement){
		this.questionnaireManagement = questionnaireManagement;
		
		initWindow();
		initHidden();
		initComponents();
		initSpringContext();
		
		VerticalLayout 		mainLayoutMain 		=	createMainLayoutComponents();			//总布局栏
		
		HorizontalLayout 	mainFootLayout 		=	createMainFootLayoutComponents();		//总布局栏
		VerticalLayout 		leftLayout 			= 	createLeftLayoutComponents();			//左边栏
		VerticalLayout 		questionnarieInfo	=  	createQuestionnarieInfoComponents();	//问卷信息栏
		VerticalLayout    	questionList 		=	createQuestionListComponents();			//总布局栏
		
							rightLayout 		= 	createRightLayoutComponents();			//右边栏
		GridLayout 			questionInfo 		= 	createQuestionInfoComponents();			//问题基本信息栏
		HorizontalLayout 	toolLayout	 		= 	createToolLayoutComponents();			//工具栏
		  	 				itemLayout 			= 	createItemComponents();					//问题动态选项
		HorizontalLayout 	bottomLayout 		= 	createBottonComponents();				//底部操作栏
		HorizontalLayout 	msgLayout 			= 	createMsgComponents();				//底部提示信息栏
		
		initTableAndFlip();
		questionList.addComponent(table);
		questionList.addComponent(flip);
		questionList.addComponent(hidden_questionnaire);
 		leftLayout.addComponent(questionList);
		
		
		rightLayout.addComponent(questionInfo);
		rightLayout.addComponent(toolLayout);
		rightLayout.addComponent(itemLayout);
		rightLayout.addComponent(bottomLayout);
		rightLayout.addComponent(msgLayout);
 		
		mainFootLayout.addComponent(leftLayout);
		mainFootLayout.addComponent(rightLayout);
		
		mainLayoutMain.addComponent(questionnarieInfo);
		mainLayoutMain.addComponent(mainFootLayout);
		this.addComponent(mainLayoutMain);
	}
	/**初始化Spring上写相关系想你 */
	private void initSpringContext(){
		questionnaireService = SpringContextHolder.getBean("questionnaireService");
		questionService = SpringContextHolder.getBean("questionService");
		questionTypeService = SpringContextHolder.getBean("questionTypeService");
	}
	private HorizontalLayout createBottonComponents() {
		HorizontalLayout bottomLayout_rt = new HorizontalLayout();
		bottomLayout_rt.setSpacing(true);
		bottomLayout_rt.setWidth("100%");
		
		//保存下一题
		save = new Button("保存修改", this); 
		save.setStyleName("default");
		save.setEnabled(false);
		bottomLayout_rt.addComponent(save);
		
		//保存关闭
		saveClose = new Button("保存关闭", this);
		saveClose.setStyleName("default");
		saveClose.setEnabled(false);
		bottomLayout_rt.addComponent(saveClose);
		
		delete = new Button("删除问题", this);
		delete.setStyleName("default");
		delete.setEnabled(false);
		bottomLayout_rt.addComponent(delete);
		
		// 取消按钮
		cancel = new Button("取消", this);
		bottomLayout_rt.addComponent(cancel);
			
 		return bottomLayout_rt;
	}
	private HorizontalLayout createMsgComponents(){
		HorizontalLayout msgLayout_rt = new HorizontalLayout();
		msgLayout_rt.setSpacing(true);
		msgLayout_rt.setWidth("100%");
		lb_msg = new Label("",Label.CONTENT_XHTML);
		lb_msg.setWidth("-1px");
		msgLayout_rt.addComponent(lb_msg);
		return msgLayout_rt;
	}
	private VerticalLayout createItemComponents() {
		VerticalLayout itemLayout_rt = new VerticalLayout();
		itemLayout_rt.setSpacing(true);
		itemLayout_rt.setWidth("100%");
		return itemLayout_rt;
	}

	private VerticalLayout createQuestionListComponents() {
		VerticalLayout questionList_rt = new VerticalLayout();
		questionList_rt.setSpacing(true);
		questionList_rt.setSizeFull();
		questionList_rt.setWidth("100%");
		return questionList_rt;
	}

	private void  initHidden(){
		hidden_questionnaire = new HiddenTextField();
	}
	
	private void initComponents(){
		 
	}
	
	private GridLayout createQuestionInfoComponents() {
		GridLayout questionInfo_rt = new GridLayout(3,2);
		questionInfo_rt.setSpacing(true);
		questionInfo_rt.setSizeFull();
		questionInfo_rt.setWidth("100%");
		lb_title = new Label("标题:");
		tf_title = new TextArea();
		tf_title.setRows(2);
		tf_title.setColumns(25);
		tf_title.setMaxLength(200);
 		tf_title.setInputPrompt("请填写问题标题");
		 
 		questionInfo_rt.addComponent(lb_title);
 		questionInfo_rt.addComponent(tf_title);
 		questionInfo_rt.addComponent(new Label("<font color='red'>*</font>",Label.CONTENT_XHTML));
		
		lb_type = new Label("类型:");
		List<QuestionType> userList = questionTypeService.getAllQuestionTypeList();
		questionTypeContainer = new BeanItemContainer<QuestionType>(QuestionType.class);
		questionTypeContainer.addAll(userList);
		typeSelect = new ComboBox();
		typeSelect.setNullSelectionAllowed(false);
		typeSelect.setInputPrompt("请选择问题类型");
		typeSelect.setImmediate(true);
		typeSelect.setWidth("335px");
		typeSelect.addListener(this);
		typeSelect.setContainerDataSource(questionTypeContainer);
		typeSelect.setItemCaptionPropertyId("name");
		
		questionInfo_rt.addComponent(lb_type);
		questionInfo_rt.addComponent(typeSelect);
		questionInfo_rt.addComponent(new Label("<font color='red'>*</font>",Label.CONTENT_XHTML));
		
		return questionInfo_rt;
	}

	private VerticalLayout createQuestionnarieInfoComponents() {
		VerticalLayout questionnarieInfo_rt = new VerticalLayout();
		questionnarieInfo_rt.setSpacing(true);
		questionnarieInfo_rt.setSizeFull();
		questionnarieInfo_rt.setWidth("100%");	
		
		HorizontalLayout hrow1 = new HorizontalLayout();
		lb_MainTitle = new Label() ;
		lb_MainTitle.setWidth("260px");
		hrow1.addComponent(new Label("问卷主标题："));
		hrow1.addComponent(lb_MainTitle);
		HorizontalLayout hrow2 = new HorizontalLayout();
		lb_Subtitle= new Label() ;
		hrow2.addComponent(new Label("问卷副标题："));
		hrow2.addComponent(lb_Subtitle);
		questionnarieInfo_rt.addComponent(hrow1);
		questionnarieInfo_rt.addComponent(hrow2);
		return questionnarieInfo_rt;
	}

	//总布局栏
		private VerticalLayout createMainLayoutComponents() {
			VerticalLayout mainLayout_rt = new VerticalLayout();
			mainLayout_rt.setSpacing(true);
			mainLayout_rt.setSizeFull();
			mainLayout_rt.setWidth("100%");
			return mainLayout_rt;
		}
		
	//总布局栏
	private HorizontalLayout createMainFootLayoutComponents() {
		HorizontalLayout mainFootLayout_rt = new HorizontalLayout();
		mainFootLayout_rt.setSpacing(true);
		mainFootLayout_rt.setSizeFull();
		mainFootLayout_rt.setWidth("100%");
		return mainFootLayout_rt;
	}
	
	//左边栏
	private VerticalLayout createLeftLayoutComponents() {
		VerticalLayout 	leftLayout_rt = new VerticalLayout();
		leftLayout_rt.setSpacing(true);//组件距离
		leftLayout_rt.setSizeFull();
		leftLayout_rt.setWidth("100%");
		return leftLayout_rt;
	}
	
	private VerticalLayout createRightLayoutComponents() {
		VerticalLayout 	rightLayout_rt = new VerticalLayout();
		rightLayout_rt.setSpacing(true);
		rightLayout_rt.setSizeFull();
		rightLayout_rt.setWidth("100%");
 		return rightLayout_rt;
	}
	
	
	//工具栏
	public HorizontalLayout createToolLayoutComponents(){
		HorizontalLayout tool = new HorizontalLayout();
		tool.setSpacing(true);
 		bt_addQuestionItem = new Button("加", this);
 		bt_addQuestionItem.setEnabled(false);
		bt_delQuestionItem = new Button("减", this);
		bt_delQuestionItem.setEnabled(false);
		tool.addComponent(bt_addQuestionItem);
		tool.addComponent(bt_delQuestionItem);
		return tool;
	}	
	

	private void  initWindow(){
		this.center();
		this.setModal(true);
		this.setResizable(false);
		this.setCaption("问题修改/跳转");
		this.setWidth("900px");
		this.setHeight("460px");
	}
	
	
	
	private void initTableAndFlip(){
		initTableProperty();
		initializeSql();
		initFlip();
		initColumnsHeaders();
	}
	/**
	 *  创建格式化 了 显示内容的 Table对象
	 */
	private Table createColumnTable() {
		return new Table() {
			private static final long serialVersionUID = 1271499986743438321L;
			@Override
			protected String formatPropertyValue(Object rowId, Object columnId, Property property) {
 				if(columnId.equals("title")) {
					String mainTitle = (String) property.getValue();
					if(mainTitle.length() > 25) {
						mainTitle =mainTitle.substring(0,25)+"...";
					}
					return mainTitle;
				}
				 
				if(columnId.equals("createTime")) {
					Date d = (Date) property.getValue();
					SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					return dateFormat.format(d);
				}
				
				return super.formatPropertyValue(rowId, columnId, property);
			}
		};
	}
	
	private void initTableProperty(){
		table = createColumnTable();
		table.setSizeFull();
		table.addListener(this);
		table.setImmediate(true);
		table.setSelectable(true);
		table.setNullSelectionAllowed(true);
		table.setRowHeaderMode(Table.ROW_HEADER_MODE_INDEX);
		table.addStyleName("striped");
		table.setPageLength(PAGE_LENGTH);
		table.setHeight("300px");
		
	}
	private void initColumnsHeaders(){
		table.setVisibleColumns(COL_PROPERTIES);
		table.setColumnHeaders(COL_HEADERS);
	}
	private void initializeSql() {
 		countSql = "Select count(s) from Question as s where s.questionnaire.id = "+questionnaireid;
		searchSql = countSql.replaceFirst("count\\(s\\)", "s") ;//
		searchSql += " order by s.createTime asc";
	}
	
	private void initFlip(){
 		flip = new FlipOverTableComponent<Question>(Question.class, questionService, table, searchSql, countSql, null);
		flip.setPageLength(PAGE_LENGTH, true);
	}
	
	/**
	 * 刷新显示分机的表格
	 * @param isToFirst 刷新后是否跳转至第一页
	 */
	public void updateTable(Boolean isToFirst) {
		if (flip != null) {
			flip.setSearchSql(searchSql);
			flip.setCountSql(countSql);
			if (isToFirst) {
				flip.refreshToFirstPage();
			} else {
				flip.refreshInCurrentPage();
			}
		}
	}

	public Table getTable() {
		return table;
	}
	
	
	
	@Override
	public void attach() {
		super.attach();
		questionnaire = (Questionnaire) questionnaireManagement.getTable().getValue();
 		questionnaireid = questionnaire.getId();
		initializeSql();
		initTableUIAndData();
		updateQuestionnaire();
	}

	private void initTableUIAndData() {
		flip.setCountSql(countSql);
		flip.setSearchSql(searchSql);
		flip.refreshToFirstPage();
		table.setValue(null);
	}

	public void updateQuestionnaire(){
		if(null != questionnaire){
 			hidden_questionnaire.setData(questionnaire);
			String mainTitle = questionnaire.getMainTitle();
			String mainTitleDesc = questionnaire.getMainTitle();
			if(null!=mainTitle){
				if(mainTitle.trim().length()>60){
					mainTitle = mainTitle.substring(0,60)+"..";
				}
			}else{
				mainTitle = "";
				mainTitleDesc="";
			}
 			lb_MainTitle.setValue(mainTitle) ;
			lb_MainTitle.setDescription(mainTitleDesc);
			
			 
			String subtitle = questionnaire.getSubtitle();
			String subtitleDesc = questionnaire.getSubtitle();;
			if(null!=subtitle){
				if(subtitle.trim().length()>60){
					subtitle = subtitle.substring(0,60)+"..";
				}
			}else{
				subtitle = "";
				subtitleDesc="";
			}
			lb_Subtitle.setValue(subtitle);
			lb_Subtitle.setDescription(subtitleDesc);
		} 
	}
	
	@Override
	public void valueChange(ValueChangeEvent event) {
		Property source = event.getProperty();
		if (source == table) {
			clearMsgInfo();
			Question target = (Question) table.getValue();
			save.setEnabled(target != null);
			saveClose.setEnabled(target != null);
			delete.setEnabled(target != null);
			bt_addQuestionItem.setEnabled(target != null);
			bt_delQuestionItem.setEnabled(target != null);
			
			if (null != target) {
				this.tf_title.setValue(target.getTitle());
 				for(int i=0;i<questionTypeContainer.size();i++){
					QuestionType q1 = questionTypeContainer.getIdByIndex(i);
					if(q1.getId() != null && q1.getId().equals(target.getQuestionType().getId())){
						typeSelect.setValue(q1);
					}
				}
 				QuestionType qtar = target.getQuestionType();
 				if(qtar != null){
 					if(questionTypeContainer.size() > 0){
 	 					for(QuestionType qt : questionTypeContainer.getItemIds()) {
 	 						 if(qt.getId() != null && qt.getId().equals(qtar.getId())){
 	 							typeSelect.setValue(qt);
 	 						 }
 	 					}
 	 				}
 				}
				clearItemLayoutAndItem();
				updateItemLayoutAndItem(target.getId());
			} else {
				clearItemLayoutAndItem();
				this.tf_title.setValue("");
				this.typeSelect.select(null);
			}
		}else if (source == typeSelect) {//类型变化时改变Item对象中的属性  TODO typeSelect
			String tvalue = getTypeSelectValue(); // TODO Enable
			if(null != tvalue){
				boolean skipEnabled = false;
				boolean txCheck = false;
				if("radio".equals(tvalue)){
					skipEnabled = true;
				}
				if("open".equals(tvalue)){
					txCheck = true;
				}
 				for (int i = 0; i < itemList.size(); i++) {
					QuestionItemLayoutSelect qitem = itemList.get(i);
					if(null!=qitem){
						qitem.setSkipEnabled(skipEnabled);
						qitem.setCB_Text(txCheck);
					}
				}
			}
		}
		 
	}
	private void clearItemLayoutAndItem(){
		itemLayout.removeAllComponents();
		itemList.clear();
	}
	/**根据问题更新问题所属信息，问题信息-问题选项信息*/
	private void updateItemLayoutAndItem(Long id) {
		Question qst = questionnaireService.getQuestionById(id);	//问题
		List<QuestionOptions> options = questionnaireService.getQuestionOptionsListByQuestionId(id);
		for (int i = 0; i < options.size(); i++) {
			QuestionOptions op = options.get(i);
			QuestionItemLayoutSelect q = initQuestionItemLayoutSelect(qst,op,i,1);
			itemLayout.addComponent(q);
			itemList.add(q);
		}
	}
	
	/**更新问题选项数据*/
	private QuestionItemLayoutSelect initQuestionItemLayoutSelect(Question qst,QuestionOptions op,int itemLength,int indexType) {
		Question q = op.getSkip();
		String skip = "";	
		if (q != null && q.getId() > 0) {
			skip = q.getId()+"";
		}
		boolean skipEnabled = false;// TODO Enable
		boolean txCheck = false;
		QuestionType qtype = qst.getQuestionType();
		String tvalue = qtype.getValue();
		if(null != tvalue){
			if("radio".equals(tvalue)){
				skipEnabled = true;
			}else{
				skipEnabled = false;
			}
		}else{
			skipEnabled = true;
		}
		String optype = op.getType();
		if(null != optype ){
			if("TT".equals(optype)){
				txCheck = true;
			}else if("TF".equals(optype)){ 
				txCheck = false;
			}else{
				txCheck = false;
			}
		}
 		QuestionItemLayoutSelect qis = new QuestionItemLayoutSelect(this,itemLength,indexType,op.getName(),skip,skipEnabled,txCheck);
 		return qis;
	}
	
	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if(source == bt_addQuestionItem){
			addQuestionItem();
		}
		if(source == bt_delQuestionItem){
			deleteQuestionItem();
		}
		if(source == save){
			if(checkform()){
				excuteUpdateQuestion();		//执行保存，跳转下一题
 				initializeSql();
				initTableUIAndData();
			}
		}
		if(source == saveClose){
			if(checkform()){
				excuteUpdateQuestion();		//执行保存，跳转下一题
				this.questionnaireManagement.deleteEditSkipQuestionWindow();
			}
		}
		if(source == cancel){
			this.questionnaireManagement.deleteEditSkipQuestionWindow();
		}
		if(source == delete){
			excute_deleteQuestion();
			initializeSql();
			initTableUIAndData();
		}
	}
	/**添加问题  选项 空问题选项*/
	private void addQuestionItem() {
			QuestionItemLayoutSelect h = buildQuestionItemLayoutSelect();
			boolean skipEnabled = false;// TODO Enable
			String tvalue = getTypeSelectValue();
			if(null != tvalue){
				if("radio".equals(tvalue)){
					skipEnabled = true;
				}
			}
			h.setSkipEnabled(skipEnabled);
			itemList.add(h);
			itemLayout.addComponent(h);
	}
	/**创建一个空的问题选项 */
	private QuestionItemLayoutSelect buildQuestionItemLayoutSelect() {
		int itemLength = itemList.size();
		return new QuestionItemLayoutSelect(itemLength,1,this);
		
	}
	/**删掉一个问题选项包括有值和没有赋值的*/
	private void deleteQuestionItem(){
		if(itemList.size()==1){
			
		}else{
 			 itemLayout.removeComponent(itemList.get(itemList.size()-1));
			 itemList.remove(itemList.size()-1);
		}
	}
	
	/**校验表单 */
	private boolean checkform() {
		boolean valiTF = false;
		if(WorkUIUtils.stringIsEmpty(this.tf_title.getValue())){
			lb_msg.setValue("<font color='red'>问题标题不能为空!</font>");
		}else if(WorkUIUtils.stringIsEmpty(this.typeSelect.getValue())){
			lb_msg.setValue("<font color='red'>问题类型不能为空!</font>");
		}else{
			/*boolean qitemTF = false;
			for (int i = 0; i < itemList.size(); i++) {//问题选项校验
				QuestionItemLayoutSelect qitem_lt = itemList.get(i);
					if(!QuestionnaireUtils.stringIsEmpty(qitem_lt.getItem_title().getValue())){
							 qitemTF = true;
					 }else{
						 if(!(Boolean) qitem_lt.getCb_tf_text().getValue()){//如果是文本  可以为空 不是文本必须填写
								//不是文本
							 qitemTF = true;
						 }else{
							 qitemTF = false;
						 }
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
	
	
	/**
	 * 执行保存
	 */
	private void excuteUpdateQuestion(){
		try {
			Question old_question = (Question)table.getValue();
			if (null != old_question) {
				QuestionType qt = (QuestionType)typeSelect.getValue();
				if(null!=qt){
					old_question.setQuestionType(qt);
					old_question.setTitle(this.tf_title.getValue().toString());
					List<QuestionOptions> questionOptions = new ArrayList<QuestionOptions>();
					for (int i = 0; i < itemList.size(); i++) {
						QuestionItemLayoutSelect qitem = itemList.get(i);
						QuestionOptions option = new QuestionOptions();
						option.setName(qitem.getItem_title().getValue().toString());
						option.setCreateTime(new Date());
						option.setOrdernumber(i+1);
						boolean ttf =  (Boolean)qitem.getCb_tf_text().getValue();
						if(ttf){
							option.setType("TT");
						}else{
							option.setType("TF");
						}
						
						boolean skipEnabled = false;// TODO Enable
						String tvalue = getTypeSelectValue();
						if(null != tvalue){
							if("radio".equals(tvalue)){
								skipEnabled = true;
							}
						}
						if(skipEnabled){
							Object skipid_obj = qitem.getItem_spik().getValue();
							Long skipid = new Long(-1);
							if(!WorkUIUtils.stringIsEmpty(skipid_obj)){
								skipid = Long.valueOf(skipid_obj.toString().trim());
							}
							if(skipid>0){
								Question  q = questionnaireService.getQuestionById(skipid);
								if(null!=q){
									option.setSkip(q);
								}
							}
						}else{
							option.setSkip(null);
						}
						
						questionOptions.add(option);
					}

					QuestionAndOptions qao = new QuestionAndOptions();
					qao.setQuestionnaire(questionnaire);
					qao.setQuestion(old_question);
					qao.setQuestionOptions(questionOptions);
					boolean opt = questionnaireService.updateQuestionAndOptions(qao);
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
			logger.error("保存问题异常_excute_updateQuestion_LLXXYY", e);
			this.getApplication()
					.getMainWindow()
					.showNotification("保存问题异常!",
							Notification.TYPE_ERROR_MESSAGE);
		}
	}
	
	public void excute_deleteQuestion(){
		try {
			Question old_question = (Question) table.getValue();
			if (null != old_question) {
				if(questionService.findQuestionSkipBuId(old_question.getId())){//存在该问题跳转的选项关联
 					this.getApplication()
					.getMainWindow()
					.showNotification("存在该问题的跳转，请先删除跳转关联后再删除该问题!",
							Notification.TYPE_HUMANIZED_MESSAGE);
				}else{
					questionnaireService.deleteQuestionAndOptions(old_question.getId());
					showWindowMsgInfo("删除问题成功");
				}
			}else{
				showWindowMsgInfo("未选中问题");
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("删除问题异常_excute_deleteQuestion", e);
			this.getApplication()
					.getMainWindow()
					.showNotification("删除问题异常!",
							Notification.TYPE_ERROR_MESSAGE);
		}
	}
	
	//获得类型的选择值
	private String getTypeSelectValue(){
		String typeRT = null;
		Object tag = typeSelect.getValue();
		QuestionType t = null;
		if(null != tag){
			t = (QuestionType)tag;
		}
		if(null != t){
			typeRT = t.getValue();
		}
		return typeRT;
	}
	
	/**清空提示信息内容 */
	private void clearMsgInfo(){
		lb_msg.setValue("");
	}
	//============================================================================================================
	/**
	 * 显示提示信息，并标红
	 * 
	 * @param text
	 */
	private void showMsg(String text){
		lb_msg.setValue("<font color='red'>"+text+"</font>");	
	}
	/**
	 * 显示 窗体显示提示信息
	 * 
	 * @param msg
	 */
	private void showWindowMsgInfo(String msg){
		this.getApplication().getMainWindow().showNotification(msg, Notification.TYPE_HUMANIZED_MESSAGE);
	}
}
