package com.jiangyifen.ec2.ui.csr.toolbar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jiangyifen.ec2.bean.RoleType;
import com.jiangyifen.ec2.entity.MarketingProject;
import com.jiangyifen.ec2.entity.Role;
import com.jiangyifen.ec2.entity.SipConfig;
import com.jiangyifen.ec2.entity.Timers;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.globaldata.GlobalData;
import com.jiangyifen.ec2.globaldata.ResourceDataCsr;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.service.csr.ami.DialService;
import com.jiangyifen.ec2.service.csr.ami.UserLoginService;
import com.jiangyifen.ec2.service.eaoservice.MarketingProjectService;
import com.jiangyifen.ec2.service.eaoservice.NoticeItemService;
import com.jiangyifen.ec2.service.eaoservice.TelephoneService;
import com.jiangyifen.ec2.service.eaoservice.TimersService;
import com.jiangyifen.ec2.ui.LoginLayout;
import com.jiangyifen.ec2.ui.csr.statusbar.CsrStatusBar;
import com.jiangyifen.ec2.ui.csr.workarea.marketingtask.MyMarketingTaskTabView;
import com.jiangyifen.ec2.ui.csr.workarea.questionnairetask.QuestionnaireTaskTabView;
import com.jiangyifen.ec2.ui.mgr.kbinfo.ShowKbInfoViewWindow;
import com.jiangyifen.ec2.ui.mgr.system.tabsheet.SystemInfo;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.PopupView;
import com.vaadin.ui.PopupView.PopupVisibilityEvent;
import com.vaadin.ui.PopupView.PopupVisibilityListener;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.Window.CloseListener;
import com.vaadin.ui.themes.BaseTheme;

/**
 * 话务员的工具条 组件
 * 
 * @author jrh
 *
 */
// TODO jrh 重构时，界面组件的生成，话务员登陆时不用把所有的子窗口以及其他组件一次性生成，等到buttonClick 时再根据情况创建
@SuppressWarnings("serial")
public class CsrToolBar extends VerticalLayout implements ClickListener, CloseListener {

	// 号码加密权限
	private static final String BASE_DESIGN_MANAGEMENT_MOBILE_NUM_SECRET = "base_design_management&mobile_num_secret";
	
	private Button quit;							// 退出当前系统
	private HorizontalLayout systemLayout;			// 系统操作控制器（退出系统）
	private HorizontalLayout toolButtonsLayout;		// 工具按钮管理器
	
	private PopupView changeProjectPop;				// 切换工作项目
	private HorizontalLayout currentProjectHLayout;	// 存放以上俩组件

	private Button myNotice;						// 我的通知
	private NoticeView noticeView;					// 通知显示界面
	private Window noticeWindow;					// 我的通知子窗口
	
	private Label infoLabel;
	private Button viewNoticeDetail;				// 查看详情按钮
	private Window notificationWindow;				// 新通知来时显示窗口

	private Button timerButton;						// 定时提醒
	private TimerView timerView;					// 定时器界面
	private Window timerWindow;						// 我的定时提醒子窗口
	
	private Button p2pSettingButton;				// 外转外配置按钮
	private CsrPhone2PhoneSettingWindow p2pSettingWindow;	// 外转外配置窗口
	
	private Button knowledgeShow_bt;						//知识库显示
	private ShowKbInfoViewWindow showKbInfoViewWindow;

	private Button csrUser_bt;						// 登陆的当前用户
	private Window loginUserWindow;					// 登陆用户信息编辑子窗口
	private LoginUserInfoView loginUserInfoView;	// 当前登录用户信息显示界面
	
	private Button swithToMgrView_bt;				// 切换至管理员界面，如果当前用户拥有管理员角色
	
	private Map<String, Integer> missCallMap;		// 漏接的电话集合
	private Window missCallWindow;					// 漏接电话显示窗口

	private Label conflictNotice_lb;				// 冲突人员信息提示
	private Window conflictNoticeWindow;			// 冲突提示窗口(当有其他人使用当前用户的账号或者分机登陆时，进行提示)
	
	private Timers timers;							// 定时器对象		
	private Label timersTitle;						// 定时器响应标题
	private Label responseTime;						// 定时器响应时间
	private Label timersContent;					// 定时器提醒内容
	private HorizontalLayout timers_dial_hlo;		// 呼叫按钮存放界面
	private Button localDial_bt;					// 本地呼叫按钮
	private Button remoteDial_bt;					// 外地呼叫按钮
	private ComboBox postponedDay;					// 推迟响应的天数
	private ComboBox postponedHour;					// 推迟响应的小时
	private ComboBox postponedMinute;				// 推迟响应的分钟
	private Button postponed;						// 推迟提醒
	private Button forbidPop_bt;					// 设置不再提醒
	private boolean isDelay = false;				// 坐席是否点击了延迟按钮
	private boolean isForbidPop = false;			// 坐席是否点选择了不再提醒
	private boolean isDialedCustomer = false;		// 描述坐席是否对客户发起了呼叫
	private Window timerResponseWindow;				// 定时响应子窗口
	
