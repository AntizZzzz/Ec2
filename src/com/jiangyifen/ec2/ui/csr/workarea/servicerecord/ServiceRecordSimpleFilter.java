package com.jiangyifen.ec2.ui.csr.workarea.servicerecord;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.jiangyifen.ec2.entity.CustomerServiceRecord;
import com.jiangyifen.ec2.entity.CustomerServiceRecordStatus;
import com.jiangyifen.ec2.entity.MarketingProject;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.service.eaoservice.CustomerServiceRecordStatusService;
import com.jiangyifen.ec2.service.eaoservice.MarketingProjectService;
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
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.PopupDateField;
import com.vaadin.ui.PopupView;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.Notification;

@SuppressWarnings("serial")
public class ServiceRecordSimpleFilter extends VerticalLayout implements ClickListener {
	
	private GridLayout gridLayout;					// 面板中的布局管理器
	
	private ComboBox createTimeScope;				// “联系时间”选择框
	private PopupDateField startCreateTime;			// “起始联系”选择框
	private PopupDateField finishCreateTime;		// “截止联系”选择框

	private ComboBox projectSelector;				// 项目选择框
	private ComboBox taskFinishedStatusSelector;	// 联系结果选择框
	private TextField customerPhone_tf;				// 电话号码输入文本框
	
	private Button searchButton;					// 刷新结果按钮
	private Button clearButton;						// 清空输入内容
	private PopupView complexSearchView;			// 复杂搜索界面
	
	private ValueChangeListener createTimeScopeListener;
	private ValueChangeListener startCreateTimeListener;
	private ValueChangeListener finishCreateTimeListener;
	
	private MarketingProjectService marketingProjectService;
	private BeanItemContainer<MarketingProject> projectContainer;	// 存放项目选择的数据源
	private CustomerServiceRecordStatusService serviceRecordStatusService;
	private BeanItemContainer<CustomerServiceRecordStatus> serviceStatusContainer;	// 存放任务完成状态的数据源
	
	private User loginUser; 				// 当前的登陆用户
	private Table myServiceRecordTable;		// 存放我的客服记录查询结果的表格
	private ServiceRecordComplexFilter recordComplexFilter;					// 存放高级收索条件的组件
	private FlipOverTableComponent<CustomerServiceRecord> tableFlipOver;	// 我的客服记录显示表格的翻页组件
	
	public ServiceRecordSimpleFilter() {
		this.setSpacing(true);
		loginUser = SpringContextHolder.getLoginUser();
		marketingProjectService = SpringContextHolder.getBean("marketingProjectService");
		serviceRecordStatusService = SpringContextHolder.getBean("customerServiceRecordStatusService");
		
		gridLayout = new GridLayout(7, 2);
		gridLayout.setSpacing(true);
		gridLayout.setMargin(true, true, false, true);
		this.addComponent(gridLayout);
		
		this.createFilterHLayout1();
		this.createFilterHLayout2();
		this.createOperateUI();
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
		createTimeScope.setWidth("115px");
		createTimeScope.setImmediate(true);
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
		Label startTimeLabel = new Label("起始联系：");
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
		startCreateTime.setParseErrorMessage("时间格式不合法");
		startCreateTime.setResolution(PopupDateField.RESOLUTION_SEC);
		startCreateTime.addListener(startCreateTimeListener);
		gridLayout.addComponent(startCreateTime, 3, 0);

		// 截止联系选中框
		Label finishTimeLabel = new Label("截止联系：");
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
		finishCreateTime.setParseErrorMessage("时间格式不合法");
		finishCreateTime.setResolution(PopupDateField.RESOLUTION_SEC);
		finishCreateTime.addListener(finishCreateTimeListener);
		gridLayout.addComponent(finishCreateTime, 5, 0);
	}
	
