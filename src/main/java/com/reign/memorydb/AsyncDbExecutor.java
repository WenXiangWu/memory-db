package com.reign.memorydb;

import com.reign.memorydb.standardthread.StandardThread;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName: AsyncDbExecutor
 * @Description: DB异步执行管理器
 * @Author: wuwx
 * @Date: 2021-04-01 16:00
 **/
public final class AsyncDbExecutor {


    static final Logger log = InternalLoggerFactory.getLogger("com.reign.async");

    private static final AsyncDbExecutor instance = new AsyncDbExecutor();

    //消息队列
    private List<SQLEntry>[] messageArray = new ArrayList[2];

    //幸存者
    private List<SQLEntry> survivorList = new ArrayList<>();

    //游标
    private volatile int cursor;

    //间隔
    static final int INTERVAL = 500;

    //锁
    private Object lock = new Object();

    //ds
    private DataSource ds;

    //初始化标识
    private boolean init = false;

    //db执行线程数量
    private int threadNum = 1;

    //执行者
    private StandardThread executor;

    //线程池
    private SQLExecutorThread[] threadPool;

    //一次批量执行SQL条数
    private int batchSize = 400;

    //连接检查周期,5分钟检查一次
    private long connCheckInterval = 5 * 60 * 1000;


    //sql工厂
    private SqlFactory sqlFactory;


    /**
     * SQL执行线程
     */
    private class SQLExecutorMainThread extends StandardThread{


        public SQLExecutorMainThread(String name, Runnable runnable) {
            super(name, runnable);
        }
    }


    /**
     * SQL执行线程
     */
    private class SQLExecutorThread extends Thread {


        //sqlExecutor
        private SqlExecutor executor;

        //添加SQL
        public void addSQLEntry(SQLEntry entry) {
            executor.addSQLEntry(entry);
        }

        public int getQueueSize() {
            return executor.getQueueSize();
        }

        public SQLExecutorThread(int num) {
            super("AsyncDBExecutor-thread-" + num);
            this.executor = new SqlExecutor(instance, batchSize, connCheckInterval);
        }


        @Override
        public void run() {
            while (true) {
                //flush sql
                executor.flush();
                //休息片刻
                try {
                    sleep(INTERVAL);
                } catch (InterruptedException e) {
                    //忽略
                }
            }
        }

        /**
         * 立即flush SQL
         */
        public void flushSql(){
            executor.flush();
        }

    }


}
