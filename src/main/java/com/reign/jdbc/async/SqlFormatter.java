package com.reign.jdbc.async;

import com.reign.jdbc.Param;
import com.reign.jdbc.util.DateUtil;

import java.text.Format;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @ClassName: SqlFormatter
 * @Description: sql格式化器
 * @Author: wuwx
 * @Date: 2021-04-02 16:09
 **/
public class SqlFormatter {

    private List<TextPattern> patternList = new ArrayList<>(10);

    private String pattern;

    private List<Integer> offsetList = new ArrayList<>(10);

    private static Map<String, SqlFormatter> cacheMap = new ConcurrentHashMap<>();

    private static Object lock = new Object();


    public SqlFormatter(String pattern) {
        this.pattern = pattern;
    }

    public SqlFormatter() {
    }

    /**
     * 格式化字符串
     *
     * @param pattern
     * @param params
     * @return
     */
    public static String format(String pattern, Object... params) {
        SqlFormatter formatter = cacheMap.get(pattern);
        if (null == formatter) {
            synchronized (lock) {
                formatter = cacheMap.get(pattern);
                if (null == formatter) {
                    formatter = new SqlFormatter(pattern);
                    cacheMap.put(pattern, formatter);
                }
            }
        }
        return formatter.format(params);

    }

    /**
     * 格式化字符串
     *
     * @param pattern
     * @param paramList
     * @return
     */
    public static String format(String pattern, List<Param> paramList) {
        SqlFormatter formatter = cacheMap.get(pattern);
        if (null == formatter) {
            synchronized (lock) {
                formatter = cacheMap.get(pattern);
                if (null == formatter) {
                    formatter = new SqlFormatter(pattern);
                    cacheMap.put(pattern, formatter);
                }
            }
        }
        return formatter.format(paramList);

    }

    private String format(List<Param> paramList) {
        StringBuilder builder = new StringBuilder(this.pattern.length());
        int lastOffset = 0;
        int i = 0;
        for (Integer offset : offsetList) {
            builder.append(this.pattern.substring(lastOffset, offset));
            int index = this.patternList.get(i).index;
            if (index < paramList.size()) {
                builder.append(getValue(paramList.get(index)));
            } else {
                builder.append("?");
            }
            lastOffset = offset;
            i++;
        }
        //append到最后
        builder.append(this.pattern.substring(lastOffset, this.pattern.length()));
        return builder.toString();

    }


    private String format(Object... params) {
        StringBuilder builder = new StringBuilder(this.pattern.length());
        int lastOffset = 0;
        int i = 0;
        for (Integer offset : offsetList) {
            builder.append(this.pattern.substring(lastOffset, offset));
            int index = this.patternList.get(i).index;
            if (index < params.length) {
                builder.append(getValue(params[index]));
            } else {
                builder.append("?");
            }
            lastOffset = offset;
            i++;
        }
        //append到最后
        builder.append(this.pattern.substring(lastOffset, this.pattern.length()));
        return builder.toString();

    }

    private String getValue(Param obj) {
        if (null == obj || obj.obj == null) {
            return null;
        }
        switch (obj.type) {
            case String:
                return getValue((String) obj.obj);
            case Date:
                return "'" + DateUtil.formatDate((Date) obj.obj, DateUtil.DATETIME_FULLHYPHEN);
            default:
                break;
        }
        Object value = obj.obj;
        return getValue(value);

    }

    private String getValue(Object obj){
        if (null == obj) return null;
        Class<?> clazz = obj.getClass();
        if (clazz==String.class ||CharSequence.class.isAssignableFrom(clazz)){
            return getValue(String.valueOf(obj));
        }else if (clazz.isAssignableFrom(Date.class)||Date.class.isAssignableFrom(clazz)){
            return "'"+DateUtil.formatDate((Date)obj,DateUtil.DATETIME_FULLHYPHEN)+"'";
        }
        return String.valueOf(obj);

    }

    public String getValue(String value){
        if (null == value) return null;
        value = value.replace("'","''");
        return "'"+value+"'";
    }

    private class TextPattern {
        //序号
        public int index;
        //格式化器
        public Format format;

        public TextPattern(int index, Format format) {
            this.index = index;
            this.format = format;
        }
    }
}
