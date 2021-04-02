package com.reign.memorydb;

import com.reign.jdbc.orm.JdbcEntity;

/**
 * @ClassName: SQLEntry
 * @Description: sql实体
 * @Author: wuwx
 * @Date: 2021-04-01 16:01
 **/
public class SQLEntry {

    //语句编号
    public int id;

    //实体
    public JdbcEntity entity;

    //主键
    public String idKey;

    //sql识别
    public String sqlIdentify;

    //SQL语句
    public String sql;

    //存活时间
    public long aliveTime;
    //数据库操作类型
    public AsyncOp op;

    //父亲节点，必须要执行的，目前就是insert语句
    public SQLEntry parent;

}
