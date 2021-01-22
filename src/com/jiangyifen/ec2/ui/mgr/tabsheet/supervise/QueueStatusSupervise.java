package com.jiangyifen.ec2.ui.mgr.tabsheet.supervise;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.asteriskjava.manager.action.HangupAction;
import org.asteriskjava.manager.event.QueueEntryEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.ami.AmiManagerThread;
import com.jiangyifen.ec2.autodialout.AutoDialHolder;
import com.jiangyifen.ec2.bean.ChannelSession;
import com.jiangyifen.ec2.bean.EmployeeStatusEntity;
import com.jiangyifen.ec2.bean.ExtenStatus;
import com.jiangyifen.ec2.bean.QueueStatusStatisticEntity;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.Queue;
import com.jiangyifen.ec2.entity.QueuePauseRecord;
import com.jiangyifen.ec2.entity.StaticQueueMember;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.entity.UserQueue;
import com.jiangyifen.ec2.globaldata.GlobalVariable;
import com.jiangyifen.ec2.globaldata.ResourceDataCsr;
import com.jiangyifen.ec2.globaldata.ResourceDataMgr;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.service.csr.ami.ChannelRedirectService;
import com.jiangyifen.ec2.service.csr.ami.DialService;
import com.jiangyifen.ec2.service.csr.ami.HangupService;
import com.jiangyifen.ec2.service.csr.ami.QueuePauseService;
import com.jiangyifen.ec2.service.eaoservice.QueuePauseRecordService;
import com.jiangyifen.ec2.service.eaoservice.QueueService;
import com.jiangyifen.ec2.service.eaoservice.StaticQueueMemberService;
import com.jiangyifen.ec2.service.eaoservice.UserQueueService;
import com.jiangyifen.ec2.service.eaoservice.UserService;
import com.jiangyifen.ec2.ui.report.tabsheet.utils.DateUtil;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.validator.RegexpValidator;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.ColumnGenerator;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;
import com.vaadin.ui.themes.BaseTheme;

/**
 *	队列状态实时监控界面 
 * @author jrh
 */

@SuppressWarnings("serial")
public class QueueStatusSupervise extends VerticalLayout implements ValueChangeListener, ClickListener {
	// 日志文件
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	// 队列统计情况表格中各字段显示顺序
	private final Object[] STATISTIC_VISIBLE_PROPERTIES = new Object[] {"queueName", "inQueueCount", "maxWaitLen", "callingCount", "memberAmount", "pauseCount", "loginAmount"};

	private final String[] STATISTIC_COL_HEADERS = new String[] {QueueStatusStatisticEntity.QUEUENAME_HEADER, QueueStatusStatisticEntity.INQUEUECOUNT_HEADER, 
			QueueStatusStatisticEntity.MAXWAITLEN_HEADER,  QueueStatusStatisticEntity.CALLINGCOUNT_HEADER, QueueStatusStatisticEntity.MEMBERAMOUT_HEADER, 
			QueueStatusStatisticEntity.PAUSECOUNT_HEADER, QueueStatusStatisticEntity.LOGINAMOUNT_HEADER};
	
	// 队列统计情况表格中各字段显示顺序
	private final Object[] WAITERS_VISIBLE_PROPERTIES = new Object[] {"queue", "callerIdNum", "position", "wait", "channel"};
	
	private final String[] WAITERS_COL_HEADERS = new String[] {"队列名称", "号码", "位置", "等待时长(秒)", "通道"};
	
	// 队列成员表格中各字段显示顺序
	private final Object[] MEMBER_VISIBLE_PROPERTIES = new Object[] {"username", "realName", "empNo", "isLogin", "interfaze", "interfazeRegisterStatus", "callStatus", "callerIdNum", "connectedLineNum", "callSeconds", "deptName"};

	private final String[] MEMBER_COL_HEADERS = new String[] {EmployeeStatusEntity.USERNAME_HEADER, EmployeeStatusEntity.REALNAME_HEADER, EmployeeStatusEntity.EMPNO_HEADER, EmployeeStatusEntity.ISLOGIN_HEADER, 
			EmployeeStatusEntity.INTERFAZE_HEADER, EmployeeStatusEntity.INTERFAZE_REGISTER_STATUS_HEADER, EmployeeStatusEntity.CALLSTATUS_HEADER, EmployeeStatusEntity.CALLERIDNUM_HEADER, 
			EmployeeStatusEntity.CONNECTEDLINENUM_HEADER, EmployeeStatusEntity.CALLSECONDS_HEADER, EmployeeStatusEntity.DEPTNAME_HEADER};

	private Notification notification;			// 提示信息
	private ComboBox queueSelector;	 			// 队列选择框
	private TextField extenForSpy;	 			// 用于监听的分机

	private Table statisticsTable;				// 队列统计表格
	private Table queueWaitersTable;			// 对列中的等到人员列表
	private Table queueStatusTable; 			// 队列状态信息显示表格

	private Window spyConfirm_wd;				// 监控确认窗口
	private TextField mgrExten_tf;	 			// 用于监听的分机
	private Button save_bt;						// 保存按钮
	private Button cancel_bt;					// 取消按钮 
	
	private Domain domain;						// 当前用户所属域
	private Queue nearestSelectedQueue;			// 最近选中的队列对象
	
	private BeanItemContainer<Queue> queueContainer;
	private BeanItemContainer<QueueStatusStatisticEntity> statisticContainer;
	private BeanItemContainer<QueueEntryEvent> queueWaitersContainer;
	private BeanItemContainer<EmployeeStatusEntity> employeeStatusContainer;  
	
