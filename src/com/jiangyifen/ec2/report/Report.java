package com.jiangyifen.ec2.report;


public interface Report {
	/**
	 * <p>代码内容应该完成：</p>
	 * {
	 * 	<p>从代码中获取最后一个小时或者日的数据以确定下一个小时或者日期time</p>
	 *  <p>调用生成数据的方法并将time作为参数</p>
	 *  <p>生成数据的方法用循环每次处理1000条数据</p>
	 * }
	 */
	public void execute();

	/**
	 * 取得报表名字作为线程名
	 */
	public String getReportName();
}
