package com.jiangyifen.ec2.ui.mgr.system.tabsheet;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.entity.CustomerServiceRecordStatus;
import com.jiangyifen.ec2.entity.CustomerServiceRecordStatusNavigationKey;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.service.eaoservice.CustomerServiceRecordStatusNavigationKeyService;
import com.jiangyifen.ec2.service.eaoservice.CustomerServiceRecordStatusService;
import com.jiangyifen.ec2.ui.FlipOverTableComponent;
import com.jiangyifen.ec2.ui.mgr.recordstatus.CustomerServiceRecordStatusEditorWindow;
import com.jiangyifen.ec2.ui.mgr.recordstatus.ServiceRecordStatus2DepartmentWindow;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.event.Action;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.Notification;

/**
 * 客服记录状态管理界面
 * 
 * @author jrh
 *
 */
@SuppressWarnings("serial")
public class CustomerServiceRecordStatusManagement extends VerticalLayout implements ClickListener, ValueChangeListener {

	// 日志工具
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private final Object[] VISIBLE_PROPERTIES = new Object[] {"statusName", "isAnswered", "isMeanVipCustomer", "enabled", "direction"};

	private final String[] COL_HEADERS = new String[] {"结果名称", "是否接通", "是否客户", "可用状态", "呼叫方向"};

	private final Action ADD = new Action("添加");
	private final Action EDIT = new Action("编辑");
	private final Action ENABLE = new Action("启用");
	private final Action UNENABLE = new Action("停用");

	private Notification success_notification;					// 成功过提示信息
	private Notification warning_notification;					// 错误警告提示信息

	// 过滤组件
	private TextField statusName_tf;							// 按客服记录状态名称进行搜索
	private ComboBox isAnswered_cb;								// 按是否接通进行搜索
	private ComboBox isVip_cb;									// 按意味着是客户进行搜索
	private ComboBox isEnabled_cb;								// 按使用状态进行搜索
	private ComboBox direction_cb;								// 按呼叫方向进行搜索

	private Button search_bt; 									// 搜索按钮
	private Button clear_bt; 									// 清空按钮

	private Table recordStatus_table;							// 客服记录状态显示区域
	private Button viewAndEditStatus2Dept_bt;					// 查看并编辑部门与客服记录状态之间的关系
	private Button add_bt; 										// 查看按钮
	private Button edit_bt;										// 编辑并重发
	private Button enable_bt; 									// 启用按钮
	private Button unenable_bt; 								// 停用按钮

	private FlipOverTableComponent<CustomerServiceRecordStatus> recordStatusItemFlip; 	// 翻页组件
	
	private CustomerServiceRecordStatusEditorWindow recordStatusWindow;						// 客服记录状态添加、编辑窗口
	private ServiceRecordStatus2DepartmentWindow status2DeptWindow;						// 按部门编辑弹屏显示信息窗口

	private Domain domain;																// 当前登录用户所属域
	
	private CustomerServiceRecordStatusService customerServiceRecordStatusService;		// 客服记录状态服务类
	private CustomerServiceRecordStatusNavigationKeyService customerServiceRecordStatusNavigationKeyService;	// 客服记录状态导航键服务类
	
	public CustomerServiceRecordStatusManagement() {
		this.setWidth("100%");
		this.setSpacing(true);
		this.setMargin(true);

		domain = SpringContextHolder.getDomain();

		customerServiceRecordStatusService = SpringContextHolder.getBean("customerServiceRecordStatusService");
		customerServiceRecordStatusNavigationKeyService = SpringContextHolder.getBean("customerServiceRecordStatusNavigationKeyService");
		
		success_notification = new Notification("", Notification.TYPE_HUMANIZED_MESSAGE);
		success_notification.setDelayMsec(1000);
		success_notification.setHtmlContentAllowed(true);
		
		warning_notification = new Notification("", Notification.TYPE_WARNING_MESSAGE);
		warning_notification.setDelayMsec(1000);
		warning_notification.setHtmlContentAllowed(true);
		
		// 创建过滤器组件
		createFilterComponents();
		
		// 创建客服记录状态显示表格
		createRecordStatusTable();
		
		// 创建客服记录状态表格的翻页组件
		createTableFlipComponent();
		
		ComboBox c = new ComboBox();
		c.setFilteringMode(ComboBox.FILTERINGMODE_CONTAINS);
	}

