package com.jiangyifen.ec2.ui.report.tabsheet.utils;

import java.util.Arrays;
import java.util.LinkedHashSet;

import com.invient.vaadin.charts.InvientCharts;
import com.invient.vaadin.charts.InvientCharts.DecimalPoint;
import com.invient.vaadin.charts.InvientCharts.PointClickEvent;
import com.invient.vaadin.charts.InvientCharts.SeriesType;
import com.invient.vaadin.charts.InvientCharts.XYSeries;
import com.invient.vaadin.charts.InvientChartsConfig;
import com.invient.vaadin.charts.InvientChartsConfig.AxisBase.AxisTitle;
import com.invient.vaadin.charts.InvientChartsConfig.AxisBase.NumberPlotLine;
import com.invient.vaadin.charts.InvientChartsConfig.AxisBase.NumberPlotLine.NumberValue;
import com.invient.vaadin.charts.InvientChartsConfig.CategoryAxis;
import com.invient.vaadin.charts.InvientChartsConfig.HorzAlign;
import com.invient.vaadin.charts.InvientChartsConfig.Legend;
import com.invient.vaadin.charts.InvientChartsConfig.Legend.Layout;
import com.invient.vaadin.charts.InvientChartsConfig.LineConfig;
import com.invient.vaadin.charts.InvientChartsConfig.NumberYAxis;
import com.invient.vaadin.charts.InvientChartsConfig.Position;
import com.invient.vaadin.charts.InvientChartsConfig.Tooltip;
import com.invient.vaadin.charts.InvientChartsConfig.VertAlign;
import com.invient.vaadin.charts.InvientChartsConfig.XAxis;
import com.invient.vaadin.charts.InvientChartsConfig.YAxis;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class InvientChartsLine extends VerticalLayout {

	public InvientChartsLine() {
		InvientChartsConfig chartsConfig = new InvientChartsConfig();
		chartsConfig.getGeneralChartConfig().setType(SeriesType.LINE);
		chartsConfig.getTitle().setText("This is my first test");

		CategoryAxis axis1 = new CategoryAxis();
		axis1.setCategories(Arrays.asList("Apples", "Oranges", "Pears",
				"Grapes", "Bananas"));

		NumberYAxis yAxis = new NumberYAxis();
		NumberPlotLine plotLine = new NumberPlotLine("test1");
		plotLine.setValue(new NumberValue(0.0));
		yAxis.setTitle(new AxisTitle("test"));
		yAxis.addPlotLine(plotLine);
		LinkedHashSet<XAxis> xAxesSet = new LinkedHashSet<InvientChartsConfig.XAxis>();
		xAxesSet.add(axis1);

		LinkedHashSet<YAxis> yAxesSet = new LinkedHashSet<InvientChartsConfig.YAxis>();
		yAxesSet.add(yAxis);

		chartsConfig.setXAxes(xAxesSet);
		chartsConfig.setYAxes(yAxesSet);

		Legend legend = new Legend();
		legend.setLayout(Layout.VERTICAL);
		Position position = new Position();
		position.setAlign(HorzAlign.RIGHT);
		position.setVertAlign(VertAlign.TOP);
		position.setX(-10);
		position.setY(100);
		legend.setPosition(position);
		chartsConfig.setLegend(legend);

		LineConfig lineConfig = new LineConfig();
		chartsConfig.addSeriesConfig(lineConfig);

		Tooltip tooltip = new Tooltip();
		tooltip.setFormatterJsFunc("function() {"
				+ " return '' + this.series.name +': '+ this.y +''; " + "}");

		chartsConfig.setTooltip(tooltip);
		chartsConfig.getCredit().setEnabled(false);

		InvientCharts charts = new InvientCharts(chartsConfig);

		XYSeries seriesDate = new XYSeries("Lc");

		DecimalPoint lcDecimalPoint1 = new DecimalPoint(seriesDate, 1, 2);
		DecimalPoint lcDecimalPoint2 = new DecimalPoint(seriesDate, 2, 5);
		DecimalPoint lcDecimalPoint3 = new DecimalPoint(seriesDate, 4, 8);
		LinkedHashSet<DecimalPoint> decimalPoints = new LinkedHashSet<InvientCharts.DecimalPoint>();
		decimalPoints.add(lcDecimalPoint1);
		decimalPoints.add(lcDecimalPoint2);
		decimalPoints.add(lcDecimalPoint3);
		seriesDate.setSeriesPoints(decimalPoints);

		charts.addSeries(seriesDate);

		XYSeries seriesDate2 = new XYSeries("Chb");

		LinkedHashSet<DecimalPoint> decimalPoints2 = new LinkedHashSet<InvientCharts.DecimalPoint>();
		decimalPoints2.add(new DecimalPoint(seriesDate2, 1));
		decimalPoints2.add(new DecimalPoint(seriesDate2, 2));
		decimalPoints2.add(new DecimalPoint(seriesDate2, 3));
		decimalPoints2.add(new DecimalPoint(seriesDate2, 4));
		seriesDate2.setSeriesPoints(decimalPoints2);

		charts.addSeries(seriesDate2);

		XYSeries seriesDate3 = new XYSeries("Jrh");

		DecimalPoint decimalPoint3 = new DecimalPoint(seriesDate3, 5, 6);
		LinkedHashSet<DecimalPoint> decimalPoints3 = new LinkedHashSet<InvientCharts.DecimalPoint>();
		decimalPoints3.add(decimalPoint3);
		seriesDate3.setSeriesPoints(decimalPoints3);

		charts.addSeries(seriesDate3);

		addComponent(charts);

		charts.addListener(new InvientCharts.PointClickListener() {

			public void pointClick(PointClickEvent pointClickEvent) {
				// TODO Auto-generated method stub
				getApplication().getMainWindow().showNotification(
						"PointX : "
								+ (Double) pointClickEvent.getPoint().getX()
								+ " PointY : "
								+ (Double) pointClickEvent.getPoint().getY());
			}
		});

	}
}
