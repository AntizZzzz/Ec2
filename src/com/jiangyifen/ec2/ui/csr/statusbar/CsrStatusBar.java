package com.jiangyifen.ec2.ui.csr.statusbar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.asteriskjava.fastagi.AgiChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.bean.ChannelSession;
import com.jiangyifen.ec2.entity.CallAfterHandleLog;
import com.jiangyifen.ec2.entity.CustomerResource;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.StaticQueueMember;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.entity.UserQueue;
import com.jiangyifen.ec2.globaldata.GlobalVariable;
import com.jiangyifen.ec2.globaldata.ResourceDataCsr;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.service.csr.ami.ChannelRedirectService;
import com.jiangyifen.ec2.service.csr.ami.DialService;
import com.jiangyifen.ec2.service.csr.ami.QueuePauseService;
import com.jiangyifen.ec2.service.eaoservice.CallAfterHandleLogService;
import com.jiangyifen.ec2.service.eaoservice.StaticQueueMemberService;
import com.jiangyifen.ec2.service.eaoservice.UserQueueService;
import com.jiangyifen.ec2.ui.csr.CsrWorkArea;
import com.jiangyifen.ec2.ui.csr.workarea.common.CustomDialPopupWindow;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.validator.RegexpValidator;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.PopupView;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.Notification;
import com.vaadin.ui.themes.BaseTheme;

@SuppressWarnings("serial")
public class CsrStatusBar extends VerticalLayout implements ClickListener {
	
	private final String RUN_CALL_AFTER = "running"; 
	
	private final String EXIT_CALL_AFTER = "unrunning";
	
	private final String OUT_EXIT_CALL_AFTER = "执行--<b><font color='red'>呼出</font></b>话后";
	private final String OUT_RUN_CALL_AFTER = "<font color='black'><b>终止</b></font>--<b><font color='red'>呼出</font></b>话后";
	
	private final String IN_EXIT_CALL_AFTER = "执行--<b>呼入</b>话后";
	private final String IN_RUN_CALL_AFTER = "<font color='black'><b>终止</b></font>--<b>呼入</b>话后";

	// 日志工具
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private final SimpleDateFormat SDF_ONLY_TIME = new SimpleDateFormat("HH:mm:ss");
	
	private Panel panel;
	
	private Notification warning_notification;			// 错误警告提示信息

	private Label calledNoLabel; 						// 被叫号码标题标签
	private TextField calledNo; 						// 被叫号码输入区
	private Button dial; 								// 呼叫
	private CustomDialPopupWindow customDialPopupWindow;// 自定义呼叫弹屏窗口

	private Button dialHoldOnCaller;					// 联系呼叫等待客户
	private Button holdOn_bt;							// 让被叫进入呼叫保持

	private Button joinMeettingRoom_bt;					// 进入会议室，执行三方通话
	
	private HangupMenu hangupMenu;						// 自定义挂断组件
	
	private Button outAfterCall_bt;						// 呼出话后处理
	private Button inAfterCall_bt;						// 呼入话后处理

	private AllQueuesPauseMenu allQueuesPauseMenu; 		// 全队列置忙菜单
	private SingleQueuePauseView singleQueuePauseView;
	private Label callStatusLabel; 						// 通话状态标签
	private Embedded callStatus; 						// 通话状态按钮
	private Label name; 								// 用户真是姓名
	private Label extNo; 								// 分机号标签
	private Label username; 							// 用户昵称

	private CsrChannelRedirectWindow channelRedirectWindow;
	private Button redirectButton;						// 转接弹出
	private GridLayout gridLayout;						// 存放以上所有组件
	
	private Label csrStatisticInfo_lb;					// 坐席的统计信息

	private String exten; 								// 分机号
	private User loginUser; 							// 当前登录用户
	private Domain domain;								// 当前登录用户所属域
	private String loginTimeStr;						// 当前用户本次登陆界面的时间
	private Integer[] screenResolution;					// 屏幕分辨率
	private ArrayList<String> queueNameList;			// 队列名集合

	private CsrWorkArea csrWorkArea;					// 坐席中央主界面
	
