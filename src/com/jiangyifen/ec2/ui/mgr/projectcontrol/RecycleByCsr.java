package com.jiangyifen.ec2.ui.mgr.projectcontrol;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.jiangyifen.ec2.entity.MarketingProject;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.service.eaoservice.MarketingProjectTaskService;
import com.jiangyifen.ec2.ui.mgr.tabsheet.ProjectControl;
import com.jiangyifen.ec2.ui.mgr.util.DragAndDropSupport;
import com.jiangyifen.ec2.ui.mgr.util.OperationLogUtil;
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
 * 回收资源组件
 * @author chb
 *
 */
@SuppressWarnings("serial")
public class RecycleByCsr extends Window implements Button.ClickListener{
/**
 * 主要组件输出	
 */
	/*======= 中间的左侧（表格和搜索）和右侧（表格和搜索）输出 ========*/
	private HorizontalLayout centerTablesLayout;
	//关键字和搜索按钮
	//左
	private TextField leftKeyWord;
	private Button leftSearch;
	//右
	private TextField rightKeyWord;
	private Button rightSearch;
	
	//中间的添加按钮
	private VerticalLayout centerButtonsLayout;
	private Button addAll;
	private Button add;
	private Button remove;
	private Button removeAll;
	
	//表格
	private Object[] columns = new String[] { "id", "username","empNo", "realName", "department.name"};
	private String[] headers = new String[] { "ID", "用户名","工号", "姓名", "部门"};
	private Table leftTable;
	private BeanItemContainer<User> leftTableContainer;
	private VerticalLayout tableLeftLayout;

	private Table rightTable;
	private BeanItemContainer<User> rightTableContainer;
	private VerticalLayout tableRightLayout;
	
	private User loginUser=SpringContextHolder.getLoginUser();
	
	/*===========下方的回收和取消按钮============*/
	//回收和取消按钮
	private Button recycle;
	private Button cancel;
	
/**
 * 其他组件	
 */
	//持有从Table取出来的MarketingProject引用
	private MarketingProject project;
	//持有调用它的组件引用ProjectControl,以刷新父组件
	private ProjectControl projectControl;
	private MarketingProjectTaskService marketingProjectTaskService;
	/**
	 * 构造器
	 */
	public RecycleByCsr(ProjectControl projectControl) {
		this.initService();
		this.center();
		this.setCaption("按CSR回收资源");
		this.setModal(true);
		this.setResizable(false);
		this.projectControl=projectControl;
		
		//添加Window内最大的Layout
		VerticalLayout windowContent=new VerticalLayout();
		windowContent.setSizeUndefined();
		windowContent.setMargin(false, true, true, true);
		windowContent.setSpacing(true);
		windowContent.setStyleName(StyleConfig.VERTICAL_STYLE);
		this.setContent(windowContent);
		
		//添加中间组件输出
		windowContent.addComponent(buildCenterTablesLayout());
		
		//右下角的按钮输出
		HorizontalLayout tempButtonsLayout=buildButtonsLayout();
		windowContent.addComponent(tempButtonsLayout);
		windowContent.setComponentAlignment(tempButtonsLayout, Alignment.BOTTOM_RIGHT);
		
		this.setButtonsStyle(StyleConfig.BUTTON_STYLE);
	}
	/**
	 * 初始化Service
	 */
	private void initService() {
		marketingProjectTaskService=SpringContextHolder.getBean("marketingProjectTaskService");
	}

