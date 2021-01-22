package com.jiangyifen.ec2.ui.mgr.system.tabsheet;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.bean.DayOfWeek;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.Phone2PhoneSetting;
import com.jiangyifen.ec2.entity.Queue;
import com.jiangyifen.ec2.entity.StaticQueueMember;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.entity.UserQueue;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.service.csr.ami.QueueMemberRelationService;
import com.jiangyifen.ec2.service.eaoservice.Phone2PhoneSettingService;
import com.jiangyifen.ec2.service.eaoservice.QueueService;
import com.jiangyifen.ec2.service.eaoservice.StaticQueueMemberService;
import com.jiangyifen.ec2.service.eaoservice.UserQueueService;
import com.jiangyifen.ec2.service.eaoservice.UserService;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.filter.Like;
import com.vaadin.data.util.filter.Or;
import com.vaadin.event.DataBoundTransferable;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.event.dd.acceptcriteria.ClientSideCriterion;
import com.vaadin.event.dd.acceptcriteria.SourceIs;
import com.vaadin.ui.AbstractSelect.AcceptItem;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.TableDragMode;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;

/**
 * 外传外配置管理界面
 * @author jrh
 */
@SuppressWarnings("serial")
public class MgrPhone2PhoneSettingView extends VerticalLayout implements ValueChangeListener, ClickListener {
	// 日志工具
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	// 日期格式化工具
	private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
	// 职工表格信息显示列
	private final Object[] VISIBLE_PROPERTIES = new String[] {"empNo", "username", "realName", "phoneNumber", "department.name" };
	private final String[] COL_HEADERS = new String[] {"工号", "用户名", "姓名", "电话", "部门" };

	private Notification notification;				// 提示信息
	
	private Panel panel;							// 作为一下组件的容器
	private OptionGroup startSettingOption;			// 全局配置选择（是否开启外转外）
	private OptionGroup licensed2CsrOption;			// 授权给CSR （是否授权给CSR）
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
	private OptionGroup specifiedOption;			// 是否指定手机强制转（智能呼叫：管理员对那些选中的话务员是完全控制外转外的）
	private TextArea phoneArea;						// 强制转接的电话号码
	private HorizontalLayout phoneAreaLayout;		// 存放 phoneArea
	
	private HorizontalLayout csrSelectHLayout;		// 存放以下选CSR 的各种组件
	// 左
	private TextField leftKeyword;	// 可选CSR 成员表格搜索框
	private Button leftSearch;		// 搜索按钮
	// 右
	private TextField rightKeyword;	// 已选CSR 成员表格搜索框
	private Button rightSearch;		// 搜索按钮

	// 中间的添加按钮
	private Button addAll;			// 添加所有CSR
	private Button add;				// 添加选中的CSR
	private Button remove;			// 移除选中的CSR
	private Button removeAll;		// 移除所有CSR

	// 表格
	private Table leftTable;		// 可选CSR 成员显示表格
	private Table rightTable;		// 已选CSR 成员显示表格
	
	private BeanItemContainer<User> leftTableContainer;
	private BeanItemContainer<User> rightTableContainer;

	// 操作按钮
	private Button edit;			// 编辑配置项
	private Button save;			// 保存配置项
	private Button cancel;			// 取消更改
	
	/**
	 * 其他参数
	 */
	private Domain domain;											// 当前登陆用户所属域
	private User loginUser;											// 当前登录对象
	private Phone2PhoneSetting globalP2PSetting;					// 全局外转外配置对象
	
	private UserService userService;								// 用户服务类
	private QueueService queueService;								// 队列服务类
	private UserQueueService userQueueService;						// 动态队列成员服务类
	private QueueMemberRelationService queueMemberRelationService;	// 队列成员关系管理服务类
	private Phone2PhoneSettingService phone2PhoneSettingService;	// 外转外配置服务类
	private StaticQueueMemberService staticQueueMemberService; 		// 静态队列成员服务类
	
	public MgrPhone2PhoneSettingView() {
		this.setMargin(true);
		this.setWidth("100%");
		this.setSpacing(true);
		
		domain = SpringContextHolder.getDomain();
		loginUser = SpringContextHolder.getLoginUser();
		userService = SpringContextHolder.getBean("userService");
		queueService = SpringContextHolder.getBean("queueService");
		userQueueService = SpringContextHolder.getBean("userQueueService");
		queueMemberRelationService = SpringContextHolder.getBean("queueMemberRelationService");
		phone2PhoneSettingService = SpringContextHolder.getBean("phone2PhoneSettingService");
		staticQueueMemberService = SpringContextHolder.getBean("staticQueueMemberService");

		notification = new Notification("");
		notification.setDelayMsec(1000);
		notification.setHtmlContentAllowed(true);
		
		VerticalLayout panelContent = new VerticalLayout();
		panelContent.setSpacing(true);
		panelContent.setMargin(true);
		panelContent.setWidth("100%");
		
		panel = new Panel("配置项");
		panel.setContent(panelContent);
		panel.setStyleName("light");
		this.addComponent(panel);
		
		// 创建全局开启外转外设置项组件
		createStartSetting(panelContent);
		
		// 创建授权CSR 自定义设置外转外配置项
		createLicensed2Csr(panelContent);
		
		// 创建转接日期设置组件
		createDaysOfWeekType(panelContent);
		
		// 创建在一星期中那就天进行转接
		createDayOfWeek(panelContent);
		
		// 创建转呼手机的时间设置
		createRunRedirectTime(panelContent);
		
		// 创建转接时机选择组件
		createRedirectType(panelContent);
		
		// 创建无人接听时，客户等多久才转呼手机 组件
		createNoanwserTimeout(panelContent);
		
		// 创建是否开启强制转户指定手机选择项组件
		createSpecifiedPhone(panelContent);
		
		// 创建指定强制转呼的手机号码输入框
		createPhoneArea(panelContent);
		
		// 创建需要当有客户呼入时，需要转呼手机的话务员双表格选择组件
		createCsrSelectTables(panelContent);
		
		// 添加拖拽支持
		makeTableDragAble(new SourceIs(rightTable), leftTable, true);
		makeTableDragAble(new SourceIs(leftTable), rightTable, false);
		
		// 创建操作组件
		HorizontalLayout operators = createOperatorButtons();
		this.addComponent(operators);
	}

