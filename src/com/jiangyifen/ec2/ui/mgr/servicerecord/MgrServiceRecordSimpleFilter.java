package com.jiangyifen.ec2.ui.mgr.servicerecord;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import com.jiangyifen.ec2.ui.mgr.tabsheet.MgrServiceRecord;
import com.jiangyifen.ec2.ui.mgr.util.SqlGenerator;
import com.jiangyifen.ec2.ui.util.NotificationUtil;
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
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.PopupDateField;
import com.vaadin.ui.PopupView;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.Notification;

@SuppressWarnings("serial")
public class MgrServiceRecordSimpleFilter extends VerticalLayout implements
		ClickListener {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	/**
	 * 主要组件
	 */
	private ComboBox projectComboBox; 	// 项目选择的ComboBox
	private ComboBox statusComboBox; 	// 外呼记录状态的ComboBox
	private ComboBox timeScope;			// “联系时间”选择框
	private ComboBox empNoComboBox;		// 工号选择框
	private ComboBox deptComboBox;		// 部门选择框
//	private ComboBox csrQcComboBox;		// 坐席质检选择框
	private ComboBox mgrQcComboBox;		// 管理员质检选择框
	private PopupDateField startTime;	// “起始时间”选择框
	private PopupDateField finishTime;	// “截止时间”选择框
	private TextField phoneNumberField; // 电话号码
	private Button searchButton; 		// 刷新结果按钮
	private Button clearButton; 		// 清空输入内容
	
	/**
	 * 其他组件
	 */
	// 持有外呼记录的引用
	private MgrServiceRecord mgrServiceRecord;
	private ValueChangeListener timeScopeListener;
	private ValueChangeListener startTimeListener;
	private ValueChangeListener finishTimeListener;
	
	/**
	 * Service
	 */
	private Domain domain;
	private User loginUser;
	private MarketingProjectService projectService;
	private CustomerServiceRecordStatusService customerServiceRecordStatusService;
	private UserService userService;
	private DepartmentService departmentService;
	
	/**
	 * 构造器
	 * 
	 * @param mgrServiceRecord
	 */
	public MgrServiceRecordSimpleFilter(MgrServiceRecord mgrServiceRecord) {
		initService();
		this.mgrServiceRecord = mgrServiceRecord;
		this.setSpacing(true);
		// Grid布局管理器
		GridLayout gridLayout = new GridLayout(8, 3);
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
		
		// 时间范围选中框
		gridLayout.addComponent(new Label("联系时间："), 0, 0);
		gridLayout.addComponent(buildTimeScopeComboBox(), 1, 0);
		
		// 开始时间选中框
		gridLayout.addComponent(new Label("起始时间："), 2, 0);
		
		startTime = new PopupDateField();
		startTime.setWidth("160px");
		startTime.setValidationVisible(false);
		startTime.setDateFormat("yyyy-MM-dd HH:mm:ss");
		startTime.setResolution(PopupDateField.RESOLUTION_SEC);
		startTime.setImmediate(true);
		Date[] dates = ParseDateSearchScope.parseToDate("今天");
		startTimeListener=new Property.ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				timeScope.removeListener(timeScopeListener);
				timeScope.setValue("精确时间");
				timeScope.addListener(timeScopeListener);
			}
		};
		startTime.setValue(dates[0]);
		startTime.addListener(startTimeListener);
		gridLayout.addComponent(startTime, 3, 0);

		// 截止时间选中框
		gridLayout.addComponent(new Label("截止时间："), 4, 0);
		finishTime = new PopupDateField();
		finishTime.setWidth("160px");
		finishTime.setValidationVisible(false);
		finishTime.setDateFormat("yyyy-MM-dd HH:mm:ss");
		finishTime.setResolution(PopupDateField.RESOLUTION_SEC);
		finishTime.setImmediate(true);
		finishTimeListener=new Property.ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				timeScope.removeListener(timeScopeListener);
				timeScope.setValue("精确时间");
				timeScope.addListener(timeScopeListener);
			}
		};
		finishTime.setValue(dates[1]);
		finishTime.addListener(finishTimeListener);
		gridLayout.addComponent(finishTime, 5, 0);
		
		// 任务项目选择框
		gridLayout.addComponent(new Label("项目选择："), 0, 1);
		gridLayout.addComponent(buildProjectComboBox(allGovernedDeptIds), 1, 1);

		// 呼叫结果选择框
		gridLayout.addComponent(new Label("联系结果："), 2, 1);
		gridLayout.addComponent(buildStatusComboBox(), 3, 1);
		
		//客服工号
		gridLayout.addComponent(new Label("客服工号："), 4, 1);
		gridLayout.addComponent(buildEmpNoComboBox(allGovernedDeptIds), 5, 1);
		
		// 清空按钮
		clearButton = new Button("清 空", (ClickListener) this);
		clearButton.addStyleName("small");
		gridLayout.addComponent(clearButton, 6, 1);
		gridLayout.setComponentAlignment(clearButton, Alignment.MIDDLE_RIGHT);

		// 查询按钮
		searchButton = new Button("查 询", (ClickListener) this);
		searchButton.setStyleName("default");
		searchButton.addStyleName("small");
		gridLayout.addComponent(searchButton, 6, 0);

		
		gridLayout.addComponent(new Label("部门选择："), 0, 2);
		gridLayout.addComponent(buildDeptComboBox(), 1, 2);
		