	private Calendar calendar;						// 日历工具 用于改变定时器timers 的值
	private User loginUser;							// 当前登录用户
	private String exten;							// 当前登录用户的分机号
	private UserLoginService userLoginService;		// 用户登录或退出服务类
	private NoticeItemService noticeItemService;	// 通知的服务类
	private TimersService timersService;			// 定时提醒服务类
	private SimpleDateFormat simpleDateFormat;		// 时间格式化工具 年-月-日 时：分：秒
	
	private boolean isEncryptMobile = true;			// 电话号码是否需要加密
	private ArrayList<String> ownBusinessModels;	// 当前登陆用户目前使用的角色类型下，所拥有的权限

	private MarketingProject oldProject;			// 切换项目之前的工作项目
	private MarketingProject currentWorkingProject;	// 当前工作项目
	private List<MarketingProject> projectList;		// 分配给当前用户的项目中所有正在进行的项目
	private MarketingProjectService marketingProjectService;		// 项目任务服务类
	private TelephoneService telephoneService;		// 电话服务类
	private DialService dialService;				// 拨号服务类
	
	private LoginLayout loginLayout;				// 登陆界面
	
	public CsrToolBar(LoginLayout loginLayout) {
		this.loginLayout = loginLayout;
		
		// 给Service 类等字段赋值
		initializeParameters();
		
		toolButtonsLayout = new HorizontalLayout();	
		toolButtonsLayout.setWidth("100%");
		toolButtonsLayout.setSpacing(true);
		toolButtonsLayout.setMargin(false, true, false, true);
		this.addComponent(toolButtonsLayout);
		
		HorizontalLayout logoLayout = new HorizontalLayout();
		logoLayout.setSpacing(true);
		toolButtonsLayout.addComponent(logoLayout);
		
		Embedded logoIcon = null;
		if("F0:1F:AF:D8:D8:62".equals(GlobalData.MAC_ADDRESS)) {	// 如果是中道的服务器，则使用中道自己的图标
			logoIcon = new Embedded(null, ResourceDataCsr.logo_32_zhongdao);
		} else {	// 否则使用共同的图标
			logoIcon = new Embedded(null, ResourceDataCsr.logo_ico);
		}
		logoLayout.addComponent(logoIcon);
		logoLayout.setComponentAlignment(logoIcon, Alignment.MIDDLE_LEFT);

		if(!"F0:1F:AF:D8:D8:62".equals(GlobalData.MAC_ADDRESS)) {	// 如果不是中道的服务器，则加上附加信息
			Label companyLabel = new Label("<font size = 4 color='blue'><B>&nbsp;"+SystemInfo.getSystemTitle()+"</B></font><br/>", Label.CONTENT_XHTML);
			logoLayout.addComponent(companyLabel);
			logoLayout.setComponentAlignment(companyLabel, Alignment.MIDDLE_LEFT);
		}
		
		systemLayout = new HorizontalLayout();
		systemLayout.setSpacing(true);
		toolButtonsLayout.addComponent(systemLayout);
		toolButtonsLayout.setComponentAlignment(systemLayout, Alignment.MIDDLE_RIGHT);
		
		// 创建当前工作项目切换组件
		this.createCurrentProjectHLayout();
		
		// 创建用户的通知按钮、及通知详细内容显示子窗口
		this.createNoticeComponent();
		
		// 创建新通知来时显示窗口
		this.createNotificationComponent();
		
		// 创建漏接电话提示窗口
		this.createMissCallWindow();
		
		// 创建冲突提示窗口
		this.createConflictNoticeWindow();
		
		// 创建用户的闹钟按钮、及闹钟详细内容显示子窗口
		this.createTimerComponent();

		// 创建定时提醒响应界面子窗口
		this.createTimerResponseComponent();
		
		// 创建外转外配置组件
		this.createPhone2PhoneSettingComponent();
		
		// 创建知识库组件
		this.createKnowledgeShowComponets();

		// 创建登陆用户按钮、及其信息显示子窗口
		this.createUserInfoComponent();
		
		quit = new Button("注 销", this);
		quit.setIcon(ResourceDataCsr.quit_ico);
		quit.setStyleName(BaseTheme.BUTTON_LINK);
		systemLayout.addComponent(quit);
	}

