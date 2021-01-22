package com.jiangyifen.ec2.ui.csr;

import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.globaldata.ResourceDataCsr;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.ui.csr.workarea.callrecord.CdrTabView;
import com.jiangyifen.ec2.ui.csr.workarea.email.EmailConfigView;
import com.jiangyifen.ec2.ui.csr.workarea.email.EmailHistoryView;
import com.jiangyifen.ec2.ui.csr.workarea.email.EmailSendView;
import com.jiangyifen.ec2.ui.csr.workarea.marketingtask.MyMarketingTaskTabView;
import com.jiangyifen.ec2.ui.csr.workarea.messagesend.CsrNoticeHistory;
import com.jiangyifen.ec2.ui.csr.workarea.messagesend.CsrNoticeSend;
import com.jiangyifen.ec2.ui.csr.workarea.mycustomer.ProprietaryCustomersTabView;
import com.jiangyifen.ec2.ui.csr.workarea.myresource.MyResourcesTabView;
import com.jiangyifen.ec2.ui.csr.workarea.order.MyHistoryOrderTabView;
import com.jiangyifen.ec2.ui.csr.workarea.othermodules.CsrMeetMeView;
import com.jiangyifen.ec2.ui.csr.workarea.questionnairetask.QuestionnaireTaskTabView;
import com.jiangyifen.ec2.ui.csr.workarea.servicerecord.MyServiceRecordAllTabView;
import com.jiangyifen.ec2.ui.csr.workarea.sms.CsrHistoryMessageView;
import com.jiangyifen.ec2.ui.csr.workarea.sms.CsrMessageTemplateView;
import com.jiangyifen.ec2.ui.csr.workarea.sms.SendMutiMessageView;
import com.jiangyifen.ec2.ui.csr.workarea.voicemail.CsrVoiceMailManagement;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.ui.Component;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.CloseHandler;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;
import com.vaadin.ui.TabSheet.SelectedTabChangeListener;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class CsrWorkAreaRightView extends VerticalLayout implements CloseHandler, SelectedTabChangeListener {

	private MyMarketingTaskTabView myTaskTabView;						// "我的营销任务" Tab页管理模块
	
	private QuestionnaireTaskTabView questionnaireTaskTabView;			// "我的问卷任务" Tab页管理模块

	private MyServiceRecordAllTabView myServiceRecordAllTabView;		// "我的服务记录" Tab 页管理模块
	
	private MyHistoryOrderTabView myHistoryOrderTabView;				// "我的历史订单记录" Tab 页管理模块

	private CdrTabView cdrTabView;										// "呼叫记录" Tab页管理模块
	
	private ProprietaryCustomersTabView proprietaryCustomersTabView; 	// 专有客户模块

	private MyResourcesTabView myResourcesTabView; 					// 我的资源 模块
	
	private CsrMessageTemplateView messageTemplateView;				// 短信模板模块
	
	private SendMutiMessageView sendMessageView;						// 发送短信模块
	
	private CsrHistoryMessageView historyMessageView;					// 历史短信模块
	
	private EmailSendView emailSendView;								// 邮件发送模块
	private EmailHistoryView emailHistoryView;							// 历史邮件发送模块
	private EmailConfigView emailConfigView;							// 邮件配置模块
	
	private CsrMeetMeView csrMeetMeView; 								// 我的会议室 模块
	
	private CsrVoiceMailManagement csrVoiceMailManagement;				// 语音留言查看 模块
	
//	private Tab sendMassageTab;											//消息发送tab页
	private CsrNoticeSend csrNoticeSendView;							//消息发送
	
//	private Tab csrHistoryMessageTab;									//消息查询tab页
	private CsrNoticeHistory csrNoticeHistory;							//消息查询
	
	private Tab selectedTab;											// 当前选中的 tab页
	private TabSheet tabSheet;
	private User loginUser;
	
	public CsrWorkAreaRightView() {
		this.setSizeFull();
		loginUser = SpringContextHolder.getLoginUser();
		
		// 将组建存于内存
		ShareData.csrToWorkAreaRightView.put(loginUser.getId(), this);
		
		tabSheet = new TabSheet();
		tabSheet.setCloseHandler(this);
		tabSheet.addListener(this);
		tabSheet.setImmediate(true);
		tabSheet.setSizeFull();
		tabSheet.setStyleName("borderless");
		this.addComponent(tabSheet);
	}
	
	/** 创建并显示我的任务显示组件 */
	public void showOutgoingTaskManage() {
		if(myTaskTabView == null) {
			myTaskTabView = new MyMarketingTaskTabView();
		}
		selectedTab = tabSheet.addTab(myTaskTabView, "我的任务--营销", ResourceDataCsr.task_16_ico);
		selectedTab.setClosable(true);
		tabSheet.setSelectedTab(selectedTab);
	}
	
	/** 创建并显示问卷任务显示组件 */
	public void showQuestionnaireTaskManage() {
		if(questionnaireTaskTabView == null) {
			questionnaireTaskTabView = new QuestionnaireTaskTabView();
		}
		selectedTab = tabSheet.addTab(questionnaireTaskTabView, "我的任务--问卷", ResourceDataCsr.questionnarie_investigate_16_ico);
		selectedTab.setClosable(true);
		tabSheet.setSelectedTab(selectedTab);
	}

	/** 创建并显示我的服务记录显示组件 */
	public void showServiceRecordManage() {
		if(myServiceRecordAllTabView == null) {
			myServiceRecordAllTabView = new MyServiceRecordAllTabView();
		}
		selectedTab = tabSheet.addTab(myServiceRecordAllTabView, "我的客服记录", ResourceDataCsr.service_record_16_ico);
		selectedTab.setClosable(true);
		tabSheet.setSelectedTab(selectedTab);
	}
	
	/** 创建并显示我的历史订单记录显示组件 */
	public void showHistoryOrderManage() {
		if(myHistoryOrderTabView == null) {
			myHistoryOrderTabView = new MyHistoryOrderTabView();
		}
		selectedTab = tabSheet.addTab(myHistoryOrderTabView, "我的订单", ResourceDataCsr.cart_16_ico);
		selectedTab.setClosable(true);
		tabSheet.setSelectedTab(selectedTab);
	}
	
	/** 创建并显示我的呼叫记录显示组件 */
	public void showCallRecordManage() {
		if(cdrTabView == null) {
			cdrTabView = new CdrTabView();
		}
		selectedTab = tabSheet.addTab(cdrTabView, "呼叫记录", ResourceDataCsr.call_record_16_ico);
		selectedTab.setClosable(true);
		tabSheet.setSelectedTab(selectedTab);
	}
	
	/** 创建并显示我的专有客户显示组件 */
	public void showProprietaryCustomersManage() {
		if(proprietaryCustomersTabView == null) {
			proprietaryCustomersTabView = new ProprietaryCustomersTabView();
		}
		selectedTab = tabSheet.addTab(proprietaryCustomersTabView, "我的客户", ResourceDataCsr.my_customer_16_ico);
		selectedTab.setClosable(true);
		tabSheet.setSelectedTab(selectedTab);
	}

	/**创建并显示我的资源显示组件*/
	public void showMyResourcesManage() {
		if(myResourcesTabView == null) {
			myResourcesTabView = new MyResourcesTabView();
		}
		selectedTab = tabSheet.addTab(myResourcesTabView, "我的资源", ResourceDataCsr.my_resource_16_ico);
		selectedTab.setClosable(true);
		tabSheet.setSelectedTab(selectedTab);
	}
	
	/**创建并显示短信模板显示组件*/
	public void showMessageTemplateView() {
		if(messageTemplateView == null) {
			messageTemplateView = new CsrMessageTemplateView();
		}
		selectedTab = tabSheet.addTab(messageTemplateView, "短信模板", ResourceDataCsr.phone_message_template_16_ico);
		selectedTab.setClosable(true);
		tabSheet.setSelectedTab(selectedTab);
	}
	
	/**创建并显示发送短信显示组件*/
	public void showSendMessageView() {
		if(sendMessageView == null) {
			sendMessageView = new SendMutiMessageView();
		}
		selectedTab = tabSheet.addTab(sendMessageView, "发送短信", ResourceDataCsr.phone_message_send_16_ico);
		selectedTab.setClosable(true);
		tabSheet.setSelectedTab(selectedTab);
	}
	
	/**创建并显示我的历史短信显示组件*/
	public void showHistoryMessageView() {
		if(historyMessageView == null) {
			historyMessageView = new CsrHistoryMessageView();
		}
		selectedTab = tabSheet.addTab(historyMessageView, "历史短信", ResourceDataCsr.history_message_16_ico);
		selectedTab.setClosable(true);
		tabSheet.setSelectedTab(selectedTab);
	}
	
	/** 创建并显示发送邮件显示组件 */
	public void showEmailSendView() {
		if(emailSendView == null) {
			emailSendView = new EmailSendView();
		}
		selectedTab = tabSheet.addTab(emailSendView, "发送邮件", ResourceDataCsr.phone_message_send_16_ico);
		selectedTab.setClosable(true);
		tabSheet.setSelectedTab(selectedTab);
	}
	
	/** 创建并显示历史邮件显示组件 */
	public void showEmailHistoryView() {
		if(emailHistoryView == null) {
			emailHistoryView = new EmailHistoryView();
		}
		selectedTab = tabSheet.addTab(emailHistoryView, "历史邮件", ResourceDataCsr.history_message_16_ico);
		selectedTab.setClosable(true);
		tabSheet.setSelectedTab(selectedTab);
	}
	
	/** 创建并显示邮箱配置显示组件 */
	public void showEmailConfigView() {
		if(emailConfigView == null) {
			emailConfigView = new EmailConfigView();
		}
		selectedTab = tabSheet.addTab(emailConfigView, "配置邮箱", ResourceDataCsr.phone_message_template_16_ico);
		selectedTab.setClosable(true);
		tabSheet.setSelectedTab(emailConfigView);
	}
	
	/**创建并显示我的会议室显示组件*/
	public void showCsrMeetMeView() {
		if(csrMeetMeView == null) {
			csrMeetMeView = new CsrMeetMeView();
		}
		selectedTab = tabSheet.addTab(csrMeetMeView, "我的会议室", ResourceDataCsr.csr_meeting_room_16_icon);
		selectedTab.setClosable(true);
		tabSheet.setSelectedTab(selectedTab);
	}
	
	/**创建并显示语音留言组件*/
	public void showCsrVoiceManagement() {
		if(csrVoiceMailManagement == null) {
			csrVoiceMailManagement = new CsrVoiceMailManagement();
		}
		selectedTab = tabSheet.addTab(csrVoiceMailManagement, "语音留言管理", ResourceDataCsr.file_folder_main_16_png);
		selectedTab.setClosable(true);
		tabSheet.setSelectedTab(selectedTab);
	}
	
	public void showNoticeSendView() {
		if(csrNoticeSendView == null) {
			csrNoticeSendView = new CsrNoticeSend();
		}
		selectedTab = tabSheet.addTab(csrNoticeSendView, "消息发送", ResourceDataCsr.csr_message_send);
		selectedTab.setClosable(true);
		tabSheet.setSelectedTab(selectedTab);
	}
	
	public void showCsrNoticeHistory() {
		if(csrNoticeHistory == null) {
			csrNoticeHistory = new CsrNoticeHistory();
		}
		selectedTab = tabSheet.addTab(csrNoticeHistory, "历史消息", ResourceDataCsr.csr_history_message);
		selectedTab.setClosable(true);
		tabSheet.setSelectedTab(selectedTab);
	}
	
	@Override
	public void onTabClose(TabSheet tabsheet, Component tabContent) {
		tabsheet.removeComponent(tabContent);
		if(tabContent == myTaskTabView) {
			myTaskTabView = null;
		} else if(tabContent == questionnaireTaskTabView) {
			questionnaireTaskTabView = null;
		} else if(tabContent == cdrTabView) {
			cdrTabView = null;
		} else if(tabContent == myServiceRecordAllTabView) {
			myServiceRecordAllTabView = null;
		} else if(tabContent == myHistoryOrderTabView) {
			myHistoryOrderTabView = null;
		} else if(tabContent == proprietaryCustomersTabView) {
			proprietaryCustomersTabView = null;
		} else if(tabContent == myResourcesTabView) {
			myResourcesTabView = null;
		} else if(tabContent == messageTemplateView) {
			messageTemplateView = null;
		} else if(tabContent == sendMessageView) {
			sendMessageView = null;
		} else if(tabContent == historyMessageView) {
			historyMessageView = null;
		} else if(tabContent == csrMeetMeView) {
			// 停止监控线程
			stopSupperviceThread(csrMeetMeView);
			csrMeetMeView = null;
		} else if(tabContent == csrNoticeSendView){
			csrNoticeSendView = null;
 		} else if(tabContent == csrNoticeHistory){
 			csrNoticeHistory = null;
 		}
	}

	/**
	 * 处理Tab 页切换事件
	 *  当csr 当前所操作的Tab 页发生变化时，则改变ShareData 中csr 与操作的Tab页的对应关系
	 */
	@Override
	public void selectedTabChange(SelectedTabChangeEvent event) {
		Component selected = tabSheet.getSelectedTab();

		// 停止监控线程
		stopSupperviceThread(selected);

		// 修改当前选中的tab页
		if(selected != myServiceRecordAllTabView) {
			ShareData.csrToCurrentTab.put(loginUser.getId(), (VerticalLayout) selected);
		}
		
		if(selected == myTaskTabView) {
			myTaskTabView.refreshTable(false);
		} else if(selected == questionnaireTaskTabView) {
			questionnaireTaskTabView.refreshTable(false);
		} else if(selected == myServiceRecordAllTabView) {
			myServiceRecordAllTabView.refreshTable(false);
		} else if(selected == myHistoryOrderTabView) {
			myHistoryOrderTabView.refreshTable(false);
		} else if(selected == cdrTabView) {
			cdrTabView.refreshTable(false);
		} else if(selected == proprietaryCustomersTabView) {
			proprietaryCustomersTabView.refreshTable(false);
		} else if(selected == myResourcesTabView) {
			myResourcesTabView.refreshTable(false);
		} else if(selected == messageTemplateView) {
			messageTemplateView.refreshTable(false);
		} else if(selected == sendMessageView) {
			sendMessageView.refreshTemplates();
		} else if(selected == historyMessageView) {
			historyMessageView.refreshTable(false);
		} else if(selected == csrMeetMeView) {
			csrMeetMeView.startSuperviseThread();
		}

	}

	/**
	 * 停止监控线程
	 * @param selected
	 */
	public void stopSupperviceThread(Component selected) {
		if (csrMeetMeView != null) {
			csrMeetMeView.stopSuperviseThread();
		}
	}

}
