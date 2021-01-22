package com.jiangyifen.ec2.ui.mgr.projectcontrol;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.jiangyifen.ec2.bean.RoleType;
import com.jiangyifen.ec2.entity.Department;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.MarketingProject;
import com.jiangyifen.ec2.entity.Role;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.entity.enumtype.ExecuteType;
import com.jiangyifen.ec2.service.common.CommonService;
import com.jiangyifen.ec2.service.eaoservice.DepartmentService;
import com.jiangyifen.ec2.service.eaoservice.MarketingProjectService;
import com.jiangyifen.ec2.service.eaoservice.UserService;
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
 * 为项目添加Csr
 * @author chb
 *
 */
@SuppressWarnings("serial")
public class AssignCsr extends Window implements Button.ClickListener{
/**
 * 主要组件输出
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
	private Object[] columns = new String[] { "id", "username","empNo", "realName", "department.name"};
	private String[] headers = new String[] { "ID", "用户名","工号", "姓名", "部门"};
	private Table leftTable;
	private UserService userService;

	private Table rightTable;
	private BeanItemContainer<User> rightTableContainer;
	private BeanItemContainer<User> leftTableContainer;
	
	//下面的添加和取消按钮
	private Button assign;
	private Button cancel;
/**
 * 其他组件
 */
	private User loginUser;
	private Domain domain;
	private DepartmentService departmentService;
	private CommonService commonService;
	private MarketingProjectService marketingProjectService;
	//持有从Table取出来的MarketingProject引用
	private MarketingProject project;
	//持有调用它的组件引用ProjectControl,以刷新父组件
	private ProjectControl projectControl;
	//右侧组件刚开始的CSR数据，用于计算本次分配的数目
	private int originalSize;
	private List<Long> originalCsrIds;
	
	public AssignCsr(ProjectControl projectControl) {
		this.initService();
		this.center();
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
		
		/*=====中间的两个表格和按钮=========*/
		HorizontalLayout tempTablesLayout=buildTablesLayout();
		windowContent.addComponent(tempTablesLayout);
		windowContent.setExpandRatio(tempTablesLayout, 1.0f);
		
		/*=========下面的按钮输出===========*/
		HorizontalLayout tempButtonsLayout=buildButtonsLayout();
		windowContent.addComponent(tempButtonsLayout);
		windowContent.setComponentAlignment(tempButtonsLayout, Alignment.BOTTOM_RIGHT);
		
		//设置按钮样式
		this.setButtonsStyle(StyleConfig.BUTTON_STYLE);
		//添加拖拽支持
		DragAndDropSupport.addDragAndDropSupport(leftTable, rightTable);
	}
	
	/**
	 * 初始化Service
	 */
	private void initService() {
		loginUser = SpringContextHolder.getLoginUser();
		domain=SpringContextHolder.getDomain();
		commonService=SpringContextHolder.getBean("commonService");
		marketingProjectService=SpringContextHolder.getBean("marketingProjectService");
		userService=SpringContextHolder.getBean("userService");
		departmentService=SpringContextHolder.getBean("departmentService");
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
		rightTableContainer.addNestedContainerProperty("department.name");
		rightTable.setContainerDataSource(rightTableContainer);
		rightTable.setVisibleColumns(columns);
		rightTable.setColumnHeaders(headers);
		tableLayout.addComponent(rightTable);
		return tableLayout;
	}
	
	/**
	 * 由buildRightTable和attach调用，更新右侧表格的回显信息
	 */
	private void updateRightTable() {
		List<User> csrs=marketingProjectService.getCsrsByProject(project, domain);
		rightTableContainer.removeAllItems();
		// jrh 为了可以按话务员姓名进行搜索，如果不设置，查询时，会报空指针异常
		for(User csr : csrs) {
			if(csr.getRealName() == null) {
				csr.setRealName("");
			}
		}
		rightTableContainer.addAll(csrs);
		
		//chb 添加，记录项目上绑定的CSR的Id值
		originalCsrIds=new ArrayList<Long>();
		for(User csr:csrs){
			originalCsrIds.add(csr.getId());
		}
		
		originalSize = csrs.size();
	}
	
