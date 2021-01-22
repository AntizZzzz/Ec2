package com.jiangyifen.ec2.ui.mgr.accordion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.globaldata.ResourceDataMgr;
import com.jiangyifen.ec2.ui.mgr.tabsheet.MgrTabSheet;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.ui.Accordion;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.VerticalLayout;

/**
 * 左侧Accordion视图
 * 
 * @author chb
 * 
 */
@SuppressWarnings("serial")
public class MgrAccordion extends Accordion implements Button.ClickListener {
	// 日志工具
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	// ============== 权限控制 ================//
	// 数据管理
	public static final String DATA_MANAGEMENT = "data_management";
	public static final String DATA_MANAGEMENT_RESOURCE_IMPORT = "data_management&resource_import";
	public static final String DATA_MANAGEMENT_GLOBAL_SEARCH = "data_management&global_search";

	public static final String DATA_MANAGEMENT_SERVICE_RECORD = "data_management&service_record";
	public static final String DATA_MANAGEMENT_DOWNLOAD_SERVICE_RECORD = "data_management&download_service_record";
	public static final String DATA_MANAGEMENT_DOWNLOAD_SOUND_SERVICE_RECORD = "data_management&download_sound_service_record";

	public static final String DATA_MANAGEMENT_CALL_RECORD = "data_management&call_record";
	public static final String DATA_MANAGEMENT_DOWNLOAD_CALL_RECORD = "data_management&download_call_record";
	public static final String DATA_MANAGEMENT_DOWNLOAD_URL_CALL_RECORD = "data_management&download_url_call_record";
	public static final String DATA_MANAGEMENT_DOWNLOAD_SOUND_CALL_RECORD = "data_management&download_sound_call_record";
	public static final String DATA_MANAGEMENT_RESOURCE_MANAGE = "data_management&resource_manage";

	public static final String DATA_MANAGEMENT_MEETTING_DETAIL_RECORD = "data_management&meetting_detail_record";
	public static final String DATA_MANAGEMENT_DOWNLOAD_MEETTING_DETAIL_RECORD = "data_management&download_meetting_detail_record";
	public static final String DATA_MANAGEMENT_DOWNLOAD_SOUND_MEETTING_DETAIL_RECORD = "data_management&download_sound_meetting_detail_record";

	public static final String DATA_MANAGEMENT_MISS_CALL_LOG = "data_management&miss_call_log";
	public static final String DATA_MANAGEMENT_DOWNLOAD_MISS_CALL_LOG = "data_management&download_miss_call_log";
	
	public static final String DATA_MANAGEMENT_MANAGER_VOICEMAIL_DETAILS = "data_management&manager_voicemail_details";	// 语音留言信息查看

	// 项目控制
	public static final String PROJECT_MANAGEMENT = "project_management";
	public static final String PROJECT_MANAGEMENT_PROJECT_CONTROL = "project_management&project_control";

	// 自动外呼管理
	public static final String AUTO_DIAL_MANAGEMENT = "auto_dial_management";
	public static final String AUTO_DIAL_MANAGEMENT_AUTO_DIAL = "auto_dial_management&auto_dial";
	public static final String AUTO_DIAL_MANAGEMENT_VOICE_PACKET = "auto_dial_management&voice_packet";
	public static final String AUTO_DIAL_MANAGEMENT_VOICE_UPLOAD = "auto_dial_management&voice_upload";
	public static final String AUTO_DIAL_MANAGEMENT_RECYCLE_RESOURCE = "auto_dial_management&recycle_resource";

	// 报表管理
	private static final String REPORT_MANAGEMENT = "report_management";
	private static final String REPORT_MANAGEMENT_CALL_STATISTIC_OVERVIEW = "report_management&call_statistic_overview";
	private static final String REPORT_MANAGEMENT_PROJECT_FINISH_STATUS = "report_management&project_finish_status";
	private static final String REPORT_MANAGEMENT_SERVICE_RECORD_STATUS = "report_management&service_record_status";
	public static final String REPORT_MANAGEMENT_DOWNLOAD_SERVICE_RECORD_STATUS = "report_management&download_service_record_status";
	private static final String REPORT_MANAGEMENT_TELEPHONE_TRAFFIC_CHECK = "report_management&telephone_traffic_check";
	public static final String REPORT_MANAGEMENT_DOWNLOAD_TELEPHONE_TRAFFIC_CHECK = "report_management&download_telephone_traffic_check";
	private static final String REPORT_MANAGEMENT_TELEPHONE_TRAFFIC_CHECK_BY_CALL_TIME = "report_management&telephone_traffic_check_by_call_time";
	public static final String REPORT_MANAGEMENT_DOWNLOAD_TELEPHONE_TRAFFIC_CHECK_BY_CALL_TIME = "report_management&download_telephone_traffic_check_by_call_time";
	private static final String REPORT_MANAGEMENT_STAFF_CHECK = "report_management&staff_check";
	public static final String REPORT_MANAGEMENT_DOWNLOAD_STAFF_CHECK = "report_management&download_staff_check";
	private static final String REPORT_MANAGEMENT_STAFF_TIME_SHEET = "report_management&staff_time_sheet";
	public static final String REPORT_MANAGEMENT_DOWNLOAD_STAFF_TIME_SHEET = "report_management&download_staff_time_sheet";
	private static final String REPORT_MANAGEMENT_CONCURRENT_STATICS = "report_management&concurrent_statics";
	public static final String REPORT_MANAGEMENT_DOWNLOAD_CONCURRENT_STATICS = "report_management&download_concurrent_statics";
	private static final String REPORT_MANAGEMENT_SEE_QUEUE_DETAIL = "report_management&see_queue_detail";
	public static final String REPORT_MANAGEMENT_DOWNLOAD_SEE_QUEUE_DETAIL = "report_management&download_see_queue_detail";
	private static final String REPORT_MANAGEMENT_SEE_AUTODIAL_DETAIL = "report_management&see_autodial_detail";
	public static final String REPORT_MANAGEMENT_DOWNLOAD_SEE_AUTODIAL_DETAIL = "report_management&download_see_autodial_detail";
	private static final String REPORT_MANAGEMENT_SATISFACTION_INVESTIGATE = "report_management&satisfaction_investigate";
	public static final String REPORT_MANAGEMENT_DOWNLOAD_SATISFACTION_INVESTIGATE = "report_management&download_satisfaction_investigate";
	private static final String REPORT_MANAGEMENT_KPI = "report_management&kpi_detail";
	public static final String REPORT_MANAGEMENT_DOWNLOAD_KPI_DETAIL = "report_management&download_kpi_detail";
	private static final String REPORT_MANAGEMENT_PROJECT_POOL = "report_management&project_pool";
	private static final String REPORT_MANAGEMENT_WORKFLOW_TRANSFER_LOG = "report_management&workflow_transfer_log";
	private static final String REPORT_MANAGEMENT_QUESTIONNAIRE_DETAIL = "report_management&questionnaire_detail";
	private static final String REPORT_MANAGEMENT_CUSTOMER_DETAIL = "report_management&customer_detail";
	private static final String REPORT_MANAGEMENT_BUSINESS_DETAIL = "report_management&business_detail";
	private static final String REPORT_MANAGEMENT_CSR_WORK_DETAIL = "report_management&csr_work_detail";
	public static final String REPORT_MANAGEMENT_ADVANCE_KPI = "report_management&advance_kpi";
	public static final String REPORT_MANAGEMENT_DOWNLOAD_ADVANCE_KPI = "report_management&download_advance_kpi";
	public static final String REPORT_MANAGEMENT_PAUSE_DETAIL = "report_management&pause_detail";// 置忙报表  XKP
	public static final String REPORT_MANAGEMENT_DOWNLOAD_PAUSE_DETAIL = "report_management&download_pause_detail";// 置忙报表导出权限 XKP
	public static final String REPORT_MANAGEMENT_PAUSE_DETAIL_STATISTICS = "report_management&pause_detail_statistics";	// 置忙详情统计报表
	public static final String REPORT_MANAGEMENT_DOWNLOAD_PAUSE_DETAIL_STATISTICS = "report_management&download_pause_detail_statistics";	// 置忙详情统计报表导出权限
	public static final String REPORT_MANAGEMENT_CUSTOMER_SERVICE_RECORD_STATISTICAL = "report_management&customer_service_record_statistical";	// 客服记录统计报表
	public static final String REPORT_MANAGEMENT_DOWNLOAD_CUSTOMER_SERVICE_RECORD_STATISTICAL = "report_management&download_customer_service_record_statistical";	// 客服记录统计报表下载

