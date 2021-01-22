package com.jiangyifen.ec2.ui.csr.workarea.order;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.jiangyifen.ec2.entity.Order;
import com.jiangyifen.ec2.entity.User;
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
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.PopupDateField;
import com.vaadin.ui.PopupView;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

/**
 * 我的历史订单简单查询组件
 * @author jrh
 *  2013-7-17
 */
@SuppressWarnings("serial")
public class HistoryOrderSimpleFilter extends VerticalLayout implements ClickListener {
	
	private GridLayout gridLayout;					// 面板中的布局管理器
	
	private ComboBox createTimeScope;				// “下单时间”选择框
	private PopupDateField startCreateTime;			// “起始下单”选择框
	private PopupDateField finishCreateTime;		// “截止下单”选择框
	
	private TextField customerName_tf;				// 客户姓名输入文本框
	private TextField customerPhone_rf;				// 电话号码输入文本框
	
	private Button searchButton;					// 刷新结果按钮
	private Button clearButton;						// 清空输入内容
	private PopupView complexSearchView;			// 复杂搜索界面
	
	private ValueChangeListener createTimeScopeListener;
	private ValueChangeListener startCreateTimeListener;
	private ValueChangeListener finishCreateTimeListener;
	
	private User loginUser; 						// 当前的登陆用户
	private Table myOrderTable;						// 存放我的订单查询结果的表格
	private HistoryOrderComplexFilter recordComplexFilter;					// 存放高级收索条件的组件
	private FlipOverTableComponent<Order> tableFlipOver;	// 我的订单显示表格的翻页组件
	
	public HistoryOrderSimpleFilter() {
		this.setSpacing(true);
		loginUser = SpringContextHolder.getLoginUser();
		
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
		startCreateTime.setWidth("156px");
		startCreateTime.setValue(dates[0]);
		startCreateTime.setDateFormat("yyyy-MM-dd HH:mm:ss");
		startCreateTime.setParseErrorMessage("时间格式不合法");
		startCreateTime.setResolution(PopupDateField.RESOLUTION_SEC);
		startCreateTime.addListener(startCreateTimeListener);
		gridLayout.addComponent(startCreateTime, 3, 0);

		// 截止联系选中框
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
		finishCreateTime.setWidth("156px");
		finishCreateTime.setValue(dates[1]);
		finishCreateTime.setDateFormat("yyyy-MM-dd HH:mm:ss");
		finishCreateTime.setParseErrorMessage("时间格式不合法");
		finishCreateTime.setResolution(PopupDateField.RESOLUTION_SEC);
		finishCreateTime.addListener(finishCreateTimeListener);
		gridLayout.addComponent(finishCreateTime, 5, 0);
	}
	
	private void createFilterHLayout2() {
		// 客户姓名输入区
		Label customerNameLabel = new Label("客户姓名：");
		customerNameLabel.setWidth("-1px");
		gridLayout.addComponent(customerNameLabel, 0, 1);

		customerName_tf = new TextField();
		customerName_tf.setWidth("115px");
		gridLayout.addComponent(customerName_tf, 1, 1);

		// 电话号码号输入区
		Label customerPhoneLabel = new Label("电话号码：");
		customerPhoneLabel.setWidth("-1px");
		gridLayout.addComponent(customerPhoneLabel, 2, 1);
		
		customerPhone_rf = new TextField();
		customerPhone_rf.setWidth("156px");
		gridLayout.addComponent(customerPhone_rf, 3, 1);
		
	}

	private void createOperateUI() {
		searchButton = new Button("查 询", (ClickListener) this);
		searchButton.setStyleName("default");
		gridLayout.addComponent(searchButton, 6, 0);
		gridLayout.setComponentAlignment(searchButton, Alignment.MIDDLE_LEFT);
		
		clearButton = new Button("清 空", (ClickListener) this);
		gridLayout.addComponent(clearButton, 6, 1);
		gridLayout.setComponentAlignment(clearButton, Alignment.MIDDLE_LEFT);
		
		recordComplexFilter = new HistoryOrderComplexFilter();
		recordComplexFilter.setHeight("-1px");
		
		complexSearchView = new PopupView(recordComplexFilter);
		complexSearchView.setHideOnMouseOut(false);
		gridLayout.addComponent(complexSearchView, 5, 1);
		gridLayout.setComponentAlignment(complexSearchView, Alignment.MIDDLE_RIGHT);
	}
	
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if(source == searchButton) {
			String countSql = createCountSql();
			String searchSql = countSql.replaceFirst("count\\(e\\)", "e") + " order by e.generateDate desc";
			tableFlipOver.setSearchSql(searchSql);
			tableFlipOver.setCountSql(countSql);
			tableFlipOver.refreshToFirstPage();
			
			// 在查询到结果后，默认选择 Table 的第一条记录
			BeanItemContainer<Order> taskBeanItemContainer = tableFlipOver.getEntityContainer();
			if(taskBeanItemContainer.size() > 0) {
				Object firstItemId = taskBeanItemContainer.getIdByIndex(0);
				myOrderTable.setValue(taskBeanItemContainer.getItem(firstItemId).getBean());
			} else {
				myOrderTable.setValue(null);
			}
		} else if(source == clearButton) {
			createTimeScope.setValue("今天");
			customerName_tf.setValue("");
			customerPhone_rf.setValue("");
		}
	}

	private String  createCountSql() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
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
		
		String customerPhoneSql = "";
		String phoneNumber = customerPhone_rf.getValue().toString().trim();
		if(!"".equals(phoneNumber) && phoneNumber != null) {
			customerPhoneSql = " and e.customerResource in (select p.customerResource from Telephone as p where p.number like '%" + phoneNumber + "%') ";
		}
		
		String sql = specificStartTimeSql + specificFinishTimeSql + customerNameSql + customerPhoneSql;
		return "select count(e) from Order as e where e.csrUserId = "+loginUser.getId() + sql;
	}
	
	public void setTableFlipOver(FlipOverTableComponent<Order> tableFlipOver) {
		this.tableFlipOver = tableFlipOver;
		this.myOrderTable = tableFlipOver.getTable();
		recordComplexFilter.setTableFlipOver(tableFlipOver);
	}
	
}
