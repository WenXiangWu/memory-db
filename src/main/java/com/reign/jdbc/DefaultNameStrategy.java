package com.reign.jdbc;

/**
 * @ClassName: DefaultNameStrategy
 * @Description: 默认名称转换策略
 * @Author: wuwx
 * @Date: 2021-04-02 14:56
 **/
public class DefaultNameStrategy implements NameStrategy{
    @Override
    public String columnNameToPropertyName(String colomnName) {
        return null;
    }

    @Override
    public String propertyNameToColumnName(String propertyName) {
        return null;
    }
}
