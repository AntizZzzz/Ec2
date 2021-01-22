package com.jiangyifen.ec2.ui.mgr.recordstatus;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.entity.CustomerServiceRecordStatus;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.service.eaoservice.CustomerServiceRecordStatusService;
import com.jiangyifen.ec2.ui.mgr.system.tabsheet.CustomerServiceRecordStatusManagement;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.DefaultFieldFactory;
import com.vaadin.ui.Field;
import com.vaadin.ui.Form;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * 客服记录状态添加、编辑窗口
 * 
 * @author jrh
 *
 */
@SuppressWarnings("serial")
public class CustomerServiceRecordStatusEditorWindow extends Window implements ClickListener, ValueChangeListener {

	// 日志工具
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final Object[] VISIBLE_PROPERTIES = new Object[] {"statusName", "isAnswered", "isMeanVipCustomer", "enabled", "direction"};
	
	private Notification success_notification;						// 成功过提示信息
	private Notification warning_notification;						// 错误警告提示信息
	
	private Form recordStatusForm;									// 表单
	private TextField statusName_tf;								// 状态名称
	private OptionGroup isAnswered_og;								// 是否接通
	private OptionGroup isVip_og;									// 是否客户
	private OptionGroup isEnabled_og;								// 可使用状态
	private OptionGroup direction_og;								// 呼叫方向
	
	private Button save_button;										// 保存按钮
	private Button cancel_button;									// 取消按钮

	private CustomerServiceRecordStatusManagement recordStatusManagement;	// 客服记录状态管理界面

	private Domain domain;											// 当前用户所属域
	private CustomerServiceRecordStatus recordStatus;
	private boolean isAddNewRecordStatus;
	private String oldStatusName;
	private String oldDirection;
	
	private CustomerServiceRecordStatusService customerServiceRecordStatusService;		// 客服记录状态服务类
	
	public CustomerServiceRecordStatusEditorWindow(CustomerServiceRecordStatusManagement recordStatusManagement) {
		this.center();
		this.setResizable(false);
		this.recordStatusManagement = recordStatusManagement;

		domain = SpringContextHolder.getDomain();
		customerServiceRecordStatusService = SpringContextHolder.getBean("customerServiceRecordStatusService");

		success_notification = new Notification("", Notification.TYPE_HUMANIZED_MESSAGE);
		success_notification.setDelayMsec(1000);
		success_notification.setHtmlContentAllowed(true);
		
		warning_notification = new Notification("", Notification.TYPE_WARNING_MESSAGE);
		warning_notification.setDelayMsec(1000);
		warning_notification.setHtmlContentAllowed(true);
		
		//添加Window内最大的Layout
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

	/**
	 *  创建表单中的主要组件
	 */
	private void createFieldComponents() {
		statusName_tf = new TextField("结果名称：");
		statusName_tf.setRequired(true);
		statusName_tf.setMaxLength(32);
		statusName_tf.setNullRepresentation("");
		
		isAnswered_og = new OptionGroup();
		isAnswered_og.setRequired(true);
		isAnswered_og.addItem(true);
		isAnswered_og.addItem(false);
		isAnswered_og.setItemCaption(true, "是");
		isAnswered_og.setItemCaption(false, "否");
		isAnswered_og.setNullSelectionAllowed(false);
		isAnswered_og.setStyleName("twocol100");
		isAnswered_og.setCaption("是否接通：");
		isAnswered_og.setImmediate(true);

		isVip_og = new OptionGroup();
		isVip_og.setRequired(true);
		isVip_og.addItem(true);
		isVip_og.addItem(false);
		isVip_og.setItemCaption(true, "是");
		isVip_og.setItemCaption(false, "否");
		isVip_og.setNullSelectionAllowed(false);
		isVip_og.setStyleName("twocol100");
		isVip_og.setCaption("是否客户：");
		isVip_og.setImmediate(true);

		isEnabled_og = new OptionGroup();
		isEnabled_og.setRequired(true);
		isEnabled_og.addItem(true);
		isEnabled_og.addItem(false);
		isEnabled_og.setItemCaption(true, "可用");
		isEnabled_og.setItemCaption(false, "不可用");
		isEnabled_og.setNullSelectionAllowed(false);
		isEnabled_og.setStyleName("twocol100");
		isEnabled_og.setCaption("可用状态：");
		isEnabled_og.setImmediate(true);
		
		direction_og = new OptionGroup();
		direction_og.setRequired(true);
		direction_og.addListener(this);
		direction_og.setImmediate(true);
		direction_og.addItem("outgoing");
		direction_og.addItem("incoming");
		direction_og.setItemCaption("outgoing", "呼出");
		direction_og.setItemCaption("incoming", "呼入");
		direction_og.setNullSelectionAllowed(false);
		direction_og.setStyleName("twocol100");
		direction_og.setCaption("呼叫方向：");
	}

	/**
	 * 创建Form 表单, 并放入windowContent 
	 * @param windowContent
	 */
	private void createFormComponent(VerticalLayout windowContent) {
		recordStatusForm = new Form();
		recordStatusForm.setValidationVisibleOnCommit(true);
		recordStatusForm.setValidationVisible(false);
		recordStatusForm.setInvalidCommitted(false);
		recordStatusForm.setWriteThrough(false);
		recordStatusForm.setImmediate(true);
		recordStatusForm.setFormFieldFactory(new MyFieldFactory());
		recordStatusForm.setFooter(creatFormFooterComponents());
		windowContent.addComponent(recordStatusForm);
	}

	/**
	 *  创建操作按钮
	 */
	private HorizontalLayout creatFormFooterComponents() {
		HorizontalLayout operator_l = new HorizontalLayout();
		operator_l.setSpacing(true);
		operator_l.setWidth("100%");

		save_button = new Button("保 存", this);
		save_button.setStyleName("default");
		operator_l.addComponent(save_button);
		
		cancel_button = new Button("取 消", this);
		operator_l.addComponent(cancel_button);
		
		return operator_l;
	}

	@Override
	public void valueChange(ValueChangeEvent event) {
		Property source = event.getProperty();
		if(source == direction_og) {
			String direction = (String) direction_og.getValue();
			if("incoming".equals(direction)) {
				isAnswered_og.setValue(true);
				isAnswered_og.setEnabled(false);
			} else {
				isAnswered_og.setEnabled(true);
			}
		}
	}

	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if(source == save_button) {
			executeSave();
		} else if(source == cancel_button) {
			recordStatusForm.discard();
			this.getApplication().getMainWindow().removeWindow(this);
		}
	}

