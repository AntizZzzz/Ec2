package com.jiangyifen.ec2.ui.report.tabsheet.utils;

import java.io.File;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jxl.Workbook;
import jxl.write.WritableCellFormat;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

public class ExportExcelUtil {

	private static Logger logger = LoggerFactory
			.getLogger(ExportExcelUtil.class);

	private static WritableWorkbook writableWorkbook = null;

	private static WritableSheet sheet = null;

	private static File file = null;

	private static Properties properties;

	private static String path;

	public static File exportExcel(String titleName, List<Object[]> list,
			List<String> columnNames, String startValue, String endValue) {

		try {

			properties = new Properties();
			properties.load(ExportExcelUtil.class.getClassLoader()
					.getResourceAsStream("report.properties"));

			path = properties.getProperty("report_path");

			String filename = path + titleName + "(" + startValue + "~"
					+ endValue + ").xls";
			file = new File(
					new String((filename).getBytes("GBK"), "ISO-8859-1"));

			if (!file.getParentFile().exists()) {
				file.getParentFile().mkdirs();

			}

			if (!file.exists()) {
				file.createNewFile();
				logger.info("----------报表文件创建成功----------");
			}

			writableWorkbook = Workbook.createWorkbook(file);
			sheet = writableWorkbook.createSheet("Sheet0", 0);

			WritableCellFormat cellFormat = new WritableCellFormat();
			cellFormat.setAlignment(jxl.format.Alignment.CENTRE);
			cellFormat.setBackground(jxl.format.Colour.LIGHT_GREEN); // 设置单元格背景颜色为浅绿色

			// 设置excel列名
			for (int i = 0; i < columnNames.size(); i++) {
				sheet.addCell(new jxl.write.Label(i, 0, columnNames.get(i),
						cellFormat));
			}

			// jrh 客户不需要这个，有标题反而影响客户按列进行过滤
			// // 设置excel标题
			// sheet.mergeCells(0, 0, columnNames.size(), 0);
			// sheet.addCell(new jxl.write.Label(0, 0, titleName + "("+
			// startValue + "~" + endValue + ")", cellFormat));

			// 向excel中导入数据
			for (int i = 0; i < list.size(); i++) {
				Object[] objects = list.get(i);
				for (int j = 0; j < objects.length; j++) {
					sheet.addCell(new jxl.write.Label(j, i + 1,
							objects[j] == null ? null : objects[j].toString()));
				}
			}

			writableWorkbook.write();
			writableWorkbook.close();

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException(e.getMessage(), e);

		}

		return file;

	}
}
