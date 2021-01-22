package com.jiangyifen.ec2.ui.csr.statusbar;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.jiangyifen.ec2.entity.PauseReason;
import com.jiangyifen.ec2.entity.QueuePauseRecord;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.globaldata.GlobalVariable;
import com.jiangyifen.ec2.globaldata.ResourceDataCsr;
import com.jiangyifen.ec2.service.csr.ami.QueuePauseService;
import com.jiangyifen.ec2.service.eaoservice.QueuePauseRecordService;
import com.jiangyifen.ec2.ui.csr.statusbar.SingleQueuePauseView.SipQueue;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.VerticalLayout;

/**
 * 单队列置忙菜单组件
 * @author jrh
 */
@SuppressWarnings("serial")
public class SingleQueuePauseMenu extends VerticalLayout {
	
	private MenuBar menubar;
	private MenuItem pauseMember;
	
	private String queueName;
	private String reason;
	private boolean isPaused;
	private QueuePauseService queuePauseService;
	private QueuePauseRecordService queuePauseRecordService;

	private ArrayList<String> pauseReasonNames;	// 所有置忙原因的名称
	
	
	/**
	 * 单队列置忙菜单构造函数
	 * @param queueName		队列名称
	 * @param isPaused		是否置忙
	 * @param loginUser		当前用户
	 * @param exten			使用分机
	 * @param reasons		置忙原因
	 */
	public SingleQueuePauseMenu(SipQueue sipQueue, final User loginUser, 
					final String exten, List<PauseReason> pauseReasons) {
		this.queueName = sipQueue.getQueueName();
		this.reason = sipQueue.getReason();
		this.isPaused = sipQueue.isPaused();

		pauseReasonNames = new ArrayList<String>();
		for(PauseReason pauseReason : pauseReasons) {
			pauseReasonNames.add(pauseReason.getReason());
		}
		pauseReasonNames.add(GlobalVariable.MANAGER_FORCE_EXTEN_PAUSE_REASON);	// jrh 外加 1种特殊的置忙原因

		menubar = new MenuBar();
		menubar.addStyleName("nobackground");
		this.addComponent(menubar);
		
		Command pauseCommand = new Command() {	// 置忙命令
			public void menuSelected(MenuItem selectedItem) {
				// 为了确保数据的完整性，先检查是否已经存在置忙的记录，如果有，则先置闲，再置忙（这一步只是为了数据库中的记录完整）
				QueuePauseRecord queuePauseRecord = queuePauseRecordService.getLastPauseRecord(loginUser.getUsername(), exten, queueName);
				if(queuePauseRecord != null){
					queuePauseRecord.setUnpauseDate(new Date());
					queuePauseRecordService.update(queuePauseRecord);
				}
				createNewPauseRecord(loginUser, exten, selectedItem.getText());
				queuePauseService.pause(queueName, exten, true, selectedItem.getText());
			}
		};
		
		Command unpauseCommand = new Command() {	// 置闲命令
			public void menuSelected(MenuItem selectedItem) {
				if(pauseReasonNames.contains(pauseMember.getText())) {	// 只有在当前状态为忙的时候才增加置闲记录
					QueuePauseRecord queuePauseRecord = queuePauseRecordService.getLastPauseRecord(loginUser.getUsername(), exten, queueName);
					if(queuePauseRecord != null) {
						queuePauseRecord.setUnpauseDate(new Date());
						queuePauseRecordService.update(queuePauseRecord);
					}
					createNewPauseRecord(loginUser, exten, selectedItem.getText());
					queuePauseService.pause(queueName, exten, false, selectedItem.getText());
				}
			}
		};
		
		if(isPaused) {
			pauseMember = menubar.addItem(reason, ResourceDataCsr.red_14_sidebar_ico, null);
		} else {
			pauseMember = menubar.addItem(reason, ResourceDataCsr.green_14_sidebar_ico, null);
		}
		
		pauseMember.setStyleName("mypadding");
		pauseMember.addItem(GlobalVariable.DEFAULT_UNPAUSE_EXTEN_REASON, ResourceDataCsr.green_14_sidebar_ico, unpauseCommand);
		pauseMember.addSeparator();
		for(PauseReason reason : pauseReasons) {
			pauseMember.addItem(reason.getReason(), ResourceDataCsr.red_14_sidebar_ico, pauseCommand);
		}
	}

	private void createNewPauseRecord(User loginUser, String exten, String reason) {
		QueuePauseRecord newPauseRecord = new QueuePauseRecord();
		newPauseRecord.setUsername(loginUser.getUsername());
		newPauseRecord.setSipname("SIP/" + exten);
		newPauseRecord.setPauseDate(new Date());
		newPauseRecord.setReason(reason);
		newPauseRecord.setQueue(queueName);
		newPauseRecord.setDeptId(loginUser.getDepartment().getId());
		newPauseRecord.setDeptName(loginUser.getDepartment().getName());
		newPauseRecord.setDomainId(loginUser.getDomain().getId());
		queuePauseRecordService.save(newPauseRecord);
	}
	
	public void setQueuePauseService(QueuePauseService queuePauseService) {
		this.queuePauseService = queuePauseService;
	}

	public void setQueuePauseRecordService(QueuePauseRecordService queuePauseRecordService) {
		this.queuePauseRecordService = queuePauseRecordService;
	}

}
