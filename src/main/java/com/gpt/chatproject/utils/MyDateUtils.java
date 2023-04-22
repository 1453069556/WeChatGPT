package com.gpt.chatproject.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class MyDateUtils {
    private static final String DEFAULT_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * 获取中国北京时间字符串
     *
     * @param format 日期格式字符串
     * @return 中国北京时间字符串
     */
    public static String getBeijingTime(String format) {
        TimeZone timeZone = TimeZone.getTimeZone("Asia/Shanghai");
        DateFormat dateFormat = new SimpleDateFormat(format);
        dateFormat.setTimeZone(timeZone);
        Date now = new Date();
        return dateFormat.format(now);
    }
    /**
     * 获取中国北京时间字符串
     *
     * @return 中国北京时间字符串
     */
    public static Date getBeijingDate() {
        TimeZone timeZone = TimeZone.getTimeZone("Asia/Shanghai");
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setTimeZone(timeZone);
        return new Date();
    }
    /**
     * 获取中国北京时间字符串（默认格式）
     *
     * @return 中国北京时间字符串
     */
    public static String getBeijingTime() {
        return getBeijingTime(DEFAULT_FORMAT);
    }

    /**
     * 对指定日期进行加减运算
     *
     * @param date   要进行加减运算的日期
     * @param field  加减的时间单位，如Calendar.DATE表示天数
     * @param amount 加减的数量，可以为负数
     * @return 运算后的日期
     */
    public static Date add(Date date, int field, int amount) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(field, amount);
        return calendar.getTime();
    }

    /**
     * 对当前日期进行加减运算
     *
     * @param field  加减的时间单位，如Calendar.DATE表示天数
     * @param amount 加减的数量，可以为负数
     * @return 运算后的日期
     */
    public static Date add(int field, int amount) {
        TimeZone timeZone = TimeZone.getTimeZone("Asia/Shanghai");
        Calendar calendar = Calendar.getInstance(timeZone);
        Date date = calendar.getTime();
        return add(date, field, amount);
    }


    /**
     * 对当前日期进行format
     *
     * @param date   要进行加减运算的日期
     * @param format format字符串
     * @return 运算后的日期
     */
    public static String dateFormat(Date date,String format) {
        TimeZone timeZone = TimeZone.getTimeZone("Asia/Shanghai");
        DateFormat dateFormat = new SimpleDateFormat(format);
        dateFormat.setTimeZone(timeZone);
        return dateFormat.format(date);
    }

    /**
     * 将字符串转换成DATE
     *
     * @param dateString   要进行转换的日期字符串
     * @param format format字符串
     * @return 运算后的日期
     */
    public static Date formatDate(String dateString,String format) {
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        Date date = null;
        try {
            date = formatter.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }
}
