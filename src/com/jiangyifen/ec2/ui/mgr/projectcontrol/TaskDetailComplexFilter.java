package com.jiangyifen.ec2.ui.mgr.projectcontrol;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

import com.jiangyifen.ec2.bean.RoleType;
import com.jiangyifen.ec2.entity.CustomerServiceRecordStatus;
import com.jiangyifen.ec2.entity.Department;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.MarketingProject;
import com.jiangyifen.ec2.entity.Role;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.entity.enumtype.QcMgr;
import com.jiangyifen.ec2.service.eaoservice.CustomerServiceRecordStatusService;
import com.jiangyifen.ec2.service.eaoservice.DepartmentService;
import com.jiangyifen.ec2.service.eaoservice.MarketingProjectService;
import com.jiangyifen.ec2.service.eaoservice.UserService;
import com.jiangyifen.ec2.ui.mgr.util.SqlGenerator;
import com.jiangyifen.ec2.utils.ParseDateSearchScope;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.PopupDateField;
import com.vaadin.ui.PopupView.Content;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class TaskDetailComplexFilter extends VerticalLayout implements
		ClickListener, ValueChangeListener, Content {
	/**
	 * 主要组件
	 */
	private GridLayout gridLayout;
	private Label countNo; // 查询结果记录数标签

	// 第1行
	private ComboBox projectComboBox; // 项目选择的ComboBox
	private ComboBox empNoComboBox;			// 工号选择框
	private ComboBox deptComboBox;		// 部门选择框
	private ComboBox mgrQcComboBox;		// 管理员质检选择框
	private ComboBox statusComboBox; // 外呼记录状态的ComboBox

	// 第2行
	private TextField customerName; // 客户姓名输入文本框
//	private TextField customerMobile; // 客户手机输入文本框

	// 第3行
	private ComboBox createTimeScope; 			// “时间范围”选择框
	private PopupDateField startCreateTime; 	// “开始时间”选择框
	private PopupDateField finishCreateTime; 	// “截止时间”选择框

	// 第4行
	private ComboBox orderTimeScope;			// “预约时间”选择框
	private PopupDateField startOrderTime;		// “起始预约”选择框
	private PopupDateField finishOrderTime;		// “截止预约”选择框
	
	// 按钮
	private Button searchButton; // 刷新结果按钮
	private Button clearButton; // 清空输入内容
	
	/**
	 * 其他组件
	 */
	// 持有外呼记录的引用
	private TaskDetailWindow taskDetailWindow;
	
	private ValueChangeListener createTimeScopeListener;
	private ValueChangeListener startCreateTimeListener;
	private ValueChangeListener finishCreateTimeListener;

	private ValueChangeListener orderTimeScopeListener;
	private ValueChangeListener startOrderTimeListener;
	private ValueChangeListener finishOrderTimeListener;
	
	/**
	 * Service
	 */
	private Domain domain;
	private User loginUser;
	private MarketingProjectService projectService;
	private DepartmentService departmentService;
	private CustomerServiceRecordStatusService customerServiceRecordStatusService;
	private UserService userService;
	
/**
 * 构造器
 */
	public TaskDetailComplexFilter(TaskDetailWindow taskDetailWindow) {
		initService();
		this.taskDetailWindow = taskDetailWindow;
		this.setSpacing(true);
		// GridLayout组件
		gridLayout = new GridLayout(7, 4);
		gridLayout.setCaption("搜索条件");
		gridLayout.setSpacing(true);
		gridLayout.setMargin(true, true, false, true);
		this.addComponent(gridLayout);
		
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
		
		/* ===============第1行组件================== */
		// 任务项目选择框
		Label projectLabel = new Label("项目选择：");
		projectLabel.setWidth("-1px");
		gridLayout.addComponent(projectLabel, 0, 0);
		gridLayout.addComponent(buildProjectComboBox(allGovernedDeptIds), 1, 0);

		// 项目类别选择框
		Label statusLabel = new Label("呼叫结果：");
		statusLabel.setWidth("-1px");
		gridLayout.addComponent(statusLabel, 2, 0);
		gridLayout.addComponent(buildStatusComboBox(), 3, 0);

		//客服工号
		Label empnoLabel = new Label("客服工号：");
		empnoLabel.setWidth("-1px");
		gridLayout.addComponent(empnoLabel, 4, 0);
		gridLayout.addComponent(buildEmpNoComboBox(allGovernedDeptIds),5,0);
		
		// 查询结果总数显示标签
		countNo = new Label("<B>结果总数：0</B>", Label.CONTENT_XHTML);
		countNo.setWidth("-1px");
		gridLayout.addComponent(countNo, 6, 0);

		/* ===============第2行组件================== */
		// 时间范围选中框
		Label timeScopeLabel = new Label("时间范围：");
		timeScopeLabel.setWidth("-1px");
		gridLayout.addComponent(timeScopeLabel, 0, 1);
		gridLayout.addComponent(buildTimeScopeComboBox(), 1, 1);
		
		//从事件，与到时间
		Label startTimeLabel = new Label("开始时间：");
		startTimeLabel.setWidth("-1px");
		gridLayout.addComponent(startTimeLabel, 2, 1);
		startCreateTime = new PopupDateField();
		startCreateTime.setWidth("160px");
		startCreateTime.setDateFormat("yyyy-MM-dd HH:mm:ss");
		startCreateTime.setResolution(PopupDateField.RESOLUTION_SEC);
		startCreateTime.setImmediate(true);
		startCreateTimeListener=new Property.ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				createTimeScope.removeListener(createTimeScopeListener);
				createTimeScope.setValue("全部");
				createTimeScope.addListener(createTimeScopeListener);
			}
		};
		startCreateTime.addListener(startCreateTimeListener);
		gridLayout.addComponent(startCreateTime, 3, 1);

		// 截止时间选中框
		Label finishTimeLabel = new Label("截止时间：");
		finishTimeLabel.setWidth("-1px");
		gridLayout.addComponent(finishTimeLabel, 4, 1);

		finishCreateTime = new PopupDateField();
		finishCreateTime.setWidth("160px");
		finishCreateTime.setDateFormat("yyyy-MM-dd HH:mm:ss");
		finishCreateTime.setResolution(PopupDateField.RESOLUTION_SEC);
		finishCreateTime.setImmediate(true);
		finishCreateTimeListener=new Property.ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				createTimeScope.removeListener(createTimeScopeListener);
				createTimeScope.setValue("全部");
				createTimeScope.addListener(createTimeScopeListener);
			}
		};
		finishCreateTime.addListener(finishCreateTimeListener);
		gridLayout.addComponent(finishCreateTime, 5, 1);
		
		// 清空按钮
		clearButton = new Button("清 空", this);
		clearButton.addStyleName("small");
		gridLayout.addComponent(clearButton, 6, 1);
		gridLayout.setComponentAlignment(clearButton, Alignment.MIDDLE_RIGHT);
		
		/* ===============第3行组件================== */
		// 客户姓名输入区
		Label customerNameLabel = new Label("客户姓名：");
		customerNameLabel.setWidth("-1px");
		gridLayout.addComponent(customerNameLabel, 0, 2);

		customerName = new TextField();
		customerName.setWidth("150px");
		customerName.setInputPrompt("请输入客户姓名");
		gridLayout.addComponent(customerName, 1, 2);

		// 客户手机号输入区
		Label deptLabel = new Label("部门选择：");
		deptLabel.setWidth("-1px");
		gridLayout.addComponent(deptLabel, 2, 2);

		gridLayout.addComponent(buildDeptComboBox(), 3, 2);
		
		Label mgrQcLabel = new Label("Mgr质检：");
		mgrQcLabel.setWidth("-1px");
		gridLayout.addComponent(mgrQcLabel, 4, 2);
		gridLayout.addComponent(buildMgrQcComboBox(), 5, 2);
		
		// jrh 如果是预约状态，则进行一些查询 	
		this.createFilterHLayout4();
		
