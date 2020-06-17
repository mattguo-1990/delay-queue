package com.fenqile.delayqueue.util;

import java.util.Calendar;
import java.util.Date;

/**
 * @author mattguo
 * @version 2018年07月23日 20:17
 */
public class DateUtil {
    public static final String ENG_DATE_FROMAT = "EEE, d MMM yyyy HH:mm:ss z";
    public static final String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";
    public static final String YYYY_MM_DD_HH_MM = "yyyy-MM-dd HH:mm";
    public static final String YYYY_MM_DD = "yyyy-MM-dd";
    public static final String YYYY = "yyyy";
    public static final String MM = "MM";
    public static final String DD = "dd";

    public static Long addMillSeconds(Date date, Integer millseconds) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MILLISECOND, millseconds);
        return calendar.getTime().getTime();
    }

    public static Long addSeconds(Date date, Integer seconds) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.SECOND, seconds);
        return calendar.getTime().getTime();
    }

    public static Long addSeconds(Long time, Integer seconds) {
        Date date = new Date(time);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.SECOND, seconds);
        return calendar.getTime().getTime();
    }

    public static Long now() {
        return new Date().getTime();
    }
}
