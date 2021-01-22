package com.jiangyifen.ec2.ui.mgr.recordstatusnavigationkey;

import java.util.List;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.entity.CustomerServiceRecordStatus;
import com.jiangyifen.ec2.entity.CustomerServiceRecordStatusNavigationKey;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.service.eaoservice.CustomerServiceRecordStatusNavigationKeyService;
import com.jiangyifen.ec2.service.eaoservice.CustomerServiceRecordStatusService;
import com.jiangyifen.ec2.ui.mgr.system.tabsheet.CustomerServiceRecordStatusNavigationKeyManagement;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Form;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * 客服记录状态按键管理配置修改和添加窗口界面
 * @author JHT
 * @date 2014-11-19 下午1:52:24
 */
public class RecordStatusNavigationKeyEditorWindow extends Window implements Button.ClickListener, Property.ValueChangeListener {
	
	private static final long serialVersionUID = -2401271076774203381L;
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private Form form;								// 表单
	private ComboBox cmbDirection;					// 呼叫方向
	private ComboBox cmbStatusName;					// 状态名称
	private ComboBox cmbInputKey;					// 状态按键
	private OptionGroup ogIsEnabled;				// 是否可用
	private Button btnSave;
	private Button btnCancel;
	
	private boolean isAddNewNavigationKey;
	private CustomerServiceRecordStatusNavigationKeyManagement recordStatusNavigationKeyManagement;
	private CustomerServiceRecordStatusNavigationKeyService customerServiceRecordStatusNavigationKeyService;
	private CustomerServiceRecordStatusService customerServiceRecordStatusService;
	private CustomerServiceRecordStatusNavigationKey navigationKey;
	private List<CustomerServiceRecordStatus> recordStatusList;
	private BeanItemContainer<CustomerServiceRecordStatus> recordContainer = new BeanItemContainer<CustomerServiceRecordStatus>(CustomerServiceRecordStatus.class);
	private Domain domain;
	
	// 构造方法
	public RecordStatusNavigationKeyEditorWindow(CustomerServiceRecordStatusNavigationKeyManagement recordStatusNavigationKeyManagement){
		this.center();
		this.setResizable(false);
		this.setModal(true);
		this.recordStatusNavigationKeyManagement = recordStatusNavigationKeyManagement;
		
		domain = SpringContextHolder.getDomain();
		customerServiceRecordStatusNavigationKeyService = SpringContextHolder.getBean("customerServiceRecordStatusNavigationKeyService");
		customerServiceRecordStatusService = SpringContextHolder.getBean("customerServiceRecordStatusService");
		
		VerticalLayout windowContent = new VerticalLayout();
		windowContent.setSizeUndefined();
		windowContent.setMargin(false, true, true, true);
		windowContent.setSpacing(true);
		this.setContent(windowContent);
		
		// 创建表单中的主要组件
		createFieldComponents();
		
		// 创建Form 表单, 并放入windowContent 
		createFormComponent(windowContent);
	}
	
	// 创建表单中的主要组件
	private void createFieldComponents(){
		cmbDirection = new ComboBox("呼叫方向：");
		cmbDirection.setRequired(true);
		cmbDirection.addItem("all");
		cmbDirection.addItem("outgoing");
		cmbDirection.addItem("incoming");
		cmbDirection.setItemCaption("all", "全部");
		cmbDirection.setItemCaption("outgoing", "呼出");
		cmbDirection.setItemCaption("incoming", "呼入");
		cmbDirection.setNullSelectionAllowed(false);
		cmbDirection.setWidth("200px");
		
		cmbStatusName = new ComboBox("结果名称：");
		cmbStatusName.setRequired(true);
		cmbStatusName.setItemCaptionPropertyId("migrateRecordStatus");
		cmbStatusName.setNullSelectionAllowed(false);
		cmbStatusName.setInputPrompt("结果名称-呼叫方向-可用状态");
		cmbStatusName.setWidth("200px");
		
		cmbInputKey = new ComboBox("状态按键：");
		cmbInputKey.setRequired(true);
		cmbInputKey.setNullSelectionAllowed(false);
		cmbInputKey.addItem("0");
		cmbInputKey.addItem("1");
		cmbInputKey.addItem("2");
		cmbInputKey.addItem("3");
		cmbInputKey.addItem("4");
		cmbInputKey.addItem("5");
		cmbInputKey.addItem("6");
		cmbInputKey.addItem("7");
		cmbInputKey.addItem("8");
		cmbInputKey.addItem("9");
		cmbInputKey.setInputPrompt("导航按键");
		cmbInputKey.setWidth("200px");
		
		ogIsEnabled = new OptionGroup("是否可用：");
		ogIsEnabled.setRequired(true);
		ogIsEnabled.addItem(true);
		ogIsEnabled.addItem(false);
		ogIsEnabled.setItemCaption(true, "是");
		ogIsEnabled.setItemCaption(false, "否");
		ogIsEnabled.setValue(true);
		ogIsEnabled.setNullSelectionAllowed(false);
		ogIsEnabled.addStyleName("twocol100");
		ogIsEnabled.addStyleName("myopacity");
		ogIsEnabled.setImmediate(true);
	}
	
