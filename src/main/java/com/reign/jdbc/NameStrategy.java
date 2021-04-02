package com.reign.jdbc;

/**
 * @ClassName: NameStrategy
 * @Description: 名称转换策略
 * @Author: wuwx
 * @Date: 2021-04-02 14:54
 **/
public interface NameStrategy {
    /**
     * 将列名转换为属性名称
     * @param colomnName
     * @return
     */
    String columnNameToPropertyName(String colomnName);


    /**
     * 将属性名称转换为列名
     * @param propertyName
     * @return
     */
    String propertyNameToColumnName(String propertyName);

}
