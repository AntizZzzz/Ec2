package com.jiangyifen.ec2.ui.report.tabsheet;

import com.jiangyifen.ec2.ui.report.tabsheet.top.TopUiAutodialDetail;

import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class AutodialDetail extends VerticalLayout {
	private TopUiAutodialDetail autodialDetail;
	private TabSheet tabSheet;

	public AutodialDetail() {

		this.setSizeFull();
		// this.setMargin(false, false, false, true);

		autodialDetail = new TopUiAutodialDetail(this);
		this.addComponent(autodialDetail);

		tabSheet = new TabSheet();
		tabSheet.setSizeFull();

		this.addComponent(tabSheet);
		this.setExpandRatio(tabSheet, 1);
	}

	public TabSheet getTabSheet() {
		return tabSheet;
	}

}
