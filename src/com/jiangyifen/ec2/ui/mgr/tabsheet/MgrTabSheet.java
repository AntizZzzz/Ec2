package com.jiangyifen.ec2.ui.mgr.tabsheet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.globaldata.ResourceDataCsr;
import com.jiangyifen.ec2.globaldata.ResourceDataMgr;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.ui.csr.workarea.sms.SendMutiMessageView;
import com.jiangyifen.ec2.ui.mgr.system.tabsheet.About;
import com.jiangyifen.ec2.ui.mgr.system.tabsheet.CustomerServiceRecordStatusManagement;
import com.jiangyifen.ec2.ui.mgr.system.tabsheet.CustomerServiceRecordStatusNavigationKeyManagement;
import com.jiangyifen.ec2.ui.mgr.system.tabsheet.DeptManagement;
import com.jiangyifen.ec2.ui.mgr.system.tabsheet.DialplanInfoView;
import com.jiangyifen.ec2.ui.mgr.system.tabsheet.DutyTableManagement;
import com.jiangyifen.ec2.ui.mgr.system.tabsheet.DynQueueMemberManagement;
import com.jiangyifen.ec2.ui.mgr.system.tabsheet.ExtManagement;
import com.jiangyifen.ec2.ui.mgr.system.tabsheet.FirstManagement;
import com.jiangyifen.ec2.ui.mgr.system.tabsheet.InnerConfigManagement;
import com.jiangyifen.ec2.ui.mgr.system.tabsheet.MgrPhone2PhoneSettingView;
import com.jiangyifen.ec2.ui.mgr.system.tabsheet.MusicOnHoldManagement;
import com.jiangyifen.ec2.ui.mgr.system.tabsheet.OperationLogView;
import com.jiangyifen.ec2.ui.mgr.system.tabsheet.OutlineManagement;
import com.jiangyifen.ec2.ui.mgr.system.tabsheet.QueueManagement;
import com.jiangyifen.ec2.ui.mgr.system.tabsheet.RoleManagement;
import com.jiangyifen.ec2.ui.mgr.system.tabsheet.StaticQueueMemberManagement;
import com.jiangyifen.ec2.ui.mgr.system.tabsheet.SystemInfo;
import com.jiangyifen.ec2.ui.mgr.system.tabsheet.SystemLicence;
import com.jiangyifen.ec2.ui.mgr.system.tabsheet.SystemStatus;
import com.jiangyifen.ec2.ui.mgr.system.tabsheet.UserManagement;
import com.jiangyifen.ec2.ui.mgr.system.tabsheet.UserOutlineManagement;
import com.jiangyifen.ec2.ui.mgr.system.tabsheet.UserOutlinePoolManagement;
import com.jiangyifen.ec2.ui.mgr.system.tabsheet.ivr.IvrManagement;
import com.jiangyifen.ec2.ui.mgr.tabsheet.blacklist.BlacklistView;
import com.jiangyifen.ec2.ui.mgr.tabsheet.csrtimers.CsrTimersOrderManagement;
import com.jiangyifen.ec2.ui.mgr.tabsheet.email.EmailConfigView;
import com.jiangyifen.ec2.ui.mgr.tabsheet.email.EmailHistoryView;
import com.jiangyifen.ec2.ui.mgr.tabsheet.email.EmailSendView;
import com.jiangyifen.ec2.ui.mgr.tabsheet.meettingrecord.MeettingDetailRecordManagement;
import com.jiangyifen.ec2.ui.mgr.tabsheet.misscalllog.MissCallLogManagement;
import com.jiangyifen.ec2.ui.mgr.tabsheet.supervise.CallStatusSupervise;
import com.jiangyifen.ec2.ui.mgr.tabsheet.supervise.EmployeeStatusSupervise;
import com.jiangyifen.ec2.ui.mgr.tabsheet.supervise.MeetMeSupervise;
import com.jiangyifen.ec2.ui.mgr.tabsheet.supervise.QueueStatusSupervise;
import com.jiangyifen.ec2.ui.mgr.tabsheet.supervise.SipStatusSupervise;
import com.jiangyifen.ec2.ui.mgr.tabsheet.voicemail.VoiceMailManagement;
import com.jiangyifen.ec2.ui.report.tabsheet.AutodialDetail;
import com.jiangyifen.ec2.ui.report.tabsheet.BusinessDetail;
import com.jiangyifen.ec2.ui.report.tabsheet.CallCheck;
import com.jiangyifen.ec2.ui.report.tabsheet.CallCheckByCallTimeLength;
import com.jiangyifen.ec2.ui.report.tabsheet.ConcurrentStatics;
import com.jiangyifen.ec2.ui.report.tabsheet.CsrWork;
import com.jiangyifen.ec2.ui.report.tabsheet.CustomerDetail;
import com.jiangyifen.ec2.ui.report.tabsheet.CustomerSatisfactionInvestigate;
import com.jiangyifen.ec2.ui.report.tabsheet.EmployeeCheck;
import com.jiangyifen.ec2.ui.report.tabsheet.EmployeeLoginLogoutDetail;
import com.jiangyifen.ec2.ui.report.tabsheet.KPI;
import com.jiangyifen.ec2.ui.report.tabsheet.PauseDetailReport;
import com.jiangyifen.ec2.ui.report.tabsheet.PauseDetailStatisticsReport;
import com.jiangyifen.ec2.ui.report.tabsheet.ProjectFinishedStatus;
import com.jiangyifen.ec2.ui.report.tabsheet.ProjectPool;
import com.jiangyifen.ec2.ui.report.tabsheet.QuestionnaireDetail;
import com.jiangyifen.ec2.ui.report.tabsheet.QueueDetail;
import com.jiangyifen.ec2.ui.report.tabsheet.ServiceRecordStatus;
import com.jiangyifen.ec2.ui.report.tabsheet.WorkflowTransferLogView;
import com.jiangyifen.ec2.ui.report.tabsheet.advkpi.AdvanceKPIReportView;
import com.jiangyifen.ec2.ui.report.tabsheet.callstatistic.CallStatisticNativeSqlOverviewReport;
import com.jiangyifen.ec2.ui.report.tabsheet.statistical.CustomerServiceRecordStatisticalView;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.ui.Component;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.SelectedTabChangeListener;

@SuppressWarnings("serial")
public class MgrTabSheet extends TabSheet implements SelectedTabChangeListener {
	// 日志服务类
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	/**
	 * 基本信息管理模块
	 */
	// 资源导入Tab页
	private ResourceImport resourceImport;

	// 全局搜索Tab页
	private GlobalSearch globalSearch;

	// 客服记录Tab页
	private MgrServiceRecordAllView mgrServiceRecordView;

	// 项目控制Tab页
	private ProjectControl projectControl;

	// 自动外呼Tab页
	private AutoDialout autoDialout;

	// 自动外呼的资源回收Tab页
	private ResourceRecycle resourceRecycle;

	// 语音群发Tab页
	private SoundDialout soundDialout;

	// 语音上传Tab页
	private SoundUpload soundUpload;

	// 内部配置管理Tab页
	private InnerConfigManagement innerConfig;

	// 客服记录状态管理界面Tab页
	private CustomerServiceRecordStatusManagement serviceRecordStatusManagement;

	// 消息发送Tab页
	private NoticeSend noticeSend;

