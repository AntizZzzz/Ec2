package com.jiangyifen.ec2.ui.mgr.tabsheet.supervise;

import java.util.ArrayList;
import java.util.List;

import org.asteriskjava.manager.action.CommandAction;
import org.asteriskjava.manager.response.CommandResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.ami.AmiManagerThread;
import com.jiangyifen.ec2.bean.MeetMeActiveStatus;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.ColumnGenerator;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.BaseTheme;

/**
 *	 活动会议室监控界面 
 * @author chb
 */

@SuppressWarnings("serial")
public class MeetMeSupervise extends VerticalLayout {

	// 表格各字段显示顺序
	private final Object[] VISIBLE_PROPERTIES = new Object[] {"confNum", "parties", "activity"};
	private final String[] COLUMN_HEADER = new String[] {"会议室号码", "会议室人数", "会议室活跃时间"};

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private Table table; 									// 通话状态信息显示表格
	private Domain domain;									// 当前用户所属域
	private volatile boolean isGotoRun = true;			// 是否允许刷新表格中的内容
	private volatile boolean hasCreatedThread = false;	// 否创建一个新的线程
	private BeanItemContainer<MeetMeActiveStatus> meetMeActiveStatusContainer;  
	
	private MeetMeDetailSupervise detailSupervise;//监控详情页面
	
	public MeetMeSupervise() {
		this.setSizeFull();
		this.setSpacing(true);
		this.setMargin(true, true, false, true);
		
		domain = SpringContextHolder.getDomain();
		meetMeActiveStatusContainer = new BeanItemContainer<MeetMeActiveStatus>(MeetMeActiveStatus.class);
		
		// 创建表格组件
		table=createStatusTable();
		this.addComponent(table);
		
		detailSupervise=new MeetMeDetailSupervise();
		detailSupervise.setSizeFull();
		this.addComponent(detailSupervise);
		this.setExpandRatio(detailSupervise, 1.0f);
	}
	
	/**
	 *  创建表格组件
	 */
	private Table createStatusTable() {
		table = new Table();
		table.setWidth("100%");
		table.setSelectable(true);
		table.setImmediate(true);
		table.setStyleName("striped");
		table.setRowHeaderMode(Table.ROW_HEADER_MODE_INDEX);
		
		table.setContainerDataSource(meetMeActiveStatusContainer);
		table.setVisibleColumns(VISIBLE_PROPERTIES);
		table.setColumnHeaders(COLUMN_HEADER);
		table.setPageLength(10);
		table.addGeneratedColumn("operate", new OperateColumn());
		table.setColumnHeader("operate", "操作项");
		return table;
	}

	/**
	 * @Description 描述：自动生成操作列，操作选项：终止会议室
	 * 
	 * @author  jrh
	 * @date    2013-11-5 上午9:34:29
	 * @version
	 */
	private class OperateColumn implements ColumnGenerator {
		@Override
		public Object generateCell(Table source, Object itemId, Object columnId) {
			MeetMeActiveStatus meetmeStatus = (MeetMeActiveStatus) itemId;
			final String meetingRoom = meetmeStatus.getConfNum();
			HorizontalLayout op_lo = new HorizontalLayout();
			op_lo.setSpacing(true);
			
			// 终止整个会议室
			Button stop_bt = new Button("终止会议");
			stop_bt.setStyleName(BaseTheme.BUTTON_LINK);
			op_lo.addComponent(stop_bt);
			
			stop_bt.addListener(new ClickListener() {
				@Override
				public void buttonClick(ClickEvent event) {
					CommandAction commandAction = new CommandAction("meetme kick "+meetingRoom+" all");
					AmiManagerThread.sendAction(commandAction);
				}
			});
			
			return op_lo;
		}
	}
	
	/**
	 * 更新通监控界面
	 */
	public void update() {
		
		if(!hasCreatedThread || !isGotoRun) {	// 第一次点击的时候是否创建
			// 每次调用刷新方法时，先将设置可刷新状态
			isGotoRun = true;
			
			// 由于前面发生了ValueChange 事件，所有数据已经更新， 然后每隔一秒更新一下界面
			new Thread("MeetMe监控线程"+System.currentTimeMillis()) {
				@Override
				public void run() {
					while(isGotoRun) {
						try {
							// 更新的信息
							updateTableDataSource();
							
							detailSupervise.updateTableDataSource();
							// 刷新表格的缓存资源
							synchronized (table) {
								table.refreshRowCache();
							}
						} catch (Exception e) {
							/*e.printStackTrace();*/
							logger.error(e.getMessage() +"更新活动会议室监控界面 ，线程更新表格信息出现异常！" + e.getMessage(), e);
							/*throw new RuntimeException("更新活动会议室监控界面 ，线程更新表格信息出现异常！");*/
						}
						try {
							Thread.sleep(5000); // 更新成功还是失败，都让线程睡眠1秒
						} catch (InterruptedException e) {
							/*e.printStackTrace();*/
							logger.error(e.getMessage()+"更新活动会议室监控界面 ，线程休眠出现异常！" + e.getMessage(), e);
							/*throw new RuntimeException("更新活动会议室监控界面 ，线程休眠出现异常！");*/
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
		synchronized (meetMeActiveStatusContainer) {
			meetMeActiveStatusContainer.removeAllItems();
		}

		// 获取当前用户所在域的所有通话
		List<MeetMeActiveStatus> meetMeActiveStatus = new ArrayList<MeetMeActiveStatus>();

		// managerConnection.login();
		CommandAction action = new CommandAction("meetme list concise");
		CommandResponse r = (CommandResponse) AmiManagerThread.sendResponseAction(action);
		
		for (String line : r.getResult()) {
			String[] fields = line.split("!");
			if(fields.length<3)
				continue;

			String confNum = fields[0];
			if(!ShareData.domainToExts.get(domain.getId()).contains(confNum)){
				continue;
			}

			String parties = fields[1];
			String activity = fields[3];

			MeetMeActiveStatus meetMeActiveStatu=new MeetMeActiveStatus();
			meetMeActiveStatu.setConfNum(confNum);
			meetMeActiveStatu.setParties(parties);
			meetMeActiveStatu.setActivity(activity);
			meetMeActiveStatus.add(meetMeActiveStatu);
		}
		
		// 修改通话情况表格中的内容
		synchronized (meetMeActiveStatusContainer) {
			meetMeActiveStatusContainer.addAll(meetMeActiveStatus);
		}
		
		// 排序
		synchronized (meetMeActiveStatusContainer) {
			meetMeActiveStatusContainer.sort(new Object[]{"confNum"}, new boolean[]{true});
		}
	}

	public void setGotoRun(boolean isGotoRun) {
		this.isGotoRun = isGotoRun;
	}
	
}
