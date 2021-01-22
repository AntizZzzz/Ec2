package com.jiangyifen.ec2.ui.mgr.tabsheet.supervise;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.asteriskjava.manager.action.HangupAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.ami.AmiManagerThread;
import com.jiangyifen.ec2.bean.ChannelSession;
import com.jiangyifen.ec2.bean.EmployeeStatusEntity;
import com.jiangyifen.ec2.bean.ExtenStatus;
import com.jiangyifen.ec2.bean.RoleType;
import com.jiangyifen.ec2.entity.Department;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.Role;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.globaldata.ResourceDataMgr;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.service.csr.ami.ChannelRedirectService;
import com.jiangyifen.ec2.service.csr.ami.DialService;
import com.jiangyifen.ec2.service.csr.ami.HangupService;
import com.jiangyifen.ec2.service.eaoservice.DepartmentService;
import com.jiangyifen.ec2.service.eaoservice.UserService;
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
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;
import com.vaadin.ui.themes.BaseTheme;

/**
 *	职工状态实时监控界面
 * @author jrh
 */
@SuppressWarnings("serial")
public class EmployeeStatusSupervise extends VerticalLayout implements ValueChangeListener, ClickListener {

	// 表格中语音文件夹的各字段显示顺序
	private final Object[] VISIBLE_PROPERTIES = new Object[] {"username", "realName", "empNo", "isLogin", "interfaze", "interfazeRegisterStatus", "callStatus", "callerIdNum", "connectedLineNum", "callSeconds", "deptName"};

	private final String[] COL_HEADERS = new String[] {EmployeeStatusEntity.USERNAME_HEADER, EmployeeStatusEntity.REALNAME_HEADER, EmployeeStatusEntity.EMPNO_HEADER, EmployeeStatusEntity.ISLOGIN_HEADER, 
			EmployeeStatusEntity.INTERFAZE_HEADER, EmployeeStatusEntity.INTERFAZE_REGISTER_STATUS_HEADER, EmployeeStatusEntity.CALLSTATUS_HEADER, EmployeeStatusEntity.CALLERIDNUM_HEADER, 
			EmployeeStatusEntity.CONNECTEDLINENUM_HEADER, EmployeeStatusEntity.CALLSECONDS_HEADER, EmployeeStatusEntity.DEPTNAME_HEADER};
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private ComboBox deptSelector;	 			// 部门选择框
	private Label onlineCountValueLabel;	 	// 当前在线人数 chb
	private TextField extenForSpy;	 			// 用于监听的分机
	private Table employeeStatusTable; 			// 职工状态信息显示表格
	private Notification notification;			// 提示信息
	
	private Window spyConfirm_wd;				// 监控确认窗口
	private TextField mgrExten_tf;	 			// 用于监听的分机
	private Button save_bt;						// 保存按钮
	private Button cancel_bt;					// 取消按钮 
	
	private User loginUser;						// 当前登陆的用户
	private Domain domain;						// 当前用户所属域
	private Department nearestSelectedDept;		// 最近选中的部门
	private BeanItemContainer<Department> deptContainer;	 // 当前用户可以看到的部门
	private BeanItemContainer<EmployeeStatusEntity> employeeStatusContainer;
	
