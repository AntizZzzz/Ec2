package com.jiangyifen.ec2.ui.mgr.tabsheet;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.Questionnaire;
import com.jiangyifen.ec2.globaldata.ResourceDataCsr;
import com.jiangyifen.ec2.service.eaoservice.QuestionnaireService;
import com.jiangyifen.ec2.ui.FlipOverTableComponent;
import com.jiangyifen.ec2.ui.mgr.questionnaire.AddQuestion;
import com.jiangyifen.ec2.ui.mgr.questionnaire.AddQuestionnaire;
import com.jiangyifen.ec2.ui.mgr.questionnaire.EditQuestionnaire;
import com.jiangyifen.ec2.ui.mgr.questionnaire.EditSkipQuestion;
import com.jiangyifen.ec2.ui.mgr.questionnaire.ShowQuestionnaireBrowse;
import com.jiangyifen.ec2.ui.mgr.questionnaire.ShowQuestionnaireBrowseShowAll;
import com.jiangyifen.ec2.ui.mgr.questionnaire.ShowQuestionnaireReport;
import com.jiangyifen.ec2.ui.mgr.questionnaire.utils.ui.ShowWindow;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.event.Action;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.Notification;

/**
 * 
 * @author lxy
 * 
 * <br>模块内容：后台问卷调查管理主页面
 *
 */
public class QuestionnaireManagement extends VerticalLayout implements Property.ValueChangeListener, Action.Handler, ClickListener  {
	
	private static final long serialVersionUID = 5429401624938789634L;
	// 日志工具
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
		
	private final Object[] COL_PROPERTIES = new Object[] {"mainTitle","subtitle","stateCode","showcode","createTime"};
	private final String[] COL_HEADERS = new String[] { "主标题","副标题","问卷状态","问卷类型","创建时间"};
	
	private static final int PAGE_LENGTH = 20;
	// 右键组件
	private final Action AddCustomer = new Action("添加问卷信息", ResourceDataCsr.customer_info_16_ico);
 	private final Action EditCustomer = new Action("编辑问卷信息", ResourceDataCsr.address_16_ico);
	private final Action DeleteCustomer = new Action("删除问卷记录", ResourceDataCsr.customer_history_record_16_ico);
	
	// 检索
	private TextField keyword;					//文本输入框
	private Button bt_search;					//所有按钮
	
	private Table table;						//表格组件
	private String searchListJpql;				//检索列表Jpql
	private String searchCountJpql;				//检索总数Jpql
  	private FlipOverTableComponent<Questionnaire> flip;//本页面分页组件

	private Button bt_add_questionnaire;		// 添加问卷
	private Button bt_edit_questionnaire;		// 编辑问卷
	private Button bt_delete_questionnaire;		// 删除问卷
	private Button bt_change_stateCode;			// 执行问卷
	private Button bt_add_question;				// 添加问题
	private Button bt_editSkip_question;			// 问题修改跳转
	private Button bt_close_questionnaire;		// 关闭问卷
	private Button bt_report_questionnaire;		// 问卷报告
	//private Button bt_dotest;
	
	// 弹出窗口 只创建一次
	private AddQuestionnaire addQuestionnaireWindow;		// 添加问卷
	private EditQuestionnaire editQuestionnaireWindow;		// 编辑问卷
	private AddQuestion addQuestioWindow;					// 添加问题
	private EditSkipQuestion editSkipQuestionWindow;		// 编辑修改跳转问题
	private ShowQuestionnaireReport showQuestionnaireReportWindow;// 显示问卷报告
	private ShowQuestionnaireBrowse showQuestionnaireBrowseWinodw;// 显示问卷浏览
	private ShowQuestionnaireBrowseShowAll showQuestionnaireBrowseShowAll;//
	//private TestRealTimeWindow realTimeWindow;
	// 业务组件
	private Domain domain;									//EC域
	//private User loginUser;		// 当前登陆用户
  	private QuestionnaireService questionnaireService;	
 	/** 该类构造函数 */
	public QuestionnaireManagement() {
		this.setSpacing(true);						// 设置边界等
		this.setMargin(true);
		this.setWidth("100%");	
		
		initBusinessAndSpring();			//	初始化业务与Spring组件
		initQuestionTypes();				//	初始化创建问题类型
		createTableTopComponents();			// 	创建队列表格上方的组件（包括搜索组件及多项删除组件）
		createQuestionnairTable();			// 	创建表格,分机表格显示区组件
		createTableFooterComponents();		// 	创建表格下方的各种操作组件（添加、编辑、删除按钮，以及表格的翻页组件）
	}
	
