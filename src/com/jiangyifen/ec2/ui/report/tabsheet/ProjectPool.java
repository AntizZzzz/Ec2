package com.jiangyifen.ec2.ui.report.tabsheet;

import com.jiangyifen.ec2.ui.report.tabsheet.top.TopUiProjectPool;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class ProjectPool extends VerticalLayout {
	private TopUiProjectPool projectPool;
	private TabSheet tabSheet;

	public ProjectPool() {

		this.setSizeFull();

		projectPool = new TopUiProjectPool(this);
		this.addComponent(projectPool);

		tabSheet = new TabSheet();
		tabSheet.setSizeFull();

		this.addComponent(tabSheet);
		this.setExpandRatio(tabSheet, 1);
	}

	public TabSheet getTabSheet() {
		return tabSheet;
	}

}
