package com.jiangyifen.ec2.ui.csr.toolbar;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.bean.DayOfWeek;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.Phone2PhoneSetting;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.service.eaoservice.Phone2PhoneSettingService;
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
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * 话务员的外转外配置界面
 * 
 * @author jrh
 *
 */
@SuppressWarnings("serial")
public class CsrPhone2PhoneSettingWindow extends Window implements ClickListener, ValueChangeListener {
	// 日志工具
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	// 日期格式化工具
	private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");

	private Notification notification;				// 提示信息
	private Label noticeLabel;						// 提示便签，如果当前显示的是全局配置，则显示提示信息
	
	private OptionGroup startSettingOption;			// 全局配置选择（是否开启外转外）
	private ComboBox dayOfWeekTypeSelector;			// 转呼手机日期类型选择(工作日、周末、自定义)
	private OptionGroup daysOfWeekOption;			// 转接日期选择(星期一、星期二、星期三...)
	private HorizontalLayout dayOfWeekLayout;		// 存放dayOfWeekOption
	private ComboBox startRedirectHour;				// 开始呼叫手机的小时设置
	private ComboBox startRedirectMinute;			// 开始呼叫手机的分钟设置
	private ComboBox stopRedirectHour;				// 结束呼叫手机的小时设置
	private ComboBox stopRedirectMinute;			// 结束呼叫手机的分钟设置
	private OptionGroup redirectTypeOption;			// 转接时机选择(noanswer、unonline、busy)
	private ComboBox noanswerTimeoutSelector;		// noanswer 时，客户等待多久后转呼手机
	private HorizontalLayout noanswerLayout;		// 存放 noanswerTimeoutSelector

	// 操作按钮
	private Button edit;			// 编辑配置项
	private Button save;			// 保存配置项
	private Button cancel;			// 取消更改
	private HorizontalLayout operatorLayout;	// 存放以上三个按钮

	/**
	 * 其他参数
	 */
	private Domain domain;											// 当前登陆用户所属域
	private User loginUser;											// 当前登录对象
	private boolean isGlobalP2PSetting;								// 当前显示是否是全局外转外配置（当全局外转外配置开启，并且当前用户受控制的情况下）
	private Phone2PhoneSetting customP2PSetting;					// 全局外转外配置对象
	private Phone2PhoneSettingService phone2PhoneSettingService;	// 外转外配置服务类
	
	public CsrPhone2PhoneSettingWindow() {
		this.setWidth("413px");
		this.setHeight("220px");
		this.setResizable(false);
		
		VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.setWidth("100%");
		mainLayout.setSpacing(true);
		mainLayout.setMargin(true);
		this.setContent(mainLayout);

		domain = SpringContextHolder.getDomain();
		loginUser = SpringContextHolder.getLoginUser();
		phone2PhoneSettingService = SpringContextHolder.getBean("phone2PhoneSettingService");
		
		notification = new Notification("");
		notification.setDelayMsec(1000);
		notification.setHtmlContentAllowed(true);
		
		// 创建提示信息显示标签（如果当前显示的是全局配置，则该组件可见）
		String notice = "<font color='red'><B>您的外转外控制权已被管理员收回，当前是全局配置信息！</B></font>";
		noticeLabel = new Label(notice, Label.CONTENT_XHTML);
		noticeLabel.setVisible(false);
		mainLayout.addComponent(noticeLabel);
		
		// 初始化自定义外转外配置信息 
		this.customP2PSetting = initializeCustomP2PSetting();

		// 创建全局开启外转外设置项组件
		createlStartSetting(mainLayout);
		
		// 创建转接日期设置组件
		createlDaysOfWeekType(mainLayout);
		
		// 创建在一星期中那就天进行转接
		createlDayOfWeek(mainLayout);
		
		// 创建转呼手机的时间设置
		createRunRedirectTime(mainLayout);
		
		// 创建转接时机选择组件
		createlRedirectType(mainLayout);
		
		// 创建无人接听时，客户等多久才转呼手机 组件
		createNoanwserTimeout(mainLayout);

		// 创建操作组件
		createOperatorButtons(mainLayout);
	}

