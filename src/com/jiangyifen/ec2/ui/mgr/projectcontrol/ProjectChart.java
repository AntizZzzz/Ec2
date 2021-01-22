package com.jiangyifen.ec2.ui.mgr.projectcontrol;

import java.util.LinkedHashSet;

import com.invient.vaadin.charts.InvientCharts;
import com.invient.vaadin.charts.InvientCharts.DecimalPoint;
import com.invient.vaadin.charts.InvientCharts.SeriesType;
import com.invient.vaadin.charts.InvientCharts.XYSeries;
import com.invient.vaadin.charts.InvientChartsConfig;
import com.invient.vaadin.charts.InvientChartsConfig.PieConfig;
import com.invient.vaadin.charts.InvientChartsConfig.PieDataLabel;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class ProjectChart extends VerticalLayout {	
/**
 * 图表信息
 */
	private InvientCharts charts;
	private XYSeries taskInfo;
/**
 * 	图表数据
 */
	private LinkedHashSet<DecimalPoint> decimalPoints;
	
/**
 * 构造器
 */
	public ProjectChart() {
		this.setSizeFull();
		
		//图表全部配置信息
		InvientChartsConfig chartsConfig = new InvientChartsConfig();
		chartsConfig.getGeneralChartConfig().setType(SeriesType.PIE);//设置为饼图
		chartsConfig.getTitle().setText("项目信息"); //设置标题
		chartsConfig.getLegend().setEnabled(true);
		
		//饼图配置信息
		PieConfig pieConfig = new PieConfig();
		pieConfig.setAllowPointSelect(true);//设置为可选中状态
		pieConfig.setCursor("pointer"); //设置鼠标为手型
		pieConfig.setDataLabel(new PieDataLabel());  //设置Label标签为饼图标签(线宽、线长、颜色等)
		pieConfig.getDataLabel().setEnabled(true); //设置标签为显示状态
		pieConfig.getDataLabel().setFormatterJsFunc(
				"function() {"
						+ " return '' + this.point.name +': <b>'+ this.y +'</b>条'; "
						+ "}");
		chartsConfig.addSeriesConfig(pieConfig); //添加到总配置信息
		chartsConfig.getCredit().setEnabled(false); //显示HighCharts图标
		
		//图表数据
		taskInfo= new XYSeries("任务信息");
		taskInfo.setSeriesPoints(null);
		
		//根据已经配置好的信息创建饼图
		charts = new InvientCharts(chartsConfig);
		charts.addSeries(taskInfo); //添加空数据
		charts.setWidth("300px");
		charts.setHeight("250px");
		this.addComponent(charts);
	}
	/**
	 * 更新此图表信息
	 * @param finished
	 * @param notFinshed
	 * @param notDistributed
	 */
	public void updateProjectChart(Long finishedNum, Long notFinishedNum,
			Long notDistributedNum){
		//更新饼图数据源
		taskInfo.removeAllPoints();
		decimalPoints = new LinkedHashSet<InvientCharts.DecimalPoint>();
		decimalPoints.add(new DecimalPoint(taskInfo, "已完成", finishedNum));
		decimalPoints.add(new DecimalPoint(taskInfo, "未完成", notFinishedNum));
		decimalPoints.add(new DecimalPoint(taskInfo, "未分配", notDistributedNum));
		taskInfo.setSeriesPoints(decimalPoints);
		//更新饼图的一组数据
		charts.removeSeries(taskInfo); 
		charts.addSeries(taskInfo); //调用了Repaint方法
	}
}