	// 客户管理
	private static final String CUSTOMER_MANAGEMENT = "customer_management";
	private static final String CUSTOMER_MANAGEMENT_CUSTOMER_MEMBER_MANAGE = "customer_management&customer_member_manage";
	private static final String CUSTOMER_MANAGEMENT_CUSTOMER_MIGRATE_LOG_MANAGE = "customer_management&customer_migrate_log_manage";
	public static final String CUSTOMER_MANAGEMENT_DOWNLOAD_CUSTOMER_MIGRATE_LOG_MANAGE = "customer_management&download_customer_migrate_log_manage";

	// 监控管理
	private static final String SUPERVISE_MANAGEMENT = "supervise_management";
	private static final String SUPERVISE_MANAGEMENT_EMPLOYEE_STATUS_SUPERVISE = "supervise_management&employee_status_supervise";
	private static final String SUPERVISE_MANAGEMENT_SIP_STATUS_SUPERVISE = "supervise_management&sip_status_supervise";
	private static final String SUPERVISE_MANAGEMENT_QUEUE_STATUS_SUPERVISE = "supervise_management&queue_status_supervise";
	private static final String SUPERVISE_MANAGEMENT_CALL_STATUS_SUPERVISE = "supervise_management&call_status_supervise";
	private static final String SUPERVISE_MANAGEMENT_MEETING_ROOM_SUPERVISE = "supervise_management&meeting_room_supervise";

	// 业务管理
	private static final String BUSINESS_MANAGEMENT = "business_management";
	private static final String BUSINESS_MANAGEMENT_COMMODITY_MANAGE = "business_management&commodity_manage";
	private static final String BUSINESS_MANAGEMENT_ORDER_MANAGE = "business_management&order_manage";
	private static final String BUSINESS_MANAGEMENT_QUESTIONNAIRE_MANAGE = "business_management&questionnaire_manage";
	private static final String BUSINESS_MANAGEMENT_QUESTIONNAIRE_EDIT_MANAGE = "business_management&questionnaire_edit_manage";

	// 消息管理
	public static final String MESSAGE_MANAGEMENT = "message_management";
	public static final String MESSAGE_MANAGEMENT_MESSAGE_SEND = "message_management&message_send";
	public static final String MESSAGE_MANAGEMENT_HISTORY_MESSAGE = "message_management&history_message";
	public static final String MESSAGE_MANAGEMENT_CSR_TIMERS_ORDER_DETAIL = "message_management&csr_timers_order_detail";
	public static final String MESSAGE_MANAGEMENT_DOWNLOAD_CSR_TIMERS_ORDER_DETAIL = "message_management&download_csr_timers_order_detail";
	
	// 短信管理组件
	private static final String PHONE_MESSAGE_MANAGEMENT = "phone_message_management";
	private static final String PHONE_MESSAGE_MANAGEMENT_TEMPLATE_MANAGEMENT = "phone_message_management&template_management";
	private static final String PHONE_MESSAGE_MANAGEMENT_SEND_MANAGEMENT = "phone_message_management&send_management";
	private static final String PHONE_MESSAGE_MANAGEMENT_HISTORY_MANAGEMENT = "phone_message_management&history_management";
	
	// 邮件管理
	public static final String EMAIL_MANAGEMENT = "manager_email_management";
	public static final String EMAIL_MANAGEMENT_SEND_EMAIL = "manager_email_management&manager_send_email";				// 发送邮件
	public static final String EMAIL_MANAGEMENT_HISTORY_EMAIL = "manager_email_management&manager_history_email";			// 历史邮件
	public static final String EMAIL_MANAGEMENT_CONFIG_EMAIL = "manager_email_management&manager_config_email";			// 配置邮箱

	// 知识库管理组件
	private static final String KNOWLEDGE_BASE_MANAGEMENT = "knowledge_base_management";
	private static final String KNOWLEDGE_BASE_MANAGEMENT_KNOWLEDGE_MANAGE = "knowledge_base_management&knowledge_manage";
//	private static final String KNOWLEDGE_BASE_MANAGEMENT_KNOWLEDGE_VIEW = "knowledge_base_management&knowledge_view";

	// 系统管理
	private static final String SYSTEM_MANAGEMENT = "system_management";
	private static final String SYSTEM_MANAGEMENT_USER_MANAGE = "system_management&user_manage";
	private static final String SYSTEM_MANAGEMENT_ROLE_MANAGE = "system_management&role_manage";
	private static final String SYSTEM_MANAGEMENT_DEPARTMENT_MANAGE = "system_management&department_manage";
	private static final String SYSTEM_MANAGEMENT_EXTEN_MANAGE = "system_management&exten_manage";
	private static final String SYSTEM_MANAGEMENT_OUTLINE_MANAGE = "system_management&outline_manage";
	private static final String SYSTEM_MANAGEMENT_QUEUE_MANAGE = "system_management&queue_manage";
	private static final String SYSTEM_MANAGEMENT_OUTLINE_MEMBER_MANAGE = "system_management&outline_member_manage";
	private static final String SYSTEM_MANAGEMENT_OUTLINE_POOL_MANAGE = "system_management&outline_pool_manage";
	private static final String SYSTEM_MANAGEMENT_MOH_MANAGE = "system_management&moh_manage";
	private static final String SYSTEM_MANAGEMENT_DYNAMIC_QUEUE_MEMBER_MANAGE = "system_management&dynamic_queue_member_manage";
	private static final String SYSTEM_MANAGEMENT_STATIC_QUEUE_MEMBER_MANAGE = "system_management&static_queue_member_manage";
	private static final String SYSTEM_MANAGEMENT_INTERNAL_SETTING_MANAGE = "system_management&internal_setting_manage";
	private static final String SYSTEM_MANAGEMENT_SERVICE_RECORD_STATUS_MANAGE = "system_management&service_record_status_manage";
	private static final String SYSTEM_MANAGEMENT_DUTY_REPORT_MANAGE = "system_management&duty_report_manage";
	private static final String SYSTEM_MANAGEMENT_MGR_PHONE2PHONE_SETTING_MANAGE = "system_management&mgr_phone2phone_setting_manage";
	private static final String SYSTEM_MANAGEMENT_MGR_MEET_ME = "system_management&mgr_meet_me";
	private static final String SYSTEM_MANAGEMENT_OPERATION_LOG_VIEW = "system_management&operation_log_view";
	private static final String SYSTEM_MANAGEMENT_IVR_MANAGE = "system_management&ivr_manage";
	private static final String SYSTEM_MANAGEMENT_BLACKLIST_MANAGE = "system_management&blackList_manage";
	private static final String SYSTEM_MANAGEMENT_SOFTPHONE_DIALPLAN_MANAGE = "system_management&softphone_dialplan_manage";
	private static final String SYSTEM_MANAGEMENT_SATIS_NUM_MANAGE = "system_management&satis_num_manage";
	private static final String SYSTEM_MANAGEMENT_CUSTOMER_SERVICE_RECORD_STATUS_NAVIGATION_KEY = "system_management&customer_service_record_status_navigation_key";
	private static final String SYSTEM_MANAGEMENT_FIRSTMANAGEMENT = "system_management&firstmanagement";
	public static final String SYSTEM_MANAGEMENT_FIRSTMANAGEMENT_REMOVEFILE = "system_management&firstmanagement_removefile";
	// 系统信息管理组件
	public static final String SYSTEM_INFO_MANAGEMENT = "system_info_management";
	public static final String SYSTEM_INFO_MANAGEMENT_SYSTEM_STATUS = "system_info_management&system_status";
	public static final String SYSTEM_INFO_MANAGEMENT_SYSTEM_INFO = "system_info_management&system_info";
	public static final String SYSTEM_INFO_MANAGEMENT_SYSTEM_LISCENCE = "system_info_management&system_liscence";
	public static final String SYSTEM_INFO_MANAGEMENT_ABOUT_US = "system_info_management&about_us";
	public static final String SYSTEM_INFO_MANAGEMENT_SYSTEM_LISCENCE_SYSTEM_OPERATE = "system_info_management&system_liscence_system_operate";
	// -------------- 数据管理模块 ----------------//

	// 数据管理
	private VerticalLayout dataManagement;

	// 资源管理--组件
	private NativeButton resourceImport;
	// Excel配置--组件
	private NativeButton globalSearch;
	// 客服记录管理--组件
	private NativeButton serviceRecord;
	// 呼叫记录管理选项 jrh
	private NativeButton callRecordManage;
	// 电话漏接记录选项 jrh
	private NativeButton missCallLogManage;
	// 多方通话管理选项 jrh
	private NativeButton meettingDetailRecordManage;
	// 资源管理
	private NativeButton resourceManage;

	// -------------- 项目管理模块 ---------------- //

	// 项目管理
	private VerticalLayout projectManagement;

	// 项目管理--组件
	private NativeButton projectControl;

	// -------------- 自动外呼管理模块 ----------------//

	// 自动外呼
	private VerticalLayout autoDialoutManagement;