	@Override
	public void attach() {
		// 窗口居中
		this.center();
		this.isGlobalP2PSetting = false;
		
		// 判断 1、全局配置是否开启，2、启动时刻 <= 当前时刻，3、全局是否是指定了接听用的手机号（便捷呼叫），4、不是便捷话机的情况下，指定的话务员中是否包含自己
		Phone2PhoneSetting globalSetting = phone2PhoneSettingService.getGlobalSettingByDomain(domain.getId());
		if(globalSetting == null) {
			// 让组件值不可编辑
			changeComponentsStatus(true);
			noticeLabel.setVisible(true);
			edit.setVisible(false);
			return;
		}
		
		// 话务员不能编辑已经查看自定义的外转外配置的情况有：  TODO 第二种情况可能有变动
		if(!globalSetting.getIsLicensed2Csr()) {	// 1、话务员不持有自定义的授权；
			isGlobalP2PSetting = true;
		} else if(!globalSetting.getIsSpecifiedPhones()) {	// 2、全局外转外启动方式是“智能呼叫”，并且选择的话务员中包含当前话务员
			for(User csr : globalSetting.getSpecifiedCsrs()) {	// 包含当前话务员，则显示全局配置
				if(csr.getId() != null && csr.getId().equals(loginUser.getId())) {
					isGlobalP2PSetting = true;
					break;
				}
			}
		} else if(globalSetting.getIsStartedRedirect()) {	// 3、全局外转外启动方式是“便捷呼叫”，并且已经启动；
			boolean beforeNowGlobal = phone2PhoneSettingService.confirmSettingIsRunning(globalSetting);
			isGlobalP2PSetting = beforeNowGlobal ? true : false;	// 这样写为了方便阅读
		}
		
		if(isGlobalP2PSetting) {	// 回显全局配置项
			this.setCaption("全局外转外配置");
			refreshSettingInfos(globalSetting);
		} else {	// 回显自定义配置项
			this.setCaption("我的外转外配置");
			refreshSettingInfos(this.customP2PSetting);
		}
	}

	/**
	 * 初始化自定义外转外配置信息 
	 * @return
	 */
	private Phone2PhoneSetting initializeCustomP2PSetting() {
		Phone2PhoneSetting p2pSetting = phone2PhoneSettingService.getByUser(loginUser.getId());
		// 如果全局配置不存在，则创建并且设置默认值
		if(p2pSetting == null) {
			p2pSetting = new Phone2PhoneSetting();
			p2pSetting.setIsStartedRedirect(false);
			p2pSetting.setDayOfWeekType("weekday");

			Set<DayOfWeek> daysOfWeek = new HashSet<DayOfWeek>();
			daysOfWeek.add(DayOfWeek.mon);
			daysOfWeek.add(DayOfWeek.fri);
			p2pSetting.setDaysOfWeek(daysOfWeek);
			
			p2pSetting.setStartTime("18:00");
			p2pSetting.setStopTime("22:59");
			
			Set<String> types = new HashSet<String>();
			types.add("unonline");
			p2pSetting.setRedirectTypes(types);
			
			p2pSetting.setIsSpecifiedPhones(true);
			p2pSetting.setCreator(loginUser);
			p2pSetting.setDomain(domain);
		}
		return p2pSetting;
	}
	