	/** 初始化业务与Spring组件 */
	private void initBusinessAndSpring(){
		domain = SpringContextHolder.getDomain();
		//loginUser = SpringContextHolder.getLoginUser();
 		questionnaireService = SpringContextHolder.getBean("questionnaireService");
	}
	
	
	/** 初始化创建问题类型*/
	private void initQuestionTypes() {
		questionnaireService.initQuestionTypes();
	}
	
	/** 创建队列表格上方的组件（包括搜索组件及多项删除组件） */
	private void createTableTopComponents() {
		// 创建搜索分机的相应组件
		HorizontalLayout searchHLayout = new HorizontalLayout();
		searchHLayout.setSpacing(true);
		this.addComponent(searchHLayout);
		
		Label caption = new Label("主标题：");
		caption.setWidth("-1px");
		searchHLayout.addComponent(caption);
		
		keyword = new TextField();
		keyword.setImmediate(true);
		keyword.setMaxLength(20);
		keyword.setInputPrompt("请输入主标题关键字");
		keyword.setDescription("可按'主标题'进行搜索");
		keyword.setStyleName("search");
		keyword.addListener(this);
		searchHLayout.addComponent(keyword);
		
		bt_search = new Button("搜索", this);
		bt_search.setImmediate(true);
		searchHLayout.addComponent(bt_search);		
	}

	/** 创建分机表格显示区组件*/
	private void createQuestionnairTable() {
		table = createFormatColumnTable();
		table.setWidth("100%");	
 		table.addListener(this);
		table.setImmediate(true);
		table.setSelectable(true);
		table.setNullSelectionAllowed(true);
		table.setRowHeaderMode(Table.ROW_HEADER_MODE_INDEX);
		table.addStyleName("striped");
		table.addActionHandler(this);
		table.setPageLength(PAGE_LENGTH);
		this.addComponent(table);
	}
	
	/** 创建格式化 了 显示内容的 Table对象 */
	private Table createFormatColumnTable() {
		return new Table() {
			private static final long serialVersionUID = 4347052160778120067L;
			@Override
			protected String formatPropertyValue(Object rowId, Object columnId, Property property) {
 				if(columnId.equals("mainTitle")) {
					String mainTitle = (String) property.getValue();
					if(null != mainTitle && mainTitle.length() > 40) {
						mainTitle =mainTitle.substring(0,40)+"...";
					}
					return mainTitle;
				}
				if(columnId.equals("stateCode")) {
					String statecode = (String) property.getValue();
					if(null != statecode && "D".equals(statecode)) {
						statecode = "设计";
					}else if(null != statecode && "E".equals(statecode)) {
						statecode = "执行";
					}else if(null != statecode && "C".equals(statecode)) {
						statecode = "关闭";
					}else{
						statecode = "异常";
					}
					return statecode;
				}
				if(columnId.equals("subtitle")) {
					String subtitle = (String) property.getValue();
					if(null!=subtitle && subtitle.length() > 40) {
						subtitle =subtitle.substring(0,40)+"...";
					}
					return subtitle;
				}
				if(columnId.equals("createTime")) {
					String st = "";
					try {
						if(null!=property.getValue()){
							Date d = (Date) property.getValue();
							SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
							st = dateFormat.format(d);
						}
					} catch (Exception e) {
						logger.error("日期转换异常_createFormatColumnTable_LLXXYY", e);
					}
					return st;
				}
				if(columnId.equals("showcode")) {
					int showCode = -1;
					String showCodeStr = "";
					if(null != property.getValue()){
						showCode =  (Integer) property.getValue();
					}
					if(showCode == 2) {
						showCodeStr = "全部模式";
					}else  {
						showCodeStr = "跳转模式";
					}
					return showCodeStr;
				}
				return super.formatPropertyValue(rowId, columnId, property);
			}
		};
	}