	/**
	 * 执行保存
	 */
	private void executeSave() {
		//检查输入是否合法
		String currentStatusName = StringUtils.trimToEmpty((String) statusName_tf.getValue());
		if("".equals(currentStatusName)) {
			warning_notification.setCaption("客服记录状态的名称不能为空！");
			this.getApplication().getMainWindow().showNotification(warning_notification);
			return;
		}

		// 检查当前客服记录状态是否已经保存过，如果已经存在，则直接返回
		String currentDirection = (String) direction_og.getValue();
		if(!currentStatusName.equals(oldStatusName) || !currentDirection.equals(oldDirection)) {
			boolean existed = customerServiceRecordStatusService.checkExistedByStatusName(currentStatusName, currentDirection, domain.getId());
			if(existed == true) {
				String typeCaption = "incoming".equals(currentDirection) ? "呼入" : "呼出";
				warning_notification.setCaption("呼叫方向为 ["+typeCaption+"]，结果名称为 ["+currentStatusName+"] 的客服记录状态已经存在！");
				this.getApplication().getMainWindow().showNotification(warning_notification);
				return;
			}
		}
		recordStatusForm.commit();
		
		try {
			if(isAddNewRecordStatus) {
				customerServiceRecordStatusService.save(recordStatus);
			} else {
				customerServiceRecordStatusService.update(recordStatus);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("管理员保存客服记录出现异常---> "+e.getMessage(), e);
			warning_notification.setCaption("保存失败，请检查信息后重试！");
			this.getApplication().getMainWindow().showNotification(warning_notification);
			return;
		}
		// 更新客服记录状态显示界面的信息
		recordStatusManagement.refreshTable(isAddNewRecordStatus);
		
		success_notification.setCaption("保存信息成功！");
		this.getApplication().getMainWindow().showNotification(success_notification);
		this.getApplication().getMainWindow().removeWindow(this);
	}

	/**
	 * 自定义Form 域创建类
	 */
	private class MyFieldFactory extends DefaultFieldFactory {
		@Override  
		public Field createField(Item item, Object propertyId,
				Component uiContext) {
			if("statusName".equals(propertyId)){
				return statusName_tf;
			} else if("isAnswered".equals(propertyId)){
				return isAnswered_og;
			} else if("isMeanVipCustomer".equals(propertyId)){
				return isVip_og;
			} else if("enabled".equals(propertyId)){
				return isEnabled_og;
			} else if("direction".equals(propertyId)){
				return direction_og;
			} 
			return null;
		}
	}

	/**
	 * 更新Form 的数据源
	 * @param recordStatus			黑名单对象
	 * @param isAddNewRecordStatus		是否为添加新的服务记录状态操作
	 */
	public void updateFormDataSource(CustomerServiceRecordStatus recordStatus, boolean isAddNewRecordStatus) {
		this.recordStatus = recordStatus;
		this.oldStatusName = recordStatus.getStatusName();
		this.isAddNewRecordStatus = isAddNewRecordStatus;
		this.oldDirection = recordStatus.getDirection();
		recordStatusForm.setItemDataSource(new BeanItem<CustomerServiceRecordStatus>(recordStatus), Arrays.asList(VISIBLE_PROPERTIES));
	}
	
}