	private UserService userService;						// 用户服务类
	private QueueService queueService;						// 队列服务类
	private DialService dialService;						// 呼叫服务类
	private HangupService hangupService;					// 挂机服务类
	private UserQueueService userQueueService;				// 队列成员服务类
	private QueuePauseService queuePauseService;			// 队列置忙服务类
	private StaticQueueMemberService staticQueueMemberService;
	private QueuePauseRecordService queuePauseRecordService;
	private ChannelRedirectService channelRedirectService;	// 电话转接服务类

	private volatile boolean isGotoRun = true;			// 是否允许刷新表格中的内容
	private volatile boolean hasCreatedThread = false;	// 否创建一个新的线程
	
	public QueueStatusSupervise() {
		this.setSizeFull();
		this.setSpacing(true);
		this.setMargin(true, true, false, true);
		domain = SpringContextHolder.getDomain();
		
		userService = SpringContextHolder.getBean("userService");
		queueService = SpringContextHolder.getBean("queueService");
		userQueueService = SpringContextHolder.getBean("userQueueService");
		staticQueueMemberService = SpringContextHolder.getBean("staticQueueMemberService");
		queuePauseService = SpringContextHolder.getBean("queuePauseService");
		dialService = SpringContextHolder.getBean("dialService");
		hangupService = SpringContextHolder.getBean("hangupService");
		queuePauseRecordService = SpringContextHolder.getBean("queuePauseRecordService");
		channelRedirectService = SpringContextHolder.getBean("channelRedirectService");

		notification = new Notification("");
		notification.setDelayMsec(1000);
		notification.setHtmlContentAllowed(true);
		
		queueContainer = new BeanItemContainer<Queue>(Queue.class);
		employeeStatusContainer = new BeanItemContainer<EmployeeStatusEntity>(EmployeeStatusEntity.class);
		queueWaitersContainer = new BeanItemContainer<QueueEntryEvent>(QueueEntryEvent.class);
		statisticContainer = new BeanItemContainer<QueueStatusStatisticEntity>(QueueStatusStatisticEntity.class);
		
		// 创建表格上方的组件
		this.createTableTopComponenets();
		
		// 创建队列统计表格
		this.createStatisticsTable();
		
		// 创建排队信息统计表格
		this.createWaitersTable();
		
		// 创建表格组件
		this.createEmployeeTable();

		// 创建监听确认窗口
		this.createSpyConfirmWindow();
	}

	/**
	 *  创建表格上方的组件
	 */
	private void createTableTopComponenets() {
		HorizontalLayout topLayout = new HorizontalLayout();
		topLayout.setSpacing(true);
		topLayout.setWidth("100%");
		this.addComponent(topLayout);
		
		// 队列选择框
		HorizontalLayout searchLayout = new HorizontalLayout();
		searchLayout.setSpacing(true);
		topLayout.addComponent(searchLayout);
		
		Label deptLabel = new Label("<B>队列选择：</B>", Label.CONTENT_XHTML);
		deptLabel.setWidth("-1px");
		searchLayout.addComponent(deptLabel);
		
		queueSelector = new ComboBox();
		queueSelector.addListener(this);
		queueSelector.setNullSelectionAllowed(false);
		queueSelector.setContainerDataSource(queueContainer);
		queueSelector.setItemCaptionPropertyId("descriptionAndName");
		searchLayout.addComponent(queueSelector);

		// 用于监听的分机输入框
		HorizontalLayout extenLayout = new HorizontalLayout();
		extenLayout.setSpacing(true);
		topLayout.addComponent(extenLayout);
		topLayout.setComponentAlignment(extenLayout, Alignment.MIDDLE_RIGHT);

		Label extenLabel = new Label("<B>监听者使用的分机：</B>", Label.CONTENT_XHTML);
		extenLabel.setWidth("-1px");
		extenLayout.addComponent(extenLabel);
		
		extenForSpy = new TextField();
		extenForSpy.setWidth("140px");
		extenForSpy.setInputPrompt("请输入分机号！");
		extenForSpy.setValidationVisible(false);
		extenForSpy.setDescription("<B>此处用于填写监听者要使用的分机号！</B>");
		extenForSpy.addValidator(new RegexpValidator("\\d+", "分机号只能为数字，请重试！"));
		extenLayout.addComponent(extenForSpy);
	}

	/**
	 *  创建队列清空统计表格
	 */
	private void createStatisticsTable() {
		statisticsTable = new Table("队列统计状况：");
		statisticsTable.setWidth("100%");
		statisticsTable.setHeight("40px");
		statisticsTable.setPageLength(1);
		statisticsTable.setSelectable(false);
		statisticsTable.setImmediate(true);
		statisticsTable.addStyleName("boldcaption");
		statisticsTable.addStyleName("striped");
		this.addComponent(statisticsTable);
		
		statisticsTable.setContainerDataSource(statisticContainer);
		statisticsTable.setVisibleColumns(STATISTIC_VISIBLE_PROPERTIES);
		statisticsTable.setColumnHeaders(STATISTIC_COL_HEADERS);
		statisticsTable.setErrorHandler(new ComponentErrorHandler() {
			@Override
			public boolean handleComponentError(ComponentErrorEvent event) {
				return true;
			}
		});
	}

	private void createWaitersTable() {
		queueWaitersTable = new Table("排队人员状况：");
		queueWaitersTable.setWidth("100%");
		queueWaitersTable.setHeight("-1px");
		queueWaitersTable.setPageLength(10);
		queueWaitersTable.setSelectable(true);
		queueWaitersTable.setImmediate(true);
		queueWaitersTable.addStyleName("boldcaption");
		queueWaitersTable.addStyleName("striped");
		queueWaitersTable.setRowHeaderMode(Table.ROW_HEADER_MODE_INDEX);
		this.addComponent(queueWaitersTable);
		
		queueWaitersTable.setContainerDataSource(queueWaitersContainer);
		queueWaitersTable.setVisibleColumns(WAITERS_VISIBLE_PROPERTIES);
		queueWaitersTable.setColumnHeaders(WAITERS_COL_HEADERS);
		queueWaitersTable.setErrorHandler(new ComponentErrorHandler() {
			@Override
			public boolean handleComponentError(ComponentErrorEvent event) {
				return true;
			}
		});
	}
	
