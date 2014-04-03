package com.android.ivymobi.pedometer.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtil {
    public static final String YYYYMMDD = "yyyy.MM.dd";
    public static final String HHmmss = "HH:mm:ss";
    /**
     * 
     * @Title: getCurrentDate 
     * @Description: 获取当天的年月日 格式：2014.03.10
     * @return String     获取当天的日期
     * @author msx
     * @date 2014-4-3 下午1:48:13
     */
    public static final String getCurrentDate() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat(YYYYMMDD);
        return dateFormat.format(calendar.getTime());
    }
    public static final String convertTime(long value){
        value=value/1000;
        long _h, _m, _s;
        _h = value / 3600;
        _m = (value % 3600) / 60;
        _s = value - 3600 * _h - 60 * _m;
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        Calendar calendar = Calendar.getInstance();
        calendar.set(0, 0, 0, (int)_h,(int) _m,(int) _s);

        return formatter.format(new Date(calendar.getTimeInMillis()));
       
    }
    /**
     * 
     * @Title: getDate 
     * @Description: 获取当天的年月日 格式：2014.03.10
     * @return String     获取当天的日期
     * @author msx
     * @date 2014-4-3 下午1:48:13
     */
    public static final String getDate(long time){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        SimpleDateFormat dateFormat = new SimpleDateFormat(YYYYMMDD);
        return dateFormat.format(calendar.getTime());
    }
    /**
     * 
     * @Title: getCurrentDate 
     * @Description: 获取当前时间的HH:mm 格式：13:00
     * @return String     获取当前时间
     * @author msx
     * @date 2014-4-3 下午1:48:13
     */
    public static final String getTime(long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
        return dateFormat.format(calendar.getTime());
    }
    /**
     * 
     * @Title: getCurrentDate 
     * @Description: 获取当前时间的HH:mm 格式：13:00
     * @return String     获取当前时间
     * @author msx
     * @date 2014-4-3 下午1:48:13
     */
    public static final String getCurrentTime() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
        return dateFormat.format(calendar.getTime());
    }
    /**
     * 
     * @Title: getCurrentDate 
     * @Description: 获取当天的年月日 格式：2014.03.10
     * @return String     获取当天的日期
     * @author msx
     * @date 2014-4-3 下午1:48:13
     */
    public static final String getTime() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat(YYYYMMDD);
        return dateFormat.format(calendar.getTime());
    }
}
