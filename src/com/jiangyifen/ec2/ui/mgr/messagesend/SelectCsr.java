package com.jiangyifen.ec2.ui.mgr.messagesend;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.service.eaoservice.UserService;
import com.jiangyifen.ec2.ui.mgr.tabsheet.NoticeSend;
import com.jiangyifen.ec2.ui.mgr.util.DragAndDropSupport;
import com.jiangyifen.ec2.ui.mgr.util.StyleConfig;
import com.jiangyifen.ec2.ui.util.NotificationUtil;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.filter.Like;
import com.vaadin.data.util.filter.Or;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
/**
 * 为项目添加Csr
 * @author chb
 *
 */
@SuppressWarnings("serial")
public class SelectCsr extends Window implements Button.ClickListener{
/**
 * 主要组件输出
 */
	//关键字和搜索按钮
	private TextField leftKeyWord;
	private Button leftSearch;
	private TextField rightKeyWord;
	private Button rightSearch;
	
	//中间的添加按钮
	private Button addAll;
	private Button add;
	private Button remove;
	private Button removeAll;
	
	//表格
	private Object[] columns = new String[] { "id", "username","empNo","realName", "department.name"};
	private String[] headers = new String[] { "ID", "用户名","工号","姓名","部门"};
	private Table leftTable;
	private UserService userService;

	private Table rightTable;
	private BeanItemContainer<User> rightTableContainer;
	private BeanItemContainer<User> leftTableContainer;
	
	//下面的添加和取消按钮
	private Button addCsr;
	private Button cancel;
/**
 * 其他组件
 */
	//持有调用它的组件引用ProjectControl,以刷新父组件
	private NoticeSend messageSend;
	
	public SelectCsr(NoticeSend messageSend) {
		this.center();
		this.setCaption("选择接受消息的CSR");
		this.setModal(true);
		this.messageSend=messageSend;
		
		//添加Window内最大的Layout
		VerticalLayout windowContent=new VerticalLayout();
		windowContent.setSizeUndefined();
		windowContent.setMargin(false,true,true,true);
		windowContent.setSpacing(true);
		windowContent.setStyleName(StyleConfig.VERTICAL_STYLE);
		this.setContent(windowContent);
		
		//中间的两个表格和按钮
		HorizontalLayout tempTablesLayout=buildTablesLayout();
		windowContent.addComponent(tempTablesLayout);
		windowContent.setExpandRatio(tempTablesLayout, 1.0f);
		
		//下面的按钮输出
		HorizontalLayout tempButtonsLayout=buildButtonsLayout();
		windowContent.addComponent(tempButtonsLayout);
		windowContent.setComponentAlignment(tempButtonsLayout, Alignment.BOTTOM_RIGHT);
		
		//设置按钮样式
		this.setButtonsStyle(StyleConfig.BUTTON_STYLE);
		//添加拖拽支持
		DragAndDropSupport.addDragAndDropSupport(leftTable, rightTable);
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
		buttonsLayout.setWidth("25%");
		//发送
		addCsr=new Button("添加");
		addCsr.addListener((Button.ClickListener)this);
		buttonsLayout.addComponent(addCsr);
		buttonsLayout.setComponentAlignment(addCsr, Alignment.MIDDLE_CENTER);
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
		leftSearchLayout.addComponent(new Label("关键字"));
		leftKeyWord=new TextField();//关键字
		leftKeyWord.setWidth("5em");
		leftKeyWord.setStyleName("search");
		leftKeyWord.setInputPrompt("关键字");
		leftSearchLayout.addComponent(leftKeyWord);
		
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
		//表格
		leftTable=new Table();
		leftTable.setStyleName("striped");
		leftTable.setSelectable(true);
		leftTable.setMultiSelect(true);
		leftTable.setWidth("100%");
		tableLayout.addComponent(leftTable);
		
		//Container组件
		if(userService==null){
			userService=SpringContextHolder.getBean("userService");
		}
		leftTableContainer=new BeanItemContainer<User>(User.class);
		leftTableContainer.addNestedContainerProperty("department.name");
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
		rightSearchLayout.addComponent(new Label("关键字"));
		rightKeyWord=new TextField();//关键字
		rightKeyWord.setWidth("5em");
		rightKeyWord.setStyleName("search");
		rightKeyWord.setInputPrompt("关键字");
		rightSearchLayout.addComponent(rightKeyWord);
		
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
		//表格
		rightTable=new Table();
		rightTable.setStyleName("striped");
		rightTable.setPageLength(10);
		rightTable.setSelectable(true);
		rightTable.setMultiSelect(true);
		rightTable.setWidth("100%");
		rightTableContainer= new BeanItemContainer<User>(User.class);
		rightTableContainer.addNestedContainerProperty("department.name");
		rightTable.setContainerDataSource(rightTableContainer);
		rightTable.setVisibleColumns(columns);
		rightTable.setColumnHeaders(headers);
		tableLayout.addComponent(rightTable);
		return tableLayout;
	}
	/**
	 * 由 构造器 调用，设置按钮的样式
	 * @param style
	 */
	private void setButtonsStyle(String style) {
		style="small";
		leftSearch.setStyleName(style);
		rightSearch.setStyleName(style);
		add.setStyleName(style);
		addAll.setStyleName(style);
		remove.setStyleName(style);
		removeAll.setStyleName(style);
	}
	/**
	 * 由buttonClick调用,将选中表格tableFrom的值添加到tableTo
	 * @param tableFrom 从哪个表取数据
	 * @param tableTo	添加到哪个表
	 * @param isAll 是否添加全部
	 */
	@SuppressWarnings("unchecked")
	private void addToOpposite(Table tableFrom,Table tableTo,Boolean isAll) {
		if(tableFrom==null||tableTo==null) return;
		
		//如果添加全部，不对tableFrom选择的值进行验证,否则看选中的值是否为Null
		if(!isAll) if (tableFrom.getValue() == null) {
			this.getApplication().getMainWindow().showNotification("请选择要添加或移除的CSR!",
					Window.Notification.TYPE_HUMANIZED_MESSAGE);
			return;
		}
		
		//从tableFrom中取出所有选中的Csr
		Collection<User> csrs=null;
		if(isAll){
			//出现 java.util.ConcurrentModificationException异常，所以包装
			csrs=new ArrayList<User>((Collection<User>)tableFrom.getItemIds());
		}else{
			csrs=(Collection<User>) tableFrom.getValue();
		}
		//通过循环来改变TableFrom和TableTo的Item	
		for (User csr : csrs) {
			tableFrom.getContainerDataSource().removeItem(csr);
			tableTo.getContainerDataSource().addItem(csr);
		}
	}

	
	/**
	 * 由buttonClick调用，执行生成左侧Table的搜索的Sql,并刷新Table的Container
	 */
	private void executeLeftSearch() {
		// 搜索符合条件的批次信息
		leftTableContainer.removeAllContainerFilters();
		// 关键字
		String leftKeyWordStr ="";
		if (leftKeyWordStr.equals("")) {
			leftKeyWordStr = leftKeyWord.getValue().toString();
		}
		Or compareAll = new Or(
	             new Like("username", "%" + leftKeyWordStr + "%", false), 
	             new Like("empNo", "%" + leftKeyWordStr + "%", false), 
	             new Like("department.name", "%" + leftKeyWordStr + "%", false));
		leftTableContainer.addContainerFilter(compareAll);
	}
	
