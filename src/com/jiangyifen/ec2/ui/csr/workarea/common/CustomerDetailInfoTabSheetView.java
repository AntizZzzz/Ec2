package com.jiangyifen.ec2.ui.csr.workarea.common;

import com.jiangyifen.ec2.entity.Address;
import com.jiangyifen.ec2.entity.CustomerResource;
import com.jiangyifen.ec2.entity.CustomerResourceDescription;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.globaldata.ResourceDataCsr;
import com.jiangyifen.ec2.ui.FlipOverTableComponent;
import com.vaadin.ui.Component;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;
import com.vaadin.ui.TabSheet.SelectedTabChangeListener;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class CustomerDetailInfoTabSheetView extends VerticalLayout {
	private TabSheet customerDetailInfoTabSheet;				// 客服记录TabSheet
	
	// 客户描述信息显示 Tab 页
	private VerticalLayout descriptionTabVLayout;				// 客户描述Tab 页布局管理器
	private CustomerDescriptionView customerDescriptionView;	// 客户描述信息的显示表格
	private FlipOverTableComponent<CustomerResourceDescription> descriptionTableFlip;	// 客户描述信息显示表格的翻页组件
	
	// 客户地址信息显示 Tab 页
	private VerticalLayout addressTabVLayout;					// 客户地址Tab 页布局管理器
	private CustomerAddressView customerAddressView;			// 客户地址信息的显示表格
	private FlipOverTableComponent<Address> addressTableFlip;	// 客户地址信息显示表格的翻页组件
	
	private User loginUser;										// 当前登陆用户
	private CustomerResource customerResource;					// 客户资源对象，是以上两个Tab 的数据源
	
	public CustomerDetailInfoTabSheetView(User loginUser) {
		this.setWidth("100%");
		this.setHeight("193px");
		this.loginUser = loginUser;
		
		customerDetailInfoTabSheet = new TabSheet();
		customerDetailInfoTabSheet.setWidth("100%");
		this.addComponent(customerDetailInfoTabSheet);
		
		// 创建客户描述信息显示 Tab 页
		createDescriptionTab();
		
		// 创建客户地址信息显示 Tab 页
		createAddressTab();
		
		customerDetailInfoTabSheet.addListener(new SelectedTabChangeListener() {
			@Override
			public void selectedTabChange(SelectedTabChangeEvent event) {
				Component tab = customerDetailInfoTabSheet.getSelectedTab();
				if(tab == descriptionTabVLayout) {
					customerDescriptionView.echoCustomerDescription(customerResource);	// 回显指定任务对应客户的所有描述信息
				} else if(tab == addressTabVLayout) {
					customerAddressView.echoCustomerAddress(customerResource);			// 回显指定任务对应客户的所有地址信息
				}
			}
		});
	}
	
	/**
	 *  创建客户描述信息显示 Tab 页
	 */
	private void createDescriptionTab() {
		descriptionTabVLayout = new VerticalLayout();

		customerDescriptionView = new CustomerDescriptionView(loginUser);
		customerDescriptionView.setMargin(true, true, false, true);
		descriptionTabVLayout.addComponent(customerDescriptionView);
		descriptionTableFlip = customerDescriptionView.getDescriptionTableFlip();
		customerDetailInfoTabSheet.addTab(descriptionTabVLayout, "客户描述信息", ResourceDataCsr.customer_description_16_ico);
	}

	/**
	 *  创建地址信息Tab 页
	 */
	private void createAddressTab() {
		addressTabVLayout = new VerticalLayout();

		customerAddressView = new CustomerAddressView();
		customerAddressView.setMargin(true, true, false, true);
		addressTabVLayout.addComponent(customerAddressView);
		addressTableFlip = customerAddressView.getAddressTableFlip();
		customerDetailInfoTabSheet.addTab(addressTabVLayout, "客户地址信息", ResourceDataCsr.address_16_ico);
	}

	/**
	 * 设置客户描述信息与客户地址信息 表格的显示行数，以及其对应的翻页组件的搜索条数
	 * @param len 行数
	 */
	public void setTablePageLength(int len) {
		descriptionTableFlip.getTable().setPageLength(len);
		descriptionTableFlip.setPageLength(len, false);
		addressTableFlip.getTable().setPageLength(len);
		addressTableFlip.setPageLength(len, false);
	}

	public CustomerDescriptionView getCustomerDescriptionView() {
		return customerDescriptionView;
	}
	
	public CustomerAddressView getCustomerAddressView() {
		return customerAddressView;
	}
	
	public TabSheet getCustomerDetailInfoTabSheet() {
		return customerDetailInfoTabSheet;
	}

	public void setCustomerResource(CustomerResource customerResource) {
		this.customerResource = customerResource;
	}
	
}
