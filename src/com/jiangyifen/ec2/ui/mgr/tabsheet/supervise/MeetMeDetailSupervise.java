package com.jiangyifen.ec2.ui.mgr.tabsheet.supervise;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.asteriskjava.manager.action.CommandAction;
import org.asteriskjava.manager.action.HangupAction;
import org.asteriskjava.manager.action.MeetMeMuteAction;
import org.asteriskjava.manager.action.MeetMeUnmuteAction;
import org.asteriskjava.manager.response.CommandResponse;

import com.jiangyifen.ec2.ami.AmiManagerThread;
import com.jiangyifen.ec2.bean.MeetMeStatus;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.entity.enumtype.ExecuteType;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.service.common.CommonService;
import com.jiangyifen.ec2.service.csr.ami.DialService;
import com.jiangyifen.ec2.ui.util.NotificationUtil;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.validator.RegexpValidator;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.Window;
import com.vaadin.ui.Table.ColumnGenerator;
import com.vaadin.ui.Window.Notification;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.BaseTheme;

/**
 *	会议室详情监控界面 
 * @author chb
 */

@SuppressWarnings("serial")
public class MeetMeDetailSupervise extends VerticalLayout implements Button.ClickListener {

	// 表格各字段显示顺序
	private final Object[] VISIBLE_PROPERTIES = new Object[] {"callerIdNum", "channel","time", "name", "isMute"};
	private final String[] COLUMN_HEADER = new String[] {"成员号码", "通道","时长","成员信息","成员状态"};

//	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	// 搜索的组件
	private TextField keyWord;
	private Button search;
	private TextField extenForSpy;	 			// 用于监听的分机
	
	private Table table; 						// 通话状态信息显示表格

	private Window spyConfirm_wd;				// 监控确认窗口
	private TextField mgrExten_tf;	 			// 用于监听的分机
	private Button save_bt;						// 保存按钮
	private Button cancel_bt;					// 取消按钮 
	
//	private volatile boolean isGotoRun = true;	// 是否允许刷新表格中的内容
	private BeanItemContainer<MeetMeStatus> meetMeStatusContainer;  
	
	private String superviseConfno="";
	private User loginUser;
	private Domain domain;						// 当前用户所属域

	private CommonService commonService;
	private DialService dialService;			// 呼叫服务类
	
	public MeetMeDetailSupervise() {

		loginUser = SpringContextHolder.getLoginUser();
		domain = SpringContextHolder.getDomain();
		meetMeStatusContainer = new BeanItemContainer<MeetMeStatus>(MeetMeStatus.class);

		commonService= SpringContextHolder.getBean("commonService");
		dialService = SpringContextHolder.getBean("dialService");
		
		 //搜索的内容输出
		this.addComponent(buildSearchLayout());
		
		// 创建表格组件
		table=createStatusTable();
		this.addComponent(table);
		this.setExpandRatio(table, 1.0f);

		// 创建监听确认窗口
		this.createSpyConfirmWindow();
	}
	
	/**
	 * 创建搜索的内容输出
	 * 
	 * @return
	 */
	private HorizontalLayout buildSearchLayout() {
		HorizontalLayout mainLayout = new HorizontalLayout();
		mainLayout.setWidth("100%");

		// 关键字Label
		Label room_lb = new Label("会议室号码：");
		room_lb.setWidth("-1px");
		mainLayout.addComponent(room_lb);

		// 关键字组件
		keyWord = new TextField();
		mainLayout.addComponent(keyWord);

		// 搜索按钮的约束组件
		HorizontalLayout buttonConstraintLayout = new HorizontalLayout();
		mainLayout.addComponent(buttonConstraintLayout);

		// 搜索按钮
		search = new Button("查看");
		search.addListener(this);
		buttonConstraintLayout.addComponent(search);

		creatSpyExtenComponents(mainLayout);
		
		return mainLayout;
	}

