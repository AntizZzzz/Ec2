package com.jiangyifen.ec2.ui.report.tabsheet.utils;

import java.util.LinkedHashSet;
import java.util.List;

import com.invient.vaadin.charts.InvientCharts;
import com.invient.vaadin.charts.InvientCharts.DecimalPoint;
import com.invient.vaadin.charts.InvientCharts.Series;
import com.invient.vaadin.charts.InvientCharts.SeriesType;
import com.invient.vaadin.charts.InvientCharts.XYSeries;
import com.invient.vaadin.charts.InvientChartsConfig;
import com.invient.vaadin.charts.InvientChartsConfig.AreaConfig;
import com.invient.vaadin.charts.InvientChartsConfig.AxisBase.AxisTitle;
import com.invient.vaadin.charts.InvientChartsConfig.CategoryAxis;

import com.invient.vaadin.charts.InvientChartsConfig.HorzAlign;
import com.invient.vaadin.charts.InvientChartsConfig.Legend;
import com.invient.vaadin.charts.InvientChartsConfig.GeneralChartConfig.ZoomType;
import com.invient.vaadin.charts.InvientChartsConfig.Legend.Layout;
import com.invient.vaadin.charts.InvientChartsConfig.MarkerState;
import com.invient.vaadin.charts.InvientChartsConfig.NumberYAxis;
import com.invient.vaadin.charts.InvientChartsConfig.Position;
import com.invient.vaadin.charts.InvientChartsConfig.ScatterConfig;
import com.invient.vaadin.charts.InvientChartsConfig.Stacking;
import com.invient.vaadin.charts.InvientChartsConfig.SymbolMarker;
import com.invient.vaadin.charts.InvientChartsConfig.SymbolMarker.Symbol;
import com.invient.vaadin.charts.InvientChartsConfig.Tooltip;
import com.invient.vaadin.charts.InvientChartsConfig.VertAlign;
import com.invient.vaadin.charts.InvientChartsConfig.XAxis;
import com.invient.vaadin.charts.InvientChartsConfig.YAxis;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class ReportAreaUtil extends VerticalLayout {

	private InvientChartsConfig chartsConfig;
	private CategoryAxis axis1;
	private LinkedHashSet<XAxis> xAxesSet;
	private NumberYAxis yAxis;
	private LinkedHashSet<YAxis> yAxesSet;
	private Legend legend;
	private AreaConfig areaConfig;
	private Tooltip tooltip;
	private InvientCharts charts;
	private XYSeries seriesDate;

	public ReportAreaUtil(String mainTitle, String yAxisTitle,
			String xySeriesName, List<String> xAxis, List<Double> datas) {

		this.setSizeFull();
		this.setSpacing(true);
		this.setMargin(true, false, false, false);

		chartsConfig = new InvientChartsConfig();
		chartsConfig.getGeneralChartConfig().setType(SeriesType.AREA);
		chartsConfig.getGeneralChartConfig().setZoomType(ZoomType.XY);
		chartsConfig.getTitle().setText(mainTitle);

		axis1 = new CategoryAxis();
		axis1.setCategories(xAxis);

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

		areaConfig = new AreaConfig();

		areaConfig.setStacking(Stacking.NORMAL);
		SymbolMarker marker = new SymbolMarker();
		areaConfig.setMarker(marker);
		marker.setEnabled(false);
		marker.setSymbol(Symbol.DIAMOND);
		marker.setRadius(2);
		marker.setHoverState(new MarkerState(true));

		chartsConfig.addSeriesConfig(areaConfig);

		tooltip = new Tooltip();
		tooltip.setFormatterJsFunc("function() { "
				+ " return '<b>' + this.series.name + '</b><br/>' + ': '+ this.y "
				+ "}");

		chartsConfig.setTooltip(tooltip);
		chartsConfig.getCredit().setEnabled(false);

		charts = new InvientCharts(chartsConfig);
		charts.setHeight("90%");
		charts.setSizeFull();

		ScatterConfig maleScatterCfg = new ScatterConfig();

		seriesDate = new XYSeries(xySeriesName, maleScatterCfg);
		seriesDate.setSeriesPoints(getPoints(seriesDate, datas));

		charts.addSeries(seriesDate);

		this.addComponent(charts);
		this.setComponentAlignment(charts, Alignment.TOP_CENTER);

	}

	private static LinkedHashSet<DecimalPoint> getPoints(Series series,
			List<Double> datas) {
		LinkedHashSet<DecimalPoint> points = new LinkedHashSet<DecimalPoint>();
		for (double value : datas) {
			points.add(new DecimalPoint(series, value));
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
