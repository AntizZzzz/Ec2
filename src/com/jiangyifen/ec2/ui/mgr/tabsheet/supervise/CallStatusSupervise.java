package com.jiangyifen.ec2.ui.mgr.tabsheet.supervise;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.asteriskjava.manager.action.HangupAction;
import org.asteriskjava.manager.event.StatusEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.ami.AmiManagerThread;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.globaldata.ResourceDataMgr;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.service.csr.ami.DialService;
import com.jiangyifen.ec2.service.csr.ami.HangupService;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.validator.RegexpValidator;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
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
 *	通话状态实时监控界面 
 * @author jrh
 */

@SuppressWarnings("serial")
public class CallStatusSupervise extends VerticalLayout implements ClickListener {

	// 表格中语音文件夹的各字段显示顺序
	private final Object[] VISIBLE_PROPERTIES = new Object[] {"callerIdNum", "extension", "channel", "bridgedChannel", "channelStateDesc", "seconds"};

	private final String[] COL_HEADERS = new String[] { "主叫", "当前流程", "当前通道", "连接通道", "通话状态", "持续时长"};
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private Label callCount_lb;					 		// 当前呼叫总数
	private TextField extenForSpy;	 						// 用于监听的分机

	private Window spyConfirm_wd;							// 监控确认窗口
	private TextField mgrExten_tf;	 						// 用于监听的分机
	private Button save_bt;								// 保存按钮
	private Button cancel_bt;								// 取消按钮 
	
	private Table callStatusTable; 						// 通话状态信息显示表格
	private Domain domain;									// 当前用户所属域
	private volatile boolean isGotoRun = true;			// 是否允许刷新表格中的内容
	private volatile boolean hasCreatedThread = false;	// 否创建一个新的线程
	private BeanItemContainer<StatusEvent> callStatusContainer;  

	private HangupService hangupService;			// 挂机服务类
	private DialService dialService;				// 呼叫服务类
	
	public CallStatusSupervise() {
		this.setSizeFull();
		this.setSpacing(true);
		this.setMargin(true, true, false, true);
		
		domain = SpringContextHolder.getDomain();
		callStatusContainer = new BeanItemContainer<StatusEvent>(StatusEvent.class);
		
		hangupService = SpringContextHolder.getBean("hangupService");
		dialService = SpringContextHolder.getBean("dialService");
		
		// 创建顶部的附加组件
		createTopUI();
		
		// 创建表格组件
		createCallStatusTable();
		
		// 创建监听确认窗口
		this.createSpyConfirmWindow();
	}

