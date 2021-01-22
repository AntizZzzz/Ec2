package com.jiangyifen.ec2.ui.mgr.tabsheet;

import com.jiangyifen.ec2.ui.mgr.search.SearchTabSheet;
import com.jiangyifen.ec2.ui.util.NotificationUtil;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class GlobalSearch extends VerticalLayout{
	/**
	 * 重要组件
	 */
	private SearchTabSheet tabSheet;
	private Table table;
	
	/**
	 * 其他组件
	 */

	
	/**
	 * 构造器
	 */
	public GlobalSearch() {
		this.setSizeFull();
		// 约束组件，使组件紧密排列
		VerticalLayout constrantLayout = new VerticalLayout();
		constrantLayout.setWidth("100%");
		constrantLayout.setMargin(true);
		constrantLayout.setSpacing(true);
		this.addComponent(constrantLayout);

		// 创建TabSheet输出区域
		tabSheet=new SearchTabSheet(this);
		tabSheet.setWidth("100%");
		constrantLayout.addComponent(tabSheet);
		
		//添加搜索结果描述信息
		constrantLayout.addComponent(new Label("搜索结果:"));
		
		// 创建搜索结果显示表格输出区域
		VerticalLayout tableLayout = buildTableLayout();
		constrantLayout.addComponent(tableLayout);
	}
	
	/**
	 * 创建表格输出区域
	 * @return
	 */
	private VerticalLayout buildTableLayout() {
		//表格组件
		VerticalLayout tableLayout = new VerticalLayout();
		table=new Table();
		table.setWidth("100%");
		table.setPageLength(15);
		tableLayout.setWidth("100%");
		tableLayout.addComponent(table);
		
		//表格下方的翻页组件
		return tableLayout;
	}

	/**
	 * 执行搜索
	 * @param 
	 */
	public void executeSearch(String selectSql,String countSql){
		NotificationUtil.showWarningNotification(this,"还没有开发完！");
System.out.println("========================");
System.out.println("selectSql:"+selectSql);
System.out.println("countSql:"+countSql);
System.out.println("========================");
	}

	
}
