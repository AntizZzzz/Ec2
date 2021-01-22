package com.jiangyifen.ec2.ui.mgr.system.tabsheet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.entity.CustomerServiceRecordStatusNavigationKey;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.service.eaoservice.CustomerServiceRecordStatusNavigationKeyService;
import com.jiangyifen.ec2.ui.FlipOverTableComponent;
import com.jiangyifen.ec2.ui.mgr.recordstatusnavigationkey.RecordStatusNavigationKeyEditorWindow;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.event.Action;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.Notification;

/**
 * 客服记录状态按键管理界面
 * @author JHT
 * @date 2014-11-18 上午11:29:40
 */
public class CustomerServiceRecordStatusNavigationKeyManagement extends VerticalLayout implements Button.ClickListener, Property.ValueChangeListener{

	private static final long serialVersionUID = -8031959145518364083L;
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private Object[] VISIBLE_PROPERTIES = new Object[]{"serviceRecordStatus.statusName", "inputKey", "enabled", "serviceRecordStatus.direction"};
	private final String[] COL_HEADERS = new String[]{"结果名称", "按键", "可用状态", "呼叫方向"};
	
	private final Action ADD = new Action("添加");
	private final Action EDIT = new Action("编辑");
	private final Action ENABLE = new Action("启用");
	private final Action UNENABLE = new Action("停用");
	
	private TextField tfStatusName;				// 结果名称额
	private ComboBox cmbEnabled;				// 可用状态
	private ComboBox cmbDirection;				// 呼叫方向
	
	private Button btnSearch;					// 搜索按钮
	private Button btnClear;					// 清空按钮
	
	private Table table;						// 存储显示信心的Table
	private Button btnAdd;						// 添加
	private Button btnEdit;						// 编辑
	private Button btnEnable;					// 启用
	private Button btnUnenable;					// 禁用
	
	private RecordStatusNavigationKeyEditorWindow recordStatusNavigationKeyEditorWindow;	// 添加修改导航界面窗口
	private FlipOverTableComponent<CustomerServiceRecordStatusNavigationKey> flip;		// 翻页组件
	private String jpqlCount;
	private String jpqlSearch;
	private CustomerServiceRecordStatusNavigationKeyService customerServiceRecordStatusNavigationKeyService;
	private Domain domain;
	
	// 构造方法
	public CustomerServiceRecordStatusNavigationKeyManagement(){
		this.setWidth("100%");
		this.setSpacing(true);
		this.setMargin(true);
		
		domain = SpringContextHolder.getDomain();
		customerServiceRecordStatusNavigationKeyService = SpringContextHolder.getBean("customerServiceRecordStatusNavigationKeyService");
		
		createSearchComponents();
		createTableComponent();
		createButtonsAndFlip();
	}
	
