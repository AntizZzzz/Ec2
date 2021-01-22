package com.jiangyifen.ec2.ui.mgr.system.tabsheet;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.jiangyifen.ec2.email.entity.MailConfig;
import com.jiangyifen.ec2.email.service.MailConfigService;
import com.jiangyifen.ec2.entity.CustomerLevel;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.Ec2Configuration;
import com.jiangyifen.ec2.entity.TableKeyword;
import com.jiangyifen.ec2.entity.TableKeywordDefault;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.entity.enumtype.ExecuteType;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.service.common.CommonService;
import com.jiangyifen.ec2.service.eaoservice.CustomerLevelService;
import com.jiangyifen.ec2.service.eaoservice.DomainService;
import com.jiangyifen.ec2.service.eaoservice.Ec2ConfigurationService;
import com.jiangyifen.ec2.service.eaoservice.TableKeywordService;
import com.jiangyifen.ec2.ui.util.NotificationUtil;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.Notification;

@SuppressWarnings("serial")
public class InnerConfigManagement extends VerticalLayout implements Button.ClickListener,Property.ValueChangeListener {
	private static String NOBELONG="NOBELONG";
	private static String ZEROBELONG="ZEROBELONG";
	private static String POSTCODEBELONG="POSTCODEBELONG";
	
	//---------------------------------------------基础配置 
	// jrh 等级可降级配置
	private ComboBox levelDownableComboBox;
	private Ec2Configuration levelDownableConfiguration;
	
	//客户呼入满意度调查配置
	private ComboBox incomingSatisfactionComboBox;
	private Ec2Configuration incomingSatisfactionConfiguration;

	//客户呼出满意度调查配置 
	private ComboBox outgoingSatisfactionComboBox;
	private Ec2Configuration outgoingSatisfactionConfiguration;
	
	//号码归属地识别 
	private ComboBox belongComboBox; //null是不开启，false是加0，true是加区号
	private Ec2Configuration belongConfiguration;
	
	// jrh 【话务员是否可以将已经与自己成交的客户，从‘我的客户’中移除】
	private ComboBox customerRemoveAbleByCsrComboBox;
	private Ec2Configuration customerRemoveAbleByCsrConfiguration;
	
	// jrh 【部门间历史客服记录可见】
	private ComboBox viewRecordSpanDeptComboBox;
	private Ec2Configuration viewRecordSpanDeptConfiguration;
	
	
	// jrh 【按外线区分黑名单】
	private ComboBox settingBlacklistByOutlineComboBox;
	private Ec2Configuration settingBlacklistByOutlineConfiguration;
	
	// jrh 【部门间历史订单可见】
	private ComboBox viewOrderSpanDeptComboBox;
	private Ec2Configuration viewOrderSpanDeptConfiguration;
	
	// jrh 【登陆分机默认置忙】
	private ComboBox pauseExtenOnLoginComboBox;
	private Ec2Configuration pauseExtenOnLoginConfiguration;
	
	// jrh 【呼叫弹屏默认置忙】
	private ComboBox pauseExtenOnPopupWindowComboBox;
	private Ec2Configuration pauseExtenOnPopupWindowConfiguration;
	
	// jrh 【呼叫弹屏置忙时，是否创建置忙记录】
	private ComboBox popupWinPauseLogCreatableComboBox;
	private Ec2Configuration popupWinPauseLogCreatableConfiguration;
	
	// jrh 【呼叫弹屏置忙时，是否创建话后处理记录】
	private ComboBox popupWinCallAfterLogCreatableComboBox;
	private Ec2Configuration popupWinCallAfteLogCreatableConfiguration;
	
	// jinht 【分机创建客服记录类型，true：按客户电话创建，false：按上次通话创建】
	private ComboBox createCustomerServiceRecordByExtenTypeComboBox;
	private Ec2Configuration createCustomerServiceRecordByExtenTypeConfiguration;
	
	// jinht 【设置全局是否使用同一个邮箱进行发送邮件，true：是，false：否】
	private ComboBox settingGlobalEmailComboBox;
	private Ec2Configuration settingGlobalEmailConfiguration;
	
	// jinht 【系统磁盘使用超过 90% 时, 是否自动删除录音，true：是，false：否】
	private ComboBox settingDiskSpaceDeleteSoundFileComboBox;
	private Ec2Configuration settingDiskSpaceDeleteSoundFileConfiguration;
	
	
	private HorizontalLayout basicButtonsLayout;
	private Button basicEditButton;
	private Button basicSaveButton;
	private Button basicCancelButton;
	
	
	//---------------------------------------------高级配置 
	private Button keywordConfigAdd;
	private Button keywordConfigDelete;
	private ListSelect keywordConfigSelect;
	private TextField keywordConfigInputField;
	private BeanItemContainer<TableKeyword> keywordConfigKeywordContainer;
	
	private VerticalLayout levelEditLayout;
	private HorizontalLayout levelEditButtonLayout;
	private VerticalLayout levelEditPlaceHolderLayout;
	private ListSelect levelSelect;
	private TextField levelField;
	private ComboBox levelComboBox;
	private ComboBox protectTimeComboBox;
	private Button addLevel;
	private Button editLevel;
	private Button deleteLevel;
	private BeanItemContainer<CustomerLevel> levelContainer;

	//---------------------------------------------Service
	private TableKeywordService keywordService;
	private CustomerLevelService customerLevelService;
	private CommonService commonService;
	private Ec2ConfigurationService ec2ConfigurationService;
	private MailConfigService mailConfigService;
	private DomainService domainService;
	private Domain domain;
	private User loginUser;
	
	/**
	 * 编辑状态信息组件
	 */
	private Button saveLevel;
	private Button cancelLevel;
	private CustomerLevel customerLevel;
	/**
	 * 构造器
	 */
	public InnerConfigManagement() {
		this.initService();
		this.setWidth("100%");
		this.setMargin(true);
		this.setSpacing(true);
		
		//约束组件
		HorizontalLayout layout = new HorizontalLayout();
		layout.setSizeFull();
		layout.setSpacing(true);
		layout.setWidth("100%");
		layout.addComponent(buildBasicConfigLayout());
		layout.addComponent(buildAdvanceConfigLayout());
		this.addComponent(layout);

	}

