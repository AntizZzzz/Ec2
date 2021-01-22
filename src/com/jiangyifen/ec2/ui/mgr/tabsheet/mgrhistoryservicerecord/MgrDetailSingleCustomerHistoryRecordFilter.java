package com.jiangyifen.ec2.ui.mgr.tabsheet.mgrhistoryservicerecord;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.jiangyifen.ec2.bean.RoleType;
import com.jiangyifen.ec2.entity.CustomerResource;
import com.jiangyifen.ec2.entity.CustomerServiceRecord;
import com.jiangyifen.ec2.entity.CustomerServiceRecordStatus;
import com.jiangyifen.ec2.entity.Department;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.MarketingProject;
import com.jiangyifen.ec2.entity.Role;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.service.eaoservice.CustomerServiceRecordStatusService;
import com.jiangyifen.ec2.service.eaoservice.DepartmentService;
import com.jiangyifen.ec2.service.eaoservice.MarketingProjectService;
import com.jiangyifen.ec2.ui.FlipOverTableComponent;
import com.jiangyifen.ec2.utils.ParseDateSearchScope;
import com.jiangyifen.ec2.utils.SpringContextHolder;
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
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.Notification;

/**
 * 单个客户的历史服务记录详细信息搜索组件
 * @author jrh
 *  2013-7-17
 */
@SuppressWarnings("serial")
public class MgrDetailSingleCustomerHistoryRecordFilter extends VerticalLayout implements ClickListener, Content {
	
	private GridLayout gridLayout;				// 面板中的布局管理器
	
	private ComboBox projectSelector;			// 项目选择框
	private ComboBox directionSelector;			// 呼叫方向选择框
	private ComboBox taskFinishedStatuSelector;	// 联系结果选择框
	
	private ComboBox createTimeScope;			// “联系时间”选择框
	private PopupDateField startCreateTime;		// “起始时间”选择框
	private PopupDateField finishCreateTime;	// “截止时间”选择框
	
	private Button searchButton;				// 刷新结果按钮
	private Button clearButton;					// 清空输入内容

	private ValueChangeListener timeScopeListener;
	private ValueChangeListener startTimeListener;
	private ValueChangeListener finishTimeListener;
	
	private Table singleCustomerServiceRecordTable;										// 存放我的客服记录查询结果的表格
	private FlipOverTableComponent<CustomerServiceRecord> tableFlipOver;	// 我的客服记录显示表格的翻页组件
	
	private CustomerServiceRecordStatus finishedStatus; 					// 手动建立的状态名为 "全部 " 的状态对象
	private List<CustomerServiceRecordStatus> allServiceRecordStatus; 		// 所有的客服记录状态对象
	private List<CustomerServiceRecordStatus> allOutgoingRecordStatus; 		// 所有的客服记录中 呼出 记录的状态对象
	private List<CustomerServiceRecordStatus> allIncomingRecordStatus; 		// 所有的客服记录中 呼入 记录的状态对象
	private BeanItemContainer<MarketingProject> projectContainer;			// 存放项目选择的数据源
	private BeanItemContainer<CustomerServiceRecordStatus> serviceStatusContainer;	// 存放任务完成状态的数据源
	
	private User loginUser; 												// 当前的登陆用户
	private Domain domain;													// 当前用户所属域
	private List<Long> allGovernedDeptIds;									// 当前用户所有管辖部门的id号
	private String governedDeptIdSql = "";									// 按记录创建人所属部门的id查询的语句
	private CustomerResource customerResource;								// 当前选中任务对应的客户资源
	
	private MarketingProjectService marketingProjectService;				// 项目服务类
	private CustomerServiceRecordStatusService serviceRecordStatusService;	// 客服记录服务类
	private DepartmentService departmentService;							// 部门服务类

