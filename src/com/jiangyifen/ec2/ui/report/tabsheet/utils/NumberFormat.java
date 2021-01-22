package com.jiangyifen.ec2.ui.report.tabsheet.utils;

public class NumberFormat {

	public static String numberFormat(Integer integer) {

		String str = integer.toString().trim();

		StringBuffer buffer = new StringBuffer();

		if (str.length() > 6) {
			throw new RuntimeException("数字过大，最大只能是六位数");
		}

		int count = 6 - str.length();

		for (int i = 0; i < count; i++) {

			buffer.append("0");
		}

		buffer.append(str);

		return buffer.toString();
	}

}
