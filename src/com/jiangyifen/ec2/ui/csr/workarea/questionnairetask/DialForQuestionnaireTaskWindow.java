package com.jiangyifen.ec2.ui.csr.workarea.questionnairetask;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

import org.asteriskjava.fastagi.AgiChannel;
import org.asteriskjava.fastagi.AgiException;

import com.jiangyifen.ec2.bean.RoleType;
import com.jiangyifen.ec2.entity.CustomerResource;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.MarketingProjectTask;
import com.jiangyifen.ec2.entity.Telephone;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.globaldata.ResourceDataCsr;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.service.csr.ami.DialService;
import com.jiangyifen.ec2.ui.FlipOverTableComponent;
import com.jiangyifen.ec2.ui.csr.statusbar.CsrStatusBar;
import com.jiangyifen.ec2.ui.csr.workarea.common.CustomDialRecordInfoCreator;
import com.jiangyifen.ec2.ui.csr.workarea.common.CustomerAddressView;
import com.jiangyifen.ec2.ui.csr.workarea.common.CustomerBaseInfoView;
import com.jiangyifen.ec2.ui.csr.workarea.common.CustomerDescriptionView;
import com.jiangyifen.ec2.ui.csr.workarea.common.CustomerDetailInfoTabSheetView;
import com.jiangyifen.ec2.ui.csr.workarea.common.HistoryRecordView;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.CloseListener;

@SuppressWarnings("serial")
public class DialForQuestionnaireTaskWindow extends Window implements CloseListener {

	// 弹屏后是否需要将分机置忙
	private final static String PASUE_EXTEN_AFTER_CSR_POPUP_CALLING_WINDOW = "pasue_exten_after_csr_popup_calling_window";
	private final static String CREATE_AFTERCALL_LOG_AFTER_CSR_POPUP_CALLING_WINDOW = "create_afterCall_log_after_csr_popup_calling_window";	// 是否需要创建话后处理日志

	private VerticalLayout leftVLayout;											// 窗口内的左侧布局
	private CustomerBaseInfoView customerBaseInfoView;							// 客户资源对象的基本信息界面
	
	private TabSheet businessOperations;										// 业务TabSheet 选择
	private CustomDialRecordInfoCreator dialRecordInfoEditor;					// 外呼结果的创建界面
	private QuestionnaireInvestigateCreator questionnaireInvestigateCreator;	// 问卷调查创建界面

	private Table focusTaskTable;												// 当前正在操作的任务表格
	private FlipOverTableComponent<MarketingProjectTask> unfocusTaskTableFlip;	// 当前  没有  操作的任务表格的翻页组件
	
	private VerticalLayout rightVLayout;										// 窗口内的右侧布局
	private CustomerDetailInfoTabSheetView detailInfoTabSheetView;				// 客户详细信息的 TabSheet 组件
	private CustomerDescriptionView customerDescriptionView;					// 客户的描述信息显示界面
	private CustomerAddressView customerAddressView;							// 客户的地址信息显示界面
	
	private HistoryRecordView historyRecordView;								// 对该客户做的所有历史记录

	private Domain domain;
	private String exten;
	private User loginUser;														// 当前登陆用户
	private boolean isExecuteAutoDial = false;									// 是否开启自动慢拨号
	private AgiChannel agiChannel;												// 当前弹出对应主叫方的通道信息