	/**
	 * 由buttonClick调用,右侧组件搜索
	 */
	private void executeRightSearch() {
		// 搜索符合条件的批次信息
		rightTableContainer.removeAllContainerFilters();
		// 关键字
		String rightKeyWordStr ="";
		if (rightKeyWordStr.equals("")) {
			rightKeyWordStr = rightKeyWord.getValue().toString();
		}
		Or compareAll = new Or(
	             new Like("username", "%" + rightKeyWordStr + "%", false), 
	             new Like("empNo", "%" + rightKeyWordStr + "%", false), 
	             new Like("department.name", "%" + rightKeyWordStr + "%", false));
		rightTableContainer.addContainerFilter(compareAll);
	}
	/**
	 * 执行给项目添加CSR的任务
	 */
	@SuppressWarnings("unchecked")
	private void executeAdd() {
		//更新给项目分配的CSR
		Set<User> selectedCsrs=new HashSet<User>((Collection<User>)rightTable.getContainerDataSource().getItemIds());
		if(selectedCsrs.size()==0){
			NotificationUtil.showWarningNotification(this, "请添加CSR到右侧表格");
			return;
		}
		//将CSR设置到父组件
		messageSend.setSelectedCsrs(selectedCsrs);
		this.getParent().removeWindow(this);
	}
	
	/**
	 * 弹出新的窗口,刷新Table里面数据的值
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void attach() {
		super.attach();
		//为左侧组件设置Container
		List<User> entitys =messageSend.getAllCsrs(); 
		if(leftTableContainer!=null){
			leftTableContainer.removeAllItems();
			leftTableContainer.addAll(entitys);
		}
		//移除在已经选中的Item,双重循环来排除重复,性能可能下降
		Collection<User> rightUsers=messageSend.getSelectedCsrs();
		if(rightUsers==null){
			rightTableContainer.removeAllItems();
			return;
		}
		Collection<User> leftUsers=(Collection<User>)leftTable.getItemIds();
		ArrayList<User> toRemove=new ArrayList<User>();
		for(User user:rightUsers){
			//以前的做法	leftTable.removeItem(user);
			for(User user2:leftUsers){
				if(user.getId() != null && user.getId().equals(user2.getId()))
					toRemove.add(user2);
			}
		}
		for(int i=0;i<toRemove.size();i++){
			leftTable.removeItem(toRemove.get(i));
		}
		//向右侧组件添加Csr
		rightTableContainer.removeAllItems();
		rightTableContainer.addAll(rightUsers);
	}
	
	/**
	 * 监听搜索、高级搜索，按钮的单击事件
	 * 
	 * 加 按钮事件（add，addAll，remove，removeAll）
	 */
	@Override
	public void buttonClick(ClickEvent event) {
		if(event.getButton()==leftSearch){
			executeLeftSearch();
		}else if(event.getButton()==add){
			addToOpposite(leftTable, rightTable,false);
		}else if(event.getButton()==addAll){
			addToOpposite(leftTable, rightTable,true);
		}else if(event.getButton()==remove){
			addToOpposite(rightTable,leftTable,false);
		}else if(event.getButton()==removeAll){
			addToOpposite(rightTable,leftTable,true);
		}else if(event.getButton()==rightSearch){
			executeRightSearch();
		}else if(event.getButton()==addCsr){
			executeAdd();
		}else if(event.getButton()==cancel){
			this.getParent().removeWindow(this);
		}
	}
}