	/**
	 * 初始化该类需要的一些字段
	 */
	private void initializeParameters() {
		this.setWidth("100%");
		this.setSpacing(true);
		this.setMargin(true, true, false, false);
		calendar = Calendar.getInstance();
		loginUser = SpringContextHolder.getLoginUser();		
		exten = SpringContextHolder.getExten();
		ownBusinessModels = SpringContextHolder.getBusinessModel();

		// 判断是否需要加密
		isEncryptMobile = ownBusinessModels.contains(BASE_DESIGN_MANAGEMENT_MOBILE_NUM_SECRET);
		
		noticeItemService = SpringContextHolder.getBean("noticeItemService");
		userLoginService = SpringContextHolder.getBean("userLoginService");
		timersService = SpringContextHolder.getBean("timersService");
		marketingProjectService = SpringContextHolder.getBean("marketingProjectService");
		telephoneService = SpringContextHolder.getBean("telephoneService");
		dialService = SpringContextHolder.getBean("dialService");
		
		projectList = marketingProjectService.getProjectsByUserAndStatus(loginUser, 
					com.jiangyifen.ec2.bean.MarketingProjectStatus.RUNNING);
		if(projectList.size() > 0) {
			currentWorkingProject = projectList.get(0);
			oldProject = projectList.get(0);
		}
		
		simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		// 将组件加入需要更新组件的Map 集合中
		ShareData.csrToToolBar.put(loginUser.getId(), this);
	}
	
	/**
	 * 创建当前工作项目布局管理器，及相应的组件, 并加入 任务垂直布局管理器中
	 * @param taskVLayout 垂直布局管理器
	 */
	private void createCurrentProjectHLayout() {
		currentProjectHLayout = new HorizontalLayout();
		systemLayout.addComponent(currentProjectHLayout);
		
		Embedded projectIcon = new Embedded(null, ResourceDataCsr.project_24_ico);
		currentProjectHLayout.addComponent(projectIcon);
		
		Label currentProjectLabel = new Label("当前工作项目：");
		currentProjectLabel.setWidth("-1px");
		currentProjectHLayout.addComponent(currentProjectLabel);
		currentProjectHLayout.setComponentAlignment(currentProjectLabel, Alignment.MIDDLE_LEFT);
		
		final ChangeCurrentProjectView changeProjectView = new ChangeCurrentProjectView(projectList);
		changeProjectPop = new PopupView(changeProjectView);
		currentProjectHLayout.addComponent(changeProjectPop);
		currentProjectHLayout.setComponentAlignment(changeProjectPop, Alignment.MIDDLE_RIGHT);
		changeProjectPop.addListener(new PopupVisibilityListener() {
			@Override
			public void popupVisibilityChange(PopupVisibilityEvent event) {
				if(changeProjectPop.isPopupVisible()) {	// 在PopupView 可见的时候，为其内部OptionGroup 更改数据源
					projectList = marketingProjectService.getProjectsByUserAndStatus(loginUser, 
							com.jiangyifen.ec2.bean.MarketingProjectStatus.RUNNING);
					changeProjectView.refershOptionSource(projectList);
				} else {	// 当关闭PopupView 后，更加需求刷新任务组件
					currentWorkingProject = (MarketingProject) changeProjectView.getProjectOption().getValue();
					// 只有当前工作项目发生变化时才更新界面信息，不然不做任何更改
					if(currentWorkingProject == null && oldProject == null) {
						return;
					} else if(currentWorkingProject != null && oldProject != null && currentWorkingProject.getId().equals(oldProject.getId())) {
						return;
					} else {
						changeProjectView.refershOptionSource(projectList);
						
						//取得分机
						String exten=ShareData.userToExten.get(loginUser.getId());
						if(currentWorkingProject == null) {
							ShareData.extenToDynamicOutline.remove(exten);
							ShareData.extenToProject.remove(exten);
						} else {
							//取得外线
							SipConfig outLineSip=currentWorkingProject.getSip();
							//存储分机和外线的对应关系
							if(outLineSip!=null){
								ShareData.extenToDynamicOutline.put(exten, outLineSip.getName());
							}else{
								ShareData.extenToDynamicOutline.remove(exten);
							}
							//存储分机和项目的对应关系
							ShareData.extenToProject.put(exten, currentWorkingProject.getId());
						}
						
						// 刷新我的营销表格的翻页组件的搜索条件
						MyMarketingTaskTabView taskTabView = SpringContextHolder.getMyTaskTabView();
						if(taskTabView != null) {
							taskTabView.refreshMarketingTaskModel();
						}
						// 刷新我的问卷任务表格的翻页组件的搜索条件
						QuestionnaireTaskTabView questionnaireTaskTabView = SpringContextHolder.getQuestionnaireTaskTabView();
						if(questionnaireTaskTabView != null) {
							SpringContextHolder.getQuestionnaireTaskTabView().refreshQuestionnaireTaskModel();
						}
						oldProject = currentWorkingProject;							// 修改oldProject 的值
					}
				}
			}
		});
	}
	
