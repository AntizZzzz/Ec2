package com.jiangyifen.ec2.ui.report.tabsheet;

import com.jiangyifen.ec2.ui.report.tabsheet.top.TopUiConcurrentStatistics;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class ConcurrentStatics extends VerticalLayout {
	private TopUiConcurrentStatistics concurrentStatistics;
	private TabSheet tabSheet;

	public ConcurrentStatics() {

		this.setSizeFull();
		// this.setMargin(false, false, false, true);

		concurrentStatistics = new TopUiConcurrentStatistics(this);
		this.addComponent(concurrentStatistics);

		tabSheet = new TabSheet();
		tabSheet.setSizeFull();

		this.addComponent(tabSheet);
		this.setExpandRatio(tabSheet, 1);
	}

	public TabSheet getTabSheet() {
		return tabSheet;
	}

}
