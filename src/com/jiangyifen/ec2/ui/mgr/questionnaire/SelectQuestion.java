package com.jiangyifen.ec2.ui.mgr.questionnaire;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.jiangyifen.ec2.entity.Question;
import com.jiangyifen.ec2.service.eaoservice.QuestionService;
import com.jiangyifen.ec2.ui.FlipOverTableComponent;
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
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class SelectQuestion extends Window implements  Property.ValueChangeListener,Action.Handler,ClickListener  {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2401348833599382342L;
	
	private static final int PAGELENGTH = 10;
	
	private final Object[] COL_PROPERTIES = new Object[] {"id","title"};
	private final String[] COL_HEADERS = new String[] { "编号","问题标题"};
 	
	private Table table;
	
	private TextField item_spik;
	
	//搜索组件
	private TextField keyword;
	private Button search;

	private Button saveSpik;
	private Button cancel;
	private Button saveNull;
	private Label lb_msg;
	
	private TextArea tf_title;
	
	// 分机表格组件
	private String countSql;
	private String searchSql;
	
	private Long questionnaireid = new Long(-1);
	
	private FlipOverTableComponent<Question> flip;
	
	private QuestionService questionService;
 	
	private EditSkipQuestion editSkipQuestion;
	public SelectQuestion(TextField item_spik,EditSkipQuestion editSkipQuestion){
		this.editSkipQuestion = editSkipQuestion;
		this.item_spik = item_spik;
		initWindow();
		
		initSpringContext();
		
		VerticalLayout 		mainLayout 		=	createMainLayoutComponents();		//总布局栏
		HorizontalLayout 	infoLayout 		= 	createinfoLayoutComponents();		//信息栏
		HorizontalLayout 	searchLayout 	= 	createSearchLayoutComponents();		//检索栏
		VerticalLayout 		tableLayout 	= 	createTableLayoutComponents();		//表格栏
		HorizontalLayout 	showLayout 		= 	createShowLayoutComponents();		//检索栏
		HorizontalLayout 	operateLayout 	= 	createOperateLayoutComponents();	//操作栏
		
		mainLayout.addComponent(infoLayout);
		mainLayout.addComponent(searchLayout);
		mainLayout.addComponent(tableLayout);
		mainLayout.addComponent(showLayout);
		mainLayout.addComponent(operateLayout);
		
		this.addComponent(mainLayout);
	}
	 

	private HorizontalLayout createOperateLayoutComponents() {
		HorizontalLayout operateLayout_rt = new HorizontalLayout();
		operateLayout_rt.setSpacing(true);
		operateLayout_rt.setSizeFull();
		
		saveSpik = new Button("保存跳转",this);
		saveSpik.setStyleName("default");
		operateLayout_rt.addComponent(saveSpik);
		
		saveNull =  new Button("保存为空",this);
		saveNull.setStyleName("default");
		operateLayout_rt.addComponent(saveNull);
		
		// 取消按钮
		cancel = new Button("取消", this);
		operateLayout_rt.addComponent(cancel);
		
		lb_msg = new Label("",Label.CONTENT_XHTML);
		lb_msg.setWidth("-1px");
		operateLayout_rt.addComponent(lb_msg);
		
		return operateLayout_rt;
	}


	private HorizontalLayout createShowLayoutComponents() {
		HorizontalLayout infoLayout_rt = new HorizontalLayout();
		infoLayout_rt.setSpacing(true);
		infoLayout_rt.addComponent(new Label("问题标题："));
		tf_title = new TextArea();
		tf_title.setRows(2);
		tf_title.setColumns(25);
		infoLayout_rt.addComponent(tf_title);
		return infoLayout_rt;
	}


	private HorizontalLayout createinfoLayoutComponents() {
		HorizontalLayout infoLayout_rt = new HorizontalLayout();
		infoLayout_rt.setSpacing(true);
		return infoLayout_rt;
	}

	private void  initWindow(){
		this.center();
		this.setModal(true);
		this.setResizable(false);
		this.setCaption("选择跳转至的问题");
		this.setWidth("450px");
		this.setHeight("450px");
	}
	
	private void initSpringContext() {
		questionService = SpringContextHolder.getBean("questionService");
	}
	
	private VerticalLayout createTableLayoutComponents() {
		VerticalLayout tableLayout_rt = new VerticalLayout();
		tableLayout_rt.setSpacing(true);
		tableLayout_rt.setWidth("100%");
		initTableProperty();
		
		
		initializeSql();
		initFlip();
		initColumnsHeaders();
		
		tableLayout_rt.addComponent(table);
		tableLayout_rt.addComponent(flip);
		
		return tableLayout_rt;
	}

	private void initTableProperty(){
		table = createFormatColumnTable();
		table.setSizeFull();
		table.addListener(this);
		table.setImmediate(true);
		table.setSelectable(true);
		table.setNullSelectionAllowed(true);
		table.setRowHeaderMode(Table.ROW_HEADER_MODE_INDEX);
		table.addStyleName("striped");
		table.setPageLength(PAGELENGTH);
		table.setHeight("210px");
	}
	
	private void initColumnsHeaders(){
		table.setVisibleColumns(COL_PROPERTIES);
		table.setColumnHeaders(COL_HEADERS);
	}
	
	private void initFlip(){
		flip = new FlipOverTableComponent<Question>(Question.class, questionService, table, searchSql, countSql, null);
		flip.setPageLength(PAGELENGTH, true);
	}

	private HorizontalLayout createSearchLayoutComponents() {
		HorizontalLayout searchLayout_rt = new HorizontalLayout();
		searchLayout_rt.setSpacing(true);
		
		Label caption = new Label("关键字：");
		caption.setWidth("-1px");
		searchLayout_rt.addComponent(caption);
		
		keyword = new TextField();
		keyword.setImmediate(true);
		keyword.setInputPrompt("请输入搜索关键字");
		keyword.setDescription("按关键字进行搜索");
		keyword.setStyleName("search");
		keyword.addListener(this);
		searchLayout_rt.addComponent(keyword);
		
		search = new Button("搜索", this);
		search.setImmediate(true);
		searchLayout_rt.addComponent(search);
		
		return searchLayout_rt;
	}


	private VerticalLayout createMainLayoutComponents() {
		VerticalLayout 	mainLayout_rt = new VerticalLayout();
		mainLayout_rt.setMargin(false, true, true, true);
		mainLayout_rt.setSpacing(true);
		return mainLayout_rt;
	}


	
	/**
	 *  创建格式化 了 显示内容的 Table对象
	 */
	private Table createFormatColumnTable() {
		return new Table() {
			private static final long serialVersionUID = -2615629931409788749L;
			@Override
			protected String formatPropertyValue(Object rowId, Object columnId, Property property) {
				//Question qtn = (Question)rowId;
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
	
	
	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if(search == source){
			initializeSql();
			flip.setCountSql(countSql);
			flip.setSearchSql(searchSql);
			flip.refreshToFirstPage();
			table.setValue(null);
		}
		if(source == saveSpik) {
 			Question target = (Question) table.getValue();
			if(null!=target){
				Question squestion =  (Question)editSkipQuestion.getTable().getValue();
				if(null!=squestion){
					if(squestion.getId().equals(target.getId())){
						this.lb_msg.setValue("<font color='red'>本题不能跳转至问题，请选其他</font>");
					}else{
						this.lb_msg.setValue("");
						this.item_spik.setValue(target.getId());
						this.getApplication().getMainWindow().removeWindow(this);
					}
				}else{
					this.lb_msg.setValue("<font color='red'>未选中问题，请返回选择问题</font>");
				}
				
			}else{
				this.lb_msg.setValue("<font color='red'>请选择问题</font>");
			}
		}
		
		if(source == saveNull){
			this.lb_msg.setValue("");
			this.item_spik.setValue("");
			this.getApplication().getMainWindow().removeWindow(this);
		}
		
		if(source == cancel){
			this.lb_msg.setValue("");
 			this.getApplication().getMainWindow().removeWindow(this);
		}
	}

	
	/**
	 * 根据不同的需求，初始化查询语句
	 */
	private void initializeSql() {
		String keywordValue = (String) keyword.getValue();
 		countSql = "Select count(s) from Question as s where s.title like '%" + keywordValue + "%' and s.questionnaire.id = "+questionnaireid;// s.domain.id = " +domain.getId()+ "  and  and s.sipType in " +sipTypeSql+ "
		searchSql = countSql.replaceFirst("count\\(s\\)", "s") ;//
		searchSql += " order by s.createTime asc";
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
	public void valueChange(ValueChangeEvent event) {
		Property source = event.getProperty();
		if(source == table) {
			Question target = (Question) table.getValue();
			if(null!=target){
				tf_title.setValue(target.getTitle());
			}
		} 
	}

	@Override
	public Action[] getActions(Object target, Object sender) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public void handleAction(Action action, Object sender, Object target) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void attach() {
		super.attach();
 		Question old_question = (Question) editSkipQuestion.getTable().getValue();
 		questionnaireid = old_question.getQuestionnaire().getId();
 		initializeSql();
 		initTableUIAndData();
	}
	
	private void initTableUIAndData() {
		flip.setCountSql(countSql);
		flip.setSearchSql(searchSql);
		flip.refreshToFirstPage();
		table.setValue(null);
	}
}
