package com.jiangyifen.ec2.ui.mgr.servicerecord;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.jiangyifen.ec2.bean.RoleType;
import com.jiangyifen.ec2.entity.CustomerLevel;
import com.jiangyifen.ec2.entity.CustomerServiceRecordStatus;
import com.jiangyifen.ec2.entity.Department;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.MarketingProject;
import com.jiangyifen.ec2.entity.Role;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.entity.enumtype.QcMgr;
import com.jiangyifen.ec2.service.eaoservice.CustomerLevelService;
import com.jiangyifen.ec2.service.eaoservice.CustomerServiceRecordStatusService;
import com.jiangyifen.ec2.service.eaoservice.DepartmentService;
import com.jiangyifen.ec2.service.eaoservice.MarketingProjectService;
import com.jiangyifen.ec2.service.eaoservice.UserService;
import com.jiangyifen.ec2.ui.mgr.tabsheet.MgrServiceRecord;
import com.jiangyifen.ec2.ui.mgr.util.SqlGenerator;
import com.jiangyifen.ec2.utils.ParseDateSearchScope;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.validator.RegexpValidator;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.PopupDateField;
import com.vaadin.ui.PopupView.Content;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.Notification;
import com.vaadin.ui.themes.BaseTheme;

@SuppressWarnings("serial")
public class MgrServiceRecordComplexFilter extends VerticalLayout implements
		ClickListener, ValueChangeListener, Content {

	/**
	 * 主要组件
	 */
	private GridLayout gridLayout;

	// 第1行
	private ComboBox projectComboBox;   // 项目选择的ComboBox
	private ComboBox directionSelector;	// 呼叫方向选择框
	private ComboBox empNoComboBox;		// 工号选择框
	private Label countNo; // 查询结果记录数标签
	
	// 第2行
	private ComboBox createTimeScope; 			// “联系时间”选择框
	private PopupDateField startCreateTime; 	// “开始时间”选择框
	private PopupDateField finishCreateTime; 	// “截止时间”选择框


	// 第3行
	private TextField customerName; 		// 客户姓名输入文本框
	private ComboBox deptComboBox;			// 部门选择框
	private ComboBox mgrQcComboBox;			// 管理员质检选择框

	// jrh 第4行
	private ComboBox orderTimeScope;		// “预约时间”选择框
	private PopupDateField startOrderTime;	// “起始预约”选择框
	private PopupDateField finishOrderTime;	// “截止预约”选择框
	
	// 第五行
	private TextField customerId_tf;		// 客户编号输入文本框
	private TextField customerPhone_tf; 		// 客户电话输入文本框
	private ComboBox customerLevel_cb; 		// “客户等级”选择

	/******************  客服记录结果选择组件   ************************/
	private Button selectAll_bt;
	private Button noSelectAll_bt;
	private Button invertSelect_bt;
	private OptionGroup serviceResult_og;

	// 按钮
	private Button searchButton; // 刷新结果按钮
	private Button clearButton; // 清空输入内容
	
	/**
	 * 其他组件
	 */
	// 持有外呼记录的引用
	private MgrServiceRecord mgrOutgoingRecord;
	
	private ValueChangeListener createTimeScopeListener;
	private ValueChangeListener startCreateTimeListener;
	private ValueChangeListener finishCreateTimeListener;

	private ValueChangeListener orderTimeScopeListener;
	private ValueChangeListener startOrderTimeListener;
	private ValueChangeListener finishOrderTimeListener;

	// jrh
	private List<CustomerServiceRecordStatus> allServiceRecordStatus; 		// 所有的客服记录状态对象
	private List<CustomerServiceRecordStatus> allOutgoingRecordStatus; 		// 所有的客服记录中 呼出 记录的状态对象
	private List<CustomerServiceRecordStatus> allIncomingRecordStatus; 		// 所有的客服记录中 呼入 记录的状态对象
	private BeanItemContainer<CustomerServiceRecordStatus> serviceStatusContainer;	// 存放任务完成状态的数据源
	private BeanItemContainer<CustomerLevel> customerLevelContainer;		// 客户级别容器
	
	/**
	 * Service
	 */
	private Domain domain;
	private User loginUser;
	private MarketingProjectService projectService;
	private DepartmentService departmentService;
	private CustomerServiceRecordStatusService customerServiceRecordStatusService;
	private CustomerLevelService customerLevelService;						// 客户级别服务类
	private UserService userService;
	
/**
 * 构造器
 */
	public MgrServiceRecordComplexFilter(MgrServiceRecord mgrOutgoingRecord) {
		initService();
		this.mgrOutgoingRecord=mgrOutgoingRecord;
		this.setSpacing(true);
		this.setWidth("-1px");
		
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
		
		allServiceRecordStatus = customerServiceRecordStatusService.getAllByDeptIdsAndDirection(allGovernedDeptIds, "inAndOut", true, domain.getId());
		allOutgoingRecordStatus = customerServiceRecordStatusService.getAllByDeptIdsAndDirection(allGovernedDeptIds, "outgoing", true, domain.getId());
		allIncomingRecordStatus = customerServiceRecordStatusService.getAllByDeptIdsAndDirection(allGovernedDeptIds, "incoming", true, domain.getId());

		serviceStatusContainer = new BeanItemContainer<CustomerServiceRecordStatus>(CustomerServiceRecordStatus.class);
		serviceStatusContainer.addAll(allServiceRecordStatus);

		CustomerLevel customerLevel = new CustomerLevel();
		customerLevel.setLevelName("全部");
		customerLevel.setDomain(domain); 
		customerLevelContainer = new BeanItemContainer<CustomerLevel>(CustomerLevel.class);
		customerLevelContainer.addBean(customerLevel);
		customerLevelContainer.addAll(customerLevelService.getAll(domain));
		
		// GridLayout组件
		gridLayout = new GridLayout(7, 5);
		gridLayout.setCaption("搜索条件");
		gridLayout.setSpacing(true);
		gridLayout.setMargin(true, true, false, true);
		this.addComponent(gridLayout);
		
		// 创建联系结果的相关组件
		createStatusComponents();
		
		/* ===============第1行组件================== */
		// 任务项目选择框
		Label projectLabel = new Label("项目选择：");
		projectLabel.setWidth("-1px");
		gridLayout.addComponent(projectLabel, 0, 0);
		gridLayout.addComponent(buildProjectComboBox(allGovernedDeptIds), 1, 0);
		
		// 项目类别选择框
		Label directionLabel = new Label("呼叫方向：");
		directionLabel.setWidth("-1px");
		gridLayout.addComponent(directionLabel, 2, 0);
		gridLayout.addComponent(buildDirectionComboBox(), 3, 0);

		//客服工号
		Label empnoLabel = new Label("坐席工号：");
		empnoLabel.setWidth("-1px");
		gridLayout.addComponent(empnoLabel, 4, 0);
		gridLayout.addComponent(buildEmpNoComboBox(allGovernedDeptIds),5,0);
		
		// 查询结果总数显示标签
		countNo = new Label("<B>结果总数：0</B>", Label.CONTENT_XHTML);
		countNo.setWidth("-1px");
		gridLayout.addComponent(countNo, 6, 0);

		/* ===============第2行组件================== */
		// 时间范围选中框
		Label timeScopeLabel = new Label("联系时间：");
		timeScopeLabel.setWidth("-1px");
		gridLayout.addComponent(timeScopeLabel, 0, 1);
		gridLayout.addComponent(buildTimeScopeComboBox(), 1, 1);
		
		//从事件，与到时间
		Label startTimeLabel = new Label("开始时间：");
		startTimeLabel.setWidth("-1px");
		gridLayout.addComponent(startTimeLabel, 2, 1);
		startCreateTime = new PopupDateField();
		startCreateTime.setWidth("160px");
		startCreateTime.setValidationVisible(false);
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
		finishCreateTime.setValidationVisible(false);
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

		createTimeScope.setValue("今天");
		
		// 清空按钮
		clearButton = new Button("清 空", this);
//		clearButton.addStyleName("small");
		gridLayout.addComponent(clearButton, 6, 1);
		gridLayout.setComponentAlignment(clearButton, Alignment.MIDDLE_RIGHT);
		
		/* ===============第3行组件================== */
		// 客户手机号输入区
		Label deptLabel = new Label("部门选择：");
		deptLabel.setWidth("-1px");
		gridLayout.addComponent(deptLabel, 0, 2);
		
		gridLayout.addComponent(buildDeptComboBox(), 1, 2);
		
		Label mgrQcLabel = new Label("Mgr质检：");
		mgrQcLabel.setWidth("-1px");
		gridLayout.addComponent(mgrQcLabel, 2, 2);
		gridLayout.addComponent(buildMgrQcComboBox(), 3, 2);
		
		// 客户姓名输入区
		Label customerNameLabel = new Label("客户姓名：");
		customerNameLabel.setWidth("-1px");
		gridLayout.addComponent(customerNameLabel, 4, 2);

		customerName = new TextField();
		customerName.setWidth("158px");
		customerName.setInputPrompt("请输入客户姓名");
		gridLayout.addComponent(customerName, 5, 2);
		
		// 查询按钮
		searchButton = new Button("查 询", this);
		searchButton.addStyleName("default");
		gridLayout.addComponent(searchButton, 6, 2);
		gridLayout.setComponentAlignment(searchButton, Alignment.MIDDLE_RIGHT);
		
		// jrh 如果是预约状态，则进行一些查询 	
		this.createFilterHLayout4();
		
		// jrh 客户编号
		Label  customerIdLabel = new Label("客户编号：");
		customerIdLabel.setWidth("-1px");
		gridLayout.addComponent(customerIdLabel, 0, 4);
		gridLayout.addComponent(buildCustomerIdTextField(),1,4);

		// 客户电话输入区
		Label mobileLabel = new Label("客户电话：");
		mobileLabel.setWidth("-1px");
		gridLayout.addComponent(mobileLabel, 2, 4);
		
		customerPhone_tf = new TextField();
		customerPhone_tf.setWidth("158px");
		customerPhone_tf.addValidator(new RegexpValidator("\\d+", "电话号码只能由数字组成"));
		customerPhone_tf.setValidationVisible(false);
		gridLayout.addComponent(customerPhone_tf, 3, 4);
		
		// jrh 客户等级选择
		Label levelLabel = new Label("客户等级：");
		levelLabel.setWidth("-1px");
		gridLayout.addComponent(levelLabel, 4, 4);
		gridLayout.addComponent(buildCustomerLevelComboBox(), 5, 4);

	}

	/**
	 *  创建联系结果的相关组件
	 */
	private void createStatusComponents() {
		VerticalLayout serviceResult_vlo = new VerticalLayout();
		serviceResult_vlo.setSpacing(true);
		serviceResult_vlo.setMargin(false, true, false, true);
		this.addComponent(serviceResult_vlo);
		
		HorizontalLayout result_hlo = new HorizontalLayout();
		result_hlo.setSpacing(true);
		serviceResult_vlo.addComponent(result_hlo);
	
		Label result_lb = new Label("<b>联系结果：</b><font color='red'>(至少选择一个)</font>", Label.CONTENT_XHTML);
		result_lb.setWidth("-1px");
		result_hlo.addComponent(result_lb);
		
		selectAll_bt = new Button("全选", this);
		selectAll_bt.setImmediate(true);
		selectAll_bt.setStyleName(BaseTheme.BUTTON_LINK);
		result_hlo.addComponent(selectAll_bt);
		
		invertSelect_bt = new Button("反选", this);
		invertSelect_bt.setImmediate(true);
		invertSelect_bt.setStyleName(BaseTheme.BUTTON_LINK);
		result_hlo.addComponent(invertSelect_bt);
		
		noSelectAll_bt = new Button("全不选", this);
		noSelectAll_bt.setImmediate(true);
		noSelectAll_bt.setStyleName(BaseTheme.BUTTON_LINK);
		result_hlo.addComponent(noSelectAll_bt);
		
		serviceResult_og = new OptionGroup();
		serviceResult_og.addStyleName("fourcol800");			// 按三列排版
		serviceResult_og.addStyleName("boldcaption");
		serviceResult_og.setMultiSelect(true);
		serviceResult_og.addListener(this);
		serviceResult_og.setContainerDataSource(serviceStatusContainer);
		serviceResult_og.setNullSelectionAllowed(false);
		serviceResult_og.setImmediate(true);
		serviceResult_vlo.addComponent(serviceResult_og);
	
		// 修改客服记录状态显示名称
		editRecordStatusShowName();
	}

	/**
	 * 初始化Service
	 */
	private void initService() {
		loginUser = SpringContextHolder.getLoginUser();
		domain=SpringContextHolder.getDomain();
		projectService = SpringContextHolder.getBean("marketingProjectService");
		userService=SpringContextHolder.getBean("userService");
		customerLevelService=SpringContextHolder.getBean("customerLevelService");
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
		orderTimeScope.setValue("精确时间");
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
		startOrderTime.setValue(null);
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
		finishOrderTime.setValue(null);
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
	private ComboBox buildDirectionComboBox() {
		directionSelector = new ComboBox();
		directionSelector.setWidth("158px");
		directionSelector.addItem("全部");
		directionSelector.addItem("呼入");
		directionSelector.addItem("呼出");
		directionSelector.setValue("全部");
		directionSelector.setImmediate(true);
		directionSelector.setNullSelectionAllowed(false);
		addListenerToDirectionSelector();
		return directionSelector;
	}

	/**
	 * 为方向选择框添加监听器
	 */
	private void addListenerToDirectionSelector() {
		directionSelector.addListener(new ValueChangeListener() {
			public void valueChange(ValueChangeEvent event) {
				String value = (String) directionSelector.getValue();
				serviceStatusContainer.removeAllItems();
				if("全部".equals(value)) {
					serviceStatusContainer.addAll(allServiceRecordStatus);
				} else if("呼出".equals(value)) {
					serviceStatusContainer.addAll(allOutgoingRecordStatus);
				} else {
					serviceStatusContainer.addAll(allIncomingRecordStatus);
				}

				// 修改客服记录状态显示名称
				editRecordStatusShowName();
			}
		});
	}
	
	/**
	 * 修改客服记录状态显示名称
	 */
	private void editRecordStatusShowName() {
		serviceResult_og.setValue(null);
		for(CustomerServiceRecordStatus status : serviceStatusContainer.getItemIds()) {
			String direction = "incoming".equals(status.getDirection()) ? "呼入" : "呼出";
			serviceResult_og.setItemCaption(status, status.getStatusName()+" - "+direction);
			serviceResult_og.select(status);
		}
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
	 * jrh 创建客户编号查询组件
	 * @return
	 */
	private TextField buildCustomerIdTextField() {
		customerId_tf = new TextField();
		customerId_tf.addValidator(new RegexpValidator("\\d+", "客户id 只能由数字组成！"));
		customerId_tf.setValidationVisible(false);
		customerId_tf.setWidth("150px");
		return customerId_tf;
	}
	
	/**
	 * 客户等级选择
	 */
	private ComboBox buildCustomerLevelComboBox() {
		customerLevel_cb = new ComboBox();
		customerLevel_cb.setImmediate(true);
		customerLevel_cb.setWidth("158px");
		customerLevel_cb.setNullSelectionAllowed(false);
		customerLevel_cb.setItemCaptionPropertyId("levelName");
		customerLevel_cb.setContainerDataSource(customerLevelContainer);
		customerLevel_cb.select(customerLevelContainer.getItemIds().toArray()[0]);
		return customerLevel_cb;
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
		deptComboBox.setWidth("150px");
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
	@SuppressWarnings("unchecked")
	private void executeSearch() {
		// 项目选择信息
		String projectIdStr = "";
		MarketingProject project = (MarketingProject) projectComboBox
				.getValue();
		if (!"全部".equals(project.getProjectName())) {
			projectIdStr = project.getId().toString();
		}
		
		String directionSql = "";
		if("呼入".equals(directionSelector.getValue())) {
			directionSql = "incoming";
		} else if("呼出".equals(directionSelector.getValue())) {
			directionSql = "outgoing";
		}
		
		// 用户工号选择信息
		User user= (User) empNoComboBox.getValue();
		String empNoStr = "";
		if ("全部".equals(user.getEmpNo())) {
			empNoStr = "";
		} else {
			empNoStr = user.getEmpNo().toString();
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
		
		// jrh 客户编号
		String customerIdStr = StringUtils.trimToEmpty((String) customerId_tf.getValue());

		// Sql生成器
		SqlGenerator sqlGenerator = new SqlGenerator("CustomerServiceRecord");

		// 项目过滤
		SqlGenerator.Equal projectId = new SqlGenerator.Equal(
				"marketingProject.id", projectIdStr, false);
		sqlGenerator.addAndCondition(projectId);
		
		// jrh 方向过滤
		if(!"".equals(directionSql)) {
			SqlGenerator.Equal direction = new SqlGenerator.Equal(
					"serviceRecordStatus.direction", directionSql, true);
			sqlGenerator.addAndCondition(direction);
		}

		// 工号过滤
		SqlGenerator.Equal empNo = new SqlGenerator.Equal(
				"creator.empNo", empNoStr, false);
		sqlGenerator.addAndCondition(empNo);
		
		// jrh 客户编号
		if(!"".equals(customerIdStr)) {
			SqlGenerator.Equal customerId = new SqlGenerator.Equal(
					"customerResource.id", customerIdStr, false);
			sqlGenerator.addAndCondition(customerId);
		}
		
		// 客户姓名
		SqlGenerator.Like name = new SqlGenerator.Like("customerResource.name", customerNameStr);
		sqlGenerator.addAndCondition(name);
		
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

		String searchSql = sqlGenerator.generateSelectSql();
		String countSql = sqlGenerator.generateCountSql();

		Set<CustomerServiceRecordStatus> recordStatusSet = (Set<CustomerServiceRecordStatus>) serviceResult_og.getValue();
		if(recordStatusSet.size() != serviceStatusContainer.getItemIds().size()) {
			ArrayList<Long> statusIds = new ArrayList<Long>();
			for(CustomerServiceRecordStatus status : recordStatusSet) {
				statusIds.add(status.getId());
			}
			String statusIdSql = StringUtils.join(statusIds, ",");
			searchSql += " and e.serviceRecordStatus.id in ("+statusIdSql+")";
			countSql += " and e.serviceRecordStatus.id in ("+statusIdSql+")";
		}
		
		CustomerLevel customerLevel = (CustomerLevel) customerLevel_cb.getValue();	// jrh  客户等级 2014-06-09
		if(customerLevel.getId() != null) {
			searchSql += " and e.customerResource.customerLevel.id = " + customerLevel.getId();
			countSql += " and e.customerResource.customerLevel.id = " + customerLevel.getId();
		}

		//电话号码
		String phoneNumber = ((String) customerPhone_tf.getValue()).trim();
		if(!"".equals(phoneNumber)){
			searchSql += " and e.customerResource.id in (select p.customerResource.id from Telephone as p where p.number like '%"+ phoneNumber + "%') ";
			countSql += " and e.customerResource.id in (select p.customerResource.id from Telephone as p where p.number like '%"+ phoneNumber + "%') ";
		}

		// 排序
		searchSql += " order by e.id desc";
		
		mgrOutgoingRecord.setSqlSelect(searchSql);
		mgrOutgoingRecord.setSqlCount(countSql);
		//如果导出时，导出的文件名称
		String fileName=project.getProjectName();
		mgrOutgoingRecord.setExportFileName(fileName);
		
		// 更新外呼记录Table，并使Table处于未选中
		mgrOutgoingRecord.updateTable(true);
		if (mgrOutgoingRecord.getTable() != null) {
			mgrOutgoingRecord.getTable().setValue(null);
		}
		countNo.setValue("<B>结果总数：" + mgrOutgoingRecord.getFlip().getTotalRecord() + "</B>");
	}

	@SuppressWarnings("unchecked")
	@Override
	public void valueChange(ValueChangeEvent event) {
		Property source = event.getProperty();
		if(source == serviceResult_og) {
			Set<CustomerServiceRecordStatus> recordStatusSet = (Set<CustomerServiceRecordStatus>) serviceResult_og.getValue();
			if(recordStatusSet.size() == 1) {	// 当管理员选择的联系结果只有1个时， 并且选择的客服记录结果是‘预约’，则让预约时间选择组件 可见
				CustomerServiceRecordStatus status = recordStatusSet.iterator().next();
				String statusName = status.getStatusName();
				boolean iscontain = statusName.contains("预约"); 
				for(int i = 0; i < gridLayout.getColumns(); i++) {
					Component component = gridLayout.getComponent(i, 3);
					if(component != null) {
						component.setVisible(iscontain);
					}
				}
			} else {	// 当管理员选择的联系结果超过1个时，则让预约时间选择组件不可见
				for(int i = 0; i < gridLayout.getColumns(); i++) {
					Component component = gridLayout.getComponent(i, 3);
					if(component != null) {
						component.setVisible(false);
					}
				}
			}
		} 
	}

	/**
	 * 搜索而和清空按钮监听器
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if (searchButton == source) {
			if(!customerId_tf.isValid()) {
				customerId_tf.getApplication().getMainWindow().showNotification("客户编号只能由数字组成，请修改后重试！", Notification.TYPE_WARNING_MESSAGE);
				return;
			}
			
			if(!customerPhone_tf.isValid()) {
				customerPhone_tf.getApplication().getMainWindow().showNotification("客户电话只能由数字组成，请修改后重试！", Notification.TYPE_WARNING_MESSAGE);
				return;
			}
			
			if(!startCreateTime.isValid() || startCreateTime.getValue() == null) {
				startCreateTime.getApplication().getMainWindow().showNotification("起始联系时间格式不正确，请修改后重试！", Notification.TYPE_WARNING_MESSAGE);
				return;
			}
			
			if(!finishCreateTime.isValid() || finishCreateTime.getValue() == null) {
				finishCreateTime.getApplication().getMainWindow().showNotification("截止联系时间格式不正确，请修改后重试！", Notification.TYPE_WARNING_MESSAGE);
				return;
			}
			
			Date startTimeDate = (Date) startCreateTime.getValue();
			Date finishTimeDate = (Date) finishCreateTime.getValue();
			Calendar cal = Calendar.getInstance();
			cal.setTime(finishTimeDate);
			cal.add(Calendar.MONTH, -1);
			Date lastMonth = cal.getTime();
			if(startTimeDate.before(lastMonth)) {
				finishCreateTime.getApplication().getMainWindow().showNotification("对不起，单次只能查询一个月范围内的数据，请修改后重试！", Notification.TYPE_WARNING_MESSAGE);
				return;
			}

			executeSearch();
		} else if (clearButton == source) {
			projectComboBox.select(projectComboBox.getItemIds().toArray()[0]);
			directionSelector.setValue("全部");
			selectAll_bt.click();

			deptComboBox.select(deptComboBox.getItemIds().toArray()[0]);
			mgrQcComboBox.select(mgrQcComboBox.getItemIds().toArray()[0]);
			
			createTimeScope.setValue("今天");
			customerName.setValue("");
			
			orderTimeScope.setValue("精确时间");
			startOrderTime.setValue(null);
			finishOrderTime.setValue(null);
			
			empNoComboBox.setValue(empNoComboBox.getItemIds().toArray()[0]);
			customerId_tf.setValue("");
			customerPhone_tf.setValue("");
			customerLevel_cb.select(customerLevel_cb.getItemIds().toArray()[0]);
		} else if(source == selectAll_bt) {
			for(CustomerServiceRecordStatus status : serviceStatusContainer.getItemIds()) {
				serviceResult_og.select(status);
			}
		} else if(source == invertSelect_bt) {
			Set<CustomerServiceRecordStatus> recordStatusSet = (Set<CustomerServiceRecordStatus>) serviceResult_og.getValue();
			serviceResult_og.setValue(null);	// 然后先清空选项
			
			CustomerServiceRecordStatus selectedStatus = null;
			for(CustomerServiceRecordStatus status : serviceStatusContainer.getItemIds()) {
				if(!recordStatusSet.contains(status)) {
					serviceResult_og.select(status);
				}
				
				if(selectedStatus == null) {
					selectedStatus = status;
				}
			}
			Set<CustomerServiceRecordStatus> currentSelectedStatusSet = (Set<CustomerServiceRecordStatus>) serviceResult_og.getValue();
			if(currentSelectedStatusSet.size() == 0) {	// 要保证，至少选择一个客服记录结果
				serviceResult_og.select(selectedStatus);
			}
			
		} else if(source == noSelectAll_bt) {
			serviceResult_og.setValue(null);
			for(CustomerServiceRecordStatus status : serviceStatusContainer.getItemIds()) {	// 要保证，至少选择一个客服记录结果
				serviceResult_og.select(status);
				break;
			}
		}
	}

	@Override
	public String getMinimizedValueAsHTML() {
		return "高级查询";
	}

	@Override
	public Component getPopupComponent() {
		return this;
	}
	
}
