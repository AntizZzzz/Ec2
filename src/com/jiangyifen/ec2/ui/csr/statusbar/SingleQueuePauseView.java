package com.jiangyifen.ec2.ui.csr.statusbar;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.jiangyifen.ec2.entity.PauseReason;
import com.jiangyifen.ec2.entity.StaticQueueMember;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.entity.UserQueue;
import com.jiangyifen.ec2.globaldata.GlobalVariable;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.service.csr.ami.QueuePauseService;
import com.jiangyifen.ec2.service.eaoservice.PauseReasonService;
import com.jiangyifen.ec2.service.eaoservice.QueuePauseRecordService;
import com.jiangyifen.ec2.service.eaoservice.StaticQueueMemberService;
import com.jiangyifen.ec2.service.eaoservice.UserQueueService;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Component;
import com.vaadin.ui.PopupView.Content;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.ColumnGenerator;
import com.vaadin.ui.VerticalLayout;

/**
 * 单队列置忙显示界面
 * @author jrh
 */
@SuppressWarnings("serial")
public class SingleQueuePauseView extends VerticalLayout implements Content {

	// 坐席登陆就置忙
	private final String PASUE_EXTEN_AFTER_CSR_LOGIN = "pasue_exten_after_csr_login";
	
	private final String[] VISIBLE_PROPERTIES = new String[] {"queueName"};
	private final String[] COL_HEADERS = new String[] {"队列名"};
	
	private Table queueStatusTable;
	private AllQueuesPauseMenu allQueuesPauseMenu;
	
	private String exten;				// 当前用户在使用的分机
	private User loginUser;				// 当前登录用户
	private List<PauseReason> reasons;	// 置忙原因
	private Boolean ispauseExtenLogin;	// 用户登陆后是否默认置忙

	private BeanItemContainer<SipQueue> queueContainer;
	private PauseReasonService pauseReasonService;
	private QueuePauseService queuePauseService;
	private QueuePauseRecordService queuePauseRecordService;
	private UserQueueService userQueueService;
	private StaticQueueMemberService staticQueueMemberService;

	/**
	 * 单队列置忙显示界面构造函数
	 * @param allQueuesPauseMenu 全队列置忙组件，用于单队列置忙发生变化后，修改全队列置忙组件的显示信息
	 */
	public SingleQueuePauseView(AllQueuesPauseMenu allQueuesPauseMenu) {
		this.setWidth("250px");
		this.setSpacing(true);
		this.allQueuesPauseMenu = allQueuesPauseMenu;
		
		exten = SpringContextHolder.getExten();
		loginUser = SpringContextHolder.getLoginUser();
		pauseReasonService = SpringContextHolder.getBean("pauseReasonService");
		queuePauseService = SpringContextHolder.getBean("queuePauseService");
		queuePauseRecordService = SpringContextHolder.getBean("queuePauseRecordService");
		userQueueService = SpringContextHolder.getBean("userQueueService");
		staticQueueMemberService = SpringContextHolder.getBean("staticQueueMemberService");
		
		reasons = pauseReasonService.getAllByEnabled(loginUser.getDomain(), true);

		// jrh 2013-12-17 判断员工登陆系统后是否需要默认置忙       ---------------  开始
		ConcurrentHashMap<String, Boolean> domainConfigs = ShareData.domainToConfigs.get(loginUser.getDomain().getId());
		if(domainConfigs != null) {
			ispauseExtenLogin = domainConfigs.get(PASUE_EXTEN_AFTER_CSR_LOGIN);
			if(ispauseExtenLogin == null) {
				ispauseExtenLogin = false;
			}
		}	//	------------- 结束
		
		queueContainer = new BeanItemContainer<SipQueue>(SipQueue.class);
		queueContainer.sort(new String[]{"queueName"}, new boolean[]{true});
		
		// jrh 2013-12-17 判断坐席是否拥有登陆默认将分机置忙的权限，如果有，则置忙
		String defautStatusReson = GlobalVariable.DEFAULT_UNPAUSE_EXTEN_REASON;
		if(ispauseExtenLogin) {
			defautStatusReson = GlobalVariable.DEFAULT_LOGIN_EXTEN_PAUSE_REASON;
		} 			

		// 从动态队列中查询获取该分机所属的队列
		for(UserQueue userQueue : userQueueService.getAllByUsername(loginUser.getUsername())) {
			SipQueue sipQueue = new SipQueue(userQueue.getQueueName(), ispauseExtenLogin, defautStatusReson);
			queueContainer.addBean(sipQueue);
		}
		
		// 从静态队列中查询获取该分机所属的队列
		for(StaticQueueMember sqm : staticQueueMemberService.getAllBySipname(loginUser.getDomain(), exten)) {
			SipQueue sipQueue = new SipQueue(sqm.getQueueName(), ispauseExtenLogin, defautStatusReson);
			queueContainer.addBean(sipQueue);
		}
		
		queueStatusTable = new Table();
		queueStatusTable.setPageLength(0);
		queueStatusTable.setImmediate(true);
		queueStatusTable.setWidth("100%");
		queueStatusTable.setSelectable(false);
		queueStatusTable.setStyleName("striped");
		queueStatusTable.setRowHeaderMode(Table.ROW_HEADER_MODE_INDEX);
		this.addComponent(queueStatusTable);
		
		queueStatusTable.setContainerDataSource(queueContainer);
		queueStatusTable.setVisibleColumns(VISIBLE_PROPERTIES);
		queueStatusTable.setColumnHeaders(COL_HEADERS);
		
		queueStatusTable.addGeneratedColumn("状态", new StatusColumn());
		queueStatusTable.setColumnAlignment("状态", Table.ALIGN_CENTER);
		queueStatusTable.setColumnExpandRatio("状态", 0.7f);
	}
	
