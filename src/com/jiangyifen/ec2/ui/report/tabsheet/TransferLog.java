package com.jiangyifen.ec2.ui.report.tabsheet;

import com.jiangyifen.ec2.ui.report.tabsheet.top.TopUiTransferLog;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class TransferLog extends VerticalLayout {
	private TopUiTransferLog transferLog;
	private TabSheet tabSheet;

	public TransferLog() {

		this.setSizeFull();

		transferLog = new TopUiTransferLog(this);
		this.addComponent(transferLog);

		tabSheet = new TabSheet();
		tabSheet.setSizeFull();

		this.addComponent(tabSheet);
		this.setExpandRatio(tabSheet, 1);
	}

	public TabSheet getTabSheet() {
		return tabSheet;
	}

}
