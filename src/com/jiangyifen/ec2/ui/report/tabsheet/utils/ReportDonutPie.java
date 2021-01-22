package com.jiangyifen.ec2.ui.report.tabsheet.utils;

import java.util.LinkedHashSet;
import java.util.List;

import com.invient.vaadin.charts.InvientCharts;
import com.invient.vaadin.charts.InvientChartsConfig;
import com.invient.vaadin.charts.InvientCharts.DecimalPoint;
import com.invient.vaadin.charts.InvientCharts.Series;
import com.invient.vaadin.charts.InvientCharts.SeriesType;
import com.invient.vaadin.charts.InvientCharts.XYSeries;

import com.invient.vaadin.charts.InvientChartsConfig.PieConfig;
import com.invient.vaadin.charts.InvientChartsConfig.PieDataLabel;

import com.vaadin.ui.Alignment;

import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class ReportDonutPie extends VerticalLayout {

	private InvientChartsConfig chartsConfig;

	private InvientCharts charts;
	private XYSeries seriesDate;
	private PieConfig pieConfig;
	private XYSeries series;
	private LinkedHashSet<DecimalPoint> points;

	public ReportDonutPie(String mainTitle, String yAxisTitle,
			String xySeriesName, List<String> xAxis, List<Double> datas) {

		this.setSizeFull();
		this.setSpacing(true);
		this.setMargin(true, false, false, false);

		chartsConfig = new InvientChartsConfig();
		chartsConfig.getGeneralChartConfig().setType(SeriesType.PIE);
		chartsConfig.getTitle().setText(mainTitle);

		chartsConfig.getTitle().setText(
				"Browser market shares at a specific website");
		chartsConfig.getSubtitle().setText(
				"Inner circle: 2008, outer circle: 2010");

		chartsConfig.getTooltip().setFormatterJsFunc(
				"function() {"
						+ " return '<b>'+ this.series.name +'</b><br/>'+ "
						+ "     this.point.name +': '+ this.y +' %'; " + "}");

		charts = new InvientCharts(chartsConfig);
		charts.setHeight("90%");
		charts.setSizeFull();

		pieConfig = new PieConfig();
		pieConfig.setInnerSize(150);
		pieConfig.setDataLabel(new PieDataLabel());

		series = new XYSeries("2010", SeriesType.PIE, pieConfig);
		points = new LinkedHashSet<DecimalPoint>();
		points.add(new DecimalPoint(series, "Firefox", 45.0));
		points.add(new DecimalPoint(series, "IE", 26.8));
		points.add(new DecimalPoint(series, "Chrome", 12.8));
		points.add(new DecimalPoint(series, "Safari", 8.5));
		points.add(new DecimalPoint(series, "Opera", 6.2));
		points.add(new DecimalPoint(series, "Mozilla", 0.2));
		series.setSeriesPoints(points);
		charts.addSeries(seriesDate);

		pieConfig = new PieConfig();
		pieConfig.setInnerSize(150);
		pieConfig.setDataLabel(new PieDataLabel());

		series = new XYSeries("2010", SeriesType.PIE, pieConfig);
		points = new LinkedHashSet<DecimalPoint>();

		points.add(new DecimalPoint(series, "Firefox", 45.0));
		points.add(new DecimalPoint(series, "IE", 26.8));
		points.add(new DecimalPoint(series, "Chrome", 12.8));
		points.add(new DecimalPoint(series, "Safari", 8.5));
		points.add(new DecimalPoint(series, "Opera", 6.2));
		points.add(new DecimalPoint(series, "Mozilla", 0.2));
		series.setSeriesPoints(points);

		charts.addSeries(series);

		this.addComponent(charts);
		this.setComponentAlignment(charts, Alignment.TOP_CENTER);

	}

	@SuppressWarnings("unused")
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
