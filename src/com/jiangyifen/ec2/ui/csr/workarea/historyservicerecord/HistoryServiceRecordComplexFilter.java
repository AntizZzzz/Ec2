package com.jiangyifen.ec2.ui.csr.workarea.historyservicerecord;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.jiangyifen.ec2.entity.CustomerLevel;
import com.jiangyifen.ec2.entity.CustomerServiceRecord;
import com.jiangyifen.ec2.entity.CustomerServiceRecordStatus;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.MarketingProject;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.service.eaoservice.CustomerLevelService;
import com.jiangyifen.ec2.service.eaoservice.CustomerServiceRecordStatusService;
import com.jiangyifen.ec2.service.eaoservice.MarketingProjectService;
import com.jiangyifen.ec2.ui.FlipOverTableComponent;
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
import com.vaadin.ui.Window.Notification;
import com.vaadin.ui.themes.BaseTheme;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

/**
 * 历史客服记录高级查询组件
 * @author jrh
 *  2013-7-18
 */
@SuppressWarnings("serial")
public class HistoryServiceRecordComplexFilter extends VerticalLayout implements ClickListener, ValueChangeListener, Content {
	
	private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private GridLayout gridLayout;				// 面板中的布局管理器
	
	private ComboBox createTimeScope;			// “联系时间”选择框
	private PopupDateField startCreateTime;		// “起始联系”选择框
	private PopupDateField finishCreateTime;	// “截止联系”选择框
	
	private ComboBox projectSelector;			// 项目选择框
	private ComboBox directionSelector;			// 呼叫方向选择框
	private TextField customerId_tf;			// 客户编号输入文本框
	
	private TextField customerName_tf;			// 客户姓名输入文本框
	private TextField customerPhone_tf;			// 电话号码输入文本框
	private TextField customerCompany_tf;		// 电话号码输入文本框

	private ComboBox orderTimeScope;			// “预约时间”选择框
	private PopupDateField startOrderTime;		// “起始预约”选择框
	private PopupDateField finishOrderTime;		// “截止预约”选择框
	
	private ComboBox customerLevel_cb; 			// “客户等级”选择

	/******************  客服记录结果选择组件   ************************/
	private Button selectAll_bt;
	private Button noSelectAll_bt;
	private Button invertSelect_bt;
	private OptionGroup serviceResult_og;
	
	private Label countNo;						// 查询结果记录数标签
	private Button searchButton;				// 刷新结果按钮
	private Button clearButton;					// 清空输入内容
	
	private ValueChangeListener createTimeScopeListener;
	private ValueChangeListener startCreateTimeListener;
	private ValueChangeListener finishCreateTimeListener;
	
	private ValueChangeListener orderTimeScopeListener;
	private ValueChangeListener startOrderTimeListener;
	private ValueChangeListener finishOrderTimeListener;
	
	private List<CustomerServiceRecordStatus> allServiceRecordStatus; 		// 所有的客服记录状态对象
	private List<CustomerServiceRecordStatus> allOutgoingRecordStatus; 		// 所有的客服记录中 呼出 记录的状态对象
	private List<CustomerServiceRecordStatus> allIncomingRecordStatus; 		// 所有的客服记录中 呼入 记录的状态对象
	private BeanItemContainer<MarketingProject> projectContainer;			// 存放项目选择的数据源
	private BeanItemContainer<CustomerLevel> customerLevelContainer;		// 客户级别容器
	private BeanItemContainer<CustomerServiceRecordStatus> serviceStatusContainer;	// 存放任务完成状态的数据源
	
	private Table myServiceRecordTable;										// 存放我的客服记录查询结果的表格
	private FlipOverTableComponent<CustomerServiceRecord> tableFlipOver;	// 我的客服记录显示表格的翻页组件
	
	private MarketingProjectService marketingProjectService;				// 项目服务类
	private CustomerLevelService customerLevelService;						// 客户级别服务类
	private CustomerServiceRecordStatusService serviceRecordStatusService;	// 客服记录服务类
	
	private User loginUser; 					// 当前的登陆用户
	private Domain domain;						// 当前用户所属域