	private DialService dialService;					// 拨号服务类
	private ChannelRedirectService channelRedirectService; 		// 电话转接服务类
	private CallAfterHandleLogService callAfterHandleLogService;
	private QueuePauseService queuePauseService;
	private UserQueueService userQueueService;
	private StaticQueueMemberService staticQueueMemberService;

	public CsrStatusBar(CsrWorkArea csrWorkArea) {
		this.csrWorkArea = csrWorkArea;
		
		// 给 service 等字段赋值
		initializeParameters();
		
		panel = new Panel();
		this.addComponent(panel);

		gridLayout = new GridLayout(13, 2);
		gridLayout.setWidth("100%");
		gridLayout.setSpacing(true);
		gridLayout.addStyleName("csrstatusbarmargin");
//		gridLayout.setMargin(false, true, false, true);
		panel.setContent(gridLayout);

		int coli_r1 = 0;
		HorizontalLayout dialHLayout = new HorizontalLayout();
		dialHLayout.setSpacing(true);
		gridLayout.addComponent(dialHLayout, coli_r1++, 0);

		calledNoLabel = new Label("&nbsp被叫：", Label.CONTENT_XHTML);
		calledNoLabel.setWidth("-1px");
		dialHLayout.addComponent(calledNoLabel);

		calledNo = new TextField();
		calledNo.setWidth("100px");
		calledNo.setInputPrompt("请输入被叫号码！");
		calledNo.setDescription("<B>拨打长途电话请加 '0'！</B>");
		calledNo.addValidator(new RegexpValidator("\\*?\\d+", "电话号格式错误，请重试！"));
		calledNo.setValidationVisible(false);
		dialHLayout.addComponent(calledNo);

		dial = new Button("呼叫", this);
		dial.setIcon(ResourceDataCsr.dial_16_sidebar_ico);
		dial.addStyleName("borderless");
		dial.addStyleName(BaseTheme.BUTTON_LINK);
		dialHLayout.addComponent(dial);

		// 转接电话区
		HorizontalLayout redirectHLayout = new HorizontalLayout();
		redirectHLayout.setSpacing(true);
		gridLayout.addComponent(redirectHLayout, coli_r1++, 0);

		channelRedirectWindow = new CsrChannelRedirectWindow();
		
		// 将转接界面加入共享区
		ShareData.csrToDialRedirectWindow.put(loginUser.getId(), channelRedirectWindow);

		redirectButton = new Button("电话转接", (ClickListener) this);
		redirectButton.setIcon(ResourceDataCsr.transfer_16_ico);
		redirectButton.setStyleName(BaseTheme.BUTTON_LINK);
		redirectHLayout.addComponent(redirectButton);

		// 联系呼叫等待用户按钮
		dialHoldOnCaller = new Button("联系呼叫等待客户<B>(0)</B>", this);
		dialHoldOnCaller.setIcon(ResourceDataCsr.dia_toward_right_16_sidebar_ico);
		dialHoldOnCaller.addStyleName("borderless");
		dialHoldOnCaller.setHtmlContentAllowed(true);
		dialHoldOnCaller.addStyleName(BaseTheme.BUTTON_LINK);
		gridLayout.addComponent(dialHoldOnCaller, coli_r1++, 0);
		this.updateDialHoldOnCallerBt();
		
		holdOn_bt = new Button("呼叫保持", this);
		holdOn_bt.addStyleName("borderless");
		holdOn_bt.setHtmlContentAllowed(true);
		holdOn_bt.addStyleName(BaseTheme.BUTTON_LINK);
		gridLayout.addComponent(holdOn_bt, coli_r1++, 0);

		joinMeettingRoom_bt = new Button("执行三方通话", this);
		joinMeettingRoom_bt.setIcon(ResourceDataCsr.csr_meeting_room_16_icon);
		joinMeettingRoom_bt.addStyleName("borderless");
		joinMeettingRoom_bt.setHtmlContentAllowed(true);
		joinMeettingRoom_bt.addStyleName(BaseTheme.BUTTON_LINK);
		gridLayout.addComponent(joinMeettingRoom_bt, coli_r1++, 0);
		
		HorizontalLayout hangupLayout = new HorizontalLayout();
		hangupMenu = new HangupMenu(exten);
		hangupLayout.addComponent(hangupMenu);
		gridLayout.addComponent(hangupLayout, coli_r1++, 0);

		// 进入呼出话后按钮
		outAfterCall_bt = new Button(OUT_EXIT_CALL_AFTER, this);
		outAfterCall_bt.setIcon(ResourceDataCsr.call_after_out_16_sidebar_ico);
		outAfterCall_bt.addStyleName("borderless");
		outAfterCall_bt.setHtmlContentAllowed(true);
		outAfterCall_bt.addStyleName(BaseTheme.BUTTON_LINK);
		outAfterCall_bt.setData(EXIT_CALL_AFTER);
		gridLayout.addComponent(outAfterCall_bt, coli_r1++, 0);
		
		// 进入呼出话后按钮
		inAfterCall_bt = new Button(IN_EXIT_CALL_AFTER, this);
		inAfterCall_bt.setIcon(ResourceDataCsr.call_after_in_16_sidebar_ico);
		inAfterCall_bt.addStyleName("borderless");
		inAfterCall_bt.setHtmlContentAllowed(true);
		inAfterCall_bt.addStyleName(BaseTheme.BUTTON_LINK);
		inAfterCall_bt.setData(EXIT_CALL_AFTER);
		gridLayout.addComponent(inAfterCall_bt, coli_r1++, 0);
		
		HorizontalLayout pauseHLayout = new HorizontalLayout();
		pauseHLayout.setSpacing(true);
		gridLayout.addComponent(pauseHLayout, coli_r1++, 0);

		// 全队列置忙标签
		Label pauseAllLabel = new Label("全队列置忙：");
		pauseAllLabel.setWidth("-1px");
		pauseHLayout.addComponent(pauseAllLabel);

		allQueuesPauseMenu = new AllQueuesPauseMenu();
		pauseHLayout.addComponent(allQueuesPauseMenu);
		
		// 单队列置忙
		singleQueuePauseView = new SingleQueuePauseView(allQueuesPauseMenu);
		PopupView singlePause = new PopupView(singleQueuePauseView);
		singlePause.setHideOnMouseOut(false);	// 千万注意，这里不能使用鼠标异常就隐藏(即不鞥设置为true),否则该组件下的菜单的“置闲”功能将失效
		pauseHLayout.addComponent(singlePause);

		HorizontalLayout callStatusHLayout = new HorizontalLayout();
		callStatusHLayout.setSpacing(true);
		gridLayout.addComponent(callStatusHLayout, coli_r1++, 0);

		callStatusLabel = new Label("通话状态：");
		callStatusLabel.setWidth("-1px");
		callStatusHLayout.addComponent(callStatusLabel);

		callStatus = new Embedded();
		callStatus.setSource(ResourceDataCsr.green_14_sidebar_ico);
		callStatusHLayout.addComponent(callStatus);
		callStatusHLayout.setComponentAlignment(callStatus, Alignment.MIDDLE_CENTER);

		username = new Label();
		username.setWidth("-1px");
		username.setValue("用户名：" + loginUser.getUsername());
		gridLayout.addComponent(username, coli_r1++, 0);

		name = new Label();
		name.setWidth("-1px");
		if(loginUser.getRealName() == null) {
			name.setValue("姓名：");
		} else {
			name.setValue("姓名：" + loginUser.getRealName());
		}
		gridLayout.addComponent(name, coli_r1++, 0);

		extNo = new Label();
		extNo.setWidth("-1px");
		extNo.setValue("分机：" + exten);
		gridLayout.addComponent(extNo, coli_r1++, 0);

		csrStatisticInfo_lb = new Label("", Label.CONTENT_XHTML);
		csrStatisticInfo_lb.setWidth("-1px");
		gridLayout.addComponent(csrStatisticInfo_lb, 0, 1, 12, 1);
		
		// 创建呼叫客户时需要显示个各种信息的组件
		createOutgoingDialWindow();
	}

