package com.reign.common;

import com.reign.jdbc.Type;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @ClassName: Lang
 * @Description: 工具类
 * @Author: wuwx
 * @Date: 2021-04-02 17:34
 **/
public class Lang {


    /**
     * 获取类上的指定注解
     * @param clazz
     * @param anClass
     * @param <T>
     * @return
     */
    public static <T extends Annotation> T getAnnotation(Class<?> clazz, Class<T> anClass) {
        Class<?> cc = clazz;
        T annotation = null;
        while (null != cc && cc != Object.class) {
            annotation = cc.getAnnotation(anClass);
            if (null != annotation) {
                return annotation;
            }
            cc = cc.getSuperclass();
        }
        return null;
    }

    /**
     * 获取jdbc的类型
     * @param type
     * @return
     */
    public static Type getJdbcType(String type) {
        type = type.toLowerCase();
        if (type.startsWith("int")||type.startsWith("tinyint")||type.startsWith("mediumint")||type.startsWith("bit")||type.startsWith("smallint")){
            return Type.Int;
        }else if (type.startsWith("bigint")){
            return Type.Long;
        }else if (type.startsWith("text")||type.startsWith("varchar")||type.startsWith("mediumtext")){
            return Type.String;
        }else if (type.startsWith("datetime")||type.startsWith("timestamp")||type.startsWith("date")){
            return Type.Date;
        }else if (type.startsWith("float")){
            return Type.Float;
        }else if (type.startsWith("double")){
            return Type.Double;
        }else if (type.startsWith("blob")){
            return Type.Bytes;
        }
        return null;
    }

    public enum ClassType{
        PRIMITIVE_TYPE,STATIC_TYPE,FINAL_TYPE,DATE_TYPE,MAP_TYPE,LIST_TYPE,ARRAY_TYPE
    }

    public static class  MyField{
        public Field field;
        public String fieldName;
        public ClassType type;
        public Method getter;
        public Method writter;

    }

    /**
     * 老版本apache common包中的
     * @param str
     * @return
     */
    public static String capitalize(String  str){
        int strLen;
        return str!=null&& (strLen = str.length())!=0?(new StringBuffer(strLen).append(Character.toTitleCase(str.charAt(0))).append(str.substring(1))).toString():str;

    }

}
