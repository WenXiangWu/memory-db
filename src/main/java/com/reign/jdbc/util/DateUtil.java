package com.reign.jdbc.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @ClassName: DateUtil
 * @Description: TODO
 * @Author: wuwx
 * @Date: 2021-04-02 16:19
 **/
public class DateUtil {


    public static final String DATETIME_FULLHYPHEN = "yyyy-MM-dd HH:mm:ss";


    public static String formatDate(Date date, String pattern) {
        if (date == null) {
            return null;
        } else if (pattern == null || pattern.isEmpty()) {
            return null;
        } else {
            SimpleDateFormat sm = new SimpleDateFormat(pattern);
            String covStr = sm.format(date);
            return covStr;
        }
    }
}
