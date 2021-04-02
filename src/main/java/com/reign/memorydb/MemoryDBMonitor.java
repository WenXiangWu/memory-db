package com.reign.memorydb;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @ClassName: MemoryDBMonitor
 * @Description: 数据库监控
 * @Author: wuwx
 * @Date: 2021-04-01 17:08
 **/
public class MemoryDBMonitor {


    //sql队列长度
    private static int sqlQueueSize;

    //sql队列长度阈值，红线阈值
    private static volatile int SQL_QUEUESIZE_HIGH_THRESHOLD = 200;

    //SQL队列长度阈值，绿线阈值
    private static volatile int SQL_QUEUESIZE_LOW_THRESHOLD = 50;

    //SQL队列一直处于阈值时间长度,单位秒
    private static volatile long SQL_QUEUESIZE_LAST_TIME = 2 * 60;

    //DB恢复观察时间,单位秒
    private static volatile long DB_CRASH_RECOVER_TIME = 2 * 60;

    //是否健康
    private static boolean isHealth = true;

    //第一次报告不健康的时间
    private static long firstWarnTime = 0;

    //上次db crash恢复时间
    private static volatile boolean dbCrash = false;

    //上次db crash恢复时间
    private static long lastDbCrashRecoverTime = 0;

    //存储到map中的对象
    private static final Object OBJECT = new Object();

    //manageTable，db不健康时被托管的内存表
    private static Map<MemoryTable, Object> managedTable = new ConcurrentHashMap<>();


    public static void recordSQLQueueSize(int sqlQueueSize) {
        //1.无变化
        if (MemoryDBMonitor.sqlQueueSize == sqlQueueSize && MemoryDBMonitor.sqlQueueSize <= SQL_QUEUESIZE_LOW_THRESHOLD && !dbCrash)
            return;

        MemoryDBMonitor.sqlQueueSize = sqlQueueSize;
        if (MemoryDBMonitor.sqlQueueSize >= SQL_QUEUESIZE_HIGH_THRESHOLD) {
            if (isHealth && firstWarnTime == 0) {
                //第一次报警
                firstWarnTime = System.currentTimeMillis();
            } else if (isHealth && (System.currentTimeMillis() - firstWarnTime) >= SQL_QUEUESIZE_LAST_TIME) {
                //持续2分钟了，表明数据库处理SQL已经不健康了
                isHealth = false;
            }

            //持续不健康，打印日志
            if (!isHealth) {
                System.out.println("memory db not health");
            }
            System.out.println("async db queue size is " + sqlQueueSize + "  is higher than threshold " + SQL_QUEUESIZE_HIGH_THRESHOLD);
            return;

        } else if (firstWarnTime > 0) {
            //低于报警值
            firstWarnTime = 0;
        }

        if (!isHealth && MemoryDBMonitor.sqlQueueSize <= SQL_QUEUESIZE_LOW_THRESHOLD) {
            isHealth = true;
            firstWarnTime = 0;
            //处理托管table

            doManageTable();
            return;
        }

        //判断DB是否恢复
        if (dbCrash && lastDbCrashRecoverTime > 0 && (System.currentTimeMillis() - lastDbCrashRecoverTime) >= DB_CRASH_RECOVER_TIME) {
            handleDbRecover();
            System.out.println(" db  recover");
        }

    }

    public static void setSqlQueuesizeHighThreshold(int highThreshold) {
        SQL_QUEUESIZE_HIGH_THRESHOLD = highThreshold;
    }

    public static void setSqlQueuesizeLowThreshold(int lowThreshold) {
        SQL_QUEUESIZE_LOW_THRESHOLD = lowThreshold;
    }

    public static void setSqlQueuesizeLastTime(long lastTime) {
        SQL_QUEUESIZE_LAST_TIME = lastTime;
    }

    public static void setDbCrashRecoverTime(long recoverTime) {
        DB_CRASH_RECOVER_TIME = recoverTime;
    }


    public static void setDbCrash(boolean isCrash) {
        if (isCrash) {
            dbCrash = true;
            lastDbCrashRecoverTime = 0L;
        } else {
            lastDbCrashRecoverTime = System.currentTimeMillis();
        }

    }

    /**
     * 处理DB恢复了
     */
    private static void handleDbRecover() {
        dbCrash = false;
        lastDbCrashRecoverTime = 0L;
        doManageTable();
    }

    /**
     * 托管自己
     */
    private static void doManageTable(MemoryTable table) {
        managedTable.put(table, OBJECT);
        if (isHealth()) {
            doManageTable();
        }
    }

    /**
     * 内存库是否健康
     *
     * @return
     */
    private static boolean isHealth() {
        return isHealth && !dbCrash;
    }

    /**
     * 处理托管table
     */
    public static void doManageTable() {
        if (isHealth() && managedTable.size() > 0) {
            Set<MemoryTable> keySet = new HashSet<>(managedTable.keySet());
            for (MemoryTable table : keySet) {
                //flush清理操作
                table.flushDeleteOp();
                //移除自身
                managedTable.remove(table);
            }

        }

    }


}
