package com.jiangyifen.ec2.ui.mgr.system.tabsheet;

import java.util.ArrayList;
import java.util.List;

import com.jiangyifen.ec2.entity.CustomerLevel;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.Ec2Configuration;
import com.jiangyifen.ec2.entity.TableKeyword;
import com.jiangyifen.ec2.entity.TableKeywordDefault;
import com.jiangyifen.ec2.entity.enumtype.ExecuteType;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.service.common.CommonService;
import com.jiangyifen.ec2.service.eaoservice.CustomerLevelService;
import com.jiangyifen.ec2.service.eaoservice.Ec2ConfigurationService;
import com.jiangyifen.ec2.service.eaoservice.TableKeywordService;
import com.jiangyifen.ec2.ui.util.NotificationUtil;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class CopyOfInnerConfigManagement extends VerticalLayout implements Button.ClickListener/*,Property.ValueChangeListener*/ {
	private static String NOBELONG="NOBELONG";
	private static String ZEROBELONG="ZEROBELONG";
	private static String POSTCODEBELONG="POSTCODEBELONG";
	
	/**
	 * 主要组件
	 */
	private Button add;
	private Button delete;
	private ListSelect keywordSelect;
	private TextField inputField;
	private BeanItemContainer<TableKeyword> keywordContainer;
	private TableKeywordService keywordService;

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
	
	// jrh 等级可降级配置
	private ComboBox levelDownableComboBox;
	private Button editLevelDownable;
	private Button saveLevelDownable;
	private Button cancelLevelDownable;
	private Ec2Configuration levelDownableConfiguration;
	
	// 客户呼入满意度调查配置
	private ComboBox incomingSatisfactionComboBox;
	private Button incomingEditSatisfaction;
	private Button incomingSaveSatisfaction;
	private Button incomingCancelSatisfaction;
	private Ec2Configuration incomingSatisfactionConfiguration;

	// 客户呼出满意度调查配置
	private ComboBox outgoingSatisfactionComboBox;
	private Button outgoingEditSatisfaction;
	private Button outgoingSaveSatisfaction;
	private Button outgoingCancelSatisfaction;
	private Ec2Configuration outgoingSatisfactionConfiguration;

	//======  号码归属地识别  ======//
	private ComboBox belongComboBox; //null是不开启，false是加0，true是加区号
	private Button belongEdit;
	private Button belongSave;
	private Button belongCancel;
	private Ec2Configuration belongConfiguration;
	
	// jrh 【话务员是否可以将已经与自己成交的客户，从‘我的客户’中移除】
	private ComboBox customerRemoveAbleByCsrComboBox;
	private Button editCustomerRemoveAbleByCsr;
	private Button saveCustomerRemoveAbleByCsr;
	private Button cancelCustomerRemoveAbleByCsr;
	private Ec2Configuration customerRemoveAbleByCsrConfiguration;
	
	// jrh 【部门间历史客服记录可见】
	private ComboBox viewRecordSpanDeptComboBox;
	private Button editViewRecordSpanDept;
	private Button saveViewRecordSpanDept;
	private Button cancelViewRecordSpanDept;
	private Ec2Configuration viewRecordSpanDeptConfiguration;
	
	// jrh 【按外线区分黑名单】
	private ComboBox settingBlacklistByOutlineComboBox;
	private Button editSettingBlacklistByOutline;
	private Button saveSettingBlacklistByOutline;
	private Button cancelSettingBlacklistByOutline;
	private Ec2Configuration settingBlacklistByOutlineConfiguration;
	
	// jrh 【部门间历史订单可见】
	private ComboBox viewOrderSpanDeptComboBox;
	private Button editViewOrderSpanDept;
	private Button saveViewOrderSpanDept;
	private Button cancelViewOrderSpanDept;
	private Ec2Configuration viewOrderSpanDeptConfiguration;
	
	// jrh 【登陆分机默认置忙】
	private ComboBox pauseExtenOnLoginComboBox;
	private Button editPauseExtenOnLogin;
	private Button savePauseExtenOnLogin;
	private Button cancelPauseExtenOnLogin;
	private Ec2Configuration pauseExtenOnLoginConfiguration;
	
	// jrh 【呼叫弹屏默认置忙】
	private ComboBox pauseExtenOnPopupWindowComboBox;
	private Button editPauseExtenOnPopupWindow;
	private Button savePauseExtenOnPopupWindow;
	private Button cancelPauseExtenOnPopupWindow;
	private Ec2Configuration pauseExtenOnPopupWindowConfiguration;
	
	private CustomerLevelService customerLevelService;
	private CommonService commonService;
	private Ec2ConfigurationService ec2ConfigurationService;
	private Domain domain;
	
	/**
	 * 编辑状态信息组件
	 */
	private Button saveLevel;
	private Button cancelLevel;
	private CustomerLevel customerLevel;
	/**
	 * 构造器
	 */
	public CopyOfInnerConfigManagement() {
		this.initService();
		this.setWidth("100%");
		this.setMargin(true);
		this.setSpacing(true);

		
		// 约束组件，使组件紧密排列
		HorizontalLayout constrantLayout1 = new HorizontalLayout();
		constrantLayout1.setWidth("100%");
		constrantLayout1.setSpacing(true);
		this.addComponent(constrantLayout1);

		HorizontalLayout layout1 = new HorizontalLayout();
		layout1.setSpacing(true);
		constrantLayout1.addComponent(layout1);

		// 创建左侧和右侧输出，并添加到constrantLayout
		layout1.addComponent(buildConfigLayout());
		layout1.addComponent(buildInnerConfigLayout());
		
		// jrh 2013-12-19 
		KickCsrLogoutConfigView kickCsrLogoutConfigView = new KickCsrLogoutConfigView();
		layout1.addComponent(kickCsrLogoutConfigView);
		
		//============================第二层=========================================//

		// 约束组件，使组件紧密排列
		HorizontalLayout constrantLayout2 = new HorizontalLayout();
		constrantLayout2.setWidth("100%");
		constrantLayout2.setSpacing(true);
		this.addComponent(constrantLayout2);
		
		HorizontalLayout layout2 = new HorizontalLayout();
		layout2.setSpacing(true);
		constrantLayout2.addComponent(layout2);
		
		VerticalLayout verticalLayout2_1=new VerticalLayout();
		verticalLayout2_1.setSpacing(true);
		verticalLayout2_1.addComponent(buildLevelDownableLayout());
		verticalLayout2_1.addComponent(buildCustomerRemoveAble2CsrLayout());
		
		verticalLayout2_1.addComponent(buildIncomingSatisfactionLayout());
		verticalLayout2_1.addComponent(buildOutgoingSatisfactionLayout());
		verticalLayout2_1.addComponent(buildBelongLayout());
		layout2.addComponent(verticalLayout2_1);
		
		//=====================================================================//
		
//		// 约束组件，使组件紧密排列
		VerticalLayout verticalLayout2_2 = new VerticalLayout();
		verticalLayout2_2.setSpacing(true);
		layout2.addComponent(verticalLayout2_2);
		
		//后来添加，资源级别的输出组件
		verticalLayout2_2.addComponent(buildLevelConfigLayout());
		// 创建编辑组件的输出，但是并没有显示出来
		levelEditLayout = buildLevelEditLayout();
		levelEditButtonLayout = buildLevelEditButtonLayout();
		levelEditPlaceHolderLayout = new VerticalLayout();
		layout2.addComponent(levelEditPlaceHolderLayout);
		
		verticalLayout2_2.addComponent(buildViewRecordSpanDeptLayout());
		
		//=============================第三层========================================//

		// 约束组件，使组件紧密排列
		HorizontalLayout constrantLayout3 = new HorizontalLayout();
		constrantLayout3.setWidth("100%");
		constrantLayout3.setSpacing(true);
		this.addComponent(constrantLayout3);
		
		HorizontalLayout layout3 = new HorizontalLayout();
		layout3.setSpacing(true);
		constrantLayout3.addComponent(layout3);
		
		VerticalLayout verticalLayout3_1=new VerticalLayout();
		verticalLayout3_1.setSpacing(true);
		verticalLayout3_1.addComponent(buildSettingBlacklistByOutlineLayout());
		verticalLayout3_1.addComponent(buildPauseExtenOnLoginLayout());
		layout3.addComponent(verticalLayout3_1);
		
		VerticalLayout verticalLayout3_2=new VerticalLayout();
		verticalLayout3_2.setSpacing(true);
		layout3.addComponent(verticalLayout3_2);
		verticalLayout3_2.addComponent(buildViewOrderSpanDeptLayout());
		verticalLayout3_2.addComponent(buildPauseExtenOnPopupWindowLayout());

		
		// 更新信息
		update();
	}

	/**
	 * 更新管理员管理的Excel字段内容
	 */
	public void update() {
		super.attach();
		List<TableKeyword> tableKeyWords = keywordService
				.getAllByDomain(SpringContextHolder.getDomain());
		// 清空List的数据源
		keywordContainer.removeAllItems();

		// 添加到List的数据源
		for (TableKeyword keyWord : tableKeyWords) {
			keywordContainer.addBean(keyWord);
		}

		/** =================== 更新客服记录结果 ========================== **/
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
		domain = SpringContextHolder.getDomain();
	}

	/**
	 * 创建管理员配置界面输出
	 * 
	 * @return
	 */
	private VerticalLayout buildConfigLayout() {
		VerticalLayout configLayout = new VerticalLayout();
		configLayout.setSpacing(true);
		configLayout.setMargin(true);

		// 管理员管理的字段的组件
		keywordSelect = new ListSelect("管理员Excel字段");
		keywordContainer = new BeanItemContainer<TableKeyword>(
				TableKeyword.class);
		keywordSelect.setContainerDataSource(keywordContainer);
		keywordSelect.setItemCaptionPropertyId("columnName");
		keywordSelect.setMultiSelect(false);
		keywordSelect.setColumns(10);
		keywordSelect.setWidth("15em");
		configLayout.addComponent(keywordSelect);

		// 添加可以输入的组件
		inputField = new TextField();
		inputField.setImmediate(true);
		inputField.setWidth("10em");
		configLayout.addComponent(inputField);

		// 按钮组件
		HorizontalLayout buttonsLayout = new HorizontalLayout();
		buttonsLayout.setSpacing(true);
		add = new Button("添加");
		add.addListener((Button.ClickListener)this);
		buttonsLayout.addComponent(add);

		delete = new Button("删除");
		delete.addListener((Button.ClickListener)this);
		buttonsLayout.addComponent(delete);
		configLayout.addComponent(buttonsLayout);

		return configLayout;
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
		levelContainer = new BeanItemContainer<CustomerLevel>(CustomerLevel.class);
		levelSelect.setContainerDataSource(levelContainer);
		levelSelect.setItemCaptionPropertyId("levelName");
		levelSelect.setImmediate(true);
		levelSelect.setMultiSelect(false);
		levelSelect.addListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				if(levelSelect.getValue()==null){
					CopyOfInnerConfigManagement.this.updateLevelEditLayout(null,false);
				}else{
					CopyOfInnerConfigManagement.this.updateLevelEditLayout((CustomerLevel)levelSelect.getValue(),false);
				}
			}
		});
		levelSelect.setImmediate(true);
		levelSelect.setColumns(10);
		levelSelect.setWidth("15em");
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
		for(int i=1;i<=10;i++){
			levelComboBox.addItem(i);
		}
		levelComboBox.setImmediate(true);
		levelComboBox.setWidth("10em");
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
	 * jrh 创建等级可升降配置项
	 * @return
	 */
	private VerticalLayout buildLevelDownableLayout() {
		VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.setSpacing(true);
		
		HorizontalLayout layout1 = new HorizontalLayout();
		layout1.setWidth("100%");
		layout1.setSpacing(true);
		mainLayout.addComponent(layout1);
		
		Label label = new Label("客户等级可降：");
		label.setWidth("-1px");
		label.setDescription("<B>客户的等级是否可以降低，如果不可以，则等级只能持平或升级</B>");
		layout1.addComponent(label);
		
		levelDownableComboBox = new ComboBox();
		levelDownableComboBox.setDescription("<B>客户的等级是否可以降低，如果不可以，则等级只能持平或升级</B>");
		levelDownableComboBox.addItem(false);
		levelDownableComboBox.addItem(true);
		levelDownableComboBox.setItemCaption(false, "否 (只升降)");
		levelDownableComboBox.setItemCaption(true, "是 (可降级)");
		levelDownableComboBox.setNullSelectionAllowed(false);
		levelDownableComboBox.setWidth("139px");
		layout1.addComponent(levelDownableComboBox);
		
		levelDownableConfiguration =  ec2ConfigurationService.getByKey("customer_level_down_able", domain.getId());
		if(levelDownableConfiguration == null || !levelDownableConfiguration.getValue()) {
			levelDownableComboBox.setValue(false);	// 默认是不可降级的
		} else {
			levelDownableComboBox.setValue(true);
		}
		
		HorizontalLayout layout2 = new HorizontalLayout();
		layout2.setSpacing(true);
		mainLayout.addComponent(layout2);
		
		editLevelDownable = new Button("编辑", CopyOfInnerConfigManagement.this);
		layout2.addComponent(editLevelDownable);
		saveLevelDownable = new Button("保存", CopyOfInnerConfigManagement.this);
		layout2.addComponent(saveLevelDownable);
		cancelLevelDownable = new Button("取消", CopyOfInnerConfigManagement.this);
		cancelLevelDownable.setStyleName("default");
		layout2.addComponent(cancelLevelDownable);
		
		setProperty2LevelDownable(false);
		return mainLayout;
	}
	
	/**
	 * chb 客户满意度调查配置项
	 * @return
	 */
	private VerticalLayout buildIncomingSatisfactionLayout() {
		VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.setSpacing(true);
		
		HorizontalLayout layout1 = new HorizontalLayout();
		layout1.setWidth("100%");
		layout1.setSpacing(true);
		mainLayout.addComponent(layout1);
		
		Label label = new Label("呼入满意度调查设置：");
		label.setWidth("-1px");
		layout1.addComponent(label);
		
		incomingSatisfactionComboBox = new ComboBox();
		incomingSatisfactionComboBox.addItem(false);
		incomingSatisfactionComboBox.addItem(true);
		incomingSatisfactionComboBox.setItemCaption(false, "不调查");
		incomingSatisfactionComboBox.setItemCaption(true, "调查");
		incomingSatisfactionComboBox.setNullSelectionAllowed(false);
		incomingSatisfactionComboBox.setWidth("100px");
		layout1.addComponent(incomingSatisfactionComboBox);
		
		incomingSatisfactionConfiguration =  ec2ConfigurationService.getByKey("incoming_sati_config", domain.getId());
		if(incomingSatisfactionConfiguration == null || incomingSatisfactionConfiguration.getValue()==false) {
			incomingSatisfactionComboBox.setValue(false);
		} else {
			incomingSatisfactionComboBox.setValue(true);
		}
		
		HorizontalLayout layout2 = new HorizontalLayout();
		layout2.setSpacing(true);
		mainLayout.addComponent(layout2);
		
		incomingEditSatisfaction = new Button("编辑", CopyOfInnerConfigManagement.this);
		layout2.addComponent(incomingEditSatisfaction);
		incomingSaveSatisfaction = new Button("保存", CopyOfInnerConfigManagement.this);
		layout2.addComponent(incomingSaveSatisfaction);
		incomingCancelSatisfaction = new Button("取消", CopyOfInnerConfigManagement.this);
		incomingCancelSatisfaction.setStyleName("default");
		layout2.addComponent(incomingCancelSatisfaction);
		
		setIncomingProperty2Satisfaction(false);
		return mainLayout;
	}
	
	/**
	 * chb 客户满意度调查配置项
	 * @return
	 */
	private VerticalLayout buildOutgoingSatisfactionLayout() {
		VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.setSpacing(true);
		
		HorizontalLayout layout1 = new HorizontalLayout();
		layout1.setWidth("100%");
		layout1.setSpacing(true);
		mainLayout.addComponent(layout1);
		
		Label label = new Label("呼出满意度调查设置：");
		label.setWidth("-1px");
		layout1.addComponent(label);
		
		outgoingSatisfactionComboBox = new ComboBox();
		outgoingSatisfactionComboBox.addItem(false);
		outgoingSatisfactionComboBox.addItem(true);
		outgoingSatisfactionComboBox.setItemCaption(false, "不调查");
		outgoingSatisfactionComboBox.setItemCaption(true, "调查");
		outgoingSatisfactionComboBox.setNullSelectionAllowed(false);
		outgoingSatisfactionComboBox.setWidth("100px");
		layout1.addComponent(outgoingSatisfactionComboBox);
		
		outgoingSatisfactionConfiguration =  ec2ConfigurationService.getByKey("outgoing_sati_config", domain.getId());
		if(outgoingSatisfactionConfiguration == null) {
			outgoingSatisfactionComboBox.setValue(false);
		} else {
			outgoingSatisfactionComboBox.setValue(outgoingSatisfactionConfiguration.getValue());
		}
		
		HorizontalLayout layout2 = new HorizontalLayout();
		layout2.setSpacing(true);
		mainLayout.addComponent(layout2);
		
		outgoingEditSatisfaction = new Button("编辑", CopyOfInnerConfigManagement.this);
		layout2.addComponent(outgoingEditSatisfaction);
		outgoingSaveSatisfaction = new Button("保存", CopyOfInnerConfigManagement.this);
		layout2.addComponent(outgoingSaveSatisfaction);
		outgoingCancelSatisfaction = new Button("取消", CopyOfInnerConfigManagement.this);
		outgoingCancelSatisfaction.setStyleName("default");
		layout2.addComponent(outgoingCancelSatisfaction);
		
		setOutgoingProperty2Satisfaction(false);
		return mainLayout;
	}
	
	/**
	 * chb 号码归属地识别
	 * @return
	 */
	private VerticalLayout buildBelongLayout() {
		VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.setSpacing(true);
		
		HorizontalLayout layout1 = new HorizontalLayout();
		layout1.setWidth("100%");
		layout1.setSpacing(true);
		mainLayout.addComponent(layout1);
		
		Label label = new Label("号码归属地识别：");
		label.setWidth("-1px");
		layout1.addComponent(label);
		
		belongComboBox = new ComboBox();
		belongComboBox.setNullSelectionAllowed(false);
		belongComboBox.addItem(CopyOfInnerConfigManagement.NOBELONG);
		belongComboBox.addItem(CopyOfInnerConfigManagement.ZEROBELONG);
		belongComboBox.addItem(CopyOfInnerConfigManagement.POSTCODEBELONG);
		belongComboBox.setItemCaption(CopyOfInnerConfigManagement.NOBELONG, "不开启归属识别");
		belongComboBox.setItemCaption(CopyOfInnerConfigManagement.ZEROBELONG, "号码本外地识别");
		belongComboBox.setItemCaption(CopyOfInnerConfigManagement.POSTCODEBELONG, "号码加区号");
		belongComboBox.setWidth("130px");
		layout1.addComponent(belongComboBox);
		
		belongConfiguration =  ec2ConfigurationService.getByKey("belong_config", domain.getId());
		if(belongConfiguration == null||belongConfiguration.getValue()== null) {
			belongComboBox.setValue(CopyOfInnerConfigManagement.NOBELONG);
		}else if(belongConfiguration.getValue()==false){
			belongComboBox.setValue(CopyOfInnerConfigManagement.ZEROBELONG);
		} else if(belongConfiguration.getValue()==true){
			belongComboBox.setValue(CopyOfInnerConfigManagement.POSTCODEBELONG);
		}
		
		HorizontalLayout layout2 = new HorizontalLayout();
		layout2.setSpacing(true);
		mainLayout.addComponent(layout2);
		
		belongEdit = new Button("编辑", CopyOfInnerConfigManagement.this);
		layout2.addComponent(belongEdit);
		belongSave = new Button("保存", CopyOfInnerConfigManagement.this);
		layout2.addComponent(belongSave);
		belongCancel= new Button("取消", CopyOfInnerConfigManagement.this);
		belongCancel.setStyleName("default");
		layout2.addComponent(belongCancel);
		
		setBelongButtonVisible(false);
		return mainLayout;
	}

	/**
	 * jrh 创建【话务员是否可以将已经与自己成交的客户，从‘我的客户’中移除】 组件
	 * @return
	 */
	private VerticalLayout buildCustomerRemoveAble2CsrLayout() {
		VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.setSpacing(true);
		
		HorizontalLayout layout1 = new HorizontalLayout();
		layout1.setWidth("100%");
		layout1.setSpacing(true);
		mainLayout.addComponent(layout1);
		
		Label label = new Label("坐席可移除客户：");
		label.setWidth("-1px");
		label.setDescription("<B>坐席是否可以将已经与自己成交的客户从‘我的客户’中移除</B>");
		layout1.addComponent(label);
		
		customerRemoveAbleByCsrComboBox = new ComboBox();
		customerRemoveAbleByCsrComboBox.setDescription("<B>话务员是否可以将已经与自己成交的客户从‘我的客户’中移除</B>");
		customerRemoveAbleByCsrComboBox.addItem(false);
		customerRemoveAbleByCsrComboBox.addItem(true);
		customerRemoveAbleByCsrComboBox.setItemCaption(false, "否 (不可移除)");
		customerRemoveAbleByCsrComboBox.setItemCaption(true, "是 (可移除)");
		customerRemoveAbleByCsrComboBox.setNullSelectionAllowed(false);
		customerRemoveAbleByCsrComboBox.setWidth("125px");
		layout1.addComponent(customerRemoveAbleByCsrComboBox);
		
		customerRemoveAbleByCsrConfiguration =  ec2ConfigurationService.getByKey("customer_remove_able_by_csr", domain.getId());
		if(customerRemoveAbleByCsrConfiguration == null) {
			customerRemoveAbleByCsrComboBox.setValue(false);
		} else {
			customerRemoveAbleByCsrComboBox.setValue(customerRemoveAbleByCsrConfiguration.getValue());
		}
		
		HorizontalLayout layout2 = new HorizontalLayout();
		layout2.setSpacing(true);
		mainLayout.addComponent(layout2);
		
		editCustomerRemoveAbleByCsr = new Button("编辑", CopyOfInnerConfigManagement.this);
		layout2.addComponent(editCustomerRemoveAbleByCsr);
		saveCustomerRemoveAbleByCsr = new Button("保存", CopyOfInnerConfigManagement.this);
		layout2.addComponent(saveCustomerRemoveAbleByCsr);
		cancelCustomerRemoveAbleByCsr = new Button("取消", CopyOfInnerConfigManagement.this);
		cancelCustomerRemoveAbleByCsr.setStyleName("default");
		layout2.addComponent(cancelCustomerRemoveAbleByCsr);
		
		setProperty2CustomerRemoveAble(false);
		return mainLayout;
	}
	
	/**
	 * jrh 创建【部门间历史客服记录可见】 组件
	 * @return
	 */
	private VerticalLayout buildViewRecordSpanDeptLayout() {
		VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.setSpacing(true);
		
		HorizontalLayout layout1 = new HorizontalLayout();
		layout1.setWidth("100%");
		layout1.setSpacing(true);
		mainLayout.addComponent(layout1);
		
		Label label = new Label("坐席查看历史客服记录范围：");
		label.setWidth("-1px");
		label.setDescription("<B>设定各部门之间的话务员是否可以看到相互之间跟同一个客户的历史客服记录！</B>");
		layout1.addComponent(label);
		
		viewRecordSpanDeptComboBox = new ComboBox();
		viewRecordSpanDeptComboBox.setDescription("<B>设定各部门之间的话务员是否可以看到相互之间跟同一个客户的历史客服记录！</B>");
		viewRecordSpanDeptComboBox.addItem("global");
		viewRecordSpanDeptComboBox.addItem(false);
		viewRecordSpanDeptComboBox.addItem(true);
		viewRecordSpanDeptComboBox.setItemCaption("global", "全局范围");
		viewRecordSpanDeptComboBox.setItemCaption(false, "单个坐席");
		viewRecordSpanDeptComboBox.setItemCaption(true, "单个部门");
		viewRecordSpanDeptComboBox.setNullSelectionAllowed(false);
		viewRecordSpanDeptComboBox.setWidth("100px");
		layout1.addComponent(viewRecordSpanDeptComboBox);
		
		viewRecordSpanDeptConfiguration =  ec2ConfigurationService.getByKey("csr_view_record_span_department", domain.getId());
		if(viewRecordSpanDeptConfiguration == null || viewRecordSpanDeptConfiguration.getValue() == null) {
			viewRecordSpanDeptComboBox.setValue("global");
		} else {
			viewRecordSpanDeptComboBox.setValue(viewRecordSpanDeptConfiguration.getValue());
		}
		
		HorizontalLayout layout2 = new HorizontalLayout();
		layout2.setSpacing(true);
		mainLayout.addComponent(layout2);
		
		editViewRecordSpanDept = new Button("编辑", CopyOfInnerConfigManagement.this);
		layout2.addComponent(editViewRecordSpanDept);
		saveViewRecordSpanDept = new Button("保存", CopyOfInnerConfigManagement.this);
		layout2.addComponent(saveViewRecordSpanDept);
		cancelViewRecordSpanDept = new Button("取消", CopyOfInnerConfigManagement.this);
		cancelViewRecordSpanDept.setStyleName("default");
		layout2.addComponent(cancelViewRecordSpanDept);
		
		setProperty2ViewRecordSpanDept(false);
		return mainLayout;
	}
	
	/**
	 * jrh 创建按外线区分黑名单配置项
	 * @return
	 */
	private VerticalLayout buildSettingBlacklistByOutlineLayout() {
		VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.setSpacing(true);
		
		HorizontalLayout layout1 = new HorizontalLayout();
		layout1.setWidth("100%");
		layout1.setSpacing(true);
		mainLayout.addComponent(layout1);
		
		Label label = new Label("按外线区分黑名单：");
		label.setWidth("-1px");
		label.setDescription("<B>如果不开启，全局范围内都使用相同的黑名单，否则按外线进行区分</B>");
		layout1.addComponent(label);
		
		settingBlacklistByOutlineComboBox = new ComboBox();
		settingBlacklistByOutlineComboBox.setDescription("<B>如果不开启，全局范围内都使用相同的黑名单，否则按外线进行区分</B>");
		settingBlacklistByOutlineComboBox.addItem(false);
		settingBlacklistByOutlineComboBox.addItem(true);
		settingBlacklistByOutlineComboBox.setItemCaption(false, "否");
		settingBlacklistByOutlineComboBox.setItemCaption(true, "是");
		settingBlacklistByOutlineComboBox.setNullSelectionAllowed(false);
		settingBlacklistByOutlineComboBox.setWidth("115px");
		layout1.addComponent(settingBlacklistByOutlineComboBox);
		
		settingBlacklistByOutlineConfiguration =  ec2ConfigurationService.getByKey("setting_blacklist_by_outline", domain.getId());
		if(settingBlacklistByOutlineConfiguration == null || !settingBlacklistByOutlineConfiguration.getValue()) {
			settingBlacklistByOutlineComboBox.setValue(false);	// 默认是不可降级的
		} else {
			settingBlacklistByOutlineComboBox.setValue(true);
		}
		
		HorizontalLayout layout2 = new HorizontalLayout();
		layout2.setSpacing(true);
		mainLayout.addComponent(layout2);
		
		editSettingBlacklistByOutline = new Button("编辑", CopyOfInnerConfigManagement.this);
		layout2.addComponent(editSettingBlacklistByOutline);
		saveSettingBlacklistByOutline = new Button("保存", CopyOfInnerConfigManagement.this);
		layout2.addComponent(saveSettingBlacklistByOutline);
		cancelSettingBlacklistByOutline = new Button("取消", CopyOfInnerConfigManagement.this);
		cancelSettingBlacklistByOutline.setStyleName("default");
		layout2.addComponent(cancelSettingBlacklistByOutline);
		
		setProperty2BlacklistByOutline(false);
		return mainLayout;
	}

	/**
	 * jrh 创建【部门间历史订单可见】 组件
	 * @return
	 */
	private VerticalLayout buildViewOrderSpanDeptLayout() {
		VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.setSpacing(true);
		
		HorizontalLayout layout1 = new HorizontalLayout();
		layout1.setWidth("100%");
		layout1.setSpacing(true);
		mainLayout.addComponent(layout1);
		
		Label label = new Label("坐席查看客户历史订单范围：");
		label.setWidth("-1px");
		label.setDescription("<B>设定各部门之间的话务员是否可以看到相互之间跟同一个客户的历史订单！</B>");
		layout1.addComponent(label);
		
		viewOrderSpanDeptComboBox = new ComboBox();
		viewOrderSpanDeptComboBox.setDescription("<B>设定各部门之间的话务员是否可以看到相互之间跟同一个客户的历史订单！</B>");
		viewOrderSpanDeptComboBox.addItem("global");
		viewOrderSpanDeptComboBox.addItem(false);
		viewOrderSpanDeptComboBox.addItem(true);
		viewOrderSpanDeptComboBox.setItemCaption("global", "全局范围");
		viewOrderSpanDeptComboBox.setItemCaption(false, "单个坐席");
		viewOrderSpanDeptComboBox.setItemCaption(true, "单个部门");
		viewOrderSpanDeptComboBox.setNullSelectionAllowed(false);
		viewOrderSpanDeptComboBox.setWidth("100px");
		layout1.addComponent(viewOrderSpanDeptComboBox);
		
		viewOrderSpanDeptConfiguration =  ec2ConfigurationService.getByKey("csr_view_order_span_department", domain.getId());
		if(viewOrderSpanDeptConfiguration == null || viewOrderSpanDeptConfiguration.getValue() == null) {
			viewOrderSpanDeptComboBox.setValue("global");
		} else {
			viewOrderSpanDeptComboBox.setValue(viewOrderSpanDeptConfiguration.getValue());
		}
		
		HorizontalLayout layout2 = new HorizontalLayout();
		layout2.setSpacing(true);
		mainLayout.addComponent(layout2);
		
		editViewOrderSpanDept = new Button("编辑", CopyOfInnerConfigManagement.this);
		layout2.addComponent(editViewOrderSpanDept);
		saveViewOrderSpanDept = new Button("保存", CopyOfInnerConfigManagement.this);
		layout2.addComponent(saveViewOrderSpanDept);
		cancelViewOrderSpanDept = new Button("取消", CopyOfInnerConfigManagement.this);
		cancelViewOrderSpanDept.setStyleName("default");
		layout2.addComponent(cancelViewOrderSpanDept);
		
		setProperty2ViewOrderSpanDept(false);
		return mainLayout;
	}

	/**
	 * jrh 创建登陆分机默认置忙配置项
	 * @return
	 */
	private VerticalLayout buildPauseExtenOnLoginLayout() {
		VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.setSpacing(true);
		
		HorizontalLayout layout1 = new HorizontalLayout();
		layout1.setWidth("100%");
		layout1.setSpacing(true);
		mainLayout.addComponent(layout1);
		
		Label label = new Label("登陆分机默认置忙：");
		label.setWidth("-1px");
		layout1.addComponent(label);
		
		pauseExtenOnLoginComboBox = new ComboBox();
		pauseExtenOnLoginComboBox.setDescription("<B>设置用户登录后，是否默认将分机置忙</B>");
		pauseExtenOnLoginComboBox.addItem(false);
		pauseExtenOnLoginComboBox.addItem(true);
		pauseExtenOnLoginComboBox.setItemCaption(false, "否");
		pauseExtenOnLoginComboBox.setItemCaption(true, "是");
		pauseExtenOnLoginComboBox.setNullSelectionAllowed(false);
		pauseExtenOnLoginComboBox.setWidth("115px");
		layout1.addComponent(pauseExtenOnLoginComboBox);
		
		pauseExtenOnLoginConfiguration =  ec2ConfigurationService.getByKey("pasue_exten_after_csr_login", domain.getId());
		if(pauseExtenOnLoginConfiguration == null || !pauseExtenOnLoginConfiguration.getValue()) {
			pauseExtenOnLoginComboBox.setValue(false);	// 默认是不置忙
		} else {
			pauseExtenOnLoginComboBox.setValue(true);
		}
		
		HorizontalLayout layout2 = new HorizontalLayout();
		layout2.setSpacing(true);
		mainLayout.addComponent(layout2);
		
		editPauseExtenOnLogin = new Button("编辑", CopyOfInnerConfigManagement.this);
		layout2.addComponent(editPauseExtenOnLogin);
		savePauseExtenOnLogin = new Button("保存", CopyOfInnerConfigManagement.this);
		layout2.addComponent(savePauseExtenOnLogin);
		cancelPauseExtenOnLogin = new Button("取消", CopyOfInnerConfigManagement.this);
		cancelPauseExtenOnLogin.setStyleName("default");
		layout2.addComponent(cancelPauseExtenOnLogin);
		
		setPauseExtenOnLogin(false);
		return mainLayout;
	}
	
	/**
	 * jrh 创建呼叫弹屏默认置忙配置项
	 * @return
	 */
	private VerticalLayout buildPauseExtenOnPopupWindowLayout() {
		VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.setSpacing(true);
		
		HorizontalLayout layout1 = new HorizontalLayout();
		layout1.setWidth("100%");
		layout1.setSpacing(true);
		mainLayout.addComponent(layout1);
		
		Label label = new Label("呼叫弹屏默认置忙：");
		label.setWidth("-1px");
		layout1.addComponent(label);
		
		pauseExtenOnPopupWindowComboBox = new ComboBox();
		pauseExtenOnPopupWindowComboBox.setDescription("<B>设置呼入、呼出弹屏后，是否默认将分机置忙，在弹屏关闭后将自动将分机置闲</B>");
		pauseExtenOnPopupWindowComboBox.addItem(false);
		pauseExtenOnPopupWindowComboBox.addItem(true);
		pauseExtenOnPopupWindowComboBox.setItemCaption(false, "否");
		pauseExtenOnPopupWindowComboBox.setItemCaption(true, "是");
		pauseExtenOnPopupWindowComboBox.setNullSelectionAllowed(false);
		pauseExtenOnPopupWindowComboBox.setWidth("115px");
		layout1.addComponent(pauseExtenOnPopupWindowComboBox);
		
		pauseExtenOnPopupWindowConfiguration =  ec2ConfigurationService.getByKey("pasue_exten_after_csr_popup_calling_window", domain.getId());
		if(pauseExtenOnPopupWindowConfiguration == null || !pauseExtenOnPopupWindowConfiguration.getValue()) {
			pauseExtenOnPopupWindowComboBox.setValue(false);	// 默认是不置忙
		} else {
			pauseExtenOnPopupWindowComboBox.setValue(true);
		}
		
		HorizontalLayout layout2 = new HorizontalLayout();
		layout2.setSpacing(true);
		mainLayout.addComponent(layout2);
		
		editPauseExtenOnPopupWindow = new Button("编辑", CopyOfInnerConfigManagement.this);
		layout2.addComponent(editPauseExtenOnPopupWindow);
		savePauseExtenOnPopupWindow = new Button("保存", CopyOfInnerConfigManagement.this);
		layout2.addComponent(savePauseExtenOnPopupWindow);
		cancelPauseExtenOnPopupWindow = new Button("取消", CopyOfInnerConfigManagement.this);
		cancelPauseExtenOnPopupWindow.setStyleName("default");
		layout2.addComponent(cancelPauseExtenOnPopupWindow);
		
		setPauseExtenOnPopupWindow(false);
		return mainLayout;
	}

	/**
	 * jrh 设置客户等级可降级的可视化属性
	 * @param visible
	 */
	private void setProperty2LevelDownable(boolean visible) {
		levelDownableComboBox.setReadOnly(!visible);
		editLevelDownable.setVisible(!visible);
		saveLevelDownable.setVisible(visible);
		cancelLevelDownable.setVisible(visible);
	}
	
	/**
	 * chb 设置满意度调查组件的可视化属性
	 * @param visible
	 */
	private void setIncomingProperty2Satisfaction(boolean visible) {
		incomingSatisfactionComboBox.setReadOnly(!visible);
		incomingEditSatisfaction.setVisible(!visible);
		incomingSaveSatisfaction.setVisible(visible);
		incomingCancelSatisfaction.setVisible(visible);
	}
	
	/**
	 * chb 设置满意度调查组件的可视化属性
	 * @param visible
	 */
	private void setOutgoingProperty2Satisfaction(boolean visible) {
		outgoingSatisfactionComboBox.setReadOnly(!visible);
		outgoingEditSatisfaction.setVisible(!visible);
		outgoingSaveSatisfaction.setVisible(visible);
		outgoingCancelSatisfaction.setVisible(visible);
	}
	
	/**
	 * chb 设置归属地可见
	 * @param visible
	 */
	private void setBelongButtonVisible(boolean visible) {
		belongComboBox.setReadOnly(!visible);
		belongEdit.setVisible(!visible);
		belongSave.setVisible(visible);
		belongCancel.setVisible(visible);
	}
	
	/**
	 * jrh 设置【话务员可移除自己的客户组件】的可视化属性
	 * @param visible
	 */
	private void setProperty2CustomerRemoveAble(boolean visible) {
		customerRemoveAbleByCsrComboBox.setReadOnly(!visible);
		editCustomerRemoveAbleByCsr.setVisible(!visible);
		saveCustomerRemoveAbleByCsr.setVisible(visible);
		cancelCustomerRemoveAbleByCsr.setVisible(visible);
	}
	
	/**
	 * jrh 设置【部门间历史客服记录可见】的可视化属性
	 * @param visible
	 */
	private void setProperty2ViewRecordSpanDept(boolean visible) {
		viewRecordSpanDeptComboBox.setReadOnly(!visible);
		editViewRecordSpanDept.setVisible(!visible);
		saveViewRecordSpanDept.setVisible(visible);
		cancelViewRecordSpanDept.setVisible(visible);
	}
	
	/**
	 * jrh 设置【按外线区分黑名单】的可视化属性
	 * @param visible
	 */
	private void setProperty2BlacklistByOutline(boolean visible) {
		settingBlacklistByOutlineComboBox.setReadOnly(!visible);
		editSettingBlacklistByOutline.setVisible(!visible);
		saveSettingBlacklistByOutline.setVisible(visible);
		cancelSettingBlacklistByOutline.setVisible(visible);
	}
	
	/**
	 * jrh 设置【部门间历史订单可见】的可视化属性
	 * @param visible
	 */
	private void setProperty2ViewOrderSpanDept(boolean visible) {
		viewOrderSpanDeptComboBox.setReadOnly(!visible);
		editViewOrderSpanDept.setVisible(!visible);
		saveViewOrderSpanDept.setVisible(visible);
		cancelViewOrderSpanDept.setVisible(visible);
	}

	/**
	 * jrh 设置【登陆分机默认置忙】的可视化属性
	 * @param visible
	 */
	private void setPauseExtenOnLogin(boolean visible) {
		pauseExtenOnLoginComboBox.setReadOnly(!visible);
		editPauseExtenOnLogin.setVisible(!visible);
		savePauseExtenOnLogin.setVisible(visible);
		cancelPauseExtenOnLogin.setVisible(visible);
	}

	/**
	 * jrh 设置【呼叫弹屏默认置忙】的可视化属性
	 * @param visible
	 */
	private void setPauseExtenOnPopupWindow(boolean visible) {
		pauseExtenOnPopupWindowComboBox.setReadOnly(!visible);
		editPauseExtenOnPopupWindow.setVisible(!visible);
		savePauseExtenOnPopupWindow.setVisible(visible);
		cancelPauseExtenOnPopupWindow.setVisible(visible);
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
		if(belongComboBox.getValue()==CopyOfInnerConfigManagement.NOBELONG){
			belong=null;
		}else if(belongComboBox.getValue()==CopyOfInnerConfigManagement.ZEROBELONG){
			belong=false;
		}else if(belongComboBox.getValue()==CopyOfInnerConfigManagement.POSTCODEBELONG){
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
	 * 创建内部组件设置的输出
	 * 
	 * @return
	 */
	private VerticalLayout buildInnerConfigLayout() {
		VerticalLayout innerConfigLayout = new VerticalLayout();
		innerConfigLayout.setMargin(true);
		// 默任关键字的集合
		List<String> defaultKeywords = new ArrayList<String>();
		for (TableKeywordDefault kewords : TableKeywordDefault.values()) {
			String keywordStr = kewords.getName();
			defaultKeywords.add(keywordStr);
		}
		// 默认字段的组件
		ListSelect defaultKeywordSelect = new ListSelect("默认Excel字段(不能修改)",
				defaultKeywords);
		defaultKeywordSelect.setColumns(10);
		defaultKeywordSelect.setWidth("15em");
		innerConfigLayout.addComponent(defaultKeywordSelect);

		return innerConfigLayout;
	}

	/**
	 * 删除操作
	 */
	private void executeDelete() {
		TableKeyword keyWord = (TableKeyword) keywordSelect.getValue();
		keywordService.deleteTableKeywordById(keyWord.getId());
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
	 * 添加操作
	 */
	private void executeAdd() {
		String inputKeyword = "";
		if (inputField.getValue() != null) {
			inputKeyword = inputField.getValue().toString().trim();
		}
		// 如果是空字符串，显示提示信息
		if (inputKeyword.equals("")) {
			NotificationUtil.showWarningNotification(this, "表格字段不能为空！");
			return;
		}

		TableKeyword tableKeyword = new TableKeyword();
		tableKeyword.setColumnName(inputKeyword);
		// TODO chb 应该加上描述信息
		tableKeyword.setDomain(SpringContextHolder.getDomain());
		keywordService.saveTableKeyword(tableKeyword);
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
		this.update();
	}
	
	/**
	 * 点击添加和删除按钮时的事件
	 */
	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if (source == add) {
			try {
				executeAdd();
				inputField.setValue("");
			} catch (Exception e) {
				e.printStackTrace();
				this.getWindow().showNotification("添加失败");
			}
		} else if (source == delete) {
			try {
				executeDelete();
			} catch (Exception e) {
				e.printStackTrace();
				NotificationUtil.showWarningNotification(this, "删除失败,请看是否选择了要删除的对象！");
			}
		} else if (source == addLevel) {
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
//	客户等级可降
		}else if(source == editLevelDownable) {
			setProperty2LevelDownable(true);
		}else if(source == saveLevelDownable) {
			excuteLevelDownable();
			setProperty2LevelDownable(false);
		}else if(source == cancelLevelDownable) {
			if(levelDownableConfiguration == null || !levelDownableConfiguration.getValue()) {
				levelDownableComboBox.setValue(false);
			} else {
				levelDownableComboBox.setValue(true);
			}
			setProperty2LevelDownable(false);
//呼入满意度
		}else if(source == incomingEditSatisfaction) {
			setIncomingProperty2Satisfaction(true);
		}else if(source == incomingSaveSatisfaction) {
			excuteIncomingSaveSatisfaction();
			setIncomingProperty2Satisfaction(false);
		}else if(source == incomingCancelSatisfaction) {
			if(incomingSatisfactionConfiguration == null || incomingSatisfactionConfiguration.getValue()) {
				incomingSatisfactionComboBox.setValue(true);
			} else {
				incomingSatisfactionComboBox.setValue(false);
			}
			setIncomingProperty2Satisfaction(false);
//呼出满意度
		}else if(source == outgoingEditSatisfaction) {
			setOutgoingProperty2Satisfaction(true);
		}else if(source == outgoingSaveSatisfaction) {
			excuteOutgoingSaveSatisfaction();
			setOutgoingProperty2Satisfaction(false);
		}else if(source == outgoingCancelSatisfaction) {
			if(outgoingSatisfactionConfiguration == null || outgoingSatisfactionConfiguration.getValue()) {
				outgoingSatisfactionComboBox.setValue(true);
			} else {
				outgoingSatisfactionComboBox.setValue(false);
			}
			setOutgoingProperty2Satisfaction(false);
//号码归属地
		}else if(source == belongEdit) {
			setBelongButtonVisible(true);
		}else if(source ==  belongSave) {
			excuteBelongSave();
			setBelongButtonVisible(false);
		}else if(source == belongCancel) {
			if(belongConfiguration == null || belongConfiguration.getValue()==null) {
				belongComboBox.setValue(CopyOfInnerConfigManagement.NOBELONG);
			} else if( belongConfiguration.getValue()==false){
				belongComboBox.setValue(CopyOfInnerConfigManagement.ZEROBELONG);
			}else{
				belongComboBox.setValue(CopyOfInnerConfigManagement.POSTCODEBELONG);
			}
			setBelongButtonVisible(false);
// 可移除客户
		}else if(source == editCustomerRemoveAbleByCsr) {
			setProperty2CustomerRemoveAble(true);
		}else if(source == saveCustomerRemoveAbleByCsr) {
			excuteCustomerRemoveAbleByCsr();
			setProperty2CustomerRemoveAble(false);
		}else if(source == cancelCustomerRemoveAbleByCsr) {
			if(customerRemoveAbleByCsrConfiguration == null || customerRemoveAbleByCsrConfiguration.getValue()) {
				customerRemoveAbleByCsrComboBox.setValue(true);
			} else {
				customerRemoveAbleByCsrComboBox.setValue(false);
			}
			setProperty2CustomerRemoveAble(false);
// 部门间历史客服记录可见
		}else if(source == editViewRecordSpanDept) {
			setProperty2ViewRecordSpanDept(true);
		}else if(source == saveViewRecordSpanDept) {
			excuteViewRecordSpanDept();
			setProperty2ViewRecordSpanDept(false);
		}else if(source == cancelViewRecordSpanDept) {
			if(viewRecordSpanDeptConfiguration == null || viewRecordSpanDeptConfiguration.getValue() == null) {
				viewRecordSpanDeptComboBox.setValue(null);
			} 
			setProperty2ViewRecordSpanDept(false);
// 按外线区分黑名单
		}else if(source == editSettingBlacklistByOutline) {
			setProperty2BlacklistByOutline(true);
		}else if(source == saveSettingBlacklistByOutline) {
			excuteSettingBlacklistByOutline();
			setProperty2BlacklistByOutline(false);
		}else if(source == cancelSettingBlacklistByOutline) {
			if(settingBlacklistByOutlineConfiguration == null) {
				settingBlacklistByOutlineComboBox.setValue(true);
			} 
			setProperty2BlacklistByOutline(false);
// 部门间历史订单可见
		}else if(source == editViewOrderSpanDept) {
			setProperty2ViewOrderSpanDept(true);
		}else if(source == saveViewOrderSpanDept) {
			excuteViewOrderSpanDept();
			setProperty2ViewOrderSpanDept(false);
		}else if(source == cancelViewOrderSpanDept) {
			if(viewOrderSpanDeptConfiguration == null || viewOrderSpanDeptConfiguration.getValue() == null) {
				viewOrderSpanDeptComboBox.setValue(null);
			} 
			setProperty2ViewOrderSpanDept(false);

// 登陆分机自动置忙
		}else if(source == editPauseExtenOnLogin) {
			setPauseExtenOnLogin(true);
		}else if(source == savePauseExtenOnLogin) {
			excutePauseExtenOnLogin();
			setPauseExtenOnLogin(false);
		}else if(source == cancelPauseExtenOnLogin) {
			if(pauseExtenOnLoginConfiguration == null) {
				pauseExtenOnLoginComboBox.setValue(true);
			} 
			setPauseExtenOnLogin(false);
			
// 弹屏分机自动置忙
		}else if(source == editPauseExtenOnPopupWindow) {
			setPauseExtenOnPopupWindow(true);
		}else if(source == savePauseExtenOnPopupWindow) {
			excutePauseExtenOnPopupWindow();
			setPauseExtenOnPopupWindow(false);
		}else if(source == cancelPauseExtenOnPopupWindow) {
			if(pauseExtenOnPopupWindowConfiguration == null) {
				pauseExtenOnPopupWindowComboBox.setValue(true);
			} 
			setPauseExtenOnPopupWindow(false);
		}
		
		// 更新显示的信息
		this.update();
	}

}