	public MgrDetailSingleCustomerHistoryRecordFilter() {
		// 初始化各种参数
		this.setSpacing(true);

		loginUser = SpringContextHolder.getLoginUser();
		domain = SpringContextHolder.getDomain();
		
		marketingProjectService = SpringContextHolder.getBean("marketingProjectService");
		serviceRecordStatusService = SpringContextHolder.getBean("customerServiceRecordStatusService");
		departmentService = SpringContextHolder.getBean("departmentService");
		
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
		
		// 根据当前用户所属部门，及其下属部门，创建动态查询语句, allGovernedDeptIds 至少会含有一个 id 为 0 的元素
		String deptIdsStr = "";
		for (int i = 0; i < allGovernedDeptIds.size(); i++) {
			if (i == (allGovernedDeptIds.size() - 1)) {
				deptIdsStr += allGovernedDeptIds.get(i);
			} else {
				deptIdsStr += allGovernedDeptIds.get(i) + ",";
			}
		}
		governedDeptIdSql = " c.creator.department.id in (" + deptIdsStr+ ")";
		
		finishedStatus = new CustomerServiceRecordStatus();
		finishedStatus.setStatusName("全部");
		allServiceRecordStatus = serviceRecordStatusService.getAllByDeptIdsAndDirection(allGovernedDeptIds, "inAndOut", true, domain.getId());
		allOutgoingRecordStatus = serviceRecordStatusService.getAllByDeptIdsAndDirection(allGovernedDeptIds, "outgoing", true, domain.getId());
		allIncomingRecordStatus = serviceRecordStatusService.getAllByDeptIdsAndDirection(allGovernedDeptIds, "incoming", true, domain.getId());
		allServiceRecordStatus.add(0, finishedStatus);
		allOutgoingRecordStatus.add(0, finishedStatus);
		allIncomingRecordStatus.add(0, finishedStatus);
		
		serviceStatusContainer = new BeanItemContainer<CustomerServiceRecordStatus>(CustomerServiceRecordStatus.class);
		serviceStatusContainer.addAll(allServiceRecordStatus);

		// 创建主要组件
		gridLayout = new GridLayout(7, 2);
		gridLayout.setCaption("高级搜索");
		gridLayout.setSpacing(true);
		gridLayout.setMargin(true, true, false, true);
		this.addComponent(gridLayout);
		
		this.createFilterHLayout1();
		this.createFilterHLayout2();
		this.createButtons();
	}
	