	/**
	 * 基础配置信息
	 * @return
	 */
	private Component buildBasicConfigLayout() {
		Panel panel=new Panel("基础配置");
		
		//约束组件
		VerticalLayout constraintLayout=new VerticalLayout();
		constraintLayout.setWidth("100%");
		panel.addComponent(constraintLayout);
		
		GridLayout gridLayout = new GridLayout(2,1);
//		gridLayout.setWidth("100%");
		gridLayout.setSpacing(true);
		constraintLayout.addComponent(gridLayout);
		
		//---------------------------------------------jrh 创建等级可升降配置项
		int column=0;
		gridLayout.setRows(column+1);
		
		//Label
		Label label_leveldownable = new Label("客户等级可降：");
		label_leveldownable.setWidth("-1px");
		label_leveldownable.setDescription("<B>客户的等级是否可以降低，如果不可以，则等级只能持平或升级</B>");
		gridLayout.addComponent(label_leveldownable, 0, column);
		
		//ComboBox
		levelDownableComboBox = new ComboBox();
		levelDownableComboBox.setDescription("<B>客户的等级是否可以降低，如果不可以，则等级只能持平或升级</B>");
		levelDownableComboBox.addItem(false);
		levelDownableComboBox.addItem(true);
		levelDownableComboBox.setItemCaption(false, "否 (只升降)");
		levelDownableComboBox.setItemCaption(true, "是 (可降级)");
		levelDownableComboBox.setNullSelectionAllowed(false);
		levelDownableComboBox.setWidth("20em");
		gridLayout.addComponent(levelDownableComboBox, 1, column);
		
		//Default Value
		levelDownableConfiguration =  ec2ConfigurationService.getByKey("customer_level_down_able", domain.getId());
		if(levelDownableConfiguration == null || !levelDownableConfiguration.getValue()) {
			levelDownableComboBox.setValue(false);	// 默认是不可降级的
		} else {
			levelDownableComboBox.setValue(true);
		}
		
		//---------------------------------------------客户呼入满意度调查配置
		column++;
		gridLayout.setRows(column+1);

		//Label
		Label label_incoming_satisaction = new Label("呼入满意度调查设置：");
		label_incoming_satisaction.setWidth("-1px");
		label_incoming_satisaction.setDescription("<B>是否开启呼入满意度调查</B>");
		gridLayout.addComponent(label_incoming_satisaction, 0, column);
		
		//ComboBox
		incomingSatisfactionComboBox = new ComboBox();
		incomingSatisfactionComboBox.setDescription("<B>是否开启呼入满意度调查</B>");
		incomingSatisfactionComboBox.addItem(false);
		incomingSatisfactionComboBox.addItem(true);
		incomingSatisfactionComboBox.setItemCaption(false, "不调查");
		incomingSatisfactionComboBox.setItemCaption(true, "调查");
		incomingSatisfactionComboBox.setNullSelectionAllowed(false);
		incomingSatisfactionComboBox.setWidth("20em");
		gridLayout.addComponent(incomingSatisfactionComboBox, 1, column);
		
		//Default Value
		incomingSatisfactionConfiguration =  ec2ConfigurationService.getByKey("incoming_sati_config", domain.getId());
		if(incomingSatisfactionConfiguration == null || incomingSatisfactionConfiguration.getValue()==false) {
			incomingSatisfactionComboBox.setValue(false);
		} else {
			incomingSatisfactionComboBox.setValue(true);
		}
		
		//---------------------------------------------客户呼出满意度调查配置
		column++;
		gridLayout.setRows(column+1);

		//Label
		Label label_outgoing_satisaction = new Label("呼出满意度调查设置：");
		label_outgoing_satisaction.setWidth("-1px");
		label_outgoing_satisaction.setDescription("<B>是否开启呼出满意度调查</B>");
		gridLayout.addComponent(label_outgoing_satisaction, 0, column);
		
		//ComboBox
		outgoingSatisfactionComboBox = new ComboBox();
		outgoingSatisfactionComboBox.setDescription("<B>是否开启呼出满意度调查</B>");
		outgoingSatisfactionComboBox.addItem(false);
		outgoingSatisfactionComboBox.addItem(true);
		outgoingSatisfactionComboBox.setItemCaption(false, "不调查");
		outgoingSatisfactionComboBox.setItemCaption(true, "调查");
		outgoingSatisfactionComboBox.setNullSelectionAllowed(false);
		outgoingSatisfactionComboBox.setWidth("20em");
		gridLayout.addComponent(outgoingSatisfactionComboBox, 1, column);
		
		//Default Value
		outgoingSatisfactionConfiguration =  ec2ConfigurationService.getByKey("outgoing_sati_config", domain.getId());
		if(outgoingSatisfactionConfiguration == null) {
			outgoingSatisfactionComboBox.setValue(false);
		} else {
			outgoingSatisfactionComboBox.setValue(outgoingSatisfactionConfiguration.getValue());
		}

		//---------------------------------------------客户呼出满意度调查配置
		column++;
		gridLayout.setRows(column+1);
		
		//Label
		Label label_belong_ident = new Label("号码归属地识别：");
		label_belong_ident.setWidth("-1px");
		label_belong_ident.setDescription("<B>号码归属地识别开启后会自动在外地手机前加0呼出</B>");
		gridLayout.addComponent(label_belong_ident, 0, column);
		
		//ComboBox
		belongComboBox = new ComboBox();
		belongComboBox.setDescription("<B>是否开启号码归属地识别</B>");
		belongComboBox.setNullSelectionAllowed(false);
		belongComboBox.addItem(InnerConfigManagement.NOBELONG);
		belongComboBox.addItem(InnerConfigManagement.ZEROBELONG);
		belongComboBox.addItem(InnerConfigManagement.POSTCODEBELONG);
		belongComboBox.setItemCaption(InnerConfigManagement.NOBELONG, "不开启归属识别");
		belongComboBox.setItemCaption(InnerConfigManagement.ZEROBELONG, "号码本外地识别");
		belongComboBox.setItemCaption(InnerConfigManagement.POSTCODEBELONG, "号码加区号");
		belongComboBox.setWidth("20em");
		gridLayout.addComponent(belongComboBox, 1, column);
		
		//Default Value
		belongConfiguration =  ec2ConfigurationService.getByKey("belong_config", domain.getId());
		if(belongConfiguration == null||belongConfiguration.getValue()== null) {
			belongComboBox.setValue(InnerConfigManagement.NOBELONG);
		}else if(belongConfiguration.getValue()==false){
			belongComboBox.setValue(InnerConfigManagement.ZEROBELONG);
		} else if(belongConfiguration.getValue()==true){
			belongComboBox.setValue(InnerConfigManagement.POSTCODEBELONG);
		}
		
		
		//---------------------------------------------jrh 创建【话务员是否可以将已经与自己成交的客户，从‘我的客户’中移除】 组件
		column++;
		gridLayout.setRows(column+1);
		
		//Label
		Label labelRemoveAbleByCsr = new Label("坐席可移除客户：");
		labelRemoveAbleByCsr.setWidth("-1px");
		labelRemoveAbleByCsr.setDescription("<B>坐席是否可以将已经与自己成交的客户从‘我的客户’中移除</B>");
		gridLayout.addComponent(labelRemoveAbleByCsr, 0, column);
		
		//ComboBox
		customerRemoveAbleByCsrComboBox = new ComboBox();
		customerRemoveAbleByCsrComboBox.setDescription("<B>话务员是否可以将已经与自己成交的客户从‘我的客户’中移除</B>");
		customerRemoveAbleByCsrComboBox.addItem(false);
		customerRemoveAbleByCsrComboBox.addItem(true);
		customerRemoveAbleByCsrComboBox.setItemCaption(false, "否 (不可移除)");
		customerRemoveAbleByCsrComboBox.setItemCaption(true, "是 (可移除)");
		customerRemoveAbleByCsrComboBox.setNullSelectionAllowed(false);
		customerRemoveAbleByCsrComboBox.setWidth("20em");
		gridLayout.addComponent(customerRemoveAbleByCsrComboBox, 1, column);
		
		//Default Value
		customerRemoveAbleByCsrConfiguration =  ec2ConfigurationService.getByKey("customer_remove_able_by_csr", domain.getId());
		if(customerRemoveAbleByCsrConfiguration == null) {
			customerRemoveAbleByCsrComboBox.setValue(false);
		} else {
			customerRemoveAbleByCsrComboBox.setValue(customerRemoveAbleByCsrConfiguration.getValue());
		}
		
		//---------------------------------------------jrh 创建【部门间历史客服记录可见】 组件
		column++;
		gridLayout.setRows(column+1);
		
		//Label
		Label labelRecordSpanDept = new Label("坐席查看历史客服记录范围：");
		labelRecordSpanDept.setWidth("-1px");
		labelRecordSpanDept.setDescription("<B>设定各部门之间的话务员是否可以看到相互之间跟同一个客户的历史客服记录！</B>");
		gridLayout.addComponent(labelRecordSpanDept, 0, column);
		
		//ComboBox
		viewRecordSpanDeptComboBox = new ComboBox();
		viewRecordSpanDeptComboBox.setDescription("<B>设定各部门之间的话务员是否可以看到相互之间跟同一个客户的历史客服记录！</B>");
		viewRecordSpanDeptComboBox.addItem("global");
		viewRecordSpanDeptComboBox.addItem(false);
		viewRecordSpanDeptComboBox.addItem(true);
		viewRecordSpanDeptComboBox.setItemCaption("global", "全局范围");
		viewRecordSpanDeptComboBox.setItemCaption(false, "单个坐席");
		viewRecordSpanDeptComboBox.setItemCaption(true, "单个部门");
		viewRecordSpanDeptComboBox.setNullSelectionAllowed(false);
		viewRecordSpanDeptComboBox.setWidth("20em");
		gridLayout.addComponent(viewRecordSpanDeptComboBox, 1, column);
		
		//Default Value
		viewRecordSpanDeptConfiguration =  ec2ConfigurationService.getByKey("csr_view_record_span_department", domain.getId());
		if(viewRecordSpanDeptConfiguration == null || viewRecordSpanDeptConfiguration.getValue() == null) {
			viewRecordSpanDeptComboBox.setValue("global");
		} else {
			viewRecordSpanDeptComboBox.setValue(viewRecordSpanDeptConfiguration.getValue());
		}
		
		
		//---------------------------------------------jrh 创建【部门间历史订单可见】 组件
		column++;
		gridLayout.setRows(column+1);
		
		//Label
		Label labelViewOrderSpanDept = new Label("坐席查看客户历史订单范围：");
		labelViewOrderSpanDept.setWidth("-1px");
		labelViewOrderSpanDept.setDescription("<B>设定各部门之间的话务员是否可以看到相互之间跟同一个客户的历史订单！</B>");
		gridLayout.addComponent(labelViewOrderSpanDept, 0, column);
		
		//ComboBox
		viewOrderSpanDeptComboBox = new ComboBox();
		viewOrderSpanDeptComboBox.setDescription("<B>设定各部门之间的话务员是否可以看到相互之间跟同一个客户的历史订单！</B>");
		viewOrderSpanDeptComboBox.addItem("global");
		viewOrderSpanDeptComboBox.addItem(false);
		viewOrderSpanDeptComboBox.addItem(true);
		viewOrderSpanDeptComboBox.setItemCaption("global", "全局范围");
		viewOrderSpanDeptComboBox.setItemCaption(false, "单个坐席");
		viewOrderSpanDeptComboBox.setItemCaption(true, "单个部门");
		viewOrderSpanDeptComboBox.setNullSelectionAllowed(false);
		viewOrderSpanDeptComboBox.setWidth("20em");
		gridLayout.addComponent(viewOrderSpanDeptComboBox, 1, column);
		
		//Default Value
		viewOrderSpanDeptConfiguration =  ec2ConfigurationService.getByKey("csr_view_order_span_department", domain.getId());
		if(viewOrderSpanDeptConfiguration == null || viewOrderSpanDeptConfiguration.getValue() == null) {
			viewOrderSpanDeptComboBox.setValue("global");
		} else {
			viewOrderSpanDeptComboBox.setValue(viewOrderSpanDeptConfiguration.getValue());
		}
		
		
		//---------------------------------------------jrh 创建登陆分机默认置忙配置项
		column++;
		gridLayout.setRows(column+1);
		
		//Label
		Label labelPauseExtenOnLogin = new Label("登陆分机默认置忙：");
		labelPauseExtenOnLogin.setWidth("-1px");
		labelPauseExtenOnLogin.setDescription("<B>设置用户登录后，是否默认将分机置忙</B>");
		gridLayout.addComponent(labelPauseExtenOnLogin, 0, column);
		
		//ComboBox
		pauseExtenOnLoginComboBox = new ComboBox();
		pauseExtenOnLoginComboBox.setDescription("<B>设置用户登录后，是否默认将分机置忙</B>");
		pauseExtenOnLoginComboBox.addItem(false);
		pauseExtenOnLoginComboBox.addItem(true);
		pauseExtenOnLoginComboBox.setItemCaption(false, "否");
		pauseExtenOnLoginComboBox.setItemCaption(true, "是");
		pauseExtenOnLoginComboBox.setNullSelectionAllowed(false);
		pauseExtenOnLoginComboBox.setWidth("20em");
		gridLayout.addComponent(pauseExtenOnLoginComboBox, 1, column);
		
		//Default Value
		pauseExtenOnLoginConfiguration =  ec2ConfigurationService.getByKey("pasue_exten_after_csr_login", domain.getId());
		if(pauseExtenOnLoginConfiguration == null || !pauseExtenOnLoginConfiguration.getValue()) {
			pauseExtenOnLoginComboBox.setValue(false);	// 默认是不置忙
		} else {
			pauseExtenOnLoginComboBox.setValue(true);
		}
		
		
		//---------------------------------------------jrh 创建呼叫弹屏默认置忙配置项
		column++;
		gridLayout.setRows(column+1);
		
		//Label
		Label labelPauseExtenOnPopup = new Label("呼叫弹屏默认置忙：");
		labelPauseExtenOnPopup.setWidth("-1px");
		labelPauseExtenOnPopup.setDescription("<B>设置呼入、呼出弹屏后，是否默认将分机置忙，在弹屏关闭后将自动将分机置闲</B>");
		gridLayout.addComponent(labelPauseExtenOnPopup, 0, column);
		
		//ComboBox
		pauseExtenOnPopupWindowComboBox = new ComboBox();
		pauseExtenOnPopupWindowComboBox.setDescription("<B>设置呼入、呼出弹屏后，是否默认将分机置忙，在弹屏关闭后将自动将分机置闲</B>");
		pauseExtenOnPopupWindowComboBox.setImmediate(true);
		pauseExtenOnPopupWindowComboBox.addListener((ValueChangeListener) this);
		pauseExtenOnPopupWindowComboBox.addItem(false);
		pauseExtenOnPopupWindowComboBox.addItem(true);
		pauseExtenOnPopupWindowComboBox.setItemCaption(false, "否");
		pauseExtenOnPopupWindowComboBox.setItemCaption(true, "是");
		pauseExtenOnPopupWindowComboBox.setNullSelectionAllowed(false);
		pauseExtenOnPopupWindowComboBox.setWidth("20em");
		
		//ComboBox
		popupWinPauseLogCreatableComboBox = new ComboBox();
		popupWinPauseLogCreatableComboBox.setDescription("<B>如果弹屏置忙，则是否需要创建置忙记录</B>");
		popupWinPauseLogCreatableComboBox.addItem(true);
		popupWinPauseLogCreatableComboBox.addItem(false);
		popupWinPauseLogCreatableComboBox.setItemCaption(true, "创建置忙记录");
		popupWinPauseLogCreatableComboBox.setItemCaption(false, "不创建置忙记录");
		popupWinPauseLogCreatableComboBox.setNullSelectionAllowed(false);
		popupWinPauseLogCreatableComboBox.setWidth("20em");
		
		//ComboBox
		popupWinCallAfterLogCreatableComboBox = new ComboBox();
		popupWinCallAfterLogCreatableComboBox.setDescription("<B>如果弹屏置忙，则是否需要创建话后处理记录</B>");
		popupWinCallAfterLogCreatableComboBox.addItem(true);
		popupWinCallAfterLogCreatableComboBox.addItem(false);
		popupWinCallAfterLogCreatableComboBox.setItemCaption(true, "创建话后处理记录");
		popupWinCallAfterLogCreatableComboBox.setItemCaption(false, "不创建话后处理记录");
		popupWinCallAfterLogCreatableComboBox.setNullSelectionAllowed(false);
		popupWinCallAfterLogCreatableComboBox.setWidth("20em");
		
		VerticalLayout pauseExten_vlo = new VerticalLayout();
		pauseExten_vlo.setSpacing(true);
		pauseExten_vlo.addComponent(pauseExtenOnPopupWindowComboBox);
		pauseExten_vlo.addComponent(popupWinPauseLogCreatableComboBox);
		pauseExten_vlo.addComponent(popupWinCallAfterLogCreatableComboBox);
		gridLayout.addComponent(pauseExten_vlo, 1, column);
		
		//Default Value
		pauseExtenOnPopupWindowConfiguration =  ec2ConfigurationService.getByKey("pasue_exten_after_csr_popup_calling_window", domain.getId());
		if(pauseExtenOnPopupWindowConfiguration == null || !pauseExtenOnPopupWindowConfiguration.getValue()) {
			pauseExtenOnPopupWindowComboBox.setValue(false);	// 默认是不置忙
			popupWinPauseLogCreatableComboBox.setVisible(false);
			popupWinCallAfterLogCreatableComboBox.setVisible(false);
		} else {
			pauseExtenOnPopupWindowComboBox.setValue(true);
			popupWinPauseLogCreatableComboBox.setVisible(true);
			popupWinCallAfterLogCreatableComboBox.setVisible(true);
		}

		popupWinPauseLogCreatableConfiguration =  ec2ConfigurationService.getByKey("create_pause_log_after_csr_popup_calling_window", domain.getId());
		if(popupWinPauseLogCreatableConfiguration == null || popupWinPauseLogCreatableConfiguration.getValue()) {
			popupWinPauseLogCreatableComboBox.setValue(true);	// 默认是创建置忙记录
		} else {
			popupWinPauseLogCreatableComboBox.setValue(false);
		}
		
		popupWinCallAfteLogCreatableConfiguration =  ec2ConfigurationService.getByKey("create_afterCall_log_after_csr_popup_calling_window", domain.getId());
		if(popupWinCallAfteLogCreatableConfiguration == null || popupWinCallAfteLogCreatableConfiguration.getValue()) {
			popupWinCallAfterLogCreatableComboBox.setValue(true);	// 默认是创建置忙记录
		} else {
			popupWinCallAfterLogCreatableComboBox.setValue(false);
		}
		
		
		//---------------------------------------------jrh 创建按外线区分黑名单配置项
		column++;
		gridLayout.setRows(column+1);
		
		//Label
		Label labelSettingBlacklistByOutline = new Label("按外线区分黑名单：");
		labelSettingBlacklistByOutline.setWidth("-1px");
		labelSettingBlacklistByOutline.setDescription("<B>如果不开启，全局范围内都使用相同的黑名单，否则按外线进行区分</B>");
		gridLayout.addComponent(labelSettingBlacklistByOutline, 0, column);
		
		//ComboBox
		settingBlacklistByOutlineComboBox = new ComboBox();
		settingBlacklistByOutlineComboBox.setDescription("<B>如果不开启，全局范围内都使用相同的黑名单，否则按外线进行区分</B>");
		settingBlacklistByOutlineComboBox.addItem(false);
		settingBlacklistByOutlineComboBox.addItem(true);
		settingBlacklistByOutlineComboBox.setItemCaption(false, "否");
		settingBlacklistByOutlineComboBox.setItemCaption(true, "是");
		settingBlacklistByOutlineComboBox.setNullSelectionAllowed(false);
		settingBlacklistByOutlineComboBox.setWidth("20em");
		gridLayout.addComponent(settingBlacklistByOutlineComboBox, 1, column);
		
		//Default Value
		settingBlacklistByOutlineConfiguration =  ec2ConfigurationService.getByKey("setting_blacklist_by_outline", domain.getId());
		if(settingBlacklistByOutlineConfiguration == null || !settingBlacklistByOutlineConfiguration.getValue()) {
			settingBlacklistByOutlineComboBox.setValue(false);	// 默认是不可降级的
		} else {
			settingBlacklistByOutlineComboBox.setValue(true);
		}
		
		//---------------------------------------------jinht 分机创建客服记录类型
		column++;
		gridLayout.setRows(column+1);
		
		// Label
		Label labelCreateCustomerServiceRecordByExtenType = new Label("分机创建客服记录类型：");
		labelCreateCustomerServiceRecordByExtenType.setWidth("-1px");
		labelCreateCustomerServiceRecordByExtenType.setDescription("<B>如果不开启，没有电脑的座席每次保存客服记录都是根据上次通话进行创建</B>");
		gridLayout.addComponent(labelCreateCustomerServiceRecordByExtenType, 0, column);
		
		// ComboBox
		createCustomerServiceRecordByExtenTypeComboBox = new ComboBox();
		createCustomerServiceRecordByExtenTypeComboBox.setDescription("<B>如果不开启，没有电脑的座席每次保存客服记录都是根据上次通话进行创建</B>");
		createCustomerServiceRecordByExtenTypeComboBox.addItem(false);
		createCustomerServiceRecordByExtenTypeComboBox.addItem(true);
		createCustomerServiceRecordByExtenTypeComboBox.setItemCaption(false, "按上通通话创建");
		createCustomerServiceRecordByExtenTypeComboBox.setItemCaption(true, "按客户电话创建");
		createCustomerServiceRecordByExtenTypeComboBox.setNullSelectionAllowed(false);
		createCustomerServiceRecordByExtenTypeComboBox.setWidth("20em");
		gridLayout.addComponent(createCustomerServiceRecordByExtenTypeComboBox, 1, column);
		
		//Default Value																			 
		createCustomerServiceRecordByExtenTypeConfiguration =  ec2ConfigurationService.getByKey("create_customer_service_record_by_exten_type", domain.getId());
		if(createCustomerServiceRecordByExtenTypeConfiguration == null || !createCustomerServiceRecordByExtenTypeConfiguration.getValue()) {
			createCustomerServiceRecordByExtenTypeComboBox.setValue(false);	// 
		} else {
			createCustomerServiceRecordByExtenTypeComboBox.setValue(true);
		}
		
		// --------------------------------------------- jinht 设置邮箱是否使用全局默认配置进行发送邮件
		column++;
		gridLayout.setRows(column + 1);
		
		// Label
		Label labelSettingGlobalEmail = new Label("设置全局邮箱发送方式：");
		labelSettingGlobalEmail.setWidth("-1px");
		labelSettingGlobalEmail.setDescription("<B>如果开启，则表示全局使用统一配置的管理员的邮箱帐号密码进行发送邮件；否则，表示每个人都应该配置自己的个人邮件进行发送邮件</B>");
		gridLayout.addComponent(labelSettingGlobalEmail, 0, column);
		
		// ComboBox
		settingGlobalEmailComboBox = new ComboBox();
		settingGlobalEmailComboBox.setDescription("<B>如果开启，则表示全局使用统一配置的管理员的邮箱帐号密码进行发送邮件；否则，表示每个人都应该配置自己的个人邮件进行发送邮件</B>");
		settingGlobalEmailComboBox.addItem(false);
		settingGlobalEmailComboBox.addItem(true);
		settingGlobalEmailComboBox.setItemCaption(false, "按员工个人邮箱发送");
		settingGlobalEmailComboBox.setItemCaption(true, "全局按管理员邮箱发送");
		settingGlobalEmailComboBox.setNullSelectionAllowed(false);
		settingGlobalEmailComboBox.setWidth("20em");
		gridLayout.addComponent(settingGlobalEmailComboBox, 1, column);
		
		// Default Value
		settingGlobalEmailConfiguration = ec2ConfigurationService.getByKey("setting_global_email", domain.getId());
		if(settingGlobalEmailConfiguration == null || !settingGlobalEmailConfiguration.getValue()) {
			settingGlobalEmailComboBox.setValue(false);	
		} else {
			settingGlobalEmailComboBox.setValue(true); 
		}
		
		// --------------------------------------------- jinht 设置系统磁盘超过 90% 是否自动删除录音
		column++;
		gridLayout.setRows(column+1);
		
		// Label 
		Label labelSettingDiskSpaceDeleteSoundFile = new Label("磁盘空间不足自动删除录音：");
		labelSettingDiskSpaceDeleteSoundFile.setWidth("-1px");
		labelSettingDiskSpaceDeleteSoundFile.setDescription("<B>设置系统磁盘使用超过 90% 时, 是否自动删除录音</B>");
		gridLayout.addComponent(labelSettingDiskSpaceDeleteSoundFile, 0, column);
		
		// ComboBox
		settingDiskSpaceDeleteSoundFileComboBox = new ComboBox();
		settingDiskSpaceDeleteSoundFileComboBox.setDescription("<B>设置系统磁盘使用超过 90% 时, 是否自动删除录音</B>");
		settingDiskSpaceDeleteSoundFileComboBox.addItem(false);
		settingDiskSpaceDeleteSoundFileComboBox.addItem(true);
		settingDiskSpaceDeleteSoundFileComboBox.setItemCaption(false, "否");
		settingDiskSpaceDeleteSoundFileComboBox.setItemCaption(true, "是");
		settingDiskSpaceDeleteSoundFileComboBox.setNullSelectionAllowed(false);
		settingDiskSpaceDeleteSoundFileComboBox.setWidth("20em");
		gridLayout.addComponent(settingDiskSpaceDeleteSoundFileComboBox, 1, column);
		
		// Default Value
		settingDiskSpaceDeleteSoundFileConfiguration = ec2ConfigurationService.getByKey("setting_diskspace_delete_soundfile", domain.getId());
		if(settingDiskSpaceDeleteSoundFileConfiguration == null || settingDiskSpaceDeleteSoundFileConfiguration.getValue()) {
			settingDiskSpaceDeleteSoundFileComboBox.setValue(true);
		} else {
			settingDiskSpaceDeleteSoundFileComboBox.setValue(false);
		}
		
		//---------------------------------------------下方按钮
		column++;
		gridLayout.setRows(column+1);
		basicButtonsLayout=new HorizontalLayout();
		basicButtonsLayout.setSpacing(true);
		basicEditButton   =new Button("基础配置编辑", this);
		basicSaveButton   =new Button("保存", this);
		basicCancelButton =new Button("取消", this);
		basicButtonsLayout.addComponent(basicEditButton);
		gridLayout.addComponent(basicButtonsLayout, 1, column);
		
		//强制坐席退出
		KickCsrLogoutConfigView kickCsrLogoutConfigView = new KickCsrLogoutConfigView();
		constraintLayout.addComponent(kickCsrLogoutConfigView);
		
		//设置是否可编辑
		setBasicConfigEnabled(false);
		
		return panel;
	}
	