	// 创建搜索组件
	private void createSearchComponents(){
		HorizontalLayout searchLayout = new HorizontalLayout();
		searchLayout.setSpacing(true);
		this.addComponent(searchLayout);
		
		Label lblStatusName = new Label("结果名称：");
		lblStatusName.setWidth("-1px");
		searchLayout.addComponent(lblStatusName);
		searchLayout.setComponentAlignment(lblStatusName, Alignment.MIDDLE_LEFT);
		tfStatusName = new TextField();
		tfStatusName.setWidth("100px");
		tfStatusName.setImmediate(true);
		tfStatusName.setInputPrompt("结果名称");
		searchLayout.addComponent(tfStatusName);
		searchLayout.setComponentAlignment(tfStatusName, Alignment.MIDDLE_LEFT);
		
		Label lblEnabled = new Label("是否可用：");
		lblEnabled.setWidth("-1px");
		searchLayout.addComponent(lblEnabled);
		searchLayout.setComponentAlignment(lblEnabled, Alignment.MIDDLE_LEFT);
		cmbEnabled = new ComboBox();
		cmbEnabled.setWidth("100px");
		cmbEnabled.setImmediate(true);
		cmbEnabled.addItem("all");
		cmbEnabled.addItem("enable");
		cmbEnabled.addItem("unenable");
		cmbEnabled.setItemCaption("all", "全部");
		cmbEnabled.setItemCaption("enable", "可用");
		cmbEnabled.setItemCaption("unenable", "不可用");
		cmbEnabled.setNullSelectionAllowed(false);
		cmbEnabled.setValue("enable");
		searchLayout.addComponent(cmbEnabled);
		searchLayout.setComponentAlignment(cmbEnabled, Alignment.MIDDLE_LEFT);
		
		Label lblDirection = new Label("呼叫方向：");
		lblDirection.setWidth("-1px");
		searchLayout.addComponent(lblDirection);
		searchLayout.setComponentAlignment(lblDirection, Alignment.MIDDLE_LEFT);
		cmbDirection = new ComboBox();
		cmbDirection.setWidth("100px");
		cmbDirection.setImmediate(true);
		cmbDirection.addItem("all");
		cmbDirection.addItem("outgoing");
		cmbDirection.addItem("incoming");
		cmbDirection.setItemCaption("all", "全部");
		cmbDirection.setItemCaption("outgoing", "呼出");
		cmbDirection.setItemCaption("incoming", "呼入");
		cmbDirection.setValue("all");
		cmbDirection.setNullSelectionAllowed(false);
		searchLayout.addComponent(cmbDirection);
		searchLayout.setComponentAlignment(cmbDirection, Alignment.MIDDLE_LEFT);
		
		btnClear = new Button("清 空", this);
		searchLayout.addComponent(btnClear);
		
		btnSearch = new Button("查 询", this);
		btnSearch.setStyleName("default");
		searchLayout.addComponent(btnSearch);
		
	}
	
	// 创建表格显示组件
	private void createTableComponent(){
		table = new Table(){
			private static final long serialVersionUID = 302098149036321503L;
			@Override
			protected String formatPropertyValue(Object rowId, Object colId, Property property) {
				Object value = property.getValue();
				if(value == null){
					return "";
				} else if("serviceRecordStatus.direction".equals(colId)){
					boolean isOutgoing = "outgoing".equals((String)property.getValue());
					return isOutgoing ? "呼出" : "呼入";
				} else if("enabled".equals(colId)){	// TODO 这里存在问题
					boolean isEnabled = (Boolean) property.getValue();
					return isEnabled ? "可用" : "不可用";
				}
				return super.formatPropertyValue(rowId, colId, property);
			}
		};
	
		table.setWidth("100%");
		table.setHeight("-1px");
		table.setImmediate(true);
		table.addListener(this);
		table.setSelectable(true);
		table.setStyleName("striped");
		table.setRowHeaderMode(Table.ROW_HEADER_MODE_INDEX);
		this.addComponent(table);
		
		// 为表格添加右键点击事件
		addActionToTable(table);
	}
	
	// 为表格添加右键点击事件
	private void addActionToTable(final Table table){
		table.addActionHandler(new Action.Handler() {
			private static final long serialVersionUID = 1279371219287372747L;
			@Override
			public void handleAction(Action action, Object sender, Object target) {
				table.select(target);
				if(action == ADD){
					btnAdd.click();
				} else if(action == EDIT){
					btnEdit.click();
				} else if(action == ENABLE){
					btnEnable.click();
				} else if(action == UNENABLE){
					btnUnenable.click();
				}
			}
			
			@Override
			public Action[] getActions(Object target, Object sender) {
				if(target == null){
					return new Action[]{ADD};
				}
				
				CustomerServiceRecordStatusNavigationKey navigationKey = (CustomerServiceRecordStatusNavigationKey) target;
				if(navigationKey.getEnabled()){
					return new Action[]{ADD, EDIT, UNENABLE};
				}
			
				return new Action[]{ADD, EDIT, ENABLE};
			}
		});
	}
	