	/**
	 *  创建用户的通知按钮、及通知详细内容显示子窗口
	 */
	private void createNoticeComponent() {
		myNotice = new Button();
		myNotice.addListener(this);
		myNotice.setImmediate(true);
		myNotice.setHtmlContentAllowed(true);
		myNotice.setIcon(ResourceDataCsr.message_24_ico);
		myNotice.setStyleName(BaseTheme.BUTTON_LINK);
		systemLayout.addComponent(myNotice);
		refreshNoticeButtonCaption();
		
		noticeWindow = new Window("我的通知");
		noticeWindow.center();
		noticeWindow.setWidth("800px");
		noticeWindow.setHeight("600px");
		noticeWindow.addListener(this);
		
		noticeView = new NoticeView();
		noticeView.setMyNotice(myNotice);
		noticeWindow.setContent(noticeView);
	}

	/**
	 * 创建新通知来时显示窗口
	 */
	private void createNotificationComponent() {
		notificationWindow = new Window();
		notificationWindow.setStyleName("opaque");
		notificationWindow.setResizable(false);
		
		HorizontalLayout notificationHLayout = new HorizontalLayout();
		notificationHLayout.setSpacing(true);
		notificationHLayout.setMargin(true);
		notificationWindow.setContent(notificationHLayout);
		
		infoLabel = new Label("<font size='5' color='red'><B>您有一条新信息!</B></font>", Label.CONTENT_XHTML);
		notificationHLayout.addComponent(infoLabel);
		
		viewNoticeDetail = new Button("查看详情", (ClickListener)this);
		viewNoticeDetail.setStyleName(BaseTheme.BUTTON_LINK);
		notificationHLayout.addComponent(viewNoticeDetail);
		notificationHLayout.setComponentAlignment(viewNoticeDetail, Alignment.MIDDLE_CENTER);
	}
	
	/**
	 * 创建漏接电话提示窗口
	 */
	private void createMissCallWindow() {
		missCallMap = new HashMap<String, Integer>();
		
		missCallWindow = new Window();
		missCallWindow.setStyleName("opaque");
		missCallWindow.addListener(this);
		
		VerticalLayout missCallContent = new VerticalLayout();
		missCallContent.setSpacing(true);
		missCallContent.setMargin(true);
		missCallContent.setHeight("155px");
		missCallContent.setWidth("450px");
		missCallWindow.setContent(missCallContent);
	}
	
	/**
	 * 创建冲突提示窗口
	 */
	private void createConflictNoticeWindow() {
		conflictNoticeWindow = new Window("登陆冲突提示");
		conflictNoticeWindow.setResizable(false);
		
		VerticalLayout conflictLayout = new VerticalLayout();
		conflictLayout.setSpacing(true);
		conflictLayout.setHeight("100px");
		conflictLayout.setWidth("300px");
		conflictLayout.setMargin(false, true, true, true);
		conflictNoticeWindow.setContent(conflictLayout);

		Label captionLabel = new Label("<font color='blue'><B>当前冲突人员：</B></font>", Label.CONTENT_XHTML);
		captionLabel.setWidth("-1px");
		conflictLayout.addComponent(captionLabel);
		
		conflictNotice_lb = new Label("", Label.CONTENT_XHTML);
		conflictNotice_lb.setWidth("-1px");
		conflictLayout.addComponent(conflictNotice_lb);
	}

	/**
	 * 创建用户的闹钟按钮、即闹钟详细内容显示子窗口
	 */
	private void createTimerComponent() {
		timerButton = new Button("我的定时提醒", this);
		timerButton.setImmediate(true);
		timerButton.setIcon(ResourceDataCsr.alarm_24_ico);
		timerButton.setStyleName(BaseTheme.BUTTON_LINK);
		systemLayout.addComponent(timerButton);
		
		timerWindow = new Window("我的定时提醒");
		timerWindow.center();
		timerWindow.setWidth("46%");
		timerWindow.setHeight("67%");
		timerWindow.addListener(this);
		
		timerView = new TimerView();
		timerWindow.setContent(timerView);
	}

