package com.jiangyifen.ec2.ui.csr.workarea.othermodules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.asteriskjava.manager.action.CommandAction;
import org.asteriskjava.manager.action.HangupAction;
import org.asteriskjava.manager.action.MeetMeMuteAction;
import org.asteriskjava.manager.action.MeetMeUnmuteAction;
import org.asteriskjava.manager.event.QueueEntryEvent;
import org.asteriskjava.manager.response.CommandResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.ami.AmiManagerThread;
import com.jiangyifen.ec2.autodialout.AutoDialHolder;
import com.jiangyifen.ec2.bean.MeetMeStatus;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.service.csr.ami.ChannelRedirectService;
import com.jiangyifen.ec2.service.csr.ami.DialService;
import com.jiangyifen.ec2.service.eaoservice.UserService;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.ColumnGenerator;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.BaseTheme;

/**
 * 话务员会议室监控界面
 * @author jrh
 *
 */
@SuppressWarnings("serial")
public class CsrMeetMeDetailSupervise extends VerticalLayout {

	// 表格各字段显示顺序
	private final Object[] VISIBLE_PROPERTIES = new Object[] {"callerIdNum", "channel","time", "name", "isMute", "roverStatus"};
	private final String[] COLUMN_HEADER = new String[] {"成员号码", "通道","时长","成员信息","旁听状态", "与会状态"};

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final String glabolQueue = "900000";	// 全局共用的队列，租户不可见
	
	private Table table; 							// 话务员拥有的会议室状态信息显示表格
	
	private volatile boolean isGotoRun = true;		// 是否允许刷新表格中的内容
	private volatile boolean isSpyAble = false;		// 描述当前坐席是否可以监听自己分机对应的这个会议室，如果自己也在会议室中，则不可监听，否则可以
	private BeanItemContainer<MeetMeStatus> meetMeStatusContainer;  
	
	private String meetingRoom;						// 监控的会议室名称
	private User loginUser;
	private HashMap<String, MeetMeStatus> roverMembers;

	private UserService userService;
	private DialService dialService;			// 呼叫服务类
	private ChannelRedirectService redirectService;
	
