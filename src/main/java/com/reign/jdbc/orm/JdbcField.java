package com.reign.jdbc.orm;

import com.reign.common.Lang;
import com.reign.jdbc.NameStrategy;
import com.reign.jdbc.Type;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @ClassName: JdbcField
 * @Description: jdbc 属性
 * @Author: wuwx
 * @Date: 2021-04-02 14:50
 **/
public class JdbcField {

    public Field field;

    public String fieldName;

    public String propertyName;

    public String columnName;

    public Lang.ClassType type;

    public Method getter;

    public Method writter;

    public boolean isPrimary;

    //插入时是否忽略
    public boolean insertIgnore;

    //忽略的字段
    public boolean ignore;

    //数据库类型
    public Type jdbcType;

    public JdbcField(Lang.MyField field, NameStrategy nameStrategy){
        this.field = field.field;
        this.fieldName = Lang.capitalize(field.fieldName);
        this.propertyName = field.fieldName;
        this.columnName = nameStrategy.propertyNameToColumnName(fieldName);
        this.type = field.type;
        this.getter = field.getter;
        this.writter = field.writter;

    }
}