//		gridLayout.addComponent(new Label("坐席质检："), 2, 2);
//		gridLayout.addComponent(buildCsrQcComboBox(), 3, 2);
		
		gridLayout.addComponent(new Label("Mgr质检："), 2, 2);
		gridLayout.addComponent(buildMgrQcComboBox(), 3, 2);
		
		gridLayout.addComponent(new Label("客户电话："), 4, 2);
		gridLayout.addComponent(buildPhoneNumberComboBox(), 5, 2);
		

		// 高级查询的弹出组件
		MgrServiceRecordComplexFilter complexFilter = new MgrServiceRecordComplexFilter(mgrServiceRecord);
		complexFilter.setHeight("-1px");
		PopupView complexPop = new PopupView(complexFilter);
		complexPop.setHideOnMouseOut(false);
		gridLayout.addComponent(complexPop, 6, 2);
		
		timeScope.setValue("今天");
	}
	
	/**
	 * 初始化Service
	 */
	private void initService() {
		loginUser = SpringContextHolder.getLoginUser();
		domain=SpringContextHolder.getDomain();
		projectService = SpringContextHolder.getBean("marketingProjectService");
		userService=SpringContextHolder.getBean("userService");
		customerServiceRecordStatusService = SpringContextHolder.getBean("customerServiceRecordStatusService");
		departmentService = SpringContextHolder.getBean("departmentService");
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
		statusContainer.addAll(customerServiceRecordStatusService.getAll(domain));

		// 创建ComboBox
		statusComboBox = new ComboBox();
		statusComboBox.setContainerDataSource(statusContainer);
		statusComboBox.setValue(status);
		statusComboBox.setWidth("158px");
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
		projectComboBox.setWidth("140px");
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
				/**
				 * 存在的问题：
				 * 	当当前用户拥有多个 manager 角色时，并且每个 manager 角色所管辖的部门不同；
				 *  这个时候就会出现这个用户每次登录之后在查询客服记录的时候，部门下拉选项每次显示的部门信息不一致
				 */
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
	
//	/**
//	 * Csr质检
//	 * 
//	 * @return
//	 */
//	private ComboBox buildCsrQcComboBox() {
//		// 创建ComboBox
//		csrQcComboBox = new ComboBox();
//		csrQcComboBox.addItem("全部");
//		for (QcCsr qcCsr : QcCsr.values()) {
//			csrQcComboBox.addItem(qcCsr);
//		}
//		csrQcComboBox.setWidth("158px");
//		csrQcComboBox.setNullSelectionAllowed(false);
//		csrQcComboBox.setValue("全部");
//		return csrQcComboBox;
//	}
	
	/**
	 * 客户电话
	 * @return
	 */
	private TextField buildPhoneNumberComboBox() {
		// 创建TextField
		phoneNumberField =new TextField();
		phoneNumberField.setWidth("158px");
		phoneNumberField.setValue("");
		return phoneNumberField;
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
		
		timeScope = new ComboBox();
		timeScope.addItem("今天");
		timeScope.addItem("昨天");
		timeScope.addItem("本周");
		timeScope.addItem("上周");
		timeScope.addItem("本月");
		timeScope.addItem("上月");
		timeScope.addItem("精确时间");
		timeScope.setValue("今天");
		timeScope.setWidth("140px");
		timeScope.setNullSelectionAllowed(false);
		timeScope.setImmediate(true);
		timeScopeListener = new Property.ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				startTime.removeListener(startTimeListener);
				finishTime.removeListener(finishTimeListener);
				String scopeValue=(String)timeScope.getValue();
				if(scopeValue.equals("精确时间")){
					return;
				}
				startTime.removeListener(startTimeListener);
				finishTime.removeListener(finishTimeListener);
				Date[] dates=parseToDate((String)timeScope.getValue());
				startTime.setValue(dates[0]);
				finishTime.setValue(dates[1]);
				startTime.addListener(startTimeListener);
				finishTime.addListener(finishTimeListener);
			}
		};
		timeScope.addListener(timeScopeListener);
		return timeScope;
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
			int day = cal.get(Calendar.DAY_OF_WEEK);
			if(day == 1) {
				cal.add(Calendar.DAY_OF_YEAR, -1);
			}
			
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
		MarketingProject project = (MarketingProject) projectComboBox
				.getValue();
		String projectIdStr = "";
		if ("全部".equals(project.getProjectName())) {
			projectIdStr = "";
		} else {
			projectIdStr = project.getId().toString();
		}

		// 用户工号选择信息
		User user= (User) empNoComboBox.getValue();
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
		SimpleDateFormat sdf = new SimpleDateFormat(
		 "yyyy-MM-dd HH:mm:ss");
		Date startTimeValue = (Date)startTime.getValue();
		Date finishTimeValue = (Date)finishTime.getValue();
		String startTimeStr="";
		if(startTimeValue!=null){	
			startTimeStr=sdf.format(startTimeValue);
		}
		String finishTimeStr="";
		if(finishTimeValue!=null){
			finishTimeStr=sdf.format(finishTimeValue);
		}
		
		//电话号码
		String phoneNumber = (String)phoneNumberField.getValue();
		phoneNumber=phoneNumber.trim();
		if(!phoneNumber.equals("")&&!StringUtils.isNumeric(phoneNumber)){
			NotificationUtil.showWarningNotification(this, "电话号码不合法");
		}
		
		// Sql生成器
		SqlGenerator sqlGenerator = new SqlGenerator("CustomerServiceRecord");
		
		//时间过滤
		SqlGenerator.Between timeBetween=new SqlGenerator.Between("createDate", startTimeStr, finishTimeStr, true);
		sqlGenerator.addAndCondition(timeBetween);
				
		// 项目过滤
		SqlGenerator.Equal projectId = new SqlGenerator.Equal(
				"marketingProject.id", projectIdStr, false);
		sqlGenerator.addAndCondition(projectId);

		// 工号过滤
		SqlGenerator.Equal empNo = new SqlGenerator.Equal(
				"creator.empNo", empNoStr, true);
		sqlGenerator.addAndCondition(empNo);
		
		// 客服记录状态过滤
		if(recordStatuId != null) {
			SqlGenerator.Equal statu = new SqlGenerator.Equal(
					"serviceRecordStatus.id", recordStatuId.toString(), true);
			sqlGenerator.addAndCondition(statu);
		}

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
		
