package com.leif.util;

import org.joda.time.DateTime;

public class DateTimeUtil {

    public static String getNowString() {
        return DateTime.now().toString("yyyy-MM-dd HH:mm");
    }
}
