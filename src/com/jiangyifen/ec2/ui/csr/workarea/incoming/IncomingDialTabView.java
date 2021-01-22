package com.jiangyifen.ec2.ui.csr.workarea.incoming;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import com.jiangyifen.ec2.bean.ChannelSession;
import com.jiangyifen.ec2.bean.IncomingDialInfo;
import com.jiangyifen.ec2.bean.RoleType;
import com.jiangyifen.ec2.entity.BusinessModel;
import com.jiangyifen.ec2.entity.CustomerResource;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.MarketingProject;
import com.jiangyifen.ec2.entity.Queue;
import com.jiangyifen.ec2.entity.Role;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.globaldata.ResourceDataCsr;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.service.eaoservice.MarketingProjectService;
import com.jiangyifen.ec2.service.eaoservice.QueueService;
import com.jiangyifen.ec2.ui.csr.workarea.common.CustomerAddressView;
import com.jiangyifen.ec2.ui.csr.workarea.common.CustomerBaseInfoView;
import com.jiangyifen.ec2.ui.csr.workarea.common.CustomerDescriptionView;
import com.jiangyifen.ec2.ui.csr.workarea.common.CustomerDetailInfoTabSheetView;
import com.jiangyifen.ec2.ui.csr.workarea.common.CustomerLogTabSheetView;
import com.jiangyifen.ec2.ui.csr.workarea.common.HistoryOrderDetailView;
import com.jiangyifen.ec2.ui.csr.workarea.common.HistoryRecordView;
import com.jiangyifen.ec2.ui.csr.workarea.order.CommodityOrderCreator;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.terminal.gwt.server.WebApplicationContext;
import com.vaadin.terminal.gwt.server.WebBrowser;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.VerticalLayout;

/**
 * 某个客户呼入时，在弹屏界面中的一个Tab页
 * @author jrh
 */
@SuppressWarnings("serial")
public class IncomingDialTabView extends VerticalLayout {

	// 订单权限
	private static final String TASK_MANAGEMENT_HISTORY_ORDER = "task_management&history_order";
	
	private HorizontalLayout mainHLayout;								// Tab的中心布局
	private VerticalLayout leftVLayout;									// Tab 页内的左侧布局
	private CustomerBaseInfoView customerBaseInfoView;					// 客户资源对象的基本信息界面

	private TabSheet businessOperations;								// 业务TabSheet 选择
	private IncomingDialRecordInfoCreator incomingDialRecordInfoEditor;	// 呼入处理结果的创建界面
	private CommodityOrderCreator commodityOrderCreator;				// 商品下单界面
	private QuestionnaireInvestigate2IncomingCreator questionnaireInvestigate2IncomingCreator;	// 问卷模块
	private Tab questionnaireInvestigateTab;							// 问卷调查Tab 页
	
	private VerticalLayout rightVLayout;								// Tab 页内的右侧布局
	private CustomerDetailInfoTabSheetView detailInfoTabSheetView;		// 客户详细信息的 TabSheet 组件
	private CustomerDescriptionView customerDescriptionView;			// 客户的描述信息显示界面
	private CustomerAddressView customerAddressView;					// 客户的地址信息显示界面

	private CustomerLogTabSheetView customerLogTabSheetView;			// 窗口右下方用于显示与客户相关的记录信息的地方
	private HistoryRecordView serviceRecordView;						// 对该客户做的所有历史记录
	private HistoryOrderDetailView orderDetailView;						// 该客户所有买过的商品详单记录
	
	private HorizontalLayout tabTop_hlo;								// 当前tab 页上方的附加信息
	private Label callerUseOutline_lb;									// 呼入客户使用的外线
	
	private String exten;												// 当前用户使用的分机
	private User loginUser;
	private Domain domain;
	private ArrayList<String> ownModels;								// 当前用户所拥有的功能权限的集合
	private boolean orderMakable = false;								// 是否可以制作订单
	private IncomingDialInfo incomingDialInfo;							// 客户呼入使用的外线信息
	private ChannelSession ringingExtenChannelSession;					// 被叫分机振铃时的通道信息
	
	private MarketingProjectService marketingProjectService;			// 项目服务类
	private QueueService queueService;
	