	// 历史消息Tab页
	private HistoryNotice historyNotice;
	private boolean isSend; // 如果是发送消息，则刷新到首页，否则刷新当前页

	// 坐席定时预约详情
	private CsrTimersOrderManagement csrTimersOrderManagement;
	
	// ---------------报表Tab页---------------------
	// TODO 报表
	// 话务考核
	private CallCheck callCheck;

	// 按通话时长考核话务
	private CallCheckByCallTimeLength callCheckByCallTimeLength;

	// 人员考核
	private EmployeeCheck employeeCheck;

	// 员工上下线祥单
	private EmployeeLoginLogoutDetail loginLogoutDetail;

	// 并发统计
	private ConcurrentStatics concurrentStatics;

	// 话务统计总概览报表Tab页
	private CallStatisticNativeSqlOverviewReport callStatisticNativeSqlOverviewReport;

	// 项目完成情况Tab页
	private ProjectFinishedStatus projectFinishedStatus;

	// 客服记录情况Tab页
	private ServiceRecordStatus serviceRecordStatus;

	// 队列详情
	private QueueDetail queueDetail;

	// 自动外呼详情
	private AutodialDetail autodialDetail;

	// 满意度调查
	private CustomerSatisfactionInvestigate satisfactionInvestigate;

	// TODO JHT 客服记录统计
	private CustomerServiceRecordStatisticalView customerServiceRecordStatisticalView;
	
	// kpi
	private KPI kpi;
	
	// advanceKPIReportView
	private AdvanceKPIReportView advanceKPIReportView;

	// projectPool
	private ProjectPool projectPool;

	// private TransferLog transferLog;

	private WorkflowTransferLogView workflowTransferLogView;

	// 问卷
	private QuestionnaireDetail questionnaire;

	// 明细
	private CustomerDetail customerDetail;

	// 业务报表
	private BusinessDetail businessDetail;

	// 座席工作详情
	private CsrWork csrWorkDetail;
	
	//置忙报表
	private PauseDetailReport pauseDetail;
	
	// 置忙详情统计报表
	private PauseDetailStatisticsReport pauseDetailStatisticsReport;
	
	/**
	 * 呼叫记录管理
	 */
	// "呼叫记录" Tab页管理模块
	private CallRecordManagement callRecordManagement;
	// 电话漏接记录详情
	private MissCallLogManagement missCallLogManagement;
	// "多方通话记录管理" Tab页管理模块
	private MeettingDetailRecordManagement meettingDetailRecordManagement;
	// 资源管理
	private ResourceManage resourceManage;

	/**
	 * 客户管理模块
	 */
	// 坐席状态监控
	private CustomerMemberManagement customerMemberManagement;
	// 客户迁移日志
	private CustomerMigrateLogView customerMigrateLogView;

	/**
	 * 实时监控模块
	 */
	// 坐席状态监控
	private EmployeeStatusSupervise employeeStatusSupervise;
	// 分机状态监控
	private SipStatusSupervise sipStatusSupervise;
	// 队列状态监控
	private QueueStatusSupervise queueStatusSupervise;
	// 通话状态监控
	private CallStatusSupervise callStatusSupervise;
	// 活动会议室监控
	private MeetMeSupervise meetMeSupervise;

	/**
	 * 商品订单管理
	 */
	// 商品管理Tab页
	private CommodityManagement commodityManagement;
	// 订单管理Tab页
	private OrderManagement orderManagement;
	// 问卷调查管理Tab页
	private QuestionnaireManagement questionnaireManagement;
	// 问卷数据编辑Tab页
	private QuestionnaireManagementEdit questionnaireManagementEdit;

	/**
	 * 短信管理模块
	 */
	// 模板管理Tab页
	private MessageTemplateManage templateManage;
	// 短信发送管理Tab页
//	private SendMessageManage sendMessageManage;
	private SendMutiMessageView sendMutiMessageView;
	// 历史短消息查看
	private MessageShow historySms;

	/**
	 * 知识库管理模块
	 */
	// 知识库管理
	// private KnowledgeManagement knowledgeManagement;
	private KbInfoManagement kbInfoManagement;
	// 知识库查看
	private KnowledgeView knowledgeView;

	/**
	 * 邮件模块
	 */
	private EmailSendView emailSendView;
	private EmailHistoryView emailHistoryView;
	private EmailConfigView emailConfigView;

	/**
	 * 系统信息管理模块
	 */
	// 用户管理 Tab页
	private UserManagement userManagement;
	// 角色管理 Tab页
	private RoleManagement roleManagement;
	// 部门管理 Tab页
	private DeptManagement deptManagement;
	// 分机管理 Tab页
	private ExtManagement extManagement;
	// 分机管理 Tab页
	private OutlineManagement outlineManagement;
	// 外线成员管理 Tab页
	private UserOutlineManagement userOutlineManagement;
	// 外线成员管理 Tab页
	private UserOutlinePoolManagement outlinePoolManagement;
	// 队列管理 Tab页
	private QueueManagement queueManagement;
	// 语音导航
	private IvrManagement ivrManagement;
	// 语音文件夹管理 Tab页
	private MusicOnHoldManagement musicOnHoldManagement;
	// 动态队列成员管理 Tab页
	private DynQueueMemberManagement dynQueueMemberManagement;
	// 静态队列成员管理 Tab页
	private StaticQueueMemberManagement staticQueueMemberManagement;
	// 值班表管理 Tab页
	private DutyTableManagement dutyTableManagement;
	// 外转外配置 Tab页
	private MgrPhone2PhoneSettingView mgrPhone2PhoneSettingView;
	// 黑名单管理 Tab页
	private BlacklistView blacklistView;
	// 软电话拨号方案 Tab页
	private DialplanInfoView dialplanInfoView;
	//满意度调查按键管理
	private SatisNumManager satisNumManager;
	// 管理员会议室
	private MgrMeetMe mgrMeetMe;
	// 操作日志查看
	private OperationLogView operationLogView;
	// 客服记录状态按键管理
	private CustomerServiceRecordStatusNavigationKeyManagement customerServiceRecordStatusNavigationKeyManagement;
	// 系统首页 Tab页
	private FirstManagement firstManagement;
	/**
	 * 系统信息
	 */
	// 系统状态管理Tab页
	private SystemStatus systemStatusManagement;

	// 系统信息管理 供用户配置
	private SystemInfo systemInfoManagement;

	// 系统License管理 Tab页
	private SystemLicence systemLicenceManagement;

	// AboutTab页
	private About aboutManagement;
	
	private VoiceMailManagement voiceMailManagement;

	// 当前选择的Tab
	private Tab selectTab;

	private User loginUser;

