package com.reign.jdbc;

import java.util.ArrayList;

/**
 * @ClassName: Params
 * @Description: 参数集合
 * @Author: wuwx
 * @Date: 2021-04-02 10:40
 **/
public class Params extends ArrayList<Param> {

    //TODO 生成序列化编号
    private static final long serial = 1L;
    /**空参数*/
    public static final Params EMPTY = new Params();

    public Params() {
    }

    /**
     * 添加参数
     * @param param
     */
    public void addParam(Param param){
        super.add(param);
    }

    /**
     * 添加参数
     * @param obj
     */
    public void addParam(Object obj){
        super.add(new Param(obj));
    }

    /**
     * 添加参数
     * @param obj
     * @param type
     */
    public void addParam(Object obj,Type type){
        super.add(new Param(obj,type));
    }
}
