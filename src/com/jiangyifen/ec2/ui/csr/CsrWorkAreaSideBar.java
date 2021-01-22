package com.jiangyifen.ec2.ui.csr;

import java.util.ArrayList;

import com.jiangyifen.ec2.globaldata.ResourceDataCsr;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.ui.Accordion;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class CsrWorkAreaSideBar extends VerticalLayout implements ClickListener {
	
	// 我的任务
	private static final String TASK_MANAGEMENT = "task_management";
	private static final String TASK_MANAGEMENT_OUTGOING_TASK = "task_management&outgoing_task";
	private static final String TASK_MANAGEMENT_QUESTIONNAIRE_TASK = "task_management&questionnaire_task";
	private static final String TASK_MANAGEMENT_SERVICE_RECORD = "task_management&service_record";
	private static final String TASK_MANAGEMENT_HISTORY_ORDER = "task_management&history_order";

	// 我的呼叫记录
	private static final String CALL_RECORD_MANAGEMENT = "call_record_management";
	private static final String CALL_RECORD_MANAGEMENT_CALL_RECORD = "call_record_management&call_record";
	
	// 客户记录
	private static final String MY_CUSTOMER_MANAGEMENT = "my_customer_management";
	private static final String MY_CUSTOMER_MANAGEMENT_PROPRIETARY_CUSTOMERS = "my_customer_management&proprietary_customers";
	private static final String MY_CUSTOMER_MANAGEMENT_MY_RESOURCES = "my_customer_management&my_resources";
	
	// 短信功能模块
	private static final String CSR_SMS_MANAGEMENT = "csr_sms_management";
	private static final String CSR_SMS_MANAGEMENT_MESSAGE_TEMPLATE_MANAGE = "csr_sms_management&message_template_manage";
	private static final String CSR_SMS_MANAGEMENT_MESSAGE_SEND_MANAGE = "csr_sms_management&message_send_manage";
	private static final String CSR_SMS_MANAGEMENT_MESSAGE_HISTORY_MANAGE = "csr_sms_management&message_history_manage";
	
	// 邮件功能模块
	private static final String CSR_EMAIL_MANAGEMENT = "csr_email_management";
	private static final String CSR_EMAIL_MANAGEMENT_SEND_EMAIL = "csr_email_management&csr_send_email";
	private static final String CSR_EMAIL_MANAGEMENT_HISTORY_EMAIL = "csr_email_management&csr_history_email";
	private static final String CSR_EMAIL_MANAGEMENT_CONFIG_EMAIL = "csr_email_management&csr_config_email";

	// 其他功能模块
	private static final String OTHER_FUNCS_MANAGEMENT = "other_funcs_management";
	private static final String OTHER_FUNCS_MANAGEMENT_CSR_MEET_ME = "other_funcs_management&csr_meet_me";
	private static final String OTHER_FUNCS_MANAGEMENT_CSR_VOICEMAIL_DETAIL = "other_funcs_management&csr_voicemail_detail";
	
	//发送消息
	private static final String CSR_MESSAGE_MANAGEMENT = "csr_message_management";
	private static final String CSR_MESSAGE_MANAGEMENT_SEND_MESSAGE = "csr_message_management&send_message";
	private static final String CSR_MESSAGE_MANAGEMENT_HISTORY_MESSAGE = "csr_message_management&history_message";
	
	private Accordion csrAccordion;
	private ArrayList<String> businessModel;			// 当前用户所拥有的功能权限的集合
	
	// =============== 任务管理模块  ============== // 
	private VerticalLayout taskManagement;				// 任务管理模块
	private NativeButton outgoingTask;					// 我的客服任务功能选项
	private NativeButton questionnarieTask;				// 我的问卷任务功能模块
	private NativeButton serviceRecord;			 		// 我的客服记录功能选项
	private NativeButton historyOrder;					// 我的历史订单功能选项
	
	// ============== 呼叫记录模块   =============== // 
	private VerticalLayout callRecordManagement;		// 呼叫记录管理模块
	private NativeButton recordManage;					// 呼叫记录管理选项
	
	// ========== 客户管理模块   ========== // 
	private VerticalLayout customerManagment;			// 客户管理
	private NativeButton proprietaryCustomers;			// 我的客户
	private NativeButton myResources;			 		// 我的资源
	
	// ========== 短信管理模块   ========== // 
	private VerticalLayout csrSmsManagment;			// 短息模块
	private NativeButton messageTemplate;				// 短信模板
	private NativeButton messageSend;		 			// 短信发送
	private NativeButton messageHistory;		 		// 历史短信
	
	// jinht
	// ========== 邮件管理模块   ========== // 
	private VerticalLayout csrEmailManagement;			// 邮箱模块
	private NativeButton emailSend;					// 邮件发送
	private NativeButton emailHistory;					// 历史邮件
	private NativeButton emailConfig;					// 邮箱配置
	
	//lxy 发送消息
	private VerticalLayout csrSendMassageManagemant;	// 发送消息管理
	private NativeButton sendMassage;					// 发送消息
	private NativeButton historyMassage;				// 历史消息
	
	// ========== 其他功能模块   ========== // 
	private VerticalLayout otherFunctionsManagment;		// 其他功能模块
	private NativeButton csrMeetMe;						// 话务员三方会议
	private NativeButton csrVoicemail;						// 语音留言管理
	
	// ========== 各模块的tabsheet显示区   ========== // 
	
	private CsrWorkAreaRightView csrWorkAreaRightView;	// 模块信息显示区
	
	public CsrWorkAreaSideBar(CsrWorkAreaRightView csrWorkAreaRightView) {
		this.setSizeFull();
		this.csrWorkAreaRightView = csrWorkAreaRightView;
		businessModel = SpringContextHolder.getBusinessModel();

		VerticalLayout accordingLayout=new VerticalLayout();
		accordingLayout.setSizeFull();
		accordingLayout.addStyleName("jrh");
		
		csrAccordion = new Accordion();
		csrAccordion.addStyleName("myaccordion");
		csrAccordion.setSizeFull();
		accordingLayout.addComponent(csrAccordion);
		
		this.addComponent(accordingLayout);
		
		// 创建主要组件
		createMainComponents();
	}

	@Override
	public void attach() {
//			if(businessModel.contains(TASK_MANAGEMENT_OUTGOING_TASK)) {
//				recordManage.click();
//				historyOrder.click();
//			}
	}

	/**
	 *  创建主要组件
	 */
	private void createMainComponents() {
		// 任务管理
		if(businessModel.contains(TASK_MANAGEMENT)) {
			taskManagement = new VerticalLayout();
			taskManagement.setSizeFull();
			taskManagement.setStyleName("sidebar-menu");
			taskManagement.addComponent(createTaskManagement());
			csrAccordion.addTab(taskManagement, "我的任务", ResourceDataCsr.task_14_ico);
		}

		// 呼叫记录管理
		if(businessModel.contains(CALL_RECORD_MANAGEMENT)) {
			callRecordManagement = new VerticalLayout();
			callRecordManagement.setSizeFull();
			callRecordManagement.setStyleName("sidebar-menu");
			callRecordManagement.addComponent(createCallRecordManagement());
			csrAccordion.addTab(callRecordManagement, "呼叫记录", ResourceDataCsr.call_record_14_ico);
		}
		
		// 我的客户管理
		if(businessModel.contains(MY_CUSTOMER_MANAGEMENT)) {
			customerManagment = new VerticalLayout();
			customerManagment.setSizeFull();
			customerManagment.setStyleName("sidebar-menu");
			customerManagment.addComponent(createMyCustomerManagement());
			csrAccordion.addTab(customerManagment, "客户管理", ResourceDataCsr.customer_management_14_ico);
		}
		
		// 短信管理
		if(businessModel.contains(CSR_SMS_MANAGEMENT)) {
			csrSmsManagment = new VerticalLayout();
			csrSmsManagment.setSizeFull();
			csrSmsManagment.setStyleName("sidebar-menu");
			csrSmsManagment.addComponent(createCsrSmsManagement()); 
			csrAccordion.addTab(csrSmsManagment, "短信管理", ResourceDataCsr.sms_manage_14_ico);
		}
		
		// 邮箱管理
		if(businessModel.contains(CSR_EMAIL_MANAGEMENT)) {
			csrEmailManagement = new VerticalLayout();
			csrEmailManagement.setSizeFull();
			csrEmailManagement.setStyleName("sidebar-menu");
			csrEmailManagement.addComponent(createCsrEmailManagement());
			csrAccordion.addTab(csrEmailManagement, "邮箱管理", ResourceDataCsr.sms_manage_14_ico);
		}
		
		// 消息管理
		if (businessModel.contains(CSR_MESSAGE_MANAGEMENT)) {
			csrSendMassageManagemant = new VerticalLayout();
			csrSendMassageManagemant.setSizeFull();
			csrSendMassageManagemant.setStyleName("sidebar-menu");
			csrSendMassageManagemant.addComponent(createCsrSendMassageManagemant());
			csrAccordion.addTab(csrSendMassageManagemant, "消息管理", ResourceDataCsr.csr_message_manage_14);
		}
		
		// 其他功能模块管理
		if(businessModel.contains(OTHER_FUNCS_MANAGEMENT)) {
			otherFunctionsManagment = new VerticalLayout();
			otherFunctionsManagment.setSizeFull();
			otherFunctionsManagment.setStyleName("sidebar-menu");
			otherFunctionsManagment.addComponent(createOtherFunctionsManagement());
			csrAccordion.addTab(otherFunctionsManagment, "其他功能管理", ResourceDataCsr.other_func_management_14_ico);
		}
	}

	/**
	 * 创建我的任务、客服记录模块管理组件
	 * @return
	 */
	private VerticalLayout createTaskManagement() {
		VerticalLayout vLayout = new VerticalLayout();
		
		if(businessModel.contains(TASK_MANAGEMENT_OUTGOING_TASK)) {
			outgoingTask = new NativeButton("我的任务--营销");
			outgoingTask.setImmediate(true);
			outgoingTask.addListener(this);
			vLayout.addComponent(outgoingTask);
			vLayout.setComponentAlignment(outgoingTask, Alignment.TOP_LEFT);
		}
		
		if(businessModel.contains(TASK_MANAGEMENT_QUESTIONNAIRE_TASK)) {
			questionnarieTask = new NativeButton("我的任务--问卷");
			questionnarieTask.setImmediate(true);
			questionnarieTask.addListener(this);
			vLayout.addComponent(questionnarieTask);
			vLayout.setComponentAlignment(questionnarieTask, Alignment.TOP_LEFT);
		}
		
		if(businessModel.contains(TASK_MANAGEMENT_SERVICE_RECORD)) {
			serviceRecord = new NativeButton("我的客服记录");
			serviceRecord.setImmediate(true);
			serviceRecord.addListener(this);
			vLayout.addComponent(serviceRecord);
		}
		
		if(businessModel.contains(TASK_MANAGEMENT_HISTORY_ORDER)) {
			historyOrder = new NativeButton("我的订单");
			historyOrder.setImmediate(true);
			historyOrder.addListener(this);
			vLayout.addComponent(historyOrder);
		}
		return vLayout;
	}
	
	/**
	 * 创建呼叫记录模块管理组件
	 * @return
	 */
	private VerticalLayout createCallRecordManagement() {
		VerticalLayout vLayout = new VerticalLayout();

		if(businessModel.contains(CALL_RECORD_MANAGEMENT_CALL_RECORD)) {
			recordManage = new NativeButton("呼叫记录管理");
			recordManage.setImmediate(true);
			recordManage.addListener(this);
			vLayout.addComponent(recordManage);
		}
		
		return vLayout;
	}
	
	/**
	 * 创建客户管理模块管理组件
	 * @return
	 */
	private VerticalLayout createMyCustomerManagement() {
		VerticalLayout vLayout = new VerticalLayout();
		
		if(businessModel.contains(MY_CUSTOMER_MANAGEMENT_PROPRIETARY_CUSTOMERS)) {
			proprietaryCustomers = new NativeButton("我的客户");
			proprietaryCustomers.setImmediate(true);
			proprietaryCustomers.addListener(this);
			vLayout.addComponent(proprietaryCustomers);
		}

		if(businessModel.contains(MY_CUSTOMER_MANAGEMENT_MY_RESOURCES)) {
			myResources = new NativeButton("我的资源");
			myResources.setImmediate(true);
			myResources.addListener(this);
			vLayout.addComponent(myResources);
		}

		return vLayout;
	}
	
	/**
	 * 创建短信管理模块
	 * @return
	 */
	private VerticalLayout createCsrSmsManagement() {
		VerticalLayout vLayout = new VerticalLayout();
		
		if(businessModel.contains(CSR_SMS_MANAGEMENT_MESSAGE_TEMPLATE_MANAGE)) {
			messageTemplate = new NativeButton("短信模板");
			messageTemplate.setImmediate(true);
			messageTemplate.addListener(this);
			vLayout.addComponent(messageTemplate);
		}
		
		if(businessModel.contains(CSR_SMS_MANAGEMENT_MESSAGE_SEND_MANAGE)) {
			messageSend = new NativeButton("发送短信");
			messageSend.setImmediate(true);
			messageSend.addListener(this);
			vLayout.addComponent(messageSend);
		}
		
		if(businessModel.contains(CSR_SMS_MANAGEMENT_MESSAGE_HISTORY_MANAGE)) {
			messageHistory = new NativeButton("历史短信");
			messageHistory.setImmediate(true);
			messageHistory.addListener(this);
			vLayout.addComponent(messageHistory);
		}
		
		return vLayout;
	}
	
	/**
	 * 创建邮箱管理模块
	 * @return
	 */
	private VerticalLayout createCsrEmailManagement() {
		VerticalLayout vLayout = new VerticalLayout();
		
		if(businessModel.contains(CSR_EMAIL_MANAGEMENT_SEND_EMAIL)) {
			emailSend = new NativeButton("邮件发送");
			emailSend.setImmediate(true);
			emailSend.addListener(this);
			vLayout.addComponent(emailSend);
		}
		
		if(businessModel.contains(CSR_EMAIL_MANAGEMENT_HISTORY_EMAIL)) {
			emailHistory = new NativeButton("历史邮件");
			emailHistory.setImmediate(true);
			emailHistory.addListener(this);
			vLayout.addComponent(emailHistory);
		}
		
		if(businessModel.contains(CSR_EMAIL_MANAGEMENT_CONFIG_EMAIL)) {
			emailConfig = new NativeButton("配置邮箱");
			emailConfig.setImmediate(true);
			emailConfig.addListener(this);
			Boolean isConfigEmail = ShareData.domainToConfigs.get(SpringContextHolder.getLoginUser().getDomain().getId()).get("setting_global_email");
			if(isConfigEmail == null || !isConfigEmail) {
				vLayout.addComponent(emailConfig);
			}
		}
		
		return vLayout;
	}

	/**
	 * 内部消息
	 * @return
	 */
	private VerticalLayout createCsrSendMassageManagemant() {
		VerticalLayout vLayout = new VerticalLayout();
		if(businessModel.contains(CSR_MESSAGE_MANAGEMENT_SEND_MESSAGE)) {
			sendMassage = new NativeButton("消息发送");
			sendMassage.setImmediate(true);
			sendMassage.addListener(this);
			vLayout.addComponent(sendMassage);
		}
		if(businessModel.contains(CSR_MESSAGE_MANAGEMENT_HISTORY_MESSAGE)) {
			historyMassage = new NativeButton("历史消息");
			historyMassage.setImmediate(true);
			historyMassage.addListener(this);
			vLayout.addComponent(historyMassage);
		}
		return vLayout;
	}
	
	/**
	 * 创建其他功能模块管理组件
	 * @return
	 */
	private VerticalLayout createOtherFunctionsManagement() {
		VerticalLayout vLayout = new VerticalLayout();
		if(businessModel.contains(OTHER_FUNCS_MANAGEMENT_CSR_MEET_ME)) {
			csrMeetMe = new NativeButton("我的会议室");
			csrMeetMe.setImmediate(true);
			csrMeetMe.addListener(this);
			vLayout.addComponent(csrMeetMe);
		}
		// 语音留言管理
		if(businessModel.contains(OTHER_FUNCS_MANAGEMENT_CSR_VOICEMAIL_DETAIL)) {
			csrVoicemail = new NativeButton("语音留言管理");
			csrVoicemail.setImmediate(true);
			csrVoicemail.addListener(this);
			vLayout.addComponent(csrVoicemail);
		}
		
		return vLayout;
	}
	
	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		if(source == outgoingTask) {
			csrWorkAreaRightView.showOutgoingTaskManage();
		} else if(source == questionnarieTask) {
			csrWorkAreaRightView.showQuestionnaireTaskManage();
		} else if(source == serviceRecord) {
			csrWorkAreaRightView.showServiceRecordManage();
		} else if(source == historyOrder) {
			csrWorkAreaRightView.showHistoryOrderManage();
		} else if(source == recordManage) {
			csrWorkAreaRightView.showCallRecordManage();
		} else if(source == proprietaryCustomers) {
			csrWorkAreaRightView.showProprietaryCustomersManage();
		} else if(source == myResources) {
			csrWorkAreaRightView.showMyResourcesManage();
		} else if(source == messageTemplate) {
			csrWorkAreaRightView.showMessageTemplateView();
		} else if(source == messageSend) {
			csrWorkAreaRightView.showSendMessageView();
		} else if(source == messageHistory) {
			csrWorkAreaRightView.showHistoryMessageView();
		} else if(source == emailSend) {
			csrWorkAreaRightView.showEmailSendView();
		} else if(source == emailHistory) {
			csrWorkAreaRightView.showEmailHistoryView();
		} else if(source == emailConfig) {
			csrWorkAreaRightView.showEmailConfigView();
		} else if(source == csrMeetMe) {
			csrWorkAreaRightView.showCsrMeetMeView();
		} else if(source == csrVoicemail) {
			csrWorkAreaRightView.showCsrVoiceManagement();
		} else if(source == sendMassage){
			csrWorkAreaRightView.showNoticeSendView();
		} else if(source == historyMassage){
			csrWorkAreaRightView.showCsrNoticeHistory();
		}
	}

	public NativeButton getCsrMeetMe() {
		return csrMeetMe;
	}
	
}
