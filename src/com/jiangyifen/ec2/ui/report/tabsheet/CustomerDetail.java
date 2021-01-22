package com.jiangyifen.ec2.ui.report.tabsheet;

import com.jiangyifen.ec2.ui.report.tabsheet.top.TopUiCustomerDetail;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class CustomerDetail extends VerticalLayout {
	private TopUiCustomerDetail customerDetail;
	private TabSheet tabSheet;

	public CustomerDetail() {

		this.setSizeFull();

		customerDetail = new TopUiCustomerDetail(this);
		this.addComponent(customerDetail);

		tabSheet = new TabSheet();
		tabSheet.setSizeFull();

		this.addComponent(tabSheet);
		this.setExpandRatio(tabSheet, 1);
	}

	public TabSheet getTabSheet() {
		return tabSheet;
	}

}
