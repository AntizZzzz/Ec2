package com.jiangyifen.ec2.ui.mgr.system.tabsheet;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.bean.DayOfWeek;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.KickCsrLogoutSetting;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.service.eaoservice.KickCsrLogoutSettingService;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.Notification;

/**
 * 
 * @Description 描述：定时强制坐席退出配置组件
 * 
 * @author  jrh
 * @date    2013年12月19日 下午1:32:52
 * @version v1.0.0
 */
@SuppressWarnings("serial")
public class KickCsrLogoutConfigView extends VerticalLayout implements ClickListener, ValueChangeListener {
	// 日志工具
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private Notification notification;				// 提示信息
	private OptionGroup startSettingOption;			// 全局配置选择（是否开启强制坐席退出）
	private ComboBox dayOfWeekTypeSelector;			// 强制退出的日期类型选择(工作日、周末、自定义)
	private OptionGroup daysOfWeekOption;			// 日期选择(星期一、星期二、星期三...)
	private HorizontalLayout dayOfWeekLayout;		// 存放dayOfWeekOption
	private ComboBox startRedirectHour;				// 开始的小时设置
	private ComboBox startRedirectMinute;			// 开始的分钟设置

	// 操作按钮
	private Button edit;			// 编辑配置项
	private Button save;			// 保存配置项
	private Button cancel;			// 取消更改
	
	private Domain domain;
	private User loginUser;
	private KickCsrLogoutSetting kickCsrLogoutSetting;

	private KickCsrLogoutSettingService kickCsrLogoutSettingService;
	
	public KickCsrLogoutConfigView() {
		this.setMargin(true, false, false, false);

		domain = SpringContextHolder.getDomain();
		loginUser = SpringContextHolder.getLoginUser();

		kickCsrLogoutSettingService = SpringContextHolder.getBean("kickCsrLogoutSettingService");

		notification = new Notification("");
		notification.setDelayMsec(1000);
		notification.setHtmlContentAllowed(true);
		
		Label caption = new Label("强制坐席定时退出配置：");
		this.addComponent(caption);
		
		Panel mainPanel = new Panel();
		this.addComponent(mainPanel);

		VerticalLayout panelContent = new VerticalLayout();
		panelContent.setSizeUndefined();
		panelContent.setMargin(true);
		panelContent.setSpacing(true);
		mainPanel.setContent(panelContent);
		
		// 创建全局开启外转外设置项组件
		createStartSetting(panelContent);
		
		// 创建转接日期设置组件
		createDaysOfWeekType(panelContent);
		
		// 创建在一星期中那就天进行转接
		createDayOfWeek(panelContent);
		
		// 创建转呼手机的时间设置
		createRunRedirectTime(panelContent);

		// 创建操作组件
		createOperatorButtons(panelContent);
		
		// 回显组件的相关值
		echoComponentValues();
	}
	
	/**
	 * 创建全局开启外转外设置项组件
	 * @param panelContent
	 */
	private void createStartSetting(VerticalLayout panelContent) {
		HorizontalLayout layout = new HorizontalLayout();
		layout.setSpacing(true);
		panelContent.addComponent(layout);
		
		Label caption = new Label("全局开启状态：");
		caption.setWidth("-1px");
		layout.addComponent(caption);
		
		startSettingOption = new OptionGroup();
		startSettingOption.addItem(true);
		startSettingOption.addItem(false);
		startSettingOption.setItemCaption(true, "开启");
		startSettingOption.setItemCaption(false, "关闭");
		startSettingOption.setImmediate(true);
		startSettingOption.setReadOnly(true);
		startSettingOption.setNullSelectionAllowed(false);
		startSettingOption.addStyleName("twocol200");
		startSettingOption.addStyleName("myopacity");
		layout.addComponent(startSettingOption);
	}

	/**
	 * 创建转接日期设置组件 (今天、工作日、双休日、自定义)
	 * @param panelContent
	 */
	private void createDaysOfWeekType(VerticalLayout panelContent) {
		HorizontalLayout layout = new HorizontalLayout();
		layout.setSpacing(true);
		panelContent.addComponent(layout);
		
		Label caption = new Label("日期类型选择：");
		caption.setWidth("-1px");
		caption.setDescription("<B>指定在哪几天需要强制坐席定时自动退出系统</B>");
		layout.addComponent(caption);
		
		dayOfWeekTypeSelector = new ComboBox();
		dayOfWeekTypeSelector.addItem("weekday");
		dayOfWeekTypeSelector.setItemCaption("weekday", "工作日");
		dayOfWeekTypeSelector.addItem("weekend");
		dayOfWeekTypeSelector.setItemCaption("weekend", "周末");
		dayOfWeekTypeSelector.addItem("custom");
		dayOfWeekTypeSelector.setItemCaption("custom", "自定义");
		dayOfWeekTypeSelector.setWidth("200px");
		dayOfWeekTypeSelector.setImmediate(true);
		dayOfWeekTypeSelector.setReadOnly(true);
		dayOfWeekTypeSelector.setDescription("<B>指定在哪几天需要强制坐席定时自动退出系统</B>");
		dayOfWeekTypeSelector.setNullSelectionAllowed(false);
		dayOfWeekTypeSelector.addListener(this);
		layout.addComponent(dayOfWeekTypeSelector);
	}
	
