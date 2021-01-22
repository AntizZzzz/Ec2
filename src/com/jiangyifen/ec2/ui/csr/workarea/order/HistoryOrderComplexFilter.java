package com.jiangyifen.ec2.ui.csr.workarea.order;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.entity.Order;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.entity.enumtype.DiliverStatus;
import com.jiangyifen.ec2.entity.enumtype.PayStatus;
import com.jiangyifen.ec2.entity.enumtype.QualityStatus;
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
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.Notification;

/**
 * 历史订单高级查询组件
 * @author jrh
 *  2013-7-18
 */
@SuppressWarnings("serial")
public class HistoryOrderComplexFilter extends VerticalLayout implements ClickListener, Content {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private Notification warning_notification;	// 错误警告提示信息
	
	private GridLayout gridLayout;				// 面板中的布局管理器
	
	private ComboBox createTimeScope;			// “下单时间”选择框
	private PopupDateField startCreateTime;		// “起始下单”选择框
	private PopupDateField finishCreateTime;	// “截止下单”选择框
	
	private ComboBox payStatus_cb;				// 支付状态框
	private ComboBox diliverStatus_cb;			// 发货状态选择框
	private ComboBox qualityStatus_cb;			// 下单结果选择框
	
	private TextField customerName_tf;			// 客户姓名输入文本框
	private TextField customerPhone_rf;			// 电话号码输入文本框

	private TextField customerId_tf;			// 客户编号输入文本框
	
	private Label countNo;						// 查询结果记录数标签
	private Button searchButton;				// 刷新结果按钮
	private Button clearButton;					// 清空输入内容
	
	private ValueChangeListener createTimeScopeListener;
	private ValueChangeListener startCreateTimeListener;
	private ValueChangeListener finishCreateTimeListener;
	
	private Table myServiceRecordTable;										// 存放我的订单查询结果的表格
	private FlipOverTableComponent<Order> tableFlipOver;	// 我的订单显示表格的翻页组件
	
	private User loginUser; 					// 当前的登陆用户

	public HistoryOrderComplexFilter() {
		this.setSpacing(true);
		
		loginUser = SpringContextHolder.getLoginUser();
		
		warning_notification = new Notification("", Notification.TYPE_WARNING_MESSAGE);
		warning_notification.setDelayMsec(0);
		warning_notification.setHtmlContentAllowed(true);
		
		gridLayout = new GridLayout(7, 5);
		gridLayout.setCaption("高级搜索");
		gridLayout.setSpacing(true);
		gridLayout.setMargin(true, true, false, true);
		this.addComponent(gridLayout);
		
		this.createFilterHLayout1();
		this.createFilterHLayout2();
		this.createFilterHLayout3();
	}

	/**
	 * 创建搜索组件中的第一行
	 */
	private void createFilterHLayout1() {
		// 下单时间选中框
		Label timeScopeLabel = new Label("下单时间：");
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
		
		// 起始下单选中框
		Label startTimeLabel = new Label("起始下单：");
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

		// 截止下单选中框
		Label finishTimeLabel = new Label("截止下单：");
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
		// 客户姓名输入区
		Label customerNameLabel = new Label("客户姓名：");
		customerNameLabel.setWidth("-1px");
		gridLayout.addComponent(customerNameLabel, 0, 1);
		
		customerName_tf = new TextField();
		customerName_tf.setWidth("150px");
		gridLayout.addComponent(customerName_tf, 1, 1);
		
		// 客户编号输入区
		Label customerIdLabel = new Label("客户编号：");
		customerIdLabel.setWidth("-1px");
		gridLayout.addComponent(customerIdLabel, 2, 1);
		
		customerId_tf = new TextField();
		customerId_tf.addValidator(new RegexpValidator("\\d+", "客户id 只能由数字组成！"));
		customerId_tf.setValidationVisible(false);
		customerId_tf.setWidth("158px");
		gridLayout.addComponent(customerId_tf, 3, 1);
		
		// 电话号码号输入区
		Label customerPhoneLabel = new Label("电话号码：");
		customerPhoneLabel.setWidth("-1px");
		gridLayout.addComponent(customerPhoneLabel, 4, 1);
		
		customerPhone_rf = new TextField();
		customerPhone_rf.setWidth("158px");
		gridLayout.addComponent(customerPhone_rf, 5, 1);
		
		// 查询按钮
		searchButton = new Button("查 询", this);
		searchButton.setStyleName("default");
		gridLayout.addComponent(searchButton, 6, 1);
		gridLayout.setComponentAlignment(searchButton, Alignment.MIDDLE_RIGHT);
	}