	public DialForQuestionnaireTaskWindow() {
		this.setImmediate(true);
		this.addListener((CloseListener)this);

		domain = SpringContextHolder.getDomain();
		exten = SpringContextHolder.getExten();
		loginUser = SpringContextHolder.getLoginUser();
		
		// ======== Tab的中心布局 ========//
		HorizontalLayout mainHLayout = new HorizontalLayout();
		mainHLayout.setWidth("100%");
		mainHLayout.setMargin(true);
		mainHLayout.setSpacing(true);
		this.setContent(mainHLayout);

		// ======== 左侧组件 ========//
		leftVLayout = new VerticalLayout();
		leftVLayout.setSpacing(true);
		mainHLayout.addComponent(leftVLayout);
		
		customerBaseInfoView = new CustomerBaseInfoView(loginUser, RoleType.csr);
		customerBaseInfoView.setOutgoingPopupWindow(this);
		customerBaseInfoView.setCustomerInfoPanelHeight("165px");
		leftVLayout.addComponent(customerBaseInfoView);

		// -------- 业务操作TabSheet ---------- // 
		businessOperations = new TabSheet();
		leftVLayout.addComponent(businessOperations);

		// 创建客服记录
		dialRecordInfoEditor = new CustomDialRecordInfoCreator(this);
		dialRecordInfoEditor.setMargin(true);
		businessOperations.addTab(dialRecordInfoEditor, "创建服务记录", ResourceDataCsr.create_record_16_ico);

		// 做问卷调查
		questionnaireInvestigateCreator = new QuestionnaireInvestigateCreator(customerBaseInfoView);
		businessOperations.addTab(questionnaireInvestigateCreator, "创建问卷调查", ResourceDataCsr.questionnarie_investigate_16_ico);
		
		// ======== 右侧 组件========//
		rightVLayout = new VerticalLayout();
		rightVLayout.setSpacing(true);
		mainHLayout.addComponent(rightVLayout);
		
		detailInfoTabSheetView = new CustomerDetailInfoTabSheetView(loginUser);
		detailInfoTabSheetView.setTablePageLength(4);
		customerDescriptionView = detailInfoTabSheetView.getCustomerDescriptionView();
		customerAddressView = detailInfoTabSheetView.getCustomerAddressView();
		rightVLayout.addComponent(detailInfoTabSheetView);
		
		Panel historyRecordPanel = new Panel("历史服务记录");
		historyRecordPanel.setStyleName("light");
		historyRecordPanel.setIcon(ResourceDataCsr.customer_history_record_16_ico);
		rightVLayout.addComponent(historyRecordPanel);

		historyRecordView = new HistoryRecordView(loginUser, RoleType.csr);
		historyRecordPanel.addComponent(historyRecordView);

		Integer[] screenResolution = SpringContextHolder.getScreenResolution();
		if(screenResolution[0] >= 1366) {
			mainHLayout.setExpandRatio(leftVLayout, 0.43f);
			mainHLayout.setExpandRatio(rightVLayout, 0.57f);
		} 
	}
	
	/**
	 * 根据客户信息回显相应组件的信息
	 * @param customerResource	客户资源对象
	 */
	public void echoInformations(CustomerResource customerResource) {
		// 原有的业务Tab 页
		Component selectedTab = businessOperations.getSelectedTab();
		
		// 第一步：向组件传递参数
		detailInfoTabSheetView.setCustomerResource(customerResource);
		
		// 第二步：回显个组件中的信息 
		dialRecordInfoEditor.setCustomerResource(customerResource);					// 回显创建记录组件中客户的级别
		questionnaireInvestigateCreator.echoQuestionnaireInfos(customerResource);	// 回显问卷调查在组件信息
		
		customerBaseInfoView.echoCustomerBaseInfo(customerResource);				// 回显指定问卷任务对应客户的基本信息
		historyRecordView.echoHistoryRecord(customerResource);			// 回显客户的历史服务记录
		
		TabSheet tabSheet = detailInfoTabSheetView.getCustomerDetailInfoTabSheet();
		Component tab = tabSheet.getSelectedTab();
		if(tab == tabSheet.getTab(0).getComponent()) {
			customerDescriptionView.echoCustomerDescription(customerResource);		// 回显指定客服记录对应的客户描述信息
		} else if(tab == tabSheet.getTab(1).getComponent()) {
			customerAddressView.echoCustomerAddress(customerResource);				// 回显指定客服记录对应的客户地址信息
		}
		
		// 选中原有的业务Tab 页
		if(selectedTab != null) {
			businessOperations.setSelectedTab(selectedTab);
		}
	}