	/**
	 * 设置基础配置中的编辑项是否可用
	 * @param isEnabled
	 */
	private void setBasicConfigEnabled(Boolean isEnabled){
		levelDownableComboBox.setReadOnly(!isEnabled);
		incomingSatisfactionComboBox.setReadOnly(!isEnabled);
		outgoingSatisfactionComboBox.setReadOnly(!isEnabled);
		belongComboBox.setReadOnly(!isEnabled);
		customerRemoveAbleByCsrComboBox.setReadOnly(!isEnabled);
		viewRecordSpanDeptComboBox.setReadOnly(!isEnabled);
		viewOrderSpanDeptComboBox.setReadOnly(!isEnabled);
		pauseExtenOnLoginComboBox.setReadOnly(!isEnabled);
		pauseExtenOnPopupWindowComboBox.setReadOnly(!isEnabled);
		popupWinPauseLogCreatableComboBox.setReadOnly(!isEnabled);
		popupWinCallAfterLogCreatableComboBox.setReadOnly(!isEnabled);
		settingBlacklistByOutlineComboBox.setReadOnly(!isEnabled);
		createCustomerServiceRecordByExtenTypeComboBox.setReadOnly(!isEnabled);
		settingGlobalEmailComboBox.setReadOnly(!isEnabled);
		settingDiskSpaceDeleteSoundFileComboBox.setReadOnly(!isEnabled);
		
	}
	
	
	/**
	 * 保存基础配置结果
	 */
	private void saveBasicConfig(){
		excuteLevelDownable();
		excuteIncomingSaveSatisfaction();
		excuteOutgoingSaveSatisfaction();
		excuteBelongSave();
		excuteCustomerRemoveAbleByCsr();
		excuteViewOrderSpanDept();
		excuteViewRecordSpanDept();
		excutePauseExtenOnLogin();
		excutePauseExtenOnPopupWindow();
		excutePopupWinPauseLogCreatable();
		excutePopupWinCallAfterLogCreatable();
		excuteSettingBlacklistByOutline();
		executeCreateCustomerServiceRecordByExtenType();
		executeSettingGlobalEmail();
		executeSettingDiskSpaceDeleteSoundFile();
	}
	
