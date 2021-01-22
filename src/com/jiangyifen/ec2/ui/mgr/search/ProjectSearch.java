package com.jiangyifen.ec2.ui.mgr.search;

import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

/**
 * 按项目搜索
 * 
 * @author chb
 * 
 */
@SuppressWarnings("serial")
public class ProjectSearch extends VerticalLayout {
	private SearchTabSheet tabSheet;

	/**
	 * 构造器
	 */
	public ProjectSearch(SearchTabSheet tabSheet) {
		this.setSizeFull();
		this.tabSheet = tabSheet;

		// 约束组件，使组件紧密排列
		HorizontalLayout constrantLayout = new HorizontalLayout();
		constrantLayout.setWidth("100%");
		constrantLayout.setMargin(true);
		constrantLayout.setSpacing(true);
		this.addComponent(constrantLayout);

		// 添加组件
		constrantLayout.addComponent(new Label("按项目搜索"));
	}

	/**
	 * 执行搜索
	 * 
	 * @param
	 */
	public void executeSearch(String selectSql, String searchSql) {
		tabSheet.executeSearch(selectSql, searchSql);
	}
}
