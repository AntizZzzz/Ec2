package com.jiangyifen.ec2.backgroundthread;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jiangyifen.ec2.report.Report;

public class ReportGenerator {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private static final int INTERVAL = 1000 * 10 * 60;// 十分钟 TODO
	
	// list中注入值
	private List<Report> reports;

	// 注入完成之后，调用此函数，启动线程
	public void runThreads() {

		for (int i = 0; i < reports.size(); i++) {
			final Report report = reports.get(i);
			// 为每个报表启动新线程
			Thread thread = new Thread(new Runnable() {

				@SuppressWarnings("static-access")
				@Override
				public void run() {
					// 系统启动时，先睡眠两分钟
					try {
						Thread.currentThread().sleep(120000);
					} catch (InterruptedException e2) {

						logger.error(e2.getMessage());
					}
					while (true) {
						try {
							report.execute();
						} catch (Exception e1) {
							logger.info("lc: 报表运行异常！ " + report.getReportName());
						}

						try {
							Thread.currentThread().sleep(INTERVAL);
						} catch (InterruptedException e) {
							logger.info("lc: 线程睡眠异常  " + report.getReportName());
							throw new RuntimeException("lc: 线程睡眠异常  "
									+ report.getReportName());
						}
					}
				}
			}, report.getReportName());

			// Spring 停止时停止所有报表线程
			thread.setDaemon(true);
			thread.start();
		}
	}

	// ================== Getter and Setter ================================//
	public List<Report> getReports() {
		return reports;
	}

	public void setReports(List<Report> reports) {
		this.reports = reports;
	}
}