	/**
	 *  创建过滤器组件
	 */
	private void createFilterComponents() {
		HorizontalLayout filter_hl = new HorizontalLayout();
		filter_hl.setSpacing(true);
		this.addComponent(filter_hl);

		Label statusNameLabel = new Label("结果名称：");
		statusNameLabel.setWidth("-1px");
		filter_hl.addComponent(statusNameLabel);
		filter_hl.setComponentAlignment(statusNameLabel, Alignment.MIDDLE_LEFT);
		
		statusName_tf = new TextField();
		statusName_tf.setWidth("120px");
		statusName_tf.setImmediate(true);
		filter_hl.addComponent(statusName_tf);
		filter_hl.setComponentAlignment(statusName_tf, Alignment.MIDDLE_LEFT);
		
		Label isAnsweredLabel = new Label("是否接通：");
		isAnsweredLabel.setWidth("-1px");
		filter_hl.addComponent(isAnsweredLabel);
		filter_hl.setComponentAlignment(isAnsweredLabel, Alignment.MIDDLE_LEFT);
		
		isAnswered_cb = new ComboBox();
		isAnswered_cb.setWidth("100px");
		isAnswered_cb.setImmediate(true);
		isAnswered_cb.addItem("all");
		isAnswered_cb.addItem("brigded");
		isAnswered_cb.addItem("unbrigded");
		isAnswered_cb.setItemCaption("all", "全部");
		isAnswered_cb.setItemCaption("brigded", "已接通");
		isAnswered_cb.setItemCaption("unbrigded", "未接通");
		isAnswered_cb.setValue("all");
		isAnswered_cb.setNullSelectionAllowed(false);
		filter_hl.addComponent(isAnswered_cb);
		filter_hl.setComponentAlignment(isAnswered_cb, Alignment.MIDDLE_LEFT);

		Label isVipLabel = new Label("是否客户：");
		isVipLabel.setWidth("-1px");
		filter_hl.addComponent(isVipLabel);
		filter_hl.setComponentAlignment(isVipLabel, Alignment.MIDDLE_LEFT);
		
		isVip_cb = new ComboBox();
		isVip_cb.setWidth("100px");
		isVip_cb.setImmediate(true);
		isVip_cb.addItem("all");
		isVip_cb.addItem("yes");
		isVip_cb.addItem("no");
		isVip_cb.setItemCaption("all", "全部");
		isVip_cb.setItemCaption("yes", "是");
		isVip_cb.setItemCaption("no", "不是");
		isVip_cb.setValue("all");
		isVip_cb.setNullSelectionAllowed(false);
		filter_hl.addComponent(isVip_cb);
		filter_hl.setComponentAlignment(isVip_cb, Alignment.MIDDLE_LEFT);
		
		Label isEnableLabel = new Label("可用状态：");
		isEnableLabel.setWidth("-1px");
		filter_hl.addComponent(isEnableLabel);
		filter_hl.setComponentAlignment(isEnableLabel, Alignment.MIDDLE_LEFT);
		
		isEnabled_cb = new ComboBox();
		isEnabled_cb.setWidth("100px");
		isEnabled_cb.setImmediate(true);
		isEnabled_cb.addItem("all");
		isEnabled_cb.addItem("enable");
		isEnabled_cb.addItem("unenable");
		isEnabled_cb.setItemCaption("all", "全部");
		isEnabled_cb.setItemCaption("enable", "可用");
		isEnabled_cb.setItemCaption("unenable", "不可用");
		isEnabled_cb.setValue("enable");
		isEnabled_cb.setNullSelectionAllowed(false);
		filter_hl.addComponent(isEnabled_cb);
		filter_hl.setComponentAlignment(isEnabled_cb, Alignment.MIDDLE_LEFT);
		
		Label directionLabel = new Label("呼叫方向：");
		directionLabel.setWidth("-1px");
		filter_hl.addComponent(directionLabel);
		filter_hl.setComponentAlignment(directionLabel, Alignment.MIDDLE_LEFT);
		
		direction_cb = new ComboBox();
		direction_cb.setWidth("100px");
		direction_cb.setImmediate(true);
		direction_cb.addItem("all");
		direction_cb.addItem("outgoing");
		direction_cb.addItem("incoming");
		direction_cb.setItemCaption("all", "全部");
		direction_cb.setItemCaption("outgoing", "呼出");
		direction_cb.setItemCaption("incoming", "呼入");
		direction_cb.setValue("all");
		direction_cb.setNullSelectionAllowed(false);
		filter_hl.addComponent(direction_cb);
		filter_hl.setComponentAlignment(direction_cb, Alignment.MIDDLE_LEFT);

		clear_bt = new Button("清 空", this);
		filter_hl.addComponent(clear_bt);
		
		search_bt = new Button("查 询", this);
		search_bt.setStyleName("default");
		filter_hl.addComponent(search_bt);	
	}