	/**
	 * 初始化该类需要的一些字段
	 */
	private void initializeParameters() {
		this.setWidth("100%");
		this.setHeight("-1px");
		
		loginUser = SpringContextHolder.getLoginUser();
		domain = SpringContextHolder.getDomain();
		exten = SpringContextHolder.getExten();
		screenResolution = SpringContextHolder.getScreenResolution();

		queueNameList = new ArrayList<String>();
		
		dialService = SpringContextHolder.getBean("dialService");
		channelRedirectService = SpringContextHolder.getBean("channelRedirectService");
		callAfterHandleLogService = SpringContextHolder.getBean("callAfterHandleLogService");
		queuePauseService = SpringContextHolder.getBean("queuePauseService");
		userQueueService = SpringContextHolder.getBean("userQueueService");
		staticQueueMemberService = SpringContextHolder.getBean("staticQueueMemberService");
		
		warning_notification = new Notification("", Notification.TYPE_WARNING_MESSAGE);
		warning_notification.setDelayMsec(0);
		warning_notification.setHtmlContentAllowed(true);
		
		loginTimeStr = SDF_ONLY_TIME.format(new Date());
		
		// 将组件加入需要更新组件的Map 集合中
		ShareData.csrToStatusBar.put(loginUser.getId(), this);
	}
	
