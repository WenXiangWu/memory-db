package com.reign.jdbc.orm;


/**
 * @ClassName: IdEntity
 * @Description: 主键实体
 * @Author: wuwx
 * @Date: 2021-04-02 14:50
 **/
public interface IdEntity {

    boolean isAutoGenerator();

    /**
     * 赋值
     * @param obj
     * @param args
     */
    void setIdValue(Object obj,Object... args);

    /**
     * 获取key值
     * @param obj
     * @return
     */
    Object[] getIdValue(Object obj);

    /**
     * 用于缓存
     * @param obj
     * @return
     */
    String getKeyValueByObject(Object obj);


    /**
     * 用于缓存
     * @param args
     * @return
     */
    String getKeyValuesByParams(Object... args);

    /**
     * 获取主键的列名
     * @return
     */
    String[] getIdColumnName();
}