	private void createFilterHLayout2() {
		// 任务项目选择框
		Label projectLabel = new Label("项目选择：");
		projectLabel.setWidth("-1px");
		gridLayout.addComponent(projectLabel, 0, 1);
		
		projectSelector = new ComboBox();
		projectSelector.setWidth("115px");
		projectSelector.setNullSelectionAllowed(false);
		gridLayout.addComponent(projectSelector, 1, 1);
		
		// 从数据库中获取分配给该登陆用户的“项目”信息，并绑定到Container中
		projectContainer = new BeanItemContainer<MarketingProject>(MarketingProject.class);
		MarketingProject project = new MarketingProject();
		project.setProjectName("全部");
		projectContainer.addBean(project);
		projectContainer.addAll(marketingProjectService.getAllProjectsByUser(loginUser));
		projectSelector.setContainerDataSource(projectContainer);
		projectSelector.setValue(project);
		
		// 项目类别选择框
		Label taskFinishedStatusLabel = new Label("客服结果：");
		taskFinishedStatusLabel.setWidth("-1px");
		gridLayout.addComponent(taskFinishedStatusLabel, 2, 1);
		
		taskFinishedStatusSelector = new ComboBox();
		taskFinishedStatusSelector.setWidth("156px");
		taskFinishedStatusSelector.setNullSelectionAllowed(false);
		gridLayout.addComponent(taskFinishedStatusSelector, 3, 1);
		
		// 从数据库中获取任务的“处理结果”信息，并绑定到Container中
		serviceStatusContainer = new BeanItemContainer<CustomerServiceRecordStatus>(CustomerServiceRecordStatus.class);
		CustomerServiceRecordStatus finishedStatus = new CustomerServiceRecordStatus();
		finishedStatus.setStatusName("全部");
		serviceStatusContainer.addBean(finishedStatus);
		
		List<CustomerServiceRecordStatus> allList = serviceRecordStatusService.getAllByDeptIdAndDirection(loginUser.getDepartment().getId(), "inAndOut", true, loginUser.getDomain().getId());
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
		
		// 电话号码号输入区
		Label customerPhoneLabel = new Label("电话号码：");
		customerPhoneLabel.setWidth("-1px");
		gridLayout.addComponent(customerPhoneLabel, 4, 1);
		
		customerPhone_tf = new TextField();
		customerPhone_tf.setWidth("157px");
		customerPhone_tf.addValidator(new RegexpValidator("\\d+", "电话号码只能由数字组成"));
		customerPhone_tf.setValidationVisible(false);
		gridLayout.addComponent(customerPhone_tf, 5, 1);
	}
	
	/**
	 * 操作组件
	 */
	private void createOperateUI() {
		HorizontalLayout operatorHLayout = new HorizontalLayout();
		operatorHLayout.setSpacing(true);
		gridLayout.addComponent(operatorHLayout, 6, 0);
		
		searchButton = new Button("查 询", (ClickListener) this);
		searchButton.setStyleName("default");
		operatorHLayout.addComponent(searchButton);

		clearButton = new Button("清 空", (ClickListener) this);
		operatorHLayout.addComponent(clearButton);
		
		// 高级搜索
		recordComplexFilter = new ServiceRecordComplexFilter();
		recordComplexFilter.setHeight("-1px");
		
		complexSearchView = new PopupView(recordComplexFilter);
		complexSearchView.setHideOnMouseOut(false);
		gridLayout.addComponent(complexSearchView, 6, 1);
		gridLayout.setComponentAlignment(complexSearchView, Alignment.MIDDLE_RIGHT);
	}
	
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if(source == searchButton) {
			if(!customerPhone_tf.isValid()) {
				customerPhone_tf.getApplication().getMainWindow().showNotification("客户电话只能由数字组成，请修改后重试！", Notification.TYPE_WARNING_MESSAGE);
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
		} else if(source == clearButton) {
			createTimeScope.setValue("今天");
			customerPhone_tf.setValue("");
			projectSelector.select(projectSelector.getItemIds().toArray()[0]);
			taskFinishedStatusSelector.select(taskFinishedStatusSelector.getItemIds().toArray()[0]);

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

		String customerPhoneSql = "";
		String phoneNumber = customerPhone_tf.getValue().toString().trim();
		if(!"".equals(phoneNumber) && phoneNumber != null) {
			customerPhoneSql = " and c.customerResource in (select p.customerResource from Telephone as p where p.number like '%" + phoneNumber + "%') ";
		}
		
		String sql = "c.creator.id = "+loginUser.getId() + customerPhoneSql + specificStartTimeSql + specificFinishTimeSql + projectSql + taskFinishedStatuSql;
		return "select count(c) from CustomerServiceRecord as c where " + sql;
	}
	
	public void setTableFlipOver(FlipOverTableComponent<CustomerServiceRecord> tableFlipOver) {
		this.tableFlipOver = tableFlipOver;
		this.myServiceRecordTable = tableFlipOver.getTable();
		recordComplexFilter.setTableFlipOver(tableFlipOver);
	}
	
}
