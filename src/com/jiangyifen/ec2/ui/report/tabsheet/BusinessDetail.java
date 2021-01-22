package com.jiangyifen.ec2.ui.report.tabsheet;

import com.jiangyifen.ec2.ui.report.tabsheet.top.TopUiBusinessDetail;

import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class BusinessDetail extends VerticalLayout {
	private TopUiBusinessDetail businessDetail;
	private TabSheet tabSheet;

	public BusinessDetail() {

		this.setSizeFull();

		businessDetail = new TopUiBusinessDetail(this);
		this.addComponent(businessDetail);

		tabSheet = new TabSheet();
		tabSheet.setSizeFull();

		this.addComponent(tabSheet);
		this.setExpandRatio(tabSheet, 1);
	}

	public TabSheet getTabSheet() {
		return tabSheet;
	}

}