	/**
	 * 创建闹铃（定时提醒相应组件）
	 */
	private void createTimerResponseComponent() {
		timerResponseWindow = new Window("定时提醒");
		timerResponseWindow.setWidth("450px");
		timerResponseWindow.setHeight("240px");
		timerResponseWindow.setStyleName("opaque");
		timerResponseWindow.setResizable(false);
		timerResponseWindow.addListener(this);
	
		VerticalLayout mainLayout = (VerticalLayout) timerResponseWindow.getContent();
		mainLayout.setMargin(false,false,true,false);
		mainLayout.setSpacing(true);
		mainLayout.setSizeFull();
		mainLayout.setStyleName("doublescroll");	// 双滚动条
		
		Panel panel = new Panel();
		panel.setWidth("450px");
		panel.setHeight("170px");
		panel.addStyleName("nocontentborder");
		mainLayout.addComponent(panel);
		
		VerticalLayout panelContent = new VerticalLayout();
		panelContent.setSpacing(true);
		panelContent.setSizeUndefined();
		panelContent.setMargin(false,true,false,false);
		panel.setContent(panelContent);
		
		timersTitle = new Label("<B>响应标题：</B>", Label.CONTENT_XHTML);
		timersTitle.setWidth("-1px");
		panelContent.addComponent(timersTitle);
		responseTime = new Label("<B>响应时间：</B>", Label.CONTENT_XHTML);
		responseTime.setWidth("-1px");
		panelContent.addComponent(responseTime);
		panelContent.addComponent(new Label("<B>提醒内容：</B>", Label.CONTENT_XHTML));
		
		timersContent = new Label("", Label.CONTENT_XHTML);
		timersContent.setWidth("-1px");
		panelContent.addComponent(timersContent);
		
		timers_dial_hlo = new HorizontalLayout();
		timers_dial_hlo.setSpacing(true);
		timers_dial_hlo.setVisible(false); 
		panelContent.addComponent(timers_dial_hlo);

		Label reDial_lb = new Label("<B>回访：</B>", Label.CONTENT_XHTML);
		reDial_lb.setWidth("-1px");
		timers_dial_hlo.addComponent(reDial_lb);
		
		localDial_bt = new Button(null, this);
		localDial_bt.setImmediate(true);
		localDial_bt.setStyleName("borderless");
		localDial_bt.setStyleName(BaseTheme.BUTTON_LINK);
		localDial_bt.setIcon(ResourceDataCsr.dial_12_ico);
		timers_dial_hlo.addComponent(localDial_bt);
		
		remoteDial_bt = new Button(null, this);
		remoteDial_bt.setImmediate(true);
		remoteDial_bt.setStyleName("borderless");
		remoteDial_bt.setStyleName(BaseTheme.BUTTON_LINK);
		remoteDial_bt.setIcon(ResourceDataCsr.dial_12_ico);
		timers_dial_hlo.addComponent(remoteDial_bt);
	
		HorizontalLayout postponedHLayout = new HorizontalLayout();
		postponedHLayout.setSpacing(true);
		mainLayout.addComponent(postponedHLayout);
		mainLayout.setComponentAlignment(postponedHLayout, Alignment.BOTTOM_LEFT);
		mainLayout.setExpandRatio(postponedHLayout, 1.0f);
	
		Label postponed_lb = new Label("<B>推迟响应：</B>", Label.CONTENT_XHTML);
		postponed_lb.setWidth("-1px");
		postponedHLayout.addComponent(postponed_lb);
		
		postponedDay = new ComboBox();
		postponedDay.setWidth("40px");
		postponedDay.setNullSelectionAllowed(false);
		postponedHLayout.addComponent(postponedDay);
		postponedHLayout.addComponent(new Label("天"));
		for(int i = 0; i < 31; i++) {
			postponedDay.addItem(i);
		}
		postponedDay.setValue(0);
		
		postponedHour = new ComboBox();
		postponedHour.setWidth("40px");
		postponedHour.setNullSelectionAllowed(false);
		postponedHLayout.addComponent(postponedHour);
		postponedHLayout.addComponent(new Label("时"));
		for(int i = 0; i < 24; i++) {
			postponedHour.addItem(i);
		}
		postponedHour.setValue(0);
		
		postponedMinute = new ComboBox();
		postponedMinute.setWidth("40px");
		postponedMinute.setNullSelectionAllowed(false);
		postponedHLayout.addComponent(postponedMinute);
		postponedHLayout.addComponent(new Label("分"));
		for(int i = 0; i < 60; i++) {
			postponedMinute.addItem(i);
		}
		postponedMinute.setValue(30);
		
		postponed = new Button("推迟", this);
		postponed.setImmediate(true);
		postponed.setStyleName("default");
		postponedHLayout.addComponent(postponed);
		
		forbidPop_bt = new Button("不再提醒", this);
		forbidPop_bt.setImmediate(true);
		postponedHLayout.addComponent(forbidPop_bt);
	}