	public HistoryServiceRecordComplexFilter() {
		this.setSpacing(true);
		this.setWidth("-1px");
		
		loginUser = SpringContextHolder.getLoginUser();
		domain = SpringContextHolder.getDomain();
		marketingProjectService = SpringContextHolder.getBean("marketingProjectService");
		serviceRecordStatusService = SpringContextHolder.getBean("customerServiceRecordStatusService");
		customerLevelService = SpringContextHolder.getBean("customerLevelService");
		
		allServiceRecordStatus = serviceRecordStatusService.getAllByDeptIdAndDirection(loginUser.getDepartment().getId(), "inAndOut", true, domain.getId());
		allOutgoingRecordStatus = serviceRecordStatusService.getAllByDeptIdAndDirection(loginUser.getDepartment().getId(), "outgoing", true, domain.getId());
		allIncomingRecordStatus = serviceRecordStatusService.getAllByDeptIdAndDirection(loginUser.getDepartment().getId(), "incoming", true, domain.getId());

		serviceStatusContainer = new BeanItemContainer<CustomerServiceRecordStatus>(CustomerServiceRecordStatus.class);
		serviceStatusContainer.addAll(allServiceRecordStatus);

		CustomerLevel customerLevel = new CustomerLevel();
		customerLevel.setLevelName("全部");
		customerLevel.setDomain(domain); 
		customerLevelContainer = new BeanItemContainer<CustomerLevel>(CustomerLevel.class);
		customerLevelContainer.addBean(customerLevel);
		customerLevelContainer.addAll(customerLevelService.getAll(domain));
		
		gridLayout = new GridLayout(7, 5);
		gridLayout.setCaption("高级搜索");
		gridLayout.setSpacing(true);
		gridLayout.setMargin(true, true, false, true);
		this.addComponent(gridLayout);

		// 创建联系结果的相关组件
		this.createStatusComponents();
		
		this.createFilterHLayout1();
		this.createFilterHLayout2();
		this.createFilterHLayout3();
		this.createFilterHLayout4();
		this.createFilterHLayout5();
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
	 * 创建搜索组件中的第一行
	 */
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
		createTimeScope.setWidth("150px");
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
		startCreateTime.setWidth("160px");
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
		finishCreateTime.setWidth("160px");
		finishCreateTime.setValue(dates[1]);
		finishCreateTime.setDateFormat("yyyy-MM-dd HH:mm:ss");
		finishCreateTime.setParseErrorMessage("时间格式不合法");
		finishCreateTime.setResolution(PopupDateField.RESOLUTION_SEC);
		finishCreateTime.addListener(finishCreateTimeListener);
		gridLayout.addComponent(finishCreateTime, 5, 0);
		
		// 查询结果总数显示标签
		countNo = new Label("<B>结果总数：0</B>", Label.CONTENT_XHTML);
		countNo.setWidth("-1px");
		gridLayout.addComponent(countNo, 6, 0);
	}

	/**
	 * 创建搜索组件中的第二行
	 */
	private void createFilterHLayout2() {
		// 任务项目选择框
		Label projectLabel = new Label("项目选择：");
		projectLabel.setWidth("-1px");
		gridLayout.addComponent(projectLabel, 0, 1);
		
		projectSelector = new ComboBox();
		projectSelector.setWidth("150px");
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
		directionSelector.setNullSelectionAllowed(false);
		addListenerToDirectionSelector();
		gridLayout.addComponent(directionSelector, 3, 1);
		
		// 客户编号输入区
		Label customerIdLabel = new Label("客户编号：");
		customerIdLabel.setWidth("-1px");
		gridLayout.addComponent(customerIdLabel, 4, 1);
		
		customerId_tf = new TextField();
		customerId_tf.addValidator(new RegexpValidator("\\d+", "客户id 只能由数字组成！"));
		customerId_tf.setValidationVisible(false);
		customerId_tf.setWidth("158px");
		gridLayout.addComponent(customerId_tf, 5, 1);

		// 修改客服记录状态显示名称
		editRecordStatusShowName();
		
		// 查询按钮
		searchButton = new Button("查 询", this);
		searchButton.setStyleName("default");
		gridLayout.addComponent(searchButton, 6, 1);
		gridLayout.setComponentAlignment(searchButton, Alignment.MIDDLE_RIGHT);
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
		serviceResult_og.setValue(null);	// 然后先清空选项
		for(CustomerServiceRecordStatus status : serviceStatusContainer.getItemIds()) {
			String direction = "incoming".equals(status.getDirection()) ? "呼入" : "呼出";
			serviceResult_og.setItemCaption(status, status.getStatusName()+" - "+direction);
			serviceResult_og.select(status);
		}
	}
	
