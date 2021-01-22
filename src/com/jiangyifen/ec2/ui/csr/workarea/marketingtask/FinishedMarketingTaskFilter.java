package com.jiangyifen.ec2.ui.csr.workarea.marketingtask;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;

import com.jiangyifen.ec2.entity.CustomerServiceRecordStatus;
import com.jiangyifen.ec2.entity.MarketingProjectTask;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.entity.enumtype.MarketingProjectTaskType;
import com.jiangyifen.ec2.service.eaoservice.CustomerServiceRecordStatusService;
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
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.PopupDateField;
import com.vaadin.ui.PopupView.Content;
import com.vaadin.ui.Window.Notification;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

/**
 * 已完成营销任务搜索组件
 * @author jrh
 *
 */
@SuppressWarnings("serial")
public class FinishedMarketingTaskFilter extends VerticalLayout implements ClickListener, Content {
	
	private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private Notification warning_notification;	// 错误警告提示信息
	
	private Label unfinishedTaskLabel;	// 说明该组件是用于搜索已完成任务的
	private GridLayout gridLayout;		// 放所有的查询组件

	private ComboBox orderTimeScope;			// “预约时间”选择框
	private PopupDateField startOrderTime;		// “起始预约”选择框
	private PopupDateField finishOrderTime;		// “截止预约”选择框
	
	private TextField customerName_tf;			// 客户姓名输入文本框
	private TextField customerPhone_tf;			// 客户电话输入文本框
	private TextField customerId_tf;			// 客户编号输入文本框
	
	private ComboBox taskFinishedStatusSelector;	// 任务呼叫完成状态选择框
	private ComboBox bridgedSelector;				// 是否接通选择框
	
	private Button searchButton;		// 刷新结果按钮
	private Button clearButton;			// 清空输入内容

	private ValueChangeListener orderTimeScopeListener;
	private ValueChangeListener startOrderTimeListener;
	private ValueChangeListener finishOrderTimeListener;
	
	private User loginUser; 			// 当前的登陆用户
	private Long currentProjectId = (long) 0;		// 当前工作项目的Id
	private BeanItemContainer<CustomerServiceRecordStatus> taskFinishedStatus;	// 存放任务完成状态的数据源
	private CustomerServiceRecordStatusService serviceRecordStatusService;
	
	private Table csrTaskTable;			// 存放任务查询结果的表格
	private FlipOverTableComponent<MarketingProjectTask> finishedTableFlip;		// finishedTaskTable的翻页组件
	
	public FinishedMarketingTaskFilter() {
		this.setSpacing(true);
		this.setWidth("-1px");
		loginUser = SpringContextHolder.getLoginUser();
		serviceRecordStatusService = SpringContextHolder.getBean("customerServiceRecordStatusService");

		warning_notification = new Notification("", Notification.TYPE_WARNING_MESSAGE);
		warning_notification.setDelayMsec(0);
		warning_notification.setHtmlContentAllowed(true);
		
		unfinishedTaskLabel = new Label("<B>搜索条件 </B>", Label.CONTENT_XHTML);
		unfinishedTaskLabel.setWidth("-1px");
		this.addComponent(unfinishedTaskLabel);
		
		gridLayout = new GridLayout(7, 3);
		gridLayout.setSpacing(true);
		gridLayout.setMargin(false, true, false, true);
		this.addComponent(gridLayout);
		this.createFilterHLayout1();
		this.createFilterHLayout3();
		this.createFilterHLayout2();
		this.createButtonsLayout();
	}
	
	private void createFilterHLayout1() {
		// 联系时间选中框
		Label timeScopeLabel = new Label("预约时间：");
		timeScopeLabel.setWidth("-1px");
		gridLayout.addComponent(timeScopeLabel, 0, 0);
		
		orderTimeScope = new ComboBox();
		orderTimeScope.addItem("今天");
		orderTimeScope.addItem("昨天");
		orderTimeScope.addItem("本周");
		orderTimeScope.addItem("上周");
		orderTimeScope.addItem("本月");
		orderTimeScope.addItem("上月");
		orderTimeScope.addItem("精确时间");
		orderTimeScope.setValue("精确时间");
		orderTimeScope.setWidth("120px");
		orderTimeScope.setImmediate(true);
		orderTimeScope.setNullSelectionAllowed(false);
		gridLayout.addComponent(orderTimeScope, 1, 0);
		
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
		startTimeLabel.setWidth("-1px");
		gridLayout.addComponent(startTimeLabel, 2, 0);
	
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
		startOrderTime.setWidth("160px");
		startOrderTime.setValue(dates[0]);
		startOrderTime.setDateFormat("yyyy-MM-dd HH:mm:ss");
		startOrderTime.setParseErrorMessage("时间格式不合法");
		startOrderTime.setResolution(PopupDateField.RESOLUTION_SEC);
		startOrderTime.addListener(startOrderTimeListener);
		gridLayout.addComponent(startOrderTime, 3, 0);
	
		// 截止联系选中框
		Label finishTimeLabel = new Label("截止预约：");
		finishTimeLabel.setWidth("-1px");
		gridLayout.addComponent(finishTimeLabel, 4, 0);
	
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
		finishOrderTime.setWidth("160px");
		finishOrderTime.setValue(dates[1]);
		finishOrderTime.setDateFormat("yyyy-MM-dd HH:mm:ss");
		finishOrderTime.setParseErrorMessage("时间格式不合法");
		finishOrderTime.setResolution(PopupDateField.RESOLUTION_SEC);
		finishOrderTime.addListener(finishOrderTimeListener);
		gridLayout.addComponent(finishOrderTime, 5, 0);
	}

