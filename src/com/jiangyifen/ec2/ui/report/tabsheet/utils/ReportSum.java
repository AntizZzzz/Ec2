package com.jiangyifen.ec2.ui.report.tabsheet.utils;

import java.util.List;

public class ReportSum {

	/**
	 * @param list
	 *            数据源
	 * @param index
	 *            需要求和的列的下标（从0开始）
	 * @return
	 */
	public static List<Object[]> sum(List<Object[]> list, int... index) {

		if (list.size() != 0) {

			Object[] sum = new Object[list.get(0).length];

			for (int i = 0; i < index[0]; i++) {

				sum[i] = "全部";
			}

			for (int i : index) {
				sum[i] = 0;
			}

			for (Object[] objects : list) {

				for (int i : index) {
					sum[i] = Integer.parseInt(sum[i].toString())
							+ Integer.parseInt(objects[i].toString());
				}

			}

			list.add(sum);
		}

		return list;
	}
}