	/**
	 * @Description 描述：界面顶部的附加组件
	 *
	 * @author  jrh
	 * @date    2014年4月14日 下午2:40:25 void
	 */
	private void createTopUI() {
		HorizontalLayout topLayout = new HorizontalLayout();
		topLayout.setSpacing(true);
		topLayout.setWidth("100%");
		this.addComponent(topLayout);

		// 左侧组件
		HorizontalLayout left_hlo = new HorizontalLayout();
		left_hlo.setSpacing(true);
		topLayout.addComponent(left_hlo);
		
		Label callCount_title_lb = new Label("<B>当前通话总数：</B>", Label.CONTENT_XHTML);
		callCount_title_lb.setWidth("-1px");
		left_hlo.addComponent(callCount_title_lb);
		
		callCount_lb = new Label("<font color='blue'><b>...</b></font>", Label.CONTENT_XHTML);
		callCount_lb.setWidth("-1px");
		left_hlo.addComponent(callCount_lb);
		
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
	private void createCallStatusTable() {
		callStatusTable = new Table();
		callStatusTable.setSizeFull();
		callStatusTable.setSelectable(true);
		callStatusTable.setImmediate(true);
		callStatusTable.setStyleName("striped");
		callStatusTable.setRowHeaderMode(Table.ROW_HEADER_MODE_INDEX);
		this.addComponent(callStatusTable);
		this.setExpandRatio(callStatusTable, 1.0f);
		
		callStatusTable.setContainerDataSource(callStatusContainer);
		callStatusTable.setVisibleColumns(VISIBLE_PROPERTIES);
		callStatusTable.setColumnHeaders(COL_HEADERS);
		callStatusTable.addGeneratedColumn("operate", new OperateColumnGenerator());
		callStatusTable.setColumnHeader("operate", "操作项");
		callStatusTable.setColumnAlignment("operate", Table.ALIGN_CENTER);
		callStatusTable.setErrorHandler(new ComponentErrorHandler() {
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
	 * 用于自动生成操作列【监听、密语、强拆、强插】
	 */
	private class OperateColumnGenerator implements Table.ColumnGenerator {
		public Object generateCell(Table source, Object itemId, Object columnId) {
			final StatusEvent statusEvent = (StatusEvent) itemId;
			if(columnId.equals("operate")) {
				Button spy_bt = new Button("监听");
				spy_bt.setIcon(ResourceDataMgr.chanSpy_12_ico);
				spy_bt.addStyleName("borderless");
				spy_bt.addStyleName(BaseTheme.BUTTON_LINK);
				spy_bt.setImmediate(true);
				
				Button hangup_bt = new Button("强拆");
				hangup_bt.setIcon(ResourceDataMgr.hangup_12_ico);
				hangup_bt.addStyleName("borderless");
				hangup_bt.addStyleName(BaseTheme.BUTTON_LINK);
				hangup_bt.setImmediate(true);

				spy_bt.addListener(new ClickListener() {
					@Override
					public void buttonClick(ClickEvent event) {
						executeOptByType(statusEvent, "spy");
					}
				});
				
				hangup_bt.addListener(new ClickListener() {
					@Override
					public void buttonClick(ClickEvent event) {
						executeHangupCsrCall(statusEvent);
					}
				});
				
				HorizontalLayout opt_hlo = new HorizontalLayout();
				opt_hlo.setSpacing(true);
				opt_hlo.addComponent(spy_bt);
				opt_hlo.addComponent(hangup_bt);
				
				return opt_hlo;
			}
			return null;
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
	private void executeHangupCsrCall(final StatusEvent statusEvent) {
		try {
			String selfChannel = statusEvent.getChannel();
			if (selfChannel != null && (!"".equals(selfChannel))) {
				hangupService.hangup(selfChannel);
			}

			String bridgedChannel = statusEvent.getBridgedChannel();
			if (bridgedChannel != null && (!"".equals(bridgedChannel))) {
				hangupService.hangup(bridgedChannel);
			}
		} catch (Exception e) {
			logger.error("jrh 通话状态监控，管理员强拆出现异常----》 "+e.getMessage(), e);
			callStatusTable.getApplication().getMainWindow().showNotification("强拆失败,请稍后重试！",Notification.TYPE_WARNING_MESSAGE);
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
	private void executeOptByType(final StatusEvent statusEvent, String optType) {
		try {
			if(extenForSpy.isValid()) {
				String mgrExten = (String) extenForSpy.getValue();
				if("".equals(mgrExten)) {
					mgrExten_tf.setValue("");
					callStatusTable.getApplication().getMainWindow().removeWindow(spyConfirm_wd);
					spyConfirm_wd.center();
					callStatusTable.getApplication().getMainWindow().addWindow(spyConfirm_wd);
					return;
				}
				
				boolean existedInDomain = ShareData.domainToExts.get(domain.getId()).contains(mgrExten);
				boolean inUsed = ShareData.extenToUser.keySet().contains(mgrExten);
				if(!existedInDomain) {
					callStatusTable.getApplication().getMainWindow().showNotification("您输入的 分机号 "+mgrExten+" 在系统中不存在，请更改后重试！", Notification.TYPE_WARNING_MESSAGE);
					extenForSpy.focus();
				} else if(inUsed) {
					callStatusTable.getApplication().getMainWindow().showNotification("您选择的分机 "+mgrExten+" 已经被他人使用，请更改后重试！", Notification.TYPE_WARNING_MESSAGE);
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
						String selfChannel = statusEvent.getChannel();
						String spyChannel = selfChannel.substring(selfChannel.indexOf("/") + 1, selfChannel.indexOf("-"));
						dialService.channelSpy(mgrExten, "555"+spyChannel, selfChannel);
					}
					
					callStatusTable.getApplication().getMainWindow().showNotification("操作成功！");
				}
			} else {
				callStatusTable.getApplication().getMainWindow().showNotification("您输入的分机号格式错误，只能是六位数字！",Notification.TYPE_WARNING_MESSAGE);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("jrh 职工状态监控，确认是否可以执行监听、密语、强插时，出现异常----》 "+e.getMessage(), e);
			callStatusTable.getApplication().getMainWindow().showNotification("操作失败,请稍后重试！",Notification.TYPE_WARNING_MESSAGE);
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
	 * 更新通话监工界面中的表格信息，已经重新获取部门选择项的内容
	 * 	当通话监控界面Tab页被选中时执行（即TabSheet selectedTabChange发生时调用）
	 */
	public void update() {

		if(!hasCreatedThread || !isGotoRun) {	// 第一次点击的时候是否创建
			// 每次调用刷新方法时，先将设置可刷新状态
			isGotoRun = true;
			
			// 由于前面发生了ValueChange 事件，所有数据已经更新， 然后每隔一秒更新一下界面
			new Thread("呼叫监控线程"+System.currentTimeMillis()) {
				@Override
				public void run() {
					while(isGotoRun) {
						try {
							// 更新部门选择框中的信息
							updateTableDataSource();
							
							// 刷新表格的缓存资源
							synchronized (callStatusTable) {
								callStatusTable.refreshRowCache();
							}
						} catch (Exception e) {
							/*e.printStackTrace();*/
							logger.error(e.getMessage() +"更新通话状态监控界面时，线程更新表格信息出现异常！" + e.getMessage(), e);
							/*throw new RuntimeException("更新通话状态监控界面时，线程更新表格信息出现异常！");*/
						}
						try {
							Thread.sleep(5000); // 更新成功还是失败，都让线程睡眠3秒
						} catch (InterruptedException e) {
							/*e.printStackTrace();*/
							logger.error(e.getMessage()+"更新通话状态监控界面时，线程休眠出现异常！" + e.getMessage(), e);
							/*throw new RuntimeException("更新通话状态监控界面时，线程休眠出现异常！");*/
						}
					}
				}
			}.start();
			
			hasCreatedThread = true;
		}
	}
	
	/**
	 * 获取所有通话状态，更新表格的数据源
	 */
	private void updateTableDataSource() {
		// 清空通话情况表格中的内容
		synchronized (callStatusContainer) {
			callStatusContainer.removeAllItems();
		}

		// 获取当前用户所在域的所有通话
		List<StatusEvent> statusEvents = new ArrayList<StatusEvent>();
		for(StatusEvent statusEvent : ShareData.statusEvents) {
			String channel = statusEvent.getChannel();
			// 只要extendsion 不为null 的通话
			if(statusEvent.getExtension() != null) {
				if(sipExistedInDomain(channel, domain)) {	// 值加载当前用户所在域的通话
					statusEvents.add(statusEvent);
				}
			}
		}
		
		// 修改通话情况表格中的内容
		synchronized (callStatusContainer) {
			callStatusContainer.addAll(statusEvents);
			callCount_lb.setValue("<font color='blue'><b>"+callStatusContainer.size()+"</b></font>");
		}
		
		// 排序
		synchronized (callStatusContainer) {
			callStatusContainer.sort(new Object[]{"callerIdNum"}, new boolean[]{true});
		}
	}
	
	/**
	 * 查询当前通话是不是当前用户所在域打出的
	 * 		只加载当前用户所属域正在进行的通话
	 * @param channel			主叫通道
	 * @param bridgedChannel	被叫通道
	 * @param domain			指定的域对象
	 * @return
	 */
	private boolean sipExistedInDomain(String channel, Domain domain) {
		boolean isInMyDomain = false;
		if(channel != null && !"".equals(channel)) {
			String sip = channel.substring(channel.indexOf("/") + 1, channel.indexOf("-"));
			// 检查主叫是不是当前域的分机
			isInMyDomain = ShareData.domainToExts.get(domain.getId()).contains(sip);
			// 如果不是，则检查是不是当前域的外线
			if(isInMyDomain == false) {
				isInMyDomain = ShareData.domainToOutlines.get(domain.getId()).contains(sip);
			}
		}
		return isInMyDomain;
	}

	public void setGotoRun(boolean isGotoRun) {
		this.isGotoRun = isGotoRun;
	}
	
}