	public MgrTabSheet() {
		loginUser = SpringContextHolder.getLoginUser();
		ShareData.mgrToTabSheet.put(loginUser.getId(), this);
		this.addListener(this);
		this.setImmediate(true);
		this.setCloseHandler(new CloseHandler() {

			// lc 关闭tab时,如果是报表tab,则把报表tab中的子tab全部清空.其它tab,直接关闭,不做任何操作
			@Override
			public void onTabClose(TabSheet tabsheet, Component tabContent) {
				if (tabContent == callCheck) {

					callCheck.getTabSheet().removeAllComponents();
					tabsheet.removeComponent(callCheck);

				} else if (tabContent == callCheckByCallTimeLength) {

					callCheckByCallTimeLength.getTabSheet()
							.removeAllComponents();
					tabsheet.removeComponent(callCheckByCallTimeLength);

				} else if (tabContent == employeeCheck) {

					employeeCheck.getTabSheet().removeAllComponents();
					tabsheet.removeComponent(employeeCheck);

				} else if (tabContent == loginLogoutDetail) {

					loginLogoutDetail.getTabSheet().removeAllComponents();
					tabsheet.removeComponent(loginLogoutDetail);

				} else if (tabContent == concurrentStatics) {

					concurrentStatics.getTabSheet().removeAllComponents();
					tabsheet.removeComponent(concurrentStatics);

				} else if (tabContent == projectFinishedStatus) {

					projectFinishedStatus.getTabSheet().removeAllComponents();
					tabsheet.removeComponent(projectFinishedStatus);

				} else if (tabContent == serviceRecordStatus) {

					serviceRecordStatus.getTabSheet().removeAllComponents();
					tabsheet.removeComponent(serviceRecordStatus);

				}

				else if (tabContent == queueDetail) {

					queueDetail.getTabSheet().removeAllComponents();
					tabsheet.removeComponent(queueDetail);

				}

				else if (tabContent == autodialDetail) {
					autodialDetail.getTabSheet().removeAllComponents();
					tabsheet.removeComponent(autodialDetail);
				}

				else if (tabContent == satisfactionInvestigate) {
					satisfactionInvestigate.getTabSheet().removeAllComponents();
					tabsheet.removeComponent(satisfactionInvestigate);
				}

				else if (tabContent == kpi) {
					kpi.getTabSheet().removeAllComponents();
					tabsheet.removeComponent(kpi);

				}

				else if (tabContent == projectPool) {
					projectPool.getTabSheet().removeAllComponents();
					tabsheet.removeComponent(projectPool);
				}
				// jrh 已经不用了 [山西焦炭] 工作流迁移日志
				// else if (tabContent == transferLog) {
				// transferLog.getTabSheet().removeAllComponents();
				// tabsheet.removeComponent(transferLog);
				// }

				else if (tabContent == questionnaire) {
					questionnaire.getTabSheet().removeAllComponents();
					tabsheet.removeComponent(questionnaire);
				}

				else if (tabContent == customerDetail) {
					customerDetail.getTabSheet().removeAllComponents();
					tabsheet.removeComponent(customerDetail);
				}

				else if (tabContent == businessDetail) {
					businessDetail.getTabSheet().removeAllComponents();
					tabsheet.removeComponent(businessDetail);
				}

				else if (tabContent == csrWorkDetail) {
					csrWorkDetail.getTabSheet().removeAllComponents();
					tabsheet.removeComponent(csrWorkDetail);
				}

				// jrh 解决点击项目界面，界面空白问题
				else if (tabContent == projectControl) {
					tabsheet.removeComponent(projectControl);
					projectControl = null;
				}

				else {
					tabsheet.removeComponent(tabContent);
					// 停线程
					stopSupperviceThread(tabContent, "onTabClose");
				}

			}
		});
	}

	/****************** 数据管理 *****************/

	public void showResourceImport() {
		if (resourceImport == null) {
			resourceImport = new ResourceImport();
		} else {
			resourceImport.updateTable(false);
		}

		selectTab = this.addTab(resourceImport, "资源导入", ResourceDataMgr.resource_import);
		selectTab.setClosable(true);
		this.setSelectedTab(selectTab);
	}

	public void showGlobalSearch() {
		if (globalSearch == null) {
			globalSearch = new GlobalSearch();
		}

		selectTab = this.addTab(globalSearch, "全局搜索", ResourceDataMgr.global_search);
		selectTab.setClosable(true);
		this.setSelectedTab(selectTab);
	}

	public void showServiceRecord() {
		if (mgrServiceRecordView == null) {
			mgrServiceRecordView = new MgrServiceRecordAllView();
		}
		selectTab = this.addTab(mgrServiceRecordView, "客服记录", ResourceDataMgr.service_record);
		selectTab.setClosable(true);
		this.setSelectedTab(selectTab);
	}

	public void showCallRecordManage() {
		if (callRecordManagement == null) {
			callRecordManagement = new CallRecordManagement();
		}
		selectTab = this.addTab(callRecordManagement, "呼叫记录", ResourceDataMgr.call_record);
		selectTab.setClosable(true);
		this.setSelectedTab(selectTab);
	}
	
	public void showMissCallLogManage() {
		if (missCallLogManagement == null) {
			missCallLogManagement = new MissCallLogManagement();
		}
		selectTab = this.addTab(missCallLogManagement, "电话漏接记录", ResourceDataMgr.call_record);
		selectTab.setClosable(true);
		this.setSelectedTab(selectTab);
	}
	
	public void showMeettingDetailRecordView() {
		if (meettingDetailRecordManagement == null) {
			meettingDetailRecordManagement = new MeettingDetailRecordManagement();
		}
		selectTab = this.addTab(meettingDetailRecordManagement, "多方通话记录", ResourceDataMgr.call_record);
		selectTab.setClosable(true);
		this.setSelectedTab(selectTab);
	}
	
	public void showResourceManage() {
		/*Tab tab = this.getTab(resourceManage);
		if(tab == null) {
			resourceManage = new ResourceManage();
		}*/
		if(resourceManage == null) {
			resourceManage = new ResourceManage();
		}
		
		selectTab = this.addTab(resourceManage, "资源管理", ResourceDataMgr.resource_manage_16_ico);
		selectTab.setClosable(true);
		this.setSelectedTab(resourceManage);
	}

	/****************** 项目管理模块 *****************/

	public void showProjectControl() {
		if (projectControl == null) {
			projectControl = new ProjectControl();
		}
		selectTab = this.addTab(projectControl, "项目控制",
				ResourceDataMgr.project_control);
		selectTab.setClosable(true);
		this.setSelectedTab(selectTab);
	}

	/****************** 自动外呼管理模块 *****************/

	public void showAutoDialout() {
		if (autoDialout == null) {
			autoDialout = new AutoDialout();
		}

		selectTab = this.addTab(autoDialout, "自动外呼",
				ResourceDataMgr.auto_dialout);
		selectTab.setClosable(true);
		this.setSelectedTab(selectTab);
	}

	/**
	 * 显示资源回收组件
	 */
	public void showResourceRecycle() {
		// TODO
		if (resourceRecycle == null) {
		// 为解决刷新组件内的数据源，暂时每次都刷新组件
			resourceRecycle = new ResourceRecycle();
		}

		selectTab = this.addTab(resourceRecycle, "资源回收", ResourceDataMgr.resource_resycle_16_ico);
		selectTab.setClosable(true);
		this.setSelectedTab(selectTab);
	}

	/**
	 * 显示语音群发
	 */
	public void showSoundDialout() {
		if (soundDialout == null) {
			soundDialout = new SoundDialout();
		}

		selectTab = this.addTab(soundDialout, "语音群发", ResourceDataMgr.sound_dialout);
		selectTab.setClosable(true);
		this.setSelectedTab(selectTab);
	}

	/**
	 * 显示语音上传
	 */
	public void showSoundUpload() {
		if (soundUpload == null) {
			soundUpload = new SoundUpload();
		}

		selectTab = this.addTab(soundUpload, "语音上传", ResourceDataMgr.sound_upload);
		selectTab.setClosable(true);
		this.setSelectedTab(selectTab);
	}