	// 创建Form 表单, 并放入windowContent 
	private void createFormComponent(VerticalLayout windowContent){
		form = new Form();
		form.addField("direction", cmbDirection);
		form.addField("statusName", cmbStatusName);
		form.addField("inputKey", cmbInputKey);
		form.addField("enabled", ogIsEnabled);
		form.setValidationVisibleOnCommit(true);
		form.setValidationVisible(false);
		form.setInvalidCommitted(false);
		form.setWriteThrough(false);
		form.setImmediate(true);
		form.setFooter(createFormFooterComponents());
		windowContent.addComponent(form);
	}
	
	// 创建操作按钮
	private HorizontalLayout createFormFooterComponents(){
		HorizontalLayout operatorLayout = new HorizontalLayout();
		operatorLayout.setSpacing(true);
		operatorLayout.setWidth("100%");
		
		btnSave = new Button("保 存", this);
		btnSave.setStyleName("default");
		operatorLayout.addComponent(btnSave);
		
		btnCancel = new Button("取 消", this);
		operatorLayout.addComponent(btnCancel);
		
		return operatorLayout;
	}
	
	// 按钮点击事件
	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if(btnSave == source){
			executeSave();
		} else if(btnCancel == source){
			form.discard();
			this.getApplication().getMainWindow().removeWindow(this);
		}
	}
	
	// 执行保存操作
	private void executeSave(){
		CustomerServiceRecordStatus recordStatus = (CustomerServiceRecordStatus) cmbStatusName.getValue();
		if(recordStatus == null){
			showNotificationMessage("请选择一个客服记录状态", Notification.TYPE_WARNING_MESSAGE);
			return;
		}
		
		String inputKey = (String) cmbInputKey.getValue();
		if(inputKey == null || inputKey.equals("")){
			showNotificationMessage("导航按键不能为空！", Notification.TYPE_WARNING_MESSAGE);
			return;
		}
		
		/**
		 * 同一方向，同一状态，只能配置一次
		 * 同一方向，同一按键，只能配置一次
		 */
		
		if(customerServiceRecordStatusNavigationKeyService.existByRecordStatus(recordStatus.getId(), navigationKey.getDomain())){
			if(navigationKey.getServiceRecordStatus() != null && navigationKey.getServiceRecordStatus().getId() != null && navigationKey.getServiceRecordStatus().getId().equals(recordStatus.getId())){
				// 这里就说明实在编辑的时候，原来设置的导航键和现在设置的导航键如果相同，那么允许修改，否则，就说明导航键已经存在
			} else {
				showNotificationMessage("客服记录状态结果在状态按键配置里面已经存在！", Notification.TYPE_WARNING_MESSAGE);
				return;
			}
		}
		
		if(customerServiceRecordStatusNavigationKeyService.existByInputKey(recordStatus.getDirection(), inputKey, navigationKey.getDomain())){
			// 如果这个按键配置存在，就看呼叫方向是否相同
			if(navigationKey.getServiceRecordStatus() != null && navigationKey.getServiceRecordStatus().getDirection().equals(recordStatus.getDirection())){	
				// 如果呼叫方向相同，在看这个按键是否和修改前一致
				if(navigationKey.getInputKey() != null && navigationKey.getInputKey().equals(inputKey)){
					// 如果相同，则说明和修改前是一致的，可以点击保存
				} else {
					// 否则，这里是不相同的，所以这里要给予提示不能保存，因为此方向的按键已经存在
					showNotificationMessage("同一呼叫方向的客服记录状态按键已经存在！", Notification.TYPE_WARNING_MESSAGE);
					return;
				}
			}else {
				// 否则，这里是不相同的，所以这里要给予提示不能保存，因为此方向的按键已经存在
				showNotificationMessage("同一呼叫方向的客服记录状态按键已经存在！", Notification.TYPE_WARNING_MESSAGE);
				return;
			}
		}
		
		boolean enabled = ogIsEnabled.getValue() == null ? false : (Boolean) ogIsEnabled.getValue();
		navigationKey.setDomain(domain);
		navigationKey.setEnabled(enabled);
		navigationKey.setInputKey(inputKey);
		navigationKey.setServiceRecordStatus(recordStatus);
		
		
		try {
			if(isAddNewNavigationKey){
				customerServiceRecordStatusNavigationKeyService.save(navigationKey);
			} else {
				customerServiceRecordStatusNavigationKeyService.update(navigationKey);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("JHT 管理员保存客服记录状态导航记录出现异常 ==》 "+e.getMessage(), e);
			showNotificationMessage("保存失败，请检查信息后重试！", Notification.TYPE_WARNING_MESSAGE);
			return;
		}
		
		// 更新客服记录状态导航显示界面信息
		recordStatusNavigationKeyManagement.refreshTable(isAddNewNavigationKey);
		recordStatusNavigationKeyManagement.initializeInputStatus();
		
		showNotificationMessage("保存信息成功！", Notification.TYPE_HUMANIZED_MESSAGE);
		this.getApplication().getMainWindow().removeWindow(this);
	}
	
	// 更新Form 的数据源
	public void updateFormDataSource(CustomerServiceRecordStatusNavigationKey navigationKey, boolean isAddNewNavigationKey){
		this.navigationKey = navigationKey;
		this.isAddNewNavigationKey = isAddNewNavigationKey;
		
		cmbStatusName.addListener(this);
		cmbDirection.addListener(this);
		cmbDirection.setValue("all");
		
		if(navigationKey != null){
			if(isAddNewNavigationKey==false){	// 表示是编辑操作，回显客服记录状态
				for(int i = 0; i < recordStatusList.size(); i++){
					if(recordStatusList.get(i).getId() != null && recordStatusList.get(i).getId().equals(navigationKey.getServiceRecordStatus().getId())){
						cmbStatusName.setValue(recordStatusList.get(i));
						break;
					}
				}
				cmbInputKey.setValue(navigationKey.getInputKey());
				ogIsEnabled.setValue(navigationKey.getEnabled());
			} else {
				cmbDirection.setValue("all");
				cmbInputKey.setValue(null);
				cmbStatusName.setValue(null);
				ogIsEnabled.setValue(true);
			}
		}
	}

	@Override
	public void valueChange(ValueChangeEvent event) {
		Property source = event.getProperty();
		if(cmbStatusName == source){
			CustomerServiceRecordStatus recordStatus = (CustomerServiceRecordStatus) cmbStatusName.getValue();
			if(recordStatus != null && recordStatus.getEnabled() == false){
				ogIsEnabled.setValue(false);
				ogIsEnabled.setReadOnly(true);
			} else {
				ogIsEnabled.setReadOnly(false);
			}
		} else if(cmbDirection == source){
			if(cmbDirection.getValue() != null){
				if(cmbDirection.getValue().equals("all")){	// 全部
					recordStatusList = customerServiceRecordStatusService.getAll(domain);
				} else {
					recordStatusList = customerServiceRecordStatusService.getByDirection(domain, (String)cmbDirection.getValue());
				}
				recordContainer.removeAllItems();
				recordContainer.addAll(recordStatusList);
				cmbStatusName.setContainerDataSource(recordContainer);
				cmbStatusName.setValue(null);
			}
		}
	}

	// 用来设置显示弹出的提示信息
	private void showNotificationMessage(String msg, int type){
		this.getApplication().getMainWindow().showNotification(msg, type);
	}

}