	/**
	 * 创建在一星期中那就天进行转接(星期一、星期二、星期三...) 只在自定义选择时才需要显示
	 * @param panelContent
	 */
	private void createDayOfWeek(VerticalLayout panelContent) {
		dayOfWeekLayout = new HorizontalLayout();
		dayOfWeekLayout.setSpacing(true);
		dayOfWeekLayout.setVisible(false);
		panelContent.addComponent(dayOfWeekLayout);
		
		Label caption = new Label("自定义日期选择：");
		caption.setWidth("-1px");
		caption.setDescription("<B>可以多选</B>");
		dayOfWeekLayout.addComponent(caption);
		
		BeanItemContainer<DayOfWeek> container = new BeanItemContainer<DayOfWeek>(DayOfWeek.class);
		container.addBean(DayOfWeek.sun);
		container.addBean(DayOfWeek.mon);
		container.addBean(DayOfWeek.tue);
		container.addBean(DayOfWeek.wen);
		container.addBean(DayOfWeek.thu);
		container.addBean(DayOfWeek.fri);
		container.addBean(DayOfWeek.sat);
		
		daysOfWeekOption = new OptionGroup();
		daysOfWeekOption.setContainerDataSource(container);
		daysOfWeekOption.setItemCaptionPropertyId("name");
		daysOfWeekOption.setMultiSelect(true);
		daysOfWeekOption.setNullSelectionAllowed(false);
		daysOfWeekOption.setImmediate(true);
		daysOfWeekOption.setReadOnly(true);
		daysOfWeekOption.setDescription("<B>可以多选</B>");
		daysOfWeekOption.addStyleName("threecol");
		daysOfWeekOption.addStyleName("myopacity");
		dayOfWeekLayout.addComponent(daysOfWeekOption);
	}
	
	/**
	 * 创建执行强制退出的具体时间设置 (18:00 - 23:59 )
	 * @param panelContent
	 */
	private void createRunRedirectTime(VerticalLayout panelContent) {
		HorizontalLayout layout = new HorizontalLayout();
		layout.setSpacing(true);
		panelContent.addComponent(layout);
		
		Label caption = new Label("退出时间设置：");
		caption.setWidth("-1px");
		layout.addComponent(caption);
		
		startRedirectHour = new ComboBox();
		startRedirectHour.setImmediate(true);
		startRedirectHour.setWidth("50px");
		startRedirectHour.setNullSelectionAllowed(false);
		layout.addComponent(startRedirectHour);
		
		Label space1 = new Label(" ：");
		space1.setWidth("-1px");
		layout.addComponent(space1);

		startRedirectMinute = new ComboBox();
		startRedirectMinute.setImmediate(true);
		startRedirectMinute.setWidth("50px");
		startRedirectMinute.setNullSelectionAllowed(false);
		layout.addComponent(startRedirectMinute);


		for(int hour = 0; hour < 24; hour++) {
			startRedirectHour.addItem(hour);
		}
		
		for(int minute = 0; minute < 60; minute++) {
			startRedirectMinute.addItem(minute);
		}
		
		startRedirectHour.setValue(18);
		startRedirectMinute.setValue(0);
		
		startRedirectHour.setReadOnly(true);
		startRedirectMinute.setReadOnly(true);
	}

	/**
	 * 创建操作组件
	 * @return
	 */
	private void createOperatorButtons(VerticalLayout panelContent) {
		HorizontalLayout layout = new HorizontalLayout();
		layout.setSpacing(true);
		panelContent.addComponent(layout);
		
		// 编辑
		edit = new Button("编 辑", this);
		edit.setStyleName("default");
		edit.setVisible(true);
		edit.setImmediate(true);
		layout.addComponent(edit);
		
		// 保存
		save = new Button("保 存", this);
		save.setStyleName("default");
		save.setVisible(false);
		save.setImmediate(true);
		layout.addComponent(save);
		
		// 取消
		cancel = new Button("取 消", this);
		layout.addComponent(cancel);
		cancel.setVisible(false);
		cancel.setImmediate(true);
	}