	public CsrMeetMeDetailSupervise() {
		this.setSizeFull();
		this.setSpacing(true);
		
		userService = SpringContextHolder.getBean("userService");
		dialService = SpringContextHolder.getBean("dialService");
		redirectService = SpringContextHolder.getBean("channelRedirectService");
		
		meetMeStatusContainer = new BeanItemContainer<MeetMeStatus>(MeetMeStatus.class);
		roverMembers = new HashMap<String, MeetMeStatus>();
		
		loginUser = SpringContextHolder.getLoginUser();
		meetingRoom = ShareData.userToExten.get(loginUser.getId());

		// 会议室提示窗口
		Label noticeLabel = new Label("<font color='blue'>实时状态监控：</font>", Label.CONTENT_XHTML);
		noticeLabel.setWidth("-1px");
//		this.addComponent(noticeLabel);
		
		// 创建表格组件
		table = createStatusTable();
		this.addComponent(table);
		this.setExpandRatio(table, 1.0f);
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
				} else if("roverStatus".equals(colId)) {
					String roverStatus = (String) property.getValue();
					if("inmeetting".equals(roverStatus)) {
						return "与会中";
					} else if("leaveMeetting".equals(roverStatus)) {
						return "离会中";
					}
				} 
				return super.formatPropertyValue(rowId, colId, property);
			}
		};
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
					CommandAction commandAction = new CommandAction("meetme kick "+meetingRoom+" "+position);
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
						MeetMeMuteAction muteAction = new MeetMeMuteAction(meetingRoom, position);
						boolean issuccess = AmiManagerThread.sendAction(muteAction);
						if(issuccess) {
							status.setIsMute(true);
							mute_bt.setCaption("解除旁听");
						}
					} else {
						MeetMeUnmuteAction unmuteAction = new MeetMeUnmuteAction(meetingRoom, position);
						boolean issuccess = AmiManagerThread.sendAction(unmuteAction);
						if(issuccess) {
							status.setIsMute(false);
							mute_bt.setCaption("旁听");
						}
					}
				}
			});

			final boolean isInMeetting = "inmeetting".equals(status.getRoverStatus());
			String roverCaption = isInMeetting ? "暂时离会" : "重邀与会";
			final Button rover_bt = new Button(roverCaption);
			rover_bt.setStyleName(BaseTheme.BUTTON_LINK);
			op_lo.addComponent(rover_bt);
			
			rover_bt.addListener(new ClickListener() {
				@Override
				public void buttonClick(ClickEvent event) {
					String memberChannel = status.getChannel();
					if(isInMeetting) {
						boolean issuccess = redirectService.redirectQueue(memberChannel, glabolQueue);
						logger.info("离开--meeting--------------"+memberChannel+"----------------离开会议");
						if(issuccess) {
							status.setRoverStatus("leaveMeetting");
							roverMembers.put(memberChannel, status);
							rover_bt.setCaption("重邀与会");
						}
					} else {
						boolean issuccess = redirectService.redirectCommonExtension(memberChannel, "998"+meetingRoom);
						logger.info("加入--meeting--------------"+memberChannel+"----------------加入会议");
						if(issuccess) {
							status.setRoverStatus("inmeetting");
							roverMembers.remove(memberChannel);
							rover_bt.setCaption("暂时离会");
						}
					}
				}
			});
			
			// 只用当前坐席不在会议室内，并且当前会议室成员是分机
			if(isSpyAble && ShareData.domainToExts.get(loginUser.getDomain().getId()).contains(callerIdNum)) {
				Button spy_bt = new Button("监听");
				spy_bt.setStyleName(BaseTheme.BUTTON_LINK);
				op_lo.addComponent(spy_bt);
				
				spy_bt.addListener(new ClickListener() {
					@Override
					public void buttonClick(ClickEvent event) {
						//chb 挂断mgrExten的所有Channel
						Set<String> channels = ShareData.peernameAndChannels.get(meetingRoom);
						if(channels != null) {
							for(String channel : channels){
								HangupAction hangupAction = new HangupAction(channel);
								AmiManagerThread.sendAction(hangupAction);
							}
						}
						dialService.dial(meetingRoom, "555"+callerIdNum);
					}
				});
			}

			return op_lo;
		}
	}
	
	/**
	 * 更新通监控界面
	 */
	public void update() {
		// 每次调用刷新方法时，先将设置可刷新状态
		isGotoRun = true;
		
		// 由于前面发生了ValueChange 事件，所有数据已经更新， 然后每隔一秒更新一下界面
		new Thread("话务员会议室监控界面"+System.currentTimeMillis()) {
			@Override
			public void run() {
				while(isGotoRun) {
					try {
						// 更新的信息
						updateTableDataSource();
						// 刷新表格的缓存资源
						synchronized(table) {
							table.refreshRowCache();
						}
					} catch (Exception e) {
						logger.error(e.getMessage() +"更新活动会议室监控界面 ，线程更新表格信息出现异常！" + e.getMessage(), e);
						throw new RuntimeException("更新活动会议室监控界面 ，线程更新表格信息出现异常！");
					}
					try {
						Thread.sleep(3000); // 更新成功还是失败，都让线程睡眠3秒
					} catch (InterruptedException e) {
						logger.error(e.getMessage()+"更新活动会议室监控界面 ，线程休眠出现异常！" + e.getMessage(), e);
						throw new RuntimeException("更新活动会议室监控界面 ，线程休眠出现异常！");
					}
				}
			}
		}.start();
	}
	
	/**
	 * 获取所有通话状态，更新表格的数据源
	 */
	private void updateTableDataSource() {
		// 清空通话情况表格中的内容
		synchronized (meetMeStatusContainer) {
			meetMeStatusContainer.removeAllItems();
		}

		isSpyAble = true;
		
		// 获取当前用户所在域的所有通话
		List<MeetMeStatus> meetMeStatus = new ArrayList<MeetMeStatus>();

		// 将那些被转移到队列中的与会成员仍然在会议室详情中显示
		List<QueueEntryEvent> qees = AutoDialHolder.queueToWaiters.get(glabolQueue);
		if(qees == null) {
			roverMembers.clear();
		} else {
			for(QueueEntryEvent qee : qees) {
				String qeeChannel = qee.getChannel();
				if(qeeChannel != null) {
					if(roverMembers.containsKey(qeeChannel)) {
						MeetMeStatus status = roverMembers.get(qeeChannel);
						if(meetingRoom.equals(status.getCallerIdNum())) {	// 如果离会成员中含有当前用户的分机，那也将不能监听
							isSpyAble = false;
						}
						meetMeStatus.add(status);
					} else {	// 客户被定到全局队列后，可能自己挂断了，所以这里需要做移除操作
						roverMembers.remove(qeeChannel);
					}
				}
			}
		}
		
		// managerConnection.login();
		CommandAction action = new CommandAction("meetme list " +meetingRoom+ " concise");
		CommandResponse r = (CommandResponse) AmiManagerThread.sendResponseAction(action);
		
		if(r.getResult() == null || r.getResult().toString().indexOf("!") == -1){
			synchronized (meetMeStatusContainer) {	// 修改通话情况表格中的内容
				meetMeStatusContainer.addAll(meetMeStatus);
			}
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

			if(meetingRoom.equals(callerIdNum)) {
				isSpyAble = false;
			}
			
			if (userId != null) {
				User user = (User) userService.get(userId);
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
			mms.setRole(role);
			mms.setIsMute(isMute);
			mms.setTime(time);
			mms.setName(name);
			mms.setRoverStatus("inmeetting");

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

	public void setGotoRun(boolean isGotoRun) {
		this.isGotoRun = isGotoRun;
	}

}
