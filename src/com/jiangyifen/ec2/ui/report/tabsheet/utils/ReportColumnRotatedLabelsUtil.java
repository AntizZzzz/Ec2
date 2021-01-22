package com.jiangyifen.ec2.ui.report.tabsheet.utils;

import java.util.LinkedHashSet;
import java.util.List;

import com.invient.vaadin.charts.Color;
import com.invient.vaadin.charts.InvientCharts;
import com.invient.vaadin.charts.InvientCharts.DecimalPoint;
import com.invient.vaadin.charts.InvientCharts.Series;
import com.invient.vaadin.charts.InvientCharts.SeriesType;
import com.invient.vaadin.charts.InvientCharts.XYSeries;
import com.invient.vaadin.charts.InvientChartsConfig;
import com.invient.vaadin.charts.InvientChartsConfig.AxisBase.AxisTitle;
import com.invient.vaadin.charts.InvientChartsConfig.CategoryAxis;
import com.invient.vaadin.charts.InvientChartsConfig.ColumnConfig;
import com.invient.vaadin.charts.InvientChartsConfig.DataLabel;
import com.invient.vaadin.charts.InvientChartsConfig.GeneralChartConfig.Margin;
import com.invient.vaadin.charts.InvientChartsConfig.HorzAlign;
import com.invient.vaadin.charts.InvientChartsConfig.Legend;
import com.invient.vaadin.charts.InvientChartsConfig.Legend.Layout;
import com.invient.vaadin.charts.InvientChartsConfig.NumberYAxis;
import com.invient.vaadin.charts.InvientChartsConfig.PointConfig;
import com.invient.vaadin.charts.InvientChartsConfig.Position;
import com.invient.vaadin.charts.InvientChartsConfig.Tooltip;
import com.invient.vaadin.charts.InvientChartsConfig.VertAlign;
import com.invient.vaadin.charts.InvientChartsConfig.XAxis;
import com.invient.vaadin.charts.InvientChartsConfig.XAxisDataLabel;
import com.invient.vaadin.charts.InvientChartsConfig.YAxis;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class ReportColumnRotatedLabelsUtil extends VerticalLayout {

	private InvientChartsConfig chartsConfig;
	private CategoryAxis axis1;
	private LinkedHashSet<XAxis> xAxesSet;
	private NumberYAxis yAxis;
	private LinkedHashSet<YAxis> yAxesSet;
	private Legend legend;
	private ColumnConfig columnConfig;
	private Tooltip tooltip;
	private InvientCharts charts;
	private XYSeries seriesDate;

	public ReportColumnRotatedLabelsUtil(String mainTitle, String yAxisTitle,
			String xySeriesName, List<String> xAxis, List<Double> datas) {

		this.setSizeFull();
		this.setSpacing(true);
		this.setMargin(true, false, false, false);

		chartsConfig = new InvientChartsConfig();
		chartsConfig.getGeneralChartConfig().setType(SeriesType.COLUMN);
		chartsConfig.getGeneralChartConfig().setMargin(new Margin());
		chartsConfig.getGeneralChartConfig().getMargin().setTop(50);
		chartsConfig.getGeneralChartConfig().getMargin().setRight(50);
		chartsConfig.getGeneralChartConfig().getMargin().setBottom(100);
		chartsConfig.getGeneralChartConfig().getMargin().setLeft(80);
		chartsConfig.getTitle().setText(mainTitle);

		axis1 = new CategoryAxis();
		axis1.setCategories(xAxis);
		axis1.setLabel(new XAxisDataLabel());
		axis1.getLabel().setRotation(-45);
		axis1.getLabel().setAlign(HorzAlign.RIGHT);
		axis1.getLabel()
				.setStyle("{ font: 'normal 13px Verdana, sans-serif' }");

		xAxesSet = new LinkedHashSet<InvientChartsConfig.XAxis>();
		xAxesSet.add(axis1);
		chartsConfig.setXAxes(xAxesSet);

		yAxis = new NumberYAxis();
		yAxis.setTitle(new AxisTitle(yAxisTitle));
		yAxesSet = new LinkedHashSet<InvientChartsConfig.YAxis>();
		yAxesSet.add(yAxis);
		chartsConfig.setYAxes(yAxesSet);

		legend = new Legend();
		legend.setFloating(true);
		legend.setLayout(Layout.HORIZONTAL);
		legend.setPosition(new Position());
		legend.getPosition().setAlign(HorzAlign.RIGHT);
		legend.getPosition().setVertAlign(VertAlign.TOP);
		legend.getPosition().setX(-10);
		legend.getPosition().setY(10);
		legend.setShadow(true);

		chartsConfig.setLegend(legend);

		columnConfig = new ColumnConfig();
		// columnConfig.setBorderWidth(20);

		chartsConfig.addSeriesConfig(columnConfig);

		tooltip = new Tooltip();
		tooltip.setFormatterJsFunc("function() { "
				+ " return '<b>' + this.series.name + '</b><br/>' + ': '+ this.y "
				+ "}");

		chartsConfig.setTooltip(tooltip);
		chartsConfig.getCredit().setEnabled(false);

		charts = new InvientCharts(chartsConfig);
		charts.setHeight("90%");
		charts.setSizeFull();

		ColumnConfig colCfg = new ColumnConfig();

		colCfg.setDataLabel(new DataLabel());
		colCfg.getDataLabel().setRotation(-90);		// 使得标签上的值逆向旋转90度
		colCfg.getDataLabel().setAlign(HorzAlign.CENTER);
		colCfg.getDataLabel().setX(-3);
		colCfg.getDataLabel().setY(30);
		colCfg.getDataLabel().setColor(new Color.RGB(255, 255, 255));
		colCfg.getDataLabel().setFormatterJsFunc(
				"function() {" + " return this.y; " + "}");
		colCfg.getDataLabel().setStyle(
				" { font: 'normal 13px Verdana, sans-serif' } ");

		seriesDate = new XYSeries(xySeriesName, colCfg);

		seriesDate.setSeriesPoints(getPoints(seriesDate, datas, new Color.RGB(
				128, 105, 155)));

		charts.addSeries(seriesDate);

		this.addComponent(charts);
		this.setComponentAlignment(charts, Alignment.TOP_CENTER);

	}

	private static LinkedHashSet<DecimalPoint> getPoints(Series series,
			List<Double> datas, Color color) {
		LinkedHashSet<DecimalPoint> points = new LinkedHashSet<DecimalPoint>();

		for (double value : datas) {
			DecimalPoint point = new DecimalPoint(series, value);
			point.setConfig(new PointConfig(color));
			points.add(point);
		}
		return points;
	}

	public XYSeries getSeriesDate() {
		return seriesDate;
	}

	public InvientCharts getCharts() {
		return charts;
	}

}