	/**
	 * 创建外转外配置按钮
	 */
	private void createPhone2PhoneSettingComponent() {
		p2pSettingButton = new Button("外转外配置", this);
		p2pSettingButton.setImmediate(true);
		p2pSettingButton.setIcon(ResourceDataCsr.phone2phone_24_ico);
		p2pSettingButton.setStyleName(BaseTheme.BUTTON_LINK);
		systemLayout.addComponent(p2pSettingButton);
	}

	/**
	 *  创建知识库组件
	 */
	private void createKnowledgeShowComponets() {
		knowledgeShow_bt = new Button("知识库", this);
		knowledgeShow_bt.setIcon(ResourceDataCsr.knowledge_24_ico);
		knowledgeShow_bt.setImmediate(true);
		knowledgeShow_bt.setStyleName(BaseTheme.BUTTON_LINK);
		systemLayout.addComponent(knowledgeShow_bt);
	}
	
	/**
	 * 创建登陆用户按钮、即其信息显示子窗口
	 */
	private void createUserInfoComponent() {
		csrUser_bt = new Button("我的账户", this);
		csrUser_bt.setHtmlContentAllowed(true);
		csrUser_bt.setIcon(ResourceDataCsr.user_24_ico);
		csrUser_bt.setStyleName(BaseTheme.BUTTON_LINK);
		systemLayout.addComponent(csrUser_bt);
		
		loginUserWindow = new Window("我的个人信息");
		loginUserWindow.setWidth("280px");
		loginUserWindow.setHeight("440px");
		loginUserWindow.setResizable(false);
		loginUserWindow.addListener(this);
		loginUserWindow.center();
		
		loginUserInfoView = new LoginUserInfoView();
		loginUserInfoView.setLoginUserWindow(loginUserWindow);
		loginUserWindow.setContent(loginUserInfoView);
		
		// 检验当前用户是否持有管理员角色
		boolean isMgr = false;
		for(Role role : loginUser.getRoles()) {
			if(role.getType().getIndex() == RoleType.manager.getIndex()) {
				isMgr = true;
				break;
			}
		}

		if(isMgr) {	// 如果持有管理员角色，就添加切换按钮
			swithToMgrView_bt = new Button("转至管理员界面", this);
			swithToMgrView_bt.setIcon(ResourceDataCsr.goto_24_ico);
			swithToMgrView_bt.setStyleName(BaseTheme.BUTTON_LINK);
			systemLayout.addComponent(swithToMgrView_bt);
		}
	}

	/**
	 * 刷新我的通知按钮的显示内容
	 */
	public void refreshNoticeButtonCaption() {
		int unreadNoticesCount = noticeItemService.getEntityCount("select count(ni) from NoticeItem as ni " +
				"where ni.user.id = " + loginUser.getId() + " and ni.hasReaded = false");
		if(unreadNoticesCount > 0) {
			myNotice.setCaption("我的通知 "+"<font color='black' size='4'><B>( "+unreadNoticesCount+" )</B></font>");
		} else {
			myNotice.setCaption("我的通知 ");
		}
	}

	/**
	 * 当有新通知时刷新 "我的通知" 按钮的标题，以及弹出 Notification子窗口，并且刷新noticeView 的界面
	 */
	public void updateNewNotice() {
		infoLabel.setValue("<font size='5' color='red'><B>您有一条新信息!</B></font>");
		refreshNoticeButtonCaption();
		int xposition = this.getWindow().getBrowserWindowWidth() - 280;
		int yposition = this.getWindow().getBrowserWindowHeight() - 80;
		notificationWindow.setPositionX(xposition);
		notificationWindow.setPositionY(yposition);
		viewNoticeDetail.setVisible(true);
		noticeView.refreshNoticeTable();
		handleSubWindow(notificationWindow);
	}

	/**
	 * 当有未接电话是显示一个Notice
	 */
	public void updateMissCallNotice(String callerIdNumber) {
		VerticalLayout content = (VerticalLayout) missCallWindow.getContent();
		
		if(missCallMap.get(callerIdNumber) != null) {
			Integer count = missCallMap.get(callerIdNumber);
			missCallMap.put(callerIdNumber, count + 1);
		} else {
			missCallMap.put(callerIdNumber, 1);
		}
		
		// 删除所有组件
		content.removeAllComponents();
		
		for(String missCall : missCallMap.keySet()) {
			String notice = missCall+ " 呼叫过你" +missCallMap.get(missCall) +"次";
			Label label = new Label("<font size='5' color='red'><B>" +notice+ "</B></font>", Label.CONTENT_XHTML);
			label.setWidth("-1px");
			content.addComponent(label);
		}
		int xposition = this.getWindow().getBrowserWindowWidth() - 480;
		int yposition = this.getWindow().getBrowserWindowHeight() - 180;
		missCallWindow.setPositionX(xposition);
		missCallWindow.setPositionY(yposition);
		handleSubWindow(missCallWindow);
	}
	