	/**
	 * 取消基础配置结果
	 */
	private void cancelBasicConfig(){
		if(levelDownableConfiguration == null || !levelDownableConfiguration.getValue()) {
			levelDownableComboBox.setValue(false);
		} else {
			levelDownableComboBox.setValue(true);
		}
		
		if(incomingSatisfactionConfiguration == null || incomingSatisfactionConfiguration.getValue()) {
			incomingSatisfactionComboBox.setValue(true);
		} else {
			incomingSatisfactionComboBox.setValue(false);
		}
		
		if(outgoingSatisfactionConfiguration == null || outgoingSatisfactionConfiguration.getValue()) {
			outgoingSatisfactionComboBox.setValue(true);
		} else {
			outgoingSatisfactionComboBox.setValue(false);
		}
		
		if(belongConfiguration == null || belongConfiguration.getValue()==null) {
			belongComboBox.setValue(InnerConfigManagement.NOBELONG);
		} else if( belongConfiguration.getValue()==false){
			belongComboBox.setValue(InnerConfigManagement.ZEROBELONG);
		}else{
			belongComboBox.setValue(InnerConfigManagement.POSTCODEBELONG);
		}
		
		if(customerRemoveAbleByCsrConfiguration == null || customerRemoveAbleByCsrConfiguration.getValue()) {
			customerRemoveAbleByCsrComboBox.setValue(true);
		} else {
			customerRemoveAbleByCsrComboBox.setValue(false);
		}
		
		if(viewRecordSpanDeptConfiguration == null || viewRecordSpanDeptConfiguration.getValue() == null) {
			viewRecordSpanDeptComboBox.setValue("global");
		} else {
			viewRecordSpanDeptComboBox.setValue(viewRecordSpanDeptConfiguration.getValue());
		}
		
		if(settingBlacklistByOutlineConfiguration == null || !settingBlacklistByOutlineConfiguration.getValue()) {
			settingBlacklistByOutlineComboBox.setValue(false);	
		} else {
			settingBlacklistByOutlineComboBox.setValue(true);
		}
		
		if(createCustomerServiceRecordByExtenTypeConfiguration == null || !createCustomerServiceRecordByExtenTypeConfiguration.getValue()){
			createCustomerServiceRecordByExtenTypeComboBox.setValue(false);
		} else {
			createCustomerServiceRecordByExtenTypeComboBox.setValue(true);
		}
		
		if(settingGlobalEmailConfiguration == null || !settingGlobalEmailConfiguration.getValue()) {
			settingGlobalEmailComboBox.setValue(false);
		} else {
			settingGlobalEmailComboBox.setValue(true);
		}
		
		if(settingDiskSpaceDeleteSoundFileConfiguration == null || settingDiskSpaceDeleteSoundFileConfiguration.getValue()) {
			settingDiskSpaceDeleteSoundFileComboBox.setValue(true);
		} else {
			settingDiskSpaceDeleteSoundFileComboBox.setValue(false);
		}
		
		if(viewOrderSpanDeptConfiguration == null || viewOrderSpanDeptConfiguration.getValue() == null) {
			viewOrderSpanDeptComboBox.setValue("global");
		} else {
			viewOrderSpanDeptComboBox.setValue(viewOrderSpanDeptConfiguration.getValue());
		}
		
		if(pauseExtenOnLoginConfiguration == null || !pauseExtenOnLoginConfiguration.getValue()) {
			pauseExtenOnLoginComboBox.setValue(false);	// 默认是不置忙
		} else {
			pauseExtenOnLoginComboBox.setValue(true);
		}
		
		if(pauseExtenOnPopupWindowConfiguration == null || !pauseExtenOnPopupWindowConfiguration.getValue()) {
			pauseExtenOnPopupWindowComboBox.setValue(false);	// 默认是不置忙
		} else {
			pauseExtenOnPopupWindowComboBox.setValue(true);
		}
		
		if(popupWinPauseLogCreatableConfiguration == null || popupWinPauseLogCreatableConfiguration.getValue()) {
			popupWinPauseLogCreatableComboBox.setValue(true);	// 默认是创建置忙记录
		} else {
			popupWinPauseLogCreatableComboBox.setValue(false);
		}
		
		if(popupWinCallAfteLogCreatableConfiguration == null || popupWinCallAfteLogCreatableConfiguration.getValue()) {
			popupWinCallAfterLogCreatableComboBox.setValue(true);	// 默认是创建置忙记录
		} else {
			popupWinCallAfterLogCreatableComboBox.setValue(false);
		}
		
	}
	
	
	/**
	 * jrh 客户等级可降低配置
	 * 由ButtonClick 调用
	 */
	public void excuteLevelDownable() {
		Boolean downable = (Boolean) levelDownableComboBox.getValue();
		if(levelDownableConfiguration == null) {	// 这样是为了解决情况：当数据中某个内部配置项为空，而多个管理员同时操作内部配置时，可能导致数据库出现多条配置信息 
			levelDownableConfiguration =  ec2ConfigurationService.getByKey("customer_level_down_able", domain.getId());
		}
		if(levelDownableConfiguration == null) {
			levelDownableConfiguration = new Ec2Configuration();
			levelDownableConfiguration.setDomain(domain);
			levelDownableConfiguration.setKey("customer_level_down_able");
			levelDownableConfiguration.setDescription("客户等级可以降低");
		}
		levelDownableConfiguration.setValue(downable);
		levelDownableConfiguration = ec2ConfigurationService.update(levelDownableConfiguration);
		
		// 更新内存中的配置信息
		ShareData.domainToConfigs.get(domain.getId()).put("customer_level_down_able", downable);
	}
	
