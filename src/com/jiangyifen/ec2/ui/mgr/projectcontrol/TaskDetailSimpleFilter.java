package com.jiangyifen.ec2.ui.mgr.projectcontrol;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.jiangyifen.ec2.bean.RoleType;
import com.jiangyifen.ec2.entity.CustomerResourceBatch;
import com.jiangyifen.ec2.entity.CustomerServiceRecordStatus;
import com.jiangyifen.ec2.entity.Department;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.Role;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.service.eaoservice.CustomerServiceRecordStatusService;
import com.jiangyifen.ec2.service.eaoservice.DepartmentService;
import com.jiangyifen.ec2.service.eaoservice.UserService;
import com.jiangyifen.ec2.ui.util.NotificationUtil;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.validator.RegexpValidator;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.PopupDateField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.Notification;

@SuppressWarnings("serial")
public class TaskDetailSimpleFilter extends VerticalLayout implements
		ClickListener {

	private Notification warning_notification;	// jrh 错误警告提示信息
	
	/**
	 * 主要组件
	 */
	private ComboBox timeScope; // “时间范围”选择框
	private PopupDateField startTime; // “开始时间”选择框
	private PopupDateField finishTime; // “截止时间”选择框
	private ComboBox batchComboBox; // 批次选择的ComboBox
	private ComboBox finishStatusComboBox; // 完成情况ComboBox
	private ComboBox empNoComboBox; // 任务坐席选择框
	private ComboBox accountMgrComboBox; // 客服经理选择框
	private ComboBox answerStatusComboBox; // 接通情况选择框
	private ComboBox distributeStatusComboBox; // 分配情况选择框
	private ComboBox useableStatusComboBox; // 启用状态选择框
	private ComboBox statusComboBox; 	// 外呼记录状态的ComboBox
	private TextField customerId_tf;		// jrh 客户编号输入文本框
	
	private Button searchButton; // 刷新结果按钮
	private Button clearButton; // 清空输入内容

	//预约时间
	private ComboBox orderTimeScope; // “时间范围”选择框
	private PopupDateField startOrderTime; // “开始时间”选择框
	private PopupDateField finishOrderTime; // “截止时间”选择框

	/**
	 * 其他组件
	 */
	// 持有外呼记录的引用
	private TaskDetailWindow taskDetailWindow;
	private ValueChangeListener timeScopeListener;
	private ValueChangeListener startTimeListener;
	private ValueChangeListener finishTimeListener;

	private ValueChangeListener orderTimeScopeListener;
	private ValueChangeListener startOrderTimeListener;
	private ValueChangeListener finishOrderTimeListener;

		
	/**
	 * Service
	 */
	private Domain domain;
	private User loginUser;
	private DepartmentService departmentService;
	private CustomerServiceRecordStatusService customerServiceRecordStatusService;
	private UserService userService;

	/**
	 * 构造器
	 * 
	 * @param taskDetailWindow
	 */
	public TaskDetailSimpleFilter(TaskDetailWindow taskDetailWindow) {
		initService();
		this.taskDetailWindow = taskDetailWindow;
		this.setSpacing(true);

		warning_notification = new Notification("", Notification.TYPE_WARNING_MESSAGE);
		warning_notification.setDelayMsec(0);
		warning_notification.setHtmlContentAllowed(true);
		
		// Grid布局管理器
		GridLayout gridLayout = new GridLayout(8, 4);
		gridLayout.setSpacing(true);
		gridLayout.setMargin(false, true, false, true);
		this.addComponent(gridLayout);

		// jrh 获取当前用户所属部门及其所有角色的管辖部门的Id号
		List<Long> allGovernedDeptIds = new ArrayList<Long>();
		for (Role role : loginUser.getRoles()) {
			if (role.getType().equals(RoleType.manager)) {
				List<Department> departments = departmentService
						.getGovernedDeptsByRole(role.getId());
				if (departments.isEmpty()) {
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

		// 时间范围选中框
		gridLayout.addComponent(new Label("更新日期："), 0, 0);
		gridLayout.addComponent(buildTimeScopeComboBox(), 1, 0);

		// 开始时间选中框
		gridLayout.addComponent(new Label("开始时间："), 2, 0);

		startTime = new PopupDateField();
		startTime.setWidth("160px");
		startTime.setDateFormat("yyyy-MM-dd HH:mm:ss");
		startTime.setResolution(PopupDateField.RESOLUTION_SEC);
		startTime.setImmediate(true);
		startTimeListener = new Property.ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				timeScope.removeListener(timeScopeListener);
				timeScope.setValue("全部");
				timeScope.addListener(timeScopeListener);
			}
		};
		startTime.addListener(startTimeListener);
		gridLayout.addComponent(startTime, 3, 0);

		// 截止时间选中框
		gridLayout.addComponent(new Label("截止时间："), 4, 0);

		finishTime = new PopupDateField();
		finishTime.setWidth("160px");
		finishTime.setDateFormat("yyyy-MM-dd HH:mm:ss");
		finishTime.setResolution(PopupDateField.RESOLUTION_SEC);
		finishTime.setImmediate(true);
		finishTimeListener = new Property.ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				timeScope.removeListener(timeScopeListener);
				timeScope.setValue("全部");
				timeScope.addListener(timeScopeListener);
			}
		};
		finishTime.addListener(finishTimeListener);
		gridLayout.addComponent(finishTime, 5, 0);
		
		//========================预约时间
		// 时间范围选中框
		gridLayout.addComponent(new Label("预约日期："), 0, 1);
		gridLayout.addComponent(buildOrderTimeScopeComboBox(), 1, 1);

		// 开始时间选中框
		gridLayout.addComponent(new Label("开始时间："), 2, 1);
				
		startOrderTime = new PopupDateField();
		startOrderTime.setWidth("160px");
		startOrderTime.setDateFormat("yyyy-MM-dd HH:mm:ss");
		startOrderTime.setResolution(PopupDateField.RESOLUTION_SEC);
		startOrderTime.setImmediate(true);
		startOrderTimeListener = new Property.ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				orderTimeScope.removeListener(timeScopeListener);
				orderTimeScope.setValue("全部");
				orderTimeScope.addListener(timeScopeListener);
			}
		};
		startOrderTime.addListener(startOrderTimeListener);
		gridLayout.addComponent(startOrderTime, 3, 1);

		// 截止时间选中框
		gridLayout.addComponent(new Label("截止时间："), 4, 1);
		
		finishOrderTime = new PopupDateField();
		finishOrderTime.setWidth("160px");
		finishOrderTime.setDateFormat("yyyy-MM-dd HH:mm:ss");
		finishOrderTime.setResolution(PopupDateField.RESOLUTION_SEC);
		finishOrderTime.setImmediate(true);
		finishOrderTimeListener = new Property.ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				orderTimeScope.removeListener(orderTimeScopeListener);
				orderTimeScope.setValue("全部");
				orderTimeScope.addListener(orderTimeScopeListener);
			}
		};
		finishOrderTime.addListener(finishOrderTimeListener);
		gridLayout.addComponent(finishOrderTime, 5, 1);

		// 任务项目选择框
		gridLayout.addComponent(new Label("批次选择："), 0, 2);
		gridLayout.addComponent(buildBatchesComboBox(), 1, 2);

		// 完成情况选择框
		gridLayout.addComponent(new Label("完成情况："), 2, 2);
		gridLayout.addComponent(buildFinishStatusComboBox(), 3, 2);

		// 客服工号
		gridLayout.addComponent(new Label("任务坐席："), 4, 2);
		gridLayout.addComponent(buildEmpNoComboBox(allGovernedDeptIds), 5, 2);
		
		// 客服工号
		gridLayout.addComponent(new Label("客户经理："), 6, 2);
		gridLayout.addComponent(buildAccountMgrComboBox(allGovernedDeptIds), 7, 2);

		// 分配情况
		Label distributeLable = new Label("分配情况：");
		distributeLable.setWidth("-1");
		gridLayout.addComponent(distributeLable, 6, 0);
		gridLayout.addComponent(buildDistributeStatusComboBox(), 7, 0);

		// 任务状态
		Label statusLable = new Label("任务状态：");
		distributeLable.setWidth("-1");
		gridLayout.addComponent(statusLable, 6, 1);
		gridLayout.addComponent(buildStatusComboBox(), 7, 1);

		HorizontalLayout operator_hlo = new HorizontalLayout();
		operator_hlo.setSpacing(true);
		gridLayout.addComponent(operator_hlo, 6, 3, 7, 3);
		
		// 清空按钮
		clearButton = new Button("清 空", (ClickListener) this);
		clearButton.addStyleName("small");
		operator_hlo.addComponent(clearButton);

		// 查询按钮
		searchButton = new Button("查 询", (ClickListener) this);
		searchButton.setStyleName("default");
		searchButton.addStyleName("small");
		operator_hlo.addComponent(searchButton);


		gridLayout.addComponent(new Label("接通情况："), 0, 3);
		gridLayout.addComponent(buildAnswerStatusComboBox(), 1, 3);

		gridLayout.addComponent(new Label("启用状态："), 2, 3);
		gridLayout.addComponent(buildUseableStatusComboBox(), 3, 3);
		
		gridLayout.addComponent(new Label("客户编号："), 4, 3);
		gridLayout.addComponent(buildCustomerIdTextField(), 5, 3);

		// // 高级查询的弹出组件
		// TaskDetailComplexFilter complexFilter = new
		// TaskDetailComplexFilter(taskDetailWindow);
		// complexFilter.setHeight("-1px");
		// PopupView complexPop = new PopupView(complexFilter);
		// complexPop.setHideOnMouseOut(false);
		// gridLayout.addComponent(complexPop, 6, 2);
	}

	/**
	 * 初始化Service
	 */
	private void initService() {
		loginUser = SpringContextHolder.getLoginUser();
		domain = SpringContextHolder.getDomain();
		userService = SpringContextHolder.getBean("userService");
		departmentService = SpringContextHolder.getBean("departmentService");
		customerServiceRecordStatusService = SpringContextHolder.getBean("customerServiceRecordStatusService");
	}

	/**
	 * 创建选择记录状态的ComboBox
	 * 
	 * @return
	 */
	private ComboBox buildFinishStatusComboBox() {
		// 创建ComboBox
		finishStatusComboBox = new ComboBox();
		finishStatusComboBox.addItem("全部");
		finishStatusComboBox.addItem("已完成");
		finishStatusComboBox.addItem("未完成");

		// 选中全部
		finishStatusComboBox.setValue("全部");
		finishStatusComboBox.setWidth("158px");
		finishStatusComboBox.setNullSelectionAllowed(false);
		return finishStatusComboBox;
	}

	/**
	 * 创建选择项目的ComboBox
	 * 
	 * @return
	 */
	private ComboBox buildBatchesComboBox() { // 暂时没有权限控制
		// 创建ComboBox
		batchComboBox = new ComboBox();
		batchComboBox.setWidth("140px");
		batchComboBox.setNullSelectionAllowed(false);

		// 获取当前项目的批次
		List<CustomerResourceBatch> batches = new ArrayList<CustomerResourceBatch>(taskDetailWindow.getCurrentProject().getBatches());
		
		// 从数据库中获取“批次”信息，并绑定到Container中
		BeanItemContainer<CustomerResourceBatch> batchContainer = new BeanItemContainer<CustomerResourceBatch>(
				CustomerResourceBatch.class);
		CustomerResourceBatch batch = new CustomerResourceBatch();
		batch.setBatchName("全部");
		batchContainer.addBean(batch);
		batchContainer.addAll(batches);
		batchComboBox.setContainerDataSource(batchContainer);
		batchComboBox.setValue(batch);
		return batchComboBox;
	}

	@Override
	public void attach() {
		super.attach();
		
		//更新批次选择框的组件
		// 获取当前项目的批次
		List<CustomerResourceBatch> batches = new ArrayList<CustomerResourceBatch>(taskDetailWindow.getCurrentProject().getBatches());
		
		// 从数据库中获取“批次”信息，并绑定到Container中
		BeanItemContainer<CustomerResourceBatch> batchContainer = new BeanItemContainer<CustomerResourceBatch>(
				CustomerResourceBatch.class);
		CustomerResourceBatch batch = new CustomerResourceBatch();
		batch.setBatchName("全部");
		batchContainer.addBean(batch);
		batchContainer.addAll(batches);
		batchComboBox.setContainerDataSource(batchContainer);
		batchComboBox.setValue(batch);
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
		statusContainer.addAll(customerServiceRecordStatusService.getAllByDirection(domain, "outgoing"));

		// 创建ComboBox
		statusComboBox = new ComboBox();
		statusComboBox.setContainerDataSource(statusContainer);
		statusComboBox.setValue(status);
		statusComboBox.setWidth("140px");
		statusComboBox.setNullSelectionAllowed(false);
		for(CustomerServiceRecordStatus recordStatus : statusContainer.getItemIds()) {
			if(recordStatus.getStatusName().equals("全部")) {
				continue;
			}
			statusComboBox.setItemCaption(recordStatus, recordStatus.getStatusName());
		}

		return statusComboBox;
	}
	
	
	/**
	 * 创建客服工号的ComboBox
	 * 
	 * @return
	 */
	private ComboBox buildEmpNoComboBox(List<Long> allGovernedDeptIds) {
		// 从数据库中获取“客服”信息，并绑定到Container中
		BeanItemContainer<User> userContainer = new BeanItemContainer<User>(
				User.class);
		User user = new User();
		user.setEmpNo("全部");
		userContainer.addBean(user);
		userContainer.addAll(userService.getCsrsByDepartment(
				allGovernedDeptIds, domain.getId()));

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
	 * 创建客服经理的ComboBox
	 * 
	 * @return
	 */
	private ComboBox buildAccountMgrComboBox(List<Long> allGovernedDeptIds) {
		// 从数据库中获取“客服”信息，并绑定到Container中
		BeanItemContainer<User> userContainer = new BeanItemContainer<User>(
				User.class);
		User user = new User();
		user.setEmpNo("全部");
		userContainer.addBean(user);
		userContainer.addAll(userService.getCsrsByDepartment(allGovernedDeptIds, domain.getId()));

		// 创建ComboBox
		accountMgrComboBox = new ComboBox();
		accountMgrComboBox.setContainerDataSource(userContainer);
		accountMgrComboBox.setItemCaptionPropertyId("empNo");
		accountMgrComboBox.setValue(user);
		accountMgrComboBox.setWidth("140px");
		accountMgrComboBox.setNullSelectionAllowed(false);
		return accountMgrComboBox;
	}
	
	/**
	 * 创建分配情况ComboBox
	 * 
	 * @return
	 */
	private ComboBox buildDistributeStatusComboBox() {
		// 创建ComboBox
		distributeStatusComboBox = new ComboBox();
		distributeStatusComboBox.addItem("全部");
		distributeStatusComboBox.addItem("已分配");
		distributeStatusComboBox.addItem("未分配");
		distributeStatusComboBox.setValue("未分配");
		distributeStatusComboBox.setWidth("140px");
		distributeStatusComboBox.setNullSelectionAllowed(false);
		return distributeStatusComboBox;
	}
	
	/**
	 * 创建应答状态ComboBox
	 * 
	 * @return
	 */
	private ComboBox buildAnswerStatusComboBox() {
		// 创建ComboBox
		answerStatusComboBox = new ComboBox();
		answerStatusComboBox.addItem("全部");
		answerStatusComboBox.addItem("已接通");
		answerStatusComboBox.addItem("未接通");
		answerStatusComboBox.setValue("全部");
		answerStatusComboBox.setWidth("140px");
		answerStatusComboBox.setNullSelectionAllowed(false);
		return answerStatusComboBox;
	}

	/**
	 * 可用状态
	 * 
	 * @return
	 */
	private ComboBox buildUseableStatusComboBox() {
		// 创建ComboBox
		useableStatusComboBox = new ComboBox();
		useableStatusComboBox.addItem("全部");
		useableStatusComboBox.addItem("已启用");
		useableStatusComboBox.addItem("未启用");
		useableStatusComboBox.setWidth("158px");
		useableStatusComboBox.setNullSelectionAllowed(false);
		useableStatusComboBox.setValue("全部");
		return useableStatusComboBox;
	}
	
	/**
	 * jrh 客户编号
	 * @return
	 */
	private TextField buildCustomerIdTextField() {
		customerId_tf = new TextField();
		customerId_tf.addValidator(new RegexpValidator("\\d+", "客户id 只能由数字组成！"));
		customerId_tf.setValidationVisible(false);
		customerId_tf.setWidth("158px");
		return customerId_tf;
	}

	/**
	 * 创建时间段的组合框
	 * 
	 * @return
	 */
	private ComboBox buildTimeScopeComboBox() {

		timeScope = new ComboBox();
		timeScope.addItem("全部");
		timeScope.addItem("今天");
		timeScope.addItem("昨天");
		timeScope.addItem("本周");
		timeScope.addItem("上周");
		timeScope.addItem("本月");
		timeScope.addItem("上月");
		timeScope.setValue("全部");
		timeScope.setWidth("140px");
		timeScope.setNullSelectionAllowed(false);
		timeScope.setImmediate(true);
		timeScopeListener = new Property.ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				startTime.removeListener(startTimeListener);
				finishTime.removeListener(finishTimeListener);
				String scopeValue = (String) timeScope.getValue();
				if (scopeValue.equals("全部")) {
					startTime.setValue(null);
					finishTime.setValue(null);
				} else {
					Date[] dates = parseToDate((String) timeScope.getValue());
					startTime.setValue(dates[0]);
					finishTime.setValue(dates[1]);
				}
				startTime.addListener(startTimeListener);
				finishTime.addListener(finishTimeListener);
			}
		};
		timeScope.addListener(timeScopeListener);
		return timeScope;
	}
	
	/**
	 * 创建时间段的组合框
	 * 
	 * @return
	 */
	private ComboBox buildOrderTimeScopeComboBox() {
		
		orderTimeScope = new ComboBox();
		orderTimeScope.addItem("全部");
		orderTimeScope.addItem("今天");
		orderTimeScope.addItem("昨天");
		orderTimeScope.addItem("本周");
		orderTimeScope.addItem("上周");
		orderTimeScope.addItem("本月");
		orderTimeScope.addItem("上月");
		orderTimeScope.setValue("全部");
		orderTimeScope.setWidth("140px");
		orderTimeScope.setNullSelectionAllowed(false);
		orderTimeScope.setImmediate(true);
		orderTimeScopeListener = new Property.ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				startOrderTime.removeListener(startOrderTimeListener);
				finishOrderTime.removeListener(finishOrderTimeListener);
				String scopeValue = (String) orderTimeScope.getValue();
				if (scopeValue.equals("全部")) {
					startOrderTime.setValue(null);
					finishOrderTime.setValue(null);
				} else {
					Date[] dates = parseToDate((String) orderTimeScope.getValue());
					startOrderTime.setValue(dates[0]);
					finishOrderTime.setValue(dates[1]);
				}
				startOrderTime.addListener(startOrderTimeListener);
				finishOrderTime.addListener(finishOrderTimeListener);
			}
		};
		orderTimeScope.addListener(orderTimeScopeListener);
		return orderTimeScope;
	}

	/**
	 * 解析字符串到日期
	 * 
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
		// 目的是将日期格式的时分秒设为00:00:00
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		try {
			dates[0] = sdf.parse(sdf.format(dates[0]));
			dates[1] = sdf.parse(sdf.format(dates[1]));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return dates;
	}

	/**
	 * 由buttonClick调用，执行搜索
	 */
	private void executeSearch() {
		String sqlCount = "select count(c) from MarketingProjectTask as c where"+ createFixedSql();
		String sqlSelect = sqlCount.replaceFirst("count\\(c\\)", "c")
				+ " order by c.id desc";
		
		taskDetailWindow.setSqlCount(sqlCount);
		taskDetailWindow.setSqlSelect(sqlSelect);
		taskDetailWindow.updateTable(false);
	}

	/**
	 * 创建其它搜索的Sql语句 
	 * @return
	 */
	private String createFixedSql() {
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss");
		// 时间范围
		String specificStartTimeSql = "";
		if (startTime.getValue() != null) {
			specificStartTimeSql = " and c.lastUpdateDate >= '"
					+ dateFormat.format(startTime.getValue()) + "'";
		}

		String specificFinishTimeSql = "";
		if (finishTime.getValue() != null) {
			specificFinishTimeSql = " and c.lastUpdateDate <= '"
					+ dateFormat.format(finishTime.getValue()) + "'";
		}

		// 预约时间范围
		String orderSpecificStartTimeSql = "";
		if (startOrderTime.getValue() != null) {
			specificStartTimeSql = " and c.orderTime >= '"
					+ dateFormat.format(startOrderTime.getValue()) + "'";
		}
		
		String orderSpecificFinishTimeSql = "";
		if (finishOrderTime.getValue() != null) {
			specificFinishTimeSql = " and c.orderTime <= '"
					+ dateFormat.format(finishOrderTime.getValue()) + "'";
		}

		// 批次情况
		String batchSql = "";
		Long batchId = ((CustomerResourceBatch) batchComboBox.getValue())
				.getId();
		if (batchId != null) {
			batchSql = " and c.batchId=" + batchId;
		}

		// 完成情况
		String finishSql = "";
		String finishStatus = (String) finishStatusComboBox.getValue();
		if (finishStatus.equals("已完成")) {
			finishSql = " and c.isFinished=true";
		} else if (finishStatus.equals("未完成")) {
			finishSql = " and c.isFinished=false";
		}

		// 分配情况
		String distributeSql = "";
		String distributeStatus = (String) distributeStatusComboBox.getValue();
		if (distributeStatus.equals("已分配")) {
			distributeSql = " and c.user is not null";
		} else if (distributeStatus.equals("未分配")) {
			distributeSql = " and c.user is null";
		}

		// 任务状态
		String statusSql = "";
		CustomerServiceRecordStatus status = (CustomerServiceRecordStatus) statusComboBox.getValue();
		if (status.getStatusName().equals("全部")) {
			//do nothing
		} else{
			statusSql = " and c.lastStatus='"+status.getStatusName()+"'";
		}


		// 工号选择
		String empNoSql = "";
		Long userId = ((User) empNoComboBox.getValue()).getId();
		if (userId != null) {
			empNoSql = " and c.user.id=" + userId;
		}
		
		// 客户经理选择
		String accountMgrSql = "";
		Long accountMgrId = ((User) accountMgrComboBox.getValue()).getId();
		if (accountMgrId != null) {
			accountMgrSql = " and c.customerResource.accountManager.id=" + accountMgrId;
		}

		// 接通状况
		String answerSql = "";
		String answerStatus = (String) answerStatusComboBox.getValue();
		if (answerStatus.equals("已接通")) {
			answerSql = " and c.isAnswered=true";
		} else if (answerStatus.equals("未接通")) {
			answerSql = " and c.isAnswered=false";
		}

		// 启用状况
		String usableSql = "";
		String usableStatus = (String) useableStatusComboBox.getValue();
		if (usableStatus.equals("已启用")) {
			answerSql = " and c.isUseable=true";
		} else if (usableStatus.equals("未启用")) {
			answerSql = " and c.isUseable=false";
		}
		
		// 客户编号
		String customerIdSql = "";
		String customerId = StringUtils.trimToEmpty((String) customerId_tf.getValue());
		if(!"".equals(customerId)) {
			customerIdSql = " and c.customerResource.id = " + customerId;
		}
		
		// 创建固定的搜索语句
		return " c.marketingProject.id="
				+ taskDetailWindow.getCurrentProject().getId() + specificStartTimeSql
				+ specificFinishTimeSql +orderSpecificStartTimeSql+orderSpecificFinishTimeSql+ batchSql + finishSql+statusSql + empNoSql+accountMgrSql+distributeSql
				+ answerSql + usableSql + customerIdSql + " and c.domain.id = " + domain.getId();
	}

	/**
	 * 取得搜索按钮，以调用触发按钮的click事件
	 * 
	 * @return
	 */
	public Button getSearchButton() {
		return searchButton;
	}

	/**
	 * 单击搜索和取消按钮的事件
	 */
	public void buttonClick(ClickEvent event) {
		if (searchButton == event.getButton()) {
			if(!customerId_tf.isValid()) {
				warning_notification.setCaption("客户编号只能由数字组成！");
				customerId_tf.getApplication().getMainWindow().showNotification(warning_notification);
				return;
			}
			try {
				executeSearch();
			} catch (Exception e) {
				NotificationUtil.showWarningNotification(this, "搜索出现异常！");
				e.printStackTrace();
			}
		} else if (clearButton == event.getButton()) {
			// 初始化查询条件的选择
			startTime.setValue(null);
			finishTime.setValue(null);
			startOrderTime.setValue(null);
			finishOrderTime.setValue(null);
			distributeStatusComboBox.select(distributeStatusComboBox.getItemIds().toArray()[0]);
			empNoComboBox.select(empNoComboBox.getItemIds().toArray()[0]);
			accountMgrComboBox.select(accountMgrComboBox.getItemIds().toArray()[0]);
			batchComboBox.select(batchComboBox.getItemIds().toArray()[0]);
			finishStatusComboBox.select(finishStatusComboBox.getItemIds().toArray()[0]);
			answerStatusComboBox.select(answerStatusComboBox.getItemIds().toArray()[0]);
			useableStatusComboBox.select(useableStatusComboBox.getItemIds().toArray()[0]);
			customerId_tf.setValue("");
		}
	}

}