	/**
	 *  创建表格组件
	 */
	private void createEmployeeTable() {
		queueStatusTable = createFormatColumnTable("队列成员状况：");
		queueStatusTable.setSizeFull();
		queueStatusTable.setSelectable(true);
		queueStatusTable.setImmediate(true);
		queueStatusTable.addStyleName("boldcaption");
		queueStatusTable.addStyleName("striped");
		queueStatusTable.setRowHeaderMode(Table.ROW_HEADER_MODE_INDEX);
		this.addComponent(queueStatusTable);
		this.setExpandRatio(queueStatusTable, 1.0f);
		
		queueStatusTable.setContainerDataSource(employeeStatusContainer);
		queueStatusTable.setVisibleColumns(MEMBER_VISIBLE_PROPERTIES);
		queueStatusTable.setColumnHeaders(MEMBER_COL_HEADERS);
		queueStatusTable.addGeneratedColumn("isLogin", new IsLoginColomn());
		queueStatusTable.setColumnAlignment("isLogin", Table.ALIGN_CENTER);
		queueStatusTable.setColumnHeader("isLogin", EmployeeStatusEntity.ISLOGIN_HEADER);
		queueStatusTable.addGeneratedColumn("pauseStatus", new PauseStatusColomn());
		queueStatusTable.setColumnAlignment("pauseStatus", Table.ALIGN_CENTER);
		queueStatusTable.setColumnHeader("pauseStatus", "置忙状态");
		queueStatusTable.addGeneratedColumn("operate", new OperateColumnGenerator());
		queueStatusTable.setColumnHeader("operate", "操作项");
		queueStatusTable.setColumnAlignment("operate", Table.ALIGN_CENTER);
		
		queueStatusTable.setErrorHandler(new ComponentErrorHandler() {
			@Override
			public boolean handleComponentError(ComponentErrorEvent event) {
				return true;
			}
		});
	}

	/**
	 *  创建监听确认窗口
	 */
	private void createSpyConfirmWindow() {
		VerticalLayout content_vl = new VerticalLayout();
		content_vl.setSpacing(true);
		content_vl.setMargin(true);
		
		spyConfirm_wd = new Window("请先填写分机");
		spyConfirm_wd.center();
		spyConfirm_wd.setContent(content_vl);
		spyConfirm_wd.setWidth("315px");
		spyConfirm_wd.setHeight("-1px");
		spyConfirm_wd.setResizable(false);
		
		mgrExten_tf = new TextField("监听者使用的分机：");
		mgrExten_tf.setWidth("140px");
		mgrExten_tf.setRequired(true);
		mgrExten_tf.setValidationVisible(false);
		mgrExten_tf.addValidator(new RegexpValidator("\\d+", "分机号只能为数字，请重试！"));
		
		FormLayout mgrExten_lo = new FormLayout();
		mgrExten_lo.addComponent(mgrExten_tf);
		spyConfirm_wd.addComponent(mgrExten_lo);
		
		save_bt = new Button("保存", this);
		save_bt.setImmediate(true);
		save_bt.setStyleName("default");
		
		cancel_bt = new Button("取消", this);
		cancel_bt.setImmediate(true);
		
		HorizontalLayout operate_lo = new HorizontalLayout();
		operate_lo.setSpacing(true);
		operate_lo.addComponent(save_bt);
		operate_lo.addComponent(cancel_bt);
		spyConfirm_wd.addComponent(operate_lo);
	}
	
	/**
	 *  创建格式化 了 Table对象
	 */
	private Table createFormatColumnTable(String caption) {
		Table table = new Table(caption) {
			@Override
			protected String formatPropertyValue(Object rowId, Object colId, Property property) {
				if(property.getType() == Boolean.class) {
					if(property.getValue() == null) {
						return "";
					} 
				} 
				return super.formatPropertyValue(rowId, colId, property);
			}
		};
		return table;
	}