	/**
	 * 创建搜索组件中的第三行
	 */
	private void createFilterHLayout3() {
		// 客户姓名输入区
		Label customerNameLabel = new Label("客户姓名：");
		customerNameLabel.setWidth("-1px");
		gridLayout.addComponent(customerNameLabel, 0, 2);
		
		customerName_tf = new TextField();
		customerName_tf.setWidth("150px");
		gridLayout.addComponent(customerName_tf, 1, 2);

		// 电话号码号输入区
		Label customerPhoneLabel = new Label("电话号码：");
		customerPhoneLabel.setWidth("-1px");
		gridLayout.addComponent(customerPhoneLabel, 2, 2);
		
		customerPhone_tf = new TextField();
		customerPhone_tf.setWidth("158px");
		customerPhone_tf.addValidator(new RegexpValidator("\\d+", "电话号码只能由数字组成"));
		customerPhone_tf.setValidationVisible(false);
		gridLayout.addComponent(customerPhone_tf, 3, 2);
		
		// 公司名称输入区
		Label companyLabel = new Label("公司名称：");
		companyLabel.setWidth("-1px");
		gridLayout.addComponent(companyLabel, 4, 2);
		
		customerCompany_tf = new TextField();
		customerCompany_tf.setWidth("158px");
		gridLayout.addComponent(customerCompany_tf, 5, 2);
		
		// 清空按钮
		clearButton = new Button("清 空", this);
		gridLayout.addComponent(clearButton, 6, 2);
		gridLayout.setComponentAlignment(clearButton, Alignment.MIDDLE_RIGHT);
	}