	/**
	 * 创建全局开启外转外设置项组件
	 * @param mainLayout
	 */
	private void createlStartSetting(VerticalLayout mainLayout) {
		HorizontalLayout layout = new HorizontalLayout();
		layout.setSpacing(true);
		mainLayout.addComponent(layout);
		
		Label caption = new Label("外转外开启状态：");
		caption.setWidth("-1px");
		caption.setDescription("<B>如果选择‘开启’，则表示在指定的时间内，客户呼叫被选择的话务员时，按照呼叫时机打手机</B>");
		layout.addComponent(caption);
		
		startSettingOption = new OptionGroup();
		startSettingOption.addItem(true);
		startSettingOption.addItem(false);
		startSettingOption.setItemCaption(true, "开启");
		startSettingOption.setItemCaption(false, "关闭");
		startSettingOption.setImmediate(true);
		startSettingOption.setValue(false);
		startSettingOption.setReadOnly(true);
		startSettingOption.setDescription("<B>如果选择‘开启’，则表示在指定的时间内，客户呼叫被选择的话务员时，按照呼叫时机打手机</B>");
		startSettingOption.setNullSelectionAllowed(false);
		startSettingOption.addStyleName("twocol200");
		startSettingOption.addStyleName("myopacity");
		layout.addComponent(startSettingOption);
	}

	/**
	 * 创建转接日期设置组件 (今天、工作日、双休日、自定义)
	 * @param mainLayout
	 */
	private void createlDaysOfWeekType(VerticalLayout mainLayout) {
		HorizontalLayout layout = new HorizontalLayout();
		layout.setSpacing(true);
		mainLayout.addComponent(layout);
		
		Label caption = new Label("日期类型选择：");
		caption.setWidth("-1px");
		caption.setDescription("<B>指定在哪几天可以进行转呼手机</B>");
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
		dayOfWeekTypeSelector.setDescription("<B>指定在哪几天可以进行转呼手机</B>");
		dayOfWeekTypeSelector.setNullSelectionAllowed(false);
		dayOfWeekTypeSelector.addListener(this);
		layout.addComponent(dayOfWeekTypeSelector);
	}
	
	/**
	 * 创建在一星期中那就天进行转接(星期一、星期二、星期三...) 只在自定义选择时才需要显示
	 * @param mainLayout
	 */
	private void createlDayOfWeek(VerticalLayout mainLayout) {
		dayOfWeekLayout = new HorizontalLayout();
		dayOfWeekLayout.setSpacing(true);
		dayOfWeekLayout.setVisible(false);
		mainLayout.addComponent(dayOfWeekLayout);
		
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
		daysOfWeekOption.addStyleName("threecol300");
		daysOfWeekOption.addStyleName("myopacity");
		dayOfWeekLayout.addComponent(daysOfWeekOption);
	}
	
	/**
	 * 创建执行呼叫手机的具体时间设置 (18:00 - 23:59 )
	 * @param mainLayout
	 */
	private void createRunRedirectTime(VerticalLayout mainLayout) {
		HorizontalLayout layout = new HorizontalLayout();
		layout.setSpacing(true);
		mainLayout.addComponent(layout);
		
		Label caption = new Label("精确时间段设置：");
		caption.setWidth("-1px");
		caption.setDescription("<B>设置精确时间段</B>");
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

		Label to = new Label(" - ");
		to.setWidth("-1px");
		layout.addComponent(to);

		stopRedirectHour = new ComboBox();
		stopRedirectHour.setImmediate(true);
		stopRedirectHour.setWidth("50px");
		stopRedirectHour.setNullSelectionAllowed(false);
		layout.addComponent(stopRedirectHour);
		
		Label space2 = new Label(" ：");
		space2.setWidth("-1px");
		layout.addComponent(space2);

		stopRedirectMinute = new ComboBox();
		stopRedirectMinute.setImmediate(true);
		stopRedirectMinute.setWidth("50px");
		stopRedirectMinute.setNullSelectionAllowed(false);
		layout.addComponent(stopRedirectMinute);

		for(int hour = 0; hour < 24; hour++) {
			startRedirectHour.addItem(hour);
			stopRedirectHour.addItem(hour);
		}
		
		for(int minute = 0; minute < 60; minute++) {
			startRedirectMinute.addItem(minute);
			stopRedirectMinute.addItem(minute);
		}
		
		startRedirectHour.setValue(18);
		stopRedirectHour.setValue(23);
		startRedirectMinute.setValue(0);
		stopRedirectMinute.setValue(59);
		
		startRedirectHour.setReadOnly(true);
		startRedirectMinute.setReadOnly(true);
		stopRedirectHour.setReadOnly(true);
		stopRedirectMinute.setReadOnly(true);
	}
	