	/**
	 * 当直接点击关闭窗口或通过调用removeWindow 方法来关闭窗口时
	 * 	更新“我的任务 -- 问卷”模块下表格中的显示内容
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void windowClose(CloseEvent e) {
		dialRecordInfoEditor.clearComponentsValue();
		questionnaireInvestigateCreator.updateQuestionnaireTableSource();

		// jrh  2013-12-17 弹屏就置忙队列成员      ---------------  开始
		ConcurrentHashMap<String, Boolean> domainConfigs = ShareData.domainToConfigs.get(domain.getId());
		if(domainConfigs != null) {
			Boolean ispauseExtenPopupWindow = domainConfigs.get(PASUE_EXTEN_AFTER_CSR_POPUP_CALLING_WINDOW);
			if(ispauseExtenPopupWindow != null && ispauseExtenPopupWindow) {
				Boolean iscreateCallAfterLog = domainConfigs.get(CREATE_AFTERCALL_LOG_AFTER_CSR_POPUP_CALLING_WINDOW);
				iscreateCallAfterLog = (iscreateCallAfterLog == null) ? true : iscreateCallAfterLog;
				
				CsrStatusBar csrStatusBar = ShareData.csrToStatusBar.get(loginUser.getId());
				if(csrStatusBar != null) {
					csrStatusBar.executeOutAfterCallHandle(iscreateCallAfterLog);
				}
			}
		}	//	------------- 结束

		// jrh  2013-12-18 自动慢拨号   --------------------- 开始
		if(isExecuteAutoDial) {				// 判断自动慢拨号是否开启了
			Collection<? extends MarketingProjectTask> taskSource = null;
			Table unfinishedTable = focusTaskTable;
			if("unfinished_table".equals(focusTaskTable.getData())) {		// 判断当前用户拨打的电话是否是“未完成任务表格”中的数据，如果不是，则下一步应该取非焦点表格的任务进行呼叫
				taskSource = (Collection<? extends MarketingProjectTask>) unfinishedTable.getItemIds();
			} else {
				unfinishedTable = unfocusTaskTableFlip.getTable();
				taskSource = (Collection<? extends MarketingProjectTask>) unfinishedTable.getItemIds();
			}
			ArrayList<MarketingProjectTask> tasks = new ArrayList<MarketingProjectTask>(taskSource);
			if(tasks.size() > 0) {		// 如果“未完成任务列表”中还有没完成的任务，则取出第一条，进行呼叫
				MarketingProjectTask nextTask = tasks.get(0);
				unfinishedTable.select(nextTask);
				
				refreshTaskInWindow(nextTask);	// JRH 20141010 解决自动慢拨号时，创建客服记录后，未完成任务列表不跳转至已完成任务列表的问题 
				
				CustomerResource nextResource = nextTask.getCustomerResource();
				for(Telephone nextPhone : nextResource.getTelephones()) {
					DialService dialService = SpringContextHolder.getBean("dialService");
					dialService.dial(exten, nextPhone.getNumber());
					break;
				}
			}
		}	//	------------- 结束
	}

	/**
	 * 当用户更改了客户的基本信息后，则将相应的模块进行回显信息
	 * @param sourceTableVLayout 当前正被操作的模块 中的表格
	 */
	public void setEchoModifyByReflect(VerticalLayout sourceTableVLayout) {
		customerBaseInfoView.setEchoModifyByReflect(sourceTableVLayout);
	}
	
	/**
	 * 为我的问卷任务模块使用
	 * @param focusTaskTableFlip	外呼问卷任务模块中当前 正在 被操作的问卷任务表格
	 * @param unfocusTaskTableFlip	外呼问卷任务模块中当前 没有 被操作的问卷任务表格
	 */
	public void refreshServiceRecordInfoEditor(FlipOverTableComponent<MarketingProjectTask> focusTaskTableFlip, 
			FlipOverTableComponent<MarketingProjectTask> unfocusTaskTableFlip) {
		this.unfocusTaskTableFlip = unfocusTaskTableFlip;
		this.focusTaskTable = focusTaskTableFlip.getTable();
		questionnaireInvestigateCreator.refreshComponent(focusTaskTableFlip, unfocusTaskTableFlip);
	}

	/**
	 * 检查客户基础信息编辑窗口，如果是处于编辑状态，则先保存客户基础信息
	 *  返回保存结果，[成功 : true, 失败 ： false]
	 * @return boolean 
	 */
	public boolean checkAndSaveCustomerResourceInfo() {
		boolean isSuccess = customerBaseInfoView.getCustomerBaseInfoEditorForm().checkAndSaveCustomerResourceInfo();
		return isSuccess;
	}

	/**
	 * 设置关闭弹屏后是否需要自动呼叫下一条任务
	 * @param isExecuteAutoDial
	 */
	public void setExecuteAutoDial(boolean isExecuteAutoDial) {
		this.isExecuteAutoDial = isExecuteAutoDial;
	}

	/**
	 * @Description 描述：当坐席点击呼叫按钮后，更新当前被叫客户对应的任务对象
	 *
	 * @author  JRH
	 * @date    2014年6月6日 下午8:47:48
	 * @param projectTask void
	 */
	public void refreshTaskInWindow(MarketingProjectTask projectTask) {
		if(questionnaireInvestigateCreator != null) {
			questionnaireInvestigateCreator.refreshCurrentTask(projectTask);
		}
	}
	
	/**
	 * 获取当前弹出对应主叫方的通道信息
	 * @return
	 */
	public AgiChannel getAgiChannel() {
		return agiChannel;
	}

	/**
	 * 设置当前弹出对应主叫方的通道信息
	 * @param agiChannel
	 */
	public void setAgiChannel(AgiChannel agiChannel) {
		this.agiChannel = agiChannel;
		
		// 显示呼出所使用的外线信息
		if(agiChannel != null) {
			String vasOutline = "";
			try {
				vasOutline = agiChannel.getVariable("outline");
			} catch (AgiException e) {
				e.printStackTrace();
			}
			this.setCaption("您使用的外线："+vasOutline);
		}
	}

	
}