	/**
	 *  创建客服记录状态显示表格
	 */
	private void createRecordStatusTable() {
		recordStatus_table = createFormatColumnTable();
		recordStatus_table.setWidth("100%");
		recordStatus_table.setHeight("-1px");
		recordStatus_table.setImmediate(true);
		recordStatus_table.addListener(this);
		recordStatus_table.setSelectable(true);
		recordStatus_table.setStyleName("striped");
		recordStatus_table.setRowHeaderMode(Table.ROW_HEADER_MODE_INDEX);
		this.addComponent(recordStatus_table);

		// 为表格添加右键单击事件
		addActionToTable(recordStatus_table);
	}

	/**
	 *  创建格式化 了 日期列的 Table对象
	 */
	private Table createFormatColumnTable() {
		return new Table() {
			@Override
            protected String formatPropertyValue(Object rowId, Object colId, Property property) {
				if(property.getValue() == null) { 
					return "";
				} else if("isAnswered".equals(colId)) {
					boolean isAnswered = (Boolean) property.getValue();
					return isAnswered ? "接通" : "未接通";
				} else if("isMeanVipCustomer".equals(colId)) {
					boolean isAnswered = (Boolean) property.getValue();
					return isAnswered ? "是" : "不是";
				} else if("enabled".equals(colId)) {
					boolean isAnswered = (Boolean) property.getValue();
					return isAnswered ? "可用" : "不可用";
				} else if("direction".equals(colId)) {
					boolean isOutgoing = "outgoing".equals((String) property.getValue());
					return isOutgoing ? "呼出" : "呼入";
				}
				return super.formatPropertyValue(rowId, colId, property);
			}
		};
	}
	
	/**
	 * 为指定模板表格 添加右键单击事件
	 */
	private void addActionToTable(final Table table) {
		table.addActionHandler(new Action.Handler() {
			@Override
			public void handleAction(Action action, Object sender, Object target) {
				table.select(target);
				if(action == ADD) {
					add_bt.click();
				} else if(action == EDIT) {
					edit_bt.click();
				} else if(action == ENABLE) {
					enable_bt.click();
				} else if(action == UNENABLE) {
					unenable_bt.click();
				}
			}
			@Override
			public Action[] getActions(Object target, Object sender) {
				if(target == null) {
					return new Action[] {ADD};
				}
				
				CustomerServiceRecordStatus status = (CustomerServiceRecordStatus) target;
				if(status.getEnabled()) {
					return new Action[] {ADD, EDIT, UNENABLE};
				} 
				return new Action[] {ADD, EDIT, ENABLE};
			}
		});
	}
	