	/**
	 * 创建搜索组件中的第三行
	 */
	private void createFilterHLayout3() {
		// 任务支付状态框
		Label payStatusLabel = new Label("支付状态：");
		payStatusLabel.setWidth("-1px");
		gridLayout.addComponent(payStatusLabel, 0, 2);
		
		payStatus_cb = new ComboBox();
		payStatus_cb.addItem("全部");
		for (PayStatus payStatus : PayStatus.values()) {
			payStatus_cb.addItem(payStatus);
		}
		payStatus_cb.setWidth("150px");
		payStatus_cb.setValue("全部");
		payStatus_cb.setFilteringMode(ComboBox.FILTERINGMODE_CONTAINS);
		payStatus_cb.setNullSelectionAllowed(false);
		gridLayout.addComponent(payStatus_cb, 1, 2);
		
		// 发货状态选择框
		Label diliverStatusLabel = new Label("发货状态：");
		diliverStatusLabel.setWidth("-1px");
		gridLayout.addComponent(diliverStatusLabel, 2, 2);
		
		diliverStatus_cb = new ComboBox();
		diliverStatus_cb.setWidth("158px");
		diliverStatus_cb.addItem("全部");
		for (DiliverStatus diliverStatus : DiliverStatus.values()) {
			diliverStatus_cb.addItem(diliverStatus);
		}
		diliverStatus_cb.setValue("全部");
		diliverStatus_cb.setFilteringMode(ComboBox.FILTERINGMODE_CONTAINS);
		diliverStatus_cb.setImmediate(true);
		diliverStatus_cb.setNullSelectionAllowed(false);
		gridLayout.addComponent(diliverStatus_cb, 3, 2);
		
		// 质检状态选择框
		Label qualityStatusLabel = new Label("质检状态：");
		qualityStatusLabel.setWidth("-1px");
		gridLayout.addComponent(qualityStatusLabel, 4, 2);
		
		qualityStatus_cb = new ComboBox();
		qualityStatus_cb.setWidth("158px");
		qualityStatus_cb.addItem("全部");
		for (QualityStatus qualityStatus : QualityStatus.values()) {
			qualityStatus_cb.addItem(qualityStatus);
		}
		qualityStatus_cb.setFilteringMode(ComboBox.FILTERINGMODE_CONTAINS);
		qualityStatus_cb.setNullSelectionAllowed(false);
		qualityStatus_cb.setValue("全部");
		gridLayout.addComponent(qualityStatus_cb, 5, 2);
		
		// 清空按钮
		clearButton = new Button("清 空", this);
		gridLayout.addComponent(clearButton, 6, 2);
		gridLayout.setComponentAlignment(clearButton, Alignment.MIDDLE_RIGHT);
	}

	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if(source == searchButton) {
			try {
				if(!customerId_tf.isValid()) {
					warning_notification.setCaption("客户编号只能由数字组成！");
					customerId_tf.getApplication().getMainWindow().showNotification(warning_notification);
					return;
				}
				
				String countSql = createCountSql();
				String searchSql = countSql.replaceFirst("count\\(e\\)", "e") + " order by e.generateDate desc";
				tableFlipOver.setSearchSql(searchSql);
				tableFlipOver.setCountSql(countSql);
				tableFlipOver.refreshToFirstPage();
				countNo.setValue("<B>结果总数：" + tableFlipOver.getTotalRecord() + "</B>");
				
				// 在查询到结果后，默认选择 Table 的第一条记录
				BeanItemContainer<Order> taskBeanItemContainer = tableFlipOver.getEntityContainer();
				if(taskBeanItemContainer.size() > 0) {
					Object firstItemId = taskBeanItemContainer.getIdByIndex(0);
					myServiceRecordTable.setValue(taskBeanItemContainer.getItem(firstItemId).getBean());
				} else {
					myServiceRecordTable.setValue(null);
				}
			} catch (Exception e) {
				e.printStackTrace();
				logger.error("jrh 坐席按高级查询订单时出现异常---》"+e.getMessage(), e);
				countNo.getApplication().getMainWindow().showNotification("查询失败，请稍后重试！", Notification.TYPE_WARNING_MESSAGE);
			}
		} else if(source == clearButton) {
			createTimeScope.setValue("今天");

			customerId_tf.setValue("");
			customerName_tf.setValue("");
			customerPhone_rf.setValue("");
			
			payStatus_cb.select("全部");
			qualityStatus_cb.select("全部");
			diliverStatus_cb.setValue("全部");
		}
	}

	private String  createCountSql() {
		String specificStartTimeSql = "";
		if(startCreateTime.getValue() != null) {
			specificStartTimeSql = " and e.generateDate >= '" + dateFormat.format(startCreateTime.getValue()) +"'";
		}

		String specificFinishTimeSql = "";
		if(finishCreateTime.getValue() != null) {
			specificFinishTimeSql = " and e.generateDate <= '" + dateFormat.format(finishCreateTime.getValue()) +"'";
		}
		
		String customerNameSql = "";
		String inputName = customerName_tf.getValue().toString().trim();
		if(!"".equals(inputName) && inputName != null) {
			customerNameSql = " and e.customerResource.name like '%" + inputName + "%'";
		}
		
		String customerIdSql = "";
		String customerId = StringUtils.trimToEmpty((String) customerId_tf.getValue());
		if(!"".equals(customerId)) {
			customerIdSql = " and e.customerResource.id = " + customerId;
		}
		
		String customerPhoneSql = "";
		String phoneNumber = customerPhone_rf.getValue().toString().trim();
		if(!"".equals(phoneNumber) && phoneNumber != null) {
			customerPhoneSql = " and e.customerResource in (select p.customerResource from Telephone as p where p.number like '%" + phoneNumber + "%') ";
		}

		String payStatusSql = "";
		if(!payStatus_cb.getValue().equals("全部")){
			PayStatus payStatus = (PayStatus) payStatus_cb.getValue();
			if (payStatus != null) {
				payStatusSql = " and e.payStatus = " + payStatus.getClass().getName() + ".";
				if (payStatus.getIndex() == 0) {
					payStatusSql += "PAYED";
				} else if (payStatus.getIndex() == 1) {
					payStatusSql += "NOTPAYED";
				}
			}
		}
		
		String diliverStatusSql = "";
		if(!diliverStatus_cb.getValue().equals("全部")){
			DiliverStatus diliverStatus = (DiliverStatus) diliverStatus_cb.getValue();
			if (diliverStatus != null) {
				diliverStatusSql = " and e.diliverStatus = " + diliverStatus.getClass().getName() + ".";
				if (diliverStatus.getIndex() == 0) {
					diliverStatusSql += "NOTDILIVERED";
				} else if (diliverStatus.getIndex() == 1) {
					diliverStatusSql += "DILIVERED";
				} else if (diliverStatus.getIndex() == 2) {
					diliverStatusSql += "RECEIVEED";
				}
			}
		}
		
		String qualityStatusSql = "";
		if(!qualityStatus_cb.getValue().equals("全部")){
			QualityStatus qualityStatus = (QualityStatus) qualityStatus_cb.getValue();
			if (qualityStatus != null) {
				qualityStatusSql = " and e.qualityStatus = " + qualityStatus.getClass().getName() + ".";
				if (qualityStatus.getIndex() == 0) {
					qualityStatusSql += "CONFIRMING";
				} else if (qualityStatus.getIndex() == 1) {
					qualityStatusSql += "CONFIRMED";
				} else if (qualityStatus.getIndex() == 2) {
					qualityStatusSql += "FINISHED";
				} else if (qualityStatus.getIndex() == 3) {
					qualityStatusSql += "CANCELED";
				}
			}
		}
		
		String sql = specificStartTimeSql + specificFinishTimeSql + customerIdSql 
				+ customerNameSql + customerPhoneSql + payStatusSql + diliverStatusSql + qualityStatusSql;
		return "select count(e) from Order as e where e.csrUserId = "+loginUser.getId() + sql;
	}
	
	public void setTableFlipOver(FlipOverTableComponent<Order> tableFlipOver) {
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
		return gridLayout;
	}
	
}
