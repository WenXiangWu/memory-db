package com.reign.memorydb;

import javax.naming.CommunicationException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @ClassName: SqlExecutor
 * @Description: sql执行器
 * @Author: wuwx
 * @Date: 2021-04-01 16:08
 **/
public class SqlExecutor {


    //测试SQL
    private static final String TEST_SQL = "select 1";

    //batchSize
    private final int batchSize;

    private final AsyncDbExecutor executor;

    //SQL队列
    private final BlockingQueue<SQLEntry> sqlQueue;

    //重试列表
    private final List<SQLEntry> retryList;

    //连接器
    private Connection conn;

    //连接创建时间
    private long createTime;

    //连接检查时间
    private long connCheckInterval;

    //db崩溃标识
    private static AtomicBoolean dbCrashFlag = new AtomicBoolean();

    //flush标识
    private AtomicInteger flushFlag;

    private final Logger log;


    public SqlExecutor(AsyncDbExecutor executor, int batchSize, long connCheckInterval) {
        super();
        this.connCheckInterval = connCheckInterval;
        this.batchSize = batchSize;
        this.executor = executor;
        this.sqlQueue = new LinkedBlockingQueue<>();
        this.retryList = new ArrayList<>();
        this.flushFlag = new AtomicInteger(0);
        this.log = AsyncDbExecutor.log;
    }

    /**
     * 添加SQL
     *
     * @param sqlEntry
     */
    public void addSQLEntry(SQLEntry sqlEntry) {
        sqlQueue.add(sqlEntry);
    }

    /**
     * 获取队列大小
     *
     * @return
     */
    public int getQueueSize() {
        return sqlQueue.size();
    }

    /**
     * 刷新所有SQL
     */
    public void flush() {
        if (sqlQueue.size() <= 0 && retryList.size() <= 0)
            return;

        //防止并发
        while (true) {
            if (flushFlag.compareAndSet(0, 1)) {
                break;
            }
        }
        try {
            //有重试队列的，先执行重试队列的
            if (retryList.size() > 0) {
                //COW机制
                List<SQLEntry> sqlList = new ArrayList<>(retryList);
                retryList.clear();
                batchExecute(sqlList, batchSize);
                return;
            }

            //执行队列中的
            List<SQLEntry> sqlList = new ArrayList<>(sqlQueue.size());
            SQLEntry sqlEntry = null;
            while ((sqlEntry = sqlQueue.poll()) != null) {
                sqlList.add(sqlEntry);
            }
            batchExecute(sqlList, batchSize);

        } finally {
            flushFlag.set(0);
        }

    }