	/****************** 自动外呼管理模块 *****************/

	public void showNoticeSend() {
		if (noticeSend == null) {
			noticeSend = new NoticeSend(this);
		}/* else {
			// 发送消息的页面进行保留，保留Csr、消息、标题
			noticeSend.updateCsrInfo();
		}*/
		selectTab = this.addTab(noticeSend, "消息发送", ResourceDataMgr.message_send);
		selectTab.setClosable(true);
		this.setSelectedTab(selectTab);
	}

	public void showHistoryNotice(Boolean isSend) {// 如果是发送消息，则刷新到首页，否则刷新当前页
		if (historyNotice == null) {
			historyNotice = new HistoryNotice(this);
		}

		this.isSend = isSend;
		selectTab = this.addTab(historyNotice, "历史消息", ResourceDataMgr.history_message);
		selectTab.setClosable(true);
		this.setSelectedTab(selectTab);
	}
	
	public void showCsrTimersOrderManage() { 
		if (csrTimersOrderManagement == null) {
			csrTimersOrderManagement = new CsrTimersOrderManagement();
		}
		
		selectTab = this.addTab(csrTimersOrderManagement, "坐席定时预约详情", ResourceDataMgr.csr_timers_order_16);
		selectTab.setClosable(true);
		this.setSelectedTab(selectTab);
	}

	/****************** 报表管理模块 ******************/

	/**
	 * 客服记录统计报表
	 */
	public void showCustomerServiceRecordStatisticalView(){
		if(customerServiceRecordStatisticalView == null){
			customerServiceRecordStatisticalView = new CustomerServiceRecordStatisticalView();
		}
		selectTab = this.addTab(customerServiceRecordStatisticalView, "客服记录统计", ResourceDataMgr.report_base_16_ico);
		selectTab.setClosable(true);
		this.setSelectedTab(selectTab);
	}
	
	/**
	 * 话务统计总概览报表查看界面
	 */
	public void showCallStatisticOverviewReport() {
//		if (callStatisticOverviewReport == null) {
//			callStatisticOverviewReport = new CallStatisticOverviewReport();
//		}
//
//		selectTab = this.addTab(callStatisticOverviewReport, "话务统计总概览报表",
//				ResourceDataMgr.report_base_16_ico);
//		selectTab.setClosable(true);
//		this.setSelectedTab(selectTab);

		if (callStatisticNativeSqlOverviewReport == null) {
			callStatisticNativeSqlOverviewReport = new CallStatisticNativeSqlOverviewReport();
		}
		
		selectTab = this.addTab(callStatisticNativeSqlOverviewReport, "话务统计总概览报表", ResourceDataMgr.report_base_16_ico);
		selectTab.setClosable(true);
		this.setSelectedTab(selectTab);
	}

	/**
	 * 项目完成情况报表 chb
	 */
	public void showProjectFinishedStatus() {
		if(projectFinishedStatus == null){
			projectFinishedStatus = new ProjectFinishedStatus();
		}
		selectTab = this.addTab(projectFinishedStatus, "项目完成情况", ResourceDataMgr.report_base_16_ico);
		selectTab.setClosable(true);
		this.setSelectedTab(selectTab);
	}

	/**
	 * 客服记录情况报表 chb
	 */
	public void showServiceRecordStatus() {
		if(serviceRecordStatus == null){
			serviceRecordStatus = new ServiceRecordStatus();
		}
		selectTab = this.addTab(serviceRecordStatus, "客服记录情况",ResourceDataMgr.report_base_16_ico);
		selectTab.setClosable(true);
		this.setSelectedTab(selectTab);
	}

	public void showCallCheck() {
		if (callCheck == null) {//lxy 0526	添加只能显示一个tab的实例
			callCheck = new CallCheck();
		}
		selectTab = this.addTab(callCheck, "话务考核",ResourceDataMgr.report_base_16_ico);
		selectTab.setClosable(true);
		this.setSelectedTab(callCheck);
	}

	public void showCallCheckByCallTimeLength() {
		if (callCheckByCallTimeLength == null) {//lxy 0526	添加只能显示一个tab的实例
			callCheckByCallTimeLength = new CallCheckByCallTimeLength();
		}
		selectTab = this.addTab(callCheckByCallTimeLength, "通次考核", ResourceDataMgr.report_base_16_ico);
		selectTab.setClosable(true);
		this.setSelectedTab(callCheckByCallTimeLength);
	}

	public void showEmployeeCheck() {

		employeeCheck = new EmployeeCheck();

		selectTab = this.addTab(employeeCheck, "人员考核", ResourceDataMgr.report_base_16_ico);
		selectTab.setClosable(true);
		this.setSelectedTab(employeeCheck);
	}

	public void showEmployeeLoginLogoutDetail() {
		if(loginLogoutDetail == null ){
			loginLogoutDetail = new EmployeeLoginLogoutDetail();
		}
		selectTab = this.addTab(loginLogoutDetail, "员工上下线详单", ResourceDataMgr.report_base_16_ico);
		selectTab.setClosable(true);
		this.setSelectedTab(loginLogoutDetail);
	}

	public void showConcurrentStatics() {
		if(concurrentStatics == null){
			concurrentStatics = new ConcurrentStatics();
		}
		selectTab = this.addTab(concurrentStatics, "并发统计", ResourceDataMgr.report_base_16_ico);
		selectTab.setClosable(true);
		this.setSelectedTab(selectTab);
	}

	public void showQueueDetail() {
		if(queueDetail == null){
			queueDetail = new QueueDetail();
		}
		selectTab = this.addTab(queueDetail, "队列详情", ResourceDataMgr.report_base_16_ico);
		selectTab.setClosable(true);
		this.setSelectedTab(selectTab);
	}

	public void showAutodialDetail() {
		if(autodialDetail == null){
			autodialDetail = new AutodialDetail();
		}
		selectTab = this.addTab(autodialDetail, "自动外呼详情", ResourceDataMgr.report_base_16_ico);
		selectTab.setClosable(true);
		this.setSelectedTab(autodialDetail);
	}

	public void showSatisfactionInvestigate() {
		if(satisfactionInvestigate == null){
			satisfactionInvestigate = new CustomerSatisfactionInvestigate();
		}
		selectTab = this.addTab(satisfactionInvestigate, "满意度调查", ResourceDataMgr.report_base_16_ico);
		selectTab.setClosable(true);
		this.setSelectedTab(satisfactionInvestigate);
	}

	public void showKpi() {

		kpi = new KPI();

		selectTab = this.addTab(kpi, "KPI详情", ResourceDataMgr.report_base_16_ico);
		selectTab.setClosable(true);
		this.setSelectedTab(kpi);
	}

	public void showAdvanceKpiReportView() {

		if (advanceKPIReportView == null) {
			advanceKPIReportView = new AdvanceKPIReportView();
		}

		selectTab = this.addTab(advanceKPIReportView, "高级KPI详情", ResourceDataMgr.report_base_16_ico);
		selectTab.setClosable(true);
		this.setSelectedTab(advanceKPIReportView);
	}
	