	/**
	 * 更新冲突提示窗口
	 * 
	 * @param conflictUser
	 */
	public void updateConflictWindow(User conflictUser, String conflictExten) {
		String placeholder = "&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp";
		String notice1 = "<B>"+placeholder+"用户名：</B>" +conflictUser.getUsername()+ ",<B> 工号：</B>" +conflictUser.getEmpNo()+ "<br/>";
		String notice2 = "<font color='red'><B>"+placeholder+"正尝试使用分机：" +conflictExten+ " 进行登陆</B></font></B><br/>";
		String notice3 = "<B>"+placeholder+"您将可能被强制退出！</B>";
		StringBuffer content = new StringBuffer();
		content.append(notice1);
		content.append(notice2);
		content.append(notice3);
		conflictNotice_lb.setValue(content.toString());
		
		int xposition = this.getWindow().getBrowserWindowWidth() - 330;
		int yposition = this.getWindow().getBrowserWindowHeight() - 170;
		conflictNoticeWindow.setPositionX(xposition);
		conflictNoticeWindow.setPositionY(yposition);
		handleSubWindow(conflictNoticeWindow);
	}
	
	/**
	 * 根据定时提醒对象的ID 号 来更改响应界面的内容
	 * 
	 * @param timersId	id号
	 */
	public void updateTimersResponse(Long timersId) {
		timers = timersService.get(timersId);
		if(timers == null) {
			return;
		}
		timersTitle.setValue("<B>响应标题：</B>" + timers.getTitle());
		responseTime.setValue("<B>响应时间：</B>" + simpleDateFormat.format(timers.getResponseTime()));
		timersContent.setValue("&nbsp&nbsp&nbsp&nbsp"+timers.getContent());
		
		String customerPhoneNum = timers.getCustomerPhoneNum();
		if(customerPhoneNum != null && !"".equals(customerPhoneNum)) {
			timers_dial_hlo.setVisible(true);
			String localNum = customerPhoneNum;
			while(localNum.startsWith("0")) {
				localNum = localNum.substring(1);
			}
			String remoteNum = customerPhoneNum;
			if(!remoteNum.startsWith("0")) {
				remoteNum = "0" + remoteNum;
			}
			if(isEncryptMobile) {
				localDial_bt.setCaption(telephoneService.encryptMobileNo(localNum));
				remoteDial_bt.setCaption(telephoneService.encryptMobileNo(remoteNum));
			} else {
				localDial_bt.setCaption(localNum);
				remoteDial_bt.setCaption(remoteNum);
			}
			localDial_bt.setData(localNum);
			remoteDial_bt.setData(remoteNum);
		}

		isDelay = false;	// 每次弹屏都重置
		isForbidPop = false;
		isDialedCustomer = false;
		int xposition = this.getWindow().getBrowserWindowWidth() - 465;
		int yposition = this.getWindow().getBrowserWindowHeight() - 245;
		timerResponseWindow.setPositionX(xposition);
		timerResponseWindow.setPositionY(yposition);
		handleSubWindow(timerResponseWindow);
		
		// 更新弹屏次数
		Integer popCount = timers.getPopCount();
		if(popCount == null) {
			popCount = 0;
		}
		timers.setPopCount(popCount+1);
		timers = timersService.update(timers);
	}
	
	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if(source == quit) {
			// 退出系统，解除与IP 电话的绑定以及将需要更新组件的从 Map 集合中删除
			userLoginService.logout(loginUser.getId(), loginUser.getDomain().getId(),
					loginUser.getUsername(), loginUser.getEmpNo(), exten, 0L, true);
			// 清理页面存储在内存中的数据
			source.getApplication().close();
		} else if(source == myNotice) {
			handleSubWindow(noticeWindow);
			noticeView.refreshNoticeTable();
		} else if(source == viewNoticeDetail) {
			myNotice.click();
			noticeView.selectItem();
			this.getWindow().removeWindow(notificationWindow);
		} else if(source == timerButton) {
			handleSubWindow(timerWindow);
			timerView.refreshTimerTable();
		} else if(source == p2pSettingButton) {
			showP2PSettingWindow();
		} else if(source == postponed) {
			calendar.setTime(timers.getResponseTime());
			calendar.add(Calendar.DAY_OF_YEAR, +(Integer)postponedDay.getValue());
			calendar.add(Calendar.HOUR_OF_DAY, +(Integer)postponedHour.getValue());
			calendar.add(Calendar.MINUTE, +(Integer)postponedMinute.getValue());
			timers.setResponseTime(calendar.getTime());
			timersService.update(timers);
			timersService.refreshSchedule(false, timers);	// 重建 TimerTask 
			this.getWindow().removeWindow(timerResponseWindow);
		} else if(source == forbidPop_bt) {
			isForbidPop = true;
			timers.setIsCsrForbidPop(true);
			timers.setForbidRespTime(new Date());
			timersService.update(timers);
			this.getWindow().removeWindow(timerResponseWindow);
		} else if(source == csrUser_bt) {
			handleSubWindow(loginUserWindow);
		} else if(source == knowledgeShow_bt){
			this.getApplication().getMainWindow().removeWindow(showKbInfoViewWindow);
			if(showKbInfoViewWindow == null) {
				showKbInfoViewWindow = new ShowKbInfoViewWindow(this.getApplication().getMainWindow().getBrowserWindowWidth());
			}
			this.getApplication().getMainWindow().addWindow(showKbInfoViewWindow);
		} else if(source == swithToMgrView_bt) {
			userLoginService.logout(loginUser.getId(), loginUser.getDomain().getId(),
					loginUser.getUsername(), loginUser.getEmpNo(), exten, 0L, false);
			loginLayout.switchAccountView(RoleType.manager);
		} else if(source == localDial_bt || source == remoteDial_bt) {
			String calledNum = (String) source.getData();
			
			// 如果当前Tab 页不是 CsrStatusBar ，则将其置为CsrStatusBar
			VerticalLayout currentTab = ShareData.csrToCurrentTab.get(loginUser.getId());
			if(currentTab == null || currentTab.getClass() != CsrStatusBar.class) {
				ShareData.csrToCurrentTab.put(loginUser.getId(), ShareData.csrToStatusBar.get(loginUser.getId()));
			} 
			dialService.dial(exten, calledNum);

			isDialedCustomer = true; // 表示已经对该客户发起了呼叫
			Integer dialCount = timers.getDialCount();
			if(dialCount == null) {
				dialCount = 0;
			}
			timers.setIsDialInLastPop(true);
			timers.setDialCount(dialCount+1);
			timers.setLastDialTime(new Date());
			timers = timersService.update(timers);
		}
	}
