package com.jiangyifen.ec2.ui.mgr.tabsheet.mgrhistoryservicerecord;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.bean.RoleType;
import com.jiangyifen.ec2.entity.CustomerServiceRecord;
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
import com.jiangyifen.ec2.ui.FlipOverTableComponent;
import com.jiangyifen.ec2.utils.ParseDateSearchScope;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.validator.RegexpValidator;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.PopupDateField;
import com.vaadin.ui.PopupView;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.Notification;

/**
 * 历史客服记录简单查询组件
 * @author jrh
 *  2013-7-17
 */
@SuppressWarnings("serial")
public class MgrHistoryServiceRecordSimpleFilter extends VerticalLayout implements ClickListener {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private GridLayout gridLayout;					// 面板中的布局管理器
	
	private ComboBox createTimeScope;				// “联系时间”选择框
	private PopupDateField startCreateTime;			// “起始时间”选择框
	private PopupDateField finishCreateTime;		// “截止时间”选择框

	private ComboBox projectSelector;				// 项目选择框
	private ComboBox taskFinishedStatusSelector;	// 联系结果选择框
	private ComboBox empNoComboBox;					// 工号选择框
	
	private ComboBox deptComboBox;					// 部门选择框
	private ComboBox mgrQcComboBox;					// 管理员质检选择框
	private TextField customerPhone_tf;				// 客户电话输入文本框
	
	private Button searchButton;					// 刷新结果按钮
	private Button clearButton;						// 清空输入内容
	private PopupView complexSearchView;			// 复杂搜索界面
	
	private ValueChangeListener createTimeScopeListener;
	private ValueChangeListener startCreateTimeListener;
	private ValueChangeListener finishCreateTimeListener;
	
	private BeanItemContainer<MarketingProject> projectContainer;	// 存放项目选择的数据源
	private BeanItemContainer<CustomerServiceRecordStatus> serviceStatusContainer;	// 存放任务完成状态的数据源

	private User loginUser; 						// 当前的登陆用户
	private Domain domain;							// 当前用户所属域
	private List<Long> allGovernedDeptIds;			// 当前用户所有管辖部门的id号
	private List<Department> allGovernedDept;		//当前用户所有管辖部门

	private MarketingProjectService marketingProjectService;
	private CustomerServiceRecordStatusService serviceRecordStatusService;
	private DepartmentService departmentService;
	private UserService userService;
	
	private Table myServiceRecordTable;		// 存放我的客服记录查询结果的表格
	private MgrHistoryServiceRecordComplexFilter recordComplexFilter;			// 存放高级收索条件的组件
	private FlipOverTableComponent<CustomerServiceRecord> tableFlipOver;	// 我的客服记录显示表格的翻页组件
	