	/**
	 * 创建呼叫客户时需要显示个各种信息的组件
	 */
	private void createOutgoingDialWindow() {
		customDialPopupWindow = new CustomDialPopupWindow();
		if(screenResolution[0] >= 1366) {
			customDialPopupWindow.setWidth("1190px");
		} else {
			customDialPopupWindow.setWidth("940px");
		}
		
		if(screenResolution[1] > 768) {
			customDialPopupWindow.setHeight("610px");
		} else if(screenResolution[1] == 768) {
			customDialPopupWindow.setHeight("560px");
		} else {
			customDialPopupWindow.setHeight("510px");
		}

		customDialPopupWindow.setResizable(false);
		customDialPopupWindow.setStyleName("opaque");
	}
	
	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		try {
			if (source == dial) {
				String calledNoStr = (String) calledNo.getValue();
				if (calledNo.isValid() && !"".equals(calledNoStr)) {
					// 如果当前Tab 页不是 CsrStatusBar ，则将其置为CsrStatusBar
					VerticalLayout currentTab = ShareData.csrToCurrentTab.get(loginUser.getId());
					if(currentTab == null || currentTab.getClass() != CsrStatusBar.class) {
						ShareData.csrToCurrentTab.put(loginUser.getId(), CsrStatusBar.this);
					} 
					dialService.dial(exten, calledNoStr);
				} else {
					this.getApplication().getMainWindow().showNotification("请在号码前加 0 拨打长途电话！", Notification.TYPE_WARNING_MESSAGE);
					calledNo.focus();
				}
			} else if (source == redirectButton) {
				this.getApplication().getMainWindow().removeWindow(channelRedirectWindow);
				channelRedirectWindow.updateTableSource();
				this.getApplication().getMainWindow().addWindow(channelRedirectWindow);
			} else if(source == dialHoldOnCaller) {
				// 获取正等待当前用户的所有客户通道
				List<String> holdOnCallerChannels = ShareData.userExtenToHoldOnCallerChannels.get(exten);
				// 获取当前话务员的所有通道
				Set<String> currentDialChannels = ShareData.peernameAndChannels.get(exten);
				if(holdOnCallerChannels == null || holdOnCallerChannels.size() < 1) {
					warning_notification.setCaption("当前没有在等你处理问题的客户！");
					this.getApplication().getMainWindow().showNotification(warning_notification);
				} else if(currentDialChannels != null && currentDialChannels.size() > 0) {
					warning_notification.setCaption("您当前尚有其他通话在进行，请挂断后重试！");
					this.getApplication().getMainWindow().showNotification(warning_notification);
				} else {
					String holdOnCallerChannel = holdOnCallerChannels.get(0);
					holdOnCallerChannels.remove(0);
					channelRedirectService.redirectExten(holdOnCallerChannel, exten);
					// 处理过一条后，更新界面信息
					this.updateDialHoldOnCallerBt();
				}
			} else if(source == holdOn_bt) {
				// 首先判断当前有没有接通的电话
				ArrayList<String> allBridgedChannels = this.getAllBridgedChannel(exten);
				if(allBridgedChannels.size() == 0) {
					this.getApplication().getMainWindow().showNotification("对不起，您当前没有建立的通话！", Notification.TYPE_WARNING_MESSAGE);
					return;
				}

				// 然后获取当前的通话对象的通道，正常情况下只会有一通电话，并将其转入到等待队列中听音乐
				String selfBridgedChannel = allBridgedChannels.get(0);
				channelRedirectService.redirectQueue(selfBridgedChannel, "900000");
				
				// 将呼入的客户通道存放到当前坐席对应的内存中去，以便其待会接回
				List<String> holdOnCallerChannels = ShareData.userExtenToHoldOnCallerChannels.get(exten);
				if(holdOnCallerChannels == null) {
					holdOnCallerChannels = new ArrayList<String>();
					ShareData.userExtenToHoldOnCallerChannels.put(exten, holdOnCallerChannels);
				}
				if(!holdOnCallerChannels.contains(selfBridgedChannel)) {
					holdOnCallerChannels.add(selfBridgedChannel);
				}
				// 更新呼叫等待自己的按钮标题信息
				this.updateDialHoldOnCallerBt();
			} else if(source == joinMeettingRoom_bt) {
				// 首先判断当前有没有接通的电话
				HashMap<String, String> selfChannelToBridgedChannels = this.getAllSelfChannelToBridgedChannel(exten);
				if(selfChannelToBridgedChannels.size() == 0) {
					this.getApplication().getMainWindow().showNotification("对不起，您当前没有建立的通话！", Notification.TYPE_WARNING_MESSAGE);
					return;
				}

//				TODO
				for(String selfChannel : selfChannelToBridgedChannels.keySet()) {
					String bridgedChannel = selfChannelToBridgedChannels.get(selfChannel);
					/***************************************************************************************************/
					/** 注意：这里发起转接时--																   				  **/
					/**    	[如果处理的是 呼出 的情况]，在接通后，将两个通道进行redirect 到会议室，那么转接传递的两个通道顺序可以随意放 				  **/
					/**	   	[如果处理的书 呼入 的情况]，在接通后，两个通道的传递顺序不能修改，第一个通道只能是被叫分机对应的通道，而第二个通道必须是主叫对应的通道  **/
					/**						        如果呼入不按上面的要求进行传递通道参数，在两个通道都进入会议室后，大概在25秒后，被叫分机将自动挂断                **/
					/**	另外：该功能目前只能应用asterisk-1.8.25及其以上版本，对于上面呼入的情况，应该是asterisk-1.8.25的一个bug。                                       **/
					/**		如果呼入，并且使用的asterisk 版本低于V1.8.25，那无论怎么传递通道，在两个通道进入会议室后，过25秒，都会出现自动挂断的情况            **/
					/***************************************************************************************************/
					boolean issuccess = channelRedirectService.redirectDoubleChannels(bridgedChannel, selfChannel, "998"+exten);
					if(issuccess) {
						this.getApplication().getMainWindow().showNotification("成功进入三方通话，请到‘我的三方通话’界面邀请其他人加入！！");
						csrWorkArea.getCsrSideBar().getCsrMeetMe().click();
					}
					break;
				}
			} else if(source == outAfterCall_bt) {			// 呼出话后处理
				executeOutAfterCallHandle(true);
			} else if(source == inAfterCall_bt) {			// 呼入话后处理
				executeInAfterCallHandle(true);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("jrh CSR 界面的状态栏点击按钮时出现异常-->"+e.getMessage(), e);
		}
	}

