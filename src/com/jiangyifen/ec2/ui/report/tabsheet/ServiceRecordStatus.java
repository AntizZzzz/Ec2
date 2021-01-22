package com.jiangyifen.ec2.ui.report.tabsheet;

import com.jiangyifen.ec2.ui.report.tabsheet.top.TopUiServiceRecordStatus;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;

/**
 * 服务记录情况报表
 * 
 * @author chb
 */
@SuppressWarnings("serial")
public class ServiceRecordStatus extends VerticalLayout {
	private TopUiServiceRecordStatus topUiServiceRecordStatus;
	private TabSheet tabSheet;

	public ServiceRecordStatus() {

		this.setSizeFull();

		topUiServiceRecordStatus = new TopUiServiceRecordStatus(this);
		this.addComponent(topUiServiceRecordStatus);

		tabSheet = new TabSheet();
		tabSheet.setSizeFull();

		this.addComponent(tabSheet);
		this.setExpandRatio(tabSheet, 1);
	}

	public TabSheet getTabSheet() {
		return tabSheet;
	}
}