	/**
	 *  创建客服记录状态表格的翻页组件
	 */
	private void createTableFlipComponent() {
		HorizontalLayout bottom_hl = new HorizontalLayout();
		bottom_hl.setWidth("100%");
		bottom_hl.setSpacing(true);
		this.addComponent(bottom_hl);
		
		// 操作按钮组件
		HorizontalLayout operator_hl = new HorizontalLayout();
		operator_hl.setSpacing(true);
		bottom_hl.addComponent(operator_hl);
		
		viewAndEditStatus2Dept_bt = new Button("按部门编辑弹屏显示信息", this);
		viewAndEditStatus2Dept_bt.setStyleName("default");
		operator_hl.addComponent(viewAndEditStatus2Dept_bt);
		
		add_bt = new Button("添加", this);
		operator_hl.addComponent(add_bt);
		
		edit_bt = new Button("编辑", this);
		edit_bt.setEnabled(false);
		operator_hl.addComponent(edit_bt);
		
		enable_bt = new Button("启用", this);
		enable_bt.setEnabled(false);
		operator_hl.addComponent(enable_bt);
		
		unenable_bt = new Button("停用", this);
		unenable_bt.setEnabled(false);
		operator_hl.addComponent(unenable_bt);
		
		String countSql = "select count(csrs) from CustomerServiceRecordStatus as csrs where csrs.domain.id = " +domain.getId()+ " and csrs.enabled = true";
		String searchSql = countSql.replaceFirst("count\\(csrs\\)", "csrs") + " order by csrs.id desc";
		
		recordStatusItemFlip = new FlipOverTableComponent<CustomerServiceRecordStatus>(CustomerServiceRecordStatus.class, 
				customerServiceRecordStatusService, recordStatus_table, searchSql , countSql, null);
		
		recordStatusItemFlip.setSearchSql(searchSql);
		recordStatusItemFlip.setCountSql(countSql);
		recordStatusItemFlip.setPageLength(16, false);
		
		recordStatus_table.setVisibleColumns(VISIBLE_PROPERTIES);
		recordStatus_table.setColumnHeaders(COL_HEADERS);
		recordStatus_table.setPageLength(16);
		
		bottom_hl.addComponent(recordStatusItemFlip);
		bottom_hl.setComponentAlignment(recordStatusItemFlip, Alignment.TOP_RIGHT);
	}