	private void createFilterHLayout2() {
		// 客户姓名输入区
		Label customerNameLabel = new Label("客户姓名：");
		customerNameLabel.setWidth("-1px");
		gridLayout.addComponent(customerNameLabel, 0, 1);
		
		customerName_tf = new TextField();
		customerName_tf.setWidth("120px");
		gridLayout.addComponent(customerName_tf, 1, 1);

		// 电话号码号输入区
		Label customerPhoneLabel = new Label("电话号码：");
		customerPhoneLabel.setWidth("-1px");
		gridLayout.addComponent(customerPhoneLabel, 2, 1);
		
		customerPhone_tf = new TextField();
		customerPhone_tf.setWidth("134px");
		gridLayout.addComponent(customerPhone_tf, 3, 1);	
		
		// 客户编号输入区
		Label customerIdLabel = new Label("客户编号：");
		customerIdLabel.setWidth("-1px");
		gridLayout.addComponent(customerIdLabel, 4, 1);
		
		customerId_tf = new TextField();
		customerId_tf.addValidator(new RegexpValidator("\\d+", "客户id 只能由数字组成！"));
		customerId_tf.setValidationVisible(false);
		customerId_tf.setWidth("134px");
		gridLayout.addComponent(customerId_tf, 5, 1);
	}

	private void createFilterHLayout3() {
		// 接通条件选择框
		Label bridgedLabel = new Label("接通情况：");
		bridgedLabel.setWidth("-1px");
		gridLayout.addComponent(bridgedLabel, 0, 2);
		
		bridgedSelector = new ComboBox();
		bridgedSelector.addItem("all");
		bridgedSelector.addItem("bridged");
		bridgedSelector.addItem("unbridged");
		bridgedSelector.setItemCaption("all", "全部");
		bridgedSelector.setItemCaption("bridged", "已接通");
		bridgedSelector.setItemCaption("unbridged", "未接通");
		bridgedSelector.setValue("all");
		bridgedSelector.setWidth("120px");
		bridgedSelector.setNullSelectionAllowed(false);
		gridLayout.addComponent(bridgedSelector, 1, 2);

		// 任务呼叫完成状态选择框
		Label taskFinishedStatusLabel = new Label("联系结果：");
		taskFinishedStatusLabel.setWidth("-1px");
		gridLayout.addComponent(taskFinishedStatusLabel, 2, 2);
		
		taskFinishedStatusSelector = new ComboBox();
		taskFinishedStatusSelector.setWidth("134px");
		taskFinishedStatusSelector.setNullSelectionAllowed(false);
		gridLayout.addComponent(taskFinishedStatusSelector, 3, 2);

		// 从数据库中获取任务的“处理结果”信息，并绑定到Container中
		taskFinishedStatus = new BeanItemContainer<CustomerServiceRecordStatus>(CustomerServiceRecordStatus.class);
		CustomerServiceRecordStatus finishedStatus = new CustomerServiceRecordStatus();
		finishedStatus.setStatusName("全部");
		taskFinishedStatus.addBean(finishedStatus);
		taskFinishedStatus.addAll(serviceRecordStatusService.getAllByDirection(loginUser.getDomain(), "outgoing"));
		taskFinishedStatusSelector.setContainerDataSource(taskFinishedStatus);
		taskFinishedStatusSelector.setValue(finishedStatus);
	}

