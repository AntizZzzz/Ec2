package com.jiangyifen.ec2.ui.report.tabsheet;

import com.jiangyifen.ec2.ui.report.tabsheet.top.TopUiQuestionnaire;

import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class QuestionnaireDetail extends VerticalLayout {
	private TopUiQuestionnaire questionnaire;
	private TabSheet tabSheet;

	public QuestionnaireDetail() {

		this.setSizeFull();

		questionnaire = new TopUiQuestionnaire(this);
		this.addComponent(questionnaire);

		tabSheet = new TabSheet();
		tabSheet.setSizeFull();

		this.addComponent(tabSheet);
		this.setExpandRatio(tabSheet, 1);
	}

	public TabSheet getTabSheet() {
		return tabSheet;
	}

}
