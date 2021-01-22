package com.jiangyifen.ec2.ui.report.tabsheet;

import com.jiangyifen.ec2.ui.report.tabsheet.top.TopUiCallCountCheck;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class CallCheckByCallTimeLength extends VerticalLayout {
	private TopUiCallCountCheck byCallTimeLength;
	private TabSheet tabSheet;

	public CallCheckByCallTimeLength() {

		this.setSizeFull();

		byCallTimeLength = new TopUiCallCountCheck(this);
		this.addComponent(byCallTimeLength);

		tabSheet = new TabSheet();
		tabSheet.setSizeFull();

		this.addComponent(tabSheet);
		this.setExpandRatio(tabSheet, 1);
	}

	public TabSheet getTabSheet() {
		return tabSheet;
	}

}