	// 自动外呼--组件
	private NativeButton autoDialout;
	// 资源回收--组件
	private NativeButton resourceRecycle;
	// 语音群发--组件
	private NativeButton soundDialout;
	// 语音上传--组件
	private NativeButton soundUpload;

	// -------------- 消息管理模块 ---------------- //

	// 消息管理
	private VerticalLayout messageManagement;

	// 消息发送--组件
	private NativeButton noticeSend;
	// 历史消息--组件
	private NativeButton historyNotice;
	// 坐席定时器预约信息管理 jrh
	private NativeButton csrTimersOrderManage;

	// -------------- 报表管理模块 ---------------- //
	// TODO 报表
	// 查看报表
	private VerticalLayout checkReportForm;
	
	// TODO 客服记录统计
	private NativeButton customerServiceRecordStatisticalView;
	// 项目完成情况
	private NativeButton callStatisticOverview_nb;
	// 项目完成情况
	private NativeButton projectFinishedStatus;
	// 客服记录情况
	private NativeButton serviceRecordStatus;
	// 话务考核
	private NativeButton callCheck;
	// 按通话时长进行话务考核（通次考核）
	private NativeButton callCountCheck;
	// 人员考核
	private NativeButton employeeCheck;
	// 员工上下线祥单
	private NativeButton loginLogoutDetail;
	// 并发统计
	private NativeButton concurrentStatics;
	// 队列详情
	private NativeButton queueDetail;
	// 自动外呼详情
	private NativeButton autodialDetail;
	// 满意度调查
	private NativeButton satisfactionInvestigate;
	// KPI
	private NativeButton kpi;
	// AdvanceKPI 报表管理
	private NativeButton advanceKpi_nb;

	private NativeButton projectPool;

	// jrh 工作流迁移日志
	private NativeButton workflowTransferLog_nb;

	// 问卷
	private NativeButton questionnaire;
	// 明细
	private NativeButton customerDetail;
	// 业务报表
	private NativeButton businessDetail;
	// 座席工作详情
	private NativeButton csrWorkDetail;
	// 置忙报表
	private NativeButton pauseDetail;
	// 忙详情统计报表
	private NativeButton pauseDetailStatistics;
	
	// ------------客户管理组件----------------//

	// 客户管理
	private VerticalLayout customerManagement;

	// 客户成员管理
	private NativeButton customerMemberManage;
	// 客户迁移日志管理
	private NativeButton customerMigrateLogMange;

	// --------------实时监控管理组件----------------//

	// 实时监控管理
	private VerticalLayout superviseManagement;

	// 坐席状态监控
	private NativeButton employeeStatusSupervise;
	// 分机状态监控
	private NativeButton sipStatusSupervise;
	// 队列状态监控
	private NativeButton queueStatusSupervise;
	// 通话状态监控
	private NativeButton callStatusSupervise;
	// 活动会议室监控
	private NativeButton meetMeSupervise;

	// --------------业务管理组件----------------//

	// 业务管理
	private VerticalLayout businessManagement;

	// 商品管理
	private NativeButton commodityManage;
	// 订单管理
	private NativeButton orderManage;
	// 问卷管理
	private NativeButton questionnaireManagement;
	// 问卷编辑
	private NativeButton questionnaireManagementEdit;

	// ------------------短信管理组件------------------//

	// 短信管理
	private VerticalLayout noteManagement;

	// 模板管理
	private NativeButton templateManage;
	// 短信发送
	private NativeButton sendMessage;
	// 已发送短信查看
	private NativeButton historySms;

	// --------------知识库管理组件----------------//

	// 知识库管理
	private VerticalLayout knowledgeBaseManagement;

	// 知识管理
	private NativeButton knowledgeManage;
	// // 知识查看
	// private NativeButton knowledgeView;

	// --------------邮件发送组件----------------//
	
	// 邮件管理
	private VerticalLayout emailManagement;
	
	// 邮件发送
	private NativeButton sendEmail;
	// 历史邮件
	private NativeButton historyEmail;
	// 配置邮件
	private NativeButton configEmail;

	// --------------系统管理组件----------------//

	// 系统管理
	private VerticalLayout systemManagement;

	// 用户管理
	private NativeButton userManagement;
	// 角色管理
	private NativeButton roleManagement;
	// 部门管理
	private NativeButton deptManagement;
	// 分机管理
	private NativeButton extManagement;
	// 外线管理
	private NativeButton outlineManagement;
	// 外线成员管理
	private NativeButton userOutlineManagement;
	// 外线池管理
	private NativeButton userOutlinePoolManagement;
	// 队列管理
	private NativeButton queueManagement;
	// 语音导航
	private NativeButton ivrManage;
	// 保持音乐管理
	private NativeButton musicOnHoldManagement;
	// 动态队列成员管理
	private NativeButton dynQueueMemberManagement;
	// 静态队列成员管理
	private NativeButton staticQueueMemberManagement;
	// 内部配置--组件
	private NativeButton innerConfig;
	// 客服记录状态配置--组件
	private NativeButton serviceRecordStatusManagement;
	// 值班表管理
	private NativeButton dutyTableManagement;
	// 外转外配置管理
	private NativeButton mgrPhone2phoneSettingManagement;
	// 管理员会议室
	private NativeButton mgrMeetMe;
	// 管理员会议室
	private NativeButton operationLogView;
	// 黑名单管理
	private NativeButton blacklistManagement;
	// 软电话拨号方案管理
	private NativeButton solfphoneDialplanManagement;
	//满意度调查按键管理
	private NativeButton satisNumManager;

	// 客服记录状态导航键管理界
	private NativeButton customerServiceRecordStatusNavigationKeyManagement;

	// 系统首页
	private NativeButton firstManagement;
		
	// --------------系统信息管理组件----------------//

	// 系统管理
	private VerticalLayout systemInfoManagement;
	// 系统状态
	private NativeButton systemStatus;
	// 系统信息
	private NativeButton systemInfo;
	// Licence信息
	private NativeButton systemLiscence;
	// 关于
	private NativeButton aboutUs;
	// 语音留言信息
	private NativeButton voiceMail;
	
	// ================持有的组件==================//
	private MgrTabSheet mgrTabSheet;

