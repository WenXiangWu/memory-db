package com.reign.memorydb;

import com.reign.jdbc.Param;
import com.reign.jdbc.SqlFactory;
import com.reign.jdbc.async.SqlFormatter;
import com.reign.log.Logger;
import com.reign.memorydb.standardthread.StandardRunnable;
import com.reign.memorydb.standardthread.StandardThread;
import com.reign.jdbc.orm.JdbcEntity;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName: AsyncDBExecutor
 * @Description: DB异步执行管理器
 * @Author: wuwx
 * @Date: 2021-04-01 16:00
 **/
public final class AsyncDBExecutor {


    static final Logger log = InternalLoggerFactory.getLogger("com.reign.async");

    private static final AsyncDBExecutor instance = new AsyncDBExecutor();

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

    public static AsyncDBExecutor getInstance() {
        return instance;
    }

    public AsyncDBExecutor() {
    }


    /**
     * 初始化
     *
     * @param sqlFactory
     * @param ds
     */
    public synchronized void init(SqlFactory sqlFactory, DataSource ds) {
        if (init) return;

        this.messageArray[0] = new ArrayList<>();
        this.messageArray[1] = new ArrayList<>();
        this.cursor = 0;
        this.ds = ds;
        this.sqlFactory = sqlFactory;
        this.executor = new StandardThread("AsyncDBExecutor-thread", new SQLExecutorMainThread(), INTERVAL);
        init = true;
    }

    /**
     * 设置db执行线程数量，必须在startExecute之前调用，否则无效
     *
     * @param num
     */
    public synchronized void setDBExecutorThreadNum(int num) {
        this.threadNum = num;
    }

    /**
     * 设置batchSize大小，必须在startExecute之前调用，否则无效
     *
     * @param batchSize
     */
    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    /**
     * 设置connCheckInterval大小，必须在startExecute之前调用，否则无效
     *
     * @param connCheckInterval
     */
    public void setConnCheckInterval(long connCheckInterval) {
        this.connCheckInterval = connCheckInterval;
    }


    /**
     * 开始执行
     */
    public void startExecute() {
        this.threadNum = Math.max(1, this.threadNum);
        this.threadPool = new SQLExecutorThread[this.threadNum];
        for (int i = 0; i < this.threadNum; i++) {
            threadPool[i] = new SQLExecutorThread(i + 1);
            threadPool[i].start();
        }
        this.executor.startExecutor();

    }


    /**
     * 添加一条需要同步的SQL
     *
     * @param sql
     * @param params
     * @param entity
     * @param order
     * @param interval
     */
    public void addSQL(String sql, List<Param> params, JdbcEntity entity, int order, long interval) {
        SQLEntry entry = new SQLEntry();
        entry.id = order;
        entry.aliveTime = interval;
        entry.op = AsyncOp.UPDATE;
        entry.entity = entity;
        getSQL(sql, params, entry);

        synchronized (lock) {
            List<SQLEntry> messageList = messageArray[cursor];
            messageList.add(entry);
            doLog(log, entry, 1);
        }
    }


    /**
     * 添加一条需要同步的SQL
     *
     * @param op
     * @param entity
     * @param obj
     * @param order
     * @param interval
     */
    public void addSQL(AsyncOp op, JdbcEntity entity, Object obj, int order, long interval) {
        SQLEntry entry = new SQLEntry();
        entry.id = order;
        entry.aliveTime = interval;
        entry.op = AsyncOp.UPDATE;
        entry.entity = entity;
        getSQL(op, entity, entry, obj);

        synchronized (lock) {
            List<SQLEntry> messageList = messageArray[cursor];
            messageList.add(entry);
            doLog(log, entry, 1);
        }
    }

    /**
     * 添加一条需要更新的SQL
     *
     * @param entity
     * @param obj
     * @param old
     * @param order
     * @param interval
     */
    public void addUpdateSQL(JdbcEntity entity, Object obj, Object old, int order, long interval) {
        SQLEntry entry = new SQLEntry();
        entry.id = order;
        entry.aliveTime = interval;
        entry.op = AsyncOp.UPDATE;
        entry.entity = entity;
        getSQL(AsyncOp.UPDATE, entity, entry, obj, old);
        synchronized (lock) {
            List<SQLEntry> messageList = messageArray[cursor];
            messageList.add(entry);
            doLog(log, entry, 1);
        }
    }


    public void flushSQL() {
        synchronized (lock) {
            List<SQLEntry> oldList = messageArray[cursor];
            //合并
            synchronized (survivorList) {
                for (SQLEntry entry : survivorList) {
                    oldList.add(entry);
                }
                survivorList.clear();

                for (SQLEntry entry : oldList) {
                    int mod = Math.abs(entry.entity.getTableName().hashCode()) % threadNum;
                    //子线程执行
                    threadPool[mod].addSQLEntry(entry);
                }
                oldList.clear();
            }
            //停止子线程
            for (SQLExecutorThread thread : threadPool) {
                thread.flushSql();
            }
        }
    }