	private void createButtonsLayout() {
		gridLayout.setColumnExpandRatio(4, 1.0f);

		searchButton = new Button("查 询", this);
		searchButton.addStyleName("default");
		gridLayout.addComponent(searchButton, 6, 0);
		gridLayout.setComponentAlignment(searchButton, Alignment.MIDDLE_RIGHT);

		clearButton = new Button("清 空", this);
		gridLayout.addComponent(clearButton, 6, 1);
		gridLayout.setComponentAlignment(clearButton, Alignment.MIDDLE_RIGHT);
	}

	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if(source == searchButton && currentProjectId != 0) {
			if(!customerId_tf.isValid()) {
				warning_notification.setCaption("客户编号只能由数字组成！");
				customerId_tf.getApplication().getMainWindow().showNotification(warning_notification);
				return;
			}
			
			String countSql = createCountSql();
			String searchSql = countSql.replaceFirst("count\\(mpt\\)", "mpt") + " order by mpt.lastUpdateDate desc, mpt.id asc";
			
			finishedTableFlip.setSearchSql(searchSql);
			finishedTableFlip.setCountSql(countSql);
			finishedTableFlip.refreshToFirstPage();
			
			// 在查询到结果后，默认选择 Table 的第一条记录
			BeanItemContainer<MarketingProjectTask> taskBeanItemContainer = finishedTableFlip.getEntityContainer();
			if(taskBeanItemContainer.size() > 0) {
				Object firstItemId = taskBeanItemContainer.getIdByIndex(0);
				csrTaskTable.setValue(taskBeanItemContainer.getItem(firstItemId).getBean());
			} else {
				csrTaskTable.setValue(null);
			}
		} else {
			orderTimeScope.setValue("精确时间");
			startOrderTime.setValue(null);
			finishOrderTime.setValue(null);
			
			customerName_tf.setValue("");
			customerPhone_tf.setValue("");
			customerId_tf.setValue("");
			
			taskFinishedStatusSelector.select(taskFinishedStatusSelector.getItemIds().toArray()[0]);
			bridgedSelector.select("全部");
		}
	}

	private String  createCountSql() {
		String specificStartOrderTimeSql = "";
		if(startOrderTime.getValue() != null) {
			specificStartOrderTimeSql = " and mpt.orderTime >= '" + dateFormat.format(startOrderTime.getValue()) +"'";
		}

		String specificFinishOrderTimeSql = "";
		if(finishOrderTime.getValue() != null) {
			specificFinishOrderTimeSql = " and mpt.orderTime <= '" + dateFormat.format(finishOrderTime.getValue()) +"'";
		}
		
		String customerNameSql = "";
		String inputName = ((String)customerName_tf.getValue()).trim();
		if(!"".equals(inputName) && inputName != null) {
			customerNameSql = " and mpt.customerResource.name like '%" + inputName + "%' ";
		}
		
		String customerPhoneSql = "";
		String phoneNumber = StringUtils.trimToEmpty((String) customerPhone_tf.getValue());
		if(!"".equals(phoneNumber)) {
			customerPhoneSql = "and mpt.customerResource in " 
					+ "( select p.customerResource from Telephone as p where p.number like '%" + phoneNumber + "%' )";
		}
		
		String customerIdSql = "";
		String customerId = StringUtils.trimToEmpty((String) customerId_tf.getValue());
		if(!"".equals(customerId)) {
			customerIdSql = " and mpt.customerResource.id = " + customerId;
		}
		
		String isAnsweredSql = "";	// 是否接通
		if( "bridged".equals(bridgedSelector.getValue()) ) {
			isAnsweredSql = " and mpt.isAnswered = true ";
		} else if( "unbridged".equals(bridgedSelector.getValue()) ) {
			isAnsweredSql = " and mpt.isAnswered = false ";
		}
		
		String taskFinishedStatuSql = "";
		if(!"全部".equals(taskFinishedStatusSelector.getValue().toString())) {
			taskFinishedStatuSql = " and mpt.lastStatus = '" + taskFinishedStatusSelector.getValue() + "' ";
		}
		
		// 按任务类型 为 “营销任务”查询
		String typeSql = MarketingProjectTaskType.class.getName()+".MARKETING";
		String sql = " and mpt.user.id = "+loginUser.getId() + " and mpt.marketingProject.id = " + currentProjectId + " and mpt.isFinished = true " 
				+ specificStartOrderTimeSql + specificFinishOrderTimeSql + taskFinishedStatuSql + customerNameSql + customerPhoneSql + customerIdSql + isAnsweredSql;
		return "select count(mpt) from MarketingProjectTask as mpt where mpt.isUseable = true and mpt.marketingProjectTaskType = " + typeSql + sql;
	}
	
//	/**
//	 * 通过手机输入框 ，返回新组成的加密搜索字符串
//	 * @return	
//	 */
//	private String createSearchPhoneNo() {
//		String phoneNumber = customerPhone.getValue().toString().trim();
//		String[] phoneNumbers = new String[2];
//		if(phoneNumber.contains("*")) {
//			int countAsterisk = phoneNumber.lastIndexOf("*") - phoneNumber.indexOf("*") + 1;
//			phoneNumbers[0] = phoneNumber.substring(0, phoneNumber.indexOf("*"));
//			phoneNumbers[1] = phoneNumber.substring(phoneNumber.lastIndexOf("*") + 1, phoneNumber.length());
//			String commonChar = "";
//			for(int i = 0; i < countAsterisk; i++) {
//				commonChar = commonChar + "_";
//			}
//			phoneNumber = phoneNumbers[0] + "%" + commonChar + "%" + phoneNumbers[1];
//		}
//		return phoneNumber;
//	}

	/**
	 * 将收索组件中的值置为默认值, 并重启设置当前项目的Id 号
	 */
	public void refresh(Long currentWorkingProjectId) {
		this.currentProjectId = currentWorkingProjectId;
		clearButton.click();
	}
	
	public void setFinishedTableFlip(FlipOverTableComponent<MarketingProjectTask> finishedTableFlip) {
		this.finishedTableFlip = finishedTableFlip;
		csrTaskTable = finishedTableFlip.getTable();
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
