package com.jiangyifen.ec2.ui.report.tabsheet;

import com.jiangyifen.ec2.ui.report.tabsheet.top.TopUiProjectFinishedStatus;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;

/**
 * 项目完成情况报表
 *  
 * @author chb
 */
@SuppressWarnings("serial")
public class ProjectFinishedStatus extends VerticalLayout{
	private TopUiProjectFinishedStatus topUiProjectFinishedStatus;
	private TabSheet tabSheet;

	public ProjectFinishedStatus() {

		this.setSizeFull();

		topUiProjectFinishedStatus = new TopUiProjectFinishedStatus(this);
		this.addComponent(topUiProjectFinishedStatus);

		tabSheet = new TabSheet();
		tabSheet.setSizeFull();

		this.addComponent(tabSheet);
		this.setExpandRatio(tabSheet, 1);
	}

	public TabSheet getTabSheet() {
		return tabSheet;
	}
}