    private void doLog(Logger log, SQLEntry entry, int type) {
        if (entry.sql != null) {
            switch (type) {

                case 1:
                    System.out.println("插入SQL到队列" + entry.entity.getTableName() + entry.id + type + entry.sql);
                    break;
                default:
                    System.out.println("插入SQL到队列" + entry.entity.getTableName() + entry.id + type + entry.sql);
                    break;
            }
        }

    }

    /**
     * 获取异步执行的SQL
     *
     * @param sql
     * @param params
     * @param entry
     */
    private void getSQL(String sql, List<Param> params, SQLEntry entry) {
        //从SQLFactory中获取SQL
        sql = sqlFactory.get(sql);
        sql = sql.trim();
        //填充参数
        if (params.size() > 0) {
            sql = SqlFormatter.format(sql, params);
        }

        entry.sqlIdentify = sql;
        entry.sql = sql;

    }

    /**
     * 获取异步执行的SQL
     *
     * @param op
     * @param entity
     */
    private void getSQL(AsyncOp op, JdbcEntity entity, SQLEntry entry, Object... args) {
        String sql = null;
        String sqlIdentity = "";
        switch (op) {
            case INSERT:
                sql = entity.getInsertSQL();
                sql = sql.trim();
                sqlIdentity = sql;
                sql = SqlFormatter.format(sql, entity.builderInsertParams(args[0]));
                break;
            case UPDATE:
                //实体更新
                if (entity.isEnhance()) {
                    String[] sqls = getDynamicUpdateSQL(args[0], args[1], entity);
                    sqlIdentity = sqls[0];
                    sql = sqls[1];
                } else {
                    String[] sqls = getUpdateSQL(args[0], entity);
                    sqlIdentity = sqls[0];
                    sql = sqls[1];
                }
                break;
            case DELETE:
                //主键删除
                sql = entity.getDeleteSQL();
                sql = sql.trim();
                sqlIdentity = sql;
                sql = SqlFormatter.format(sql, args);
                break;
            default:
                break;
        }
        entry.sqlIdentify = sqlIdentity;
        entry.sql = sqlFactory.get(sql);

    }

    /**
     * 获取更新SQL
     *
     * @param arg
     * @param entity
     * @return
     */
    private String[] getUpdateSQL(Object arg, JdbcEntity entity) {

        return null;
    }

    /**
     * 获取动态更新SQL
     *
     * @param arg
     * @param arg1
     * @param entity
     * @return
     */
    private String[] getDynamicUpdateSQL(Object arg, Object arg1, JdbcEntity entity) {
        String sqlIdentity = entity.getUpdateSQL();
        sqlIdentity = sqlIdentity.trim();
        String sql = SqlFormatter.format(sqlIdentity, entity.buildUpdateParams(obj));
        return new String[]{sqlIdentity, sql};
    }


    /**
     * SQL执行线程
     */
    private class SQLExecutorMainThread extends StandardRunnable {

        public SQLExecutorMainThread() {
            super(null);
        }

        @Override
        public void execute() {
            int oldCursor = cursor;
            synchronized (lock) {
                //切换游标
                cursor = 1 - oldCursor;
            }
            //检查队列中SQL积攒数量
            int num = 0;
            for (int i = 0; i < threadPool.length; i++) {
                num += threadPool[i].getQueueSize();
            }
            MemoryDBMonitor.recordSQLQueueSize(num);

            List<SQLEntry> oldList = messageArray[oldCursor];
            if (oldList.isEmpty())
                return;

            //合并map
            synchronized (survivorList) {
                for (SQLEntry entry : survivorList) {
                    oldList.add(entry);
                }
                survivorList.clear();

                //遍历消息，执行SQL
                for (SQLEntry entry : oldList) {
                    entry.aliveTime = entry.aliveTime - INTERVAL;
                    if (entry.aliveTime < 0) {
                        int mod = Math.abs(entry.entity.getTableName().hashCode()) % threadNum;
                        //子线程执行
                        threadPool[mod].addSQLEntry(entry);
                    } else {
                        survivorList.add(entry);
                    }
                }
                oldList.clear();

            }
        }
    }

    /**
     * 获取数据库连接
     *
     * @return
     */
    final Connection getConnection() {
        try {
            return ds.getConnection();
        } catch (SQLException e) {
            System.out.println("sql error" + e);
            return null;
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
        public void flushSql() {
            executor.flush();
        }

    }


}
