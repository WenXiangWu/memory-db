package com.reign.jdbc;

/**
 * @ClassName: Param
 * @Description: JDBC执行参数
 * @Author: wuwx
 * @Date: 2021-04-02 10:39
 **/
public class Param {

    /**参数值*/
    public Object obj;

    /**类别*/
    public Type type;


    public Param(Object obj) {
        this.obj = obj;
        this.type = Type.Object;
    }

    public Param(Object obj, Type type) {
        this.obj = obj;
        this.type = type;
    }
}