	/**
	 * chb 满意度调查配置
	 * 由ButtonClick 调用
	 */
	public void excuteIncomingSaveSatisfaction() {
		Boolean satisfaction = (Boolean) incomingSatisfactionComboBox.getValue();
		if(incomingSatisfactionConfiguration == null) {	// 这样是为了解决情况：当数据中某个内部配置项为空，而多个管理员同时操作内部配置时，可能导致数据库出现多条配置信息 
			incomingSatisfactionConfiguration =  ec2ConfigurationService.getByKey("incoming_sati_config", domain.getId());
		}
		if(incomingSatisfactionConfiguration == null) {
			incomingSatisfactionConfiguration = new Ec2Configuration();
			incomingSatisfactionConfiguration.setDomain(domain);
			incomingSatisfactionConfiguration.setKey("incoming_sati_config");
			incomingSatisfactionConfiguration.setDescription("呼入满意度调查");
		}
		incomingSatisfactionConfiguration.setValue(satisfaction);
		incomingSatisfactionConfiguration = ec2ConfigurationService.update(incomingSatisfactionConfiguration);
		
		// 更新内存中的配置信息
		ShareData.domainToConfigs.get(domain.getId()).put("incoming_sati_config", satisfaction);
	}
	

	
	/**
	 * chb 满意度调查配置
	 * 由ButtonClick 调用
	 */
	public void excuteOutgoingSaveSatisfaction() {
		Boolean satisfaction = (Boolean) outgoingSatisfactionComboBox.getValue();
		if(outgoingSatisfactionConfiguration == null) {	// 这样是为了解决情况：当数据中某个内部配置项为空，而多个管理员同时操作内部配置时，可能导致数据库出现多条配置信息 
			outgoingSatisfactionConfiguration =  ec2ConfigurationService.getByKey("outgoing_sati_config", domain.getId());
		}
		if(outgoingSatisfactionConfiguration == null) {
			outgoingSatisfactionConfiguration = new Ec2Configuration();
			outgoingSatisfactionConfiguration.setDomain(domain);
			outgoingSatisfactionConfiguration.setKey("outgoing_sati_config");
			outgoingSatisfactionConfiguration.setDescription("呼出满意度调查");
		}
		outgoingSatisfactionConfiguration.setValue(satisfaction);
		outgoingSatisfactionConfiguration = ec2ConfigurationService.update(outgoingSatisfactionConfiguration);
		
		// 更新内存中的配置信息
		ShareData.domainToConfigs.get(domain.getId()).put("outgoing_sati_config", satisfaction);
	}
	

	/**
	 * chb 归属地识别配置
	 * 由ButtonClick 调用
	 */
	public void excuteBelongSave() {
		//将用户的选择转换为boolean值
		Boolean belong =  null;
		if(belongComboBox.getValue()==InnerConfigManagement.NOBELONG){
			belong=null;
		}else if(belongComboBox.getValue()==InnerConfigManagement.ZEROBELONG){
			belong=false;
		}else if(belongComboBox.getValue()==InnerConfigManagement.POSTCODEBELONG){
			belong=true;
		}

		if(belongConfiguration == null) {	// 这样是为了解决情况：当数据中某个内部配置项为空，而多个管理员同时操作内部配置时，可能导致数据库出现多条配置信息 
			belongConfiguration =  ec2ConfigurationService.getByKey("belong_config", domain.getId());
		}
		if(belongConfiguration == null) {
			belongConfiguration = new Ec2Configuration();
			belongConfiguration.setDomain(domain);
			belongConfiguration.setKey("belong_config");
			belongConfiguration.setDescription("归属地null关闭false加0true加区号");
		}
		
		belongConfiguration.setValue(belong);
		
		if(belongConfiguration.getValue()==null){
			String sql="delete from ec2_config where key='belong_config'";
			commonService.excuteNativeSql(sql, ExecuteType.UPDATE);
		}else{
			belongConfiguration = ec2ConfigurationService.update(belongConfiguration);
		}
		
		// 更新内存中的配置信息
		if(belong==null){
			ShareData.domainToConfigs.get(domain.getId()).remove("belong_config");
		}else{
			ShareData.domainToConfigs.get(domain.getId()).put("belong_config", belong);
		}
	}
	
	/**
	 * 高级配置信息
	 * @return
	 */
	private Component buildAdvanceConfigLayout() {
		Panel panel=new Panel("高级配置");
		
		//约束组件
		VerticalLayout constraintLayout=new VerticalLayout();
		constraintLayout.setWidth("100%");
		panel.addComponent(constraintLayout);
		
		GridLayout gridLayout = new GridLayout(1,1);
		gridLayout.setWidth("100%");
		gridLayout.setSpacing(true);
		constraintLayout.addComponent(gridLayout);
		
		//管理员关键字组件
		int column=0;
		gridLayout.setRows(column+1);
		gridLayout.addComponent(buildKeywordConfigLayout(), 0, column);
		
		//资源等级
		column++;
		gridLayout.setRows(column+1);
		HorizontalLayout levelVerticalLayout=new HorizontalLayout();
		levelVerticalLayout.addComponent(buildLevelConfigLayout());
		levelEditLayout = buildLevelEditLayout();
		levelEditButtonLayout = buildLevelEditButtonLayout();
		levelEditPlaceHolderLayout = new VerticalLayout();
		levelVerticalLayout.addComponent(levelEditPlaceHolderLayout);
		gridLayout.addComponent(levelVerticalLayout, 0, column);
		
		return panel;
	}


	/**
	 * 创建管理员配置界面输出
	 * 
	 * @return
	 */
	private HorizontalLayout buildKeywordConfigLayout() {
		HorizontalLayout configLayout = new HorizontalLayout();
		configLayout.setSpacing(true);
		configLayout.setMargin(true);
		
		//--------------------------管理员管理的字段的组件
		VerticalLayout verticalLayout=new VerticalLayout();
		configLayout.addComponent(verticalLayout);
		
		keywordConfigSelect = new ListSelect("管理员Excel字段");
		keywordConfigKeywordContainer = new BeanItemContainer<TableKeyword>(TableKeyword.class);
		keywordConfigSelect.setContainerDataSource(keywordConfigKeywordContainer);
		keywordConfigSelect.setItemCaptionPropertyId("columnName");
		keywordConfigSelect.setMultiSelect(false);
		keywordConfigSelect.setNullSelectionAllowed(false);
		keywordConfigSelect.setColumns(18);
		keywordConfigSelect.setHeight("8em");
		verticalLayout.addComponent(keywordConfigSelect);

		// 添加可以输入的组件
		HorizontalLayout buttonsLayout = new HorizontalLayout();
		keywordConfigInputField = new TextField();
		keywordConfigInputField.setInputPrompt("要添加的表头字段");
		keywordConfigInputField.setImmediate(true);
		keywordConfigInputField.setWidth("8em");
		buttonsLayout.addComponent(keywordConfigInputField);

		// 按钮组件
		buttonsLayout.setSpacing(true);
		keywordConfigAdd = new Button("添加");
		keywordConfigAdd.addListener((Button.ClickListener)this);
		buttonsLayout.addComponent(keywordConfigAdd);

		keywordConfigDelete = new Button("删除");
		keywordConfigDelete.addListener((Button.ClickListener)this);
		buttonsLayout.addComponent(keywordConfigDelete);
		verticalLayout.addComponent(buttonsLayout);
		
		//--------------------------管理员Excel
		VerticalLayout innerConfigLayout = new VerticalLayout();
//		innerConfigLayout.setMargin(true);
		// 默任关键字的集合
		List<String> defaultKeywords = new ArrayList<String>();
		for (TableKeywordDefault kewords : TableKeywordDefault.values()) {
			String keywordStr = kewords.getName();
			defaultKeywords.add(keywordStr);
		}
		// 默认字段的组件
		ListSelect defaultKeywordSelect = new ListSelect("默认Excel字段(不能修改)",defaultKeywords);
		defaultKeywordSelect.setNullSelectionAllowed(false);
		defaultKeywordSelect.setColumns(18);
		defaultKeywordSelect.setHeight("8em");
		innerConfigLayout.addComponent(defaultKeywordSelect);
		configLayout.addComponent(innerConfigLayout);
		
		return configLayout;
	}
	
	/**
	 * 更新管理员管理的Excel字段内容
	 */
	public void updateTableKeywords() {
		super.attach();
		List<TableKeyword> tableKeyWords = keywordService
				.getAllByDomain(SpringContextHolder.getDomain());
		// 清空List的数据源
		keywordConfigKeywordContainer.removeAllItems();

		// 添加到List的数据源
		for (TableKeyword keyWord : tableKeyWords) {
			keywordConfigKeywordContainer.addBean(keyWord);
		}
		
	}
	
	/**
	 * 更新客服记录结果配置组件的信息
	 */
	public void updateCustomerServiceRecordLevel() {
		// 更新客服记录结果配置组件的信息
		List<CustomerLevel> levelList = customerLevelService
				.getAll(SpringContextHolder.getDomain());
		// 清空List的数据源
		levelContainer.removeAllItems();
		
		// 添加到List的数据源
		for (CustomerLevel level : levelList) {
			levelContainer.addBean(level);
		}
	}

	
	
	private void initService() {
		keywordService = SpringContextHolder.getBean("tableKeywordService");
		customerLevelService = SpringContextHolder.getBean("customerLevelService");
		commonService = SpringContextHolder.getBean("commonService");
		ec2ConfigurationService = SpringContextHolder.getBean("ec2ConfigurationService");
		mailConfigService = SpringContextHolder.getBean("mailConfigService");
		domainService = SpringContextHolder.getBean("domainService");
		domain = SpringContextHolder.getDomain();
		loginUser = SpringContextHolder.getLoginUser();
	}