	/** 创建表格下方的各种操作组件（添加、编辑、删除按钮，以及表格的翻页组件） */
	private void createTableFooterComponents() {
		HorizontalLayout footerHLayout = new HorizontalLayout();
		footerHLayout.setSpacing(true);
		footerHLayout.setWidth("100%");
		this.addComponent(footerHLayout);
		
		HorizontalLayout leftFooter = new HorizontalLayout();
		leftFooter.setSpacing(true);
		footerHLayout.addComponent(leftFooter);
		
		bt_add_questionnaire = new Button("添加问卷", this);
		bt_add_questionnaire.setImmediate(true);
		leftFooter.addComponent(bt_add_questionnaire);
		
		bt_edit_questionnaire = new Button("编辑问卷", this);
		bt_edit_questionnaire.setImmediate(true);
		bt_edit_questionnaire.setEnabled(false);
		leftFooter.addComponent(bt_edit_questionnaire);
		
		bt_delete_questionnaire = new Button("删除问卷", this);
		bt_delete_questionnaire.setImmediate(true);
		bt_delete_questionnaire.setEnabled(false);
		leftFooter.addComponent(bt_delete_questionnaire);
		
		bt_add_question = new Button("添加问题", this);
		bt_add_question.setImmediate(true);
		bt_add_question.setEnabled(false);
		leftFooter.addComponent(bt_add_question);
		
		bt_editSkip_question = new Button("修改/跳转问题", this);
		bt_editSkip_question.setImmediate(true);
		bt_editSkip_question.setEnabled(false);
		leftFooter.addComponent(bt_editSkip_question);
		
		bt_change_stateCode = new Button("执行/预览问卷", this);
		bt_change_stateCode.setImmediate(true);
		bt_change_stateCode.setEnabled(false);
		leftFooter.addComponent(bt_change_stateCode);
		
		bt_close_questionnaire = new Button("关闭问卷", this);
		bt_close_questionnaire.setImmediate(true);
		bt_close_questionnaire.setEnabled(false);
		leftFooter.addComponent(bt_close_questionnaire);
		
		bt_report_questionnaire = new Button("问卷报告", this);
		bt_report_questionnaire.setImmediate(true);
		bt_report_questionnaire.setEnabled(false);
		leftFooter.addComponent(bt_report_questionnaire);
		
		
		/*bt_dotest = new Button("测试", this);
		bt_dotest.setImmediate(true);
 		leftFooter.addComponent(bt_dotest);*/
		
		initializeSql();	// 初始化搜索语句和分页组件
		
		flip = new FlipOverTableComponent<Questionnaire>(Questionnaire.class, questionnaireService, table, searchListJpql, searchCountJpql, null);
		flip.setPageLength(PAGE_LENGTH, false);
		table.setVisibleColumns(COL_PROPERTIES);
		table.setColumnHeaders(COL_HEADERS);
 		footerHLayout.addComponent(flip);
 	}
	
	@Override
	public void attach() {
		super.attach();
		this.keyword.setValue("");
		initializeSql();
		updateTable(true);
	}
	
	/** 根据不同的需求，初始化查询语句 */
	private void initializeSql() {
		String keywordValue = (String) keyword.getValue();
 		searchCountJpql = "Select count(s) from Questionnaire as s where s.mainTitle like '%" + keywordValue + "%' and  s.domain.id = " +domain.getId();//+ "  and  and s.sipType in " +sipTypeSql+ "
		searchListJpql = searchCountJpql.replaceFirst("count\\(s\\)", "s") ;//
		searchListJpql += " order by s.createTime desc";
	}
	
	/**
	 * 刷新显示分机的表格
	 * @param isToFirst 刷新后是否跳转至第一页   true 第一页 ；false 当前页 */
	public void updateTable(Boolean isToFirst) {
		if (flip != null) {
			flip.setSearchSql(searchListJpql);
			flip.setCountSql(searchCountJpql);
			if (isToFirst) {
				flip.refreshToFirstPage();
			} else {
				flip.refreshInCurrentPage();
			}
		}
	}
	/** 本页面表格组件*/
	public Table getTable() {
		return table;
	}