	// 创建表格下方显示按钮组件和分页组件
	private void createButtonsAndFlip(){
		HorizontalLayout footerLayout = new HorizontalLayout();
		footerLayout.setWidth("100%");
		footerLayout.setSpacing(true);
		this.addComponent(footerLayout);
		
		// 操作按钮组件
		HorizontalLayout operatorLayout = new HorizontalLayout();
		operatorLayout.setSpacing(true);
		footerLayout.addComponent(operatorLayout);
		
		btnAdd = new Button("添加", this);
		btnAdd.setWidth("-1px");
		operatorLayout.addComponent(btnAdd);
		btnEdit = new Button("编辑", this);
		btnEdit.setWidth("-1px");
		btnEdit.setEnabled(false);
		operatorLayout.addComponent(btnEdit);
		btnEnable = new Button("启用", this);
		btnEnable.setWidth("-1px");
		btnEnable.setEnabled(false);
		btnEnable.setVisible(false);
		operatorLayout.addComponent(btnEnable);
		btnUnenable = new Button("禁用", this);
		btnUnenable.setWidth("-1px");
		btnUnenable.setEnabled(false);
		btnUnenable.setVisible(false);
		operatorLayout.addComponent(btnUnenable);
		
		initializeJpql();
		
		flip = new FlipOverTableComponent<CustomerServiceRecordStatusNavigationKey>(CustomerServiceRecordStatusNavigationKey.class, customerServiceRecordStatusNavigationKeyService, table, jpqlSearch, jpqlCount, null);
		for(int i = 0; i < VISIBLE_PROPERTIES.length; i++){
			flip.getEntityContainer().addNestedContainerProperty(VISIBLE_PROPERTIES[i].toString());
		}
		flip.setPageLength(16, false);
		flip.refreshToFirstPage();
		table.setVisibleColumns(VISIBLE_PROPERTIES);
		table.setColumnHeaders(COL_HEADERS);
		table.setPageLength(16);
		footerLayout.addComponent(flip);
		footerLayout.setComponentAlignment(flip, Alignment.TOP_RIGHT);
	}
	
	// 初始化JPQL查询语句
	private void initializeJpql(){
		StringBuffer conditionBuffer = new StringBuffer(" where s.domain.id="+domain.getId()+" ");
		String statusName = (String) tfStatusName.getValue();
		String enabled = (String) cmbEnabled.getValue();
		String direction = (String) cmbDirection.getValue();
		if(statusName != null && !statusName.equals("")){
			conditionBuffer.append(" and s.serviceRecordStatus.statusName like '%"+statusName+"%' ");
		}
		if(enabled != null && !enabled.equals("all")){
			if(enabled.equals("enable")){
				conditionBuffer.append(" and s.enabled="+true);
			} else {
				conditionBuffer.append(" and s.enabled="+false);
			}
		}
		if(direction != null && !direction.equals("all")){
			conditionBuffer.append(" and s.serviceRecordStatus.direction='"+direction+"' ");
		}
		
		jpqlCount = "select count(s.id) from CustomerServiceRecordStatusNavigationKey as s "+conditionBuffer.toString();
		jpqlSearch = "select s from CustomerServiceRecordStatusNavigationKey as s "+conditionBuffer.toString();
		
	}
	