	/**
	 * 创建转接时机选择组件 (noanswer、unonline、busy、force)
	 * @param mainLayout
	 */
	private void createlRedirectType(VerticalLayout mainLayout) {
		HorizontalLayout layout = new HorizontalLayout();
		layout.setSpacing(true);
		mainLayout.addComponent(layout);
		
		Label caption = new Label("转呼时机选择：");
		caption.setWidth("-1px");
		caption.setDescription("<B>在哪些状态下才进行拨打话务员或指定的手机，可多选</B>");
		layout.addComponent(caption);
		
		redirectTypeOption = new OptionGroup();
		redirectTypeOption.addItem("noanswer");
		redirectTypeOption.setItemCaption("noanswer", "无人接听");
		redirectTypeOption.addItem("busy");
		redirectTypeOption.setItemCaption("busy", "忙碌中");
		redirectTypeOption.addItem("unonline");
		redirectTypeOption.setItemCaption("unonline", "未登录");
		redirectTypeOption.setNullSelectionAllowed(false);
		redirectTypeOption.addListener(this);
		redirectTypeOption.setImmediate(true);
		redirectTypeOption.setMultiSelect(true);
		redirectTypeOption.addStyleName("threecol300");
		redirectTypeOption.addStyleName("myopacity");
		redirectTypeOption.setReadOnly(true);
		redirectTypeOption.setDescription("<B>在哪些状态下才进行拨打话务员或指定的手机，可多选</B>");
		layout.addComponent(redirectTypeOption);
	}

	/**
	 * 创建无人接听时，客户等多久才转呼手机 组件
	 * @param mainLayout
	 */
	private void createNoanwserTimeout(VerticalLayout mainLayout) {
		noanswerLayout = new HorizontalLayout();
		noanswerLayout.setSpacing(true);
		noanswerLayout.setVisible(false);
		mainLayout.addComponent(noanswerLayout);
		
		Label freCaption = new Label("话务员必需在：");
		freCaption.setWidth("-1px");
		freCaption.setDescription("<B>当转接时机选中了'无人接听'时，如果话务员在指定的时间内没有接起的话，就转呼手机</B>");
		noanswerLayout.addComponent(freCaption);
		
		noanswerTimeoutSelector = new ComboBox();
		noanswerTimeoutSelector.setImmediate(true);
		noanswerTimeoutSelector.setWidth("50px");
		noanswerTimeoutSelector.setNullSelectionAllowed(false);
		noanswerTimeoutSelector.setDescription("<B>当转接时机选中了'无人接听'时，如果话务员在指定的时间内没有接起的话，就转呼手机</B>");
		noanswerLayout.addComponent(noanswerTimeoutSelector);
		
		for(int seconds = 3; seconds < 61; seconds++) {
			noanswerTimeoutSelector.addItem(seconds);
		}
		noanswerTimeoutSelector.setValue(10);
		noanswerTimeoutSelector.setReadOnly(true);
		
		Label postCaption = new Label(" 秒内接起");
		postCaption.setWidth("-1px");
		postCaption.setDescription("<B>当转接时机选中了'无人接听'时，如果话务员在指定的时间内没有接起的话，就转呼手机</B>");
		noanswerLayout.addComponent(postCaption);
	}

	/**
	 * 创建操作组件
	 * @return
	 */
	private void createOperatorButtons(VerticalLayout mainLayout) {
		operatorLayout = new HorizontalLayout();
		operatorLayout.setSpacing(true);
		mainLayout.addComponent(operatorLayout);
		
		// 编辑
		edit = new Button("编 辑", this);
		edit.setStyleName("default");
		edit.setVisible(true);
		edit.setImmediate(true);
		operatorLayout.addComponent(edit);
		
		// 保存
		save = new Button("保 存", this);
		save.setStyleName("default");
		save.setVisible(false);
		save.setImmediate(true);
		operatorLayout.addComponent(save);
		
		// 取消
		cancel = new Button("取 消", this);
		operatorLayout.addComponent(cancel);
		cancel.setVisible(false);
		cancel.setImmediate(true);
	}

