package com.jiangyifen.ec2.ui.report.tabsheet;

import com.jiangyifen.ec2.ui.report.tabsheet.top.TopUiCsrWorkDetail;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class CsrWork extends VerticalLayout {
	private TopUiCsrWorkDetail csrWorkDetail;
	private TabSheet tabSheet;

	public CsrWork() {

		this.setSizeFull();
		// this.setMargin(false, false, false, true);

		csrWorkDetail = new TopUiCsrWorkDetail(this);
		this.addComponent(csrWorkDetail);

		tabSheet = new TabSheet();
		tabSheet.setSizeFull();

		this.addComponent(tabSheet);
		this.setExpandRatio(tabSheet, 1);
	}

	public TabSheet getTabSheet() {
		return tabSheet;
	}

}
