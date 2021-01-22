package com.jiangyifen.ec2.ui.csr.statusbar;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.jiangyifen.ec2.entity.PauseReason;
import com.jiangyifen.ec2.entity.QueuePauseRecord;
import com.jiangyifen.ec2.entity.StaticQueueMember;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.entity.UserQueue;
import com.jiangyifen.ec2.globaldata.GlobalVariable;
import com.jiangyifen.ec2.globaldata.ResourceDataCsr;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.service.csr.ami.QueuePauseService;
import com.jiangyifen.ec2.service.eaoservice.PauseReasonService;
import com.jiangyifen.ec2.service.eaoservice.QueuePauseRecordService;
import com.jiangyifen.ec2.service.eaoservice.StaticQueueMemberService;
import com.jiangyifen.ec2.service.eaoservice.UserQueueService;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.terminal.Resource;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.VerticalLayout;

/**
 * 全对了置忙组件
 * @author jrh
 */
@SuppressWarnings("serial")
public class AllQueuesPauseMenu extends VerticalLayout {

	// 坐席登陆就置忙
	private final String PASUE_EXTEN_AFTER_CSR_LOGIN = "pasue_exten_after_csr_login";
	
	private MenuBar menubar;
	private MenuItem pauseAll;
	
	private String exten;				// 当前用户在使用的分机
	private User loginUser;				// 当前登录用户
	private Boolean ispauseExtenLogin;		// 用户登陆后是否默认置忙
	private List<PauseReason> reasons;	// 置忙原因
	private ArrayList<String> queueNameList;	 // 该用户在使用的分机所对应的所有队列
	
	private PauseReasonService pauseReasonService;
	private QueuePauseService queuePauseService;
	private QueuePauseRecordService queuePauseRecordService;
	private UserQueueService userQueueService;
	private StaticQueueMemberService staticQueueMemberService;
	
	public AllQueuesPauseMenu() {
		queueNameList = new ArrayList<String>();
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
		
		menubar = new MenuBar();
		menubar.addStyleName("nobackground");
		this.addComponent(menubar);

		// 置忙命令
		Command pauseCommand = new Command() {
			public void menuSelected(MenuItem selectedItem) {
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
				
				// 如果所有队列都闲，则将所有队列直接置忙
				Date currentDate = new Date();
				for(String queueName : queueNameList) {
					QueuePauseRecord queuePauseRecord = queuePauseRecordService.getLastPauseRecord(loginUser.getUsername(), exten, queueName);
					if(queuePauseRecord != null) {
						// 为了确保数据的完整性，先检查是否已经存在置忙的记录，如果有，则先置闲，再置忙（这一步只是为了数据库中的记录完整）
						queuePauseRecord.setUnpauseDate(currentDate);
						queuePauseRecordService.update(queuePauseRecord);
					}
					queuePauseService.pause(queueName, exten, true, selectedItem.getText());
					createNewPauseRecord(selectedItem.getText(), currentDate, queueName);
				}
				pauseAll.setText("全忙");
				pauseAll.setIcon(ResourceDataCsr.red_14_sidebar_ico);
			}

		};

		// 置闲命令
		Command unpauseCommand = new Command() {
			public void menuSelected(MenuItem selectedItem) {
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
				
				// 只有在当前状态不为全闲的时候才增加置闲记录，而且只对状态为忙的队列进行操作
				if(!"全闲".equals(pauseAll.getText())) {
					Date currentDate = new Date();
					for(String queueName : queueNameList) {
						QueuePauseRecord queuePauseRecord = queuePauseRecordService.getLastPauseRecord(loginUser.getUsername(), exten, queueName);
						if(queuePauseRecord != null) {
							queuePauseRecord.setUnpauseDate(currentDate);
							queuePauseRecordService.update(queuePauseRecord);
						}
						queuePauseService.pause(queueName, exten, false, GlobalVariable.DEFAULT_UNPAUSE_EXTEN_REASON);
						createNewPauseRecord(selectedItem.getText(), currentDate, queueName);
					}
					pauseAll.setText("全闲");
					pauseAll.setIcon(ResourceDataCsr.green_14_sidebar_ico);
				}
			}
		};

		// jrh 2013-12-17 判断坐席是否拥有登陆默认将分机置忙的权限，如果有，则置忙
		String defautStatusReson = "全闲";
		Resource icon_resource = ResourceDataCsr.green_14_sidebar_ico;
		if(ispauseExtenLogin) {
			defautStatusReson = "全忙";
			icon_resource = ResourceDataCsr.red_14_sidebar_ico;
		} 			

		pauseAll = menubar.addItem(defautStatusReson, icon_resource, null);
		pauseAll.setStyleName("mypadding");
		pauseAll.addItem(GlobalVariable.DEFAULT_UNPAUSE_EXTEN_REASON, ResourceDataCsr.green_14_sidebar_ico, unpauseCommand);
		pauseAll.addSeparator();
		for(PauseReason reason : reasons) {
			pauseAll.addItem(reason.toString(), ResourceDataCsr.red_14_sidebar_ico, pauseCommand);
		}
	}

	private void createNewPauseRecord(String reason, Date currentDate, String queueName) {
		QueuePauseRecord newPauseRecord = new QueuePauseRecord();
		newPauseRecord.setUsername(loginUser.getUsername());
		newPauseRecord.setSipname("SIP/" + exten);
		newPauseRecord.setPauseDate(currentDate);
		newPauseRecord.setReason(reason);
		newPauseRecord.setQueue(queueName);
		newPauseRecord.setDeptId(loginUser.getDepartment().getId());
		newPauseRecord.setDeptName(loginUser.getDepartment().getName());
		newPauseRecord.setDomainId(loginUser.getDomain().getId());
		queuePauseRecordService.save(newPauseRecord);
	}
	
	/**
	 * 根据队列的状态修改全队列菜单的显示情况 
	 *  全队列状况有三种值 partPaused 部分忙； allPaused 全忙；allUnpaused 全闲
	 * @param queueStatus 队列的状态 
	 */
	public void refreshMenubar(String queueStatus) {
		if("partPaused".equals(queueStatus)) {
			pauseAll.setText("");
			pauseAll.setIcon(ResourceDataCsr.yellow_14_sidebar_ico);
		} else if("allPaused".equals(queueStatus)) {
			pauseAll.setText("全忙");
			pauseAll.setIcon(ResourceDataCsr.red_14_sidebar_ico);
		} else if("allUnpaused".equals(queueStatus)) {
			pauseAll.setText("全闲");
			pauseAll.setIcon(ResourceDataCsr.green_14_sidebar_ico);
		}
	}

}
