package com.jiangyifen.ec2.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 日期转换工具类
 *
 * @author jinht
 *
 * @date 2015-4-7 下午12:32:22 
 * @version v1.0.0
 */
public class DateTransformFactory {

	/**
	 * jinht
	 * 根据传入的时间获取前一天的日期
	 * @param date 当前日期
	 * @return date 当前日期的前一天
	 */
	public Date getSpecifiedDayBefore(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		int day = calendar.get(Calendar.DATE);
		calendar.set(Calendar.DATE, day - 1);
		return calendar.getTime();
	}
	
	/**
	 * jinht
	 * 根据传入的时间获取前一天的日期
	 * @param date 当前日期
	 * @param dateFormat 日期格式
	 * @return string 当前日期的前一天
	 */
	public String getSpecifiedDayBefore(String date, String dateFormat) {
		SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
		Calendar calendar = Calendar.getInstance();
		try {
			calendar.setTime(sdf.parse(date));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		int day = calendar.get(Calendar.DATE);
		calendar.set(Calendar.DATE, day - 1);
		return sdf.format(calendar.getTime());
	}
	
	/**
	 * jinht
	 * 根据传入的时间获取前一天的日期
	 * @param date 当前日期
	 * @param dateFormat 日期格式
	 * @return string 当前日期的前一天
	 */
	public String getSpecifiedDayBefore(Date date, String dateFormat) {
		SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		int day = calendar.get(Calendar.DATE);
		calendar.set(Calendar.DATE, day - 1);
		return sdf.format(calendar.getTime());
	}
	
	/**
	 * jinht
	 * 根据传入的时间获取后一天的日期
	 * @param date 当前日期
	 * @return date 当前日期的后一天
	 */
	public Date getSpecifiedDayAfter(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		int day = calendar.get(Calendar.DATE);
		calendar.set(Calendar.DATE, day + 1);
		return calendar.getTime();
	}
	
	/**
	 * jinht
	 * 根据传入的时间获取后一天的日期
	 * @param date 当前日期
	 * @param dateFormat 日期格式
	 * @return string 当前日期的后一天
	 */
	public String getSpecifiedDayAfter(String date, String dateFormat) {
		SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
		Calendar calendar = Calendar.getInstance();
		try {
			calendar.setTime(sdf.parse(date));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		int day = calendar.get(Calendar.DATE);
		calendar.set(Calendar.DATE, day + 1);
		return sdf.format(calendar.getTime());
	}
	
	/**
	 * jinht
	 * 根据传入的时间获取后一天的日期
	 * @param date 当前日期
	 * @param dateFormat 日期格式
	 * @return string 当前日期的后一天
	 */
	public String getSpecifiedDayAfter(Date date, String dateFormat) {
		SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		int day = calendar.get(Calendar.DATE);
		calendar.set(Calendar.DATE, day + 1);
		return sdf.format(calendar.getTime());
	}
	
	/**
	 * jinht
	 * 根据传入的时间获取后一天的日期
	 * @param date 当前日期
	 * @param dateFormat 日期格式
	 * @return string 当前日期的后一天
	 */
	public String getSpecifiedDayHourAfter(String date, String dateFormat) {
		SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
		Calendar calendar = Calendar.getInstance();
		try {
			calendar.setTime(sdf.parse(date));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		int hour = calendar.get(Calendar.HOUR);
		calendar.set(Calendar.HOUR, hour + 1);
		return sdf.format(calendar.getTime());
	}
	
	/**
	 * jinht
	 * 根据传入的时间获取下一个小时的日期
	 * @param date 当前日期
	 * @param dateFormat 日期格式
	 * @return string 当前日期的下一个小时
	 */
	public String getSpecifiedDayHourAfter(Date date, String dateFormat) {
		SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		int hour = calendar.get(Calendar.HOUR);
		calendar.set(Calendar.HOUR, hour + 1);
		return sdf.format(calendar.getTime());
	}
	
	
	
	
	
	public static void main(String[] args) {
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:00");
		Date date = new Date();
		
		System.out.println(sdf.format(date));
		DateTransformFactory dtf = new DateTransformFactory();
		System.out.println(dtf.getSpecifiedDayHourAfter(date, "yyyy-MM-dd HH:00"));
		System.out.println(dtf.getSpecifiedDayHourAfter("2014-12-20 23:00", "yyyy-MM-dd HH:00"));
		
	}
	
}