//		// QcCsr状态
//		if(!csrQcComboBox.getValue().equals("全部")){
//			QcCsr qcCsr = (QcCsr)csrQcComboBox.getValue();
//			if (qcCsr != null) {
//				String statuStr = qcCsr.getClass().getName() + ".";
//				if (qcCsr.getIndex() == 0) {
//					statuStr += "NOTQUALIFIED";
//				} else if (qcCsr.getIndex() == 1) {
//					statuStr += "QUALIFIED";
//				}
//				SqlGenerator.Equal qcCsrstatu = new SqlGenerator.Equal(
//						"qcCsr", statuStr, false);
//				sqlGenerator.addAndCondition(qcCsrstatu);
//			}
//		}

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
				SqlGenerator.Equal qcMgrstatu = new SqlGenerator.Equal("qcMgr", statuStr, false);
				sqlGenerator.addAndCondition(qcMgrstatu);
			}
		}
		
		String selectSql=sqlGenerator.generateSelectSql();
		String countSql=sqlGenerator.generateCountSql();
		
		if(!StringUtils.isEmpty(phoneNumber)){
			selectSql += " and e.customerResource.id in (select p.customerResource.id from Telephone as p where p.number like '%"+ phoneNumber + "%') ";
			countSql += " and e.customerResource.id in (select p.customerResource.id from Telephone as p where p.number like '%"+ phoneNumber + "%') ";
		}
		
		selectSql=selectSql+" order by e.id desc";
		
		mgrServiceRecord.setSqlSelect(selectSql);
		mgrServiceRecord.setSqlCount(countSql);
		
		//如果导出时，导出的文件名称
		String fileName=project.getProjectName();
		mgrServiceRecord.setExportFileName(fileName);
		
		// 更新外呼记录Table，并使Table处于未选中
		mgrServiceRecord.updateTable(true);
		if (mgrServiceRecord.getTable() != null) {
			mgrServiceRecord.getTable().setValue(null);
		}
	}

	/**
	 * 取得搜索按钮，以调用触发按钮的click事件
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
			try {
				if(!startTime.isValid() || startTime.getValue() == null) {
					startTime.getApplication().getMainWindow().showNotification("起始联系时间格式不正确，请修改后重试！", Notification.TYPE_WARNING_MESSAGE);
					return;
				}
				
				if(!finishTime.isValid() || finishTime.getValue() == null) {
					finishTime.getApplication().getMainWindow().showNotification("截止联系时间格式不正确，请修改后重试！", Notification.TYPE_WARNING_MESSAGE);
					return;
				}
				
				Date startTimeDate = (Date) startTime.getValue();
				Date finishTimeDate = (Date) finishTime.getValue();
				Calendar cal = Calendar.getInstance();
				cal.setTime(finishTimeDate);
				cal.add(Calendar.MONTH, -1);
				Date lastMonth = cal.getTime();
				if(startTimeDate.before(lastMonth)) {
					finishTime.getApplication().getMainWindow().showNotification("对不起，单次只能查询一个月范围内的数据，请修改后重试！", Notification.TYPE_WARNING_MESSAGE);
					return;
				}
	
				executeSearch();
			} catch (Exception e) {
				e.printStackTrace();
				logger.error("管理员客服记录简单查询 出现异常"+e.getMessage(), e);
				finishTime.getApplication().getMainWindow().showNotification("对不起，查询失败，请重试！", Notification.TYPE_WARNING_MESSAGE);
			}
		} else if (clearButton == event.getButton()) {
			//初始化查询条件的选择
			timeScope.setValue("今天");
			projectComboBox.select(projectComboBox.getItemIds().toArray()[0]);
			statusComboBox.select(statusComboBox.getItemIds().toArray()[0]);
			deptComboBox.select(deptComboBox.getItemIds().toArray()[0]);
//			csrQcComboBox.select(csrQcComboBox.getItemIds().toArray()[0]);
			mgrQcComboBox.select(mgrQcComboBox.getItemIds().toArray()[0]);
			
		}
	}

}