	@Override
	public void valueChange(ValueChangeEvent event) {
		Property source = event.getProperty();
		if(source == recordStatus_table) {
			CustomerServiceRecordStatus recordStatus = (CustomerServiceRecordStatus) recordStatus_table.getValue();
			edit_bt.setEnabled(recordStatus != null);
			enable_bt.setEnabled(recordStatus != null);
			enable_bt.setVisible(recordStatus != null && !recordStatus.getEnabled());
			unenable_bt.setEnabled(recordStatus != null);
			unenable_bt.setVisible(recordStatus != null && recordStatus.getEnabled());
		} 
	}

	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if(source == search_bt) {
			try {
				executeSearch();
			} catch (Exception e) {
				e.printStackTrace();
				logger.error("jrh 查询客服记录状态出现异常-->"+e.getMessage(), e);
			}
		} else if(source == viewAndEditStatus2Dept_bt) {
			showRecordStatus2DepartmentWindow();
		} else if(source == add_bt) {
			showBlacklistItemWindow(true);
		} else if(source == edit_bt) {
			showBlacklistItemWindow(false);
		} else if(source == enable_bt) {
			executeEditEnabledStatus(true);
		} else if(source == unenable_bt) {
			executeEditEnabledStatus(false);
		} else if(source == clear_bt) {
			statusName_tf.setValue("");
			isAnswered_cb.setValue("all");
			isVip_cb.setValue("all");
			isEnabled_cb.setValue("all");
			direction_cb.setValue("all");
		}
	}
	
	/**
	 * 显示"按部门编辑弹屏显示信息"窗口
	 */
	private void showRecordStatus2DepartmentWindow() {
		if(status2DeptWindow == null) {
			status2DeptWindow = new ServiceRecordStatus2DepartmentWindow();
		}
		this.getApplication().getMainWindow().removeWindow(status2DeptWindow);
		status2DeptWindow.updateTable();
		this.getApplication().getMainWindow().addWindow(status2DeptWindow);
	}

	/**
	 * 执行搜索
	 */
	private void executeSearch() {
		StringBuffer dynamicCountSql = new StringBuffer();
		dynamicCountSql.append("select count(csrs) from CustomerServiceRecordStatus as csrs where csrs.domain.id = ");
		dynamicCountSql.append(domain.getId().toString());

		String statusName = StringUtils.trimToEmpty((String) statusName_tf.getValue());
		if(!"".equals(statusName)) {
			dynamicCountSql.append(" and csrs.statusName like '%");
			dynamicCountSql.append(statusName);
			dynamicCountSql.append("%'");
		}
	
		String isAnsweredStr = (String) isAnswered_cb.getValue();
		if(!"all".equals(isAnsweredStr)) {
			boolean isAnswered = isAnsweredStr.equals("brigded");
			dynamicCountSql.append(" and csrs.isAnswered = ");
			dynamicCountSql.append(isAnswered);
		}
		
		String isVipStr = (String) isVip_cb.getValue();
		if(!"all".equals(isVipStr)) {
			boolean isVip = isVipStr.equals("yes");
			System.out.println(isVip);
			dynamicCountSql.append(" and csrs.isMeanVipCustomer = ");
			dynamicCountSql.append(isVip);
		}
		
		String enabledStr = (String) isEnabled_cb.getValue();
		if(!"all".equals(enabledStr)) {
			boolean isEnabled = enabledStr.equals("enable");
			dynamicCountSql.append(" and csrs.enabled = ");
			dynamicCountSql.append(isEnabled);
		}
		
		String directionStr = (String) direction_cb.getValue();
		if(!"all".equals(directionStr)) {
			dynamicCountSql.append(" and csrs.direction = '");
			dynamicCountSql.append(directionStr);
			dynamicCountSql.append("'");
		}
		
		String countSql = dynamicCountSql.toString();
		String searchSql = countSql.replaceFirst("count\\(csrs\\)", "csrs") + " order by csrs.id desc";
		
		recordStatusItemFlip.setSearchSql(searchSql);
		recordStatusItemFlip.setCountSql(countSql);
		recordStatusItemFlip.refreshToFirstPage();
	}
	
	/**
	 * 显示添加或编辑客服记录状态信息
	 * @param isAddNewStatus	是否为添加新状态
	 */
	private void showBlacklistItemWindow(boolean isAddNewStatus) {
		if(recordStatusWindow == null) {
			recordStatusWindow = new CustomerServiceRecordStatusEditorWindow(this);
		}
		this.getApplication().getMainWindow().removeWindow(recordStatusWindow);
		CustomerServiceRecordStatus recordStatus = (CustomerServiceRecordStatus) recordStatus_table.getValue();
		recordStatusWindow.setCaption("编辑联系结果");
		if(isAddNewStatus) {
			recordStatus = new CustomerServiceRecordStatus();
			recordStatus.setDomain(domain);
			recordStatus.setDirection("outgoing");
			recordStatusWindow.setCaption("添加联系结果");
		}
		
		recordStatusWindow.updateFormDataSource(recordStatus, isAddNewStatus);
		this.getApplication().getMainWindow().addWindow(recordStatusWindow);
	}

	/**
	 * 将客服记录状态置为可用或不可用
	 * @param enabled 是否可用
	 */
	private void executeEditEnabledStatus(boolean enabled) {
		CustomerServiceRecordStatus recordStatus = (CustomerServiceRecordStatus) recordStatus_table.getValue();
		recordStatus.setEnabled(enabled);
		customerServiceRecordStatusService.update(recordStatus);
		recordStatusItemFlip.refreshInCurrentPage();
		
		if(enabled==false){			// TODO 当禁用客服记录状态时，同时禁用客服记录状态导航键
			List<CustomerServiceRecordStatusNavigationKey> navigationKeyList = customerServiceRecordStatusNavigationKeyService.getAllByServiceRecordState(recordStatus, domain);
			if(navigationKeyList != null && navigationKeyList.size() > 0){
				for(int i = 0; i < navigationKeyList.size(); i++){
					CustomerServiceRecordStatusNavigationKey navigationKey = navigationKeyList.get(i);
					navigationKey.setEnabled(false);
					customerServiceRecordStatusNavigationKeyService.update(navigationKey);
				}
			}
		}
		
		if(isEnabled_cb != null && !isEnabled_cb.getValue().equals("all")){
			unenable_bt.setVisible(false);
			enable_bt.setVisible(false);
			edit_bt.setEnabled(false);
		}
		
		String operating = enabled ? "启用" : "停用";
		success_notification.setCaption(operating +" 操作成功!");
		this.getApplication().getMainWindow().showNotification(success_notification);
	}

	/**
	 * 刷新客服记录状态显示界面，当Tab 页切换的时候调用
	 */
	public void refreshTable(boolean refreshToFirstPage) {
		if(refreshToFirstPage == true) {
			recordStatusItemFlip.refreshToFirstPage();
		} else {
			recordStatusItemFlip.refreshInCurrentPage();
		}
	}
	
}