	public MgrAccordion() {
		// 数据管理
		if (SpringContextHolder.getBusinessModel().contains(DATA_MANAGEMENT)) {
			dataManagement = new VerticalLayout();
			dataManagement.setStyleName("sidebar-menu");
			dataManagement.setSizeFull();
			this.addTab(dataManagement, "数据管理", ResourceDataMgr.data_manage);
			dataManagement.addComponent(buildDataLayout());
		}

		// 项目管理
		if (SpringContextHolder.getBusinessModel().contains(PROJECT_MANAGEMENT)) {
			projectManagement = new VerticalLayout();
			projectManagement.setStyleName("sidebar-menu");
			projectManagement.setSizeFull();
			this.addTab(projectManagement, "项目管理",
					ResourceDataMgr.project_manage);
			projectManagement.addComponent(buildProjectLayout());
		}

		// 自动外呼管理
		if (SpringContextHolder.getBusinessModel().contains(
				AUTO_DIAL_MANAGEMENT)) {
			autoDialoutManagement = new VerticalLayout();
			autoDialoutManagement.setStyleName("sidebar-menu");
			autoDialoutManagement.setSizeFull();
			this.addTab(autoDialoutManagement, "自动外呼管理",
					ResourceDataMgr.auto_dialout_manage);
			autoDialoutManagement.addComponent(buildAutoDialoutLayout());
		}

		// 消息管理
		if (SpringContextHolder.getBusinessModel().contains(MESSAGE_MANAGEMENT)) {
			messageManagement = new VerticalLayout();
			messageManagement.setStyleName("sidebar-menu");
			messageManagement.setSizeFull();
			this.addTab(messageManagement, "消息管理",
					ResourceDataMgr.message_manage);
			messageManagement.addComponent(buildMessageLayout());
		}

		// 查看报表
		if (SpringContextHolder.getBusinessModel().contains(REPORT_MANAGEMENT)) {
			checkReportForm = new VerticalLayout();
			checkReportForm.setStyleName("sidebar-menu");
			checkReportForm.setSizeFull();
			this.addTab(checkReportForm, "查看报表", ResourceDataMgr.report_manage);
			checkReportForm.addComponent(buildReportLayout());
		}

		// 客户管理
		if (SpringContextHolder.getBusinessModel()
				.contains(CUSTOMER_MANAGEMENT)) {
			customerManagement = new VerticalLayout();
			customerManagement.setStyleName("sidebar-menu");
			customerManagement.setSizeFull();
			this.addTab(customerManagement, "客户管理",
					ResourceDataMgr.customer_management_14_ico);
			customerManagement.addComponent(buildCustomerLayout());
		}

		// 实时监控管理
		if (SpringContextHolder.getBusinessModel().contains(
				SUPERVISE_MANAGEMENT)) {
			superviseManagement = new VerticalLayout();
			superviseManagement.setStyleName("sidebar-menu");
			superviseManagement.setSizeFull();
			this.addTab(superviseManagement, "实时监控管理",
					ResourceDataMgr.supervise_14_ico);
			superviseManagement.addComponent(buildSuperviseLayout());
		}

		// 商品订单管理
		if (SpringContextHolder.getBusinessModel()
				.contains(BUSINESS_MANAGEMENT)) {
			businessManagement = new VerticalLayout();
			businessManagement.setStyleName("sidebar-menu");
			businessManagement.setSizeFull();
			this.addTab(businessManagement, "业务管理",
					ResourceDataMgr.business_14_ico);
			businessManagement.addComponent(buildBusinessManagementLayout());
		}

		// 短信管理
		if (SpringContextHolder.getBusinessModel().contains(PHONE_MESSAGE_MANAGEMENT)) {
			noteManagement = new VerticalLayout();
			noteManagement.setStyleName("sidebar-menu");
			noteManagement.setSizeFull();
			this.addTab(noteManagement, "短信管理", ResourceDataMgr.phone_message_14_ico);
			noteManagement.addComponent(buildNoteManagementLayout());
		}
		
		// 邮件管理
		if (SpringContextHolder.getBusinessModel().contains(EMAIL_MANAGEMENT)) {
			emailManagement = new VerticalLayout();
			emailManagement.setStyleName("sidebar-menu");
			emailManagement.setSizeFull();
			this.addTab(emailManagement, "邮件管理", ResourceDataMgr.phone_message_14_ico);
			emailManagement.addComponent(buildEmailManagementLayout());
		}

		// 知识库管理
		if (SpringContextHolder.getBusinessModel().contains(
				KNOWLEDGE_BASE_MANAGEMENT)) {
			knowledgeBaseManagement = new VerticalLayout();
			knowledgeBaseManagement.setStyleName("sidebar-menu");
			knowledgeBaseManagement.setSizeFull();
			this.addTab(knowledgeBaseManagement, "知识库管理",
					ResourceDataMgr.knowledge_14_ico);
			knowledgeBaseManagement
					.addComponent(buildKnowledgeBaseManagementLayout());
		}

		// 系统管理
		if (SpringContextHolder.getBusinessModel().contains(SYSTEM_MANAGEMENT)) {
			systemManagement = new VerticalLayout();
			systemManagement.setStyleName("sidebar-menu");
			systemManagement.setSizeFull();
			this.addTab(systemManagement, "系统管理", ResourceDataMgr.system_manage);
			systemManagement.addComponent(buildSystemManagementLayout());
		}

		// 系统信息
		if (SpringContextHolder.getBusinessModel().contains(SYSTEM_INFO_MANAGEMENT)) {
			systemInfoManagement = new VerticalLayout();
			systemInfoManagement.setStyleName("sidebar-menu");
			systemInfoManagement.setSizeFull();
			this.addTab(systemInfoManagement, "系统信息管理", ResourceDataMgr.system_info_14_ico);
			systemInfoManagement.addComponent(buildSystemInfoLayout());
		}
	}

	/**
	 * 数据管理
	 * 
	 * @return
	 */
	private VerticalLayout buildDataLayout() {
		VerticalLayout constrantLayout = new VerticalLayout();
		// 资源导入组件
		if (SpringContextHolder.getBusinessModel().contains(DATA_MANAGEMENT_RESOURCE_IMPORT)) {
			resourceImport = new NativeButton("资源导入");
			resourceImport.addListener(this);
			constrantLayout.addComponent(resourceImport);
		}
		// TODO add it, function module didn't complete
		// // 全局搜索
		// if (SpringContextHolder.getBusinessModel().contains(
		// DATA_MANAGEMENT_GLOBAL_SEARCH)) {
		// globalSearch = new NativeButton("全局搜索");
		// globalSearch.addListener(this);
		// constrantLayout.addComponent(globalSearch);
		// }

		// 客服记录查看组件
		if (SpringContextHolder.getBusinessModel().contains(DATA_MANAGEMENT_SERVICE_RECORD)) {
			serviceRecord = new NativeButton("客服记录");
			serviceRecord.addListener(this);
			constrantLayout.addComponent(serviceRecord);
		}

		if (SpringContextHolder.getBusinessModel().contains(DATA_MANAGEMENT_CALL_RECORD)) {
			callRecordManage = new NativeButton("呼叫记录");
			callRecordManage.setImmediate(true);
			callRecordManage.addListener(this);
			constrantLayout.addComponent(callRecordManage);
		}
		
		if (SpringContextHolder.getBusinessModel().contains(DATA_MANAGEMENT_MISS_CALL_LOG)) {
			missCallLogManage = new NativeButton("电话漏接记录");
			missCallLogManage.setImmediate(true);
			missCallLogManage.addListener(this);
			constrantLayout.addComponent(missCallLogManage);
		}

		if (SpringContextHolder.getBusinessModel().contains(DATA_MANAGEMENT_MEETTING_DETAIL_RECORD)) {
			meettingDetailRecordManage = new NativeButton("多方通话记录");
			meettingDetailRecordManage.setImmediate(true);
			meettingDetailRecordManage.addListener(this);
			constrantLayout.addComponent(meettingDetailRecordManage);
		}
		
		if (SpringContextHolder.getBusinessModel().contains(DATA_MANAGEMENT_RESOURCE_MANAGE)) {
			resourceManage = new NativeButton("资源管理");
			resourceManage.setImmediate(true);
			resourceManage.addListener(this);
			constrantLayout.addComponent(resourceManage);
		}
		
		if(SpringContextHolder.getBusinessModel().contains(DATA_MANAGEMENT_MANAGER_VOICEMAIL_DETAILS)) {
			voiceMail = new NativeButton("语音留言管理");
			voiceMail.addListener(this);
			constrantLayout.addComponent(voiceMail);
		}

		return constrantLayout;
	}

	/**
	 * 项目控制
	 * 
	 * @return
	 */
	private VerticalLayout buildProjectLayout() {
		VerticalLayout constrantLayout = new VerticalLayout();

		// 项目控制 组件
		if (SpringContextHolder.getBusinessModel().contains(
				PROJECT_MANAGEMENT_PROJECT_CONTROL)) {
			projectControl = new NativeButton("项目控制");
			projectControl.addListener(this);
			constrantLayout.addComponent(projectControl);
		}

		return constrantLayout;
	}

	/**
	 * 自动外呼
	 * 
	 * @return
	 */
	private VerticalLayout buildAutoDialoutLayout() {
		VerticalLayout constrantLayout = new VerticalLayout();

		// 自动外呼 组件
		if (SpringContextHolder.getBusinessModel().contains(
				AUTO_DIAL_MANAGEMENT_AUTO_DIAL)) {
			autoDialout = new NativeButton("自动外呼");
			autoDialout.addListener(this);
			constrantLayout.addComponent(autoDialout);
		}

		// 语音群发 组件
		if (SpringContextHolder.getBusinessModel().contains(
				AUTO_DIAL_MANAGEMENT_VOICE_PACKET)) {
			soundDialout = new NativeButton("语音群发");
			soundDialout.addListener(this);
			constrantLayout.addComponent(soundDialout);
		}

		// 语音上传 组件
		if (SpringContextHolder.getBusinessModel().contains(
				AUTO_DIAL_MANAGEMENT_VOICE_UPLOAD)) {
			soundUpload = new NativeButton("语音上传");
			soundUpload.addListener(this);
			constrantLayout.addComponent(soundUpload);
		}

		// 资源回收 组件
		if (SpringContextHolder.getBusinessModel().contains(
				AUTO_DIAL_MANAGEMENT_RECYCLE_RESOURCE)) {
			resourceRecycle = new NativeButton("资源回收");
			resourceRecycle.addListener(this);
			constrantLayout.addComponent(resourceRecycle);
		}

		return constrantLayout;
	}

	/**
	 * 消息发送
	 * 
	 * @return
	 */
	private VerticalLayout buildMessageLayout() {
		VerticalLayout constrantLayout = new VerticalLayout();

		// 消息发送组件
		if (SpringContextHolder.getBusinessModel().contains(
				MESSAGE_MANAGEMENT_MESSAGE_SEND)) {
			noticeSend = new NativeButton("消息发送");
			noticeSend.addListener(this);
			constrantLayout.addComponent(noticeSend);
		}

		// 消息发送组件
		if (SpringContextHolder.getBusinessModel().contains(
				MESSAGE_MANAGEMENT_HISTORY_MESSAGE)) {
			historyNotice = new NativeButton("历史消息");
			historyNotice.addListener(this);
			constrantLayout.addComponent(historyNotice);
		}

		if (SpringContextHolder.getBusinessModel().contains(
				MESSAGE_MANAGEMENT_CSR_TIMERS_ORDER_DETAIL)) {
			csrTimersOrderManage = new NativeButton("坐席定时预约详情");
			csrTimersOrderManage.setImmediate(true);
			csrTimersOrderManage.addListener(this);
			constrantLayout.addComponent(csrTimersOrderManage);
		}

		return constrantLayout;
	}

