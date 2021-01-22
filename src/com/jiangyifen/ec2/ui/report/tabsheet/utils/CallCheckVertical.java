package com.jiangyifen.ec2.ui.report.tabsheet.utils;

import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class CallCheckVertical extends VerticalLayout {

	public CallCheckVertical(VerticalLayout... columnCallChecks) {

		this.setWidth("100%");
		this.setMargin(true);
		this.setSpacing(true);

		for (VerticalLayout columnCallCheck : columnCallChecks) {
			this.addComponent(columnCallCheck);
		}

	}
}
