package com.reign.memorydb.standardthread;

/**
 * @ClassName: Executeable
 * @Description: 能否调用开始的接口协议
 * @Author: wuwx
 * @Date: 2021-04-01 17:27
 **/
public interface Executeable {

    /**
     * 启动执行器
     */
    void startExecutor();

    /**
     * 结束执行器
     */
    void stopExecutor();

}
