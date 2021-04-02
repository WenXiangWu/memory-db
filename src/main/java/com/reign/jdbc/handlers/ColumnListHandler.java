package com.reign.jdbc.handlers;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @ClassName: ColumnListHandler
 * @Description: 列列表处理器
 * @Author: wuwx
 * @Date: 2021-04-02 16:59
 **/
public class ColumnListHandler extends AbstractListHandler<Object>{

    //列id
    private final int columnIndex;

    //列名称
    private final String columnName;

    /**
     * 处理单列
     * @param rs
     * @return
     * @throws SQLException
     */
    protected Object handlerRow(ResultSet rs) throws SQLException {
        if (this.columnName == null){
            return rs.getObject(this.columnIndex);
        }else {
            return rs.getObject(this.columnName);
        }
    }



    public ColumnListHandler(int columnIndex, String columnName) {
        super();
        this.columnIndex = columnIndex;
        this.columnName = columnName;
    }

    public ColumnListHandler(int columnIndex) {
        this(columnIndex,null);
    }

    public ColumnListHandler() {
        this(1,null);
    }

    public ColumnListHandler(String columnName) {
        this(1,columnName);
    }
}