	private volatile boolean hasCreatedThread = false;	// 否创建一个新的线程
	private volatile boolean isGotoRun = true;			// 是否允许刷新表格中的内容
	private UserService userService;						// 用户服务类
	private DepartmentService departmentService;			// 部门服务类
	private HangupService hangupService;					// 挂机服务类
	private DialService dialService;						// 呼叫服务类
	private ChannelRedirectService channelRedirectService;	// 电话转接服务类
	
	
	public EmployeeStatusSupervise() {
		this.setSizeFull();
		this.setSpacing(true);
		this.setMargin(true, true, false, true);
		
		domain = SpringContextHolder.getDomain();
		loginUser = SpringContextHolder.getLoginUser();
		userService = SpringContextHolder.getBean("userService");
		departmentService = SpringContextHolder.getBean("departmentService");
		hangupService = SpringContextHolder.getBean("hangupService");
		dialService = SpringContextHolder.getBean("dialService");
		channelRedirectService = SpringContextHolder.getBean("channelRedirectService");

		notification = new Notification("");
		notification.setDelayMsec(1000);
		notification.setHtmlContentAllowed(true);
		
		deptContainer = new BeanItemContainer<Department>(Department.class);
		employeeStatusContainer = new BeanItemContainer<EmployeeStatusEntity>(EmployeeStatusEntity.class);
		
		// 创建表格上方的组件
		this.createTableTopComponenets();
		
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
		
		// 部门选择框
		HorizontalLayout searchLayout = new HorizontalLayout();
		searchLayout.setSpacing(true);
		topLayout.addComponent(searchLayout);
		
		Label deptLabel = new Label("<B>部门选择：</B>", Label.CONTENT_XHTML);
		deptLabel.setWidth("-1px");
		searchLayout.addComponent(deptLabel);
		
		deptSelector = new ComboBox();
		deptSelector.addListener(this);
		deptSelector.setNullSelectionAllowed(false);
		deptSelector.setContainerDataSource(deptContainer);
		deptSelector.setItemCaptionPropertyId("name");
		searchLayout.addComponent(deptSelector);

		Label onlineCountCaptionLabel = new Label("&nbsp;&nbsp;&nbsp;<B>登陆人数：</B>", Label.CONTENT_XHTML);
		onlineCountCaptionLabel.setWidth("-1px");
		searchLayout.addComponent(onlineCountCaptionLabel);

		onlineCountValueLabel = new Label("");
		onlineCountValueLabel.setWidth("-1px");
		searchLayout.addComponent(onlineCountValueLabel);
		
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
	 *  创建表格组件
	 */
	private void createEmployeeTable() {
		employeeStatusTable = createFormatColumnTable();
		employeeStatusTable.setSizeFull();
		employeeStatusTable.setSelectable(true);
		employeeStatusTable.setImmediate(true);
		employeeStatusTable.setStyleName("striped");
		employeeStatusTable.setRowHeaderMode(Table.ROW_HEADER_MODE_INDEX);
		this.addComponent(employeeStatusTable);
		this.setExpandRatio(employeeStatusTable, 1.0f);
		
		employeeStatusTable.setContainerDataSource(employeeStatusContainer);
		employeeStatusTable.setVisibleColumns(VISIBLE_PROPERTIES);
		employeeStatusTable.setColumnHeaders(COL_HEADERS);
		employeeStatusTable.addGeneratedColumn("operate", new OperateColumnGenerator());
		employeeStatusTable.setColumnHeader("operate", "操作项");
		employeeStatusTable.setColumnAlignment("operate", Table.ALIGN_CENTER);
		employeeStatusTable.setErrorHandler(new ComponentErrorHandler() {
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
	private Table createFormatColumnTable() {
		Table table = new Table() {
			@Override
			protected String formatPropertyValue(Object rowId, Object colId, Property property) {
				if(property.getType() == Boolean.class) {
					if(property.getValue() == null) {
						return "";
					} 
					if(property.getValue().equals(true)) {
						return "已登录";
					} else {
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
				employeeStatusTable.getApplication().getMainWindow().showNotification(notification);
			} else {
				if(extenForSpy.isValid()) {
					String mgrExten = (String) extenForSpy.getValue();
					if("".equals(mgrExten)) {
						mgrExten_tf.setValue("");
						employeeStatusTable.getApplication().getMainWindow().removeWindow(spyConfirm_wd);
						spyConfirm_wd.center();
						employeeStatusTable.getApplication().getMainWindow().addWindow(spyConfirm_wd);
						return;
					}
					
					boolean existedInDomain = ShareData.domainToExts.get(domain.getId()).contains(mgrExten);
					boolean inUsed = ShareData.extenToUser.keySet().contains(mgrExten);
					if(!existedInDomain) {
						employeeStatusTable.getApplication().getMainWindow().showNotification("您输入的 分机号 "+mgrExten+" 在系统中不存在，请更改后重试！", Notification.TYPE_WARNING_MESSAGE);
						extenForSpy.focus();
					} else if(inUsed) {
						employeeStatusTable.getApplication().getMainWindow().showNotification("您选择的分机 "+mgrExten+" 已经被他人使用，请更改后重试！", Notification.TYPE_WARNING_MESSAGE);
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
						
						employeeStatusTable.getApplication().getMainWindow().showNotification("操作成功！");
					}
				} else {
					employeeStatusTable.getApplication().getMainWindow().showNotification("您输入的分机号格式错误，只能是六位数字！",Notification.TYPE_WARNING_MESSAGE);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("jrh 职工状态监控，确认是否可以执行监听、密语、强插时，出现异常----》 "+e.getMessage(), e);
			employeeStatusTable.getApplication().getMainWindow().showNotification("操作失败,请稍后重试！",Notification.TYPE_WARNING_MESSAGE);
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
				employeeStatusTable.getApplication().getMainWindow().showNotification("用户 "+username+" 当前没有通话！",Notification.TYPE_WARNING_MESSAGE);
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
			employeeStatusTable.getApplication().getMainWindow().showNotification("强拆失败,请稍后重试！",Notification.TYPE_WARNING_MESSAGE);
		}
	}
	
	@Override
	public void valueChange(ValueChangeEvent event) {
		Property source = event.getProperty();
		if(source == deptSelector) {
			nearestSelectedDept = (Department) deptSelector.getValue();
			// 更新表格的数据源
			updateTableDataSource(nearestSelectedDept);
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
	 * 当选择的部门发生变化时，进行更新表格的操作
	 * @param selectedDept
	 */
	private void updateTableDataSource(Department selectedDept) {
		// 情况表格中的内容
		synchronized (employeeStatusContainer) {
			employeeStatusContainer.removeAllItems();
		}
		
		if(selectedDept == null) {
			return;
		}
		
		// jrh 重新获取表格需要显示的用户信息
		List<User> users = new ArrayList<User>();
		users.addAll(userService.getByDeptment(selectedDept, domain));
		
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
			String realName = StringUtils.trimToEmpty(user.getRealName());
			eStatusEntity.setRealName(realName);
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
		// 更新部门选择框中的信息
		updateDepartmentComboBox();
		
		if(!hasCreatedThread || !isGotoRun) {	// 第一次点击的时候是否创建
			// 每次调用刷新方法时，先将设置可刷新状态
			isGotoRun = true;
			
			// 由于前面发生了ValueChange 事件，所有数据已经更新， 然后每隔一秒更新一下界面
			new Thread("员工状态监控线程"+System.currentTimeMillis()) {
				@Override
				public void run() {
					while(isGotoRun) {
						try {
							List<EmployeeStatusEntity> employeeStatusEntities = new ArrayList<EmployeeStatusEntity>();
							synchronized (employeeStatusContainer) {
								employeeStatusEntities.addAll(employeeStatusContainer.getItemIds());
							}
							// 统计在线人数
							Integer onlineCount=0;
							
							for(EmployeeStatusEntity employeeStatusEntity : employeeStatusEntities) {
								Long userId = employeeStatusEntity.getUserId();
								String exten = ShareData.userToExten.get(userId);
								if(exten != null && !"".equals(exten)) {	// 用户在线
									employeeStatusEntity.setIsLogin(true);
									employeeStatusEntity.setInterfaze(exten);
									
									// 新增，分机状态信息
									ExtenStatus extenStatus = ShareData.extenStatusMap.get(exten);
									if(extenStatus != null) {
										employeeStatusEntity.setInterfazeRegisterStatus(extenStatus.getRegisterStatus());
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
//		TODO 目前一个分机只可能有一通通话，如果以后有多通通话时，则需要另外进行处理,不然会不对的
										List<String> channels = new ArrayList<String>(channelSet);
										String channel = channels.get(0);
										ChannelSession channelSession = ShareData.channelAndChannelSession.get(channel);
										if(channelSession != null) {
											String srcNum = channelSession.getCallerIdNum();
											String destNum = channelSession.getConnectedlinenum();
											if(srcNum == null) {	// 如果是呼入，分机为被叫，分机对应的ChannelSession 没有callerIdNum 值，所以要如此处理 20140818
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
									}
									onlineCount++;
								} else {	// 用户不在线
									employeeStatusEntity.setIsLogin(false);
									employeeStatusEntity.setInterfaze("");
									employeeStatusEntity.setCallerIdNum("");
									employeeStatusEntity.setCallStatus("");
									employeeStatusEntity.setConnectedLineNum("");
									employeeStatusEntity.setCallSeconds(null);
								}
							}
							
							onlineCountValueLabel.setValue(onlineCount);
							
							// 刷新表格的缓存资源
							synchronized (employeeStatusTable) {
								employeeStatusTable.refreshRowCache();
							}
						} catch (Exception e) {
							/*e.printStackTrace();*/
							logger.error(e.getMessage() +"更新职工状态监控界面时，线程更新表格信息出现异常！" + e.getMessage(), e);
							/*throw new RuntimeException("更新职工状态监控界面时，线程更新表格信息出现异常！");*/
						}
						
						try {
							Thread.sleep(5000); // 更新成功还是失败，都让线程睡眠5秒
						} catch (InterruptedException e) {
							/*e.printStackTrace();*/
							logger.error(e.getMessage()+"更新职工状态监控界面时，线程休眠出现异常！" + e.getMessage(), e);
							/*throw new RuntimeException("更新职工状态监控界面时，线程休眠出现异常！");*/
						}
					}
				}
			}.start();
			
			hasCreatedThread = true;
		}
	}

	/**
	 *  更新部门选择框中的信息
	 */
	private void updateDepartmentComboBox() {
		// jrh 获取当前用户所属角色的管辖部门的Id号
		Map<Long, Department> allGovernedDeptMap = new HashMap<Long, Department>();
		for(Role role : loginUser.getRoles()) {
			if(role.getType().equals(RoleType.manager)) {
				for(Department dept : departmentService.getGovernedDeptsByRole(role.getId())) {
					allGovernedDeptMap.put(dept.getId(), dept);
				}
			}
		}
		// 更新部门选项的值
		synchronized (deptContainer) {
			deptContainer.removeAllItems();
			deptContainer.addAll(allGovernedDeptMap.values());
		}
		// 重新回显部门选择框的值
		if(nearestSelectedDept == null) {
			for(Department dept : allGovernedDeptMap.values()) {
				nearestSelectedDept = dept;
				deptSelector.setValue(dept);
				break;
			}
		} else {
			Long nearestSelectedDeptId = nearestSelectedDept.getId();
			Department selectedDept = allGovernedDeptMap.get(nearestSelectedDeptId);
			if(selectedDept != null) {
				deptSelector.setValue(selectedDept);
			} else {
				for(Department dept : allGovernedDeptMap.values()) {
					nearestSelectedDept = dept;
					deptSelector.setValue(dept);
					break;
				}
			}
		}
	}

	public void setGotoRun(boolean isGotoRun) {
		this.isGotoRun = isGotoRun;
	}
	
}