	public void showProjectPool() {

		ProjectPool projectPool = new ProjectPool();

		selectTab = this.addTab(projectPool, "土豆池详情", ResourceDataMgr.report_base_16_ico);
		selectTab.setClosable(true);
		this.setSelectedTab(projectPool);
	}

	/**
	 * jrh 创建并显示工作流迁移日志
	 */
	public void showWorkflowTransferLog() {
		if (workflowTransferLogView == null) {
			workflowTransferLogView = new WorkflowTransferLogView();
		}
		selectTab = this.addTab(workflowTransferLogView, "工作流迁移日志",
				ResourceDataMgr.report_base_16_ico);
		selectTab.setClosable(true);
		this.setSelectedTab(workflowTransferLogView);
	}

	public void showQuestionnaireDetail() {

		QuestionnaireDetail questionnaire = new QuestionnaireDetail();

		selectTab = this.addTab(questionnaire, "问卷详情",
				ResourceDataMgr.report_base_16_ico);
		selectTab.setClosable(true);
		this.setSelectedTab(questionnaire);

	}

	public void showCustomerDetail() {

		CustomerDetail customerDetail = new CustomerDetail();

		selectTab = this.addTab(customerDetail, "客户明细",
				ResourceDataMgr.report_base_16_ico);
		selectTab.setClosable(true);
		this.setSelectedTab(customerDetail);
	}

	public void showBusinessDetail() {

		BusinessDetail businessDetail = new BusinessDetail();

		selectTab = this.addTab(businessDetail, "业务报表", null);
		selectTab.setClosable(true);
		this.setSelectedTab(businessDetail);
	}

	public void showCsrWorkDetail() {

		CsrWork csrWorkDetail = new CsrWork();

		selectTab = this.addTab(csrWorkDetail, "座席工作详情", null);
		selectTab.setClosable(true);
		this.setSelectedTab(csrWorkDetail);
	}

	public void showPausesDetail() {
		if (pauseDetail == null) {
			pauseDetail = new PauseDetailReport();
		}
		selectTab = this.addTab(pauseDetail, "置忙报表", ResourceDataMgr.report_base_16_ico);
		selectTab.setClosable(true);
		this.setSelectedTab(pauseDetail);
	}
	
	/** 置忙详情统计报表 */
	public void showPauseDetailStatistics() {
		if(pauseDetailStatisticsReport == null) {
			pauseDetailStatisticsReport = new PauseDetailStatisticsReport();
		}
		selectTab = this.addTab(pauseDetailStatisticsReport, "置忙详情统计报表", ResourceDataMgr.report_base_16_ico);
		selectTab.setClosable(true);
		this.setSelectedTab(pauseDetailStatisticsReport);
	}
	
	/****************** 客户成员管理模块 jrh ******************/
	public void showCustomerMemberManagement() {
		if (customerMemberManagement == null) {
			customerMemberManagement = new CustomerMemberManagement();
		}
		selectTab = this.addTab(customerMemberManagement, "客户成员管理",
				ResourceDataMgr.customer_member_mangement);
		selectTab.setClosable(true);
		this.setSelectedTab(selectTab);
	}

	public void showCustomerMigrateLogView() {
		if (customerMigrateLogView == null) {
			customerMigrateLogView = new CustomerMigrateLogView();
		}
		selectTab = this.addTab(customerMigrateLogView, "客户迁移日志",
				ResourceDataMgr.customer_migrate_log);
		selectTab.setClosable(true);
		this.setSelectedTab(selectTab);
	}

	/****************** 实时监控模块 jrh ******************/
	public void showEmployeeStatusSupervise() {
		if (employeeStatusSupervise == null) {
			employeeStatusSupervise = new EmployeeStatusSupervise();
		}
		try { // TODO 这个异常应该要解决掉
			selectTab = this.addTab(employeeStatusSupervise, "坐席状态监控",
					ResourceDataMgr.supervise_detail_16_ico);
		} catch (Exception e) {
			showEmployeeStatusSupervise();
			logger.error("点击‘坐席状态监控’according 按钮时，发生异常---》" + e.getMessage(), e);
		}
		selectTab.setClosable(true);
		this.setSelectedTab(selectTab);
	}

	public void showExtenStatusSupervise() {
		if (sipStatusSupervise == null) {
			sipStatusSupervise = new SipStatusSupervise();
		}
		selectTab = this.addTab(sipStatusSupervise, "分机状态监控",
				ResourceDataMgr.supervise_detail_16_ico);
		selectTab.setClosable(true);
		this.setSelectedTab(selectTab);
	}

	public void showQueueStatusSupervise() {
		if (queueStatusSupervise == null) {
			queueStatusSupervise = new QueueStatusSupervise();
		}
		try { // TODO 这个异常应该要解决掉
			selectTab = this.addTab(queueStatusSupervise, "队列状态监控",
					ResourceDataMgr.supervise_detail_16_ico);
		} catch (Exception e) {
			showQueueStatusSupervise();
			logger.error("点击‘队列状态监控’according 按钮时，发生异常---》" + e.getMessage(), e);
		}
		selectTab.setClosable(true);
		this.setSelectedTab(selectTab);
	}

	public void showCallStatusSupervise() {
		if (callStatusSupervise == null) {
			callStatusSupervise = new CallStatusSupervise();
		}
		selectTab = this.addTab(callStatusSupervise, "通话状态监控",
				ResourceDataMgr.supervise_detail_16_ico);
		selectTab.setClosable(true);
		this.setSelectedTab(selectTab);
	}

	public void showMeetMeSupervise() {
		if (meetMeSupervise == null) {
			meetMeSupervise = new MeetMeSupervise();
		}
		selectTab = this.addTab(meetMeSupervise, "活动会议室监控",
				ResourceDataMgr.supervise_detail_16_ico);
		selectTab.setClosable(true);
		this.setSelectedTab(selectTab);
	}

	/****************** 系统管理模块 jrh ******************/

	public void showUserManagement() {
		if (userManagement == null) {
			userManagement = new UserManagement();
		}
		selectTab = this.addTab(userManagement, "用户管理",
				ResourceDataMgr.user_manage);
		selectTab.setClosable(true);
		this.setSelectedTab(selectTab);
	}

	public void showRoleManagement() {
		if (roleManagement == null) {
			roleManagement = new RoleManagement();
		}
		selectTab = this.addTab(roleManagement, "角色管理",
				ResourceDataMgr.role_manage);
		selectTab.setClosable(true);
		this.setSelectedTab(selectTab);
	}

	public void showDeptManagement() {
		if (deptManagement == null) {
			deptManagement = new DeptManagement();
		}
		selectTab = this.addTab(deptManagement, "部门管理",
				ResourceDataMgr.department_manage);
		selectTab.setClosable(true);
		this.setSelectedTab(selectTab);
	}

	public void showExtManagement() {
		if (extManagement == null) {
			extManagement = new ExtManagement();
		}
		selectTab = this.addTab(extManagement, "分机管理",
				ResourceDataMgr.exten_manage);
		selectTab.setClosable(true);
		this.setSelectedTab(selectTab);
	}

	public void showOutlineManagement() {
		if (outlineManagement == null) {
			outlineManagement = new OutlineManagement();
		}
		selectTab = this.addTab(outlineManagement, "外线管理",
				ResourceDataMgr.outline_manage);
		selectTab.setClosable(true);
		this.setSelectedTab(selectTab);
	}