	//---------------------需要实现的接口方法------开始-----------------------------------
	/** 表格选择改变的监听器，设置按钮样式，状态信息*/
	@Override
	public void valueChange(ValueChangeEvent event) {
		Property source = event.getProperty();
		if(source == table) {
			Questionnaire target = (Questionnaire) table.getValue();
			bt_edit_questionnaire.setEnabled(target != null);
			bt_change_stateCode.setEnabled(target != null);
			bt_close_questionnaire.setEnabled(target != null);
			bt_add_question.setEnabled(target != null);
			bt_editSkip_question.setEnabled(target != null);
			bt_report_questionnaire.setEnabled(target != null);
			bt_delete_questionnaire.setEnabled(target != null);
		} else if(source == keyword) {
			bt_search.click();
		}
	}

	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if(source == bt_search) {
			initializeSql();
			flip.setCountSql(searchCountJpql);
			flip.setSearchSql(searchListJpql);
			flip.refreshToFirstPage();
			table.setValue(null);
		}else if(source == bt_add_questionnaire) {
			showAddWindow();
		} else if(source == bt_edit_questionnaire) {
			showEditWindow();
		}else if(source == bt_delete_questionnaire) {
			ShowWindow showWindow = new ShowWindow(this,"deleteQuestionnaire", "删除问卷：会删除包括问卷，问题，选项，用户问卷情况，项目与问卷的关系，客户所做问卷与录音的关联关系等相关数据，不可恢复，您确定要是删除问卷吗？", "450px", "60px");
			this.getApplication().getMainWindow().addWindow(showWindow);
		}else if(source == bt_change_stateCode) {
			/*ShowWindow showWindow = new ShowWindow(this,"stateCodeQuestionnaire", "执行问卷：在执行状态下问卷不能修改，您确定要执行问卷吗？", "450px", "60px");
			this.getApplication().getMainWindow().addWindow(showWindow);*/
			showQuestionnaireBrowseWindow();
		}else if(source == bt_add_question) {
			showAddQuestionWindow();
		}else if(source == bt_editSkip_question){
			showEditSkipQuestionWindow();
		}else if(source == bt_close_questionnaire) {
			ShowWindow showWindow = new ShowWindow(this,"closeQuestionnaire", "您确定要是关闭问卷吗？", "450px", "60px");
			this.getApplication().getMainWindow().addWindow(showWindow);
		}else if(source == bt_report_questionnaire){
			showReportWindow();
		}/*else if(source == bt_dotest){
			if(null == realTimeWindow){
				realTimeWindow = new TestRealTimeWindow();
			}
			//ShareData.testRealTimeWindowMap.put(loginUser.getId(), realTimeWindow);
			this.getApplication().getMainWindow().addWindow(realTimeWindow);
		}*/
	}
	
	/**	显示问卷浏览窗口	*/
	private void showQuestionnaireBrowseWindow(){
		Questionnaire target = (Questionnaire) table.getValue();
		if (null != target) {
			if(target.getShowcode() == 2){
				if(showQuestionnaireBrowseShowAll == null){
					showQuestionnaireBrowseShowAll = new ShowQuestionnaireBrowseShowAll(this);
				}
				this.getWindow().addWindow(showQuestionnaireBrowseShowAll);
				showQuestionnaireBrowseShowAll.refreshComponentInfo(target);
			}
			else{
				if(showQuestionnaireBrowseWinodw==null){
					showQuestionnaireBrowseWinodw=new ShowQuestionnaireBrowse(this);
				}
				this.getWindow().addWindow(showQuestionnaireBrowseWinodw);
				showQuestionnaireBrowseWinodw.refreshComponentInfo(target);
			}
		}else{
			this.getApplication().getMainWindow().showNotification("没有选中的问卷，请选择！", Notification.TYPE_WARNING_MESSAGE);
		}
	}
 
	/** 显示添加问题窗口	*/
	private void showAddQuestionWindow() {
		Questionnaire target = (Questionnaire) table.getValue();
		if (null != target) {
			if(questionnaireService.isDesign(target.getId())){//在设计状态
				addQuestioWindow = new AddQuestion(this);
				this.getWindow().addWindow(addQuestioWindow);
			}else{
				this.getApplication().getMainWindow().showNotification("问卷不在设计状态不能添加问题！", Notification.TYPE_WARNING_MESSAGE);
			}
		}else{
			this.getApplication().getMainWindow().showNotification("没有选中的问卷，请选择！", Notification.TYPE_WARNING_MESSAGE);
		}
	}
	
	/** 显示添加 */
	public void showNextAddQuestionWindow(){
 		this.getWindow().removeWindow(addQuestioWindow);
 		addQuestioWindow=new AddQuestion(this);
		this.getWindow().addWindow(addQuestioWindow);
	}
	
	/** 显示问卷报表判断	*/
	private void showReportWindow(){
		Questionnaire target = (Questionnaire) table.getValue();
		if (null != target) {
			if(questionnaireService.isDesign(target.getId())){//在设计状态
				this.getApplication().getMainWindow().showNotification("问卷在设计状态不能产生报表！", Notification.TYPE_WARNING_MESSAGE);
			}else{
				showReport(target.getId());
			}
		}else{
			this.getApplication().getMainWindow().showNotification("没有选中的问卷，请选择！", Notification.TYPE_WARNING_MESSAGE);
		}
	}
	/** 显示问卷报表	*/
	private void showReport(Long questionnaireId){
		if(showQuestionnaireReportWindow==null){
			showQuestionnaireReportWindow = new ShowQuestionnaireReport();
		}
		this.getWindow().addWindow(showQuestionnaireReportWindow);
		showQuestionnaireReportWindow.refreshComponentInfo(questionnaireId);
	}
	/**关闭添加问卷窗口*/
	public void closeQuestionWindow() {
		this.getWindow().removeWindow(addQuestioWindow);
	}
	/** 显示添加问卷窗口*/
	private void showAddWindow() {
		if(addQuestionnaireWindow==null){
			addQuestionnaireWindow=new AddQuestionnaire(this);
		}
		this.getWindow().addWindow(addQuestionnaireWindow);
	}
	/**
	 * 显示编辑问卷窗口*/
	protected void showEditWindow() {
		if(editQuestionnaireWindow==null){
			editQuestionnaireWindow = new EditQuestionnaire(this);
		}
		this.getWindow().addWindow(editQuestionnaireWindow);
	}
	/**  删除问卷*/
	public void deleteQuestionnaire(boolean change) {
		if(change){
			Object target =  table.getValue();
			try {
				if(target != null){
					Questionnaire q = (Questionnaire)target;
					questionnaireService.delQuestionnaireById(q.getId());
					this.getApplication().getMainWindow().showNotification("问卷删除成功！", Notification.TYPE_HUMANIZED_MESSAGE);
				}
			} catch (Exception e) {
				e.printStackTrace();
				logger.error("问卷删除异常_deleteQuestionnaire_LLXXYY", e);
				this.getApplication()
						.getMainWindow()
						.showNotification("问卷删除异常!",
								Notification.TYPE_ERROR_MESSAGE);
				
			}finally{
				this.updateTable(true);
				this.getTable().setValue(null);
			}
		}
	}
	
	/**  关闭问卷报表*/
	public void closeQuestionnaire(boolean change){
		if(change){
			Object target =  table.getValue();
			try {
				if(target != null){
					Questionnaire q = (Questionnaire)target;
					questionnaireService.closeQuestionnaireById(q.getId());
					this.getApplication().getMainWindow().showNotification("关闭问卷成功！", Notification.TYPE_HUMANIZED_MESSAGE);
				}
			} catch (Exception e) {
				e.printStackTrace();
				logger.error("关闭问卷异常_closeQuestionnaire_LLXXYY", e);
				this.getApplication()
						.getMainWindow()
						.showNotification("关闭问卷异常!",
								Notification.TYPE_ERROR_MESSAGE);
			}finally{
				this.updateTable(true);
				this.getTable().setValue(null);
			}
		}
	}
	/** 显示编辑问题列表窗口*/
	private void showEditSkipQuestionWindow(){
		Questionnaire target = (Questionnaire) table.getValue();
		if (null != target) {
			if(questionnaireService.isDesign(target.getId())){
				if(editSkipQuestionWindow==null){
					editSkipQuestionWindow=new EditSkipQuestion(this);
				}
				this.getWindow().addWindow(editSkipQuestionWindow);
			}else{
				this.getApplication().getMainWindow().showNotification("问卷不在设计状态不能编辑问题！", Notification.TYPE_WARNING_MESSAGE);
			}
		}else{
			this.getApplication().getMainWindow().showNotification("没有选中的问卷，请选择！", Notification.TYPE_WARNING_MESSAGE);
		}
	}
	/** 关闭编辑问题列表窗口*/
	public void deleteEditSkipQuestionWindow(){
		this.getWindow().removeWindow(editSkipQuestionWindow);
	}
	
	/** Action.Handler 实现方法 */
	@Override
	public Action[] getActions(Object target, Object sender) {
		if(target == null) {
			return new Action[]{AddCustomer};
		}
		return new Action[]{AddCustomer, EditCustomer, DeleteCustomer};
	}
	
	/**	页面右击时间 */
	@Override
	public void handleAction(Action action, Object sender, Object target) {
		table.setValue(null);
		table.select(target);
		if (AddCustomer == action) {
			bt_add_questionnaire.click();
		} else if (EditCustomer == action) {
			bt_edit_questionnaire.click();
		} else if (DeleteCustomer == action) {
			bt_delete_questionnaire.click();
		}
	}
	//---------------------需要实现的接口方法------结束-----------------------------------
}