	/**
	 * 更新左侧表格
	 */
	private void updateLeftTable() {
		// jrh 获取当前用户所属部门及其所有角色的管辖部门的Id号
		List<Long> allGovernedDeptIds = new ArrayList<Long>();
		for(Role role : loginUser.getRoles()) {
			if(role.getType().equals(RoleType.manager)) {
				List<Department> departments = departmentService.getGovernedDeptsByRole(role.getId());
				if(departments.isEmpty()) {
					allGovernedDeptIds.add(0L);
				} else {
					for(Department dept : departments) {
						Long deptId = dept.getId();
						if(!allGovernedDeptIds.contains(deptId)) {
							allGovernedDeptIds.add(deptId);
						}
					}
				}
			}
		}
		// jrh 获取当前用户所属部门及其子部门下的所有用户,为左侧组件设置Container,只能指派当前用户所属部门及其子部门下的用户
		List<User> entitys = new ArrayList<User>();
		entitys.addAll(userService.getCsrsByDepartment(allGovernedDeptIds, domain.getId()));
		
		leftTableContainer.removeAllItems();
		// jrh 为了可以按话务员姓名进行搜索，如果不设置，查询时，会报空指针异常
		for(User csr : entitys) {
			if(csr.getRealName() == null) {
				csr.setRealName("");
			}
		}
		leftTableContainer.addAll(entitys);
		//移除在右侧组件中出现的Item
		Collection<User> rightUsers=(Collection<User>)rightTableContainer.getItemIds();
		//将被移除的User
		List<User> toRemove=new ArrayList<User>();
		//查找出将要移除的用户
		for(User leftUser:entitys){
			for(User rightUser:rightUsers){
				if(leftUser.getId() != null && leftUser.getId().equals(rightUser.getId())){
					toRemove.add(leftUser);
				}
			}
		}
		//从左侧组件中移除出现在右侧组件中的用户
		for(int i=0;i<toRemove.size();i++){
			leftTable.removeItem(toRemove.get(i));
		}
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
		if(leftTableContainer==null) return;
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
		if(rightTableContainer==null) return;
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
	 * 执行给项目添加CSR的任务，此时以前分配资源的CSR仍然在Task中
	 */
	private void executeAssign() {
		leftKeyWord.setValue("");
		executeLeftSearch();
		rightKeyWord.setValue("");
		executeRightSearch();
		
		
		try {
			//更新给项目分配的CSR
			Set<User> selectedCsrs=new HashSet<User>((Collection<User>)rightTableContainer.getItemIds());
			//估计一个项目的CSR数量不会太多，上千上万，所以让EL自己去管理，不手工控制
			project.setUsers(selectedCsrs);
			marketingProjectService.update(project);
			
			//chb 0805 记录移除的坐席的Id
			Set<Long> selectedIds=new HashSet<Long>();
			for(User user:selectedCsrs){
				selectedIds.add(user.getId());
			}
			Set<Long> removedCsrIds=new HashSet<Long>();
			for(Long csrId:originalCsrIds){
				if(!selectedIds.contains(csrId)){
					removedCsrIds.add(csrId);
				}
			}
			if(removedCsrIds.size()>0){
				String sqlEnd=StringUtils.join(removedCsrIds,',');
				String nativeRecycleSql = "update ec2_marketing_project_task set distributetime=null,"
						+ "isanswered=false,isfinished=false,laststatus=null,lastupdatedate=null,user_id=null "
						+ "where marketingproject_id="
						+ project.getId()
						+ " and domain_id="
						+ domain.getId()
						+ " "
						+ " and isfinished=false and user_id is not null and "
						+ "user_id in("+sqlEnd+")";
				commonService.excuteNativeSql(nativeRecycleSql, ExecuteType.UPDATE);
			}

			//成功分配的CSR记录数
			int successAssignNum=selectedCsrs.size()-originalSize;//目前的计数减去初始时计数
			
			String csrStr="添加了"+successAssignNum;
			if(successAssignNum<0){
				csrStr="移除了"+(-successAssignNum);
			}
			
OperationLogUtil.simpleLog(loginUser, "项目控制-添加CSR："+csrStr);

			Notification msgNotif=new Notification("成功给项目"+project.getProjectName()+csrStr+"个CSR!",Notification.TYPE_HUMANIZED_MESSAGE);
			//更新ProjectControl的状态信息，并且显示Notif提示
			Window mainWindow=this.getApplication().getMainWindow();
			this.getParent().removeWindow(this);
			projectControl.updateProjectResourceInfo();
			mainWindow.showNotification(msgNotif);
		} catch (Exception e) {
			e.printStackTrace();
			NotificationUtil.showWarningNotification(this, "项目指派CSR出现异常！");
		}
	}
	
	/**
	 * 弹出新的窗口,刷新Table里面数据的值
	 */
	@Override
	public void attach() {
		super.attach();
		//要想剔除左侧Table包含的右侧Table的Item，必须先更新右侧组件，再更新左侧组件
		project=projectControl.getCurrentSelect();
		this.setCaption("项目 "+project.getProjectName()+" 添加/移除CSR");
		//更新右侧组件的回显信息
		this.updateRightTable();
		this.updateLeftTable();
		
		//更新信息的显示
		projectControl.updateProjectResourceInfo();
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
			rightKeyWord.setValue("");
			executeRightSearch();
			addToOpposite(leftTable, rightTable,false);
		}else if(event.getButton()==addAll){
			rightKeyWord.setValue("");
			executeRightSearch();
			addToOpposite(leftTable, rightTable,true);
		}else if(event.getButton()==remove){
			leftKeyWord.setValue("");
			executeLeftSearch();
			addToOpposite(rightTable,leftTable,false);
		}else if(event.getButton()==removeAll){
			leftKeyWord.setValue("");
			executeLeftSearch();
			addToOpposite(rightTable,leftTable,true);
		}else if(event.getButton()==rightSearch){
			executeRightSearch();
		}else if(event.getButton()==assign){
			executeAssign();
		}else if(event.getButton()==cancel){
			this.getParent().removeWindow(this);
		}
	}
}