	@Override
	public void attach() {
		// 获取域下的全局配置项
		Phone2PhoneSetting globalP2PSetting = phone2PhoneSettingService.getGlobalSettingByDomain(domain.getId());
		// 如果全局配置不存在，则创建并且设置默认值
		if(globalP2PSetting == null) {
			globalP2PSetting = new Phone2PhoneSetting();
			globalP2PSetting.setIsGlobalSetting(true);
			globalP2PSetting.setIsStartedRedirect(false);
			globalP2PSetting.setIsLicensed2Csr(false);
			globalP2PSetting.setDayOfWeekType("weekday");

			Set<DayOfWeek> daysOfWeek = new HashSet<DayOfWeek>();
			daysOfWeek.add(DayOfWeek.mon);
			daysOfWeek.add(DayOfWeek.fri);
			globalP2PSetting.setDaysOfWeek(daysOfWeek);
			
			globalP2PSetting.setStartTime("18:00");
			globalP2PSetting.setStopTime("22:59");
			
			Set<String> types = new HashSet<String>();
			types.add("unonline");
			globalP2PSetting.setRedirectTypes(types);
			
			globalP2PSetting.setIsSpecifiedPhones(false);
			globalP2PSetting.setCreator(loginUser);
			globalP2PSetting.setDomain(domain);
		}
		this.globalP2PSetting = globalP2PSetting;
		
		// 回显全局配置项
		refreshSettingInfos(globalP2PSetting);
	}

	/**
	 * 创建全局开启外转外设置项组件
	 * @param panelContent
	 */
	private void createStartSetting(VerticalLayout panelContent) {
		HorizontalLayout layout = new HorizontalLayout();
		layout.setSpacing(true);
		panelContent.addComponent(layout);
		
		Label caption = new Label("全局开启外转外：");
		caption.setWidth("-1px");
		caption.setDescription("<B>如果选择‘开启’，则表示在指定的时间内，客户呼叫被选择的话务员时，按照呼叫时机打手机</B>");
		layout.addComponent(caption);
		
		startSettingOption = new OptionGroup();
		startSettingOption.addItem(true);
		startSettingOption.addItem(false);
		startSettingOption.setItemCaption(true, "开启");
		startSettingOption.setItemCaption(false, "关闭");
		startSettingOption.setImmediate(true);
		startSettingOption.setReadOnly(true);
		startSettingOption.setDescription("<B>如果选择‘开启’，则表示在指定的时间内，客户呼叫被选择的话务员时，按照呼叫时机打手机</B>");
		startSettingOption.setNullSelectionAllowed(false);
		startSettingOption.addStyleName("twocol200");
		startSettingOption.addStyleName("myopacity");
		layout.addComponent(startSettingOption);
	}

