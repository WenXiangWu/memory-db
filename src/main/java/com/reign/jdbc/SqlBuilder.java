package com.reign.jdbc;

import com.reign.log.Logger;
import com.reign.memorydb.InternalLoggerFactory;

import java.math.BigDecimal;
import java.sql.*;
import java.util.List;

/**
 * @ClassName: SqlBuilder
 * @Description: SQL builder
 * @Author: wuwx
 * @Date: 2021-04-02 10:40
 **/
public class SqlBuilder {

    private static final Logger log = InternalLoggerFactory.getLogger("com.reign.framework.jdbc");

    /**
     * 设置SQL参数
     *
     * @param pstmt
     * @param params
     * @throws SQLException
     */
    private void buildParameters(PreparedStatement pstmt, List<Param> params) throws SQLException {
        int index = 1;
        StringBuilder builder = null;
        if (log.isDebugEnabled()) {
            builder = new StringBuilder(params.size() * 10);

        }
        for (Param param : params) {
            if (null == param) {
                pstmt.setObject(index, null);
            } else {
                fillParameter(index, pstmt, param);
            }
            if (log.isDebugEnabled()) {
                if (index != 1) {
                    builder.append(",").append(index).append(":").append(param == null ? null : param.obj);
                } else {
                    builder.append("Parameters:[").append(index).append(":").append(param == null ? null : param.obj);
                }
            }
            index++;
        }
        if (log.isDebugEnabled() && builder.length() > 0) {
            builder.append("]");
            log.debug(builder.toString());
        }
    }

    /**
     * 填充参数
     *
     * @param index
     * @param pstmt
     * @param param
     */
    private void fillParameter(int index, PreparedStatement pstmt, Param param) throws SQLException {
        if (param.obj == null) {
            pstmt.setObject(index, param.obj);
            return;
        }

        switch (param.type) {
            case Object:
                pstmt.setObject(index, param.obj);
                break;
            case Like:
                pstmt.setString(index, param.obj + "%");
                break;
            case BigDecimal:
                pstmt.setBigDecimal(index, (BigDecimal) param.obj);
                break;
            case Blob:
                pstmt.setBlob(index, (Blob) param.obj);
                break;
            case Byte:
                pstmt.setByte(index, (Byte) param.obj);
                break;
            case Bytes:
                pstmt.setBytes(index, (byte[]) param.obj);
                break;
            case Clob:
                pstmt.setClob(index, (Clob) param.obj);
                break;
            case Date:
                pstmt.setTimestamp(index, new Timestamp(((Date) param.obj).getTime() / 1000 * 1000));
                break;
            case SqlDate:
                pstmt.setDate(index, (java.sql.Date) param.obj);
                break;
            case Time:
                pstmt.setTime(index, (Time) param.obj);
                break;
            case Timestamp:
                pstmt.setTimestamp(index, (Timestamp) param.obj);
                break;
            case Double:
                pstmt.setDouble(index, (Double) param.obj);
                break;
            case Float:
                pstmt.setFloat(index, (Float) param.obj);
                break;
            case Int:
                pstmt.setInt(index, (Integer) param.obj);
                break;
            case Long:
                pstmt.setLong(index, (Long) param.obj);
                break;
            case NClob:
                pstmt.setNClob(index, (NClob) param.obj);
                break;
            case String:
                pstmt.setString(index, (String) param.obj);

                break;
            case Bool:
                pstmt.setBoolean(index, (Boolean) param.obj);
                break;
            case Out:
                break;
            default:
                throw new RuntimeException("unknown type:[type:" + param.type + "]");


        }


    }

}
