package com.leif.util;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class DateTimeUtil {

    /**
     * 获取当前时间
     * @return
     */
    public static String getNowString() {
        return DateTime.now().toString("yyyy-MM-dd HH:mm");
    }

    /**
     * 计算日期间隔
     * @param createTime
     * @return
     */
    public static Integer daysBetweenNow(String createTime) {
        //当前日期
        DateTime now = DateTime.now();
        //日期格式
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm");
        //转换传入String日期为DateTime 且格式相同
        DateTime time = DateTime.parse(createTime, dateTimeFormatter);

        return Days.daysBetween(time, now).getDays() + 1;
    }
}
