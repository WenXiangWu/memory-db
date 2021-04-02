package com.reign.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @ClassName: ResultSetHandler
 * @Description: 结果处理handler
 * @Author: wuwx
 * @Date: 2021-04-02 10:40
 **/
public interface ResultSetHandler<T> {

    T handler(ResultSet rs) throws SQLException;
}