	/**
	 * 回显组件的相关值
	 */
	public void echoComponentValues() {
		kickCsrLogoutSetting = kickCsrLogoutSettingService.getByDomainId(domain.getId());
		
		if(kickCsrLogoutSetting == null) {
			Set<DayOfWeek> daysOfWeek = new HashSet<DayOfWeek>();
			daysOfWeek.add(DayOfWeek.mon);
			daysOfWeek.add(DayOfWeek.fri);
			kickCsrLogoutSetting = new KickCsrLogoutSetting();
			kickCsrLogoutSetting.setDayOfWeekType("weekday");
			kickCsrLogoutSetting.setDomainId(domain.getId());
			kickCsrLogoutSetting.setLaunchHour(23);
			kickCsrLogoutSetting.setLaunchMinute(59);
			kickCsrLogoutSetting.setIsLaunch(false);
			kickCsrLogoutSetting.setCreatorId(loginUser.getId());
			kickCsrLogoutSetting.setEmpNo(loginUser.getEmpNo());
			kickCsrLogoutSetting.setUsername(loginUser.getUsername());
			kickCsrLogoutSetting.setRealName(loginUser.getRealName());
			kickCsrLogoutSetting.setDaysOfWeek(daysOfWeek);
		}

		// 回显全局配置项
		refreshSettingInfos(kickCsrLogoutSetting);
	}
	
	/**
	 * 回显配置信息
	 * @param kickSetting
	 */
	private void refreshSettingInfos(KickCsrLogoutSetting kickSetting) {
		if(kickSetting != null) {
			changeComponentsStatus(false);
			startSettingOption.setValue(kickSetting.getIsLaunch());
			dayOfWeekTypeSelector.setValue(kickSetting.getDayOfWeekType());
			daysOfWeekOption.setValue(kickSetting.getDaysOfWeek());
			startRedirectHour.setValue(kickSetting.getLaunchHour());
			startRedirectMinute.setValue(kickSetting.getLaunchMinute());
			changeComponentsStatus(true);
		}
	}

	@Override
	public void valueChange(ValueChangeEvent event) {
		Property source = event.getProperty();
		if(source == dayOfWeekTypeSelector) {
			String type = (String) dayOfWeekTypeSelector.getValue();
			dayOfWeekLayout.setVisible("custom".equals(type));
		} 
	}

	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if(source == edit) {
			changeComponentsStatus(false);
		} else if(source == save) {
			try {
				executeSave();
				refreshSettingInfos(kickCsrLogoutSetting);
				notification.setCaption("<B>保存成功!</B>");
				this.getApplication().getMainWindow().showNotification(notification);
			} catch (Exception e) {
				logger.error("Manager 保存外转外配置信息失败 --> "+e.getMessage(), e);
				this.getApplication().getMainWindow().showNotification("对不起，保存失败！", Notification.TYPE_WARNING_MESSAGE);
			}
		} else if(source == cancel) {
			refreshSettingInfos(kickCsrLogoutSetting);
		} 
	}

	/**
	 * 执行保存
	 */
	@SuppressWarnings("unchecked")
	private void executeSave() {
		// 这样是为了解决情况：当数据中某个内部配置项为空，而多个管理员同时操作内部配置时，可能导致数据库出现多条配置信息
		if(kickCsrLogoutSetting.getId() == null) {	
			KickCsrLogoutSetting kcls = kickCsrLogoutSettingService.getByDomainId(domain.getId());
			if(kcls != null) {	// 如果已经存在，则重新赋值
				kickCsrLogoutSetting = kcls;
			}
		}
		
		kickCsrLogoutSetting.setIsLaunch((Boolean) startSettingOption.getValue());
		kickCsrLogoutSetting.setDayOfWeekType((String) dayOfWeekTypeSelector.getValue());
		kickCsrLogoutSetting.setDaysOfWeek((Set<DayOfWeek>) daysOfWeekOption.getValue());
		kickCsrLogoutSetting.setLaunchHour((Integer) startRedirectHour.getValue());
		kickCsrLogoutSetting.setLaunchMinute((Integer) startRedirectMinute.getValue());

		if(kickCsrLogoutSetting.getId() != null) {
			kickCsrLogoutSettingService.updateKickCsrLogoutSetting(kickCsrLogoutSetting);
		} else {
			kickCsrLogoutSettingService.saveKickCsrLogoutSetting(kickCsrLogoutSetting);
		}
		
		// 创建或停止定时任务
		if(kickCsrLogoutSetting.getIsLaunch()) {
			kickCsrLogoutSettingService.createGlobalSettingScheduler(kickCsrLogoutSetting, domain);
		} else {
			kickCsrLogoutSettingService.stopGlobalSettingcheduler(domain);
		}
	}

	/**
	 * 改变各组件的可编辑状态
	 * @param readOnly
	 */
	private void changeComponentsStatus(boolean readOnly) {
		// 修改配置组件的只读属性
		startSettingOption.setReadOnly(readOnly);
		dayOfWeekTypeSelector.setReadOnly(readOnly);
		daysOfWeekOption.setReadOnly(readOnly);
		startRedirectHour.setReadOnly(readOnly);
		startRedirectMinute.setReadOnly(readOnly);
		
		// 修改操作组件的可视化属性
		edit.setVisible(readOnly);
		save.setVisible(!readOnly);
		cancel.setVisible(!readOnly);
	}

	
}
