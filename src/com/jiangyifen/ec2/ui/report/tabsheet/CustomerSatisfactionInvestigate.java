package com.jiangyifen.ec2.ui.report.tabsheet;

import com.jiangyifen.ec2.ui.report.tabsheet.top.TopUiCustomerSatisfactionInvestigate;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class CustomerSatisfactionInvestigate extends VerticalLayout {
	private TopUiCustomerSatisfactionInvestigate customerSatisfactionInvestigate;
	private TabSheet tabSheet;

	public CustomerSatisfactionInvestigate() {

		this.setSizeFull();
		// this.setMargin(false, false, false, true);

		customerSatisfactionInvestigate = new TopUiCustomerSatisfactionInvestigate(
				this);
		this.addComponent(customerSatisfactionInvestigate);

		tabSheet = new TabSheet();
		tabSheet.setSizeFull();

		this.addComponent(tabSheet);
		this.setExpandRatio(tabSheet, 1);
	}

	public TabSheet getTabSheet() {
		return tabSheet;
	}

}