	/**
	 * jrh
	 * 	创建用于监听的分机组件
	 * @return
	 */
	private void creatSpyExtenComponents(HorizontalLayout mainLayout) {
		// 用于监听的分机输入框
		HorizontalLayout extenLayout = new HorizontalLayout();
		extenLayout.setSpacing(true);
		mainLayout.addComponent(extenLayout);
		mainLayout.setComponentAlignment(extenLayout, Alignment.MIDDLE_RIGHT);
		mainLayout.setExpandRatio(extenLayout, 1.0f);

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
	private Table createStatusTable() {
		table = createFormatColumnTable();
		table.setSizeFull();
		table.setSelectable(true);
		table.setImmediate(true);
		table.setStyleName("striped");
		table.setRowHeaderMode(Table.ROW_HEADER_MODE_INDEX);
		
		table.setContainerDataSource(meetMeStatusContainer);
		table.setVisibleColumns(VISIBLE_PROPERTIES);
		table.setColumnHeaders(COLUMN_HEADER);
		table.addGeneratedColumn("operate", new OperateColumn());
		table.setColumnHeader("operate", "操作项");
		return table;
	}

	/**
	 *  创建格式化 了 日期列的 Table对象
	 */
	private Table createFormatColumnTable() {
		return new Table() {
			@Override
			protected String formatPropertyValue(Object rowId, Object colId, Property property) {
				if(property.getValue() == null) {
					return "";
				} else if("isMute".equals(colId)) {
					boolean isMute = (Boolean) property.getValue();
					return isMute ? "旁听中" : "畅谈中";
				} 
				return super.formatPropertyValue(rowId, colId, property);
			}
		};
	}

	/**
	 * jrh
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
	 * @Description 描述：自动生成操作列，操作选项：挂断、监听会议成员等
	 * 
	 * @author  jrh
	 * @date    2013-11-5 上午9:34:29
	 * @version
	 */
	private class OperateColumn implements ColumnGenerator {
		@Override
		public Object generateCell(Table source, Object itemId, Object columnId) {
			final MeetMeStatus status = (MeetMeStatus) itemId;
			final String callerIdNum = status.getCallerIdNum();
			final int position = status.getPosition();
			final boolean isMute = status.getIsMute();
			
			HorizontalLayout op_lo = new HorizontalLayout();
			op_lo.setSpacing(true);
			
			Button hangup_bt = new Button("挂断");
			hangup_bt.setStyleName(BaseTheme.BUTTON_LINK);
			op_lo.addComponent(hangup_bt);
			
			// 挂断，这里使用的发command ,也可以使用HangupAction
			hangup_bt.addListener(new ClickListener() {
				@Override
				public void buttonClick(ClickEvent event) {
					CommandAction commandAction = new CommandAction("meetme kick "+superviseConfno+" "+position);
					AmiManagerThread.sendAction(commandAction);
				}
			});

			// 让与会成员进入只听不说状态，或取消mute
			String caption = isMute ? "解除旁听" : "旁听";
			final Button mute_bt = new Button(caption);
			mute_bt.setStyleName(BaseTheme.BUTTON_LINK);
			op_lo.addComponent(mute_bt);
			
			mute_bt.addListener(new ClickListener() {
				@Override
				public void buttonClick(ClickEvent event) {
					if(!isMute) {
						MeetMeMuteAction muteAction = new MeetMeMuteAction(superviseConfno, position);
						boolean issuccess = AmiManagerThread.sendAction(muteAction);
						if(issuccess) {
							status.setIsMute(true);
							mute_bt.setCaption("解除旁听");
						}
					} else {
						MeetMeUnmuteAction unmuteAction = new MeetMeUnmuteAction(superviseConfno, position);
						boolean issuccess = AmiManagerThread.sendAction(unmuteAction);
						if(issuccess) {
							status.setIsMute(false);
							mute_bt.setCaption("旁听");
						}
					}
				}
			});
			
			// 只用当前坐席不在会议室内，并且当前会议室成员是分机
			if(ShareData.domainToExts.get(loginUser.getDomain().getId()).contains(callerIdNum)) {
				Button spy_bt = new Button("监听");
				spy_bt.setStyleName(BaseTheme.BUTTON_LINK);
				op_lo.addComponent(spy_bt);
				
				spy_bt.addListener(new ClickListener() {
					@Override
					public void buttonClick(ClickEvent event) {
						if(extenForSpy.isValid()) {
							String mgrExten = (String) extenForSpy.getValue();
							if("".equals(mgrExten)) {
								mgrExten_tf.setValue("");
								table.getApplication().getMainWindow().removeWindow(spyConfirm_wd);
								spyConfirm_wd.center();
								table.getApplication().getMainWindow().addWindow(spyConfirm_wd);
								return;
							}
							
							boolean existedInDomain = ShareData.domainToExts.get(domain.getId()).contains(mgrExten);
							boolean inUsed = ShareData.extenToUser.keySet().contains(mgrExten);
							if(!existedInDomain) {
								table.getApplication().getMainWindow().showNotification("您输入的 分机号 "+mgrExten+" 在系统中不存在，请更改后重试！", Notification.TYPE_WARNING_MESSAGE);
								extenForSpy.focus();
							} else if(inUsed) {
								table.getApplication().getMainWindow().showNotification("您选择的分机 "+mgrExten+" 已经被他人使用，请更改后重试！", Notification.TYPE_WARNING_MESSAGE);
								extenForSpy.focus();
							} else {
								// 挂断mgrExten的所有Channel
								Set<String> channels = ShareData.peernameAndChannels.get(mgrExten);
								if(channels != null) {
									for(String channel : channels){
										HangupAction hangupAction = new HangupAction(channel);
										AmiManagerThread.sendAction(hangupAction);
									}
								}
								dialService.dial(mgrExten, "555"+callerIdNum);
							}
						}
					}
				});
			}
			
			return op_lo;
		}
	}
	
//	/**
//	 * 更新通监控界面
//	 */
//	public void update() {
//		keyWord.setValue("");
//		
//		// 每次调用刷新方法时，先将设置可刷新状态
//		isGotoRun = true;
		
//		// 由于前面发生了ValueChange 事件，所有数据已经更新， 然后每隔一秒更新一下界面
//		new Thread(new Runnable() {
//			@Override
//			public void run() {
//				while(isGotoRun) {
//					try {
//						
//						// 刷新表格的缓存资源
//						table.refreshRowCache();
//					} catch (Exception e) {
//						e.printStackTrace();
//						logger.error(e.getMessage() +"更新活动会议室监控界面 ，线程更新表格信息出现异常！", e);
//						throw new RuntimeException("更新活动会议室监控界面 ，线程更新表格信息出现异常！");
//					}
//					try {
//						Thread.sleep(1000); // 更新成功还是失败，都让线程睡眠1秒
//					} catch (InterruptedException e) {
//						e.printStackTrace();
//						logger.error(e.getMessage()+"更新活动会议室监控界面 ，线程休眠出现异常！", e);
//						throw new RuntimeException("更新活动会议室监控界面 ，线程休眠出现异常！");
//					}
//				}
//			}
//		}).start();
//	}
	
	/**
	 * 获取所有通话状态，更新表格的数据源
	 */
	public void updateTableDataSource() {
		// 清空通话情况表格中的内容
		synchronized (meetMeStatusContainer) {
			meetMeStatusContainer.removeAllItems();
		}

		// 获取当前用户所在域的所有通话
		List<MeetMeStatus> meetMeStatus = new ArrayList<MeetMeStatus>();

		//如果创建的会议室是不存在的
		if(!ShareData.domainToExts.get(domain.getId()).contains(getSuperviseConfno())){
			return;
		}
		
		// managerConnection.login();
		CommandAction action = new CommandAction("meetme list " + getSuperviseConfno() + " concise");
		CommandResponse r = (CommandResponse) AmiManagerThread.sendResponseAction(action);

		if(r.getResult() == null||r.getResult().toString().indexOf("!") == -1){
			return;
		}
		
		for (String line : r.getResult()) {
			String[] fields = line.split("!");
			int position = 0;
			if (StringUtils.isNumeric(fields[0])) {
				position = new Integer(fields[0]);
			}
			String callerIdNum = fields[1];
			String callerIdName = fields[2];
			String channel = fields[3];
			String role = fields[4];
			boolean isMute = false;
			if(StringUtils.isNumeric(fields[6])) {
				int muteNum = new Integer(fields[6]);
				if(muteNum == 1) {
					isMute = true;
				}
			}
			String time = fields[9];
			String name = "";
			Long userId= ShareData.extenToUser.get(callerIdNum);

			if (userId != null) {
				User user=(User)commonService.excuteSql("select u from User as u where u.id="+userId, ExecuteType.SINGLE_RESULT);
				if(user!=null) {
					name = user.getUsername();
					if(user.getRealName() != null && !"".equals(user.getRealName())) {
						name += "-"+user.getRealName();
					}
				}
			}

			MeetMeStatus mms = new MeetMeStatus();
			mms.setPosition(position);
			mms.setCallerIdNum(callerIdNum);
			mms.setCallerIdName(callerIdName);
			mms.setChannel(channel);
			mms.setIsMute(isMute);
			mms.setRole(role);
			mms.setTime(time);
			mms.setName(name);

			meetMeStatus.add(mms);
		}
		
		// 修改通话情况表格中的内容
		synchronized (meetMeStatusContainer) {
			meetMeStatusContainer.addAll(meetMeStatus);
		}
		
		// 排序
		synchronized (meetMeStatusContainer) {
			meetMeStatusContainer.sort(new Object[]{"position"}, new boolean[]{true});
		}
	}

	/**
	 * 通过设置参数查看指定会议室
	 */
	private void executeViewConf() {
		String confNo=StringUtils.trimToEmpty((String)keyWord.getValue());
		if(confNo.equals("")||!StringUtils.isNumeric(confNo)){
			NotificationUtil.showWarningNotification(this, "请确保会议室号码正确");
			keyWord.setValue("");
			return;
		}
		
		if(!ShareData.domainToExts.get(domain.getId()).contains(confNo)){
			NotificationUtil.showWarningNotification(this, "无此会议室或无权查看此会议室");
			return;
		}
		setSuperviseConfno(confNo);
	}
	
	
	public synchronized void setSuperviseConfno(String superviseConfno) {
		superviseConfno=StringUtils.trimToEmpty(superviseConfno);
		this.superviseConfno = superviseConfno;
	}

	public synchronized String getSuperviseConfno() {
		return superviseConfno;
	}

//	public void setGotoRun(boolean isGotoRun) {
//		this.isGotoRun = isGotoRun;
//	}

	// 监听按钮点击事件
	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if (source == search) {
			try {
				executeViewConf();
			} catch (Exception e) {
				NotificationUtil.showWarningNotification(this, "不能查看次会议室！");
				e.printStackTrace();
			}
		} if(source == save_bt) {
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

}
