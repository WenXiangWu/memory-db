package com.reign.common.concurrent;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @ClassName: ReadWriteLockItem
 * @Description: 读写分离锁
 * @Author: wuwx
 * @Date: 2021-04-02 11:34
 **/
public class ReadWriteLockItem {

    /**
     * 读写锁
     */
    private final ReadWriteLock readWriteLock;
    /**
     * 读锁
     */
    private final Lock readLock;
    /**
     * 写锁
     */
    private final Lock writeLock;

    /**
     * 构造函数
     */
    public ReadWriteLockItem(){
        readWriteLock = new ReentrantReadWriteLock();
        readLock = readWriteLock.readLock();
        writeLock = readWriteLock.writeLock();
    }


    /**
     * 锁定
     *
     * @param lockType 锁类型
     */
    public void lock(final LockType lockType) {
        getLock(lockType).lock();
    }

    /**
     * 解除锁定
     *
     * @param lockType
     */
    public void unlock(final LockType lockType) {
        getLock(lockType).unlock();
    }


    /**
     * 尝试锁定
     *
     * @param lockType 锁类型
     * @param msec     等待毫秒数
     * @return
     * @throws InterruptedException
     */
    public boolean tryLock(final LockType lockType, final long msec) throws InterruptedException {
        return getLock(lockType).tryLock(msec, TimeUnit.MILLISECONDS);
    }

    /**
     * 尝试锁定
     *
     * @param lockType
     * @return
     */
    public boolean tryLock(final LockType lockType) {
        return getLock(lockType).tryLock();
    }

    private Lock getLock(final LockType lockType) {
        switch (lockType) {
            case READ:
                return readLock;
            case WRITE:
                return writeLock;
            default:
                return null;
        }
    }


}