	public void showUserOutlineManagement() {
		if (userOutlineManagement == null) {
			userOutlineManagement = new UserOutlineManagement();
		}
		selectTab = this.addTab(userOutlineManagement, "外线成员管理",
				ResourceDataMgr.outline_member);
		selectTab.setClosable(true);
		this.setSelectedTab(selectTab);
	}
	
	public void showUserOutlinePoolManagement() {
		if (outlinePoolManagement == null) {
			outlinePoolManagement = new UserOutlinePoolManagement();
		}
		selectTab = this.addTab(outlinePoolManagement, "外线池管理",
				ResourceDataMgr.outline_member);
		selectTab.setClosable(true);
		this.setSelectedTab(selectTab);
	}

	public void showQueueManagement() {
		if (queueManagement == null) {
			queueManagement = new QueueManagement();
		}
		selectTab = this.addTab(queueManagement, "队列管理",
				ResourceDataMgr.queue_manage);
		selectTab.setClosable(true);
		this.setSelectedTab(selectTab);
	}

	public void showMusicOnHoldManagement() {
		if (musicOnHoldManagement == null) {
			musicOnHoldManagement = new MusicOnHoldManagement();
		}
		selectTab = this.addTab(musicOnHoldManagement, "保持音乐管理",
				ResourceDataMgr.moh_manage);
		selectTab.setClosable(true);
		this.setSelectedTab(selectTab);
	}

	public void showDynQueueMemberManagement() {
		if (dynQueueMemberManagement == null) {
			dynQueueMemberManagement = new DynQueueMemberManagement();
		}
		selectTab = this.addTab(dynQueueMemberManagement, "动态队列成员管理",
				ResourceDataMgr.dynamic_qm_manage);
		selectTab.setClosable(true);
		this.setSelectedTab(selectTab);
	}

	public void showStaticQueueMemberManagement() {
		if (staticQueueMemberManagement == null) {
			staticQueueMemberManagement = new StaticQueueMemberManagement();
		}
		selectTab = this.addTab(staticQueueMemberManagement, "静态队列成员管理",
				ResourceDataMgr.static_qm_manage);
		selectTab.setClosable(true);
		this.setSelectedTab(selectTab);
	}

	public void showInnerConfig() {
		if (innerConfig == null) { 
			innerConfig = new InnerConfigManagement();
		}

		selectTab = this.addTab(innerConfig, "内部配置管理",
				ResourceDataMgr.internal_setting_manage);
		selectTab.setClosable(true);
		this.setSelectedTab(selectTab);
	}

	public void showRecordStatusManagement() {
		if (serviceRecordStatusManagement == null) {
			serviceRecordStatusManagement = new CustomerServiceRecordStatusManagement();
		}

		selectTab = this.addTab(serviceRecordStatusManagement, "客服记录状态管理",
				ResourceDataMgr.internal_setting_manage);
		selectTab.setClosable(true);
		this.setSelectedTab(selectTab);
	}

	public void showDutyTableManagement() {
		if (dutyTableManagement == null) {
			dutyTableManagement = new DutyTableManagement();
		}
		selectTab = this.addTab(dutyTableManagement, "值班表管理",
				ResourceDataMgr.duty_report_manage);
		selectTab.setClosable(true);
		this.setSelectedTab(selectTab);
	}

	public void showMgrPhone2PhoneSettingManagement() {
		if (mgrPhone2PhoneSettingView == null) {
			mgrPhone2PhoneSettingView = new MgrPhone2PhoneSettingView();
		}
		selectTab = this.addTab(mgrPhone2PhoneSettingView, "外转外配置管理",
				ResourceDataMgr.phone2phone_16_icon);
		selectTab.setClosable(true);
		this.setSelectedTab(selectTab);
	}

	public void showBlacklistManagement() {
		if (blacklistView == null) {
			blacklistView = new BlacklistView();
		}
		selectTab = this.addTab(blacklistView, "黑名单管理",
				ResourceDataMgr.blacklist_16_icon);
		selectTab.setClosable(true);
		this.setSelectedTab(selectTab);
	}

	public void showDialPlanManagement() {
		if (dialplanInfoView == null) {
			dialplanInfoView = new DialplanInfoView();
		}
		selectTab = this.addTab(dialplanInfoView, "拨号方案详情",
				ResourceDataMgr.dial_plan_16_icon);
		selectTab.setClosable(true);
		this.setSelectedTab(selectTab);
	}
	
	public void showSatisNumManagement() {
		if (satisNumManager == null) {
			satisNumManager = new SatisNumManager();
		}
		selectTab = this.addTab(satisNumManager, "满意度调查按键管理",
				ResourceDataMgr.dial_plan_16_icon);
		selectTab.setClosable(true);
		this.setSelectedTab(selectTab);
	}
	
	// 客服记录状态按键管理
	public void showCustomerServiceRecordStatusNavigationKeyManagement(){
		if(customerServiceRecordStatusNavigationKeyManagement == null){
			customerServiceRecordStatusNavigationKeyManagement = new CustomerServiceRecordStatusNavigationKeyManagement();
		}
		selectTab = this.addTab(customerServiceRecordStatusNavigationKeyManagement, "客服记录状态按键管理", ResourceDataMgr.internal_setting_manage);
		selectTab.setClosable(true);
		this.setSelectedTab(selectTab);
	}

	// 管理员会议室
	public void showMgrMeetMe() {
		if (mgrMeetMe == null) {
			mgrMeetMe = new MgrMeetMe();
		}
		selectTab = this.addTab(mgrMeetMe, "管理员会议室",
				ResourceDataMgr.meeting_room_16_icon);
		selectTab.setClosable(true);
		this.setSelectedTab(selectTab);
	}

	// 操作日志查看
	public void showOperationLogView() {
		if (operationLogView == null) {
			operationLogView = new OperationLogView();
		}
		selectTab = this.addTab(operationLogView, "操作日志查看",
				ResourceDataMgr.operation_log_16_icon);
		selectTab.setClosable(true);
		this.setSelectedTab(selectTab);
	}

	// 系统状态
	public void showSystemStatusManagement() {
		if (systemStatusManagement == null) {
			systemStatusManagement = new SystemStatus();
		}
		selectTab = this.addTab(systemStatusManagement, "系统状态",
				ResourceDataMgr.system_status_16_ico);
		selectTab.setClosable(true);
		this.setSelectedTab(selectTab);
	}

	// 系统信息
	public void showSystemInfoManagement() {
		/*this.removeComponent(systemInfoManagement);*/
		if (systemInfoManagement == null) {
			systemInfoManagement = new SystemInfo();
		}
		selectTab = this.addTab(systemInfoManagement, "系统信息", ResourceDataMgr.system_info_16_ico);
		selectTab.setClosable(true);
		this.setSelectedTab(selectTab);
	}
	
	// 系统 License
	public void showSystemLicenceManagement() {
		this.removeComponent(systemLicenceManagement);

//		if (systemLicenceManagement == null) { //chb 注释掉了
		systemLicenceManagement = new SystemLicence();
//		}
		selectTab = this.addTab(systemLicenceManagement, "系统Licence",
				ResourceDataMgr.license_info_16_ico);
		selectTab.setClosable(true);
		this.setSelectedTab(selectTab);
	}