	// >>>>>>>>>>>>>>>>>>>>>>>>>报表<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<//
	private VerticalLayout buildReportLayout() {
		VerticalLayout constrantLayout = new VerticalLayout();

		// transferLog
		if (SpringContextHolder.getBusinessModel().contains(
				REPORT_MANAGEMENT_CALL_STATISTIC_OVERVIEW)) {
			callStatisticOverview_nb = new NativeButton("话务统计总概览");
			callStatisticOverview_nb.addListener(this);
			constrantLayout.addComponent(callStatisticOverview_nb);
		}
		
		// 项目完成情况
		if (SpringContextHolder.getBusinessModel().contains(
				REPORT_MANAGEMENT_PROJECT_FINISH_STATUS)) {
			projectFinishedStatus = new NativeButton("项目完成情况");
			projectFinishedStatus.addListener(this);
			constrantLayout.addComponent(projectFinishedStatus);
		}

		// 客服记录情况
		if (SpringContextHolder.getBusinessModel().contains(
				REPORT_MANAGEMENT_SERVICE_RECORD_STATUS)) {
			serviceRecordStatus = new NativeButton("客服记录情况");
			serviceRecordStatus.addListener(this);
			constrantLayout.addComponent(serviceRecordStatus);
		}

		// 话务考核
		if (SpringContextHolder.getBusinessModel().contains(
				REPORT_MANAGEMENT_TELEPHONE_TRAFFIC_CHECK)) {
			callCheck = new NativeButton("话务考核");
			callCheck.addListener(this);
			constrantLayout.addComponent(callCheck);
		}

		// 按通话时长进行话务考核
		if (SpringContextHolder.getBusinessModel().contains(
				REPORT_MANAGEMENT_TELEPHONE_TRAFFIC_CHECK_BY_CALL_TIME)) {
			callCountCheck = new NativeButton("通次考核");
			callCountCheck.addListener(this);
			constrantLayout.addComponent(callCountCheck);
		}

		// 人员考核
		if (SpringContextHolder.getBusinessModel().contains(
				REPORT_MANAGEMENT_STAFF_CHECK)) {
			employeeCheck = new NativeButton("人员考核");
			employeeCheck.addListener(this);
			constrantLayout.addComponent(employeeCheck);
		}

		// 员工上下线详单
		if (SpringContextHolder.getBusinessModel().contains(
				REPORT_MANAGEMENT_STAFF_TIME_SHEET)) {
			loginLogoutDetail = new NativeButton("员工上下线详单");
			loginLogoutDetail.addListener(this);
			constrantLayout.addComponent(loginLogoutDetail);
		}

		// 并发统计
		if (SpringContextHolder.getBusinessModel().contains(
				REPORT_MANAGEMENT_CONCURRENT_STATICS)) {
			concurrentStatics = new NativeButton("并发统计");
			concurrentStatics.addListener(this);
			constrantLayout.addComponent(concurrentStatics);
		}

		// 队列详情
		if (SpringContextHolder.getBusinessModel().contains(
				REPORT_MANAGEMENT_SEE_QUEUE_DETAIL)) {
			queueDetail = new NativeButton("队列详情");
			queueDetail.addListener(this);
			constrantLayout.addComponent(queueDetail);
		}

		// 自动外呼详情
		if (SpringContextHolder.getBusinessModel().contains(
				REPORT_MANAGEMENT_SEE_AUTODIAL_DETAIL)) {
			autodialDetail = new NativeButton("自动外呼详情");
			autodialDetail.addListener(this);
			constrantLayout.addComponent(autodialDetail);
		}

		// 满意度调查
		if (SpringContextHolder.getBusinessModel().contains(
				REPORT_MANAGEMENT_SATISFACTION_INVESTIGATE)) {
			satisfactionInvestigate = new NativeButton("满意度调查");
			satisfactionInvestigate.addListener(this);
			constrantLayout.addComponent(satisfactionInvestigate);
		}

		// kpi
		if (SpringContextHolder.getBusinessModel().contains(
				REPORT_MANAGEMENT_KPI)) {
			kpi = new NativeButton("KPI详情");
			kpi.addListener(this);
			constrantLayout.addComponent(kpi);
		}

		// 高级API详情
		if (SpringContextHolder.getBusinessModel().contains(
				REPORT_MANAGEMENT_ADVANCE_KPI)) {
			advanceKpi_nb = new NativeButton("高级KPI详情");
			advanceKpi_nb.addListener(this);
			constrantLayout.addComponent(advanceKpi_nb);
		}
		
		// 客服记录统计
		if(SpringContextHolder.getBusinessModel().contains(
				REPORT_MANAGEMENT_CUSTOMER_SERVICE_RECORD_STATISTICAL)){
			customerServiceRecordStatisticalView = new NativeButton("客服记录统计");
			customerServiceRecordStatisticalView.addListener(this);
			constrantLayout.addComponent(customerServiceRecordStatisticalView);
		}
		
		// projectPool
		if (SpringContextHolder.getBusinessModel().contains(
				REPORT_MANAGEMENT_PROJECT_POOL)) {
			projectPool = new NativeButton("土豆池详情");
			projectPool.addListener(this);
			constrantLayout.addComponent(projectPool);
		}

		// transferLog
		if (SpringContextHolder.getBusinessModel().contains(
				REPORT_MANAGEMENT_WORKFLOW_TRANSFER_LOG)) {
			workflowTransferLog_nb = new NativeButton("工作流迁移日志");
			workflowTransferLog_nb.addListener(this);
			constrantLayout.addComponent(workflowTransferLog_nb);
		}

		// 问卷
		if (SpringContextHolder.getBusinessModel().contains(
				REPORT_MANAGEMENT_QUESTIONNAIRE_DETAIL)) {
			questionnaire = new NativeButton("问卷详情");
			questionnaire.addListener(this);
			constrantLayout.addComponent(questionnaire);
		}

		if (SpringContextHolder.getBusinessModel().contains(
				REPORT_MANAGEMENT_CUSTOMER_DETAIL)) {

			customerDetail = new NativeButton("客户明细");
			customerDetail.addListener(this);
			constrantLayout.addComponent(customerDetail);
		}

		if (SpringContextHolder.getBusinessModel().contains(
				REPORT_MANAGEMENT_BUSINESS_DETAIL)) {

			businessDetail = new NativeButton("业务报表");
			businessDetail.addListener(this);
			constrantLayout.addComponent(businessDetail);
		}

		if (SpringContextHolder.getBusinessModel().contains(
				REPORT_MANAGEMENT_CSR_WORK_DETAIL)) {

			csrWorkDetail = new NativeButton("座席工作详情");
			csrWorkDetail.addListener(this);
			constrantLayout.addComponent(csrWorkDetail);
		}
		
		if (SpringContextHolder.getBusinessModel().contains(REPORT_MANAGEMENT_PAUSE_DETAIL)) {

			pauseDetail = new NativeButton("置忙报表");
			pauseDetail.addListener(this);
			constrantLayout.addComponent(pauseDetail);
		}
		
		if(SpringContextHolder.getBusinessModel().contains(REPORT_MANAGEMENT_PAUSE_DETAIL_STATISTICS)) {
			pauseDetailStatistics = new NativeButton("置忙详情统计报表 ");
			pauseDetailStatistics.addListener(this);
			constrantLayout.addComponent(pauseDetailStatistics);
		}
		
		return constrantLayout;

	}

	// >>>>>>>>>>>>>>>>>>>>>>>>>客户管理<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<//

	/**
	 * jrh 客户管理
	 * 
	 * @return
	 */
	private VerticalLayout buildCustomerLayout() {
		VerticalLayout constrantLayout = new VerticalLayout();

		// 客户成员管理
		if (SpringContextHolder.getBusinessModel().contains(
				CUSTOMER_MANAGEMENT_CUSTOMER_MEMBER_MANAGE)) {
			customerMemberManage = new NativeButton("客户成员管理");
			customerMemberManage.addListener(this);
			constrantLayout.addComponent(customerMemberManage);
		}

		// 迁移日志管理
		if (SpringContextHolder.getBusinessModel().contains(
				CUSTOMER_MANAGEMENT_CUSTOMER_MIGRATE_LOG_MANAGE)) {
			customerMigrateLogMange = new NativeButton("客户迁移日志");
			customerMigrateLogMange.addListener(this);
			constrantLayout.addComponent(customerMigrateLogMange);
		}

		return constrantLayout;
	}

	// >>>>>>>>>>>>>>>>>>>>>>>>>实时监控管理<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< //