	/**
	 * @Description 描述：执行呼出话后处理   
	 * 	
	 * 	之所以将该方法配置成public , 主要是为了能让其弹屏界面能够调用
	 *
	 * @author  JRH
	 * @date    2014年6月24日 上午11:15:29
	 * @param iscreateCallAfterLog 是否需要创建话后处理日志
	 */
	public void executeOutAfterCallHandle(boolean iscreateCallAfterLog) {
		if(outAfterCall_bt.isEnabled()) {
			outAfterCall_bt.setEnabled(false);
			String status = (String) outAfterCall_bt.getData();
			this.resetQueueNameList();	// 重置队列名称的集合

			Boolean iscreatePauseLog = true;
			ConcurrentHashMap<String, Boolean> domainConfigs = ShareData.domainToConfigs.get(domain.getId());
			if(domainConfigs != null) {
				iscreatePauseLog = domainConfigs.get("create_pause_log_after_csr_popup_calling_window");
				iscreatePauseLog = (iscreatePauseLog == null) ? true : iscreatePauseLog;
			}
			
			if(EXIT_CALL_AFTER.equals(status)) {
				boolean result = true;
				if(iscreateCallAfterLog) {
					result = callAfterHandleLogService.createNewLog(loginUser, exten, CallAfterHandleLog.DIRECTION_OUT);
				}
				
				if(result) {	// 如果创建话后处理成功，则全队列置忙
					outAfterCall_bt.setData(RUN_CALL_AFTER);
					outAfterCall_bt.setCaption(OUT_RUN_CALL_AFTER);
					queuePauseService.pauseAllByUser(queueNameList, loginUser, exten, true, GlobalVariable.AFTER_CALL_OUT_PAUSE_EXTEN_REASON, GlobalVariable.AFTER_CALL_OUT_PAUSE_EXTEN_REASON, iscreatePauseLog);
				}
			} else if(RUN_CALL_AFTER.equals(status)) {
				boolean result = true;
				if(iscreateCallAfterLog) {
					result = callAfterHandleLogService.finishLastLogByUid(loginUser.getId(), CallAfterHandleLog.DIRECTION_OUT);
				}

				if(result) {	// 如果创建话后处理成功，则全队列置闲
					outAfterCall_bt.setData(EXIT_CALL_AFTER);
					outAfterCall_bt.setCaption(OUT_EXIT_CALL_AFTER);
					queuePauseService.pauseAllByUser(queueNameList, loginUser, exten, false, GlobalVariable.DEFAULT_UNPAUSE_EXTEN_REASON, GlobalVariable.DEFAULT_UNPAUSE_EXTEN_REASON, iscreatePauseLog);
				}
				
			}
			outAfterCall_bt.setEnabled(true);
		}
	}

