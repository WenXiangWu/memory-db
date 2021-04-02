package com.reign.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * @ClassName: RowProcessor
 * @Description: 行处理器
 * @Author: wuwx
 * @Date: 2021-04-02 10:41
 **/
public interface RowProcessor {

    /**
     * 返回一个数组
     * @param rs
     * @return
     * @throws SQLException
     */
    public Object[] toArray(ResultSet rs) throws SQLException;


    /**
     * 返回一个javaBean
     * @param rs  结果集
     * @param type bean类型
     * @param <T> 指定类型
     * @return
     * @throws SQLException
     */
    public <T> T toBean(ResultSet rs,Class<T> type) throws SQLException;


    /**
     * 返回一个BeanList
     * @param rs 结果集
     * @param type bean类型
     * @param <T> 指定类型
     * @return
     * @throws SQLException
     */
    public <T> List<T> toBeanList(ResultSet rs, Class<T> type) throws SQLException;

    /**
     * 返回一个map
     * @param rs 结果集
     * @return
     * @throws SQLException
     */
    public Map<String,Object> toMap(ResultSet rs) throws SQLException;
}