	/**
	 * 按预约时间查询
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
	 * 创建搜索组件中的第五行
	 */
	private void createFilterHLayout5() {
		// 客户等级选择
		Label levelLabel = new Label("客户等级：");
		levelLabel.setWidth("-1px");
		gridLayout.addComponent(levelLabel, 0, 4);

		customerLevel_cb = new ComboBox();
		customerLevel_cb.setImmediate(true);
		customerLevel_cb.setWidth("150px");
		customerLevel_cb.setNullSelectionAllowed(false);
		customerLevel_cb.setItemCaptionPropertyId("levelName");
		customerLevel_cb.setContainerDataSource(customerLevelContainer);
		customerLevel_cb.select(customerLevelContainer.getItemIds().toArray()[0]);
		gridLayout.addComponent(customerLevel_cb, 1, 4);
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

	@SuppressWarnings("unchecked")
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if(source == searchButton) {
			if(!customerId_tf.isValid()) {
				customerId_tf.getApplication().getMainWindow().showNotification("客户编号只能由数字组成，请修改后重试！", Notification.TYPE_WARNING_MESSAGE);
				return;
			}
			
			if(!customerPhone_tf.isValid()) {
				customerPhone_tf.getApplication().getMainWindow().showNotification("客户电话只能由数字组成，请修改后重试！", Notification.TYPE_WARNING_MESSAGE);
				return;
			}
			
			String countSql = createCountSql();
			String searchSql = countSql.replaceFirst("count\\(c\\)", "c") + " order by c.createDate desc";
			tableFlipOver.setSearchSql(searchSql);
			tableFlipOver.setCountSql(countSql);
			tableFlipOver.refreshToFirstPage();
			countNo.setValue("<B>结果总数：" + tableFlipOver.getTotalRecord() + "</B>");

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

			projectSelector.select(projectSelector.getItemIds().toArray()[0]);
			directionSelector.setValue("全部");
			customerId_tf.setValue("");

			orderTimeScope.setValue("精确时间");
			startOrderTime.setValue(null);
			finishOrderTime.setValue(null);

			customerName_tf.setValue("");
			customerPhone_tf.setValue("");
			customerCompany_tf.setValue("");
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

	@SuppressWarnings("unchecked")
	private String  createCountSql() {
		String projectSql = "";
		MarketingProject project = (MarketingProject) projectSelector.getValue();
		if(!"全部".equals(project.getProjectName())) {
			projectSql = " and c.marketingProject.id = " + project.getId();
		}
		
		String directionSql = "";
		if("呼入".equals(directionSelector.getValue())) {
			directionSql = " and c.direction = 'incoming'";
		} else if("呼出".equals(directionSelector.getValue())) {
			directionSql = " and c.direction = 'outgoing'";
		}

		String taskFinishedStatusSql = "";
		Set<CustomerServiceRecordStatus> recordStatusSet = (Set<CustomerServiceRecordStatus>) serviceResult_og.getValue();
		if(recordStatusSet.size() != serviceStatusContainer.getItemIds().size()) {
			ArrayList<Long> statusIds = new ArrayList<Long>();
			for(CustomerServiceRecordStatus status : recordStatusSet) {
				statusIds.add(status.getId());
			}
			String statusIdSql = StringUtils.join(statusIds, ",");
			taskFinishedStatusSql = " and c.serviceRecordStatus.id in ("+statusIdSql+")";
		}
		
		String specificStartTimeSql = "";
		if(startCreateTime.getValue() != null) {
			specificStartTimeSql = " and c.createDate >= '" + dateFormat.format(startCreateTime.getValue()) +"'";
		}

		String specificFinishTimeSql = "";
		if(finishCreateTime.getValue() != null) {
			specificFinishTimeSql = " and c.createDate <= '" + dateFormat.format(finishCreateTime.getValue()) +"'";
		}
		
		String customerNameSql = "";
		String inputName = customerName_tf.getValue().toString().trim();
		if(!"".equals(inputName) && inputName != null) {
			customerNameSql = " and c.customerResource.name like '%" + inputName + "%'";
		}
		
		String customerPhoneSql = "";
		String phoneNumber = customerPhone_tf.getValue().toString().trim();
		if(!"".equals(phoneNumber) && phoneNumber != null) {
			customerPhoneSql = " and c.customerResource.id in (select p.customerResource.id from Telephone as p where p.number like '%" + phoneNumber + "%') ";
		}
		
		String customerCompanySql = "";
		String inputCompany = customerCompany_tf.getValue().toString().trim();
		if(!"".equals(inputCompany) && inputCompany != null) {
			customerCompanySql = " and c.customerResource.company.name like '%" + inputCompany + "%'";
		}
		
		String specificStartOrderTimeSql = "";
		if(startOrderTime.isVisible() && startOrderTime.getValue() != null) {
			specificStartOrderTimeSql = " and c.orderTime >= '" + dateFormat.format(startOrderTime.getValue()) +"'";
		}

		String specificFinishOrderTimeSql = "";
		if(finishOrderTime.isVisible() && finishOrderTime.getValue() != null) {
			specificFinishOrderTimeSql = " and c.orderTime <= '" + dateFormat.format(finishOrderTime.getValue()) +"'";
		}
		
		
		String customerIdSql = "";
		String customerId = StringUtils.trimToEmpty((String) customerId_tf.getValue());
		if(!"".equals(customerId)) {
			customerIdSql = " and c.customerResource.id = " + customerId;
		}

		String customerLevelSql = "";
		CustomerLevel customerLevel = (CustomerLevel) customerLevel_cb.getValue();
		if(customerLevel.getId() != null) {
			customerLevelSql = " and c.customerResource.customerLevel.id = " + customerLevel.getId();
		}

		// 拼接查询语句
		String dynamicSql = " and c.creator.id = " +loginUser.getId() + customerIdSql + customerPhoneSql + specificStartOrderTimeSql + specificFinishOrderTimeSql
				+ specificStartTimeSql + specificFinishTimeSql + projectSql + customerLevelSql + customerNameSql + customerCompanySql + directionSql + taskFinishedStatusSql;

		/************************************************************************************
		 * lastItemSql： 新增功能，用于查询某个客户 最终客服记录
		 ***********************************************************************************/
		String lastItemSql = " and NOT EXISTS ( select c2 from CustomerServiceRecord as c2 where c2.customerResource.id = c.customerResource.id "
				+ " and c2.creator.id = " +loginUser.getId()+ " and c2.createDate > c.createDate ) ";
		
		return "select count(c) from CustomerServiceRecord as c where 1=1 " + dynamicSql + lastItemSql;
	}
	
	public void setTableFlipOver(FlipOverTableComponent<CustomerServiceRecord> tableFlipOver) {
		this.tableFlipOver = tableFlipOver;
		countNo.setValue("<B>结果总数：" + tableFlipOver.getTotalRecord() + "</B>");
		
		this.myServiceRecordTable = tableFlipOver.getTable();
	}

	@Override
	public String getMinimizedValueAsHTML() {
		return "高级搜索";
	}

	@Override
	public Component getPopupComponent() {
		return this;
	}
	
}
