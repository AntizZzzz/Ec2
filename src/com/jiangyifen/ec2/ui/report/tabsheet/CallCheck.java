package com.jiangyifen.ec2.ui.report.tabsheet;

import com.jiangyifen.ec2.ui.report.tabsheet.top.TopUiCallCheck;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class CallCheck extends VerticalLayout {
	private TopUiCallCheck callCheck;
	private TabSheet tabSheet;

	public CallCheck() {

		this.setSizeFull();
		// this.setMargin(false, false, false, true);

		callCheck = new TopUiCallCheck(this);
		this.addComponent(callCheck);

		tabSheet = new TabSheet();
		tabSheet.setSizeFull();

		this.addComponent(tabSheet);
		this.setExpandRatio(tabSheet, 1);
	}

	public TabSheet getTabSheet() {
		return tabSheet;
	}

}