	/**
	 * 创建授权CSR 自定义设置外转外配置项
	 * @param panelContent
	 */
	private void createLicensed2Csr(VerticalLayout panelContent) {
		HorizontalLayout layout = new HorizontalLayout();
		layout.setSpacing(true);
		panelContent.addComponent(layout);
		
		Label caption = new Label("允许授权话务员：");
		caption.setWidth("-1px");
		caption.setDescription("<B>如果选择‘是’，则表示话务员可以自己开启转手机</B>");
		layout.addComponent(caption);

		licensed2CsrOption = new OptionGroup();
		licensed2CsrOption.addItem(true);
		licensed2CsrOption.addItem(false);
		licensed2CsrOption.setItemCaption(true, "是");
		licensed2CsrOption.setItemCaption(false, "否");
		licensed2CsrOption.setImmediate(true);
		licensed2CsrOption.setReadOnly(true);
		licensed2CsrOption.setDescription("<B>如果选择‘是’，则表示话务员可以自己开启转手机</B>");
		licensed2CsrOption.setNullSelectionAllowed(false);
		licensed2CsrOption.addStyleName("twocol200");
		licensed2CsrOption.addStyleName("myopacity");
		layout.addComponent(licensed2CsrOption);
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
		container.addBean(DayOfWeek.mon);
		container.addBean(DayOfWeek.tue);
		container.addBean(DayOfWeek.wen);
		container.addBean(DayOfWeek.thu);
		container.addBean(DayOfWeek.fri);
		container.addBean(DayOfWeek.sat);
		container.addBean(DayOfWeek.sun);
		
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
	 * 创建执行呼叫手机的具体时间设置 (18:00 - 23:59 )
	 * @param panelContent
	 */
	private void createRunRedirectTime(VerticalLayout panelContent) {
		HorizontalLayout layout = new HorizontalLayout();
		layout.setSpacing(true);
		panelContent.addComponent(layout);
		
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
	 * @param panelContent
	 */
	private void createRedirectType(VerticalLayout panelContent) {
		HorizontalLayout layout = new HorizontalLayout();
		layout.setSpacing(true);
		panelContent.addComponent(layout);
		
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
		redirectTypeOption.addStyleName("threecol");
		redirectTypeOption.addStyleName("myopacity");
		redirectTypeOption.setReadOnly(true);
		redirectTypeOption.setDescription("<B>在哪些状态下才进行拨打话务员或指定的手机，可多选</B>");
		layout.addComponent(redirectTypeOption);
	}
	
	/**
	 * 创建无人接听时，客户等多久才转呼手机 组件
	 * @param panelContent
	 */
	private void createNoanwserTimeout(VerticalLayout panelContent) {
		noanswerLayout = new HorizontalLayout();
		noanswerLayout.setSpacing(true);
		noanswerLayout.setVisible(false);
		panelContent.addComponent(noanswerLayout);
		
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
	 * 创建是否开启强制转呼叫指定手机选择项组件
	 * @param panelContent
	 */
	private void createSpecifiedPhone(VerticalLayout panelContent) {
		HorizontalLayout layout = new HorizontalLayout();
		layout.setSpacing(true);
		panelContent.addComponent(layout);
		
		Label caption = new Label("呼叫方式选择：");
		caption.setWidth("-1px");
		caption.setDescription("<B>'智能呼叫'：指自动查找话务员拥有的手机进行呼叫；'便捷呼叫'：指所有的呼入都呼到固定的手机上</B>");
		layout.addComponent(caption);
		
		specifiedOption = new OptionGroup();
		specifiedOption.addItem(false);
		specifiedOption.addItem(true);
		specifiedOption.setItemCaption(false, "智能呼叫");
		specifiedOption.setItemCaption(true, "便捷呼叫");
		specifiedOption.addListener(this);
		specifiedOption.setImmediate(true);
		specifiedOption.setReadOnly(true);
		specifiedOption.setNullSelectionAllowed(false);
		specifiedOption.addStyleName("twocol200");
		specifiedOption.addStyleName("myopacity");
		specifiedOption.setDescription("<B>'智能呼叫'：指自动查找话务员拥有的手机进行呼叫；'便捷呼叫'：指所有的呼入都呼到固定的手机上</B>");
		layout.addComponent(specifiedOption);
	}

	/**
	 * 创建指定强制转呼的手机号码输入框
	 * @param panelContent
	 */
	private void createPhoneArea(VerticalLayout panelContent) {
		phoneAreaLayout = new HorizontalLayout();
		phoneAreaLayout.setWidth("100%");
		phoneAreaLayout.setVisible(false);
		phoneAreaLayout.setSpacing(true);
		panelContent.addComponent(phoneAreaLayout);
		
		Label caption = new Label("指定外呼手机：");
		caption.setWidth("-1px");
		caption.setDescription("<B>当选择了'便捷呼叫'方式时，所有的呼入都会往下面的电话转</B>");
		phoneAreaLayout.addComponent(caption);
		
		phoneArea = new TextArea();
		phoneArea.setRows(6);
		phoneArea.setWidth("100%");
		phoneArea.setImmediate(true);
		phoneArea.setReadOnly(true);
		phoneArea.setInputPrompt("请输入需要强制转接到的电话或手机号, 多个号码之间用逗号(,)隔开！");
		phoneArea.setDescription("<B>当选择了'便捷呼叫'方式时，所有的呼入都会往下面的电话转</B>");
		phoneAreaLayout.addComponent(phoneArea);
		phoneAreaLayout.setExpandRatio(phoneArea, 1.0f);
	}
	
	/**
	 *  创建需要当有客户呼入时，需要转呼手机的话务员双表格选择组件
	 * @param panelContent
	 */
	private void createCsrSelectTables(VerticalLayout panelContent) {
		csrSelectHLayout = new HorizontalLayout();
		csrSelectHLayout.setSpacing(true);
		csrSelectHLayout.setVisible(false);
		csrSelectHLayout.setWidth("100%");
		panelContent.addComponent(csrSelectHLayout);
		
		Label caption = new Label("须转手机人员：");
		caption.setWidth("-1px");
		caption.setDescription("<B>指定当客户拨打哪些话务员的分机时，需要转拨手机</B>");
		csrSelectHLayout.addComponent(caption);

		// 存放表格的面板布局管理器
		HorizontalLayout panelLayout = new HorizontalLayout();
		panelLayout.setSpacing(true);
		panelLayout.setMargin(false, true, false, true);
		panelLayout.setWidth("100%");

		Panel tablePanel = new Panel();
		tablePanel.setContent(panelLayout);
		csrSelectHLayout.addComponent(tablePanel);
		csrSelectHLayout.setExpandRatio(tablePanel, 1.0f);
		
		// 创建左侧组件(左侧用户显示表格、左侧表格的搜索组件)
		VerticalLayout leftComponents = createLeftComponents();
		panelLayout.addComponent(leftComponents);
		panelLayout.setExpandRatio(leftComponents, 0.4f);
		
		// 创建左右两侧表格中间的操作按钮("部分移动、全部移动"按钮)
		VerticalLayout middleComponents = createMiddleComponents();
		panelLayout.addComponent(middleComponents);
		panelLayout.setComponentAlignment(middleComponents, Alignment.MIDDLE_CENTER);
		panelLayout.setExpandRatio(middleComponents, 0.2f);

		// 创建右侧组件(右侧用户显示表格、右侧表格的搜索组件)
		VerticalLayout rightComponents = createRightComponents();
		panelLayout.addComponent(rightComponents);
		panelLayout.setExpandRatio(rightComponents, 0.4f);
	}

	/**
	 *  创建主界面左侧组件(左侧用户显示表格、左侧表格的搜索组件)
	 * @return
	 */
	 
	private VerticalLayout createLeftComponents() {
		VerticalLayout leftVLayout = new VerticalLayout();
		leftVLayout.setSpacing(true);
		leftVLayout.setWidth("100%");

		// 创建搜索分机的相应组件
		HorizontalLayout searchHLayout = new HorizontalLayout();
		searchHLayout.setSpacing(true);
		leftVLayout.addComponent(searchHLayout);

		Label caption = new Label("关键字：");
		caption.setWidth("-1px");
		searchHLayout.addComponent(caption);
		searchHLayout.setComponentAlignment(caption, Alignment.MIDDLE_CENTER);

		leftKeyword = new TextField();
		leftKeyword.setImmediate(true);
		leftKeyword.setInputPrompt("请输入搜索关键字");
		leftKeyword.setDescription("可按工号、用户名、姓名及部门名称搜索！");
		leftKeyword.setStyleName("search");
		leftKeyword.addListener(this);
		leftKeyword.setEnabled(false);
		searchHLayout.addComponent(leftKeyword);
		searchHLayout.setComponentAlignment(leftKeyword, Alignment.MIDDLE_CENTER);

		leftSearch = new Button("搜索", this);
		leftSearch.setImmediate(true);
		leftSearch.setEnabled(false);
		searchHLayout.addComponent(leftSearch);
		searchHLayout.setComponentAlignment(leftSearch, Alignment.MIDDLE_CENTER);

		// 创建表格组件
		leftTable = new Table("可选人员");
		leftTable.addStyleName("striped");
		leftTable.addStyleName("mydisabled");
		leftTable.setSelectable(true);
		leftTable.setMultiSelect(true);
		leftTable.setWidth("100%");
		leftTable.setRowHeaderMode(Table.ROW_HEADER_MODE_INDEX);
		leftVLayout.addComponent(leftTable);

		leftTableContainer = new BeanItemContainer<User>(User.class);
		leftTableContainer.addNestedContainerProperty("department.name");
		leftTable.setContainerDataSource(leftTableContainer);
		leftTable.setPageLength(10);
		leftTable.setVisibleColumns(VISIBLE_PROPERTIES);
		leftTable.setColumnHeaders(COL_HEADERS);
		
		return leftVLayout;
	}

	/**
	 * 创建左右两侧表格中间的操作按钮("部分移动、全部移动"按钮)
	 * return 
	 */
	private VerticalLayout createMiddleComponents() {
		VerticalLayout operatorVLayout = new VerticalLayout();
		operatorVLayout.setSpacing(true);
		operatorVLayout.setSizeFull();

		// 占位组件
		operatorVLayout.addComponent(new Label("&nbsp&nbsp", Label.CONTENT_XHTML));
		operatorVLayout.addComponent(new Label("&nbsp&nbsp", Label.CONTENT_XHTML));
		operatorVLayout.addComponent(new Label("&nbsp&nbsp", Label.CONTENT_XHTML));

		// 按钮组件
		addAll = new Button(">>>", this);
		addAll.setEnabled(false);
		operatorVLayout.addComponent(addAll);
		operatorVLayout.setComponentAlignment(addAll, Alignment.MIDDLE_CENTER);

		add = new Button(">>", this);
		add.setEnabled(false);
		operatorVLayout.addComponent(add);
		operatorVLayout.setComponentAlignment(add, Alignment.MIDDLE_CENTER);

		remove = new Button("<<", this);
		remove.setEnabled(false);
		operatorVLayout.addComponent(remove);
		operatorVLayout.setComponentAlignment(remove, Alignment.MIDDLE_CENTER);

		removeAll = new Button("<<<", this);
		removeAll.setEnabled(false);
		operatorVLayout.addComponent(removeAll);
		operatorVLayout.setComponentAlignment(removeAll, Alignment.MIDDLE_CENTER);
		
		// 占位组件
		operatorVLayout.addComponent(new Label("&nbsp&nbsp", Label.CONTENT_XHTML));
		operatorVLayout.addComponent(new Label("&nbsp&nbsp", Label.CONTENT_XHTML));
		
		return operatorVLayout;
	}

	/**
	 * 创建主界面右侧组件(右侧用户显示表格、右侧表格的搜索组件)
	 * return 
	 */
	private VerticalLayout createRightComponents() {
		VerticalLayout rightVLayout = new VerticalLayout();
		rightVLayout.setSpacing(true);
		rightVLayout.setWidth("100%");

		// 创建搜索分机的相应组件
		HorizontalLayout searchHLayout = new HorizontalLayout();
		searchHLayout.setSpacing(true);
		rightVLayout.addComponent(searchHLayout);

		Label caption = new Label("关键字：");
		caption.setWidth("-1px");
		searchHLayout.addComponent(caption);
		searchHLayout.setComponentAlignment(caption, Alignment.MIDDLE_CENTER);

		rightKeyword = new TextField();
		rightKeyword.setImmediate(true);
		rightKeyword.setInputPrompt("请输入搜索关键字");
		rightKeyword.setDescription("可按工号、用户名、姓名及部门名称搜索！");
		rightKeyword.setStyleName("search");
		rightKeyword.addListener(this);
		rightKeyword.setEnabled(false);
		searchHLayout.addComponent(rightKeyword);
		searchHLayout.setComponentAlignment(rightKeyword, Alignment.MIDDLE_CENTER);

		rightSearch = new Button("搜索", this);
		rightSearch.setImmediate(true);
		rightSearch.setEnabled(false);
		searchHLayout.addComponent(rightSearch);
		searchHLayout.setComponentAlignment(rightSearch, Alignment.MIDDLE_CENTER);

		// 创建表格组件
		rightTable = new Table("已选人员");
		rightTable.addStyleName("striped");
		rightTable.addStyleName("mydisabled");
		rightTable.setSelectable(true);
		rightTable.setMultiSelect(true);
		rightTable.setWidth("100%");
		rightTable.setPageLength(10);
		rightTable.setRowHeaderMode(Table.ROW_HEADER_MODE_INDEX);
		rightVLayout.addComponent(rightTable);

		rightTableContainer = new BeanItemContainer<User>(User.class);
		rightTableContainer.addNestedContainerProperty("department.name");   
		rightTable.setContainerDataSource(rightTableContainer);
		rightTable.setVisibleColumns(VISIBLE_PROPERTIES);
		rightTable.setColumnHeaders(COL_HEADERS);
		
		return rightVLayout;
	}

	/**
	 * 实现表格的拖曳功能
	 * @param acceptCriterion
	 * @param table
	 * @param isDragout 是否为从 UserQueues 中减少成员
	 */
	private void makeTableDragAble(final ClientSideCriterion acceptCriterion, final Table table, final boolean isDragout) {
		table.setDragMode(TableDragMode.ROW);
		table.setDropHandler(new DropHandler() {
			public void drop(DragAndDropEvent dropEvent) {
				// 验证传递过来的是否是可传递的对象
				DataBoundTransferable transferable = (DataBoundTransferable) dropEvent.getTransferable();
				
				// 不是BeanItemContainer则不响应，直接返回
				if (!(transferable.getSourceContainer() instanceof BeanItemContainer)) {
					return;
				}
	
				// 获取源sourceItemId
				User sourceItem = (User) transferable.getItemId();
	
				// 选中要Drop到的targetItemId
				table.getContainerDataSource().addItem(sourceItem);
				transferable.getSourceContainer().removeItem(sourceItem);
	
				// 按工号的升序排列
				leftTableContainer.sort(new Object[] {"empNo"}, new boolean[] {true});
				rightTableContainer.sort(new Object[] {"empNo"}, new boolean[] {true});
	
				// 每一次移除表格中的对象后，就重新设置左右两个表格的标题
				initializeTablesCaption();
			}
	
			public AcceptCriterion getAcceptCriterion() {
				return new com.vaadin.event.dd.acceptcriteria.And(acceptCriterion, AcceptItem.ALL);
			}
		});
	}

	/**
	 * 创建操作组件
	 * @return
	 */
	private HorizontalLayout createOperatorButtons() {
		HorizontalLayout layout = new HorizontalLayout();
		layout.setSpacing(true);
		
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
		
		return layout;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void valueChange(ValueChangeEvent event) {
		Property source = event.getProperty();
		if(source == dayOfWeekTypeSelector) {
			String type = (String) dayOfWeekTypeSelector.getValue();
			dayOfWeekLayout.setVisible("custom".equals(type));
		} else if(source == specifiedOption) {
			Boolean isSpecified = (Boolean) specifiedOption.getValue();
			phoneAreaLayout.setVisible(isSpecified);
			csrSelectHLayout.setVisible(!isSpecified);
		} else if(source == redirectTypeOption) {
			Set<String> redirectTypes = (Set<String>) redirectTypeOption.getValue();
			boolean iscontain = redirectTypes.contains("noanswer");
			if(noanswerLayout != null) {
				noanswerLayout.setVisible(iscontain);
			}
		} 
	}

	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if(source == edit) {
			changeComponentsStatus(false);
		} else if(source == save) {
			try {
				Set<String> usefullPhones = excuteConfirmPhoneNum();
				String[] runTimeScope = executeConfirmRunTime();
				if(runTimeScope != null) {
					executeSave(runTimeScope, usefullPhones);
					notification.setCaption("<B>保存成功!</B>");
					this.getApplication().getMainWindow().showNotification(notification);
					// 刷新界面
					refreshSettingInfos(this.globalP2PSetting);
				} 
			} catch (Exception e) {
				logger.error("Manager 保存外转外配置信息失败 --> "+e.getMessage(), e);
				notification.setCaption("<font color='red'>保存失败，请检查信息后重试！</font>");
				this.getApplication().getMainWindow().showNotification(notification);
			}
		} else if(source == cancel) {
			refreshSettingInfos(this.globalP2PSetting);
		} else if(source == leftSearch) {
			executeLeftSearch();
		} else if(source == rightSearch) {
			executeRightSearch();
		} else if (source == add) {
			addToOpposite(leftTable, rightTable, false);
			leftTable.setValue(null);
		} else if (source == addAll) {
			addToOpposite(leftTable, rightTable, true);
		} else if (source == remove) {
			addToOpposite(rightTable, leftTable, false);
		} else if (source == removeAll) {
			addToOpposite(rightTable, leftTable, true);
			rightTable.setValue(null);
		} 
	}

	/**
	 * 改变各组件的可编辑状态
	 * @param readOnly
	 */
	private void changeComponentsStatus(boolean readOnly) {
		// 修改配置组件的只读属性
		startSettingOption.setReadOnly(readOnly);
		licensed2CsrOption.setReadOnly(readOnly);
		dayOfWeekTypeSelector.setReadOnly(readOnly);
		daysOfWeekOption.setReadOnly(readOnly);
		startRedirectHour.setReadOnly(readOnly);
		startRedirectMinute.setReadOnly(readOnly);
		stopRedirectHour.setReadOnly(readOnly);
		stopRedirectMinute.setReadOnly(readOnly);
		redirectTypeOption.setReadOnly(readOnly);
		noanswerTimeoutSelector.setReadOnly(readOnly);
		specifiedOption.setReadOnly(readOnly);
		phoneArea.setReadOnly(readOnly);
		
		// 设置文本框、按钮的可用属性
		leftKeyword.setEnabled(!readOnly);
		leftSearch.setEnabled(!readOnly);
		rightKeyword.setEnabled(!readOnly);
		rightSearch.setEnabled(!readOnly);
		rightTable.setEnabled(!readOnly);
		leftTable.setEnabled(!readOnly);
		addAll.setEnabled(!readOnly);
		add.setEnabled(!readOnly);
		remove.setEnabled(!readOnly);
		removeAll.setEnabled(!readOnly);
		
		// 修改操作组件的可视化属性
		edit.setVisible(readOnly);
		save.setVisible(!readOnly);
		cancel.setVisible(!readOnly);
	}

	/**
	 * 回显配置信息
	 */
	private void refreshSettingInfos(Phone2PhoneSetting globalP2PSetting) {
		// 回显信息
		if(globalP2PSetting != null) {	// 对于全局配置而言，这里是肯定成立的
			// 让组件值可编辑
			changeComponentsStatus(false);
			
			// 回显全局配置
			startSettingOption.setValue(globalP2PSetting.getIsStartedRedirect());
			// 回显授权开启信息
			licensed2CsrOption.setValue(globalP2PSetting.getIsLicensed2Csr());
			// 回显日期类型选择
			dayOfWeekTypeSelector.setValue(globalP2PSetting.getDayOfWeekType());
			
			// 回显自定义信息
			Set<DayOfWeek> dayOfWeeks = globalP2PSetting.getDaysOfWeek();
			daysOfWeekOption.setValue(dayOfWeeks);
			
			// 回显执行转呼手机的时间段
			String[] startTimeStrs = globalP2PSetting.getStartTime().split(":");
			startRedirectHour.setValue(Integer.parseInt(startTimeStrs[0]));
			startRedirectMinute.setValue(Integer.parseInt(startTimeStrs[1]));

			String[] stopTimeStrs = globalP2PSetting.getStopTime().split(":");
			stopRedirectHour.setValue(Integer.parseInt(stopTimeStrs[0]));
			stopRedirectMinute.setValue(Integer.parseInt(stopTimeStrs[1]));
			
			// 回显转接类型
			Set<String> redirectTypes = globalP2PSetting.getRedirectTypes();
			redirectTypeOption.setValue(redirectTypes);
			// 呼分机时长
			noanswerLayout.setVisible(redirectTypes.contains("noanswer"));
			noanswerTimeoutSelector.setValue(globalP2PSetting.getNoanswerTimeout());
			
			leftTableContainer.removeAllItems();
			leftTableContainer.addAll(userService.getCsrsByDomain(domain));
			
			// 回显是否强制转手机
			specifiedOption.setValue(globalP2PSetting.getIsSpecifiedPhones());
			
			// 回显设置的强制转手机的号码
			StringBuffer phoneNumstrs = new StringBuffer();
			for(String phoneNum : globalP2PSetting.getSpecifiedPhones()) {
				phoneNumstrs.append(phoneNum);
				phoneNumstrs.append(",");
			}
			String phoneNums = phoneNumstrs.toString();
			if(phoneNums.endsWith(",")) {
				phoneNums = phoneNums.substring(0, phoneNums.length() - 1);
			}
			phoneArea.setValue(phoneNums);
			
			// 回显需要转接手机的话务员表格的内容
			List<User> allCsrs = userService.getCsrsByDomain(domain);
			Set<User> oldSelectedCsrs = globalP2PSetting.getSpecifiedCsrs();
			leftTableContainer.removeAllItems();
			rightTableContainer.removeAllItems();
			for(User user : allCsrs) {
				if(oldSelectedCsrs.size() == 0) {
					leftTableContainer.addAll(allCsrs);
					break;
				}
				// 用于标示当前user 对象是否应该加入到右侧表格当做，如果是，则将其值置为 true ,默认为不是
				boolean needAddToRightTable = false;
				for(User csr : oldSelectedCsrs) {
					if(user.getId().equals(csr.getId())) {
						rightTableContainer.addBean(user);
						needAddToRightTable = true;
						break;
					}
				}
				
				// 如果当前user 不是加入到右侧表格中的，那么就将其加入左侧表格
				if(needAddToRightTable == false) {
					leftTableContainer.addBean(user);
				}
			}
		}
		
		// 让组件值不可编辑
		changeComponentsStatus(true);
	}

	/**
	 * 由buttonClick调用，执行生成左侧Table的过滤器,并刷新Table的Container
	 */
	private void executeLeftSearch() {
		if(leftTableContainer==null) return;
		
		// 删除之前的所有过滤器
		leftTableContainer.removeAllContainerFilters();
		
		// 根据输入的搜索条件创建 过滤器
		String leftKeywordStr = ((String) leftKeyword.getValue()).trim();
		
		Or compareAll = new Or(
				 new Like("empNo", "%" + leftKeywordStr + "%", false), 
	             new Like("username", "%" + leftKeywordStr + "%", false), 
	             new Like("department.name", "%" + leftKeywordStr + "%", false));
		leftTableContainer.addContainerFilter(compareAll);
		// 按工号的升序排列
		leftTableContainer.sort(new Object[] {"empNo"}, new boolean[] {true});
		
		// 收索完成后初始化表格的标题
		String leftCaption = "可选成员 ( " + leftTableContainer.size() + " )";
		leftTable.setCaption(leftCaption);
	}
	
	/**
	 * 由buttonClick调用,右侧组件搜索
	 */
	private void executeRightSearch() {
		if(rightTableContainer==null) return;
		
		// 删除之前的所有过滤器
		rightTableContainer.removeAllContainerFilters();
		
		// 根据输入的搜索条件创建 过滤器
		String rightKeywordStr = ((String) rightKeyword.getValue()).trim();

		Or compareAll = new Or(
				 new Like("empNo", "%" + rightKeywordStr + "%", false), 
	             new Like("username", "%" + rightKeywordStr + "%", false), 
	             new Like("department.name", "%" + rightKeywordStr + "%", false));
		rightTableContainer.addContainerFilter(compareAll);
		// 按工号的升序排列
		rightTableContainer.sort(new Object[] {"empNo"}, new boolean[] {true});
	}

	/**
	 * 由buttonClick调用,将选中表格tableFrom的值添加到tableTo
	 * @param tableFrom 从哪个表取数据
	 * @param tableTo	添加到哪个表
	 * @param isAll 是否添加全部
	 */
	@SuppressWarnings("unchecked")
	private void addToOpposite(Table tableFrom, Table tableTo, Boolean isAll) {
		if(tableFrom == null||tableTo == null) return;
		
		//如果添加全部，不对tableFrom选择的值进行验证,否则看选中的值是否为Null
		if(!isAll && ((Collection<User>) tableFrom.getValue()).size() == 0) {
			this.getApplication().getMainWindow().showNotification("请选择要添加或移除的CSR!",
					Window.Notification.TYPE_HUMANIZED_MESSAGE);
			return;
		}
		
		//从tableFrom中取出所有选中的Csr
		Collection<User> csrs = null;
		if(isAll){
			//出现 java.util.ConcurrentModificationException异常，所以包装
			csrs = new ArrayList<User>((Collection<User>)tableFrom.getItemIds());
		} else {
			csrs = (Collection<User>) tableFrom.getValue();
		}
		
		//通过循环来改变TableFrom和TableTo的Item	
		for (User user : csrs) {
			tableFrom.getContainerDataSource().removeItem(user);
			tableTo.getContainerDataSource().addItem(user);
		}

		// 初始化表格标题
		initializeTablesCaption();
	}

	/**
	 * 初始化左右两个表格的标题
	 */
	public void initializeTablesCaption() {
		// 如果右侧搜索区域不为空，则需要先将右侧区域置空,再初始化标题
		String rightKeywordStr = ((String) rightKeyword.getValue()).trim();
		if(!"".equals(rightKeywordStr)) {
			rightKeyword.setValue("");
		}
		
		String leftCaption = "可选人员 ( " + leftTable.getContainerDataSource().size() + " )";
		String rightCaption = "已选人员 ( " + rightTable.getContainerDataSource().size() + " )";
		
		leftTable.setCaption(leftCaption);
		rightTable.setCaption(rightCaption);
	}

	/**
	 * 验证并获取可用的电话号
	 * @param usefullPhones	存放可用的电话号码
	 */
	private Set<String> excuteConfirmPhoneNum() {
		Set<String> usefullPhones = new HashSet<String>();
		Boolean specified = (Boolean) specifiedOption.getValue();
		if(specified) {
			// 解析用户输入的指定的电话号码，带 0 的电话号，跟不带 0 的本质上是属于一个电话，所以需要处理
			String phoneStr = (String) phoneArea.getValue();
			if(phoneStr != null && !"".equals(phoneStr.trim())) {
				String[] phoneNums = phoneStr.split(",");
				for(String phoneNum : phoneNums) {
					if(!phoneNum.trim().matches("\\d+")) {
						continue;
					}
					String remoteNum = "";	// 带 0 的号码
					String localNum = "";	// 不带 0 的号码
					if(phoneNum.startsWith("0")) {
						remoteNum = phoneNum;
						localNum = phoneNum.substring(1);
					} else {
						remoteNum = "0" + phoneNum;
						localNum = phoneNum;
					}
					if(!usefullPhones.contains(localNum) && !usefullPhones.contains(remoteNum)) {
						usefullPhones.add(phoneNum.trim());
					}
				}
			}
		}
		return usefullPhones;
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
	 * 执行保存操作
	 * @param runTimeScope	执行转呼手机的时间段
	 * @param usefullPhones	指定的手机号
	 * @return
	 */
	@SuppressWarnings({ "unchecked"})
	private void executeSave(String[] runTimeScope, Set<String> usefullPhones) {
		// 保存原有值
		Phone2PhoneSetting oldP2PSetting = cloneP2PSetting();
//		TODO 删除输出语句
		logger.info("oldP2PSetting.getIsStartedRedirect()--------> " +oldP2PSetting.getIsStartedRedirect());
		Boolean oldLicensed2Csr = oldP2PSetting.getIsLicensed2Csr();
		Boolean oldSpecified = oldP2PSetting.getIsSpecifiedPhones();
		Set<User> oldCsrs = oldP2PSetting.getSpecifiedCsrs();
		Set<String> oldPhones = oldP2PSetting.getSpecifiedPhones();
		logger.info(oldPhones.size() + "");
		
		// 获取当前新的值
		Boolean currentStartSetting = (Boolean) startSettingOption.getValue();
		Boolean currentLicensed2Csr = (Boolean)licensed2CsrOption.getValue();
		String currentDayOfWeekType = (String) dayOfWeekTypeSelector.getValue();
		Set<DayOfWeek> currentDaysOfWeek = (Set<DayOfWeek>) daysOfWeekOption.getValue();
		Set<String> redirectTypes = (Set<String>) redirectTypeOption.getValue();
		Integer noanswerTimeout = (Integer) noanswerTimeoutSelector.getValue();
		Boolean currentSpecified = (Boolean) specifiedOption.getValue();
		Set<User> currentCsrs = new HashSet<User>(rightTableContainer.getItemIds());
		
		// 更新对象的各字段的值
		globalP2PSetting.setIsStartedRedirect(currentStartSetting);
		globalP2PSetting.setIsLicensed2Csr(currentLicensed2Csr);
		globalP2PSetting.setDayOfWeekType(currentDayOfWeekType);
		if("custom".equals(currentDayOfWeekType)) {
			globalP2PSetting.setDaysOfWeek(currentDaysOfWeek);
		}
		globalP2PSetting.setStartTime(runTimeScope[0]);
		globalP2PSetting.setStopTime(runTimeScope[1]);
		globalP2PSetting.setRedirectTypes(redirectTypes);
		globalP2PSetting.setNoanswerTimeout(noanswerTimeout);
		globalP2PSetting.setIsSpecifiedPhones(currentSpecified);
		if(currentSpecified) {
			globalP2PSetting.setSpecifiedPhones(usefullPhones);
		}
		globalP2PSetting.setSpecifiedCsrs(currentCsrs);
		
		// 保存信息
		globalP2PSetting = phone2PhoneSettingService.update(globalP2PSetting);

		//----------------------  更新队列成员：手机成员、分机成员   ------------------// 
		String defaultOutline = ShareData.domainToDefaultOutline.get(domain.getId());
		if(defaultOutline == null) {
			return ;
		}

		// 获取所有非自动使用的队列
		List<Queue> allCommonQueues = queueService.getAllByDomain(domain, true);
		List<String> allCommonQueueNames = new ArrayList<String>();
		for(Queue autoQueue : allCommonQueues) {
			allCommonQueueNames.add(autoQueue.getName());
		}
		List<Phone2PhoneSetting> customSettings = phone2PhoneSettingService.getAllCsrCustomSettings(domain.getId());
		
		// 为了使代码逻辑简单化，不管当前全局启动项是开启还是关闭，如果原来是开启全局启动项的，并且满足启动条件，则先清空所有队列中的手机号成员对象
		boolean isOldGlobalRunning = phone2PhoneSettingService.confirmSettingIsRunning(oldP2PSetting);
		logger.info("isOldGlobalRunning------->" + isOldGlobalRunning);
		if(isOldGlobalRunning) {
			// 如果原来是强制指定转呼某些手机号，则将这些手机号从队列中移除
			logger.info("oldSpecified---------->"+ oldSpecified);
			if(oldSpecified) {
				for(String oldPhone : oldPhones) {
					logger.info(oldPhone);
					for(String queueName : allCommonQueueNames) {
						queueMemberRelationService.removeQueueMemberRelation(queueName, oldPhone+"@"+defaultOutline);
					}
				}
			} else {	// 如果原来是智能的转呼到话务员拥有的手机，则从话务员所拥有的队列中移除其对应手机号成员
				for(User oldCsr : oldCsrs) {
					logger.info("oldCsr.getid() ----> "+ oldCsr.getId());
					removePhone2QueueMember(oldCsr, defaultOutline, allCommonQueueNames);
				}
			}
		}
		
		// 为了使代码逻辑简单化，不管当前是否授权给话务员进行自定义配置，只要原来是授权了的，则获取所有话务员自定义的配置项，并从队列中移除
		if(oldLicensed2Csr) {
			for(Phone2PhoneSetting customSetting : customSettings) {
				boolean isOldCustomRunning = phone2PhoneSettingService.confirmSettingIsRunning(customSetting);
				if(isOldCustomRunning) {
					User csr = customSetting.getCreator();
					removePhone2QueueMember(csr, defaultOutline, allCommonQueueNames);
				}
			}
		}

		// 如果当前全局外转外已经正式开始转呼,则更加情况将相应的手机号加入队列
		// 满足开启的条件：1、当前配置是开启的，2、配置日期包含今天，并且启动时刻是否 <= 当前时刻 则返回 ， 并且终止时刻 >= 当前时刻
		boolean isGlobalRunning = phone2PhoneSettingService.confirmSettingIsRunning(globalP2PSetting);
		if(isGlobalRunning) {
			// 如果当前是“便捷呼叫”（转呼到指定的手机号），则将指定的所有手机号加入所有队列
			if(currentSpecified) {
				// 第一步： 将所有的手机加入各非自动外呼生成的队列
				for(String phoneNumber : usefullPhones) {
					for(String queueName : allCommonQueueNames) {
						queueMemberRelationService.addQueueMemberRelation(queueName, phoneNumber+"@"+defaultOutline, 5);
					}
				}
				// 第二步：将所有的动态队列成员对应的分机，及静态队列中的分机，从非自动外呼生成的队列中移除（实现方法是将所有在线的分机从队列中移除，这样可以不必查数据库，减少数据库的压力）
				for(String exten : ShareData.userToExten.values()) { 
					for(String queueName : allCommonQueueNames) {	// 无论普通动态队列还是静态队列都得移除分机成员
						queueMemberRelationService.removeQueueMemberRelation(queueName, exten);
					}
				}
			} else { // 如果当前是指定了话务员才有转呼手机，则将指定话务员的手机号加入队列
				for(User currentCsr : currentCsrs) {
					addPhone2QueueMember(currentCsr, defaultOutline, allCommonQueueNames);
				}
				// 如果原来是强制指定话机一些手机号“便捷呼叫”，而现在是“智能呼叫”，需要将在线话务员使用的分机加入队列
				if(oldSpecified) {
					for(Long csrId : ShareData.userToExten.keySet()) {
						addExten2QueueMember(csrId, allCommonQueueNames);
					}
				}
			}
		} else if(isOldGlobalRunning) {	// 如果当前是停止的，并且原来外转外是正在运行，则需要将所有在线话务员在使用的分机加入相应队列
			for(Long csrId : ShareData.userToExten.keySet()) {
				addExten2QueueMember(csrId, allCommonQueueNames);
			}
		}
		
		// 创建或停止定时任务
		if(globalP2PSetting.getIsStartedRedirect()) {
			phone2PhoneSettingService.createP2PScheduler(globalP2PSetting);
		} else {
			phone2PhoneSettingService.stopP2PScheduler(globalP2PSetting);
		}
		
		// 如果当前授权给话务员配置是开启的，并且话务员不受管理员的限制，则需要将话务员自定义的配置项根据情况往队列中添加
		if(currentLicensed2Csr) {
			// 保存当前选择的话务员的id 集合
			List<Long> currentCsrIds = new ArrayList<Long>();
			for(User currentCsr : currentCsrs) {
				currentCsrIds.add(currentCsr.getId());
			}
			
			for(Phone2PhoneSetting customSetting : customSettings) {
				User csr = customSetting.getCreator();
				// 如果当前全局配置项的外转外方式是“智能呼叫”，则要过滤掉全局配置中指定的话务员对象（智能呼叫，管理员对那些选中的话务员是完全控制外转外的）
				if(!currentSpecified && currentCsrIds.contains(csr.getId())) {
					continue;
				}
				// 判断自定义的配置是否正在运行
				boolean isCustomRunning = phone2PhoneSettingService.confirmSettingIsRunning(customSetting);
				if(isCustomRunning) {
					addPhone2QueueMember(csr, defaultOutline, allCommonQueueNames);
				}
			}
		}
	}

	/**
	 * 在保存前先克隆一个原有的对象
	 * @return
	 */
	private Phone2PhoneSetting cloneP2PSetting() {
		Phone2PhoneSetting oldP2PSetting = new Phone2PhoneSetting();
		oldP2PSetting.setCreator(globalP2PSetting.getCreator());
		oldP2PSetting.setDayOfWeekType(globalP2PSetting.getDayOfWeekType());
		oldP2PSetting.setDaysOfWeek(globalP2PSetting.getDaysOfWeek());
		oldP2PSetting.setDomain(globalP2PSetting.getDomain());
		oldP2PSetting.setId(globalP2PSetting.getId());
		oldP2PSetting.setIsGlobalSetting(globalP2PSetting.getIsGlobalSetting());
		oldP2PSetting.setIsLicensed2Csr(globalP2PSetting.getIsLicensed2Csr());
		oldP2PSetting.setIsSpecifiedPhones(globalP2PSetting.getIsSpecifiedPhones());
		oldP2PSetting.setSpecifiedPhones(globalP2PSetting.getSpecifiedPhones());
		oldP2PSetting.setIsStartedRedirect(globalP2PSetting.getIsStartedRedirect());
		oldP2PSetting.setRedirectTypes(globalP2PSetting.getRedirectTypes());
		oldP2PSetting.setSpecifiedCsrs(globalP2PSetting.getSpecifiedCsrs());
		oldP2PSetting.setStartTime(globalP2PSetting.getStartTime());
		oldP2PSetting.setStopTime(globalP2PSetting.getStopTime());
		return oldP2PSetting;
	}

	/**
	 * 根据话务员对象，如果话务员有电话，并且不在线，则移除手机成员
	 * @param csr					话务员对象
	 * @param defaultOutline		默认外线
	 * @param allCommonQueueNames	所有非自动外呼使用的队列
	 */
	private void removePhone2QueueMember(User csr, String defaultOutline, List<String> allCommonQueueNames) {
		// 如果用户已经登陆，则不做处理， 因为用户已登录的情况，呼入队列时找分机
		if(ShareData.userToExten.keySet().contains(csr.getId())) {
			return;
		}
		String phoneNum = csr.getPhoneNumber();
		if(phoneNum == null || "".equals(phoneNum)) {	// 话务员必须有电话号
			return;
		}
		// 只有话务员不在线，才需要将话务员的手机号移出队列
		List<UserQueue> userQueues = userQueueService.getAllByUsername(csr.getUsername());
		for(UserQueue userQueue : userQueues) {
			String queueName = userQueue.getQueueName();
			if(allCommonQueueNames.contains(queueName)) {		// 因为自动外呼队列中的成员不可能含有手机成员
				queueMemberRelationService.removeQueueMemberRelation(queueName, phoneNum+"@"+defaultOutline);
			}
		}
	}
	
	/**
	 * 根据话务员对象，如果话务员有电话，并且不在线，则添加手机成员
	 * @param csr					话务员对象
	 * @param defaultOutline		默认外线
	 * @param allCommonQueueNames	所有非自动外呼使用的队列
	 */
	private void addPhone2QueueMember(User csr, String defaultOutline, List<String> allCommonQueueNames) {
		// 如果用户已经登陆，则不做处理， 因为用户已登录的情况，呼入队列时找分机
		if(ShareData.userToExten.keySet().contains(csr.getId())) {
			return;
		}
		String phoneNum = csr.getPhoneNumber();
		if(phoneNum == null || "".equals(phoneNum)) {	// 话务员必须有电话号
			return;
		}
		// 只有话务员不在线，才需要将话务员的手机号加入队列
		List<UserQueue> userQueues = userQueueService.getAllByUsername(csr.getUsername());
		for(UserQueue userQueue : userQueues) {
			String queueName = userQueue.getQueueName();
			if(allCommonQueueNames.contains(queueName)) {		// 因为自动外呼队列中的成员不可能含有手机成员
				queueMemberRelationService.addQueueMemberRelation(queueName, phoneNum+"@"+defaultOutline, 5);
			}
		}
	}

	/**
	 * 将分机加入队列成员
	 * 	需要调用该方法的时机：
	 * 		全局配置开启，并且启动日期包含今天，并且启动时刻 <= 当前时刻
	 * 		1、如果原来是强制指定话机一些手机号“便捷呼叫”，而现在是“智能呼叫”，需要将在线话务员使用的分机加入队列
	 * 		2、如果原来外转外是启动的，以及当前全局是关闭的，则需要将所有在线话务员在使用的分机加入相应队列
	 * @param csrId					话务员id
	 * @param allCommonQueueNames	所有非自动外呼使用的队列
	 */
	private void addExten2QueueMember(Long csrId, List<String> allCommonQueueNames) {
		User csr = userService.get(csrId);
		String exten = ShareData.userToExten.get(csrId);
		// 取出用户对应的所有的动态队列
		List<UserQueue> userQueueList = userQueueService.getAllByUsername(csr.getUsername());
		// 取出用户对应的所有的静态队列
		List<StaticQueueMember> staticQMs = staticQueueMemberService.getAllBySipname(domain, exten);
		for (UserQueue userQueue : userQueueList) {
			String queueName = userQueue.getQueueName();
			if(allCommonQueueNames.contains(queueName)) {		// 因为自动外呼队列中的成员不可能含有手机成员
				queueMemberRelationService.addQueueMemberRelation(queueName, exten, userQueue.getPriority());
			}
		}
		
		for (StaticQueueMember sqm : staticQMs) {
			queueMemberRelationService.addQueueMemberRelation(sqm.getQueueName(), exten, sqm.getPriority());
		}
	}
	
	/**
	 *  更新组件信息
	 */
	public void update() {
		attach();
	}
	
}
