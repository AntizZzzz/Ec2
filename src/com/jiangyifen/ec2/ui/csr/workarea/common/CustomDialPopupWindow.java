package com.jiangyifen.ec2.ui.csr.workarea.common;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import org.asteriskjava.fastagi.AgiChannel;
import org.asteriskjava.fastagi.AgiException;

import com.jiangyifen.ec2.bean.RoleType;
import com.jiangyifen.ec2.entity.CustomerResource;
import com.jiangyifen.ec2.entity.Domain;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.globaldata.ResourceDataCsr;
import com.jiangyifen.ec2.globaldata.ShareData;
import com.jiangyifen.ec2.ui.csr.statusbar.CsrStatusBar;
import com.jiangyifen.ec2.ui.csr.workarea.order.CommodityOrderCreator;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.CloseListener;

/**
 * 该窗口作为呼叫弹出窗口
 * 可用情形：
 * 	用户在系统界面的状态栏中，直接输入号码进行拨打电话时，可以使用这个弹窗
 * 	用户在cdr 呼叫录音记录中点击号码进行拨打时，可以使用		
 */
@SuppressWarnings("serial")
public class CustomDialPopupWindow extends Window implements CloseListener {

	// 弹屏后是否需要将分机置忙
	private final static String PASUE_EXTEN_AFTER_CSR_POPUP_CALLING_WINDOW = "pasue_exten_after_csr_popup_calling_window";
	private final static String CREATE_AFTERCALL_LOG_AFTER_CSR_POPUP_CALLING_WINDOW = "create_afterCall_log_after_csr_popup_calling_window";	// 是否需要创建话后处理日志

	// 订单权限
	private static final String TASK_MANAGEMENT_HISTORY_ORDER = "task_management&history_order";

	private VerticalLayout leftVLayout;											// 窗口内的左侧布局
	private CustomerBaseInfoView customerBaseInfoView;							// 客户资源对象的基本信息界面
	
	private TabSheet businessOperations;										// 业务TabSheet 选择
	private CustomDialRecordInfoCreator dialRecordInfoEditor;					// 外呼结果的创建界面
	private CommodityOrderCreator commodityOrderCreator;						// 商品下单界面
	
	private VerticalLayout rightVLayout;										// 窗口内的右侧布局
	private CustomerDetailInfoTabSheetView detailInfoTabSheetView;				// 客户详细信息的 TabSheet 组件
	private CustomerDescriptionView customerDescriptionView;					// 客户的描述信息显示界面
	private CustomerAddressView customerAddressView;							// 客户的地址信息显示界面
	
	private CustomerLogTabSheetView customerLogTabSheetView;					// 窗口右下方用于显示与客户相关的记录信息的地方
	private HistoryRecordView serviceRecordView;								// 对该客户做的所有历史记录
	private HistoryOrderDetailView orderDetailView;								// 该客户所有买过的商品详单记录

	private Domain domain;
	private User loginUser;														// 当前登陆用户
	private ArrayList<String> ownModels;										// 当前用户所拥有的功能权限的集合
	private boolean orderMakable = false;										// 是否可以制作订单
	private AgiChannel agiChannel;												// 当前弹出对应主叫方的通道信息

	public CustomDialPopupWindow() {
		this.setImmediate(true);
		this.addListener((CloseListener)this);
		
		loginUser = SpringContextHolder.getLoginUser();
		domain = SpringContextHolder.getDomain();
		ownModels = SpringContextHolder.getBusinessModel();
		orderMakable = ownModels.contains(TASK_MANAGEMENT_HISTORY_ORDER);

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
//		customerBaseInfoView.setCustomerInfoPanelHeight("165px");
		leftVLayout.addComponent(customerBaseInfoView);

		// -------- 业务操作TabSheet ---------- // 
		businessOperations = new TabSheet();
		leftVLayout.addComponent(businessOperations);

		// 创建客服记录
		dialRecordInfoEditor = new CustomDialRecordInfoCreator(this);
		dialRecordInfoEditor.setMargin(true);
		businessOperations.addTab(dialRecordInfoEditor, "创建服务记录", ResourceDataCsr.create_record_16_ico);

		// 商品下订单
		if(orderMakable) {
			commodityOrderCreator = new CommodityOrderCreator(loginUser, customerBaseInfoView);
			businessOperations.addTab(commodityOrderCreator, "创建商品订单", ResourceDataCsr.cart_16_ico);
		}
		
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
		
		Integer[] screenResolution = SpringContextHolder.getScreenResolution();
		if(screenResolution[0] >= 1366) {
			mainHLayout.setExpandRatio(leftVLayout, 0.45f);
			mainHLayout.setExpandRatio(rightVLayout, 0.55f);
		} 
	}
	
	/**
	 * 根据客户信息回显相应组件的信息
	 * @param customerResource	客户资源对象
	 */
	public void echoInformations(CustomerResource customerResource) {
		// 原有的业务Tab 页
		Component oldSelectedTab = businessOperations.getSelectedTab();
		
		// 第一步：向组件传递参数
		detailInfoTabSheetView.setCustomerResource(customerResource);
		customerLogTabSheetView.setCustomerResource(customerResource);
		
		// 第二步：回显个组件中的信息 
		dialRecordInfoEditor.setCustomerResource(customerResource);					// 回显创建记录组件中客户的级别

		if(commodityOrderCreator != null) {
			commodityOrderCreator.setCustomerResource(customerResource);				// 传递客户对象
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
		
		// 选中原有的业务Tab 页
		if(oldSelectedTab != null) {
			businessOperations.setSelectedTab(oldSelectedTab);
		}
	}

	/**
	 * 当直接点击关闭窗口或通过调用removeWindow 方法来关闭窗口时
	 * 	调用该方法清理已经填写的一些数据
	 */
	@Override
	public void windowClose(CloseEvent e) {
		dialRecordInfoEditor.clearComponentsValue();
		if(commodityOrderCreator != null) {
			commodityOrderCreator.clearComponentsValue();
			commodityOrderCreator.setMakeOrderSuccessLabelVisible(false);
		}

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
