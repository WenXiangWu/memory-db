package com.reign.memorydb.standardthread;

/**
 * @ClassName: ThreadInterceptor
 * @Description: 线程拦截器
 * @Author: wuwx
 * @Date: 2021-04-01 17:20
 **/
public interface ThreadInterceptor <E extends StandardRunnable>{

    /**
     * 任务执行之前
     * @param thread
     * @param command
     */
    void beforeExecute(Thread thread,E command);


    /**
     * 任务执行之后
     * @param thread
     * @param command
     */
    void afterExecute(Thread thread,E command);
}