	/**
	 * 用于自动生成操作列【监听、密语、强拆、强插】
	 */
	private class OperateColumnGenerator implements Table.ColumnGenerator {
		public Object generateCell(Table source, Object itemId, Object columnId) {
			final EmployeeStatusEntity employeeStatusEntity = (EmployeeStatusEntity) itemId;
			if(columnId.equals("operate")) {
				final String csrExten = employeeStatusEntity.getInterfaze();
				final String username = employeeStatusEntity.getUsername();
				Long userId = employeeStatusEntity.getUserId();
				boolean isLoggin = false;	// 坐席是否在线
				boolean isBusy = false;		// 坐席是否在通话
				boolean isoptAble = false;	// 操作组件是否可用

				if(ShareData.userToExten.containsKey(userId)) {	// 在线
					isLoggin = true;
				}
				
				if(csrExten != null && !"".equals(csrExten)) {	// 分机合法
					Set<String> channelSet = ShareData.peernameAndChannels.get(csrExten);
					if(channelSet != null && channelSet.size() > 0) {	// 用户正在通话
						isBusy = true;
					}
				}
				
				if(isLoggin && isBusy) {	// 如果用户在线
					isoptAble = true;
				}

				Button spy_bt = new Button("监听");
				spy_bt.setIcon(ResourceDataMgr.chanSpy_12_ico);
				spy_bt.addStyleName("borderless");
				spy_bt.addStyleName(BaseTheme.BUTTON_LINK);
				spy_bt.setImmediate(true);
				spy_bt.setEnabled(isoptAble);
				
				Button whisper_bt = new Button("密语");
				whisper_bt.setIcon(ResourceDataMgr.chanWhisper_12_ico);
				whisper_bt.addStyleName("borderless");
				whisper_bt.addStyleName(BaseTheme.BUTTON_LINK);
				whisper_bt.setImmediate(true);
				whisper_bt.setEnabled(isoptAble);
				
				Button hangup_bt = new Button("强拆");
				hangup_bt.setIcon(ResourceDataMgr.hangup_12_ico);
				hangup_bt.addStyleName("borderless");
				hangup_bt.addStyleName(BaseTheme.BUTTON_LINK);
				hangup_bt.setImmediate(true);
				hangup_bt.setEnabled(isoptAble);

				Button stickIn_bt = new Button("强插");
				stickIn_bt.setIcon(ResourceDataMgr.stickin_12_ico);
				stickIn_bt.addStyleName("borderless");
				stickIn_bt.addStyleName(BaseTheme.BUTTON_LINK);
				stickIn_bt.setImmediate(true);
				stickIn_bt.setEnabled(isoptAble);

				spy_bt.addListener(new ClickListener() {
					@Override
					public void buttonClick(ClickEvent event) {
						executeOptByType(csrExten, username, "spy");
					}
				});
				
				whisper_bt.addListener(new ClickListener() {
					@Override
					public void buttonClick(ClickEvent event) {
						executeOptByType(csrExten, username, "whisper");
					}
				});
				
				stickIn_bt.addListener(new ClickListener() {
					@Override
					public void buttonClick(ClickEvent event) {
						executeOptByType(csrExten, username, "stickin");
					}
				});
				
				hangup_bt.addListener(new ClickListener() {
					@Override
					public void buttonClick(ClickEvent event) {
						executeHangupCsrCall(csrExten, username);
					}
				});
				
				HorizontalLayout opt_hlo = new HorizontalLayout();
				opt_hlo.setSpacing(true);
				opt_hlo.addComponent(spy_bt);
				opt_hlo.addComponent(whisper_bt);
				opt_hlo.addComponent(hangup_bt);
				opt_hlo.addComponent(stickIn_bt);
				
				return opt_hlo;
			}
			return null;
		}
	}
	