	/**
	 * jrh 实时监控管理
	 * 
	 * @return
	 */
	private VerticalLayout buildSuperviseLayout() {
		VerticalLayout constrantLayout = new VerticalLayout();
		// 坐席状态监控
		if (SpringContextHolder.getBusinessModel().contains(
				SUPERVISE_MANAGEMENT_EMPLOYEE_STATUS_SUPERVISE)) {
			employeeStatusSupervise = new NativeButton("坐席状态监控");
			employeeStatusSupervise.addListener(this);
			constrantLayout.addComponent(employeeStatusSupervise);
		}

		// 分机状态监控
		if (SpringContextHolder.getBusinessModel().contains(
				SUPERVISE_MANAGEMENT_SIP_STATUS_SUPERVISE)) {
			sipStatusSupervise = new NativeButton("分机状态监控");
			sipStatusSupervise.addListener(this);
			constrantLayout.addComponent(sipStatusSupervise);
		}

		// 队列状态监控
		if (SpringContextHolder.getBusinessModel().contains(
				SUPERVISE_MANAGEMENT_QUEUE_STATUS_SUPERVISE)) {
			queueStatusSupervise = new NativeButton("队列状态监控");
			queueStatusSupervise.addListener(this);
			constrantLayout.addComponent(queueStatusSupervise);
		}

		// 状态监控
		if (SpringContextHolder.getBusinessModel().contains(
				SUPERVISE_MANAGEMENT_CALL_STATUS_SUPERVISE)) {
			callStatusSupervise = new NativeButton("通话状态监控");
			callStatusSupervise.addListener(this);
			constrantLayout.addComponent(callStatusSupervise);
		}

		// 活动会议室监控
		if (SpringContextHolder.getBusinessModel().contains(
				SUPERVISE_MANAGEMENT_MEETING_ROOM_SUPERVISE)) {
			meetMeSupervise = new NativeButton("活动会议室监控");
			meetMeSupervise.addListener(this);
			constrantLayout.addComponent(meetMeSupervise);
		}

		return constrantLayout;
	}

	// >>>>>>>>>>>>>>>>>>>>>>>>>业务管理<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<//

	/**
	 * chb 业务管理
	 * 
	 * @return
	 */
	private VerticalLayout buildBusinessManagementLayout() {
		VerticalLayout constrantLayout = new VerticalLayout();

		// 商品管理
		if (SpringContextHolder.getBusinessModel().contains(
				BUSINESS_MANAGEMENT_COMMODITY_MANAGE)) {
			commodityManage = new NativeButton("商品管理");
			commodityManage.addListener(this);
			constrantLayout.addComponent(commodityManage);
		}

		// 订单管理
		if (SpringContextHolder.getBusinessModel().contains(
				BUSINESS_MANAGEMENT_ORDER_MANAGE)) {
			orderManage = new NativeButton("订单管理");
			orderManage.addListener(this);
			constrantLayout.addComponent(orderManage);
		}
		// lxy 问卷管理
		if (SpringContextHolder.getBusinessModel().contains(
				BUSINESS_MANAGEMENT_QUESTIONNAIRE_MANAGE)) {
			questionnaireManagement = new NativeButton("问卷管理");
			questionnaireManagement.addListener(this);
			constrantLayout.addComponent(questionnaireManagement);
		}
		// lxy 问卷数据编辑 
		if (SpringContextHolder.getBusinessModel().contains(
				BUSINESS_MANAGEMENT_QUESTIONNAIRE_EDIT_MANAGE)) {
			questionnaireManagementEdit = new NativeButton("问卷编辑导出");
			questionnaireManagementEdit.addListener(this);
			constrantLayout.addComponent(questionnaireManagementEdit);
		}

		return constrantLayout;
	}

	// >>>>>>>>>>>>>>>>>>>>>>>>>短信管理<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<//

	// 短信管理
	private VerticalLayout buildNoteManagementLayout() {
		VerticalLayout constrantLayout = new VerticalLayout();

		if (SpringContextHolder.getBusinessModel().contains(
				PHONE_MESSAGE_MANAGEMENT_TEMPLATE_MANAGEMENT)) {
			templateManage = new NativeButton("模板管理");
			constrantLayout.addComponent(templateManage);
			templateManage.addListener(this);
		}

		if (SpringContextHolder.getBusinessModel().contains(
				PHONE_MESSAGE_MANAGEMENT_SEND_MANAGEMENT)) {
			sendMessage = new NativeButton("短信发送");
			constrantLayout.addComponent(sendMessage);
			sendMessage.addListener(this);
		}

		if (SpringContextHolder.getBusinessModel().contains(
				PHONE_MESSAGE_MANAGEMENT_HISTORY_MANAGEMENT)) {
			historySms = new NativeButton("历史短信");
			constrantLayout.addComponent(historySms);
			historySms.addListener(this);
		}

		return constrantLayout;
	}

	// >>>>>>>>>>>>>>>>>>>>>>>>>知识库管理<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<//

	/**
	 * chb 知识库管理
	 * 
	 * @return
	 */
	private VerticalLayout buildKnowledgeBaseManagementLayout() {
		VerticalLayout constrantLayout = new VerticalLayout();

		// 知识库管理
		if (SpringContextHolder.getBusinessModel().contains(
				KNOWLEDGE_BASE_MANAGEMENT_KNOWLEDGE_MANAGE)) {
			knowledgeManage = new NativeButton("知识库管理");
			knowledgeManage.addListener(this);
			constrantLayout.addComponent(knowledgeManage);
		}

		// if (SpringContextHolder.getBusinessModel().contains(
		// KNOWLEDGE_BASE_MANAGEMENT_KNOWLEDGE_VIEW)) {
		// // 订单管理
		// knowledgeView = new NativeButton("知识库查看");
		// knowledgeView.addListener(this);
		// constrantLayout.addComponent(knowledgeView);
		// }

		return constrantLayout;
	}

	// >>>>>>>>>>>>>>>>>>>>>>>>>邮件管理<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<//
	/**
	 * 邮件管理
	 * 
	 * @return
	 */
	private VerticalLayout buildEmailManagementLayout() {
		VerticalLayout constrantLayout = new VerticalLayout();
		
		// 邮件发送
		if (SpringContextHolder.getBusinessModel().contains(EMAIL_MANAGEMENT_SEND_EMAIL)) {
			sendEmail = new NativeButton("邮件发送");
			sendEmail.addListener(this);
			constrantLayout.addComponent(sendEmail);
		}
		
		// 历史邮件
		if (SpringContextHolder.getBusinessModel().contains(EMAIL_MANAGEMENT_HISTORY_EMAIL)) {
			historyEmail = new NativeButton("历史邮件");
			historyEmail.addListener(this);
			constrantLayout.addComponent(historyEmail);
		}
		
		// 配置邮箱
		if (SpringContextHolder.getBusinessModel().contains(EMAIL_MANAGEMENT_CONFIG_EMAIL)) {
			configEmail = new NativeButton("配置邮箱");
			configEmail.addListener(this);
			constrantLayout.addComponent(configEmail);
		}
		
		return constrantLayout;
	}

	// >>>>>>>>>>>>>>>>>>>>>>>>>系统管理<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<//

