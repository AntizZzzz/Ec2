package com.jiangyifen.ec2.ui.report.tabsheet;

import com.jiangyifen.ec2.ui.report.tabsheet.top.TopUiKPI;

import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class KPI extends VerticalLayout {
	private TopUiKPI kpi;
	private TabSheet tabSheet;

	public KPI() {

		this.setSizeFull();

		kpi = new TopUiKPI(this);
		this.addComponent(kpi);

		tabSheet = new TabSheet();
		tabSheet.setSizeFull();

		this.addComponent(tabSheet);
		this.setExpandRatio(tabSheet, 1);
	}

	public TabSheet getTabSheet() {
		return tabSheet;
	}

}