	/**
	 * 创建第一行搜索组件
	 */
	private void createFilterHLayout1() {
		// 时间范围选中框
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
		createTimeScope.setWidth("150px");
		createTimeScope.setImmediate(true);
		createTimeScope.setNullSelectionAllowed(false);
		gridLayout.addComponent(createTimeScope, 1, 0);

		timeScopeListener = new ValueChangeListener() {
			public void valueChange(ValueChangeEvent event) {
				String scopeValue=(String)createTimeScope.getValue();
				if("精确时间".equals(scopeValue)) {
					return;
				}
				startCreateTime.removeListener(startTimeListener);
				finishCreateTime.removeListener(finishTimeListener);
				Date[] dates = ParseDateSearchScope.parseToDate(scopeValue);
				startCreateTime.setValue(dates[0]);
				finishCreateTime.setValue(dates[1]);
				startCreateTime.addListener(startTimeListener);
				finishCreateTime.addListener(finishTimeListener);
			}
		};
		createTimeScope.addListener(timeScopeListener);

		Date[] dates = ParseDateSearchScope.parseToDate("今天");
		
		// 开始时间选中框
		Label startTimeLabel = new Label("起始时间：");
		startTimeLabel.setWidth("-1px");
		gridLayout.addComponent(startTimeLabel, 2, 0);
		
		startTimeListener = new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				createTimeScope.removeListener(timeScopeListener);
				createTimeScope.setValue("精确时间");
				createTimeScope.addListener(timeScopeListener);
			}
		};

		startCreateTime = new PopupDateField();
		startCreateTime.setImmediate(true);
		startCreateTime.setWidth("160px");
		startCreateTime.setValue(dates[0]);
		startCreateTime.setDateFormat("yyyy-MM-dd HH:mm:ss");
		startCreateTime.setValidationVisible(false);
		startCreateTime.setResolution(PopupDateField.RESOLUTION_SEC);
		startCreateTime.addListener(startTimeListener);
		gridLayout.addComponent(startCreateTime, 3, 0);
		
		// 截止时间选中框
		Label finishTimeLabel = new Label("截止时间：");
		finishTimeLabel.setWidth("-1px");
		gridLayout.addComponent(finishTimeLabel, 4, 0);

		finishTimeListener = new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				createTimeScope.removeListener(finishTimeListener);
				createTimeScope.setValue("精确时间");
				createTimeScope.addListener(timeScopeListener);
			}
		};
		
		finishCreateTime = new PopupDateField();
		finishCreateTime.setImmediate(true);
		finishCreateTime.setWidth("160px");
		finishCreateTime.setValue(dates[1]);
		finishCreateTime.setDateFormat("yyyy-MM-dd HH:mm:ss");
		finishCreateTime.setValidationVisible(false);
		finishCreateTime.setResolution(PopupDateField.RESOLUTION_SEC);
		finishCreateTime.addListener(finishTimeListener);
		gridLayout.addComponent(finishCreateTime, 5, 0);
	}

	/**
	 * 创建第二行搜索组件
	 */
	private void createFilterHLayout2() {
		// 任务项目选择框
		Label projectLabel = new Label("项目选择：");
		projectLabel.setWidth("-1px");
		gridLayout.addComponent(projectLabel, 0, 1);
		
		// 从数据库中获取分配给该登陆用户的“项目”信息，并绑定到Container中
		List<MarketingProject> projects = marketingProjectService.getAllByDepartments(allGovernedDeptIds, domain.getId());
		MarketingProject project = new MarketingProject();
		project.setProjectName("全部");
		projectContainer = new BeanItemContainer<MarketingProject>(MarketingProject.class);
		projectContainer.addBean(project);
		projectContainer.addAll(projects);
		
		projectSelector = new ComboBox();
		projectSelector.setWidth("150px");
		projectSelector.setNullSelectionAllowed(false);
		projectSelector.setFilteringMode(ComboBox.FILTERINGMODE_CONTAINS);
		projectSelector.setContainerDataSource(projectContainer);
		projectSelector.setValue(project);
		gridLayout.addComponent(projectSelector, 1, 1);
		
		// 呼叫方向选择框
		Label directionLabel = new Label("呼叫方向：");
		directionLabel.setWidth("-1px");
		gridLayout.addComponent(directionLabel, 2, 1);
		
		directionSelector = new ComboBox();
		directionSelector.setWidth("158px");
		directionSelector.addItem("全部");
		directionSelector.addItem("呼入");
		directionSelector.addItem("呼出");
		directionSelector.setValue("全部");
		directionSelector.setImmediate(true);
		directionSelector.setFilteringMode(ComboBox.FILTERINGMODE_CONTAINS);
		directionSelector.setNullSelectionAllowed(false);
		gridLayout.addComponent(directionSelector, 3, 1);
		addListenerToDirectionSelector();
		
		// 项目类别选择框
		Label taskFinishedStatuLabel = new Label("联系结果：");
		taskFinishedStatuLabel.setWidth("-1px");
		gridLayout.addComponent(taskFinishedStatuLabel, 4, 1);
		
		taskFinishedStatuSelector = new ComboBox();
		taskFinishedStatuSelector.setWidth("158px");
		taskFinishedStatuSelector.setNullSelectionAllowed(false);
		taskFinishedStatuSelector.setFilteringMode(ComboBox.FILTERINGMODE_CONTAINS);
		taskFinishedStatuSelector.setContainerDataSource(serviceStatusContainer);
		taskFinishedStatuSelector.setValue(finishedStatus);
		gridLayout.addComponent(taskFinishedStatuSelector, 5, 1);

		// 修改客服记录状态显示名称
		editRecordStatusShowName();
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
				
				boolean statusExist = false;	// 默认是任务修改后的container 中不包含客服结果中原来选中的值
				for(CustomerServiceRecordStatus status : serviceStatusContainer.getItemIds()) {
					if(status.getStatusName().equals(taskFinishedStatuSelector.getValue().toString())) {
						taskFinishedStatuSelector.select(status);
						statusExist = true;
						break;
					}
				}
				if(!statusExist) {	// 如果不存在则选择 "全部"
					taskFinishedStatuSelector.select(finishedStatus);
				}
			}
		});
	}

	/**
	 * 修改客服记录状态显示名称
	 */
	private void editRecordStatusShowName() {
		for(CustomerServiceRecordStatus status : serviceStatusContainer.getItemIds()) {
			if(status.getStatusName().equals("全部")) {
				continue;
			}
			String direction = "incoming".equals(status.getDirection()) ? "呼入" : "呼出";
			taskFinishedStatuSelector.setItemCaption(status, status.getStatusName()+" - "+direction);
		}
	}
	
	/**
	 * 创建操作按钮
	 */
	private void createButtons() {
		// 查询按钮
		searchButton = new Button("查 询", (ClickListener)this);
		searchButton.setStyleName("default");
		gridLayout.addComponent(searchButton, 6, 0);
		gridLayout.setComponentAlignment(searchButton, Alignment.MIDDLE_RIGHT);
		
		// 清空按钮
		clearButton = new Button("清 空");
		clearButton.addListener(this);
		gridLayout.addComponent(clearButton, 6, 1);
		gridLayout.setComponentAlignment(clearButton, Alignment.MIDDLE_RIGHT);
	}

	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if(source == searchButton) {
			if(!startCreateTime.isValid()) {
				startCreateTime.getApplication().getMainWindow().showNotification("起始联系时间格式不正确，请修改后重试！", Notification.TYPE_WARNING_MESSAGE);
				return;
			}
			
			if(!finishCreateTime.isValid()) {
				finishCreateTime.getApplication().getMainWindow().showNotification("截止联系时间格式不正确，请修改后重试！", Notification.TYPE_WARNING_MESSAGE);
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
				singleCustomerServiceRecordTable.setValue(taskBeanItemContainer.getItem(firstItemId).getBean());
			} else {
				singleCustomerServiceRecordTable.setValue(null);
			}
		} else if(source == clearButton) {
			projectSelector.select(projectSelector.getItemIds().toArray()[0]);
			taskFinishedStatuSelector.select(taskFinishedStatuSelector.getItemIds().toArray()[0]);
			createTimeScope.setValue("全部");
			directionSelector.setValue("全部");
			startCreateTime.setValue(null);
			finishCreateTime.setValue(null);
		}
	}

	private String  createCountSql() {
		String projectSql = "";
		MarketingProject project = (MarketingProject) projectSelector.getValue();
		if(!"全部".equals(project.getProjectName())) {
			projectSql = " and c.marketingProject.id = " + project.getId();
		}
		
		String directionSql = "";
		if("呼入".equals(directionSelector.getValue())) {
			directionSql = " and c.direction = 'incoming' ";
		} else if("呼出".equals(directionSelector.getValue())) {
			directionSql = " and c.direction = 'outgoing' ";
		}
		
		String taskFinishedStatuSql = "";
		CustomerServiceRecordStatus recordStatu = (CustomerServiceRecordStatus) taskFinishedStatuSelector.getValue();
		if(!"全部".equals(taskFinishedStatuSelector.getValue().toString())) {
			taskFinishedStatuSql = " and c.serviceRecordStatus.id = " + recordStatu.getId();
		}
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String specificStartTimeSql = "";
		if(startCreateTime.getValue() != null) {
			specificStartTimeSql = " and c.createDate >= '" + dateFormat.format(startCreateTime.getValue()) +"' ";
		}

		String specificFinishTimeSql = "";
		if(finishCreateTime.getValue() != null) {
			specificFinishTimeSql = " and c.createDate <= '" + dateFormat.format(finishCreateTime.getValue()) +"' ";
		}
		
		String sql = projectSql + directionSql + taskFinishedStatuSql + specificStartTimeSql + specificFinishTimeSql;
		return "select count(c) from CustomerServiceRecord as c where 1=1 and c.customerResource.id = " + customerResource.getId() +" and "+ governedDeptIdSql + sql;
	}

	@Override
	public String getMinimizedValueAsHTML() {
		return "高级搜索";
	}

	@Override
	public Component getPopupComponent() {
		return gridLayout;
	}
	
	public void setTableFlipOver(FlipOverTableComponent<CustomerServiceRecord> tableFlipOver) {
		this.tableFlipOver = tableFlipOver;
		this.singleCustomerServiceRecordTable = tableFlipOver.getTable();
	}
	
	// 我的任务左侧任务显示表格中选中项，对应的资源对象
	public void setCustomerResource(CustomerResource customerResource) {
		this.customerResource = customerResource;
	}

}