	// 关于我们
	public void showAboutManagement() {
		if (aboutManagement == null) {
			aboutManagement = new About();
		}
		selectTab = this.addTab(aboutManagement, "关于我们",
				ResourceDataMgr.about_us_16_ico);
		selectTab.setClosable(true);
		this.setSelectedTab(selectTab);
	}

	// 语音信箱留言管理
	public void showVoiceMailManagement() {
		if(voiceMailManagement == null) {
			voiceMailManagement = new VoiceMailManagement();
		}
		selectTab = this.addTab(voiceMailManagement, "语音留言管理", ResourceDataCsr.file_folder_main_16_png);
		selectTab.setClosable(true);
		this.setSelectedTab(selectTab);
	}
	
	// 商品管理
	public void showCommodityManagement() {
		if (commodityManagement == null) {
			commodityManagement = new CommodityManagement();
		}
		selectTab = this.addTab(commodityManagement, "商品管理", ResourceDataMgr.commodity_manage_16_ico);
		selectTab.setClosable(true);
		this.setSelectedTab(selectTab);
	}

	// 订单管理
	public void showOrderManagement() {
		if (orderManagement == null) {
			orderManagement = new OrderManagement();
		}
		selectTab = this.addTab(orderManagement, "订单管理",
				ResourceDataMgr.oder_manage_16_ico);
		selectTab.setClosable(true);
		this.setSelectedTab(selectTab);
	}

	// 问卷管理
	public void showQuestionnaireManagement() {
		if (questionnaireManagement == null) {
			questionnaireManagement = new QuestionnaireManagement();
		}
		selectTab = this.addTab(questionnaireManagement, "问卷管理",
				ResourceDataMgr.questionnarie_manage_16_ico);
		selectTab.setClosable(true);
		this.setSelectedTab(selectTab);
	}

	// 问卷编辑导出
	public void showQuestionnaireManagementEdit() {
		if (questionnaireManagementEdit == null) {
			questionnaireManagementEdit = new QuestionnaireManagementEdit();
		}
		selectTab = this.addTab(questionnaireManagementEdit, "问卷编辑导出",
				ResourceDataMgr.questionnarie_manage_16_ico);
		selectTab.setClosable(true);
		this.setSelectedTab(selectTab);
	}

	// 短信模板
	public void showTemplateManage() {
		if (templateManage == null) {
			templateManage = new MessageTemplateManage();
		}
		selectTab = this.addTab(templateManage, "模板管理", ResourceDataMgr.phone_message_template_16_ico);
		selectTab.setClosable(true);
		this.setSelectedTab(selectTab);
	}

	// 短信发送
	public void showSendMessageManage() {
		// jrh
		if (sendMutiMessageView == null) {
			sendMutiMessageView = new SendMutiMessageView();
		}
		selectTab = this.addTab(sendMutiMessageView, "短信发送", ResourceDataMgr.phone_message_send_16_ico);
		selectTab.setClosable(true);
		this.setSelectedTab(selectTab);
		// jrh 注释，界面不好看，里面还有一些模块可以看一下，比如通讯录等
		// if (sendMessageManage == null) {
		// sendMessageManage = new SendMessageManage();
		// }
		// selectTab = this.addTab(sendMessageManage, "短信发送",
		// ResourceDataMgr.phone_message_send_16_ico);
		// selectTab.setClosable(true);
		// this.setSelectedTab(selectTab);
	}

	// 显示历史短信消息
	public void showHistorySms() {
		if (historySms == null) {
			historySms = new MessageShow();
		}/* else {
			historySms.updateTable(false);
		}*/
		selectTab = this.addTab(historySms, "历史短信",
				ResourceDataMgr.history_message_16_ico);
		selectTab.setClosable(true);
		this.setSelectedTab(selectTab);

	}

	public void showKnowledgeManage() {
		/*
		 * if (knowledgeManagement == null) { knowledgeManagement = new
		 * KnowledgeManagement(); } else {
		 * knowledgeManagement.updateTable(false); } selectTab =
		 * this.addTab(knowledgeManagement, "知识库管理",
		 * ResourceDataMgr.kownledge_manage_16_ico);
		 * selectTab.setClosable(true); this.setSelectedTab(selectTab);
		 */
		if (kbInfoManagement == null) {
			kbInfoManagement = new KbInfoManagement();
		}
		selectTab = this.addTab(kbInfoManagement, "知识库管理",
				ResourceDataMgr.kownledge_16_ico);
		selectTab.setClosable(true);
		this.setSelectedTab(selectTab);
	}

	/**
	 * 查看知识库搜索结果
	 */
	public void showKnowledgeView() {
		if (knowledgeView == null) {
			knowledgeView = new KnowledgeView();
		}/* else {
			knowledgeView.update();
		}*/
		selectTab = this.addTab(knowledgeView, "知识库查看", ResourceDataMgr.kownledge_scan_16_ico);
		selectTab.setClosable(true);
		this.setSelectedTab(selectTab);
	}
	
	// 邮件管理部分
	public void showSendEmailManage() {
		if (emailSendView == null) {
			emailSendView = new EmailSendView();
		} else {
			emailSendView.update();
		}
		selectTab = this.addTab(emailSendView, "邮件发送",
				ResourceDataMgr.phone_message_send_16_ico);
		selectTab.setClosable(true);
		this.setSelectedTab(selectTab);
	}
	public void showHistoryEmailManage() {
		if (emailHistoryView == null) {
			emailHistoryView = new EmailHistoryView();
		} else {
			emailHistoryView.update();
		}
		selectTab = this.addTab(emailHistoryView, "历史邮件",
				ResourceDataMgr.history_message_16_ico);
		selectTab.setClosable(true);
		this.setSelectedTab(selectTab);
	}
	public void showConfigEmailManage() {
		if (emailConfigView == null) {
			emailConfigView = new EmailConfigView();
		} else {
			emailConfigView.update();
		}
		selectTab = this.addTab(emailConfigView, "配置邮件", ResourceDataMgr.phone_message_template_16_ico);
		selectTab.setClosable(true);
		this.setSelectedTab(selectTab);
	}
	
	public void showIvrManagement() {
		if(ivrManagement == null) {
			ivrManagement = new IvrManagement();
		}
		
		selectTab = this.addTab(ivrManagement, "语音导航流程管理", null);
		selectTab.setClosable(true);
		this.setSelectedTab(selectTab);
	}

	public void showFirstManagement() {
		if (firstManagement == null) {
			firstManagement = new FirstManagement();
		}
		selectTab = this.addTab(firstManagement, "系统首页",ResourceDataMgr.meeting_room_16_icon);
		selectTab.setClosable(true);
		this.setSelectedTab(selectTab);
	}
	