	/**
	 * @Description 描述：确认是否可以执行监听、密语、强插，如果可以，返回管理员使用的分机号，否则返回null
	 *
	 * @author  jrh
	 * @date    2014年3月28日 下午4:16:15
	 * @param csrExten		坐席的分机
	 * @param username		用户名
	 * @return String		
	 */
	private void executeOptByType(final String csrExten, final String username, String optType) {
		try {
			Set<String> channelSet = ShareData.peernameAndChannels.get(csrExten);
			if(channelSet == null || channelSet.size() == 0) {	// 用户没有通话
				notification.setCaption("<font color='red'><B>用户 "+username+" 当前没有通话！</B></font>");
				queueStatusTable.getApplication().getMainWindow().showNotification(notification);
			} else {
				if(extenForSpy.isValid()) {
					String mgrExten = (String) extenForSpy.getValue();
					if("".equals(mgrExten)) {
						mgrExten_tf.setValue("");
						queueStatusTable.getApplication().getMainWindow().removeWindow(spyConfirm_wd);
						spyConfirm_wd.center();
						queueStatusTable.getApplication().getMainWindow().addWindow(spyConfirm_wd);
						return;
					}
					
					boolean existedInDomain = ShareData.domainToExts.get(domain.getId()).contains(mgrExten);
					boolean inUsed = ShareData.extenToUser.keySet().contains(mgrExten);
					if(!existedInDomain) {
						queueStatusTable.getApplication().getMainWindow().showNotification("您输入的 分机号 "+mgrExten+" 在系统中不存在，请更改后重试！", Notification.TYPE_WARNING_MESSAGE);
						extenForSpy.focus();
					} else if(inUsed) {
						queueStatusTable.getApplication().getMainWindow().showNotification("您选择的分机 "+mgrExten+" 已经被他人使用，请更改后重试！", Notification.TYPE_WARNING_MESSAGE);
						extenForSpy.focus();
					} else {
						//chb 强拆mgrExten的所有Channel
						Set<String> channels = ShareData.peernameAndChannels.get(mgrExten);
						if(channels != null) {
							for(String channel : channels){
								HangupAction hangupAction = new HangupAction(channel);
								AmiManagerThread.sendAction(hangupAction);
							}
						}
						
						if("spy".equals(optType)) {
							dialService.dial(mgrExten, "555"+csrExten);
//							System.out.println("mgr exten "+mgrExten+"--- spy csr exten ------"+csrExten);
						} else if("whisper".equals(optType)) {
							dialService.dial(mgrExten, "556"+csrExten);
//							System.out.println("mgr exten "+mgrExten+"--- whisper csr exten ------"+csrExten);
						} else if("stickin".equals(optType)) {
							for(String channel : channelSet) {
								ChannelSession channelSession = ShareData.channelAndChannelSession.get(channel);
								if (channelSession != null) {
									String bridgedChannel = channelSession.getBridgedChannel();
									if (bridgedChannel != null && (!"".equals(bridgedChannel))) {
										channelRedirectService.redirectExten(bridgedChannel, mgrExten);
//										System.out.println("mgr exten "+mgrExten+"--- stickin csr exten ------"+csrExten+ " ----- customer channel---"+bridgedChannel);
										break;
									}
								}
							}
						}
						
						queueStatusTable.getApplication().getMainWindow().showNotification("操作成功！");
					}
				} else {
					queueStatusTable.getApplication().getMainWindow().showNotification("您输入的分机号格式错误，只能是六位数字！",Notification.TYPE_WARNING_MESSAGE);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("jrh 职工状态监控，确认是否可以执行监听、密语、强插时，出现异常----》 "+e.getMessage(), e);
			queueStatusTable.getApplication().getMainWindow().showNotification("操作失败,请稍后重试！",Notification.TYPE_WARNING_MESSAGE);
		}
	}
	
	/**
	 * @Description 描述：执行挂断坐席的电话
	 *
	 * @author  jrh
	 * @date    2014年3月28日 下午4:14:37
	 * @param csrExten
	 * @param username void
	 */
	private void executeHangupCsrCall(final String csrExten, final String username) {
		try {
			Set<String> channelSet = ShareData.peernameAndChannels.get(csrExten);
			if(channelSet == null || channelSet.size() == 0) {	// 用户没有通话
				queueStatusTable.getApplication().getMainWindow().showNotification("用户 "+username+" 当前没有通话！",Notification.TYPE_WARNING_MESSAGE);
			} else {
				for(String channel : channelSet) {
					ChannelSession channelSession = ShareData.channelAndChannelSession.get(channel);
					hangupService.hangup(channel);
					if (channelSession != null) {
						String bridgedChannel = channelSession.getBridgedChannel();
						if (bridgedChannel != null && (!"".equals(bridgedChannel))) {
							hangupService.hangup(bridgedChannel);
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("jrh 职工状态监控，管理员强拆出现异常----》 "+e.getMessage(), e);
			queueStatusTable.getApplication().getMainWindow().showNotification("强拆失败,请稍后重试！",Notification.TYPE_WARNING_MESSAGE);
		}
	}
	
	/**
	 * 坐席是否在线，用图标消失
	 */
	public class IsLoginColomn implements ColumnGenerator {
		@Override
		public Object generateCell(Table source, Object itemId, Object columnId) {
			final EmployeeStatusEntity eStatusEntity = (EmployeeStatusEntity) itemId;
			Embedded icon = new Embedded();
			if(eStatusEntity.getIsLogin() != null && eStatusEntity.getIsLogin()) {
				icon.setSource(ResourceDataCsr.green_14_sidebar_ico);
				icon.setDescription("<B>在线</B>");
			} else {
				icon.setDescription("<B>离线</B>");
				icon.setSource(ResourceDataCsr.grey_14_sidebar_ico);
			}
			return icon;
		}
	}
	
	/**
	 * 分机单队列置忙，自动生成列
	 */
	public class PauseStatusColomn implements ColumnGenerator {
		@Override
		public Object generateCell(Table source, Object itemId, Object columnId) {
			final EmployeeStatusEntity eStatusEntity = (EmployeeStatusEntity) itemId;
			String exten = null;
			ExtenStatus extenStatus = null;
			final String interfaze = eStatusEntity.getInterfaze();
			final String queueName = nearestSelectedQueue.getName();
			final Button paused = new Button();
			paused.setStyleName(BaseTheme.BUTTON_LINK);
			paused.setImmediate(true);

			if(interfaze != null && !"".equals(interfaze)) {
				exten = interfaze.substring(interfaze.indexOf("/")+1);
				extenStatus = ShareData.extenStatusMap.get(exten);
			}
			
			if(exten != null && !"".equals(exten)) {	// 用户在线
				paused.setEnabled(true);
			} else {
				paused.setEnabled(false);
			}

			if(extenStatus != null) {	// 当前分机在当前队列中正处于忙的状态
				String pauseReason = extenStatus.getPauseQueues2Reason().get(queueName);
				
				/******************************************* 开始 ***************************************************/
				// 用于处理：能够显示各坐席维持在某一置忙状态的持续时长。如>> [管理员强制置忙01:24:46]
				QueuePauseRecord queuePauseRecord = queuePauseRecordService.getLastPauseRecord(eStatusEntity.getUsername(), exten, queueName);
				Date pauseDate = null;
				if(queuePauseRecord != null) {
					pauseDate = queuePauseRecord.getPauseDate();
				}
				String duration_title = "未知";
				if(pauseDate != null) {
					long duration = (new Date().getTime() - pauseDate.getTime())/1000;
					if(duration < 0) {
						duration = 0;
					}
					duration_title = DateUtil.getTime(duration);
				}
				/******************************************* 结束 ***************************************************/
				
				if(pauseReason != null) {
					paused.setCaption(pauseReason+" "+duration_title);
					paused.setData("pause");
					paused.setIcon(ResourceDataCsr.red_14_sidebar_ico);
				} else {
					pauseReason = extenStatus.getUnpauseQueues2Reason().get(queueName);
					if(pauseReason == null) {
						pauseReason = GlobalVariable.DEFAULT_UNPAUSE_EXTEN_REASON;
					}
					paused.setCaption(pauseReason+" "+duration_title);
					paused.setData("unpause");
					paused.setIcon(ResourceDataCsr.green_14_sidebar_ico);
				}
			} else {
				paused.setCaption(GlobalVariable.DEFAULT_UNPAUSE_EXTEN_REASON);
				paused.setData("unpause");
				paused.setIcon(ResourceDataCsr.green_14_sidebar_ico);
			}
			
			paused.addListener(new ClickListener() {
				@Override
				public void buttonClick(ClickEvent event) {
					try {
						String exten = interfaze.substring(interfaze.indexOf("/")+1);
						if("pause".equals(paused.getData())) {	// 由置忙状态--->置闲状态
							queuePauseService.pause(queueName, exten, false, GlobalVariable.MANAGER_FORCE_EXTEN_UNPAUSE_REASON);
							paused.setCaption(GlobalVariable.MANAGER_FORCE_EXTEN_UNPAUSE_REASON);
							paused.setData("unpause");
							paused.setIcon(ResourceDataCsr.green_14_sidebar_ico);
							
							// 更新置忙记录
							QueuePauseRecord queuePauseRecord = queuePauseRecordService.getLastPauseRecord(eStatusEntity.getUsername(), exten, queueName);
							if(queuePauseRecord != null) {
								queuePauseRecord.setUnpauseDate(new Date());
								queuePauseRecordService.update(queuePauseRecord);
							}
							createNewPauseRecord(eStatusEntity, queueName, exten, GlobalVariable.DEFAULT_UNPAUSE_EXTEN_REASON);
						} else {	// 由置闲状态--->置忙状态
							queuePauseService.pause(queueName, exten, true, GlobalVariable.MANAGER_FORCE_EXTEN_PAUSE_REASON);
							paused.setCaption(GlobalVariable.MANAGER_FORCE_EXTEN_PAUSE_REASON);
							paused.setData("pause");
							paused.setIcon(ResourceDataCsr.red_14_sidebar_ico);
							QueuePauseRecord queuePauseRecord = queuePauseRecordService.getLastPauseRecord(eStatusEntity.getUsername(), exten, queueName);
							if(queuePauseRecord != null) {	// 正常情况下，这里一定为空，但有可能客户在疯狂的点击，所以每次创建置忙记录前，添加一个检验过程
								queuePauseRecord.setUnpauseDate(new Date());
								queuePauseRecordService.update(queuePauseRecord);
							}
							createNewPauseRecord(eStatusEntity, queueName, exten, GlobalVariable.DEFAULT_PAUSE_RECORD_REASON);
						}
					} catch (Exception e) {
						e.printStackTrace();
						logger.error("jrh 队列状态监控，管理员将坐席置忙出现异常----》 "+e.getMessage(), e);
						queueStatusTable.getApplication().getMainWindow().showNotification("置忙失败,请稍后重试！",Notification.TYPE_WARNING_MESSAGE);
					}
				}
			});
			
			return paused;
		}
	}
	
	/**
	 * 创建队列成员置忙记录
	 * @param eStatusEntity	包含用户信息的中间实体
	 * @param queueName	队列名
	 * @param exten		分机号
	 * @param reason	置忙原因
	 */
	private void createNewPauseRecord(EmployeeStatusEntity eStatusEntity, String queueName, String exten, String reason) {
		QueuePauseRecord newPauseRecord = new QueuePauseRecord();
		newPauseRecord.setUsername(eStatusEntity.getUsername());
		newPauseRecord.setSipname("SIP/" + exten);
		newPauseRecord.setPauseDate(new Date());
		newPauseRecord.setQueue(queueName);
		newPauseRecord.setReason(reason);
		newPauseRecord.setDeptId(eStatusEntity.getDepartmentId());
		newPauseRecord.setDeptName(eStatusEntity.getDeptName());
		newPauseRecord.setDomainId(domain.getId());
		queuePauseRecordService.save(newPauseRecord);
	}
	
	@Override
	public void valueChange(ValueChangeEvent event) {
		Property source = event.getProperty();
		if(source == queueSelector) {
			nearestSelectedQueue = (Queue) queueSelector.getValue();
			
			// 更新表格的数据源
			updateStatisticTableSource(nearestSelectedQueue);
			updateQueueStatusTableSource(nearestSelectedQueue);
		}
		
	}

	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if(source == save_bt) {
			String mgrExten = (String) mgrExten_tf.getValue();
			if(!mgrExten_tf.isValid() || "".equals(mgrExten)) {
				mgrExten_tf.getApplication().getMainWindow().showNotification("分机号不能为空，并且只能由数字组成！", Notification.TYPE_WARNING_MESSAGE);
				mgrExten_tf.focus();
			} else if(!ShareData.domainToExts.get(domain.getId()).contains(mgrExten)) {
				mgrExten_tf.getApplication().getMainWindow().showNotification("您输入的 分机号 "+mgrExten+" 在系统中不存在，请更改后重试！", Notification.TYPE_WARNING_MESSAGE);
				mgrExten_tf.focus();
			} else if(ShareData.extenToUser.keySet().contains(mgrExten)) {
				mgrExten_tf.getApplication().getMainWindow().showNotification("您选择的分机 "+mgrExten+" 已经被他人使用，请更改后重试！", Notification.TYPE_WARNING_MESSAGE);
				mgrExten_tf.focus();
			} else {
				extenForSpy.setValue(mgrExten);
				extenForSpy.getApplication().getMainWindow().removeWindow(spyConfirm_wd);
			}
		} else if(source == cancel_bt) {
			extenForSpy.getApplication().getMainWindow().removeWindow(spyConfirm_wd);
		}
	}
	
	/**
	 * 更新表格  statisticsTable 的数据
	 * @param selectedQueue
	 */
	private void updateStatisticTableSource(Queue selectedQueue) {
		// 清空队列状态统计表格中的内容
		synchronized (statisticContainer) {
			statisticContainer.removeAllItems();
		}
		
		// 改变队列状态统计表格中的内容
		QueueStatusStatisticEntity queueStatusStatisticEntity = new QueueStatusStatisticEntity();
		synchronized (statisticContainer) {
			statisticContainer.addBean(queueStatusStatisticEntity);
		}
	}
	
	/**
	 * 更新表格  queueWaitersTable 的数据
	 * @param selectedQueue
	 */
	private void updateWaitersTableSource(Queue selectedQueue) {
		// 清空队列中的排队人情况表格中的内容
		synchronized (queueWaitersContainer) {
			queueWaitersContainer.removeAllItems();
		}

		// 更新排队人情况表格中的数据对象
		List<QueueEntryEvent> entryEvents = null;
		if(selectedQueue != null) {
			String queueName = selectedQueue.getName();
			entryEvents = AutoDialHolder.queueToWaiters.get(queueName);
		}
		synchronized (queueWaitersContainer) {
			if(entryEvents != null) {
				queueWaitersContainer.addAll(entryEvents);
			}
		}
		
		// 刷新缓存
		synchronized (queueWaitersTable) {
			queueWaitersTable.refreshRowCache();
		}
	}
	
	/**
	 * 更新表格  queueStatusTable 的数据
	 * @param selectedQueue
	 */
	private void updateQueueStatusTableSource(Queue selectedQueue) {
		// 清空表格中的内容
		synchronized (employeeStatusContainer) {
			employeeStatusContainer.removeAllItems();
		}
		
		// 重新获取表格需要显示的队列成员信息
		List<User> users = new ArrayList<User>();
		if(selectedQueue != null) {
			String queueName = selectedQueue.getName();
			if(selectedQueue.getDynamicmember() == true) {
				// 从动态队列中获取指定队列中对应的用户
				List<UserQueue> dynQueueMembers = userQueueService.getAllByQueueName(queueName, domain);
				for(UserQueue dynQueueMemeber : dynQueueMembers) {
					String userName = dynQueueMemeber.getUsername();
					users.addAll(userService.getUsersByUsername(userName));
				}
			} else {
				// 从动态队列中获取指定队列中对应的用户
				List<StaticQueueMember> staticQueueMembers = staticQueueMemberService.getAllByQueueName(domain, queueName);
				for(StaticQueueMember staticQueueMember : staticQueueMembers) {
					String exten = staticQueueMember.getSipname();
					Long userId = ShareData.extenToUser.get(exten);
					if(userId != null) {
						users.add(userService.get(userId));
					}
				}
			}
		}

		// 根据选有的用户对象，创建新的表格显示对象
		List<EmployeeStatusEntity> employeeStatusEntities = new ArrayList<EmployeeStatusEntity>();
		for(User user : users) {
			String exten = ShareData.userToExten.get(user.getId());
			boolean islogin = false;
			if(exten == null) {
				exten = "";
			} else {
				islogin = true;
			}
			
			EmployeeStatusEntity eStatusEntity = new EmployeeStatusEntity();
			eStatusEntity.setUserId(user.getId());
			eStatusEntity.setUsername(user.getUsername());
			eStatusEntity.setRealName(user.getRealName());
			eStatusEntity.setEmpNo(user.getEmpNo());
			eStatusEntity.setDepartmentId(user.getDepartment().getId());
			eStatusEntity.setDeptName(user.getDepartment().getName());
			eStatusEntity.setInterfaze(exten);
			eStatusEntity.setIsLogin(islogin);
			employeeStatusEntities.add(eStatusEntity);
		}

		// 添加显示对象
		synchronized (employeeStatusContainer) {
			employeeStatusContainer.addAll(employeeStatusEntities);
		}
		
		// 排序
		synchronized (employeeStatusContainer) {
			employeeStatusContainer.sort(new Object[]{"username", "empNo"}, new boolean[]{true, true});
		}
	}

	/**
	 * 更新职员监工界面中的表格信息，已经重新获取部门选择项的内容
	 * 	当职工监控界面Tab页被选中时执行（即TabSheet selectedTabChange发生时调用）
	 */
	public void update() {
		try {	// 更新队列选择框中的数据
			updateQueueComboBox();
		} catch (Exception e1) {
			e1.printStackTrace();
			logger.error(e1.getMessage() +"更新队列选择框中的数据出现异常！", e1);
			throw new RuntimeException("更新队列选择框中的数据出现异常！");
		}
		
		if(!hasCreatedThread || !isGotoRun) {	// 第一次点击的时候是否创建
			// 每次调用刷新方法时，先将设置可刷新状态
			isGotoRun = true;
			
			// 由于前面发生了ValueChange 事件，所有数据已经更新， 然后每隔一秒更新一下界面
			new Thread("队列监控线程"+System.currentTimeMillis()) {
				@Override
				public void run() {
					while(isGotoRun) {
						try {
							// 正在通话的数量
							Integer callingCount = 0;
							// 话机被置忙的数量
							Integer pausedCount = 0;
							// 在线坐席数量
							Integer onlineCount = 0;
							
							// 一、更新队列中的话务员信息
							List<EmployeeStatusEntity> employeeStatusEntities = new ArrayList<EmployeeStatusEntity>();
							synchronized (employeeStatusContainer) {
								employeeStatusEntities.addAll(employeeStatusContainer.getItemIds());
							}
							
							for(EmployeeStatusEntity employeeStatusEntity : employeeStatusEntities) {
								Long userId = employeeStatusEntity.getUserId();
								String exten = ShareData.userToExten.get(userId);
								String queueName = "";
								if(nearestSelectedQueue != null) {
									queueName = nearestSelectedQueue.getName();
								}
								
								if(exten != null && !"".equals(exten)) {	// 用户在线
									
									++onlineCount;	// 在线人数自增
									
									employeeStatusEntity.setIsLogin(true);
									employeeStatusEntity.setInterfaze(exten);
	
									// 统计置忙数量，以及检查用户使用的分机的注册状态
									ExtenStatus extenStatus = ShareData.extenStatusMap.get(exten);
									if(extenStatus != null) {
										employeeStatusEntity.setInterfazeRegisterStatus(extenStatus.getRegisterStatus());	// 分机的注册状态，新加的
										if(extenStatus.getPauseQueues2Reason().containsKey(queueName)) {	// 当前分机在当前队列中正处于忙的状态
											++pausedCount;
										}
									} else {
										employeeStatusEntity.setInterfazeRegisterStatus("UNKNOWN");
									}
	
									Set<String> channelSet = ShareData.peernameAndChannels.get(exten);  
									if(channelSet == null || channelSet.size() == 0) {	// 用户没有通话
										employeeStatusEntity.setCallerIdNum("");
										employeeStatusEntity.setCallStatus("");
										employeeStatusEntity.setConnectedLineNum("");
										employeeStatusEntity.setCallSeconds(null);
									} else if(channelSet != null && channelSet.size() > 0) {	// 用户有通话
	//	TODO 目前一个分机只可能有一通通话，如果以后有多通通话时，则需要另外进行处理,不然会不对的
										List<String> channels = new ArrayList<String>(channelSet);
										String channel = channels.get(0);
										ChannelSession channelSession = ShareData.channelAndChannelSession.get(channel);
										// 正常情况下，这里不用判断，但由于是线程，可能执行到这的时候
										if(channelSession != null) {
											String srcNum = channelSession.getCallerIdNum();
											String destNum = channelSession.getConnectedlinenum();
											if(srcNum == null) {	// 如果是呼入，分机为被叫，分机对应的ChannelSession 没有callerIdNum 值，所以要如此处理	 20140818
												srcNum = channelSession.getConnectedlinenum();
												destNum = exten;
											}
											employeeStatusEntity.setCallerIdNum(srcNum);
											employeeStatusEntity.setConnectedLineNum(destNum);
											
											employeeStatusEntity.setCallStatus(channelSession.getStatus());
											Integer seconds = channelSession.getSeconds();
											if(seconds == null) {
												seconds = (int) (System.currentTimeMillis() - channelSession.getCreateDate().getTime() + 500) / 1000;
											}
											employeeStatusEntity.setCallSeconds(seconds);
										}
										// 如果该用户的通道数大于0则表示在进行通话，通话数自增
										++callingCount;
									}
								} else {	// 用户不在线
									employeeStatusEntity.setIsLogin(false);
									employeeStatusEntity.setInterfaze("");
									employeeStatusEntity.setCallerIdNum("");
									employeeStatusEntity.setCallStatus("");
									employeeStatusEntity.setConnectedLineNum("");
									employeeStatusEntity.setCallSeconds(null);
								}
							}
							
							// 刷新队列成员表格的缓存资源
							synchronized (queueStatusTable) {
								queueStatusTable.refreshRowCache();
							}
							
							// 二、刷新队列统计数据
							List<QueueStatusStatisticEntity> queueStatusStatisticEntities = new ArrayList<QueueStatusStatisticEntity>();
							synchronized (statisticContainer) {
								queueStatusStatisticEntities.addAll(statisticContainer.getItemIds());
							}
							
							//chb 为空异常 TODO
							if(nearestSelectedQueue==null) continue;
							String queueName = nearestSelectedQueue.getName();
							for(QueueStatusStatisticEntity statusStatisticEntity : queueStatusStatisticEntities) {
								statusStatisticEntity.setQueueName(queueName);
								statusStatisticEntity.setInQueueCount(AutoDialHolder.queueToCallers.get(queueName));
								statusStatisticEntity.setMaxWaitLen(nearestSelectedQueue.getMaxlen());
								statusStatisticEntity.setCallingCount(callingCount);
								statusStatisticEntity.setMemberAmount(employeeStatusContainer.size());
								statusStatisticEntity.setPauseCount(pausedCount);
								// statusStatisticEntity.setLoginAmount(AutoDialHolder.queueToLoggedIn.get(queueName));	// 这样写有问题：这样会导致手机成员也被计入在线人数之列
								statusStatisticEntity.setLoginAmount(onlineCount);
							}
							
							// 刷新队列统计表格的缓存资源
							synchronized (statisticsTable) {
								statisticsTable.refreshRowCache();
							}
							
							// 刷新队列中排队成员表格信息
							updateWaitersTableSource(nearestSelectedQueue);
							
						} catch (Exception e) {
							/*e.printStackTrace();*/
							logger.error(e.getMessage() +"更新队列状态监控界面时，线程更新表格信息出现异常！" + e.getMessage(), e);
							/*throw new RuntimeException("更新队列状态监控界面时，线程更新表格信息出现异常！");*/
						}
						try {
							Thread.sleep(6000); // 更新成功还是失败，都让线程睡眠6秒
						} catch (InterruptedException e) {
							/*e.printStackTrace();*/
							logger.error(e.getMessage()+"更新队列状态监控界面时，线程休眠出现异常！" + e.getMessage(), e);
							/*throw new RuntimeException("更新队列状态监控界面时，线程休眠出现异常！");*/
						}
					}
				}
			}.start();
			
			hasCreatedThread = true;
		}
	}

	/**
	 *  更新队列选择框中的数据
	 */
	private void updateQueueComboBox() {
		// 重新获取指定域内的所有队列
		synchronized (queueContainer) {
			queueContainer.removeAllItems();
			queueContainer.addAll(queueService.getAllByDomain(domain));
		}

		// 重新回显队列选择框的值
		Long nearestSelectedQueueId = 0L;
		if(nearestSelectedQueue != null) {
			nearestSelectedQueueId = nearestSelectedQueue.getId();
		}
		
		if(queueContainer.size() == 0) {
			queueSelector.setValue(null);
		} else {
			// 查找之前选择的队列是否还存在，如果存在，则选中
			boolean hasExisted = false;
			for(Queue queue : queueContainer.getItemIds()) {
				if(queue.getId().equals(nearestSelectedQueueId)) {
					queueSelector.setValue(queue);
					hasExisted = true;
					break;
				}
			}
			
			// 如果没找到，默认选中第一个
			if(hasExisted == false) {
				for(Queue queue : queueContainer.getItemIds()) {
					queueSelector.setValue(queue);
					break;
				}
			}
		}
	}

	public void setGotoRun(boolean isGotoRun) {
		this.isGotoRun = isGotoRun;
	}
	
}