//		// 客户手机号输入区
//		gridLayout.addComponent(new Label("客户手机："), 2, 2);
//
//		customerMobile = new TextField();
//		customerMobile.setWidth("158px");
//		customerMobile.setInputPrompt("如：15988889999");
//		gridLayout.addComponent(customerMobile, 3, 2);

		// 查询按钮
		searchButton = new Button("查 询", this);
		searchButton.addStyleName("small");
		gridLayout.addComponent(searchButton, 6, 2);
		gridLayout.setComponentAlignment(searchButton, Alignment.MIDDLE_RIGHT);
	}

	/**
	 * 初始化Service
	 */
	private void initService() {
		loginUser = SpringContextHolder.getLoginUser();
		domain=SpringContextHolder.getDomain();
		projectService = SpringContextHolder.getBean("marketingProjectService");
		userService=SpringContextHolder.getBean("userService");
		departmentService=SpringContextHolder.getBean("departmentService");
		customerServiceRecordStatusService = SpringContextHolder.getBean("customerServiceRecordStatusService");
	}
	/**
	 * jrh 按预约时间查询
	 */
	private void createFilterHLayout4() {
		// 联系时间选中框
		Label timeScopeLabel = new Label("预约时间：");
		timeScopeLabel.setWidth("-1px");
		timeScopeLabel.setVisible(false);
		gridLayout.addComponent(timeScopeLabel, 0, 3);
		
		orderTimeScope = new ComboBox();
		orderTimeScope.addItem("今天");
		orderTimeScope.addItem("昨天");
		orderTimeScope.addItem("本周");
		orderTimeScope.addItem("上周");
		orderTimeScope.addItem("本月");
		orderTimeScope.addItem("上月");
		orderTimeScope.addItem("精确时间");
		orderTimeScope.setValue("今天");
		orderTimeScope.setWidth("150px");
		orderTimeScope.setVisible(false);
		orderTimeScope.setImmediate(true);
		orderTimeScope.setNullSelectionAllowed(false);
		gridLayout.addComponent(orderTimeScope, 1, 3);
		
		orderTimeScopeListener = new ValueChangeListener() {
			public void valueChange(ValueChangeEvent event) {
				String scopeValue=(String)orderTimeScope.getValue();
				if("精确时间".equals(scopeValue)) {
					return;
				}
				startOrderTime.removeListener(startOrderTimeListener);
				finishOrderTime.removeListener(finishOrderTimeListener);
				Date[] dates = ParseDateSearchScope.parseToDate(scopeValue);
				startOrderTime.setValue(dates[0]);
				finishOrderTime.setValue(dates[1]);
				startOrderTime.addListener(startOrderTimeListener);
				finishOrderTime.addListener(finishOrderTimeListener);
			}
		};
		orderTimeScope.addListener(orderTimeScopeListener);
	
		Date[] dates = ParseDateSearchScope.parseToDate("今天");
		
		// 起始联系选中框
		Label startTimeLabel = new Label("起始预约：");
		startTimeLabel.setVisible(false);
		startTimeLabel.setWidth("-1px");
		gridLayout.addComponent(startTimeLabel, 2, 3);
	
		startOrderTimeListener = new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				orderTimeScope.removeListener(orderTimeScopeListener);
				orderTimeScope.setValue("精确时间");
				orderTimeScope.addListener(orderTimeScopeListener);
			}
		};
		
		startOrderTime = new PopupDateField();
		startOrderTime.setImmediate(true);
		startOrderTime.setVisible(false);
		startOrderTime.setWidth("160px");
		startOrderTime.setValue(dates[0]);
		startOrderTime.setDateFormat("yyyy-MM-dd HH:mm:ss");
		startOrderTime.setParseErrorMessage("时间格式不合法");
		startOrderTime.setResolution(PopupDateField.RESOLUTION_SEC);
		startOrderTime.addListener(startOrderTimeListener);
		gridLayout.addComponent(startOrderTime, 3, 3);
	
		// 截止联系选中框
		Label finishTimeLabel = new Label("截止预约：");
		finishTimeLabel.setWidth("-1px");
		finishTimeLabel.setVisible(false);
		gridLayout.addComponent(finishTimeLabel, 4, 3);
	
		finishOrderTimeListener = new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				orderTimeScope.removeListener(finishOrderTimeListener);
				orderTimeScope.setValue("精确时间");
				orderTimeScope.addListener(orderTimeScopeListener);
			}
		};
		
		finishOrderTime = new PopupDateField();
		finishOrderTime.setImmediate(true);
		finishOrderTime.setVisible(false);
		finishOrderTime.setWidth("160px");
		finishOrderTime.setValue(dates[1]);
		finishOrderTime.setDateFormat("yyyy-MM-dd HH:mm:ss");
		finishOrderTime.setParseErrorMessage("时间格式不合法");
		finishOrderTime.setResolution(PopupDateField.RESOLUTION_SEC);
		finishOrderTime.addListener(finishOrderTimeListener);
		gridLayout.addComponent(finishOrderTime, 5, 3);
	}

	/**
	 * 创建选择记录状态的ComboBox
	 * 
	 * @return
	 */
	private ComboBox buildStatusComboBox() {
		// 从数据库中获取所有记录状态信息，并绑定到Container中
		BeanItemContainer<CustomerServiceRecordStatus> statusContainer = new BeanItemContainer<CustomerServiceRecordStatus>(
				CustomerServiceRecordStatus.class);
		CustomerServiceRecordStatus status = new CustomerServiceRecordStatus();
		status.setStatusName("全部");
		statusContainer.addBean(status);
		statusContainer.addAll(customerServiceRecordStatusService
				.getAll(domain));

		// 创建ComboBox
		statusComboBox = new ComboBox();
		statusComboBox.setContainerDataSource(statusContainer);
		statusComboBox.setValue(status);
		statusComboBox.setWidth("158px");
		statusComboBox.addListener(this);
		statusComboBox.setImmediate(true);
		statusComboBox.setNullSelectionAllowed(false);
		for(CustomerServiceRecordStatus recordStatus : statusContainer.getItemIds()) {
			if(recordStatus.getStatusName().equals("全部")) {
				continue;
			}
			String direction = "incoming".equals(recordStatus.getDirection()) ? "呼入" : "呼出";
			statusComboBox.setItemCaption(recordStatus, recordStatus.getStatusName()+" - "+direction);
		}
		
		return statusComboBox;
	}


	/**
	 * 创建选择项目的ComboBox
	 * 
	 * @return
	 */
	private ComboBox buildProjectComboBox(List<Long> allGovernedDeptIds) {
		// jrh 获取当前管理员所管辖部门下创建的项目
		List<MarketingProject> projects = projectService.getAllByDepartments(allGovernedDeptIds, domain.getId());
		
		// 从数据库中获取“项目”信息，并绑定到Container中
		BeanItemContainer<MarketingProject> projectContainer = new BeanItemContainer<MarketingProject>(
				MarketingProject.class);
		MarketingProject project = new MarketingProject();
		project.setProjectName("全部");
		projectContainer.addBean(project);
		projectContainer.addAll(projects);
		
		// 创建ComboBox
		projectComboBox = new ComboBox();
		projectComboBox.setContainerDataSource(projectContainer);
		projectComboBox.setValue(project);
		projectComboBox.setWidth("150px");
		projectComboBox.setNullSelectionAllowed(false);
		return projectComboBox;
	}

	
	/**
	 * 创建客服工号的ComboBox
	 * 
	 * @return
	 */
	private ComboBox buildEmpNoComboBox(List<Long> allGovernedDeptIds) {
		// 从数据库中获取“客服”信息，并绑定到Container中
		BeanItemContainer<User> userContainer = new BeanItemContainer<User>(User.class);
		User user=new User();
		user.setEmpNo("全部");
		userContainer.addBean(user);
		userContainer.addAll(userService.getCsrsByDepartment(allGovernedDeptIds, domain.getId()));
		
		// 创建ComboBox
		empNoComboBox = new ComboBox();
		empNoComboBox.setContainerDataSource(userContainer);
		empNoComboBox.setItemCaptionPropertyId("empNo");
		empNoComboBox.setValue(user);
		empNoComboBox.setWidth("158px");
		empNoComboBox.setNullSelectionAllowed(false);
		return empNoComboBox;
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
		deptComboBox.setWidth("158px");
		deptComboBox.setNullSelectionAllowed(false);
		return deptComboBox;
	}
	
	/**
	 * Mgr质检
	 * 
	 * @return
	 */
	private ComboBox buildMgrQcComboBox() {
		// 创建ComboBox
		mgrQcComboBox = new ComboBox();
		mgrQcComboBox.addItem("全部");
		for (QcMgr qcMgr : QcMgr.values()) {
			mgrQcComboBox.addItem(qcMgr);
		}
		mgrQcComboBox.setWidth("158px");
		mgrQcComboBox.setNullSelectionAllowed(false);
		mgrQcComboBox.setValue("全部");
		return mgrQcComboBox;
	}
	
	
	/**
	 * 创建时间段的组合框
	 * 
	 * @return
	 */
	private ComboBox buildTimeScopeComboBox() {
		
		createTimeScope = new ComboBox();
		createTimeScope.addItem("全部");
		createTimeScope.addItem("今天");
		createTimeScope.addItem("昨天");
		createTimeScope.addItem("本周");
		createTimeScope.addItem("上周");
		createTimeScope.addItem("本月");
		createTimeScope.addItem("上月");
		createTimeScope.setValue("全部");
		createTimeScope.setWidth("150px");
		createTimeScope.setNullSelectionAllowed(false);
		createTimeScope.setImmediate(true);
		createTimeScopeListener = new Property.ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				startCreateTime.removeListener(startCreateTimeListener);
				finishCreateTime.removeListener(finishCreateTimeListener);
				String scopeValue=(String)createTimeScope.getValue();
				if(scopeValue.equals("全部")){
					startCreateTime.setValue(null);
					finishCreateTime.setValue(null);
				}else{
					Date[] dates=parseToDate(scopeValue);
					startCreateTime.setValue(dates[0]);
					finishCreateTime.setValue(dates[1]);
				}
				startCreateTime.addListener(startCreateTimeListener);
				finishCreateTime.addListener(finishCreateTimeListener);
			}
		};
		createTimeScope.addListener(createTimeScopeListener);
		return createTimeScope;
	}

	/**
	 * 解析字符串到日期
	 * @param timeScopeValue
	 * @return
	 */
	// 将用户选择的组合框中的值解析为日期字符串
	private static Date[] parseToDate(String timeScopeValue) {
		Date[] dates = new Date[2]; // 存放开始于结束时间
		Calendar cal = Calendar.getInstance(); // 取得当前时间
		cal.setTime(new Date());

		if ("今天".equals(timeScopeValue)) {
			dates[0] = cal.getTime();

			cal.add(Calendar.DAY_OF_WEEK, +1);
			dates[1] = cal.getTime();
		} else if ("昨天".equals(timeScopeValue)) {
			cal.add(Calendar.DAY_OF_WEEK, -1);
			dates[0] = cal.getTime();

			cal.add(Calendar.DAY_OF_WEEK, +1);
			dates[1] = cal.getTime();
		} else if ("本周".equals(timeScopeValue)) {
			cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
			dates[0] = cal.getTime(); // 本周的第一天

			cal.set(Calendar.DAY_OF_WEEK,
					cal.getActualMaximum(Calendar.DAY_OF_WEEK));
			cal.add(Calendar.DAY_OF_WEEK, +2);
			dates[1] = cal.getTime(); // 本周的最后一天
		} else if ("本月".equals(timeScopeValue)) {
			cal.set(Calendar.DAY_OF_MONTH,
					cal.getActualMinimum(Calendar.DAY_OF_MONTH));
			dates[0] = cal.getTime(); // 本月第一天

			cal.set(Calendar.DAY_OF_MONTH,
					cal.getActualMaximum(Calendar.DAY_OF_MONTH));
			cal.add(Calendar.DAY_OF_YEAR, +1);
			dates[1] = cal.getTime(); // 本月最后一天
		} else if ("上周".equals(timeScopeValue)) {
			cal.add(Calendar.WEEK_OF_MONTH, -1);
			cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
			dates[0] = cal.getTime(); // 上周的第一天

			cal.set(Calendar.DAY_OF_WEEK,
					cal.getActualMaximum(Calendar.DAY_OF_WEEK));
			cal.add(Calendar.DAY_OF_WEEK, +2);
			dates[1] = cal.getTime(); // 上周的最后一天
		} else if ("上月".equals(timeScopeValue)) {
			cal.add(Calendar.MONTH, -1);
			cal.set(Calendar.DAY_OF_MONTH,
					cal.getActualMinimum(Calendar.DAY_OF_MONTH));
			dates[0] = cal.getTime(); // 上月月第一天

			cal.set(Calendar.DAY_OF_MONTH,
					cal.getActualMaximum(Calendar.DAY_OF_MONTH));
			cal.add(Calendar.DAY_OF_YEAR, +1);
			dates[1] = cal.getTime(); // 上月最后一天
		}
		//目的是将日期格式的时分秒设为00:00:00
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
		try {
			dates[0]=sdf.parse(sdf.format(dates[0]));
			dates[1]=sdf.parse(sdf.format(dates[1]));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return dates;
	}

	
	/**
	 * 由buttonClick调用，执行搜索
	 */
	private void executeSearch() {
		// 项目选择信息
		String projectIdStr = "";
		MarketingProject project = (MarketingProject) projectComboBox
				.getValue();
		if (!"全部".equals(project.getProjectName())) {
			projectIdStr = project.getId().toString();
		}
		
		// 用户工号选择信息
		User user= (User) empNoComboBox
				.getValue();
		String empNoStr = "";
		if ("全部".equals(user.getEmpNo())) {
			empNoStr = "";
		} else {
			empNoStr = user.getEmpNo().toString();
		}
				
		// 外呼记录选择信息
		CustomerServiceRecordStatus recordStatu = (CustomerServiceRecordStatus) statusComboBox
				.getValue();
		String statusName = recordStatu.getStatusName();
		Long recordStatuId = null;
		if (!"全部".equals(statusName)) {
			recordStatuId = recordStatu.getId();
		}
		
		//对于指定时间范围进行查询
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date startTimeValue = (Date)startCreateTime.getValue();
		Date finishTimeValue = (Date)finishCreateTime.getValue();
		String startCreateTimeStr="";
		if(startTimeValue!=null){	
			startCreateTimeStr=sdf.format(startTimeValue);
		}
		String finishCreateTimeStr="";
		if(finishTimeValue!=null){
			finishCreateTimeStr=sdf.format(finishTimeValue);
		}
		
		// 预约时间
		String specificStartOrderTimeSql = "";
		if(startOrderTime.isVisible() && startOrderTime.getValue() != null) {
			specificStartOrderTimeSql = sdf.format(startOrderTime.getValue());
		}
		String specificFinishOrderTimeSql = "";
		if(finishOrderTime.isVisible() && finishOrderTime.getValue() != null) {
			specificFinishOrderTimeSql = sdf.format(finishOrderTime.getValue());
		}
		
		//客户姓名
		String customerNameStr="";
		if(customerName.getValue()!=null){
			customerNameStr=customerName.getValue().toString().trim();
		}

//		//客户手机
//		String customerMobileStr="";
//		if(customerMobile.getValue()!=null){
//			customerMobileStr=customerMobile.getValue().toString().trim();
//		}

		// Sql生成器
		SqlGenerator sqlGenerator = new SqlGenerator("CustomerServiceRecord");

		// 项目过滤
		SqlGenerator.Equal projectId = new SqlGenerator.Equal(
				"marketingProject.id", projectIdStr, false);
		sqlGenerator.addAndCondition(projectId);

		// 工号过滤
		SqlGenerator.Equal empNo = new SqlGenerator.Equal(
				"creator.empNo", empNoStr, false);
		sqlGenerator.addAndCondition(empNo);
		
		// 客服记录状态过滤
		if(recordStatuId != null) {
			SqlGenerator.Equal statu = new SqlGenerator.Equal(
					"serviceRecordStatus.id", recordStatuId.toString(), true);
			sqlGenerator.addAndCondition(statu);
		}
		
		// 客户姓名
		SqlGenerator.Like name = new SqlGenerator.Like("customerResource.name", customerNameStr);
		sqlGenerator.addAndCondition(name);

//		// 客户手机
//		SqlGenerator.Like mobile = new SqlGenerator.Like("customerResource.mobile", customerMobileStr);
//		sqlGenerator.addAndCondition(mobile);
		
		//时间过滤
		SqlGenerator.Between timeBetween=new SqlGenerator.Between("createDate", startCreateTimeStr, finishCreateTimeStr, true);
		sqlGenerator.addAndCondition(timeBetween);

		// 预约时间过滤
		SqlGenerator.Between orderTimeBetween=new SqlGenerator.Between("orderTime", specificStartOrderTimeSql, specificFinishOrderTimeSql, true);
		sqlGenerator.addAndCondition(orderTimeBetween);
		

		//chb  部门选择信息
		Department department= (Department) deptComboBox.getValue();
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
				SqlGenerator.Equal orEqual = new SqlGenerator.Equal("creator.department.id", deptId.toString(), false);
				sqlGenerator.addOrCondition(orEqual);
			}
		} else {//显示选中的部门
			SqlGenerator.Equal andEqual = new SqlGenerator.Equal("creator.department.id", department.getId().toString(), false);
			sqlGenerator.addAndCondition(andEqual);
		}
		
		// QcMgr状态
		if(!mgrQcComboBox.getValue().equals("全部")){
			QcMgr qcMgr = (QcMgr)mgrQcComboBox.getValue();
			if (qcMgr != null) {
				String statuStr = qcMgr.getClass().getName() + ".";
				if (qcMgr.getIndex() == 0) {
					statuStr += "NOTQUALIFIED";
				} else if (qcMgr.getIndex() == 1) {
					statuStr += "QUALIFIED";
				}
				SqlGenerator.Equal qcMgrstatu = new SqlGenerator.Equal(
						"qcMgr", statuStr, false);
				sqlGenerator.addAndCondition(qcMgrstatu);
			}
		}
		
		// 排序
		sqlGenerator.setOrderBy("id", SqlGenerator.DESC);

		taskDetailWindow.setSqlSelect(sqlGenerator.generateSelectSql());
		taskDetailWindow.setSqlCount(sqlGenerator.generateCountSql());
		
		// 更新外呼记录Table，并使Table处于未选中
		taskDetailWindow.updateTable(true);
		if (taskDetailWindow.getTable() != null) {
			taskDetailWindow.getTable().setValue(null);
		}
		countNo.setValue("<B>结果总数：" + taskDetailWindow.getFlip().getTotalRecord() + "</B>");
	}

	@Override
	public void valueChange(ValueChangeEvent event) {
		Property source = event.getProperty();
		if(source == statusComboBox) {
			CustomerServiceRecordStatus serviceRecordStatus = (CustomerServiceRecordStatus) statusComboBox.getValue();
			String statusName = serviceRecordStatus.getStatusName();
			boolean iscontain = statusName.contains("预约"); 
			for(int i = 0; i < gridLayout.getColumns(); i++) {
				Component component = gridLayout.getComponent(i, 3);
				if(component != null) {
					component.setVisible(iscontain);
				}
			}
		}
	}

	/**
	 * 搜索而和清空按钮监听器
	 */
	@Override
	public void buttonClick(ClickEvent event) {
		if (searchButton == event.getButton()) {
			executeSearch();
		} else if (clearButton == event.getButton()) {
			projectComboBox.select(projectComboBox.getItemIds().toArray()[0]);
			statusComboBox.select(statusComboBox.getItemIds().toArray()[0]);
			deptComboBox.select(deptComboBox.getItemIds().toArray()[0]);
			mgrQcComboBox.select(mgrQcComboBox.getItemIds().toArray()[0]);
			createTimeScope.setValue("全部");
			startCreateTime.setValue(null);
			finishCreateTime.setValue(null);
			customerName.setValue("");
			
//			customerMobile.setValue("");
		}
	}

	@Override
	public String getMinimizedValueAsHTML() {
		return "高级查询";
	}

	@Override
	public Component getPopupComponent() {
		return gridLayout;
	}
}