	/**
	 * @Description 描述：执行呼入话后处理
	 * 	
	 * 	之所以将该方法配置成public , 主要是为了能让其弹屏界面能够调用
	 *
	 * @author  JRH
	 * @date    2014年6月24日 上午11:15:12 
	 * @param iscreateCallAfterLog 是否需要创建话后处理日志
	 */
	public void executeInAfterCallHandle(boolean iscreateCallAfterLog) {
		if(inAfterCall_bt.isEnabled()) {
			inAfterCall_bt.setEnabled(false);
			String status = (String) inAfterCall_bt.getData();
			this.resetQueueNameList();	// 重置队列名称的集合
			
			Boolean iscreatePauseLog = true;
			ConcurrentHashMap<String, Boolean> domainConfigs = ShareData.domainToConfigs.get(domain.getId());
			if(domainConfigs != null) {
				iscreatePauseLog = domainConfigs.get("create_pause_log_after_csr_popup_calling_window");
				iscreatePauseLog = (iscreatePauseLog == null) ? true : iscreatePauseLog;
			}
			
			if(EXIT_CALL_AFTER.equals(status)) {
				boolean result = true;
				if(iscreateCallAfterLog) {
					result = callAfterHandleLogService.createNewLog(loginUser, exten, CallAfterHandleLog.DIRECTION_IN);
				}

				if(result) {	// 如果创建话后处理成功，则全队列置忙
					inAfterCall_bt.setData(RUN_CALL_AFTER);
					inAfterCall_bt.setCaption(IN_RUN_CALL_AFTER);
					queuePauseService.pauseAllByUser(queueNameList, loginUser, exten, true, GlobalVariable.AFTER_CALL_IN_PAUSE_EXTEN_REASON, GlobalVariable.AFTER_CALL_IN_PAUSE_EXTEN_REASON, iscreatePauseLog);
				}
			} else if(RUN_CALL_AFTER.equals(status)) {
				boolean result = true;
				if(iscreateCallAfterLog) {
					result = callAfterHandleLogService.finishLastLogByUid(loginUser.getId(), CallAfterHandleLog.DIRECTION_IN);
				}

				if(result) {	// 如果创建话后处理成功，则全队列置闲
					inAfterCall_bt.setData(EXIT_CALL_AFTER);
					inAfterCall_bt.setCaption(IN_EXIT_CALL_AFTER);
					queuePauseService.pauseAllByUser(queueNameList, loginUser, exten, false, GlobalVariable.DEFAULT_UNPAUSE_EXTEN_REASON, GlobalVariable.DEFAULT_UNPAUSE_EXTEN_REASON, iscreatePauseLog);
				}
			}
			inAfterCall_bt.setEnabled(true);
			
		}
	}