	/**
	 * jrh 2012-12-18 这是管理员操作的部分，在某一时刻，可能有多个管理员在操作数据，所以每次切换tab 时要重新加载
	 */
	@Override
	public void selectedTabChange(SelectedTabChangeEvent event) {
		Component selected = this.getSelectedTab();
		if (selected != null) {
			if (selected == projectControl) { // 项目控制
				projectControl.updateTable(false);

			} else if (selected == autoDialout) { // 自动外呼
				autoDialout.updateTable(false);
			} else if (selected == soundDialout) { // 语音群发
				soundDialout.updateTable(false);
			} else if (selected == soundUpload) { // 语音群发
				soundUpload.updateTable(false);
			} else if (selected == soundUpload) { // 语音上传
				soundUpload.updateTable(false);
			} else if (selected == resourceImport) { // 资源导入
				resourceImport.updateTable(false);
			} else if (selected == globalSearch) { // 全局搜索
				// TODO 添加全局搜索的更新方法
			} else if (selected == mgrServiceRecordView) { // 客服记录
				mgrServiceRecordView.updateTable(false);
			} else if (selected == callRecordManagement) { // 呼叫记录
				callRecordManagement.updateTable(false);
			} else if (selected == missCallLogManagement) { // 电话漏接记录
				missCallLogManagement.updateTable(false);
			} else if (selected == meettingDetailRecordManagement) { // 多方通话记录
				meettingDetailRecordManagement.updateTable(false);
			} else if (selected == noticeSend) { // 消息发送
				noticeSend.updateCsrInfo();
			} else if (selected == historyNotice) { // 消息发送
				historyNotice.updateTable(isSend);
			} else if (selected == csrTimersOrderManagement) { // 消息发送
				csrTimersOrderManagement.updateTable(true);

				// ******************* supervise manage 系统管理部分
				// *****************************//
			} else if (selected == customerMemberManagement) { // 客户成员管理
				customerMemberManagement.updateTable(false);
			} else if (selected == customerMigrateLogView) { // 客户迁移日志
				customerMigrateLogView.updateTable(false);

				// ******************* supervise manage 系统管理部分
				// *****************************//
			} else if (selected == employeeStatusSupervise) { // 坐席状态监控
				employeeStatusSupervise.update();
			} else if (selected == sipStatusSupervise) { // 分机状态监控
				sipStatusSupervise.update();
			} else if (selected == queueStatusSupervise) { // 队列状态监控
				queueStatusSupervise.update();
			} else if (selected == callStatusSupervise) { // 通话状态监控
				callStatusSupervise.update();
			} else if (selected == meetMeSupervise) { // 会议室监控
				meetMeSupervise.update();
				// ******************* system manage 系统管理部分
				// *****************************//
			} else if (selected == userManagement) { // 用户管理
				userManagement.updateTable(false);
			} else if (selected == roleManagement) { // 角色管理
				roleManagement.updateTable(false);
			} else if (selected == deptManagement) { // 部门管理
				deptManagement.updateTable(false);
			} else if (selected == extManagement) { // 分机管理
				extManagement.updateTable(false);
			} else if (selected == outlineManagement) { // 外线管理
				outlineManagement.updateTable(false);
			} else if (selected == userOutlineManagement) { // 外线管理
				userOutlineManagement.updateTable();
			} else if (selected == queueManagement) { // 队列管理
				queueManagement.updateTable(false);
			} else if (selected == ivrManagement) { // IVR配置
				ivrManagement.refreshTable(true);
			} else if (selected == musicOnHoldManagement) { // 保持音乐音管理
				musicOnHoldManagement.updateTable(false);
			} else if (selected == operationLogView) { // 操作日志
				operationLogView.updateTable(false);
			} else if (selected == dynQueueMemberManagement) { // 动态队列成员管理
				dynQueueMemberManagement.updateTable();
			} else if (selected == staticQueueMemberManagement) { // 静态队列成员管理
				staticQueueMemberManagement.updateTable();
			} else if (selected == innerConfig) { // 内部配置
				innerConfig.updateTableKeywords();
				innerConfig.updateCustomerServiceRecordLevel();
			} else if (selected == serviceRecordStatusManagement) { // 客服记录状态配置刚管理
				serviceRecordStatusManagement.refreshTable(false);
			} else if (selected == mgrPhone2PhoneSettingView) { // 外转外配置
				mgrPhone2PhoneSettingView.update();
			} else if (selected == blacklistView) { // 黑名单配置
				blacklistView.refreshTable(false);
			} else if (selected == dutyTableManagement) { // 值班表管理
				// dutyTableManagement.updateTable(false);
			} else if (selected == commodityManagement) { // 商品管理
				commodityManagement.updateTable(false);
			} else if (selected == orderManagement) { // 订单管理
				orderManagement.refreshTable(false);
			} else if (selected == questionnaireManagement) { // 问卷管理
				questionnaireManagement.updateTable(false);
			} else if (selected == questionnaireManagementEdit) { // 问卷数据编辑
				questionnaireManagementEdit.updateTable(false);
				questionnaireManagementEdit.refreshComponentInfo();
			} else if (selected == historySms) { // 历史短息管理
				historySms.updateTable(false);
			} else if (selected == sendMutiMessageView) { // 群发短信
				sendMutiMessageView.refreshTemplates();
			} else if (selected == kbInfoManagement) { // 知识库
				kbInfoManagement.refreshComponentInfo();
				kbInfoManagement.updateTable(false);
			} else if(selected == knowledgeView) {
				knowledgeView.update();
			} else if(selected == firstManagement){
				//不添加刷新
			}
		}

		// 停止监控线程
		stopSupperviceThread(selected, "onTabChange");
	}

	/**
	 * 停止监控线程
	 *    分两种情况，一种是Tab 页发生切换，另一种是Tab页被关闭
	 * 	tab 页发生切换时，需要将出当前被选中的监控界面外，其他的都停止刷新线程
	 *  tab 页关闭时，只需要停止被关闭的监控界面所对应的线程就可以了
	 * @param selected
	 * @param eventType
	 */
	public void stopSupperviceThread(Component selected, String eventType) {
		if("onTabChange".equals(eventType)) {	
			/**
			 * 由于页面如果切换的话, 不会立即停止该线程, 而是等待由各自的设置需要等待几秒钟, 而在这几秒钟的时间里, 如果再次点击该界面的话, 
			 * 会再重新创建一个线程, 并且把让线程刷新的值, 给重新设置为 true, 也就是线程还是会一直刷新的.
			 * 所以这里不需要在 tab 切换的时候, 来关闭该线程的刷新.
			 */
			/*if (employeeStatusSupervise != null && selected != employeeStatusSupervise) {
				employeeStatusSupervise.setGotoRun(false);
			}
			if (sipStatusSupervise != null && selected != sipStatusSupervise) {
				sipStatusSupervise.setGotoRun(false);
			}
			if (queueStatusSupervise != null && selected != queueStatusSupervise) {
				queueStatusSupervise.setGotoRun(false);
			}
			if (callStatusSupervise != null && selected != callStatusSupervise) {
				callStatusSupervise.setGotoRun(false);
				callStatusSupervise.setHasCreatedThread(false);
			}
			if (meetMeSupervise != null && selected != meetMeSupervise) {
				meetMeSupervise.setGotoRun(false);
			}*/
		} else if("onTabClose".equals(eventType)) {
			if (employeeStatusSupervise != null && selected == employeeStatusSupervise) {
				employeeStatusSupervise.setGotoRun(false);
			} else if (sipStatusSupervise != null && selected == sipStatusSupervise) {
				sipStatusSupervise.setGotoRun(false);
			} else if (queueStatusSupervise != null && selected == queueStatusSupervise) {
				queueStatusSupervise.setGotoRun(false);
			} else if (callStatusSupervise != null && selected == callStatusSupervise) {
				callStatusSupervise.setGotoRun(false);
			} else if (meetMeSupervise != null && selected == meetMeSupervise) {
				meetMeSupervise.setGotoRun(false);
			}
		}
	}
   
}
