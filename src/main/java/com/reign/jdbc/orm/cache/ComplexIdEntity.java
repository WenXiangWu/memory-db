package com.reign.jdbc.orm.cache;

import com.reign.jdbc.orm.IdEntity;

/**
 * @ClassName: ComplexIdEntity
 * @Description: TODO
 * @Author: wuwx
 * @Date: 2021-04-02 18:35
 **/
public class ComplexIdEntity implements IdEntity {
    @Override
    public boolean isAutoGenerator() {
        return false;
    }

    @Override
    public void setIdValue(Object obj, Object... args) {

    }

    @Override
    public Object[] getIdValue(Object obj) {
        return new Object[0];
    }

    @Override
    public String getKeyValueByObject(Object obj) {
        return null;
    }

    @Override
    public String getKeyValuesByParams(Object... args) {
        return null;
    }

    @Override
    public String[] getIdColumnName() {
        return new String[0];
    }
}
