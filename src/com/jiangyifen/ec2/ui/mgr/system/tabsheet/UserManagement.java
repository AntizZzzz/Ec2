package com.jiangyifen.ec2.ui.mgr.system.tabsheet;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.jiangyifen.ec2.bean.RoleType;
import com.jiangyifen.ec2.entity.Department;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.Role;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.entity.enumtype.ExecuteType;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.service.common.CommonService;
import com.jiangyifen.ec2.service.eaoservice.DepartmentService;
import com.jiangyifen.ec2.service.eaoservice.UserService;
import com.jiangyifen.ec2.ui.FlipOverTableComponent;
import com.jiangyifen.ec2.ui.mgr.usermanage.AddUser;
import com.jiangyifen.ec2.ui.mgr.usermanage.EditUser;
import com.jiangyifen.ec2.ui.mgr.usermanage.MutiAddUser;
import com.jiangyifen.ec2.ui.mgr.util.ConfirmWindow;
import com.jiangyifen.ec2.ui.mgr.util.OperationLogUtil;
import com.jiangyifen.ec2.ui.mgr.util.SqlGenerator;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.event.Action;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

/**
 * @author chb
 * 
 */
@SuppressWarnings("serial")
public class UserManagement extends VerticalLayout implements
		Button.ClickListener, Property.ValueChangeListener, Action.Handler {
	/**
	 * 主要组件
	 */
	// 搜索组件
	private TextField keyWord;
	private Button search;
	private ComboBox deptComboBox;		// 部门选择框

	// 用户表格组件
	private Table table;
	private String sqlSelect;
	private String sqlCount;
	private FlipOverTableComponent<User> flip;

	// 项目表格按钮组件
	private Button mutiAdd;
	private Button add;
	private Button edit;
	private Button delete;

	/**
	 * 右键组件
	 */
	private Action MUTIADD = new Action("批量添加");
	private Action ADD = new Action("添加");
	private Action EDIT = new Action("编辑");
	private Action DELETE = new Action("删除");
	private Action[] ACTIONS = new Action[] { ADD, EDIT, DELETE };
	/**
	 * 弹出窗口
	 */
	// 弹出窗口 只创建一次
	private MutiAddUser mutiAddWindow;
	private AddUser addUserWindow;
	private EditUser editUserWindow;
	/**
	 * 其他
	 */
	private User loginUser;
	// 表格选中的User
	private Domain domain;
	private User currentSelectUser;
	private List<Long> allGovernedDeptIds;
	private UserService userService;
	private CommonService commonService;
	private DepartmentService departmentService;

	/**
	 * 构造器
	 */
	public UserManagement() {
		this.initService();
		this.setWidth("100%");
		this.setMargin(true);

		// 约束组件，使组件紧密排列
		VerticalLayout constrantLayout = new VerticalLayout();
		constrantLayout.setWidth("100%");
		constrantLayout.setSpacing(true);
		this.addComponent(constrantLayout);

		// 搜索
		constrantLayout.addComponent(buildSearchLayout());

		// 表格和按钮
		constrantLayout.addComponent(buildTabelAndButtonsLayout());
	}

	/**
	 * 初始化Service
	 */
	private void initService() {
		domain = SpringContextHolder.getDomain();
		loginUser = SpringContextHolder.getLoginUser();
		userService = SpringContextHolder.getBean("userService");
		commonService=SpringContextHolder.getBean("commonService");
		departmentService=SpringContextHolder.getBean("departmentService");
	}

	/**
	 * 创建搜索输出（Search）
	 */
	private HorizontalLayout buildSearchLayout() {
		HorizontalLayout searchLayout = new HorizontalLayout();
		searchLayout.setSpacing(true);

		// 使得KeyWord和KeyWordLabel组合在一起
		HorizontalLayout constrantLayout = new HorizontalLayout();
		constrantLayout.setSpacing(true);
		constrantLayout.addComponent(new Label("用户名:")); // 添加关键字Label
		keyWord = new TextField();
		keyWord.setWidth("10em");
		keyWord.setInputPrompt("用户名");
		constrantLayout.addComponent(keyWord); // 添加关键字Field
		searchLayout.addComponent(constrantLayout);

		HorizontalLayout constrantLayout1 = new HorizontalLayout();
		constrantLayout1.addComponent(new Label("部门选择:")); 
		constrantLayout1.addComponent(buildDeptComboBox());
		searchLayout.addComponent(constrantLayout1);
		
		// 搜索按钮
		search = new Button("搜索");
		search.addListener((Button.ClickListener) this);
		searchLayout.addComponent(search);

		return searchLayout;
	}

	/**
	 * 创建表格和按钮输出（Table）
	 * 
	 * @return
	 */
	private VerticalLayout buildTabelAndButtonsLayout() {
		VerticalLayout tabelAndButtonsLayout = new VerticalLayout();
		tabelAndButtonsLayout.setSpacing(true);
		final SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日");// 节省每次创建的资源
		// 表格组件
		table = new Table() { // 创建指定日期格式的表格组件
			@SuppressWarnings("unchecked")
			@Override
			protected String formatPropertyValue(Object rowId, Object colId, Property property) {
				Object v = property.getValue();
				if (v instanceof Date) {
					return sdf.format(v);
				} else if (colId.equals("roles")) {
					List<String> roleNameList = new ArrayList<String>();
					Set<Role> set = (Set<Role>) property.getValue();
					Iterator<Role> it = set.iterator();
					while (it.hasNext()) {
						roleNameList.add(it.next().toString());
					}
					return StringUtils.join(roleNameList, ",");
				}
				return super.formatPropertyValue(rowId, colId, property);
			}
		};
		table.setStyleName("striped");
		table.addActionHandler(this);
		table.setWidth("100%");
		table.setSelectable(true);
		table.setMultiSelect(false);
		table.setImmediate(true);
		table.setRowHeaderMode(Table.ROW_HEADER_MODE_INDEX);
		table.addListener((Property.ValueChangeListener) this);
		tabelAndButtonsLayout.addComponent(table);

		// 表格下面的按钮组件，包括Flip
		tabelAndButtonsLayout.addComponent(buildButtonsAndFlipLayout());
		return tabelAndButtonsLayout;
	}
	
	/**
	 * 创建客服工号的ComboBox
	 * 
	 * @return
	 */
	private ComboBox buildDeptComboBox() {
		// 从数据库中获取“客服”信息，并绑定到Container中
		BeanItemContainer<Department> departmentContainer = new BeanItemContainer<Department>(Department.class);
		Department department=new Department();
		department.setName("全部");
		departmentContainer.addBean(department);

		//取得用户管辖的部门
		User loginUser = SpringContextHolder.getLoginUser();
		Set<Role> userRoles = loginUser.getRoles();
		Set<Department> departments=null;
		for(Role role:userRoles){
			if(role.getType()==RoleType.manager){
				departments = role.getDepartments();
			}
		}

		//在comboBox中添加用户管辖的部门
		departmentContainer.addAll(departments);
		
		// 创建ComboBox
		deptComboBox = new ComboBox();
		deptComboBox.setContainerDataSource(departmentContainer);
		deptComboBox.setItemCaptionPropertyId("name");
		deptComboBox.setValue(department);
		deptComboBox.setWidth("140px");
		deptComboBox.setNullSelectionAllowed(false);
		return deptComboBox;
	}


	/**
	 * 由buildTabelAndButtonsLayout调用，创建Table下的按钮输出，包括Flip
	 * 
	 * @return
	 */
	private HorizontalLayout buildButtonsAndFlipLayout() {
		// 按钮输出
		HorizontalLayout tableButtons = new HorizontalLayout();
		tableButtons.setSpacing(true);
		tableButtons.setWidth("100%");

		// 左侧按钮
		HorizontalLayout tableButtonsLeft = new HorizontalLayout();
		tableButtonsLeft.setSpacing(true);
		
		mutiAdd = new Button("批量添加");
		mutiAdd.addListener((Button.ClickListener) this);
		tableButtonsLeft.addComponent(mutiAdd);

		add = new Button("添加");
		add.addListener((Button.ClickListener) this);
		tableButtonsLeft.addComponent(add);

		edit = new Button("编辑");
		edit.setEnabled(false); // 创建时为不可用
		edit.addListener((Button.ClickListener) this);
		tableButtonsLeft.addComponent(edit);
		tableButtons.addComponent(tableButtonsLeft);

		delete = new Button("删除");
		delete.setEnabled(false); // 创建时为不可用
		delete.addListener((Button.ClickListener) this);
		tableButtonsLeft.addComponent(delete);
		tableButtons.addComponent(tableButtonsLeft);

		// 右侧按钮（翻页组件）
		// table 已经创建，不为null，sql已经有search按钮的click事件初始化
		flip = new FlipOverTableComponent<User>(User.class, userService, table,
				sqlSelect, sqlCount, this);
		table.setPageLength(20); // 为了方便修改，将设置Table的操作放在这里
		flip.setPageLength(20, false);

		// 重要提示：执行此处必须将Role设置为Eager状态

		// 设置表格头部显示
		Object[] visibleColumns = new Object[] {"empNo", "realName",
				"username", "age", "gender", "phoneNumber",
				"emailAddress", "registedDate", "department", "roles" };
		String[] columnHeaders = new String[] {"工号", "姓名", "用户名",
				"年龄", "性别", "电话号码", "邮箱地址", "注册日期", "所属部门", "所属角色" };
		table.setVisibleColumns(visibleColumns);
		table.setColumnHeaders(columnHeaders);

		// Flip 组件
		tableButtons.addComponent(flip);
		tableButtons.setComponentAlignment(flip, Alignment.MIDDLE_RIGHT);
		return tableButtons;
	}

	/**
	 * 由buttonClick调用执行简单搜索，更新翻页组件到第一页
	 */
	private void initializeSql() {
		SqlGenerator sqlGenerator = new SqlGenerator("User");
		// 关键字过滤
		String keyWordStr = "";
		if (keyWord.getValue() != null) {
			keyWordStr = keyWord.getValue().toString();
		}
		// 用户名
		SqlGenerator.Like username = new SqlGenerator.Like("username",
				keyWordStr);
		sqlGenerator.addAndCondition(username);

//		// 真实名
//		SqlGenerator.Like realName = new SqlGenerator.Like("realName",
//				keyWordStr);
//		sqlGenerator.addOrCondition(realName);
//
//		// 工号
//		SqlGenerator.Like empNo = new SqlGenerator.Like("empNo", keyWordStr);
//		sqlGenerator.addOrCondition(empNo);
		
		//--------------------------------------- chb  部门选择信息
		Department department= (Department) deptComboBox
				.getValue();
		if ("全部".equals(department.getName())) { //如果选择的是全部，则只显示所管辖的部门
			// jrh 获取当前用户所属部门及其所有角色的管辖部门的Id号
			List<Long> allGovernedDeptIds = new ArrayList<Long>();
			for(Role role : loginUser.getRoles()) {
				if(role.getType().equals(RoleType.manager)) {
					List<Department> departments = departmentService.getGovernedDeptsByRole(role.getId());
					if(departments.isEmpty()) {
						allGovernedDeptIds.add(0L);
					} else {
						for (Department dept : departments) {
							Long deptId = dept.getId();
							if (!allGovernedDeptIds.contains(deptId)) {
								allGovernedDeptIds.add(deptId);
							}
						}
					}
				}
			}
			
			// jrh 获取当前用户所属部门及其子部门创建的客服记录
			for(Long deptId : allGovernedDeptIds) {
				SqlGenerator.Equal orEqual = new SqlGenerator.Equal("department.id", deptId.toString(), false);
				sqlGenerator.addOrCondition(orEqual);
			}
		} else {//显示选中的部门
			SqlGenerator.Equal andEqual = new SqlGenerator.Equal("department.id", department.getId().toString(), false);
			sqlGenerator.addAndCondition(andEqual);
		}
		
		sqlCount = sqlGenerator.generateCountSql();
		
//		sqlCount += " and e.department.id in ("+StringUtils.join(allGovernedDeptIds, ",")+")"; 
		
		sqlSelect = sqlGenerator.generateSelectSql();
		
		
		
		sqlSelect = sqlCount.replaceAll("count\\(e\\)", "e") + " order by e.id desc";
	}
	
	/**
	 * 由buttonClick调用，显示添加批量添加用户的窗口
	 */
	private void showMutiAddWindow() {
		// 添加新窗口,并且新窗口只创建一次，不重复创建
		if (mutiAddWindow == null) {
			mutiAddWindow = new MutiAddUser(this);
		}
		this.getApplication().getMainWindow().removeWindow(mutiAddWindow);
		this.getApplication().getMainWindow().addWindow(mutiAddWindow);
	}
	
	/**
	 * 由buttonClick调用，显示添加用户的窗口
	 */
	private void showAddWindow() {
		// 添加新窗口,并且新窗口只创建一次，不重复创建
		if (addUserWindow == null) {  //TODO
			addUserWindow = new AddUser(this);
		}
		this.getApplication().getMainWindow().removeWindow(addUserWindow);
		this.getApplication().getMainWindow().addWindow(addUserWindow);
	}

	/**
	 * 由buttonClick调用，显示编辑用户的窗口
	 */
	private void showEditWindow() {
		// 添加新窗口,并且新窗口只创建一次，不重复创建
		if (editUserWindow == null) {
			editUserWindow = new EditUser(this);
		}
		this.getApplication().getMainWindow().removeWindow(editUserWindow);
		this.getApplication().getMainWindow().addWindow(editUserWindow);
	}

	/**
	 * 执行删除操作
	 */
	private void executeDelete() {//用户的Id Note:隐含域
		List<String> msgList=new ArrayList<String>();
		//用户和项目中间表，中间表引用用户
		String sqla="select count(*) from ec2_markering_project_ec2_user where users_id="+currentSelectUser.getId();
		Long counta=(Long)commonService.excuteNativeSql(sqla,ExecuteType.SINGLE_RESULT);
		if(counta>0){
			msgList.add("用户正在执行某个项目，不能被删除！");
		}
		//用户可能创建过Project，被项目引用
		String sqlb="select count(*) from ec2_markering_project where creater_id="+currentSelectUser.getId();
		Long countb=(Long)commonService.excuteNativeSql(sqlb,ExecuteType.SINGLE_RESULT);
		if(countb>0){
			msgList.add("用户曾经创建过项目，不能被删除！");
		}
		//用户可能创建批次，被批次引用
		String sqlc="select count(*) from ec2_customer_resource_batch where user_id="+currentSelectUser.getId();
		Long countc=(Long)commonService.excuteNativeSql(sqlc,ExecuteType.SINGLE_RESULT);
		if(countc>0){
			msgList.add("用户曾经创建过批次，不能被删除！");
		}
		//用户可能拥有自己的客户资源CustomerResource
		String sqld="select count(*) from ec2_customer_resource where accountmanager_id="+currentSelectUser.getId();
		Long countd=(Long)commonService.excuteNativeSql(sqld,ExecuteType.SINGLE_RESULT);
		if(countd>0){
			msgList.add("用户正在独享一些资源，不能被删除！");
		}
		//Task表中用户可能在执行Task任务
		String sqle="select count(*) from ec2_marketing_project_task where user_id="+currentSelectUser.getId();
		Long counte=(Long)commonService.excuteNativeSql(sqle,ExecuteType.SINGLE_RESULT);
		if(counte>0){
			msgList.add("用户正在项目中执行任务，不能被删除！");
		}
		//用户可能含有客服记录，被记录引用
		String sqlg="select count(*) from ec2_customer_service_record where creator_id="+currentSelectUser.getId();
		Long countg=(Long)commonService.excuteNativeSql(sqlg,ExecuteType.SINGLE_RESULT);
		if(countg>0){
			msgList.add("用户存在客服记录，不能被删除！");
		}
		//用户可能接过投诉电话，被投诉电话客服记录引用
		String sqlh="select count(*) from ec2_customer_complaint_record where creator_id="+currentSelectUser.getId();
		Long counth=(Long)commonService.excuteNativeSql(sqlh,ExecuteType.SINGLE_RESULT);
		if(counth>0){
			msgList.add("用户存在接听投诉记录，不能被删除！");
		}
		//用户可能处理过投诉电话，被投诉电话客服记录引用
		String sqli="select count(*) from ec2_customer_complaint_record where owner_id="+currentSelectUser.getId();
		Long counti=(Long)commonService.excuteNativeSql(sqli,ExecuteType.SINGLE_RESULT);
		if(counti>0){
			msgList.add("用户处理过投诉电话，有记录，不能被删除！");
		}
		//用户可能是发通知的管理员
		String sqlk="select count(*) from ec2_notice where sender_id="+currentSelectUser.getId();
		Long countk=(Long)commonService.excuteNativeSql(sqlk,ExecuteType.SINGLE_RESULT);
		if(countk>0){
			msgList.add("用户曾经发过通知，不能被删除！");
		}
		//如果删除的是主管再判断User表中是否仅剩一个主管，如果是则不能删除  XKP
		String sqlj = "select u from User as u where u.domain.id="+ domain.getId()+ " and u.id != "+ currentSelectUser.getId();
		List<User> userList = userService.getAllUsersByJpql(sqlj);
		Set<Role> currentSelectRoles = currentSelectUser.getRoles();
		Iterator<Role> it = currentSelectRoles.iterator();
		while (it.hasNext()) {
			Role str = it.next();
			if (RoleType.manager.getName().equals(str.getType().getName())) {
				String type = "";
				for (User u : userList) {
					Set<Role> roles = u.getRoles();
					Iterator<Role> innerIt = roles.iterator();
					while (innerIt.hasNext()) {
						Role role = innerIt.next();
						if (RoleType.manager.getName().equals(role.getType().getName())) {
							type = RoleType.manager.getName();
						}
					}
				}
				if (type.equals("")) {
					msgList.add("此用户不能删除，请保留一个主管！");
				}
			}
		}
		
		/**=====================关联情况获取信息完毕=====================**/
		//如果有相关的引用信息，提示用户不让用户删除
		if(msgList.size()>0){
			StringBuilder sb=new StringBuilder();
			Iterator<String> iter = msgList.iterator();
			while(iter.hasNext()){
				sb.append(iter.next());
				if(iter.hasNext()){
					sb.append("</br>");
				}
			}
			this.getApplication().getMainWindow().showNotification(sb.toString());
		}else{//如果没有相关的引用，删除用户和角色的引用，用户和定时器的关联
			//用户删除
			Label label=new Label("您确定要删除用户<b>"+currentSelectUser.getUsername()+"</b>?",Label.CONTENT_XHTML);
			ConfirmWindow confirmWindow=new ConfirmWindow(label,this,"confirmDelete");
			this.getApplication().getMainWindow().removeWindow(confirmWindow);
			this.getApplication().getMainWindow().addWindow(confirmWindow);
		}
	}

	// 用来供回调的方法
	public void flipOverCallBack(List<User> users) {
		for (User user : users) {
			user.getRoles().size();// 让Roles初始化
		}
	}

	/**
	 * 由弹出窗口使用，获取table选中的值
	 * 
	 * @return
	 */
	public Table getTable() {
		return table;
	}

	/**
	 * 由executeSearch调用更新表格内容,上层组件TabSheet调用
	 * 
	 * @param isToFirst
	 *            是否更新到第一页，default 是 false
	 */
	public void updateTable(Boolean isToFirst) {
		// jrh 获取当前用户所属部门及其所有角色的管辖部门的Id号
		allGovernedDeptIds = new ArrayList<Long>();
		for(Role role : loginUser.getRoles()) {
			if(role.getType().equals(RoleType.manager)) {
				List<Department> departments = departmentService.getGovernedDeptsByRole(role.getId());
				if(departments.isEmpty()) {
					allGovernedDeptIds.add(0L);
				} else {
					for (Department dept : departments) {
						Long deptId = dept.getId();
						if (!allGovernedDeptIds.contains(deptId)) {
							allGovernedDeptIds.add(deptId);
						}
					}
				}
			}
		}
		
		initializeSql();
		
		refreshTable(isToFirst);
	}
	
	/**
	 * 由executeSearch调用更新表格内容,上层组件TabSheet调用
	 * 
	 * @param isToFirst
	 *            是否更新到第一页，default 是 false
	 */
	public void refreshTable(Boolean isToFirst) {
		if (flip != null) {
			flip.setSearchSql(sqlSelect);
			flip.setCountSql(sqlCount);
			if (isToFirst) {
				flip.refreshToFirstPage();
			} else {
				flip.refreshInCurrentPage();
			}
		}
	}

	/**
	 * 由弹出窗口回调确认删除项目,此时userService不应该为null
	 */
	public void confirmDelete(Boolean isConfirmed) {
		if (isConfirmed == true) {

			OperationLogUtil.simpleLog(loginUser, "管理员删除用户:"+currentSelectUser.getMigrateCsr());
			
			// jinht 删除用户的时候要将该用户导入过的资源移交给当前的操作者
			String sqlUpdate = "update ec2_customer_resource set owner_id = " + loginUser.getId() + " where owner_id = " + currentSelectUser.getId();
			commonService.excuteNativeSql(sqlUpdate, ExecuteType.UPDATE);
			
			//用户可能被管理员发过的一个通知引用
			String sqlj="delete from ec2_noticeitem where user_id="+currentSelectUser.getId();
			commonService.excuteNativeSql(sqlj,ExecuteType.UPDATE);

			//用户与角色中间表，用户可能被中间表引用
			String sqlf="delete from ec2_user_role_link where user_id="+currentSelectUser.getId();
			commonService.excuteNativeSql(sqlf,ExecuteType.UPDATE);

			//用户可能创建了一个定时器，被定时器引用
			String sqll="delete from ec2_timers where creator_id="+currentSelectUser.getId();
			commonService.excuteNativeSql(sqll,ExecuteType.UPDATE);
			
			//用户可能是某个队列的成员，先得删除用户与队列的对应关系
			String sqlUQ = "delete from ec2_user_queue where username = '" +currentSelectUser.getUsername()+ "'";
			commonService.excuteNativeSql(sqlUQ, ExecuteType.UPDATE);
			
			//用户可能与静态外线有对应关系
			String sqlUO = "delete from ec2_user_outline where user_id = " +currentSelectUser.getId();
			commonService.excuteNativeSql(sqlUO, ExecuteType.UPDATE);
			
			//删除用户
			String sql="delete from ec2_user where id="+currentSelectUser.getId();
			commonService.excuteNativeSql(sql,ExecuteType.UPDATE);
			
			table.setValue(null);
			this.refreshTable(false);
			this.getApplication().getMainWindow().showNotification("删除成功！");
		}
	}

	/**
	 * 表格选择改变的监听器，设置按钮样式，状态信息
	 */
	@Override
	public void valueChange(ValueChangeEvent event) {
		currentSelectUser = (User) table.getValue();
		// 改变按钮
		if (table.getValue() != null) {
			// 应该是可以通过设置父组件来使之全部为true;
			edit.setEnabled(true);
			delete.setEnabled(true);
		} else {
			// 应该是可以通过设置父组件来使之全部为false;
			edit.setEnabled(false);
			delete.setEnabled(false);
		}
	}

	/**
	 * Action.Handler 实现方法
	 */
	@Override
	public Action[] getActions(Object target, Object sender) {
		if(target == null) {
			return new Action[] {MUTIADD, ADD};
		}
		return ACTIONS;
	}

	@Override
	public void handleAction(Action action, Object sender, Object target) {
		table.setValue(null);
		table.select(target);
		if(MUTIADD == action) {
			mutiAdd.click();
		} else if (ADD == action) {
			add.click();
		} else if (EDIT == action) {
			edit.click();
		} else if (DELETE == action) {
			delete.click();
		}
	}

	/**
	 * 单击按钮时触发的事件
	 */
	@Override
	public void buttonClick(ClickEvent event) {
		if (event.getButton() == search) {
			initializeSql();
			// 按Sql更新表格显示的信息
			this.refreshTable(true);
		} else if (event.getButton() == mutiAdd) {
			showMutiAddWindow();
		} else if (event.getButton() == add) {
			showAddWindow();
		} else if (event.getButton() == edit) {
			showEditWindow();
		} else if (event.getButton() == delete) {
			if(currentSelectUser.getId().equals(loginUser.getId())) {
				this.getApplication().getMainWindow().showNotification("用户 " +currentSelectUser.getUsername()+ " 已经登陆系统，暂不能删！");
				return;
			}
			// 先判断用户是否已经登陆，如果已经登陆，则不让删除
			for(Long userId : ShareData.userToExten.keySet()) {
				if(userId == currentSelectUser.getId()) {
					this.getApplication().getMainWindow().showNotification("用户 " +currentSelectUser.getUsername()+ " 已经登陆系统，暂不能删！");
					return;
				}
			}
			
			// 执行删除操作
			executeDelete();
		}
	}

}
