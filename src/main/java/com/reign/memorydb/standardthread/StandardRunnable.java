package com.reign.memorydb.standardthread;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName: StandardRunnable
 * @Description: 标准Runnable接口
 * @Author: wuwx
 * @Date: 2021-04-01 17:18
 **/
public abstract class StandardRunnable implements Runnable {

    //拦截器
    protected List<ThreadInterceptor> interceptors;

    //是否执行拦截
    private boolean interceptor;

    //开始执行时间
    private long startTime;

    //是否发生了异常
    public boolean error;


    public StandardRunnable(List<ThreadInterceptor> interceptors) {
        this.interceptors = interceptors;
        this.interceptor = (null == interceptors || interceptors.size() <= 0) ? false : true;
    }

    /**
     * 添加拦截器
     *
     * @param interceptor
     */
    public void addInterceptor(ThreadInterceptor<?> interceptor) {
        if (null == this.interceptors) {
            this.interceptors = new ArrayList<>(2);
        }
        this.interceptors.add(interceptor);
        this.interceptor = true;
    }

    /**
     * 任务执行逻辑
     */
    @Override
    public void run() {

        if (interceptor) {
            beforeExecute();
            try {
                execute();
            } catch (RuntimeException e) {
                error = true;
                throw e;
            } finally {
                afterExecute();
            }
        } else {
            execute();
        }

    }

    /**
     * 任务执行之前拦截
     */
    private void beforeExecute() {
        try {
            for (ThreadInterceptor interceptor : interceptors) {
                interceptor.beforeExecute(Thread.currentThread(), this);
            }
        } catch (Throwable t) {
            System.out.println("thread beforeExecute error" + t);
        }

    }

    /**
     * 任务执行之后拦截
     */
    private void afterExecute() {
        try {
            for (ThreadInterceptor interceptor : interceptors) {
                interceptor.afterExecute(Thread.currentThread(), this);
            }
        } catch (Throwable t) {
            System.out.println("thread afterExecute error" + t);
        }

    }

    /**
     * 真正要执行的逻辑
     */
    public abstract void execute();
}
