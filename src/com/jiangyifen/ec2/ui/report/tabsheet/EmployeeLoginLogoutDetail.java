package com.jiangyifen.ec2.ui.report.tabsheet;

import com.jiangyifen.ec2.ui.report.tabsheet.top.TopUiEmployeeLoginLogoutDetail;

import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class EmployeeLoginLogoutDetail extends VerticalLayout {
	private TopUiEmployeeLoginLogoutDetail detail;
	private TabSheet tabSheet;

	public EmployeeLoginLogoutDetail() {

		this.setSizeFull();

		detail = new TopUiEmployeeLoginLogoutDetail(this);
		this.addComponent(detail);

		tabSheet = new TabSheet();
		tabSheet.setSizeFull();

		this.addComponent(tabSheet);
		this.setExpandRatio(tabSheet, 1);
	}

	public TabSheet getTabSheet() {
		return tabSheet;
	}

}
