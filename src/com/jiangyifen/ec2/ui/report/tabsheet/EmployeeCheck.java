package com.jiangyifen.ec2.ui.report.tabsheet;

import com.jiangyifen.ec2.ui.report.tabsheet.top.TopUiEmployeeCheck;

import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class EmployeeCheck extends VerticalLayout {
	private TopUiEmployeeCheck employeeCheck;
	private TabSheet tabSheet;

	public EmployeeCheck() {

		this.setSizeFull();

		employeeCheck = new TopUiEmployeeCheck(this);
		this.addComponent(employeeCheck);

		tabSheet = new TabSheet();
		tabSheet.setSizeFull();

		this.addComponent(tabSheet);
		this.setExpandRatio(tabSheet, 1);
	}

	public TabSheet getTabSheet() {
		return tabSheet;
	}

}