    /**
     * 批量执行SQL
     *
     * @param sqlList
     * @param batchSize
     */
    private void batchExecute(List<SQLEntry> sqlList, int batchSize) {
        //检查连接
        checkConn();
        if (null == conn) {
            retryBatch(sqlList);
            return;
        }
        Statement stmt = null;
        try {
            //创建statement
            try {
                stmt = conn.createStatement();
            } catch (Throwable e) {
                System.out.println("async db error" + e);
            }
            //无法执行，打印日志
            if (stmt == null) {
                retryBatch(sqlList);
                return;
            }

            //执行批量执行
            List<SQLEntry> subList = new ArrayList<>(batchSize + 2);
            //batch计数器
            int size = 0;
            for (SQLEntry entry : sqlList) {
                //添加父节点
                if (null != entry.parent && addBatch(stmt, entry.parent)) {
                    subList.add(entry.parent);
                    size++;
                }
                //添加自身
                if (addBatch(stmt, entry)) {
                    subList.add(entry);
                    size++;
                }
                if (size >= batchSize) {
                    doBatchExecute(stmt, subList, size);
                    size = 0;
                    subList.clear();
                }
            }

            if (size > 0) {
                doBatchExecute(stmt, sqlList, size);
            }

        } finally {
            if (null != stmt) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    //忽略
                }

            }
        }
    }

    /**
     * 添加SQL到batch里面
     *
     * @param stmt
     * @param entry
     * @return
     */
    private boolean addBatch(Statement stmt, SQLEntry entry) {

        try {
            stmt.addBatch(entry.sql);
            return true;
        } catch (SQLException e) {
            System.out.println("async db error,sql:" + entry.sql + "error:" + e);
            //顺序执行该条SQL
            doSerialExecute(stmt, entry);
        }
        return false;
    }

    /**
     * batch处理
     *
     * @param stmt
     * @param sqlList
     * @param batchSize
     */
    private void doBatchExecute(Statement stmt, List<SQLEntry> sqlList, int batchSize) {
        try {
            //设置非自动提交
            conn.setAutoCommit(false);
            int[] results = doBatchExecute(stmt, batchSize);
            for (int i = 0; i < results.length; i++) {
                SQLEntry value = sqlList.get(i);
                if (results[i] >= 0 || results[i] == -2) {
                    //executor.dolLog(log, value, 2);
                } else {
                    //executor.dolLog(log, value, 3);
                    System.out.println("async db error,sql:[{}] ,batchResult:[{}]" + value.sql + results[i]);
                }
            }
            //提交SQL
            conn.commit();
            //db标识恢复了
            batchRecover();


        } catch (Exception e) {

            //发生连接性异常，直接重试整个batch
            if (e instanceof CommunicationException) {
                retryBatch(sqlList);
                return;
            }

            //发生其他异常，回滚事务
            try {
                conn.rollback();
            } catch (Throwable t) {
                System.out.println("async db error" + t);
            } finally {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException el) {
                    //忽略
                }
                //回滚了，使用顺序执行的方式
                serialExecute(sqlList);
            }

        } finally {

            try {
                if (null != conn) {
                    conn.setAutoCommit(true);
                }
            } catch (SQLException e) {
                //直接忽略
            }
        }

    }

    /**
     * batch操作恢复了
     */
    private void batchRecover() {
        if (dbCrashFlag.compareAndSet(true, false)) {
            //db恢复了
            MemoryDBMonitor.setDbCrash(false);
        }

    }

    /**
     * 重试整个batch
     *
     * @param sqlList
     */
    private void retryBatch(List<SQLEntry> sqlList) {
        if (dbCrashFlag.compareAndSet(false, true)) {
            System.out.println(" db maybe crash,can not execute batch");

            //设置DB状态为Crash
            MemoryDBMonitor.setDbCrash(true);
        }

        //设置连接不可用
        if (null != conn) {
            try {
                conn.close();
            } catch (SQLException e) {
                //忽略
            }
            conn = null;
        }
        //加入重试队列
        this.retryList.addAll(sqlList);
    }


    /**
     * 执行batch操作
     *
     * @param stmt
     * @param batchSize
     * @return -2表示执行成功，但是影响行数未知
     * -3表示执行失败，但是驱动在执行错误之后继续执行了后续的batch命令
     * 0 or >0 返回影响的行数
     */
    private int[] doBatchExecute(Statement stmt, int batchSize) throws SQLException {
        int[] result = null;
        result = stmt.executeBatch();
        stmt.clearBatch();
        return result;
    }


    /**
     * 检测数据库是否能连接
     */
    private void checkConn() {
        if (null == conn) {
            conn = executor.getConnection();
            createTime = System.currentTimeMillis();
            return;
        }
        if (!testConnection()) {
            try {
                conn.close();
            } catch (SQLException e) {
                //忽略
            }
            //检查不通过
            conn = executor.getConnection();
            createTime = System.currentTimeMillis();

        }

    }

    /**
     * 测试db连接状态
     *
     * @return
     */
    private boolean testConnection() {
        if (System.currentTimeMillis() - createTime <= connCheckInterval) {
            //免检时间内
            return true;
        }
        //进行检查
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            stmt.execute(TEST_SQL);
            return true;
        } catch (SQLException e) {
            return false;
        } finally {
            if (null != stmt) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    return false;
                }
            }
        }

    }


    /**
     * 顺序执行SQL
     *
     * @param sqlList
     */
    private void serialExecute(List<SQLEntry> sqlList) {
        //检查连接
        checkConn();
        Statement stmt = null;

        try {
            try {
                stmt = conn.createStatement();
            } catch (Throwable t) {
                System.out.println("async db error" + t);
            }
            //无法执行，打印日志
            if (stmt == null) {
                retryBatch(sqlList);
                return;
            }
            //执行SQL
            for (SQLEntry value : sqlList) {
                doSerialExecute(stmt, value);
            }
        } finally {
            //关闭连接
            if (null != stmt) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    //忽略
                }
            }

        }

    }

    /**
     * 顺序执行SQL
     *
     * @param stmt
     * @param entry
     */
    private void doSerialExecute(Statement stmt, SQLEntry entry) {

        try {
            stmt.executeUpdate(entry.sql);
        } catch (Exception e) {
            //将异常打印到日志里面
            // executor.doLog();
            //System.out.println("async db error,sql:[{}]",log.getOriginThtowable(e),entry.sql);
        }

    }

}