	private class StatusColumn implements ColumnGenerator {
		@SuppressWarnings("unchecked")
		@Override
		public Object generateCell(Table source, Object itemId, Object columnId) {
			BeanItem<SipQueue> queueItem = (BeanItem<SipQueue>) source.getItem(itemId);
			SipQueue sipQueue = queueItem.getBean();
			SingleQueuePauseMenu singleQueuePauseMenu = new SingleQueuePauseMenu(sipQueue, loginUser, exten, reasons);
			singleQueuePauseMenu.setQueuePauseService(queuePauseService);
			singleQueuePauseMenu.setQueuePauseRecordService(queuePauseRecordService);
			return singleQueuePauseMenu;
		}
	}
	
	/**
	 * 更新单队列置忙表格的显示内容
	 * @param queueStatus	队列置忙情况集合
	 */
	public void refreshTable(Map<String, Object> queueStatus) {
// TODO 现在是只能增加队列，不能减少队列
		// 第一步：更新单队列显示表格中的组件
		String queue = (String) queueStatus.get("queue");
		String reason = (String) queueStatus.get("reason");
		boolean paused = (Boolean) queueStatus.get("paused");
		boolean existed = false;
		for(SipQueue sipQueue : queueContainer.getItemIds()) {
			if(sipQueue.getQueueName().equals(queue)) {
				existed = true;
				sipQueue.setPaused(paused);
				sipQueue.setReason(reason);
			}
		}
		if(existed == false) {
			SipQueue sipQueue = new SipQueue(queue, paused, reason);
			queueContainer.addItem(sipQueue);
		}
		queueStatusTable.refreshRowCache();
		queueContainer.sort(new String[]{"queueName"}, new boolean[]{true});
		
		// 第二步：更新全队列置忙状态的显示情况
		int pausedCount = 0;
		for(SipQueue sipQueue : queueContainer.getItemIds()) {
			if(sipQueue.isPaused) pausedCount++;
		}
		
		if(pausedCount > 0 && pausedCount < queueContainer.size()) {	// 部分忙
			allQueuesPauseMenu.refreshMenubar("partPaused");
		} else if(pausedCount == queueContainer.size()) {	// 全忙
			allQueuesPauseMenu.refreshMenubar("allPaused");
		} else {	// 全闲
			allQueuesPauseMenu.refreshMenubar("allUnpaused");
		}
	}

	@Override
	public String getMinimizedValueAsHTML() {
		return "单队列置忙";
	}

	@Override
	public Component getPopupComponent() {
		return this;
	}
	
	/**
	 * 队列状态信息内部类，用于作Table 的数据源
	 */
	public class SipQueue {
		private String queueName;
		private String reason;
		private boolean isPaused;
		
		public SipQueue(String queueName, boolean isPaused, String reason) {
			this.queueName = queueName;
			this.isPaused = isPaused;
			this.reason = reason;
		}

		public String getQueueName() {
			return queueName;
		}

		public boolean isPaused() {
			return isPaused;
		}

		public void setPaused(boolean isPaused) {
			this.isPaused = isPaused;
		}

		public String getReason() {
			return reason;
		}

		public void setReason(String reason) {
			this.reason = reason;
		}

		public void setQueueName(String queueName) {
			this.queueName = queueName;
		}
	}

}