	/**
	 * @Description 描述：重置队列名称的集合
	 *
	 * @author  JRH
	 * @date    2014年6月24日 上午10:54:51 void
	 */
	private void resetQueueNameList() {
		// 清空队列集合
		queueNameList.clear();

		// 从动态队列中查询获取该分机所属的队列
		for(UserQueue userQueue : userQueueService.getAllByUsername(loginUser.getUsername())) {
			queueNameList.add(userQueue.getQueueName());
		}
		
		// 从静态队列中查询获取该分机所属的队列
		for(StaticQueueMember sqm : staticQueueMemberService.getAllBySipname(loginUser.getDomain(), exten)) {
			queueNameList.add(sqm.getQueueName());
		}
	}
	
	/**
	 * 取得所有处于Bridged状态的Channel
	 * 
	 * @return
	 */
	private ArrayList<String> getAllBridgedChannel(String exten) {
		Set<String> channels = ShareData.peernameAndChannels.get(exten);
		ArrayList<String> allBridgedChannels = new ArrayList<String>();
		if(channels != null) {
			for(String channel : channels) {
				ChannelSession channelSession = ShareData.channelAndChannelSession.get(channel);
				if (channelSession != null) {
					String brigdedChannel = channelSession.getBridgedChannel();
					if (brigdedChannel != null && !"".equals(brigdedChannel)) {
						allBridgedChannels.add(brigdedChannel);
					}
				}
			}
		}
		
		return allBridgedChannels;
	}

	/**
	 * 取得所有处于Bridged状态的Channel
	 * 
	 * @return
	 */
	private HashMap<String, String> getAllSelfChannelToBridgedChannel(String exten) {
		HashMap<String, String> selfChannelToBridgedChannels = new HashMap<String, String>();
		Set<String> channels = ShareData.peernameAndChannels.get(exten);
		if(channels != null) {
			for(String channel : channels) {
				ChannelSession channelSession = ShareData.channelAndChannelSession.get(channel);
				if (channelSession != null) {
					String brigdedChannel = channelSession.getBridgedChannel();
					if (brigdedChannel != null && !"".equals(brigdedChannel)) {
						selfChannelToBridgedChannels.put(channel, brigdedChannel);
					}
				}
			}
		}
		
		return selfChannelToBridgedChannels;
	}
	
	/**
	 * 更新指定的队列状态
	 * @param queueStatus	队列置忙情况集合
	 */
	public void updateSinglePauseStatus(Map<String, Object> queueStatus) {
		if(singleQueuePauseView != null) {
			singleQueuePauseView.refreshTable(queueStatus);
		}
	}

	/**
	 * 更新通话状态的图标  供 AsteriskEventListener.java 中的 NewStateEvent 事件调用
	 * @param callStatuIndex
	 */
	public synchronized void updateCallStatus(int callStatuIndex) {
		if (callStatuIndex == 1) {			// 工作中
			callStatus.setSource(ResourceDataCsr.red_14_sidebar_ico);
		} else if (callStatuIndex == 0) {	// 空闲
			callStatus.setSource(ResourceDataCsr.green_14_sidebar_ico);
		}
	}

	/**
	 * 根据当前的HangupMenu中的组件情况和当前用户的通话数量
	 * 	更改挂断按钮上的组件
	 * 	供 AsteriskEventListener 使用
	 */
	public void updateHangupMenuComponents() {
		hangupMenu.updateHangupComponent();
	}
	
	/**
	 * 更新联系呼叫等待按钮的标题
	 */
	public void updateDialHoldOnCallerBt() {
		List<String> holdOnCallerChannels = ShareData.userExtenToHoldOnCallerChannels.get(exten);
		int hondOnCount = 0;
		if(holdOnCallerChannels != null) {
			hondOnCount = holdOnCallerChannels.size();
		}
		dialHoldOnCaller.setCaption("联系呼叫等待客户<font color='black'><B>("+hondOnCount+")</B></font>");
	}
	