	/**
	 * jrh 系统管理
	 * 
	 * @return
	 */
	private VerticalLayout buildSystemManagementLayout() {
		VerticalLayout constrantLayout = new VerticalLayout();

		// 用户管理
		if (SpringContextHolder.getBusinessModel().contains(
				SYSTEM_MANAGEMENT_USER_MANAGE)) {
			userManagement = new NativeButton("用户管理");
			userManagement.addListener(this);
			constrantLayout.addComponent(userManagement);
		}

		// 角色管理
		if (SpringContextHolder.getBusinessModel().contains(
				SYSTEM_MANAGEMENT_ROLE_MANAGE)) {
			roleManagement = new NativeButton("角色管理");
			roleManagement.addListener(this);
			constrantLayout.addComponent(roleManagement);
		}

		// 部门管理
		if (SpringContextHolder.getBusinessModel().contains(
				SYSTEM_MANAGEMENT_DEPARTMENT_MANAGE)) {
			deptManagement = new NativeButton("部门管理");
			deptManagement.addListener(this);
			constrantLayout.addComponent(deptManagement);
		}

		// 分机管理
		if (SpringContextHolder.getBusinessModel().contains(
				SYSTEM_MANAGEMENT_EXTEN_MANAGE)) {
			extManagement = new NativeButton("分机管理");
			extManagement.addListener(this);
			constrantLayout.addComponent(extManagement);
		}

		// 外线管理
		if (SpringContextHolder.getBusinessModel().contains(
				SYSTEM_MANAGEMENT_OUTLINE_MANAGE)) {
			outlineManagement = new NativeButton("外线管理");
			outlineManagement.addListener(this);
			constrantLayout.addComponent(outlineManagement);
		}

		// 队列管理
		if (SpringContextHolder.getBusinessModel().contains(
				SYSTEM_MANAGEMENT_QUEUE_MANAGE)) {
			queueManagement = new NativeButton("队列管理");
			queueManagement.addListener(this);
			constrantLayout.addComponent(queueManagement);
		}

		// 外线成员管理
		if (SpringContextHolder.getBusinessModel().contains(
				SYSTEM_MANAGEMENT_OUTLINE_MEMBER_MANAGE)) {
			userOutlineManagement = new NativeButton("外线成员管理");
			userOutlineManagement.addListener(this);
			constrantLayout.addComponent(userOutlineManagement);
		}
		
		// 外线成员管理
		if (SpringContextHolder.getBusinessModel().contains(
				SYSTEM_MANAGEMENT_OUTLINE_POOL_MANAGE)) {
			userOutlinePoolManagement = new NativeButton("外线池管理");
			userOutlinePoolManagement.addListener(this);
			constrantLayout.addComponent(userOutlinePoolManagement);
		}
		
		// 操作语音导航
		if (SpringContextHolder.getBusinessModel().contains(
				SYSTEM_MANAGEMENT_IVR_MANAGE)) {
			ivrManage = new NativeButton("语音导航流程管理");
			ivrManage.addListener(this);
			constrantLayout.addComponent(ivrManage);
		}
		
		// 语音文件夹管理
		if (SpringContextHolder.getBusinessModel().contains(
				SYSTEM_MANAGEMENT_MOH_MANAGE)) {
			musicOnHoldManagement = new NativeButton("保持音乐管理");
			musicOnHoldManagement.addListener(this);
			constrantLayout.addComponent(musicOnHoldManagement);
		}
		
		// 动态队列管理
		if (SpringContextHolder.getBusinessModel().contains(
				SYSTEM_MANAGEMENT_DYNAMIC_QUEUE_MEMBER_MANAGE)) {
			dynQueueMemberManagement = new NativeButton("动态队列成员管理");
			dynQueueMemberManagement.addListener(this);
			constrantLayout.addComponent(dynQueueMemberManagement);
		}

		// 动态队列管理
		if (SpringContextHolder.getBusinessModel().contains(
				SYSTEM_MANAGEMENT_STATIC_QUEUE_MEMBER_MANAGE)) {
			staticQueueMemberManagement = new NativeButton("静态队列成员管理");
			staticQueueMemberManagement.addListener(this);
			constrantLayout.addComponent(staticQueueMemberManagement);
		}

		// 内部配置管理 Excel配置
		if (SpringContextHolder.getBusinessModel().contains(
				SYSTEM_MANAGEMENT_INTERNAL_SETTING_MANAGE)) {
			innerConfig = new NativeButton("内部配置管理");
			innerConfig.addListener(this);
			constrantLayout.addComponent(innerConfig);
		}

		// 客服记录状态配置管理
		if (SpringContextHolder.getBusinessModel().contains(
				SYSTEM_MANAGEMENT_SERVICE_RECORD_STATUS_MANAGE)) {
			serviceRecordStatusManagement = new NativeButton("客服记录状态管理");
			serviceRecordStatusManagement.addListener(this);
			constrantLayout.addComponent(serviceRecordStatusManagement);
		}
		
		// 客服记录状态按键管理
		if(SpringContextHolder.getBusinessModel().contains(SYSTEM_MANAGEMENT_CUSTOMER_SERVICE_RECORD_STATUS_NAVIGATION_KEY)){
			customerServiceRecordStatusNavigationKeyManagement = new NativeButton("客服记录状态按键管理");
			customerServiceRecordStatusNavigationKeyManagement.addListener(this);
			constrantLayout.addComponent(customerServiceRecordStatusNavigationKeyManagement);
		}

		// 值班表管理
		if (SpringContextHolder.getBusinessModel().contains(
				SYSTEM_MANAGEMENT_DUTY_REPORT_MANAGE)) {
			// TODO this model need to add in future
			// dutyTableManagement=new NativeButton("值班表管理");
			// dutyTableManagement.addListener(this);
			// constrantLayout.addComponent(dutyTableManagement);
		}

		// 外传外配置管理
		if (SpringContextHolder.getBusinessModel().contains(
				SYSTEM_MANAGEMENT_MGR_PHONE2PHONE_SETTING_MANAGE)) {
			mgrPhone2phoneSettingManagement = new NativeButton("外转外配置管理");
			mgrPhone2phoneSettingManagement.addListener(this);
			constrantLayout.addComponent(mgrPhone2phoneSettingManagement);
		}

		// 管理员会议室
		if (SpringContextHolder.getBusinessModel().contains(
				SYSTEM_MANAGEMENT_MGR_MEET_ME)) {
			mgrMeetMe = new NativeButton("管理员会议室");
			mgrMeetMe.addListener(this);
			constrantLayout.addComponent(mgrMeetMe);
		}

		// 操作日志查看
		if (SpringContextHolder.getBusinessModel().contains(
				SYSTEM_MANAGEMENT_OPERATION_LOG_VIEW)) {
			operationLogView = new NativeButton("操作日志查看");
			operationLogView.addListener(this);
			constrantLayout.addComponent(operationLogView);
		}

		// 操作语音导航
		if (SpringContextHolder.getBusinessModel().contains(
				SYSTEM_MANAGEMENT_BLACKLIST_MANAGE)) {
			blacklistManagement = new NativeButton("黑名单管理");
			blacklistManagement.addListener(this);
			constrantLayout.addComponent(blacklistManagement);
		}

		// 拨号方案管理
		if (SpringContextHolder.getBusinessModel().contains(
				SYSTEM_MANAGEMENT_SOFTPHONE_DIALPLAN_MANAGE)) {
			solfphoneDialplanManagement = new NativeButton("拨号方案详情");
			solfphoneDialplanManagement.addListener(this);
			constrantLayout.addComponent(solfphoneDialplanManagement);
		}
		
		//满意度调查按键管理
		if (SpringContextHolder.getBusinessModel().contains(
				SYSTEM_MANAGEMENT_SATIS_NUM_MANAGE)) {
			satisNumManager = new NativeButton("满意度调查按键管理");
			satisNumManager.addListener(this);
			constrantLayout.addComponent(satisNumManager);
		}
		
		//系统首页
		if (SpringContextHolder.getBusinessModel().contains(SYSTEM_MANAGEMENT_FIRSTMANAGEMENT)) {
			//TODO 去掉多租户的限制 	if(ShareData.domainList.size() == 1){//单域情况下才可以使用该功能,多域不可用
				firstManagement = new NativeButton("系统首页");
				firstManagement.addListener(this);
				constrantLayout.addComponent(firstManagement);
			//}
		}
		return constrantLayout;
	}

	// >>>>>>>>>>>>>>>>>>>>>>>>>系统信息<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<//

	/**
	 * chb 系统信息
	 * 
	 * @return
	 */
	private VerticalLayout buildSystemInfoLayout() {
		VerticalLayout constrantLayout = new VerticalLayout();

		// 系统状态
		if (SpringContextHolder.getBusinessModel().contains(SYSTEM_INFO_MANAGEMENT_SYSTEM_STATUS)) {
			systemStatus = new NativeButton("系统状态");
			systemStatus.addListener(this);
			constrantLayout.addComponent(systemStatus);
		}

		//TODO chb: systemInfo 需要添加权限
		if (SpringContextHolder.getBusinessModel().contains(SYSTEM_INFO_MANAGEMENT_SYSTEM_INFO)) {
			systemInfo= new NativeButton("系统信息");
			systemInfo.addListener(this);
			constrantLayout.addComponent(systemInfo);
		}
		
		// Licence信息
		if (SpringContextHolder.getBusinessModel().contains(SYSTEM_INFO_MANAGEMENT_SYSTEM_LISCENCE)) {
			systemLiscence = new NativeButton("Licence信息");
			systemLiscence.addListener(this);
			constrantLayout.addComponent(systemLiscence);
		}

		// 关于
		if (SpringContextHolder.getBusinessModel().contains(SYSTEM_INFO_MANAGEMENT_ABOUT_US)) {
			aboutUs = new NativeButton("关于我们");
			aboutUs.addListener(this);
			constrantLayout.addComponent(aboutUs);
		}

		return constrantLayout;
	}

