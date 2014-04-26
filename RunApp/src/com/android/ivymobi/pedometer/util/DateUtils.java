/**
 * Project Name:HaiYou File Name:DateUtils.java Package
 * Name:com.hiker.onebyone.utils Date:2013-3-20上午10:41:49 Copyright (c) 2013
 * Company:苏州海客科技有限公司
 * 
 */

package com.android.ivymobi.pedometer.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * ClassName:DateUtils <br/>
 * 
 * @Description: 日期时间处理 Date: 2013-3-20 上午10:41:49 <br/>
 * @author maple
 * @version
 * @since JDK 1.6
 * @see
 */
public class DateUtils {

	public static final long MILLIS_FOR_ONE_DAY = 86400000;
	public static final String FORMAT_DATE_HHMM = "HH:mm";
	public static final String FORMAT_DATE_HHMMSS = "HH:mm:ss";
	public static final String FORMAT_DATE_YMMDD = "yy-MM-dd";
	public static final String FORMAT_DATE_YYMMDD = "yyyyMMdd";
	public static final String FORMAT_DATE_YMMDD_HHMM = "yy-MM-dd HH:mm";
	public static final String FORMAT_DATE_YYMMDD_HHMM = "yyyy-MM-dd HH:mm";
	public static final String FORMAT_DATE_YYMMDD_HHMMSS = "yyyy-MM-dd HH:mm:ss";

	/**
	 * 
	 * getDateTimes:获取指定时间字符串对应的毫秒. <br/>
	 * 
	 * @author maple
	 * @param date
	 * @param template
	 * @return
	 * @since JDK 1.6
	 */
	public static long getDateTimes(String date, String template) {
		if (date == null || ("").equals(date) || template == null || ("").equals(template)) {
			return -1;
		}
		SimpleDateFormat sdf = new SimpleDateFormat(template, Locale.CHINA);
		try {
			return sdf.parse(date).getTime();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return -1;
	}

	/**
	 * 
	 * getIncrementMillis:获取指定天数的毫秒增量. <br/>
	 * 
	 * @author maple
	 * @param days
	 * @return
	 * @since JDK 1.6
	 */
	public static long getIncrementMillis(int days) {
		return MILLIS_FOR_ONE_DAY * days;
	}

	/**
	 * 
	 * long2String:把指定的时间转换为对应的格式化字符串. <br/>
	 * 
	 * @author maple
	 * @param times
	 * @param template
	 * @return
	 * @since JDK 1.6
	 */
	public static String long2String(long times, String template) {
		if (times < 1 || template == null || ("").equals(template)) {
			return null;
		}
		Date date = new Date(times);
		SimpleDateFormat sdf = new SimpleDateFormat(template, Locale.CHINA);
		return sdf.format(date);
	}

	/**
	 * 把指定的日期字符串转换为对应的date对象
	 * 
	 * @param date
	 *            日期字符串
	 * @param template
	 *            日期格式
	 * @return
	 */
	public static Date StringToDate(String date, String template) {
		SimpleDateFormat sdf = new SimpleDateFormat(template, Locale.CHINA);
		try {
			return sdf.parse(date);
		} catch (ParseException e) {
			return null;
		}
	}

	/**
	 * 按格式生成日期
	 * 
	 * @param year
	 * @param month
	 * @param day
	 * @param format
	 * @return
	 */
	public static String getDateWithFormat(int year, int month, int day, String format) {
		Calendar c = Calendar.getInstance();
		c.set(Calendar.YEAR, year);
		c.set(Calendar.MONTH, month);
		c.set(Calendar.DATE, day);
		return new SimpleDateFormat(format, Locale.CHINA).format(c.getTime());
	}

	/***
	 * 
	 * getTime:把时间字符串转换为毫秒时间. <br/>
	 * 
	 * @author maple
	 * @param dateString
	 * @param pattern
	 * @return
	 * @since JDK 1.6
	 */
	public static long getTime(String dateString, String pattern) {
		if (dateString == null || ("").equals(dateString) || pattern == null || ("").equals(pattern)) {
			return 0;
		}
		SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.CHINA);
		Date date = null;
		try {
			date = sdf.parse(dateString);
		} catch (ParseException e) {
			e.printStackTrace();
			return 0;
		}
		return date.getTime();
	}

	/***
	 * 
	 * compareDate:比较两个时间的先后. <br/>
	 * 
	 * @author maple
	 * @param date
	 *            指定时间
	 * @param targetDate
	 *            比较目标时间
	 * @param style
	 *            时间指定格式
	 * @return 1 指定时间大于目标时间,0相等,-1指定时间小于目标时间
	 * @since JDK 1.6
	 */
	public static int compareDate(String date, String targetDate, String style) {
		long time = DateUtils.getTime((String) date, style);
		long targetTime = DateUtils.getTime((String) targetDate, style);
		if (time > targetTime) {
			return 1;
		}
		if (time == targetTime) {
			return 0;
		}
		return -1;
	}

	/**
	 * 获取当前时间
	 * 
	 * @param pattern
	 *            指定的格式
	 * @return
	 */
	public static String getCurrentDate(String pattern) {
		SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.CHINA);
		return sdf.format(new Date());
	}

	/**
	 * 
	 * @Title: timeConvert
	 * @Description: 将秒格式的时间进行转换
	 * @param value
	 * @return String "0:0:0"
	 * @author Abel
	 * @date 2013-9-13 下午4:22:51
	 */
	public static String timeConvert(int value) {
		int _h, _m, _s;
		_h = value / 3600;
		_m = (value % 3600) / 60;
		_s = value - 3600 * _h - 60 * _m;
		SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
		Calendar calendar = Calendar.getInstance();
		calendar.set(0, 0, 0, _h, _m, _s);

		return formatter.format(new Date(calendar.getTimeInMillis()));
	}

	

}