	/**
	 * 创建资源等级输出
	 * 
	 * @return
	 */
	private VerticalLayout buildLevelConfigLayout() {
		VerticalLayout configLayout = new VerticalLayout();
		configLayout.setSpacing(true);
		configLayout.setMargin(true);
		
		// 管理员管理的字段的组件
		levelSelect = new ListSelect("管理员资源等级");
		levelSelect.setColumns(18);
		levelSelect.setHeight("8em");
		levelContainer = new BeanItemContainer<CustomerLevel>(CustomerLevel.class);
		levelSelect.setContainerDataSource(levelContainer);
		levelSelect.setItemCaptionPropertyId("levelName");
		levelSelect.setImmediate(true);
		levelSelect.setMultiSelect(false);
		levelSelect.addListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				if(levelSelect.getValue()==null){
					InnerConfigManagement.this.updateLevelEditLayout(null,false);
				}else{
					InnerConfigManagement.this.updateLevelEditLayout((CustomerLevel)levelSelect.getValue(),false);
				}
			}
		});
		levelSelect.setImmediate(true);
		configLayout.addComponent(levelSelect);
		
		// 按钮组件
		HorizontalLayout buttonsLayout = new HorizontalLayout();
		buttonsLayout.setSpacing(true);
		addLevel = new Button("添加");
		addLevel.addListener((Button.ClickListener)this);
		buttonsLayout.addComponent(addLevel);
		
		editLevel = new Button("编辑");
		editLevel.addListener((Button.ClickListener)this);
		buttonsLayout.addComponent(editLevel);
		
		deleteLevel = new Button("删除");
		deleteLevel.addListener((Button.ClickListener)this);
		buttonsLayout.addComponent(deleteLevel);
		configLayout.addComponent(buttonsLayout);
		
		return configLayout;
	}

	/**
	 * 创建编辑客户级别
	 * 
	 * @return
	 */
	private VerticalLayout buildLevelEditLayout() {
		VerticalLayout constaintLayout = new VerticalLayout();
		constaintLayout.setSpacing(true);
		constaintLayout.setMargin(true);
		constaintLayout.setCaption("客户级别");
		
		// 添加级别名称
		levelField = new TextField();
		levelField.setImmediate(true);
		levelField.setWidth("10em");
		levelField.setInputPrompt("请输入级别名称");
		levelField.setNullRepresentation("");
		constaintLayout.addComponent(levelField);
		
		//级别
		levelComboBox=new ComboBox("级别");
		levelComboBox.setWidth("10em");
		for(int i=1;i<=10;i++){
			levelComboBox.addItem(i);
		}
		levelComboBox.setImmediate(true);
		levelComboBox.setInputPrompt("请选择等级");
		levelComboBox.setDescription("<B>数字越大，等级越高</B>");
		constaintLayout.addComponent(levelComboBox);

		//保护期
		protectTimeComboBox=new ComboBox("保护期(天)");
		for(int i=10;i<=1000;i++){
			protectTimeComboBox.addItem(i);
		}
		protectTimeComboBox.setInputPrompt("请选择有效天数");
		protectTimeComboBox.setImmediate(true);
		protectTimeComboBox.setWidth("10em");
		constaintLayout.addComponent(protectTimeComboBox);
		
		return constaintLayout;
	}

	/**
	 * 编辑外呼级别按钮组件
	 * @return
	 */
	private HorizontalLayout buildLevelEditButtonLayout() {
		HorizontalLayout buttonsLayout=new HorizontalLayout();
		saveLevel=new Button("保存");
		saveLevel.addListener((Button.ClickListener)this);
		buttonsLayout.addComponent(saveLevel);
		cancelLevel=new Button("取消");
		cancelLevel.addListener((Button.ClickListener)this);
		buttonsLayout.addComponent(cancelLevel);
		return buttonsLayout;
	}
	

	
	
	
	/**
	 * jrh 
	 * 由ButtonClick 调用
	 */
	public void excuteCustomerRemoveAbleByCsr() {
		Boolean satisfaction = (Boolean) customerRemoveAbleByCsrComboBox.getValue();
		if(customerRemoveAbleByCsrConfiguration == null) {	// 这样是为了解决情况：当数据中某个内部配置项为空，而多个管理员同时操作内部配置时，可能导致数据库出现多条配置信息 
			customerRemoveAbleByCsrConfiguration =  ec2ConfigurationService.getByKey("customer_remove_able_by_csr", domain.getId());
		}
		if(customerRemoveAbleByCsrConfiguration == null) {
			customerRemoveAbleByCsrConfiguration = new Ec2Configuration();
			customerRemoveAbleByCsrConfiguration.setDomain(domain);
			customerRemoveAbleByCsrConfiguration.setKey("customer_remove_able_by_csr");
			customerRemoveAbleByCsrConfiguration.setDescription("话务员可以将已经与自己成交的客户从‘我的客户’中移除");
		}
		customerRemoveAbleByCsrConfiguration.setValue(satisfaction);
		customerRemoveAbleByCsrConfiguration = ec2ConfigurationService.update(customerRemoveAbleByCsrConfiguration);
		
		// 更新内存中的配置信息
		ShareData.domainToConfigs.get(domain.getId()).put("customer_remove_able_by_csr", satisfaction);
	}
	
	/**
	 * jrh 
	 * 由ButtonClick 调用
	 */
	public void excuteViewRecordSpanDept() {
		Boolean viewSpan = null;
		if(!"global".equals(viewRecordSpanDeptComboBox.getValue())) {
			viewSpan = (Boolean) viewRecordSpanDeptComboBox.getValue();
		} 
		if(viewRecordSpanDeptConfiguration == null) {	// 这样是为了解决情况：当数据中某个内部配置项为空，而多个管理员同时操作内部配置时，可能导致数据库出现多条配置信息 
			viewRecordSpanDeptConfiguration =  ec2ConfigurationService.getByKey("csr_view_record_span_department", domain.getId());
		}
		if(viewRecordSpanDeptConfiguration == null) {
			viewRecordSpanDeptConfiguration = new Ec2Configuration();
			viewRecordSpanDeptConfiguration.setDomain(domain);
			viewRecordSpanDeptConfiguration.setKey("csr_view_record_span_department");
			viewRecordSpanDeptConfiguration.setDescription("设定坐席针对单个客户的历史客服记录的查看范围, null: 全局范围 ，true：单个部门，false：单个坐席");
		}
		viewRecordSpanDeptConfiguration.setValue(viewSpan);
		
		// 更新内存中的配置信息
		if(viewSpan == null){
			if(viewRecordSpanDeptConfiguration.getId() != null) {
				ec2ConfigurationService.deleteById(viewRecordSpanDeptConfiguration.getId());
			}
			viewRecordSpanDeptConfiguration = null;
			ShareData.domainToConfigs.get(domain.getId()).remove("csr_view_record_span_department");
		} else {
			viewRecordSpanDeptConfiguration = ec2ConfigurationService.update(viewRecordSpanDeptConfiguration);
			ShareData.domainToConfigs.get(domain.getId()).put("csr_view_record_span_department", viewSpan);
		}
	}
	
	/**
	 * jrh 
	 * 由ButtonClick 调用
	 */
	public void excuteSettingBlacklistByOutline() {
		Boolean isBlacklistByOutline = (Boolean) settingBlacklistByOutlineComboBox.getValue();
		if(settingBlacklistByOutlineConfiguration == null) {	// 这样是为了解决情况：当数据中某个内部配置项为空，而多个管理员同时操作内部配置时，可能导致数据库出现多条配置信息 
			settingBlacklistByOutlineConfiguration =  ec2ConfigurationService.getByKey("setting_blacklist_by_outline", domain.getId());
		}
		if(settingBlacklistByOutlineConfiguration == null) {
			settingBlacklistByOutlineConfiguration = new Ec2Configuration();
			settingBlacklistByOutlineConfiguration.setDomain(domain);
			settingBlacklistByOutlineConfiguration.setKey("setting_blacklist_by_outline");
			settingBlacklistByOutlineConfiguration.setDescription("如果不开启，全局范围内都使用相同的黑名单，否则按外线进行区分。 true：按外线区分，false：全局共用");
		}
		settingBlacklistByOutlineConfiguration.setValue(isBlacklistByOutline);
		settingBlacklistByOutlineConfiguration = ec2ConfigurationService.update(settingBlacklistByOutlineConfiguration);
		
		// 更新内存中的配置信息
		ShareData.domainToConfigs.get(domain.getId()).put("setting_blacklist_by_outline", isBlacklistByOutline);
	}
	
	/**
	 * jinht
	 * 由ButtonClick调用 
	 */
	public void executeCreateCustomerServiceRecordByExtenType(){	// TODO JHT
		Boolean isCustomerServiceRecordByExtenType = (Boolean) createCustomerServiceRecordByExtenTypeComboBox.getValue();
		if(createCustomerServiceRecordByExtenTypeConfiguration == null){
			createCustomerServiceRecordByExtenTypeConfiguration = ec2ConfigurationService.getByKey("create_customer_service_record_by_exten_type", domain.getId());
		}
		if(createCustomerServiceRecordByExtenTypeConfiguration == null){
			createCustomerServiceRecordByExtenTypeConfiguration = new Ec2Configuration();
			createCustomerServiceRecordByExtenTypeConfiguration.setDomain(domain);
			createCustomerServiceRecordByExtenTypeConfiguration.setKey("create_customer_service_record_by_exten_type");
			createCustomerServiceRecordByExtenTypeConfiguration.setDescription("如果不开启，没有电脑的座席每次保存客服记录都是根据上次通话进行创建。true：按客户电话创建，false：按上次通话创建");
		}
		createCustomerServiceRecordByExtenTypeConfiguration.setValue(isCustomerServiceRecordByExtenType);
		createCustomerServiceRecordByExtenTypeConfiguration = ec2ConfigurationService.update(createCustomerServiceRecordByExtenTypeConfiguration);

		// 更新内存中的配置信息
		ShareData.domainToConfigs.get(domain.getId()).put("create_customer_service_record_by_exten_type", isCustomerServiceRecordByExtenType);
	}
	
	/**
	 * jinht
	 * 由 ButtonClick 调用
	 */
	public void executeSettingGlobalEmail() {
		Boolean isSettingGlobalEmail = (Boolean) settingGlobalEmailComboBox.getValue();
		if(settingGlobalEmailConfiguration == null) {
			settingGlobalEmailConfiguration = ec2ConfigurationService.getByKey("setting_global_email", domain.getId());
		}
		if(settingGlobalEmailConfiguration == null) {
			settingGlobalEmailConfiguration = new Ec2Configuration();
			settingGlobalEmailConfiguration.setDomain(domain);
			settingGlobalEmailConfiguration.setKey("setting_global_email");
			settingGlobalEmailConfiguration.setDescription("如果开启，则表示全局使用统一配置的管理员的邮箱帐号密码进行发送邮件；否则，表示每个人都应该配置自己的个人邮件进行发送邮件");
		}
		
		/** 如果设置全局使用同一管理员邮箱进行发送邮件时，这里就需要先看下管理员是否已经配置了个人的邮箱帐号以及密码，如果没有，则返回说明无法设置全局使用同意邮箱发送邮件 */
		if(isSettingGlobalEmail != null && isSettingGlobalEmail == true) {	
			MailConfig mailConfig = mailConfigService.getMailConfigByUser(loginUser);
			StringBuffer sbMsg = new StringBuffer();
			sbMsg.append("配置全局使用统一管理员邮箱进行发送邮件，所以您必须配置自己的发送邮件信息！");
			if(mailConfig == null) {
				this.getApplication().getMainWindow().showNotification(sbMsg.toString(), Notification.TYPE_WARNING_MESSAGE);
				return;
			}
			sbMsg.setLength(0);
			sbMsg.append("[ ");
			if(mailConfig.getSmtpHost() == null) {
				sbMsg.append("邮件服务器地址 ");
			}
			if(mailConfig.getSmtpPort() == null) {
				sbMsg.append("邮件服务器端口 ");
			}
			if(mailConfig.getSenderPassword() == null) {
				sbMsg.append("邮箱密码 ");
			}
			sbMsg.append("]");
			if(sbMsg.length() > 3) {
				this.getApplication().getMainWindow().showNotification("您的邮箱配置"+sbMsg.toString()+"都必须填写！", Notification.TYPE_WARNING_MESSAGE);
				return;
			}
		}
		settingGlobalEmailConfiguration.setValue(isSettingGlobalEmail);
		settingGlobalEmailConfiguration = ec2ConfigurationService.update(settingGlobalEmailConfiguration);
		
		// 更新内存中的配置信息
		ShareData.domainToConfigs.get(domain.getId()).put("setting_global_email", isSettingGlobalEmail);
	}
	
	/**
	 * jinht
	 * 由 ButtonClick 调用
	 */
	public void executeSettingDiskSpaceDeleteSoundFile() {
		Boolean isSettingDiskSpaceDeleteSoundFile = (Boolean) settingDiskSpaceDeleteSoundFileComboBox.getValue();
		if(settingDiskSpaceDeleteSoundFileConfiguration == null) {
			settingDiskSpaceDeleteSoundFileConfiguration = ec2ConfigurationService.getByKey("setting_diskspace_delete_soundfile", domain.getId());
		}
		
		if(domainService.getAll().size() > 1) {
			this.getApplication().getMainWindow().showNotification("您现在使用的是多租户系统, 不支持该功能. 默认为自动删除录音!", Notification.TYPE_WARNING_MESSAGE);
			return;
		}
		
		if(settingDiskSpaceDeleteSoundFileConfiguration == null) {
			settingDiskSpaceDeleteSoundFileConfiguration = new Ec2Configuration();
			settingDiskSpaceDeleteSoundFileConfiguration.setDomain(domain);
			settingDiskSpaceDeleteSoundFileConfiguration.setKey("setting_diskspace_delete_soundfile");
			settingDiskSpaceDeleteSoundFileConfiguration.setDescription("设置系统磁盘使用超过 90% 时, 是否自动删除录音，true：是，false：否");
		}
		
		settingDiskSpaceDeleteSoundFileConfiguration.setValue(isSettingDiskSpaceDeleteSoundFile);
		settingDiskSpaceDeleteSoundFileConfiguration = ec2ConfigurationService.update(settingDiskSpaceDeleteSoundFileConfiguration);
		
		// 更新内存中的配置信息
		ShareData.domainToConfigs.get(domain.getId()).put("setting_diskspace_delete_soundfile", isSettingDiskSpaceDeleteSoundFile);
	}
	
	/**
	 * jrh 
	 * 由ButtonClick 调用
	 */
	public void excuteViewOrderSpanDept() {
		Boolean viewSpan = null;
		if(!"global".equals(viewOrderSpanDeptComboBox.getValue())) {
			viewSpan = (Boolean) viewOrderSpanDeptComboBox.getValue();
		} 
		if(viewOrderSpanDeptConfiguration == null) {	// 这样是为了解决情况：当数据中某个内部配置项为空，而多个管理员同时操作内部配置时，可能导致数据库出现多条配置信息 
			viewOrderSpanDeptConfiguration =  ec2ConfigurationService.getByKey("csr_view_order_span_department", domain.getId());
		}
		if(viewOrderSpanDeptConfiguration == null) {
			viewOrderSpanDeptConfiguration = new Ec2Configuration();
			viewOrderSpanDeptConfiguration.setDomain(domain);
			viewOrderSpanDeptConfiguration.setKey("csr_view_order_span_department");
			viewOrderSpanDeptConfiguration.setDescription("设定坐席针对单个客户的历史订单的查看范围, null: 全局范围 ，true：单个部门，false：单个坐席");
		}
		viewOrderSpanDeptConfiguration.setValue(viewSpan);
		
		// 更新内存中的配置信息
		if(viewSpan == null){
			if(viewOrderSpanDeptConfiguration.getId() != null) {
				ec2ConfigurationService.deleteById(viewOrderSpanDeptConfiguration.getId());
			}
			viewOrderSpanDeptConfiguration = null;
			ShareData.domainToConfigs.get(domain.getId()).remove("csr_view_order_span_department");
		} else {
			viewOrderSpanDeptConfiguration = ec2ConfigurationService.update(viewOrderSpanDeptConfiguration);
			ShareData.domainToConfigs.get(domain.getId()).put("csr_view_order_span_department", viewSpan);
		}
	}

	/**
	 * jrh 
	 * 由ButtonClick 调用
	 */
	public void excutePauseExtenOnLogin() {
		Boolean ispause = (Boolean) pauseExtenOnLoginComboBox.getValue();
		if(pauseExtenOnLoginConfiguration == null) {	// 这样是为了解决情况：当数据中某个内部配置项为空，而多个管理员同时操作内部配置时，可能导致数据库出现多条配置信息 
			pauseExtenOnLoginConfiguration =  ec2ConfigurationService.getByKey("pasue_exten_after_csr_login", domain.getId());
		}
		if(pauseExtenOnLoginConfiguration == null) {
			pauseExtenOnLoginConfiguration = new Ec2Configuration();
			pauseExtenOnLoginConfiguration.setDomain(domain);
			pauseExtenOnLoginConfiguration.setKey("pasue_exten_after_csr_login");
			pauseExtenOnLoginConfiguration.setDescription("设置用户登录后，是否默认将分机置忙。 true：置忙，false：不置忙(即置闲)");
		}
		pauseExtenOnLoginConfiguration.setValue(ispause);
		pauseExtenOnLoginConfiguration = ec2ConfigurationService.update(pauseExtenOnLoginConfiguration);
		
		// 更新内存中的配置信息
		ShareData.domainToConfigs.get(domain.getId()).put("pasue_exten_after_csr_login", ispause);
	}

	/**
	 * jrh 
	 * 由ButtonClick 调用
	 */
	public void excutePauseExtenOnPopupWindow() {
		Boolean ispause = (Boolean) pauseExtenOnPopupWindowComboBox.getValue();
		if(pauseExtenOnPopupWindowConfiguration == null) {	// 这样是为了解决情况：当数据中某个内部配置项为空，而多个管理员同时操作内部配置时，可能导致数据库出现多条配置信息 
			pauseExtenOnPopupWindowConfiguration =  ec2ConfigurationService.getByKey("pasue_exten_after_csr_popup_calling_window", domain.getId());
		}
		if(pauseExtenOnPopupWindowConfiguration == null) {
			pauseExtenOnPopupWindowConfiguration = new Ec2Configuration();
			pauseExtenOnPopupWindowConfiguration.setDomain(domain);
			pauseExtenOnPopupWindowConfiguration.setKey("pasue_exten_after_csr_popup_calling_window");
			pauseExtenOnPopupWindowConfiguration.setDescription("设置呼入、呼出弹屏后，是否默认将分机置忙，在弹屏关闭后将自动将分机置闲。 true：置忙，false：不置忙");
		}
		pauseExtenOnPopupWindowConfiguration.setValue(ispause);
		pauseExtenOnPopupWindowConfiguration = ec2ConfigurationService.update(pauseExtenOnPopupWindowConfiguration);
		
		// 更新内存中的配置信息
		ShareData.domainToConfigs.get(domain.getId()).put("pasue_exten_after_csr_popup_calling_window", ispause);
	}
	
	/**
	 * jrh 
	 * 由ButtonClick 调用
	 */
	public void excutePopupWinPauseLogCreatable() {
		Boolean isLogCreatable = (Boolean) popupWinPauseLogCreatableComboBox.getValue();
		if(popupWinPauseLogCreatableConfiguration == null) {	// 这样是为了解决情况：当数据中某个内部配置项为空，而多个管理员同时操作内部配置时，可能导致数据库出现多条配置信息 
			popupWinPauseLogCreatableConfiguration =  ec2ConfigurationService.getByKey("create_pause_log_after_csr_popup_calling_window", domain.getId());
		}
		if(popupWinPauseLogCreatableConfiguration == null) {
			popupWinPauseLogCreatableConfiguration = new Ec2Configuration();
			popupWinPauseLogCreatableConfiguration.setDomain(domain);
			popupWinPauseLogCreatableConfiguration.setKey("create_pause_log_after_csr_popup_calling_window");
			popupWinPauseLogCreatableConfiguration.setDescription("设置呼入、呼出弹屏后，如果将分机置忙，则是否需要同时创建置忙置闲记录。 true：创建，false：不创建");
		}
		popupWinPauseLogCreatableConfiguration.setValue(isLogCreatable);
		popupWinPauseLogCreatableConfiguration = ec2ConfigurationService.update(popupWinPauseLogCreatableConfiguration);
		
		// 更新内存中的配置信息
		ShareData.domainToConfigs.get(domain.getId()).put("create_pause_log_after_csr_popup_calling_window", isLogCreatable);
	}
	
	/**
	 * jrh 
	 * 由ButtonClick 调用
	 */
	public void excutePopupWinCallAfterLogCreatable() {
		Boolean isLogCreatable = (Boolean) popupWinCallAfterLogCreatableComboBox.getValue();
		if(popupWinCallAfteLogCreatableConfiguration == null) {	// 这样是为了解决情况：当数据中某个内部配置项为空，而多个管理员同时操作内部配置时，可能导致数据库出现多条配置信息 
			popupWinCallAfteLogCreatableConfiguration =  ec2ConfigurationService.getByKey("create_afterCall_log_after_csr_popup_calling_window", domain.getId());
		}
		if(popupWinCallAfteLogCreatableConfiguration == null) {
			popupWinCallAfteLogCreatableConfiguration = new Ec2Configuration();
			popupWinCallAfteLogCreatableConfiguration.setDomain(domain);
			popupWinCallAfteLogCreatableConfiguration.setKey("create_afterCall_log_after_csr_popup_calling_window");
			popupWinCallAfteLogCreatableConfiguration.setDescription("设置呼入、呼出弹屏后，如果将分机置忙，则是否需要同时创建话后处理记录。 true：创建，false：不创建");
		}
		popupWinCallAfteLogCreatableConfiguration.setValue(isLogCreatable);
		popupWinCallAfteLogCreatableConfiguration = ec2ConfigurationService.update(popupWinCallAfteLogCreatableConfiguration);
		
		// 更新内存中的配置信息
		ShareData.domainToConfigs.get(domain.getId()).put("create_afterCall_log_after_csr_popup_calling_window", isLogCreatable);
	}
	
	/**
	 * 为状态输出区域设置数据源
	 * 
	 * @return
	 */
	private void updateLevelEditLayout(CustomerLevel customerLevel,Boolean isEditable) {
		VerticalLayout constaintLayout = new VerticalLayout();
		constaintLayout.setSpacing(true);
		constaintLayout.setMargin(true);
		this.customerLevel=customerLevel;
		//移除所有组件
		levelEditPlaceHolderLayout.removeAllComponents();
		if(customerLevel==null){
			return; //如果状态为null，表示没有选中组件，不显示组件直接返回
		}else if(customerLevel.getLevelName()==null&&isEditable){//如果状态不为null，但是状态取得的状态名为空，说明是新建状态
			levelField.setValue(null);
			levelField.setEnabled(true);
			levelComboBox.setValue(null);
			levelComboBox.setEnabled(true);
			protectTimeComboBox.setValue(null);
			protectTimeComboBox.setEnabled(true);
			levelEditPlaceHolderLayout.addComponent(levelEditLayout);
			levelEditPlaceHolderLayout.addComponent(levelEditButtonLayout);
			return;
		}else if(customerLevel.getLevelName()!=null&&isEditable){//如果状态不为null，但是状态取得的状态名也不为空，说明是编辑状态
			levelField.setValue(customerLevel.getLevelName());
			levelField.setEnabled(true);
			levelComboBox.setValue(customerLevel.getLevel());
			levelComboBox.setEnabled(true);
			protectTimeComboBox.setValue(customerLevel.getProtectDay());
			protectTimeComboBox.setEnabled(true);
			levelEditPlaceHolderLayout.addComponent(levelEditLayout);
			levelEditPlaceHolderLayout.addComponent(levelEditButtonLayout);
			return;
		}else if(!isEditable){//表示不可以编辑，仅可以浏览
			levelField.setValue(customerLevel.getLevelName());
			levelField.setEnabled(false);
			levelComboBox.setValue(customerLevel.getLevel());
			levelComboBox.setEnabled(false);
			protectTimeComboBox.setValue(customerLevel.getProtectDay());
			protectTimeComboBox.setEnabled(false);
			levelEditPlaceHolderLayout.addComponent(levelEditLayout);
			return;
		}
	}



	/**
	 * 删除状态操作
	 */
	private void executeDeleteLevel() {
		CustomerLevel level = (CustomerLevel) levelSelect
				.getValue();
		String nativeSql = "select count(*) from ec2_customer_resource where customerlevel_id="
				+ level.getId() + " and domain_id=" + domain.getId();
		Long count = (Long) commonService.excuteNativeSql(nativeSql,
				ExecuteType.SINGLE_RESULT);
		if (count > 0) {
			NotificationUtil.showWarningNotification(this, "存在这个等级的客户，不允许删除");
			return;
		}
		customerLevelService.deleteById(level.getId());
	}

	
	/**
	 * 显示编辑等级组件
	 */
	private void showEditLevel() {
		CustomerLevel level = (CustomerLevel) levelSelect
				.getValue();
		if (level == null) {
			NotificationUtil.showWarningNotification(this, "请选择要编辑的等级！");
			return;
		}
		this.updateLevelEditLayout(level,true);
	}
	
	/**
	 * 显示添加等级组件
	 */
	private void showAddLevel() {
		levelEditPlaceHolderLayout.removeAllComponents();
		CustomerLevel level=new CustomerLevel();
		this.updateLevelEditLayout(level,true);
	}
	
	/**
	 * 执行保存操作
	 */
	private void executeSaveLevel() {
		String levelName="";
		if(levelField.getValue()!=null){
			levelName=levelField.getValue().toString().trim();
		}
		//等级名为空提示
		if(levelName.equals("")){
			NotificationUtil.showWarningNotification(this, "等级名不能为空");
			return;
		}
		
		Integer level=(Integer)levelComboBox.getValue();
		if(level==null){
			NotificationUtil.showWarningNotification(this, "等级不能为空");
			return;
		}
		
		Integer protectDay=(Integer)protectTimeComboBox.getValue();
		if(protectDay==null){
			NotificationUtil.showWarningNotification(this, "保护期不能为空");
			return;
		}
		
		//此时customerLevel不应该为null
		customerLevel.setDomain(domain);
		customerLevel.setLevel(level);
		customerLevel.setLevelName(levelName);
		customerLevel.setProtectDay(protectDay);
		//更新或者保存状态
		customerLevelService.update(customerLevel);
		updateLevelEditLayout(customerLevel, false);
	}
	
	/**
	 * 点击添加和删除按钮时的事件
	 */
	@Override
	public void buttonClick(ClickEvent event) {
		//基础编辑组件
		if(event.getButton()==basicEditButton){
			setBasicConfigEnabled(true);
			basicButtonsLayout.removeAllComponents();
			basicButtonsLayout.addComponent(basicSaveButton);
			basicButtonsLayout.addComponent(basicCancelButton);
		}else if(event.getButton()==basicSaveButton){
			//保存操作结果
			saveBasicConfig();
			setBasicConfigEnabled(false);
			basicButtonsLayout.removeAllComponents();
			basicButtonsLayout.addComponent(basicEditButton);
		}else if(event.getButton()==basicCancelButton){
			//取消操作结果
			cancelBasicConfig();
			setBasicConfigEnabled(false);
			basicButtonsLayout.removeAllComponents();
			basicButtonsLayout.addComponent(basicEditButton);
		}else if(event.getButton()==keywordConfigDelete){
			TableKeyword keyWord = (TableKeyword) keywordConfigSelect.getValue();
			if(keyWord==null){
				NotificationUtil.showWarningNotification(this, "请先选择要删除的管理员表头字段！");
				return;
			}
			keywordService.deleteTableKeywordById(keyWord.getId());
			NotificationUtil.showWarningNotification(this, "删除成功");
		}else if(event.getButton()==keywordConfigAdd){
			String inputKeyword = null;
			if (keywordConfigInputField.getValue() != null) {
				inputKeyword = keywordConfigInputField.getValue().toString().trim();
			}
			// 如果是空字符串，显示提示信息
			if (StringUtils.isEmpty(inputKeyword)) {
				NotificationUtil.showWarningNotification(this, "表格字段不能为空！");
				return;
			}

			TableKeyword tableKeyword = new TableKeyword();
			tableKeyword.setColumnName(inputKeyword);
			tableKeyword.setDomain(SpringContextHolder.getDomain());
			keywordService.saveTableKeyword(tableKeyword);
			updateTableKeywords();
			NotificationUtil.showWarningNotification(this, "添加成功");
		}
		
		
		Button source = event.getButton();
		if (source == addLevel) {
			showAddLevel();
		} else if (source == deleteLevel) {
			try {
				executeDeleteLevel();
			} catch (Exception e) {
				e.printStackTrace();
				NotificationUtil.showWarningNotification(this, "删除失败,请看是否选择了要删除的对象！");
			}
		} else if (source == editLevel) {
			showEditLevel();
		}else if(source == saveLevel) {
			executeSaveLevel();
		}else if(source == cancelLevel) {
			this.updateLevelEditLayout((CustomerLevel)levelSelect.getValue(),false);
		}
		
		updateCustomerServiceRecordLevel();
	}

	@Override
	public void valueChange(ValueChangeEvent event) {
		Property source = event.getProperty();
		if(source == pauseExtenOnPopupWindowComboBox) {
			Boolean value = (Boolean) pauseExtenOnPopupWindowComboBox.getValue();
			if(value != null) {
				popupWinPauseLogCreatableComboBox.setValue(value); 
				popupWinPauseLogCreatableComboBox.setVisible(value); 
				popupWinCallAfterLogCreatableComboBox.setVisible(value); 
			}
		}
	}

}