	public MgrHistoryServiceRecordSimpleFilter() {
		this.setSpacing(true);
		
		loginUser = SpringContextHolder.getLoginUser();
		domain = SpringContextHolder.getDomain();
		
		marketingProjectService = SpringContextHolder.getBean("marketingProjectService");
		serviceRecordStatusService = SpringContextHolder.getBean("customerServiceRecordStatusService");
		departmentService = SpringContextHolder.getBean("departmentService");
		userService = SpringContextHolder.getBean("userService");
		
		// jrh 获取当前用户所属部门及其所有角色的管辖部门的Id号
		allGovernedDeptIds = new ArrayList<Long>();
		allGovernedDept = new ArrayList<Department>();
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
							allGovernedDept.add(dept);
						}
					}
				}
			}
		}
		
		gridLayout = new GridLayout(7, 3);
		gridLayout.setSpacing(true);
		gridLayout.setMargin(true, true, false, true);
		this.addComponent(gridLayout);
		
		this.createFilterHLayout1();
		this.createFilterHLayout2();
		this.createFilterHLayout3();
	}

	private void createFilterHLayout1() {
		// 联系时间选中框
		Label timeScopeLabel = new Label("联系时间：");
		timeScopeLabel.setWidth("-1px");
		gridLayout.addComponent(timeScopeLabel, 0, 0);
		
		createTimeScope = new ComboBox();
		createTimeScope.addItem("今天");
		createTimeScope.addItem("昨天");
		createTimeScope.addItem("本周");
		createTimeScope.addItem("上周");
		createTimeScope.addItem("本月");
		createTimeScope.addItem("上月");
		createTimeScope.addItem("精确时间");
		createTimeScope.setValue("今天");
		createTimeScope.setWidth("140px");
		createTimeScope.setImmediate(true);
		createTimeScope.setFilteringMode(ComboBox.FILTERINGMODE_CONTAINS);
		createTimeScope.setNullSelectionAllowed(false);
		gridLayout.addComponent(createTimeScope, 1, 0);
		
		createTimeScopeListener = new ValueChangeListener() {
			public void valueChange(ValueChangeEvent event) {
				String scopeValue=(String)createTimeScope.getValue();
				if("精确时间".equals(scopeValue)) {
					return;
				}
				startCreateTime.removeListener(startCreateTimeListener);
				finishCreateTime.removeListener(finishCreateTimeListener);
				Date[] dates = ParseDateSearchScope.parseToDate(scopeValue);
				startCreateTime.setValue(dates[0]);
				finishCreateTime.setValue(dates[1]);
				startCreateTime.addListener(startCreateTimeListener);
				finishCreateTime.addListener(finishCreateTimeListener);
			}
		};
		createTimeScope.addListener(createTimeScopeListener);
		
		Date[] dates = ParseDateSearchScope.parseToDate("今天");
		
		// 起始联系选中框
		Label startTimeLabel = new Label("起始时间：");
		startTimeLabel.setWidth("-1px");
		gridLayout.addComponent(startTimeLabel, 2, 0);
		
		startCreateTimeListener = new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				createTimeScope.removeListener(createTimeScopeListener);
				createTimeScope.setValue("精确时间");
				createTimeScope.addListener(createTimeScopeListener);
			}
		};
		
		startCreateTime = new PopupDateField();
		startCreateTime.setImmediate(true);
		startCreateTime.setWidth("156px");
		startCreateTime.setValue(dates[0]);
		startCreateTime.setDateFormat("yyyy-MM-dd HH:mm:ss");
		startCreateTime.setValidationVisible(false);
		startCreateTime.setResolution(PopupDateField.RESOLUTION_SEC);
		startCreateTime.addListener(startCreateTimeListener);
		gridLayout.addComponent(startCreateTime, 3, 0);

		// 截止联系选中框
		Label finishTimeLabel = new Label("截止时间：");
		finishTimeLabel.setWidth("-1px");
		gridLayout.addComponent(finishTimeLabel, 4, 0);

		finishCreateTimeListener = new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				createTimeScope.removeListener(finishCreateTimeListener);
				createTimeScope.setValue("精确时间");
				createTimeScope.addListener(createTimeScopeListener);
			}
		};
		
		finishCreateTime = new PopupDateField();
		finishCreateTime.setImmediate(true);
		finishCreateTime.setWidth("156px");
		finishCreateTime.setValue(dates[1]);
		finishCreateTime.setDateFormat("yyyy-MM-dd HH:mm:ss");
		finishCreateTime.setValidationVisible(false);
		finishCreateTime.setResolution(PopupDateField.RESOLUTION_SEC);
		finishCreateTime.addListener(finishCreateTimeListener);
		gridLayout.addComponent(finishCreateTime, 5, 0);
		
		searchButton = new Button("查 询", (ClickListener) this);
		searchButton.setStyleName("default");
		gridLayout.addComponent(searchButton, 6, 0);
	}
	
	private void createFilterHLayout2() {
		// 任务项目选择框
		Label projectLabel = new Label("项目选择：");
		projectLabel.setWidth("-1px");
		gridLayout.addComponent(projectLabel, 0, 1);
		
		// 获取当前管理员所管辖部门下创建的项目
		List<MarketingProject> projects = marketingProjectService.getAllByDepartments(allGovernedDeptIds, domain.getId());
		MarketingProject project = new MarketingProject();
		project.setProjectName("全部");
		// 从数据库中获取分配给该登陆用户的“项目”信息，并绑定到Container中
		projectContainer = new BeanItemContainer<MarketingProject>(MarketingProject.class);
		projectContainer.addBean(project);
		projectContainer.addAll(projects);
		
		projectSelector = new ComboBox();
		projectSelector.setWidth("140px");
		projectSelector.setFilteringMode(ComboBox.FILTERINGMODE_CONTAINS);
		projectSelector.setNullSelectionAllowed(false);
		projectSelector.setContainerDataSource(projectContainer);
		projectSelector.setValue(project);
		gridLayout.addComponent(projectSelector, 1, 1);
		
		// 项目类别选择框
		Label taskFinishedStatusLabel = new Label("联系结果：");
		taskFinishedStatusLabel.setWidth("-1px");
		gridLayout.addComponent(taskFinishedStatusLabel, 2, 1);
		
		taskFinishedStatusSelector = new ComboBox();
		taskFinishedStatusSelector.setWidth("156px");
		taskFinishedStatusSelector.setFilteringMode(ComboBox.FILTERINGMODE_CONTAINS);
		taskFinishedStatusSelector.setNullSelectionAllowed(false);
		gridLayout.addComponent(taskFinishedStatusSelector, 3, 1);
		
		// 从数据库中获取任务的“处理结果”信息，并绑定到Container中
		serviceStatusContainer = new BeanItemContainer<CustomerServiceRecordStatus>(CustomerServiceRecordStatus.class);
		CustomerServiceRecordStatus finishedStatus = new CustomerServiceRecordStatus();
		finishedStatus.setStatusName("全部");
		serviceStatusContainer.addBean(finishedStatus);
		
		List<CustomerServiceRecordStatus> allList = serviceRecordStatusService.getAllByDeptIdsAndDirection(allGovernedDeptIds, "inAndOut", true, domain.getId());
		serviceStatusContainer.addAll(allList);
		
		taskFinishedStatusSelector.setContainerDataSource(serviceStatusContainer);
		for(CustomerServiceRecordStatus status : serviceStatusContainer.getItemIds()) {
			if(status.getStatusName().equals("全部")) {
				continue;
			}
			String direction = "incoming".equals(status.getDirection()) ? "呼入" : "呼出";
			taskFinishedStatusSelector.setItemCaption(status, status.getStatusName()+" - "+direction);
		}
		taskFinishedStatusSelector.setValue(finishedStatus);

		// 创建坐席
		Label empNoLabel = new Label("创建坐席：");
		empNoLabel.setWidth("-1px");
		gridLayout.addComponent(empNoLabel, 4, 1);
		
		// 从数据库中获取“客服”信息，并绑定到Container中
		BeanItemContainer<User> userContainer = new BeanItemContainer<User>(User.class);
		User allCsr = new User();
		allCsr .setEmpNo("全部");
		userContainer.addBean(allCsr);
		List<User> csrs = userService.getCsrsByDepartment(allGovernedDeptIds, domain.getId());
		userContainer.addAll(csrs);
		
		empNoComboBox = new ComboBox();
		empNoComboBox.setContainerDataSource(userContainer);
		empNoComboBox.setValue(allCsr );
		empNoComboBox.setWidth("158px");
		empNoComboBox.setNullSelectionAllowed(false);
		empNoComboBox.setFilteringMode(ComboBox.FILTERINGMODE_CONTAINS);
		gridLayout.addComponent(empNoComboBox, 5, 1);
		for(User csr : userContainer.getItemIds()) {
			String caption = csr.getEmpNo();
			if(csr.getRealName() != null) {
				caption = caption +" - "+ csr.getRealName();
			}
			empNoComboBox.setItemCaption(csr, caption);
		}
		
		clearButton = new Button("清 空", (ClickListener) this);
		gridLayout.addComponent(clearButton, 6, 1);
		gridLayout.setComponentAlignment(clearButton, Alignment.MIDDLE_RIGHT);
	}
	
	/**
	 * 创建第三行的搜索组件
	 */
	private void createFilterHLayout3() {
		// 部门选择
		Label deptLabel = new Label("部门选择：");
		deptLabel.setWidth("-1px");
		gridLayout.addComponent(deptLabel, 0, 2);
		
		// 从数据库中获取“客服”信息，并绑定到Container中
		BeanItemContainer<Department> departmentContainer = new BeanItemContainer<Department>(Department.class);
		Department allDept = new Department();
		allDept.setName("全部");
		departmentContainer.addBean(allDept);
		//在comboBox中添加用户管辖的部门
		departmentContainer.addAll(allGovernedDept);
		
		deptComboBox = new ComboBox();
		deptComboBox.setContainerDataSource(departmentContainer);
		deptComboBox.setItemCaptionPropertyId("name");
		deptComboBox.setValue(allDept);
		deptComboBox.setWidth("140px");
		deptComboBox.setValue(allDept);
		deptComboBox.setFilteringMode(ComboBox.FILTERINGMODE_CONTAINS);
		deptComboBox.setNullSelectionAllowed(false);
		gridLayout.addComponent(deptComboBox, 1, 2);

		// 部门选择
		Label mgrQcLabel = new Label("Mgr质检：");
		mgrQcLabel.setWidth("-1px");
		gridLayout.addComponent(mgrQcLabel, 2, 2);
		
		// 创建ComboBox
		mgrQcComboBox = new ComboBox();
		mgrQcComboBox.addItem("全部");
		for (QcMgr qcMgr : QcMgr.values()) {
			mgrQcComboBox.addItem(qcMgr);
		}
		mgrQcComboBox.setWidth("158px");
		mgrQcComboBox.setFilteringMode(ComboBox.FILTERINGMODE_CONTAINS);
		mgrQcComboBox.setNullSelectionAllowed(false);
		mgrQcComboBox.setValue("全部");
		gridLayout.addComponent(mgrQcComboBox, 3, 2);

		// 客户电话号输入区
		Label customerPhoneLabel = new Label("客户电话：");
		customerPhoneLabel.setWidth("-1px");
		gridLayout.addComponent(customerPhoneLabel, 4, 2);
		
		customerPhone_tf = new TextField();
		customerPhone_tf.setWidth("158px");
		customerPhone_tf.addValidator(new RegexpValidator("\\d+", "电话号码只能由数字组成"));
		customerPhone_tf.setValidationVisible(false);
		gridLayout.addComponent(customerPhone_tf, 5, 2);
		
		// 高级搜索
		recordComplexFilter = new MgrHistoryServiceRecordComplexFilter();
		recordComplexFilter.setHeight("-1px");
		complexSearchView = new PopupView(recordComplexFilter);
		complexSearchView.setHideOnMouseOut(false);
		gridLayout.addComponent(complexSearchView, 6, 2);
	}
	
	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if(source == searchButton) {
			try {
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
				
				Date startTime = (Date) startCreateTime.getValue();
				Date finishTime = (Date) finishCreateTime.getValue();
				Calendar cal = Calendar.getInstance();
				cal.setTime(finishTime);
				cal.add(Calendar.MONTH, -1);
				Date lastMonth = cal.getTime();
				if(startTime.before(lastMonth)) {
					finishCreateTime.getApplication().getMainWindow().showNotification("对不起，单次只能查询一个月范围内的数据，请修改后重试！", Notification.TYPE_WARNING_MESSAGE);
					return;
				}
	
				String countSql = createCountSql();
				String searchSql = countSql.replaceFirst("count\\(c\\)", "c") + " order by c.createDate desc";
				
				tableFlipOver.setSearchSql(searchSql);
				tableFlipOver.setCountSql(countSql);
				tableFlipOver.refreshToFirstPage();
				
				// 在查询到结果后，默认选择 Table 的第一条记录
				BeanItemContainer<CustomerServiceRecord> taskBeanItemContainer = tableFlipOver.getEntityContainer();
				if(taskBeanItemContainer.size() > 0) {
					Object firstItemId = taskBeanItemContainer.getIdByIndex(0);
					myServiceRecordTable.setValue(taskBeanItemContainer.getItem(firstItemId).getBean());
				} else {
					myServiceRecordTable.setValue(null);
				}
			} catch (Exception e) {
				e.printStackTrace();
				logger.error("jrh 管理员历史客服记录简单查询 出现异常"+e.getMessage(), e);
				myServiceRecordTable.getApplication().getMainWindow().showNotification("对不起，查询失败，请重试！", Notification.TYPE_WARNING_MESSAGE);
			}
		} else if(source == clearButton) {
			createTimeScope.setValue("今天");
			
			projectSelector.select(projectSelector.getItemIds().toArray()[0]);
			taskFinishedStatusSelector.select(taskFinishedStatusSelector.getItemIds().toArray()[0]);
			empNoComboBox.select(empNoComboBox.getItemIds().toArray()[0]);
			
			deptComboBox.select(deptComboBox.getItemIds().toArray()[0]);
			mgrQcComboBox.select(mgrQcComboBox.getItemIds().toArray()[0]);
			customerPhone_tf.setValue("");

		}
	}

	private String  createCountSql() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String specificStartTimeSql = "";
		if(startCreateTime.getValue() != null) {
			specificStartTimeSql = " and c.createDate >= '" + dateFormat.format(startCreateTime.getValue()) +"'";
		}
	
		String specificFinishTimeSql = "";
		if(finishCreateTime.getValue() != null) {
			specificFinishTimeSql = " and c.createDate <= '" + dateFormat.format(finishCreateTime.getValue()) +"'";
		}
		
		String projectSql = "";
		MarketingProject project = (MarketingProject) projectSelector.getValue();
		if(!"全部".equals(project.getProjectName())) {
			projectSql = " and c.marketingProject.id = " + project.getId();
		}
		
		String taskFinishedStatuSql = "";
		CustomerServiceRecordStatus recordStatus = (CustomerServiceRecordStatus) taskFinishedStatusSelector.getValue();
		if(!"全部".equals(taskFinishedStatusSelector.getValue().toString())) {
			taskFinishedStatuSql = " and c.serviceRecordStatus.id = " + recordStatus.getId();
		}
	
		// 用户工号选择信息,按坐席id 查询更快
		User user= (User) empNoComboBox.getValue();
		String empNoStr = "";
		if (!"全部".equals(user.getEmpNo())) {
			empNoStr = " and c.creator.id = " + user.getId();
		}
		
		String deptSql = "";
		Department dept = (Department) deptComboBox.getValue();
		if(dept.getId() != null) {
			deptSql = " and c.creator.department.id = " + dept.getId();
		}
		
		// 按质检结果查询
		String mgrQcSql = "";
		if(!mgrQcComboBox.getValue().equals("全部")){
			QcMgr qcMgr = (QcMgr)mgrQcComboBox.getValue();
			if (qcMgr != null) {
				mgrQcSql = " and c.qcMgr = " + qcMgr.getClass().getName() + ".";
				if (qcMgr.getIndex() == 0) {
					mgrQcSql += "NOTQUALIFIED";
				} else if (qcMgr.getIndex() == 1) {
					mgrQcSql += "QUALIFIED";
				}
			}
		}

		String customerPhoneSql = "";
		String phoneNumber = customerPhone_tf.getValue().toString().trim();
		if(!"".equals(phoneNumber) && phoneNumber != null) {
			customerPhoneSql = " and c.customerResource.id in (select p.customerResource.id from Telephone as p where p.number like '%" + phoneNumber + "%') ";
		}
		
		// 根据当前用户所属部门，及其下属部门，创建动态查询语句, allGovernedDeptIds 至少会含有一个 id 为 0 的元素
		String governedDeptIdSql = StringUtils.join(allGovernedDeptIds, ",");
		String governedDeptSql1 = " and c.creator.department.id in (" + governedDeptIdSql+ ")";

		// 拼接查询语句
		String dynamicSql = customerPhoneSql + specificStartTimeSql + specificFinishTimeSql + projectSql + empNoStr + deptSql + governedDeptSql1 + mgrQcSql + taskFinishedStatuSql;

		/************************************************************************************
		 * lastItemSql： 新增功能，用于查询某个客户 最终客服记录
		 ***********************************************************************************/
		String lastItemSql = " and NOT EXISTS (select c2 from CustomerServiceRecord as c2 "
					+ " where c2.customerResource.id = c.customerResource.id and c2.creator.department.id in ("+governedDeptIdSql+") and c2.createDate > c.createDate ) ";
		
		return "select count(c) from CustomerServiceRecord as c where 1=1 " + dynamicSql + lastItemSql;
		
	}
	
	public void setTableFlipOver(FlipOverTableComponent<CustomerServiceRecord> tableFlipOver) {
		this.tableFlipOver = tableFlipOver;
		this.myServiceRecordTable = tableFlipOver.getTable();
		recordComplexFilter.setTableFlipOver(tableFlipOver);
	}

	public Button getSearchButton() {
		return searchButton;
	}
	
}