//	TODO 以后其他的按钮操作也按此种方式做
	/**
	 * 显示外转外配置界面 
	 */
	private void showP2PSettingWindow() {
		if(p2pSettingWindow == null) { 
			p2pSettingWindow = new CsrPhone2PhoneSettingWindow();
		} 
		handleSubWindow(p2pSettingWindow);
	}

	/**
	 * 添加子窗口，先删除子窗口，在添加它
	 * @param subWindow 需要添加的子窗口
	 */
	private void handleSubWindow(Window subWindow) {
		this.getApplication().getMainWindow().removeWindow(subWindow);
		this.getApplication().getMainWindow().addWindow(subWindow);
	}

	@Override
	public void windowClose(CloseEvent e) {
		Window window = e.getWindow();
		if(window == loginUserWindow) {
			Button cancel = loginUserInfoView.getCancel();
			if(cancel != null) {
				cancel.click();
			}
		} else if(window == noticeWindow) {
			noticeView.getNoticeTable().setValue(null);
		} else if(window == timerWindow) {
			timerView.getTimersTable().setValue(null);
		} else if(window == missCallWindow) {
			missCallMap.clear();
		} else if(window == timerResponseWindow) {
			// 当坐席点击关闭定时提醒窗口或者通过其他方式关闭窗口时，判断是否需要自动延期再提醒, 自动推迟的时间 = Timers.NEXT_POP_MIN 分钟
			// 	延期需要满足条件：1、坐席没有点击推迟按钮；2、坐席没有点击不再提醒按钮；3、坐席没有发起回访；4、该定时提醒跟客户相关
			if(!isDelay && !isForbidPop && !isDialedCustomer && timers.getCustomerId() != null) {
				calendar.setTime(new Date());
				calendar.add(Calendar.MINUTE, +Timers.NEXT_POP_MIN);
				timers.setResponseTime(calendar.getTime());
				timersService.update(timers);
				timersService.refreshSchedule(false, timers);	// 重建 TimerTask 
			}
			
		}
	}

}