	public IncomingDialTabView(User loginUser, TabSheet tabSheet) {
		this.loginUser = loginUser;
		this.domain = loginUser.getDomain();
		
		exten = ShareData.userToExten.get(loginUser.getId());
		marketingProjectService = SpringContextHolder.getBean("marketingProjectService");
		queueService = SpringContextHolder.getBean("queueService");
		
		ownModels = new ArrayList<String>();
		for(Role role : loginUser.getRoles()) {
			if(role.getType().equals(RoleType.csr)) {
				for(BusinessModel bm : role.getBusinessModels()) {
					ownModels.add(bm.toString());
				} 
			} 
		}
		orderMakable = ownModels.contains(TASK_MANAGEMENT_HISTORY_ORDER);
		
		// ========= 在Tab 页顶部的附加信息  ================ //
		tabTop_hlo = new HorizontalLayout();
		tabTop_hlo.setMargin(false, true, false, true);
		tabTop_hlo.setSpacing(true);
		this.addComponent(tabTop_hlo);
		
		callerUseOutline_lb = new Label("", Label.CONTENT_XHTML);
		callerUseOutline_lb.setWidth("-1px");
		tabTop_hlo.addComponent(callerUseOutline_lb);
		
		// ======== Tab的中心布局 ========//
		mainHLayout = new HorizontalLayout();
		mainHLayout.setWidth("100%");
		mainHLayout.setMargin(true);
		mainHLayout.setSpacing(true);
		this.addComponent(mainHLayout);

		// ======== 左侧组件 ========//
		leftVLayout = new VerticalLayout();
		leftVLayout.setSpacing(true);
		mainHLayout.addComponent(leftVLayout);
		
		customerBaseInfoView = new CustomerBaseInfoView(loginUser, RoleType.csr);
//		customerBaseInfoView.setCustomerInfoPanelHeight("165px");
		customerBaseInfoView.setIncomingDialTabView(this);
		leftVLayout.addComponent(customerBaseInfoView);

		// -------- 业务操作TabSheet ---------- // 
		businessOperations = new TabSheet();
		businessOperations.setImmediate(true);
		leftVLayout.addComponent(businessOperations);
		
		// 创建客服记录
		incomingDialRecordInfoEditor = new IncomingDialRecordInfoCreator(loginUser, this);
		incomingDialRecordInfoEditor.setMargin(true);
		incomingDialRecordInfoEditor.setTabSheet(tabSheet);
		businessOperations.addTab(incomingDialRecordInfoEditor, "创建服务记录", ResourceDataCsr.create_record_16_ico);
		
		// 商品下订单
		if(orderMakable) {
			commodityOrderCreator = new CommodityOrderCreator(loginUser, customerBaseInfoView);
			businessOperations.addTab(commodityOrderCreator, "创建商品订单", ResourceDataCsr.cart_16_ico);
		}
		
		// 问卷调查
		questionnaireInvestigate2IncomingCreator = new QuestionnaireInvestigate2IncomingCreator(loginUser, this);
		questionnaireInvestigateTab = businessOperations.addTab(questionnaireInvestigate2IncomingCreator, "问卷调查", ResourceDataCsr.questionnarie_investigate_16_ico);
		questionnaireInvestigateTab.setVisible(false);
		
		// ======== 右侧 组件========//
		rightVLayout = new VerticalLayout();
		rightVLayout.setSpacing(true);
		mainHLayout.addComponent(rightVLayout);

		detailInfoTabSheetView = new CustomerDetailInfoTabSheetView(loginUser);				// 右上
		detailInfoTabSheetView.setTablePageLength(4);
		rightVLayout.addComponent(detailInfoTabSheetView);
		
		customerDescriptionView = detailInfoTabSheetView.getCustomerDescriptionView();
		customerAddressView = detailInfoTabSheetView.getCustomerAddressView();
		
		customerLogTabSheetView = new CustomerLogTabSheetView(loginUser, RoleType.csr);		// 右下
		customerLogTabSheetView.setTablePageLength(7);
		rightVLayout.addComponent(customerLogTabSheetView);
		
		serviceRecordView = customerLogTabSheetView.getServiceRecordView();
		orderDetailView = customerLogTabSheetView.getOrderDetailView();
	}  
	
