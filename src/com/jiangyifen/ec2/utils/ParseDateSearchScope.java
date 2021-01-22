package com.jiangyifen.ec2.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 解析日期收索范围，根据用户的选择得到一个时间段
 * @author jrh
 */
public class ParseDateSearchScope {

	/**
	 * 解析字符串到日期
	 * @param timeScopeValue
	 * @return
	 */
	// 将用户选择的组合框中的值解析为日期字符串
	// TODO 改成英文判断，不要含有中文
	public static Date[] parseToDate(String timeScopeValue) {
		Date[] dates = new Date[2]; // 存放开始于结束时间
		Calendar cal = Calendar.getInstance(); // 取得当前时间
		cal.setTime(new Date());

		if ("今天".equals(timeScopeValue)) {
			dates[0] = cal.getTime();

			cal.add(Calendar.DAY_OF_WEEK, +1);
			dates[1] = cal.getTime();
		} else if ("昨天".equals(timeScopeValue)) {
			cal.add(Calendar.DAY_OF_WEEK, -1);
			dates[0] = cal.getTime();

			cal.add(Calendar.DAY_OF_WEEK, +1);
			dates[1] = cal.getTime();
		} else if ("本周".equals(timeScopeValue)) {
			int day = cal.get(Calendar.DAY_OF_WEEK);
			if(day == 1) {
				cal.add(Calendar.DAY_OF_YEAR, -1);
			}
			cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
			dates[0] = cal.getTime(); // 本周的第一天

			cal.set(Calendar.DAY_OF_WEEK, cal.getActualMaximum(Calendar.DAY_OF_WEEK));
			cal.add(Calendar.DAY_OF_WEEK, +2);
			dates[1] = cal.getTime(); // 本周的最后一天
		} else if ("本月".equals(timeScopeValue)) {
			cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.DAY_OF_MONTH));
			dates[0] = cal.getTime(); // 本月第一天

			cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
			cal.add(Calendar.DAY_OF_YEAR, +1);
			dates[1] = cal.getTime(); // 本月最后一天
		} else if ("上周".equals(timeScopeValue)) {
			cal.add(Calendar.WEEK_OF_MONTH, -1);
			cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
			dates[0] = cal.getTime(); // 上周的第一天

			cal.set(Calendar.DAY_OF_WEEK,
					cal.getActualMaximum(Calendar.DAY_OF_WEEK));
			cal.add(Calendar.DAY_OF_WEEK, +2);
			dates[1] = cal.getTime(); // 上周的最后一天
		} else if ("上月".equals(timeScopeValue)) {
			cal.add(Calendar.MONTH, -1);
			cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.DAY_OF_MONTH));
			dates[0] = cal.getTime(); // 上月月第一天

			cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
			cal.add(Calendar.DAY_OF_YEAR, +1);
			dates[1] = cal.getTime(); // 上月最后一天
		}
		
		//目的是将日期格式的时分秒设为
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd 00:00:00");
		try {
			dates[0]=sdf.parse(sdf.format(dates[0]));
			dates[1]=sdf.parse(sdf.format(dates[1]));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return dates;
	}
	
	// TODO 可能会有时间精准度的问题	
	// 将时间范围timeScopeValue值 解析为日期字符串，并存入数据中返回
	public static String[] parseDateSearchScope(String timeScopeValue) {
		String[] dates = new String[2];	// 存放开始于结束时间
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd 00:00:00");
		Calendar cal = Calendar.getInstance();		// 取得当前时间
		cal.setTime(new Date());
		
		if ("今天".equals(timeScopeValue)) {
			dates[0] = df.format(cal.getTime());
			
			cal.add(Calendar.DAY_OF_WEEK, +1);
			dates[1] = df.format(cal.getTime());
		}  else if ("昨天".equals(timeScopeValue)) {
			cal.add(Calendar.DAY_OF_WEEK, -1);
			dates[0] = df.format(cal.getTime());
			
			cal.add(Calendar.DAY_OF_WEEK, +1);
			dates[1] = df.format(cal.getTime());
		} else if ("本周".equals(timeScopeValue)) {
			int day = cal.get(Calendar.DAY_OF_WEEK);
			if(day == 1) {
				cal.add(Calendar.DAY_OF_YEAR, -1);
			}
			cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
			dates[0] = df.format(cal.getTime());	// 本周的第一天
			
			cal.set(Calendar.DAY_OF_WEEK, cal.getActualMaximum(Calendar.DAY_OF_WEEK));
			cal.add(Calendar.DAY_OF_WEEK, +2);
			dates[1] = df.format(cal.getTime());	// 本周的最后一天
		} else if ("本月".equals(timeScopeValue)) {
			cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.DAY_OF_MONTH));
			dates[0] = df.format(cal.getTime());	// 本月第一天
			
			cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
			cal.add(Calendar.DAY_OF_YEAR, +1);
			dates[1] =  df.format(cal.getTime());	// 本月最后一天
		} else if ("上周".equals(timeScopeValue)) {
			cal.add(Calendar.WEEK_OF_MONTH, -1);
			cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
			dates[0] = df.format(cal.getTime());	// 上周的第一天
			
			cal.set(Calendar.DAY_OF_WEEK, cal.getActualMaximum(Calendar.DAY_OF_WEEK));
			cal.add(Calendar.DAY_OF_WEEK, +2);
			dates[1] = df.format(cal.getTime());	// 上周的最后一天
		} else if ("上月".equals(timeScopeValue)) {
			cal.add(Calendar.MONTH, -1);
			cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.DAY_OF_MONTH));
			dates[0] = df.format(cal.getTime());	// 上月月第一天
			
			cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
			cal.add(Calendar.DAY_OF_YEAR, +1);
			dates[1] =  df.format(cal.getTime());	// 上月最后一天
		}
		return dates;
	}
	
	/**
	 * 取得距当前日期相差 dayAmount 天的日期
	 * @param dayAmount 相差的天数
	 * @return
	 */
	public static String getSpecifyDateStr(int dayAmount) {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Calendar cal = Calendar.getInstance();		// 取得当前时间
		cal.setTime(new Date());
		cal.add(Calendar.DAY_OF_YEAR, dayAmount);
		return df.format(cal.getTime());
	}
	
	/**
	 * 取得距当前日期相差 dayAmount 天的日期
	 * @param dayAmount 相差的天数
	 * @return
	 */
	public static Date getSpecifyDate(int dayAmount) {
		Calendar cal = Calendar.getInstance();		// 取得当前时间
		cal.setTime(new Date());
		cal.add(Calendar.DAY_OF_YEAR, dayAmount);
		return cal.getTime();
	}
	
}