	/**
	 * 当电话振铃时，弹屏
	 * 	用于在系统中直接点击呼叫按钮，导致的弹屏
	 */
	public void showSystemCallPopWindow(CustomerResource customerResource, AgiChannel agiChannel) {
		this.getApplication().getMainWindow().removeWindow(customDialPopupWindow);
		
		// 回显弹窗中各组件的信息
		customDialPopupWindow.echoInformations(customerResource);
		customDialPopupWindow.setAgiChannel(agiChannel);
		customDialPopupWindow.center();
		this.getApplication().getMainWindow().addWindow(customDialPopupWindow);
	}
	
	/**
	 * 当电话振铃时，弹屏
	 * 	用于使用软电话直接发起的呼叫，导致的弹屏
	 */
	public void showSoftPhoneCallPopWindow(CustomerResource customerResource, AgiChannel agiChannel) {
		// 进行弹屏
		showSystemCallPopWindow(customerResource, agiChannel);
	}
	
	/**
	 * 供后台线程 UpdateCsrStatusBarThread 调用更新坐席的界面状态栏信息，要刷新的数据有：
	 * 		坐席：【上次登陆时间，上次登陆时长,今日累计登陆时长,今日累计置忙时长,呼叫总数，接通总数，总通时，平均应答速度】
	 * @param statisticInfos 存放意思八个数据的数组，该数组长度为8
	 */
	public void updateStatisticInfos(Object[] statisticInfos) {
		try {
			Object nearestLoginTime = (statisticInfos[0] == null) ? "无记录" : statisticInfos[0];
			Object nearestLoginDuration = (statisticInfos[1] == null) ? "00:00:00" : statisticInfos[1];
			Object currentLoginDuration = (statisticInfos[2] == null) ? "00:00:00" : statisticInfos[2];
			Object currentPauseDuration = (statisticInfos[3] == null) ? "00:00:00" : statisticInfos[3];
			Object callAmount = (statisticInfos[4] == null) ? "0" : statisticInfos[4];
			Object birdgedAmount = (statisticInfos[5] == null) ? "0" : statisticInfos[5];
			Object totalCallTime = (statisticInfos[6] == null) ? "0" : statisticInfos[6];
			Object avgAnwserSpeed = (statisticInfos[7] == null) ? "0.0" : statisticInfos[7];

			StringBuffer info_sbf = new StringBuffer();
			String pre_style = "<font color='red'><b>";
			String tail_style = "</b></font>";
			info_sbf.append("&nbsp;&nbsp;上次登陆时间：");
			info_sbf.append(pre_style);
			info_sbf.append(nearestLoginTime);
			info_sbf.append(tail_style);
			info_sbf.append(";&nbsp;&nbsp;上次在线时长：");
			info_sbf.append(pre_style);
			info_sbf.append(nearestLoginDuration);
			info_sbf.append(tail_style);
			info_sbf.append(";&nbsp;&nbsp;本次登陆时间：");
			info_sbf.append(pre_style);
			info_sbf.append(loginTimeStr);
			info_sbf.append(tail_style);
			info_sbf.append(";&nbsp;&nbsp;在线时长：");
			info_sbf.append(pre_style);
			info_sbf.append(currentLoginDuration);
			info_sbf.append(tail_style);
			info_sbf.append(";&nbsp;&nbsp;置忙时长：");
			info_sbf.append(pre_style);
			info_sbf.append(currentPauseDuration);
			info_sbf.append(tail_style);
			info_sbf.append(";&nbsp;&nbsp;呼叫总数：");
			info_sbf.append(pre_style);
			info_sbf.append(callAmount);
			info_sbf.append(tail_style);
			info_sbf.append(";&nbsp;&nbsp;接通总数：");
			info_sbf.append(pre_style);
			info_sbf.append(birdgedAmount);
			info_sbf.append(tail_style);
			info_sbf.append(";&nbsp;&nbsp;总通时：");
			info_sbf.append(pre_style);
			info_sbf.append(totalCallTime);
			info_sbf.append(tail_style);
			info_sbf.append("秒;&nbsp;&nbsp;平均应答速度：");
			info_sbf.append(pre_style);
			info_sbf.append(avgAnwserSpeed);
			info_sbf.append(tail_style);
			info_sbf.append("秒");
			csrStatisticInfo_lb.setValue(info_sbf.toString());
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("jrh 更新坐席"+username+"的状态栏时出现错误！-->"+e.getMessage(), e);
		}
	}
	
}