	// 刷新表格数据
	public void refreshTable(Boolean isToFirst){
		if(flip != null){
			flip.setSearchSql(jpqlSearch);
			flip.setCountSql(jpqlCount);
			if(isToFirst){
				flip.refreshToFirstPage();
			} else {
				flip.refreshInCurrentPage();
			}
		}
	}
	
	
	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if(btnClear == source){		// 清空按钮
			tfStatusName.setValue("");
			cmbDirection.setValue("all");
			cmbEnabled.setValue("enable");
		} else if(btnSearch == source){		// 查询按钮
			initializeJpql();
			refreshTable(true);
		} else if(btnAdd == source){	// 添加按钮
			showBlacklistItemWindow(true);
		} else if(btnEdit == source){	// 编辑按钮
			showBlacklistItemWindow(false);
		} else if(btnEnable == source){		// 改为可用状态
			updateNavigationKeyStatus(true);
		} else if(btnUnenable == source){	// 改为禁用状态
			updateNavigationKeyStatus(false);
		}
	}
	
	// 显示添加或编辑客服记录状态导航信息
	private void showBlacklistItemWindow(boolean isAddnewStatus){
		if(recordStatusNavigationKeyEditorWindow == null){
			recordStatusNavigationKeyEditorWindow = new RecordStatusNavigationKeyEditorWindow(this);
		}
		this.getApplication().getMainWindow().removeWindow(recordStatusNavigationKeyEditorWindow);
		CustomerServiceRecordStatusNavigationKey navigationKey = (CustomerServiceRecordStatusNavigationKey) table.getValue();
		recordStatusNavigationKeyEditorWindow.setCaption("编辑状态按键");
		if(isAddnewStatus){
			navigationKey = new CustomerServiceRecordStatusNavigationKey();
			navigationKey.setDomain(domain);
			recordStatusNavigationKeyEditorWindow.setCaption("添加状态按键");
		}
		recordStatusNavigationKeyEditorWindow.updateFormDataSource(navigationKey, isAddnewStatus);
		this.getApplication().getMainWindow().addWindow(recordStatusNavigationKeyEditorWindow);
	}
	
	// 修改状态
	private void updateNavigationKeyStatus(boolean enabled){
		
		CustomerServiceRecordStatusNavigationKey serviceRecordStatusNavigationKey = (CustomerServiceRecordStatusNavigationKey) table.getValue();
		if(serviceRecordStatusNavigationKey != null){
			if(serviceRecordStatusNavigationKey.getEnabled()!=enabled){
				if(serviceRecordStatusNavigationKey.getServiceRecordStatus() != null && serviceRecordStatusNavigationKey.getServiceRecordStatus().getEnabled()){
					serviceRecordStatusNavigationKey.setEnabled(enabled);
				} else {	// 如果客服状态不可用，那么这个客服状态导航也不可用
					serviceRecordStatusNavigationKey.setEnabled(false);
				}
				
				try {
					customerServiceRecordStatusNavigationKeyService.update(serviceRecordStatusNavigationKey);
					
					if(serviceRecordStatusNavigationKey.getEnabled()){
						showNotificationMessage("客服记录状态导航启用成功！", Notification.TYPE_HUMANIZED_MESSAGE);
					} else {
						showNotificationMessage("客服记录状态导航禁用成功！", Notification.TYPE_HUMANIZED_MESSAGE);
					}
					
					if(flip != null){
						flip.refreshToFirstPage();
					}
					
					initializeInputStatus();
					
				} catch (Exception e) {
					e.printStackTrace();
					logger.error("JHT 修改客服记录状态导航是否可用出现异常！ --》"+e.getMessage() ,e);
				}
			}
		} else {
			showNotificationMessage("请选中一条记录后在执行此操作", Notification.TYPE_WARNING_MESSAGE);
		}
		
	}

	@Override
	public void valueChange(ValueChangeEvent event) {
		Property source = event.getProperty();
		if(source == table){
			CustomerServiceRecordStatusNavigationKey navigationKey = (CustomerServiceRecordStatusNavigationKey) table.getValue();
			btnEdit.setEnabled(navigationKey != null);
			if(navigationKey != null && navigationKey.getServiceRecordStatus() != null && navigationKey.getServiceRecordStatus().getEnabled() == false){
				btnEnable.setEnabled(false);
				btnEnable.setDescription("请先在客服记录状态管理界面把这个关联的结果名称启用后在执行此操作！");
			} else {
				btnEnable.setEnabled(navigationKey != null);
				btnEnable.setDescription("");
			}
			btnEnable.setVisible(navigationKey != null && !navigationKey.getEnabled());
			btnUnenable.setEnabled(navigationKey != null);
			btnUnenable.setVisible(navigationKey != null && navigationKey.getEnabled());
		}
	}
	
	// 初始化按钮的状态信息
	public void initializeInputStatus(){
		if(cmbEnabled != null && !cmbEnabled.getValue().equals("all")){
//			btnEnable.setEnabled(false);
			btnEnable.setVisible(false);
//			btnUnenable.setEnabled(false);
			btnUnenable.setVisible(false);
			btnEdit.setEnabled(false);
		}
	}
	
	// 用来设置显示弹出的提示信息
	private void showNotificationMessage(String msg, int type){
		this.getApplication().getMainWindow().showNotification(msg, type);
	}
	
}