	/**
	 * 监听按钮单击事件
	 */
	@Override
	public void buttonClick(ClickEvent event) {
		Button source = event.getButton();
		try {
			if (source == resourceImport) { // 数据管理模块
				mgrTabSheet.showResourceImport();
			} else if (source == globalSearch) {
				mgrTabSheet.showGlobalSearch();
			} else if (source == serviceRecord) {
				mgrTabSheet.showServiceRecord();
			} else if (source == callRecordManage) {
				mgrTabSheet.showCallRecordManage();
			} else if (source == missCallLogManage) {
				mgrTabSheet.showMissCallLogManage();
			} else if (source == meettingDetailRecordManage) {
				mgrTabSheet.showMeettingDetailRecordView();
			} else if (source == resourceManage) {
				mgrTabSheet.showResourceManage();
			} else if (source == projectControl) { // 项目管理模块
				mgrTabSheet.showProjectControl();
			} else if (source == autoDialout) { // 自动外呼管理模块
				mgrTabSheet.showAutoDialout();
			} else if (source == soundDialout) {
				mgrTabSheet.showSoundDialout();
			} else if (source == soundUpload) {
				mgrTabSheet.showSoundUpload();
			} else if (source == resourceRecycle) {
				mgrTabSheet.showResourceRecycle();
			} else if (source == noticeSend) { // 消息管理模块
				mgrTabSheet.showNoticeSend();
			} else if (source == historyNotice) {
				mgrTabSheet.showHistoryNotice(true);
			} else if (source == csrTimersOrderManage) {
				mgrTabSheet.showCsrTimersOrderManage();
			} else if (source == callStatisticOverview_nb) { // 报表管理模块
				mgrTabSheet.showCallStatisticOverviewReport();
			} else if(source == customerServiceRecordStatisticalView){	// 客服记录统计
				mgrTabSheet.showCustomerServiceRecordStatisticalView();
			} else if (source == projectFinishedStatus) { 
				mgrTabSheet.showProjectFinishedStatus();
			} else if (source == serviceRecordStatus) {
				mgrTabSheet.showServiceRecordStatus();
			} else if (source == callCheck) {
				mgrTabSheet.showCallCheck();
			} else if (source == callCountCheck) {
				mgrTabSheet.showCallCheckByCallTimeLength();
			} else if (source == employeeCheck) {
				mgrTabSheet.showEmployeeCheck();
			} else if (source == loginLogoutDetail) {
				mgrTabSheet.showEmployeeLoginLogoutDetail();
			} else if (source == concurrentStatics) {
				mgrTabSheet.showConcurrentStatics();
			}

			else if (source == queueDetail) {
				mgrTabSheet.showQueueDetail();
			} else if (source == autodialDetail) {
				mgrTabSheet.showAutodialDetail();
			} else if (source == satisfactionInvestigate) {
				mgrTabSheet.showSatisfactionInvestigate();
			} else if (source == kpi) {
				mgrTabSheet.showKpi();
			} else if (source == advanceKpi_nb) {
				mgrTabSheet.showAdvanceKpiReportView();
			} else if (source == projectPool) {
				mgrTabSheet.showProjectPool();
			} else if (source == workflowTransferLog_nb) {
				mgrTabSheet.showWorkflowTransferLog();
			} else if (source == questionnaire) {
				mgrTabSheet.showQuestionnaireDetail();
			} else if (source == customerDetail) {
				mgrTabSheet.showCustomerDetail();
			} else if (source == businessDetail) {
				mgrTabSheet.showBusinessDetail();
			} else if(source == csrWorkDetail){
				mgrTabSheet.showCsrWorkDetail();
			} else if (source == pauseDetail) {
				mgrTabSheet.showPausesDetail();
			} else if(source == pauseDetailStatistics) {
				mgrTabSheet.showPauseDetailStatistics();
			}
			
			else if (source == customerMemberManage) { // 客户管理模块
				mgrTabSheet.showCustomerMemberManagement();
			} else if (source == customerMigrateLogMange) {
				mgrTabSheet.showCustomerMigrateLogView();
			} else if (source == employeeStatusSupervise) { // 实时监控管理模块
				mgrTabSheet.showEmployeeStatusSupervise();
			} else if (source == sipStatusSupervise) {
				mgrTabSheet.showExtenStatusSupervise();
			} else if (source == queueStatusSupervise) {
				mgrTabSheet.showQueueStatusSupervise();
			} else if (source == callStatusSupervise) {
				mgrTabSheet.showCallStatusSupervise();
			} else if (source == meetMeSupervise) {
				mgrTabSheet.showMeetMeSupervise();
			} else if (source == userManagement) { // 系统管理模块
				mgrTabSheet.showUserManagement();
			} else if (source == roleManagement) {
				mgrTabSheet.showRoleManagement();
			} else if (source == deptManagement) {
				mgrTabSheet.showDeptManagement();
			} else if (source == extManagement) {
				mgrTabSheet.showExtManagement();
			} else if (source == outlineManagement) {
				mgrTabSheet.showOutlineManagement();
			} else if (source == userOutlineManagement) {
				mgrTabSheet.showUserOutlineManagement();
			} else if (source == userOutlinePoolManagement) {
				mgrTabSheet.showUserOutlinePoolManagement();
			} else if (source == queueManagement) {
				mgrTabSheet.showQueueManagement();
			} else if (source == musicOnHoldManagement) {
				mgrTabSheet.showMusicOnHoldManagement();
			} else if (source == dynQueueMemberManagement) {
				mgrTabSheet.showDynQueueMemberManagement();
			} else if (source == staticQueueMemberManagement) {
				mgrTabSheet.showStaticQueueMemberManagement();
			} else if (source == innerConfig) {
				mgrTabSheet.showInnerConfig();
			} else if (source == serviceRecordStatusManagement) {
				mgrTabSheet.showRecordStatusManagement();
			} else if (source == dutyTableManagement) {
				mgrTabSheet.showDutyTableManagement();
			} else if (source == mgrPhone2phoneSettingManagement) {
				mgrTabSheet.showMgrPhone2PhoneSettingManagement();
			} else if (source == blacklistManagement) {
				mgrTabSheet.showBlacklistManagement();
			} else if (source == solfphoneDialplanManagement) {
				mgrTabSheet.showDialPlanManagement();
			} else if(source == satisNumManager){
				mgrTabSheet.showSatisNumManagement();
			} else if(source == customerServiceRecordStatusNavigationKeyManagement){
				mgrTabSheet.showCustomerServiceRecordStatusNavigationKeyManagement();
			} else if (source == systemStatus) { // 系统信息模块
				mgrTabSheet.showSystemStatusManagement();
			} else if (source == systemInfo) { 
				mgrTabSheet.showSystemInfoManagement();
			} else if (source == systemLiscence) {
				mgrTabSheet.showSystemLicenceManagement();
			} else if (source == aboutUs) {
				mgrTabSheet.showAboutManagement();
			} else if (source == voiceMail) {
				mgrTabSheet.showVoiceMailManagement();
			} else if (source == commodityManage) {// 商品订单管理
				mgrTabSheet.showCommodityManagement();
			} else if (source == orderManage) {
				mgrTabSheet.showOrderManagement();
			} else if (source == questionnaireManagement) {// 问卷管理
				mgrTabSheet.showQuestionnaireManagement();
			} else if (source == questionnaireManagementEdit) {// 问卷数据编辑
				mgrTabSheet.showQuestionnaireManagementEdit();
			} else if (source == mgrMeetMe) {
				mgrTabSheet.showMgrMeetMe();
			} else if (source == operationLogView) {
				mgrTabSheet.showOperationLogView();
			} else if (source == sendMessage) { // 短信管理
				mgrTabSheet.showSendMessageManage();
			} else if (source == templateManage) { // 模板管理
				mgrTabSheet.showTemplateManage();
			} else if (source == historySms) {
				mgrTabSheet.showHistorySms();
			} else if (source == ivrManage) {
				mgrTabSheet.showIvrManagement();
			} else if (source == knowledgeManage) { // 知识库管理部分// 模板管理
				mgrTabSheet.showKnowledgeManage();
			} else if (source == sendEmail) { // 邮件管理部分
				mgrTabSheet.showSendEmailManage();
			} else if (source == historyEmail) { 
				mgrTabSheet.showHistoryEmailManage();
			} else if (source == configEmail) { 
				mgrTabSheet.showConfigEmailManage();
			}else if (source == firstManagement){ //系统首页
				mgrTabSheet.showFirstManagement();
			}
			
		} catch (Exception e) {
			logger.error("manager 单击accordion 中按钮时出现异常 --> " + e.getMessage(),
					e);
		}
	}

	// 供MgrCenter调用来使Accordion持有MgrTabSheet引用
	public void setMgrTabSheet(MgrTabSheet mgrTabSheet) {
		this.mgrTabSheet = mgrTabSheet;
		if (SpringContextHolder.getBusinessModel().contains(SYSTEM_MANAGEMENT_FIRSTMANAGEMENT)) {//如果有系统首页权限，登录是，系统首先显示
			mgrTabSheet.showFirstManagement();
		}
	}

	// TODO
	@Override
	public void attach() {
//		queueManagement.click();
//		ivrManage.click();
	}

}
