package com.jiangyifen.ec2.ui.report.tabsheet.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtil {

	// 把毫秒数转为xxx时xx分xx秒
	public static String getTime(long l) {
		if(l <= 0) {
			return "00:00:00";
		}
		
		long sec = 0;
		long min = 0;
		long hours = 0;
		long days = 0;

		String stSec = null;
		String stMin = null;
		String stHours = null;

		long c = l;
		if (c < 60) {
			sec = c;
		} else {
			sec = c % 60;
		}

		c = l / 60;
		if (c < 60) {
			min = c;
		} else {
			min = c % 60;
		}

		c = l / 60 / 60;

		if(c < 24) {
			hours = c;
		} else {
			hours = c % 24;
		}

		days = c / 24;
		
		if (sec < 10) {
			stSec = "0" + sec;
		} else {
			stSec = String.valueOf(sec);
		}

		if (min < 10) {
			stMin = "0" + min;
		} else {
			stMin = String.valueOf(min);
		}
		
		if (hours < 10) {
			stHours = "0" + hours;
		} else {
			stHours = String.valueOf(hours);
		}
		
		if(days > 0) {
			return days + "天 " + stHours + ":" + stMin + ":" + stSec + "";
		} else {
			return stHours + ":" + stMin + ":" + stSec + "";
		}
		
	}

	// 测试main方法
	// public static void main(String[] args) {
	//
	// System.out.println(getTime(10000000));
	// }

	// 当前日期所在周的第一天的日期(周日为一周的第一天)
	public static String getFirstDayWeek() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Calendar calendar = Calendar.getInstance();

		calendar.set(Calendar.DATE, calendar.get(Calendar.DAY_OF_MONTH)
				- (calendar.get(Calendar.DAY_OF_WEEK) - 1));

		return dateFormat.format(calendar.getTime());
	}

	// 当前日期所在周的最后一天的日期(周六为一周的最后一天)
	public static String getLastDayWeek() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.DATE, calendar.get(Calendar.DAY_OF_MONTH)
				+ (7 - calendar.get(Calendar.DAY_OF_WEEK)));

		return dateFormat.format(calendar.getTime());
	}

	// 当前日期所在月份的第一天的日期
	public static String getFirstDayMonth() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.DATE,
				calendar.getActualMinimum(Calendar.DAY_OF_MONTH));

		return dateFormat.format(calendar.getTime());
	}

	// 当前日期所在月份的最后一天的日期
	public static String getLastDayMonth() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.DATE,
				calendar.getActualMaximum(Calendar.DAY_OF_MONTH));

		return dateFormat.format(calendar.getTime());
	}

	// 当前日期所在年的第一天的日期
	public static String getFirstDayYear() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Calendar calendar = Calendar.getInstance();
		calendar.set(calendar.get(Calendar.YEAR),
				calendar.getActualMinimum(Calendar.MONTH),
				calendar.getActualMinimum(Calendar.DAY_OF_MONTH));

		return dateFormat.format(calendar.getTime());

	}

	// 获得指定日期的前一天
	public static String getBeforeDay(Date date) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Calendar calendar = dateFormat.getCalendar();
		calendar.setTime(date);
		calendar.add(Calendar.DAY_OF_MONTH, -1);

		return dateFormat.format(calendar.getTime());
	}

	// 获得批定日期的下一天
	public static String getNextDay(Date date) {

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Calendar calendar = dateFormat.getCalendar();
		calendar.setTime(date);
		calendar.add(Calendar.DAY_OF_MONTH, +1);

		return dateFormat.format(calendar.getTime());
	}

	// 获得指定日期的上一小时
	public static String getBeforeHour(Date date) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:00");
		Calendar calendar = format.getCalendar();
		calendar.setTime(date);
		calendar.add(Calendar.HOUR_OF_DAY, -1);

		return format.format(calendar.getTime());
	}

	// 获得指定日期的下一小时
	public static String getNextHour(Date date) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:00");
		Calendar calendar = format.getCalendar();
		calendar.setTime(date);
		calendar.add(Calendar.HOUR_OF_DAY, 1);

		return format.format(calendar.getTime());
	}

	public static void main(String[] args) throws ParseException {
		System.out.println(getNextHour(new Date()));
	}

}
