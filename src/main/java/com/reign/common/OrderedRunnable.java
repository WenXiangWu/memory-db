package com.reign.common;

/**
 * @ClassName: OrderedRunnable
 * @Description: 有序任务
 * @Author: wuwx
 * @Date: 2021-04-02 10:50
 **/
public interface OrderedRunnable extends Runnable{

    Object getOrder();
}
