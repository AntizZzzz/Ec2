package com.jiangyifen.ec2.ui.csr.workarea.common;

import com.jiangyifen.ec2.bean.RoleType;
import com.jiangyifen.ec2.entity.CustomerResource;
import com.jiangyifen.ec2.entity.CustomerServiceRecord;
import com.jiangyifen.ec2.entity.Orderdetails;
import com.jiangyifen.ec2.entity.User;
import com.jiangyifen.ec2.globaldata.ResourceDataCsr;
import com.jiangyifen.ec2.ui.FlipOverTableComponent;
import com.vaadin.ui.Component;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;
import com.vaadin.ui.TabSheet.SelectedTabChangeListener;
import com.vaadin.ui.VerticalLayout;

/**
 * @Description 描述：
 *
 * @author  JRH
 * @date    2014年5月26日 下午4:50:51
 * @version v1.0.0
 */
@SuppressWarnings("serial")
public class CustomerLogTabSheetView extends VerticalLayout {
	
	private TabSheet customerLogTabSheet;				// 客服记录TabSheet
	
	// 客服记录信息显示 Tab 页
	private VerticalLayout recordTabVLayout;									// 客服记录Tab 页布局管理器
	private HistoryRecordView serviceRecordView;								// 对该客户做的所有历史记录
	private FlipOverTableComponent<CustomerServiceRecord> recordTableFlip;		// 历史记录Table的翻页组件

	
	// 客户订单详情显示 Tab 页
	private VerticalLayout detailTabVLayout;									// 客户地址Tab 页布局管理器
	private HistoryOrderDetailView orderDetailView;								// 该客户所有买过的商品详单记录
	private FlipOverTableComponent<Orderdetails> detailTableFlip;				// 历史订单详情Table的翻页组件
	
	private User loginUser;														// 当前登陆用户
	private RoleType roleType;													// 角色类型
	private CustomerResource customerResource;									// 客户资源对象，是以上两个Tab 的数据源
	
	public CustomerLogTabSheetView(User loginUser, RoleType roleType) {
		this.setWidth("100%");
		this.loginUser = loginUser;
		this.roleType = roleType;
		
		customerLogTabSheet = new TabSheet();
		customerLogTabSheet.setWidth("100%");
		this.addComponent(customerLogTabSheet);
		
		// 创建客服记录信息显示 Tab 页
		createServiceRecordTab();
		
		// 创建客户订单详情显示 Tab 页
		createOrderDetailTab();
		
		customerLogTabSheet.addListener(new SelectedTabChangeListener() {
			@Override
			public void selectedTabChange(SelectedTabChangeEvent event) {
				Component tab = customerLogTabSheet.getSelectedTab();
				if(tab == recordTabVLayout) {
					serviceRecordView.echoHistoryRecord(customerResource);	// 回显指定任务对应客户的所有服务记录信息
				} else if(tab == detailTabVLayout) {
					orderDetailView.echoOrderDetailInfos(customerResource);			// 显示该条任务对应客户相关的所有的订单详情信息
				}
			}
		});
	}
	
	/**
	 *  创建客服记录信息显示 Tab 页
	 */
	private void createServiceRecordTab() {
		recordTabVLayout = new VerticalLayout();
		
		serviceRecordView = new HistoryRecordView(loginUser, roleType);
		recordTabVLayout.addComponent(serviceRecordView);
		recordTableFlip = serviceRecordView.getHistoryTableFlip();
		customerLogTabSheet.addTab(recordTabVLayout, "服务记录", ResourceDataCsr.customer_description_16_ico);
	}

	/**
	 *  创建地址信息Tab 页
	 */
	private void createOrderDetailTab() {
		detailTabVLayout = new VerticalLayout();

		orderDetailView = new HistoryOrderDetailView(loginUser, roleType);
		detailTabVLayout.addComponent(orderDetailView);
		detailTableFlip = orderDetailView.getDetailTableFlip();
		customerLogTabSheet.addTab(detailTabVLayout, "订单记录", ResourceDataCsr.cart_16_ico);
	}

	/**
	 * 设置客服记录与客户订单详情 表格的显示行数，以及其对应的翻页组件的搜索条数
	 * @param len 行数
	 */
	public void setTablePageLength(int len) {
		recordTableFlip.getTable().setPageLength(len);
		recordTableFlip.setPageLength(len, false);
		detailTableFlip.getTable().setPageLength(len+4);
		detailTableFlip.setPageLength(len+4, false);
	}

	public TabSheet getCustomerLogTabSheet() {
		return customerLogTabSheet;
	}

	public HistoryRecordView getServiceRecordView() {
		return serviceRecordView;
	}

	public HistoryOrderDetailView getOrderDetailView() {
		return orderDetailView;
	}

	public void setCustomerResource(CustomerResource customerResource) {
		this.customerResource = customerResource;
	}
	
}
