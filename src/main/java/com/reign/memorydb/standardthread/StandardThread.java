package com.reign.memorydb.standardthread;

/**
 * @ClassName: StandardThread
 * @Description: 标准线程模型
 * @Author: wuwx
 * @Date: 2021-04-01 17:14
 **/
public class StandardThread extends Thread implements Executeable {

    //标准间隔时间，1分钟
    public static final long DEFAULT_INTERVAL = 60 * 1000;

    //最小间隔时间，300ms
    public static final long MIN_INTERVAL = 300;

    //线程名字
    private String name;

    //需要运行的逻辑
    private Runnable runnable;

    //是否仅运行一次
    private boolean runOnce;

    //是否停止运行
    private volatile boolean stop;

    //运行间隔
    private volatile long interval;


    /**
     * 该构造函数构建一个标准后台线程，每分钟运行一次逻辑
     *
     * @param name
     * @param runnable
     */
    public StandardThread(String name, Runnable runnable) {
        super(name);
        this.name = name;
        this.runnable = runnable;
        this.runOnce = false;
        this.stop = false;
        this.interval = DEFAULT_INTERVAL;
    }


    /**
     * 是否仅仅运行一次
     *
     * @param name
     * @param runnable
     * @param runOnce
     */
    public StandardThread(String name, Runnable runnable, boolean runOnce) {
        super(name);
        this.name = name;
        this.runnable = runnable;
        this.runOnce = runOnce;
    }


    /**
     * 该构造函数构造一个标准后台线程，指定间隔运行一次逻辑
     *
     * @param name
     * @param runnable
     * @param interval
     */
    public StandardThread(String name, Runnable runnable, long interval) {
        super(name);
        this.name = name;
        this.runnable = runnable;
        this.runOnce = false;
        this.stop = false;
        this.interval = Math.max(MIN_INTERVAL, interval);
    }


    @Override
    public void startExecutor() {
        super.start();
    }

    @Override
    public void stopExecutor() {
        stopThread();
    }

    /**
     * 中断线程
     */
    private void stopThread() {

        try {
            this.interrupt();
        } catch (Exception e) {

        }
        this.stop = true;
    }

    public void changeInterval(long interval) {
        this.interval = Math.max(interval, MIN_INTERVAL);
    }

    public String getThreadName() {
        return this.name;
    }

    @Override
    public void run() {
        if (runOnce) {
            runOnce();
            return;
        }
        while (!stop) {
            //运行逻辑
            runOnce();
            try {
                sleep(interval);
            } catch (InterruptedException e) {

            }
        }

    }


    private void runOnce(){
        try {
            runnable.run();
        }catch (Throwable t){
            System.out.println("thread run error:"+name+"runOnce:"+runOnce);
        }

    }
}
