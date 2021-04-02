package com.reign.jdbc.orm.cache;

import com.reign.jdbc.orm.IdEntity;
import com.reign.jdbc.orm.JdbcEntity;
import com.reign.jdbc.orm.JdbcField;
import com.reign.jdbc.orm.annotation.AutoGenerator;

/**
 * @ClassName: SingleIdEntity
 * @Description: 单主键实体
 * @Author: wuwx
 * @Date: 2021-04-02 18:19
 **/
public class SingleIdEntity implements IdEntity {


    private JdbcField field;

    private boolean autoGenerator;

    private JdbcEntity entity;

    public SingleIdEntity(JdbcField field, JdbcEntity entity) {
        this.field = field;
        this.entity = entity;
        this.autoGenerator = field.field.getAnnotation(AutoGenerator.class) != null;
    }

    @Override
    public boolean isAutoGenerator() {
        return autoGenerator;
    }

    @Override
    public void setIdValue(Object obj, Object... args) {
        if (!autoGenerator) {
            return;
        }
        try {
            field.field.setAccessible(true);
            field.field.set(obj, args[0]);
        } catch (Throwable t) {
            throw new RuntimeException("set key error" + t);
        }

    }

    @Override
    public Object[] getIdValue(Object obj) {
        try {
            field.field.setAccessible(true);
            Object result = field.field.get(obj);
            return new Object[]{result};
        } catch (Throwable t) {
            throw new RuntimeException("get key error " + t);
        }
    }

    @Override
    public String getKeyValueByObject(Object obj) {

        try {
            field.field.setAccessible(true);
            Object result = field.field.get(obj);
            return String.valueOf(result);
        } catch (Throwable t) {
            throw new RuntimeException("get key error " + t);
        }
    }

    @Override
    public String getKeyValuesByParams(Object... args) {
        return String.valueOf(args[0]);
    }

    @Override
    public String[] getIdColumnName() {
        return new String[]{field.columnName};
    }
}
