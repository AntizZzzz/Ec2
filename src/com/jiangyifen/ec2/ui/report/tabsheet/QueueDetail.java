package com.jiangyifen.ec2.ui.report.tabsheet;

import com.jiangyifen.ec2.ui.report.tabsheet.top.TopUiQueueDetail;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class QueueDetail extends VerticalLayout {
	private TopUiQueueDetail queueDetail;
	private TabSheet tabSheet;

	public QueueDetail() {

		this.setSizeFull();
		// this.setMargin(false, false, false, true);

		queueDetail = new TopUiQueueDetail(this);
		this.addComponent(queueDetail);

		tabSheet = new TabSheet();
		tabSheet.setSizeFull();

		this.addComponent(tabSheet);
		this.setExpandRatio(tabSheet, 1);
	}

	public TabSheet getTabSheet() {
		return tabSheet;
	}

}
