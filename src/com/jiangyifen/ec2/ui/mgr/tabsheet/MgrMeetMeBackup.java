package com.jiangyifen.ec2.ui.mgr.tabsheet;

import java.util.List;

import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.entity.enumtype.ExecuteType;
import com.jiangyifen.ec2.service.common.CommonService;
import com.jiangyifen.ec2.ui.mgr.util.DragAndDropSupport;
import com.jiangyifen.ec2.ui.mgr.util.StyleConfig;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

/**
 * 管理员会议室查看页面
 * @author chb
 * 
 */
@SuppressWarnings("serial")
public class MgrMeetMeBackup extends VerticalLayout implements
		Button.ClickListener {
	// 分机输入
	private TextField extenInputField;
	private Button confirm;

	// 手机输入
	private TextField phoneNumberInputField;
	private Button invitePhoneNumber;
	
	/**
	 * 坐席选择区域
	 */
	//关键字和搜索按钮
	//左
	private TextField leftKeyWord;
	private Button leftSearch;
	//右
	private TextField rightKeyWord;
	private Button rightSearch;
	
	//中间的添加按钮
	private Button addAll;
	private Button add;
	private Button remove;
	private Button removeAll;
	
	//表格
	private Object[] columns = new String[] { "id", "username","empNo"};
	private String[] headers = new String[] { "ID", "用户名","工号"};
	private Table leftTable;
	private CommonService commonService;
	private Domain domain;

	private Table rightTable;
	private BeanItemContainer<User> rightTableContainer;
	private BeanItemContainer<User> leftTableContainer;

	//下面的添加和取消按钮
	private Button assign;
	private Button cancel;
	
	public MgrMeetMeBackup() {
		commonService=SpringContextHolder.getBean("commonService");
		domain=SpringContextHolder.getDomain();
		this.setSizeFull();
		this.setMargin(true);

		// 约束组件，使组件紧密排列
		VerticalLayout constrantLayout = new VerticalLayout();
		constrantLayout.setSpacing(true);
		this.addComponent(constrantLayout);

		// 分机输入区域
		constrantLayout.addComponent(buildExtenInputLayout());

		// 坐席选择区域
		constrantLayout.addComponent(buildCsrSelectLayout());
		
		//外部手机号码输入区
		constrantLayout.addComponent(buildPhoneNumberInputLayout());
	}

	/**
	 * 创建坐席选择输出
	 * @return
	 */
	private VerticalLayout buildCsrSelectLayout() {
		//添加Window内最大的Layout
		VerticalLayout windowContent=new VerticalLayout();
		windowContent.setSizeUndefined();
		windowContent.setMargin(false, true, true, true);
		windowContent.setSpacing(true);
		windowContent.setStyleName(StyleConfig.VERTICAL_STYLE);
		
		/*=====中间的两个表格和按钮=========*/
		HorizontalLayout tempTablesLayout=buildTablesLayout();
		windowContent.addComponent(tempTablesLayout);
		windowContent.setExpandRatio(tempTablesLayout, 1.0f);
		
		/*=========下面的按钮输出===========*/
		HorizontalLayout tempButtonsLayout=buildButtonsLayout();
		windowContent.addComponent(tempButtonsLayout);
		windowContent.setComponentAlignment(tempButtonsLayout, Alignment.BOTTOM_RIGHT);
		
		//添加拖拽支持
		DragAndDropSupport.addDragAndDropSupport(leftTable, rightTable);
		return windowContent;
	}

	/**
	 * 创建主要输出部分，中间的两个Table组件和Table按钮组件
	 * @return
	 */
	private HorizontalLayout buildTablesLayout() {
		HorizontalLayout tablesLayout=new HorizontalLayout();
		tablesLayout.setSpacing(true);
		//要想剔除左侧Table包含的右侧Table的Item，必须先创建右侧组件，再创建左侧组件
		VerticalLayout tempRight = buildRight();
		VerticalLayout tempLeft = buildLeft();

		//左侧输出
		tablesLayout.addComponent(tempLeft);
		tablesLayout.setExpandRatio(tempLeft, 8.0f);
		
		//中间输出
		VerticalLayout tempCenter = buildCenter();
		tablesLayout.addComponent(tempCenter);
		tablesLayout.setExpandRatio(tempCenter, 1.5f);
		
		//右侧输出
		tablesLayout.addComponent(tempRight);
		tablesLayout.setExpandRatio(tempRight, 8.0f);
		return tablesLayout;
	}

	/**
	 * 添加Csr左侧输出
	 * @return
	 */
	private VerticalLayout buildLeft() {
		//左侧
		VerticalLayout leftLayout=new VerticalLayout();
		leftLayout.setSpacing(true);
		
		//搜索组件
		leftLayout.addComponent(buildLeftSearch());
		
		//Table的状态Label
		leftLayout.addComponent(new Label("全部CSR"));
		
		//表格组件 和翻页组件
		leftLayout.addComponent(buildLeftTable());
		
		return leftLayout;
	}

	/**
	 * 添加Csr右侧输出
	 * @return
	 */
	private VerticalLayout buildRight() {
		VerticalLayout rightLayout=new VerticalLayout();
		rightLayout.setSpacing(true);
		
		//搜索组件
		rightLayout.addComponent(buildRightSearch());
		
		//Table的状态Label
		rightLayout.addComponent(new Label("已选CSR"));
		
		//表格组件
		rightLayout.addComponent(buildRightTable());
		
		return rightLayout;
	}

	/**
	 * 添加Csr中间输出
	 * @return
	 */
	private VerticalLayout buildCenter() {
		VerticalLayout centerLayout=new VerticalLayout();
		centerLayout.setSpacing(true);
		//占位组件
		centerLayout.addComponent(new Label("&nbsp&nbsp",Label.CONTENT_XHTML));
		centerLayout.addComponent(new Label("&nbsp&nbsp",Label.CONTENT_XHTML));
		centerLayout.addComponent(new Label("&nbsp&nbsp",Label.CONTENT_XHTML));
		
		//按钮组件
		addAll=new Button(">>>");
		addAll.addListener(this);
		centerLayout.addComponent(addAll);
		centerLayout.setComponentAlignment(addAll, Alignment.MIDDLE_CENTER);
		
		add=new Button(">>");
		add.addListener(this);
		centerLayout.addComponent(add);
		centerLayout.setComponentAlignment(add, Alignment.MIDDLE_CENTER);

		remove=new Button("<<");
		remove.addListener(this);
		centerLayout.addComponent(remove);
		centerLayout.setComponentAlignment(remove, Alignment.MIDDLE_CENTER);
		
		removeAll=new Button("<<<");
		removeAll.addListener(this);
		centerLayout.addComponent(removeAll);
		centerLayout.setComponentAlignment(removeAll, Alignment.MIDDLE_CENTER);
		
		return centerLayout;
	}
	/**
	 * 创建此窗口最下边按钮的输出
	 * @return
	 */
	private HorizontalLayout buildButtonsLayout() {
		HorizontalLayout buttonsLayout=new HorizontalLayout();
		buttonsLayout.setSpacing(true);
		buttonsLayout.setWidth("23%");
		//添加
		assign=new Button("确定");
		assign.addListener((Button.ClickListener)this);
		buttonsLayout.addComponent(assign);
		buttonsLayout.setComponentAlignment(assign, Alignment.MIDDLE_CENTER);
		//取消
		cancel=new Button("取消");
		cancel.addListener((Button.ClickListener)this);
		buttonsLayout.addComponent(cancel);
		buttonsLayout.setComponentAlignment(cancel, Alignment.MIDDLE_CENTER);
		return buttonsLayout;
	}
	
	/**
	 * 由 buildLeft 调用，左侧的搜索组件
	 * @return
	 */
	private HorizontalLayout buildLeftSearch() {
		HorizontalLayout leftSearchLayout=new HorizontalLayout();
		leftSearchLayout.setSpacing(true);
		
		HorizontalLayout constraintLayout=new HorizontalLayout();
		constraintLayout.addComponent(new Label("关键字"));
		leftKeyWord=new TextField();//关键字
		leftKeyWord.setStyleName("search");
		leftKeyWord.setWidth("5em");
		leftKeyWord.setInputPrompt("关键字");
		constraintLayout.addComponent(leftKeyWord);
		leftSearchLayout.addComponent(constraintLayout);
		
		// 搜索按钮
		leftSearch = new Button("搜索");
		leftSearch.addListener(this);
		leftSearchLayout.addComponent(leftSearch);
		return leftSearchLayout;
	}
	/**
	 * 由 buildLeft 调用，左侧表格和翻页组件,此时sqlSelect和sqlCount应该已经被初始化
	 * @return
	 */
	private VerticalLayout buildLeftTable() {
		VerticalLayout tableLayout=new VerticalLayout();
		tableLayout.setWidth("100%");
		//表格
		leftTable=new Table();
		leftTable.setStyleName("striped");
		leftTable.setSelectable(true);
		leftTable.setMultiSelect(true);
		leftTable.setWidth("100%");
		tableLayout.addComponent(leftTable);
		
		leftTableContainer=new BeanItemContainer<User>(User.class);
		leftTable.setContainerDataSource(leftTableContainer);
		leftTable.setPageLength(10);
		leftTable.setVisibleColumns(columns);
		leftTable.setColumnHeaders(headers);
		
		return tableLayout;
	}

	/**
	 * 由 buildRight 调用，右侧的搜索组件
	 * @return
	 */
	private HorizontalLayout buildRightSearch() {
		HorizontalLayout rightSearchLayout=new HorizontalLayout();
		rightSearchLayout.setSpacing(true);
		
		HorizontalLayout constraintLayout=new HorizontalLayout();
		constraintLayout.addComponent(new Label("关键字"));
		rightKeyWord=new TextField();//关键字
		rightKeyWord.setWidth("5em");
		rightKeyWord.setStyleName("search");
		rightKeyWord.setInputPrompt("关键字");
		constraintLayout.addComponent(rightKeyWord);
		rightSearchLayout.addComponent(constraintLayout);
		
		// 搜索按钮
		rightSearch = new Button("搜索");
		rightSearch.addListener(this);
		rightSearchLayout.addComponent(rightSearch);
		return rightSearchLayout;
	}
	/**
	 * 由buildRight调用，创建右侧表格组件
	 * @return
	 */
	private VerticalLayout buildRightTable() {
		VerticalLayout tableLayout=new VerticalLayout();
		tableLayout.setWidth("100%");
		//表格
		rightTable=new Table();
		rightTable.setStyleName("striped");
		rightTable.setPageLength(10);
		rightTable.setSelectable(true);
		rightTable.setMultiSelect(true);
		rightTable.setWidth("100%");
		rightTableContainer= new BeanItemContainer<User>(User.class);
		rightTable.setContainerDataSource(rightTableContainer);
		rightTable.setVisibleColumns(columns);
		rightTable.setColumnHeaders(headers);
		tableLayout.addComponent(rightTable);
		return tableLayout;
	}

	@Override
	public void attach() {
		update();
	}
	//更新用户显示
	@SuppressWarnings("unchecked")
	public void update(){
		String sql="select u from User u where u.domain.id="+domain.getId();
		List<User> entitys = (List<User>)commonService.excuteSql(sql, ExecuteType.RESULT_LIST);
		
		leftTableContainer.removeAllItems();
		rightTableContainer.removeAllItems();
		
		
		leftTableContainer.addAll(entitys);
	}
	
	/**
	 * 创建分机输入区域
	 * 
	 * @return
	 */
	private HorizontalLayout buildExtenInputLayout() {
		HorizontalLayout mainLayout = new HorizontalLayout();

		// 关键字Label
		mainLayout.addComponent(new Label("请输入分机号："));

		// 关键字组件
		extenInputField = new TextField();
		mainLayout.addComponent(extenInputField);

		// 搜索按钮的约束组件
		HorizontalLayout buttonConstraintLayout = new HorizontalLayout();
		mainLayout.addComponent(buttonConstraintLayout);

		// 搜索按钮
		confirm = new Button("加入会议室");
		confirm.addListener(this);
		buttonConstraintLayout.addComponent(confirm);
		return mainLayout;
	}

	/**
	 * 外部手机号码输入区
	 * @return
	 */
	private HorizontalLayout buildPhoneNumberInputLayout() {
		HorizontalLayout mainLayout = new HorizontalLayout();
		
		// 关键字Label
		mainLayout.addComponent(new Label("请输入手机号："));
		
		// 关键字组件
		phoneNumberInputField = new TextField();
		mainLayout.addComponent(phoneNumberInputField);
		
		// 搜索按钮的约束组件
		HorizontalLayout buttonConstraintLayout = new HorizontalLayout();
		mainLayout.addComponent(buttonConstraintLayout);
		
		// 搜索按钮
		invitePhoneNumber = new Button("邀请");
		invitePhoneNumber.addListener(this);
		buttonConstraintLayout.addComponent(invitePhoneNumber);
		return mainLayout;
	}

	// 监听按钮点击事件
	@Override
	public void buttonClick(ClickEvent event) {
		if (event.getButton() == confirm) {
			//判断可用性，加入会议室
		}else if (event.getButton() == invitePhoneNumber) {
			//向手机发出加入会议室的邀请
		}
	}

}
