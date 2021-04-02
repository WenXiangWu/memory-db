package com.reign.jdbc.handlers;

import com.reign.jdbc.ResultSetHandler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName: AbstractListHandler
 * @Description: 将resultSet处理为list结果集
 * @Author: wuwx
 * @Date: 2021-04-02 16:59
 **/
public abstract class AbstractListHandler<T> implements ResultSetHandler<List<T>> {

    /**
     * 处理结果集
     * @param rs
     * @return
     * @throws SQLException
     */
    public List<T> handler(ResultSet rs) throws SQLException {
        List<T> rows = new ArrayList<T>();
        while (rs.next()){
            rows.add(this.handlerRow(rs));
        }
        return rows;
    }

    protected abstract T handlerRow(ResultSet rs)  throws SQLException;


}
