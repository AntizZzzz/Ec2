package com.jiangyifen.ec2.ui.csr.workarea.common;

import com.jiangyifen.ec2.bean.RoleType;
import com.jiangyifen.ec2.entity.CustomerResource;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.globaldata.ResourceDataCsr;
import com.jiangyifen.ec2.utils.SpringContextHolder;
import com.vaadin.ui.Component;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;
import com.vaadin.ui.TabSheet.SelectedTabChangeListener;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * 该类用于让csr 查看客户的工种基本信息
 * 	MyTaskTabView 中有调用
 */
@SuppressWarnings("serial")
public class CustomerAllInfoWindow extends Window implements SelectedTabChangeListener {
	
	private TabSheet customerInfoTabSheet;							// 客服信息TabSheet
	
	// 客户基本信息显示 Tab 页
	private VerticalLayout baseInfoVLayout;							// 客户基本信息Tab 页布局管理器
	private CustomerBaseInfoView customerBaseInfoView;				// 客户基本信息编辑表单
	
	// 客户描述信息显示 Tab 页
	private VerticalLayout descriptionTabVLayout;					// 客户描述Tab 页布局管理器
	private CustomerDescriptionView customerDescriptionView;		// 客户描述信息的显示表格
	
	// 客户地址信息显示 Tab 页
	private VerticalLayout addressTabVLayout;						// 客户描述Tab 页布局管理器
	private CustomerAddressView customerAddressView;				// 客户描述信息的显示表格
	
	// 客户历史记录显示 Tab 页
	private VerticalLayout historyRecordTabVLayout;					// 客户描述Tab 页布局管理器
	private HistoryRecordView historyRecordView;					// 对该客户做的所有历史记录
	
	private User loginUser;											// 当前登陆用户
	private RoleType roleType;										// 当前用户登陆时使用的角色类型
	private CustomerResource customerResource;						// 个组件的数据源----客户资源
	
	public CustomerAllInfoWindow(RoleType roleType) {
		this.center();
		this.setWidth("680px");
		this.setHeight("380px");
		this.setImmediate(true);
		this.roleType = roleType;
		
		loginUser = SpringContextHolder.getLoginUser();
		
		customerInfoTabSheet = new TabSheet();
		customerInfoTabSheet.setWidth("100%");
		customerInfoTabSheet.setHeight("345px");
		customerInfoTabSheet.addListener((SelectedTabChangeListener)this);
		this.addComponent(customerInfoTabSheet);
		
		// 创建基本信息显示 Tab 页
		createBaseInfoTab();
		
		// 创建客户描述信息显示 Tab 页
		createDescriptionTab();
		
		// 创建客户地址信息显示 Tab 页
		createAddressTab();
		
		// 创建客户历史记录显示 Tab 页
		createHistoryRecordTab();
	}
	
	@Override
	public void selectedTabChange(SelectedTabChangeEvent event) {
		Component tab = customerInfoTabSheet.getSelectedTab();
		if(tab == baseInfoVLayout) {
			customerBaseInfoView.echoCustomerBaseInfo(customerResource);		// 回显表格中选中项对应客户的基本信息
		} else if(tab == descriptionTabVLayout) {
			customerDescriptionView.echoCustomerDescription(customerResource);	// 回显表格中选中项对应客户的所有描述信息
		} else if(tab == addressTabVLayout) {
			customerAddressView.echoCustomerAddress(customerResource);			// 回显表格中选中项对应客户的所有地址信息
		} else if(tab == historyRecordTabVLayout) {
			historyRecordView.echoHistoryRecord(customerResource);	// 回显表格中选中项对应客户的所有地址信息
		}
	}

	/**
	 *  创建基本信息显示 Tab 页
	 */
	private void createBaseInfoTab() {
		baseInfoVLayout = new VerticalLayout();
		baseInfoVLayout.setMargin(true);
		
		customerBaseInfoView = new CustomerBaseInfoView(loginUser, roleType);
		customerBaseInfoView.setCustomerInfoPanelHeight("290px");
		baseInfoVLayout.addComponent(customerBaseInfoView);
		customerInfoTabSheet.addTab(baseInfoVLayout, "客户基本信息", ResourceDataCsr.customer_info_16_ico);
	}

	/**
	 *  创建客户描述信息显示 Tab 页
	 */
	private void createDescriptionTab() {
		descriptionTabVLayout = new VerticalLayout();
		descriptionTabVLayout.setMargin(true);

		customerDescriptionView = new CustomerDescriptionView(loginUser);
		descriptionTabVLayout.addComponent(customerDescriptionView);
		customerInfoTabSheet.addTab(descriptionTabVLayout, "客户描述信息", ResourceDataCsr.customer_description_16_ico);
	}

	/**
	 *  创建地址信息Tab 页
	 */
	private void createAddressTab() {
		addressTabVLayout = new VerticalLayout();
		addressTabVLayout.setMargin(true);

		customerAddressView = new CustomerAddressView();
		addressTabVLayout.addComponent(customerAddressView);
		customerInfoTabSheet.addTab(addressTabVLayout, "客户地址信息", ResourceDataCsr.address_16_ico);
	}
	
	/** 
	 * 创建客户历史记录显示 Tab 页
	 */
	private void createHistoryRecordTab() {
		historyRecordTabVLayout = new VerticalLayout();
		historyRecordTabVLayout.setMargin(true);
		
		historyRecordView = new HistoryRecordView(loginUser, roleType);
		historyRecordTabVLayout.addComponent(historyRecordView);
		customerInfoTabSheet.addTab(historyRecordTabVLayout, "客户历史记录", ResourceDataCsr.customer_history_record_16_ico);
	}

	/**
	 * 当用户更改了客户的基本信息后，则将相应的模块进行回显信息
	 * @param sourceTableVLayout 当前正被操作的模块 中的表格
	 */
	public void setEchoModifyByReflect(VerticalLayout sourceTableVLayout) {
		customerBaseInfoView.setEchoModifyByReflect(sourceTableVLayout);
	}
	
	/**
	 * 根据客户信息回显相应组件的信息
	 * @param customerResource	客户资源对象
	 */
	public void initCustomerResource(CustomerResource customerResource) {
		this.customerResource = customerResource;
	}
	
	public void echoCustomerBaseInfo(CustomerResource customerResource) {
		customerBaseInfoView.echoCustomerBaseInfo(customerResource);		// 回显表格中选中项对应客户的基本信息
	}
	
	public void echoCustomerDescription(CustomerResource customerResource) {
		customerDescriptionView.echoCustomerDescription(customerResource);	// 回显表格中选中项对应客户的所有描述信息
	}

	public void echoCustomerAddress(CustomerResource customerResource) {
		customerAddressView.echoCustomerAddress(customerResource);			// 回显表格中选中项对应客户的所有地址信息
	}

	public void echoHistoryRecord(CustomerResource customerResource) {
		historyRecordView.echoHistoryRecord(customerResource);	// 回显表格中选中项对应客户的所有地址信息
	}

	public TabSheet getCustomerInfoTabSheet() {
		return customerInfoTabSheet;
	}

}