	/**
	 * 回显配置信息
	 */
	private void refreshSettingInfos(Phone2PhoneSetting p2pSetting) {
		// 让组件值可编辑
		changeComponentsStatus(false);

		// 回显全局配置
		startSettingOption.setValue(p2pSetting.getIsStartedRedirect());
		// 回显日期类型选择
		dayOfWeekTypeSelector.setValue(p2pSetting.getDayOfWeekType());
		
		// 回显自定义信息
		Set<DayOfWeek> dayOfWeeks = p2pSetting.getDaysOfWeek();
		daysOfWeekOption.setValue(dayOfWeeks);
		
		// 回显执行转呼手机的时间段
		String[] startTimeStrs = p2pSetting.getStartTime().split(":");
		startRedirectHour.setValue(Integer.parseInt(startTimeStrs[0]));
		startRedirectMinute.setValue(Integer.parseInt(startTimeStrs[1]));

		String[] stopTimeStrs = p2pSetting.getStopTime().split(":");
		stopRedirectHour.setValue(Integer.parseInt(stopTimeStrs[0]));
		stopRedirectMinute.setValue(Integer.parseInt(stopTimeStrs[1]));
		
		// 回显转接类型
		Set<String> redirectTypes = p2pSetting.getRedirectTypes();
		redirectTypeOption.setValue(redirectTypes);
		// 呼分机时长
		noanswerLayout.setVisible(redirectTypes.contains("noanswer"));
		noanswerTimeoutSelector.setValue(p2pSetting.getNoanswerTimeout());
					
		// 让组件值不可编辑
		changeComponentsStatus(true);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void valueChange(ValueChangeEvent event) {
		Property source = event.getProperty();
		if(source == dayOfWeekTypeSelector) {
			String type = (String) dayOfWeekTypeSelector.getValue();
			boolean isCustomType = "custom".equals(type);
			dayOfWeekLayout.setVisible(isCustomType);
			if(this.getHeight() < 275 && isCustomType) {
				float height = this.getHeight() + 55;
				this.setHeight(height+"px");
			} else if(this.getHeight() >= 275 && !isCustomType) {
				float height = this.getHeight() - 55;
				this.setHeight(height+"px");
			}
			this.center();
		} else if(source == redirectTypeOption) {
			Set<String> redirectTypes = (Set<String>) redirectTypeOption.getValue();
			boolean iscontain = redirectTypes.contains("noanswer");
			if((this.getHeight() == 220 || this.getHeight() == 275) && iscontain) {
				float height = this.getHeight() + 15;
				this.setHeight(height+"px");
			} else if((this.getHeight() == 235 || this.getHeight() == 290) && !iscontain) {
				float height = this.getHeight() - 15;
				this.setHeight(height+"px");
			}
			if(noanswerLayout != null) {
				noanswerLayout.setVisible(iscontain);
			}
			this.center();
		} 
	}

	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if(source == edit) {
			changeComponentsStatus(false);
		} else if(source == save) {
			try {
				String[] runTimeScope = executeConfirmRunTime();
				if(runTimeScope != null) {
					executeSave(runTimeScope);
				}
				notification.setCaption("<B>保存成功!</B>");
				this.getApplication().getMainWindow().showNotification(notification);
				// 刷新界面
				refreshSettingInfos(this.customP2PSetting);
			} catch (Exception e) {
				logger.error("Manager 保存外转外配置信息失败 --> "+e.getMessage(), e);
				notification.setCaption("<font color='red'>保存失败，请检查信息后重试！</font>");
				this.getApplication().getMainWindow().showNotification(notification);
			}
		} else if(source == cancel) {
			refreshSettingInfos(this.customP2PSetting);
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
		stopRedirectHour.setReadOnly(readOnly);
		stopRedirectMinute.setReadOnly(readOnly);
		redirectTypeOption.setReadOnly(readOnly);
		noanswerTimeoutSelector.setReadOnly(readOnly);
		
		// 修改操作组件的可视化属性
		edit.setVisible(readOnly);
		save.setVisible(!readOnly);
		cancel.setVisible(!readOnly);
		
		noticeLabel.setVisible(isGlobalP2PSetting);
		operatorLayout.setVisible(!isGlobalP2PSetting);
	}

	/**
	 * 检查用户输入的开启时间和结束时间是否符合要求
	 * @return boolean 
	 * @throws ParseException
	 */
	private String[] executeConfirmRunTime() {
		String[] runTimeScope = new String[2];
		try {
			// 判断转呼手机的时间设置是否符合要求（开始时间 < 结束时间）
			StringBuffer startTimeStr = new StringBuffer();
			startTimeStr.append(((Integer)startRedirectHour.getValue()).toString());
			startTimeStr.append(":");
			startTimeStr.append(((Integer)startRedirectMinute.getValue()).toString());
			Date startDate = simpleDateFormat.parse(startTimeStr.toString());
			
			StringBuffer stopTimeStr = new StringBuffer();
			stopTimeStr.append(((Integer)stopRedirectHour.getValue()).toString());
			stopTimeStr.append(":");
			stopTimeStr.append(((Integer)stopRedirectMinute.getValue()).toString());
			Date stopDate = simpleDateFormat.parse(stopTimeStr.toString());
			if(startDate.after(stopDate)) {
				notification.setCaption("<font color='red'>开启转呼手机的时间必须小于停止转呼手机的时间，如18:00-22:00!</font>");
				save.getApplication().getMainWindow().showNotification(notification);
				return null;
			} 
			
			runTimeScope[0] = startTimeStr.toString();
			runTimeScope[1] = stopTimeStr.toString();
		} catch (Exception e) {
			logger.error("Manager 界面 将字符串解析成日期类型时失败 ---> "+e.getMessage(), e);
			notification.setCaption("<font color='red'>保存失败请重试!</font>");
			this.getApplication().getMainWindow().showNotification(notification);
			return null;
		}
		return runTimeScope;
	}
	
	/**
	 * 保存编辑后的自定义外转外配置信息
	 */
	@SuppressWarnings("unchecked")
	private void executeSave(String[] runTimeScope) {
		// 如果当前还没有自定义的外转外配置，则新建一个
		if(customP2PSetting == null) {
			customP2PSetting = new Phone2PhoneSetting();
		}
		// 获取当前新的值
		Boolean isStartSetting = (Boolean) startSettingOption.getValue();
		String dayOfWeekType = (String) dayOfWeekTypeSelector.getValue();
		Set<DayOfWeek> daysOfWeek = (Set<DayOfWeek>) daysOfWeekOption.getValue();
		Set<String> redirectTypes = (Set<String>) redirectTypeOption.getValue();
		Integer noanswerTimeout = (Integer) noanswerTimeoutSelector.getValue();
		
		// 更新对象的各字段的值
		customP2PSetting.setIsStartedRedirect(isStartSetting);
		customP2PSetting.setDayOfWeekType(dayOfWeekType);
		if("custom".equals(dayOfWeekType)) {
			customP2PSetting.setDaysOfWeek(daysOfWeek);
		}
		customP2PSetting.setStartTime(runTimeScope[0]);
		customP2PSetting.setStopTime(runTimeScope[1]);
		customP2PSetting.setRedirectTypes(redirectTypes);
		customP2PSetting.setNoanswerTimeout(noanswerTimeout);
		
		// 保存信息
		customP2PSetting = phone2PhoneSettingService.update(customP2PSetting);
		
		// 创建或终止定时任务
		if(customP2PSetting.getIsStartedRedirect()) {
			phone2PhoneSettingService.createP2PScheduler(customP2PSetting);
		} else {
			phone2PhoneSettingService.stopP2PScheduler(customP2PSetting);
		}
	}

}