	/**
	 * 创建中间的组件组件
	 * @return
	 */
	private HorizontalLayout buildCenterTablesLayout() {
		centerTablesLayout=new HorizontalLayout();
		centerTablesLayout.setSpacing(true);

		//左侧输出
		tableLeftLayout = buildLeft();
		centerTablesLayout.addComponent(tableLeftLayout);
		centerTablesLayout.setExpandRatio(tableLeftLayout, 8.0f);
		
		//中间输出
		centerButtonsLayout = buildCenterButtons();
		centerTablesLayout.addComponent(centerButtonsLayout);
		centerTablesLayout.setExpandRatio(centerButtonsLayout, 1.5f);
		
		//右侧输出
		tableRightLayout = buildRight();
		centerTablesLayout.addComponent(tableRightLayout);
		centerTablesLayout.setExpandRatio(tableRightLayout, 8.0f);
		
		//添加拖拽支持
		DragAndDropSupport.addDragAndDropSupport(leftTable, rightTable);		
		return centerTablesLayout;
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
	private VerticalLayout buildCenterButtons() {
		VerticalLayout centerLayout=new VerticalLayout();
		centerLayout.setSpacing(true);
		//占位组件
		centerLayout.addComponent(new Label("&nbsp&nbsp",Label.CONTENT_XHTML));
		centerLayout.addComponent(new Label("&nbsp&nbsp",Label.CONTENT_XHTML));
		centerLayout.addComponent(new Label("&nbsp&nbsp",Label.CONTENT_XHTML));
		
		//按钮组件
		addAll=new Button(">>>");
		addAll.addListener((Button.ClickListener)this);
		centerLayout.addComponent(addAll);
		centerLayout.setComponentAlignment(addAll, Alignment.MIDDLE_CENTER);
		
		add=new Button(">>");
		add.addListener((Button.ClickListener)this);
		centerLayout.addComponent(add);
		centerLayout.setComponentAlignment(add, Alignment.MIDDLE_CENTER);

		remove=new Button("<<");
		remove.addListener((Button.ClickListener)this);
		centerLayout.addComponent(remove);
		centerLayout.setComponentAlignment(remove, Alignment.MIDDLE_CENTER);
		
		removeAll=new Button("<<<");
		removeAll.addListener((Button.ClickListener)this);
		centerLayout.addComponent(removeAll);
		centerLayout.setComponentAlignment(removeAll, Alignment.MIDDLE_CENTER);
		
		return centerLayout;
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
		rightTableContainer.addNestedContainerProperty("department.name");
		rightTable.setContainerDataSource(rightTableContainer);
		rightTable.setVisibleColumns(columns);
		rightTable.setColumnHeaders(headers);
		tableLayout.addComponent(rightTable);
		return tableLayout;
	}

	/**
	 * 由 buildLeft 调用，左侧表格
	 * @return
	 */
	private VerticalLayout buildLeftTable() {
		VerticalLayout tableLayout=new VerticalLayout();
		tableLayout.setWidth("100%");
		//表格
		leftTable=new Table();
		leftTable.setStyleName("striped");
		leftTable.setPageLength(10);
		leftTable.setSelectable(true);
		leftTable.setMultiSelect(true);
		leftTable.setWidth("100%");
		leftTableContainer= new BeanItemContainer<User>(User.class);
		leftTableContainer.addNestedContainerProperty("department.name");
		leftTable.setContainerDataSource(leftTableContainer);
		leftTable.setVisibleColumns(columns);
		leftTable.setColumnHeaders(headers);
		tableLayout.addComponent(leftTable);
		return tableLayout;
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
		//关键字
		leftKeyWord=new TextField();
		leftKeyWord.setWidth("5em");
		leftKeyWord.setStyleName("search");
		leftKeyWord.setInputPrompt("关键字");
		constraintLayout.addComponent(leftKeyWord);
		leftSearchLayout.addComponent(constraintLayout);
		
		// 搜索按钮
		leftSearch = new Button("搜索");
		leftSearch.addListener((Button.ClickListener)this);
		leftSearchLayout.addComponent(leftSearch);
		
		return leftSearchLayout;
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
		//关键字
		rightKeyWord=new TextField();
		rightKeyWord.setWidth("5em");
		rightKeyWord.setStyleName("search");
		rightKeyWord.setInputPrompt("关键字");
		constraintLayout.addComponent(rightKeyWord);
		rightSearchLayout.addComponent(constraintLayout);
		
		// 搜索按钮
		rightSearch = new Button("搜索");
		rightSearch.addListener((Button.ClickListener)this);
		rightSearchLayout.addComponent(rightSearch);
		
		return rightSearchLayout;
	}
	
	/**
	 * 创建此窗口最下边按钮的输出
	 * @return
	 */
	private HorizontalLayout buildButtonsLayout() {
		HorizontalLayout buttonsLayout=new HorizontalLayout();
		buttonsLayout.setWidth("23%");
		//添加
		recycle=new Button("回收");
		recycle.addListener((Button.ClickListener)this);
		buttonsLayout.addComponent(recycle);
		buttonsLayout.setComponentAlignment(recycle, Alignment.MIDDLE_CENTER);
		//取消
		cancel=new Button("取消");
		cancel.addListener((Button.ClickListener)this);
		buttonsLayout.addComponent(cancel);
		buttonsLayout.setComponentAlignment(cancel, Alignment.MIDDLE_CENTER);
		return buttonsLayout;
	}
	/**
	 * 由buttonClick调用,完成左侧组件的搜索
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
	             new Like("realName", "%" + leftKeyWordStr + "%", false), 
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
	             new Like("realName", "%" + rightKeyWordStr + "%", false), 
	             new Like("department.name", "%" + rightKeyWordStr + "%", false));
		rightTableContainer.addContainerFilter(compareAll);
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
	 * 由 构造器 调用，设置按钮的样式
	 * @param style
	 */
	private void setButtonsStyle(String style) {
		style="small";
		//搜索按钮和添加移除按钮
		leftSearch.setStyleName(style);
		rightSearch.setStyleName(style);
		add.setStyleName(style);
		addAll.setStyleName(style);
		remove.setStyleName(style);
		removeAll.setStyleName(style);
		
		//最下面的两个按钮
		style="";
		recycle.setStyleName(style);
		cancel.setStyleName(style);
	}
	/**
	 * 执行回收
	 */
	private void executeRecycle() {
		leftKeyWord.setValue("");
		executeLeftSearch();
		rightKeyWord.setValue("");
		executeRightSearch();
		
		
		Window mainWindow=this.getApplication().getMainWindow();
		
		Collection<User> csrs = (Collection<User>)rightTableContainer.getItemIds();
		int recycleNum=0;
		//移除Window后就取不到Application了，所以先取出mainWindow的引用
		for(User user:csrs){
			OperationLogUtil.simpleLog(loginUser, "项目控制-按CSR回收任务："+user.getUsername());
			recycleNum+=marketingProjectTaskService.recycleByCsr(project, user, SpringContextHolder.getDomain());
		}
		//更新消息
		this.getParent().removeWindow(this);
		projectControl.updateProjectResourceInfo();
		//显示提示
		Notification msgNotif=new Notification("项目"+project.getProjectName()+"成功回收了"+recycleNum+"条资源!",Notification.TYPE_HUMANIZED_MESSAGE);
		mainWindow.showNotification(msgNotif);
	}
	
	/**
	 * 每次Attach时根据MarketingProject重查数据库，重设左侧TableContainer的数据源
	 */
	@Override
	public void attach() {
		super.attach();
		leftTableContainer.removeAllItems();
		rightTableContainer.removeAllItems();
		project=(MarketingProject)projectControl.getTable().getValue();
		List<User> csrs=marketingProjectTaskService.getNotFinishedCsrsByProject(project);
		// jrh 为了可以按话务员姓名进行搜索，如果不设置，查询时，会报空指针异常
		for(User csr : csrs) {
			if(csr.getRealName() == null) {
				csr.setRealName("");
			}
		}
		leftTableContainer.addAll(csrs);
	}
	
	/**
	 * 监听搜索、高级搜索，按钮的单击事件
	 * 加 按钮事件（add，addAll，remove，removeAll） 
	 * 监听 add和cancel按钮的事件
	 * @param event
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
		}else if(event.getButton()==recycle||event.getButton()==cancel){
			bottomButtonClick(event);
		}
	}
	/**
	 * 点击回收或取消按钮的事件,为了统一，将其设置为public
	 * @param event
	 */
	public void bottomButtonClick(ClickEvent event) {
		if(event.getButton()==recycle){
			try {
				executeRecycle();
			} catch (Exception e) {
				e.printStackTrace();
				NotificationUtil.showWarningNotification(this, "按CSR回收资源失败");
			}
		}else if(event.getButton()==cancel){
			this.getParent().removeWindow(this);
			return;
		}
	}
	
}
