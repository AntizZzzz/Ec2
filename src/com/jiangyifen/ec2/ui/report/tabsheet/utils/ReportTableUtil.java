package com.jiangyifen.ec2.ui.report.tabsheet.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Property;
import com.vaadin.ui.Alignment;

import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class ReportTableUtil extends VerticalLayout {

	private final Logger logger = LoggerFactory.getLogger(ReportTableUtil.class);
	private final SimpleDateFormat SDF_DAY = new SimpleDateFormat("yyyy-MM-dd");
	
	private Label title;

	private Table table;

	private List<String> names;// 报表列名(table列名)

	private List<Object[]> list;// 数据库检索结果

	private String titleName;

	private String startValue;// 开始时间

	private String endValue;// 结束时间

	public ReportTableUtil(List<Object[]> list, String titleName,
			List<String> names, String startValue, String endValue) {
		this.list = list;
		this.names = names;
		this.titleName = titleName;
		this.startValue = startValue;
		this.endValue = endValue;

		this.setSizeFull();
		this.setSpacing(true);
		this.setMargin(true);

		// jrh 给表格统计区设置标题
		// TODO 标题居中没起作用
		title = new Label("<font size='3'><B>" + titleName + "</B></font>",
				Label.CONTENT_XHTML);
		this.addComponent(title);
		this.setComponentAlignment(title, Alignment.MIDDLE_CENTER);

		table = new Table(){

			@Override
			protected String formatPropertyValue(Object rowId, Object colId, Property property) {
				if(property.getValue() == null || "".equals(property.getValue())) {
					return null;
				} else if(colId.equals("日期") && !"全部".equals(property.getValue())) {
					try {
						return SDF_DAY.format(SDF_DAY.parse((String) property.getValue()));
					} catch (ParseException e) {
						logger.warn("jinht -->> 日期转换时出现异常！"+e.getMessage(), e);
					}
				}
				return super.formatPropertyValue(rowId, colId, property);
			}
			
		};
		
		table.setSizeFull();
		table.setSelectable(true);
		// table.setFooterVisible(true);
		table.setStyleName("striped");
		table.setImmediate(true);
		table.setRowHeaderMode(Table.ROW_HEADER_MODE_INDEX);

		// 设置table列名
		for (String str : names) {
			table.addContainerProperty(str, String.class, null);
		}

		// table绑定Item
		for (Object[] objects : list) {
			table.addItem(objects, null);

		}

		this.addComponent(table);
		this.setExpandRatio(table, 1);

	}

	public Label getTitle() {
		return title;
	}

	public Table getTable() {
		return table;
	}

	public void setTable(Table table) {
		this.table = table;
	}

	public void setTitle(Label title) {
		this.title = title;
	}

	public List<String> getNames() {
		return names;
	}

	public List<Object[]> getList() {
		return list;
	}

	public String getTitleName() {
		return titleName;
	}

	public String getStartValue() {
		return startValue;
	}

	public String getEndValue() {
		return endValue;
	}

}