	/**
	 * 根据客户信息回显相应组件的信息
	 * @param customerResource	客户资源对象
	 */
	public void echoInformations(CustomerResource customerResource) {
		// 第一步：向组件传递参数
		detailInfoTabSheetView.setCustomerResource(customerResource);
		customerLogTabSheetView.setCustomerResource(customerResource);
		
		// 第二步：回显个组件中的信息
		incomingDialRecordInfoEditor.setCustomerResource(customerResource);			// 回显创建记录组件中客户的级别
		if(commodityOrderCreator != null) {
			commodityOrderCreator.setCustomerResource(customerResource);				// 传递客户对象
		}
		
		MarketingProject currentProject = null;										// 回显客户问卷调查在组件信息
		Long projectId = ShareData.extenToProject.get(exten); 
		questionnaireInvestigateTab.setVisible(false);
		if(projectId != null) {		// 如果当前工作项目不存在与问卷的关联，则不显示问卷调查Tab 页
			currentProject = marketingProjectService.get(projectId);
			if(currentProject != null && currentProject.getQuestionnaires().size() > 0) {
				questionnaireInvestigate2IncomingCreator.echoQuestionnaireInfos(currentProject, customerResource);
				questionnaireInvestigateTab.setVisible(true);
				businessOperations.setSelectedTab(questionnaireInvestigate2IncomingCreator);
			}
		}
		
		customerBaseInfoView.echoCustomerBaseInfo(customerResource);				// 回显指定任务对应客户的基本信息

		TabSheet leftTopTS = detailInfoTabSheetView.getCustomerDetailInfoTabSheet();
		Component lt_tab = leftTopTS.getSelectedTab();
		if(lt_tab == leftTopTS.getTab(0).getComponent()) {
			customerDescriptionView.echoCustomerDescription(customerResource);		// 回显指定客服记录对应的客户描述信息
		} else if(lt_tab == leftTopTS.getTab(1).getComponent()) {
			customerAddressView.echoCustomerAddress(customerResource);				// 回显指定客服记录对应的客户地址信息
		}
		
		TabSheet rightBottomTS = customerLogTabSheetView.getCustomerLogTabSheet();
		Component rb_tab = rightBottomTS.getSelectedTab();
		if(rb_tab == rightBottomTS.getTab(0).getComponent()) {
			serviceRecordView.echoHistoryRecord(customerResource);					// 回显客户的历史服务记录
		} else if(rb_tab == rightBottomTS.getTab(1).getComponent()) {
			orderDetailView.echoOrderDetailInfos(customerResource);					// 回显客户的订单详情记录
		}
	}
	
	/**
	 * 当关闭Tab 页时调用
	 */
	public void clearComponentsValue() {
		incomingDialRecordInfoEditor.clearComponentsValue();
		if(commodityOrderCreator != null) {
			commodityOrderCreator.clearComponentsValue();
			commodityOrderCreator.setMakeOrderSuccessLabelVisible(false);
		}
	}
	
	/**
	 * 自行以的加载方法
	 */
	public void myAttach() {
		WebApplicationContext context = (WebApplicationContext) this.getApplication().getContext();
		WebBrowser webBrowser = context.getBrowser();
		int width = webBrowser.getScreenWidth();
		if(width >= 1366) {
			mainHLayout.setExpandRatio(leftVLayout, 0.45f);
			mainHLayout.setExpandRatio(rightVLayout, 0.55f);
		}
		incomingDialRecordInfoEditor.myAttach();
	}

	/**
	 * 获取被叫分机振铃时的通道信息
	 * @return
	 */
	public ChannelSession getRingingExtenChannelSession() {
		return ringingExtenChannelSession;
	}

	/**
	 *  更新呼入弹屏的Tab 页中的被叫分机振铃时的ChannelSession
	 *  如果在话务员关闭呼入Tab页之前，该客户再次呼入时，虽然不用重新创建Tab 页，但是主叫的通道信息会发生变化，如通道的uniqueid
	 * @param ringingExtenChannelSession 被叫分机振铃时的通道信息
	 */
	public void setRingingExtenChannelSession(ChannelSession ringingExtenChannelSession) {
		this.ringingExtenChannelSession = ringingExtenChannelSession;
		this.incomingDialInfo = null;

//--------------------------------------------------------------------------------------------------------------------------
		// jrh 2013-11-21 新增，显示客户来电号码
		String callerNum = ringingExtenChannelSession.getConnectedlinenum();
		ConcurrentHashMap<String, IncomingDialInfo> incomingCallerToDialInfos = ShareData.domainToIncomingDialInfoMap.get(loginUser.getDomain().getId());
		if(incomingCallerToDialInfos != null) {
			IncomingDialInfo dialInfo = incomingCallerToDialInfos.get(callerNum);
			if(dialInfo != null) {
				this.incomingDialInfo = dialInfo;
				String outline = dialInfo.getVasOutline();
				String lb_info = "客户呼入外线："+outline;
				
				Long projectId = ShareData.outlineToProject.get(outline);
				if(projectId != null) {	// 显示呼入的队列信息
					String queueName = ShareData.projectToQueue.get(projectId);
					Queue queue = (queueName != null) ? queueService.getByName(queueName, domain.getId()) : null; 
					lb_info = (queue != null) ? lb_info+"; 外线对应队列："+queue.getDescriptionAndName() : lb_info;
				}
				
				callerUseOutline_lb.setValue("<font color='blue'>"+lb_info+"</font>");
			}
		}
//--------------------------------------------------------------------------------------------------------------------------
	}

	public IncomingDialInfo getIncomingDialInfo() {
		return incomingDialInfo;
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
	
}
