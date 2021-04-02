package com.reign.common.concurrent;


import java.util.*;
import java.util.concurrent.TimeoutException;
/**
 * @ClassName: StripedReadWriteLock
 * @Description: 分离锁，提供读写分离锁
 *
 * https://blog.csdn.net/youngsend/article/details/47704175?utm_medium=distribute.pc_relevant.none-task-blog-2%7Edefault%7EOPENSEARCH%7Edefault-5.control&dist_request_id=1328767.10103.16173422706573301&depth_1-utm_source=distribute.pc_relevant.none-task-blog-2%7Edefault%7EOPENSEARCH%7Edefault-5.control
 * @Author: wuwx
 * @Date: 2021-04-02 11:42
 **/
public class StripedReadWriteLock {
    /**
     * 默认的分离锁大小-2048
     */
    public static final int DEFAULT_NUMBER_OF_MUTEXES = 2048;

    private final ReadWriteLockItem[] mutexs;

    private final int numOfStripes;

    public StripedReadWriteLock() {
        this(DEFAULT_NUMBER_OF_MUTEXES);
    }


    /**
     * 分离锁构造函数   需要在这里找到比提供参数的正好大的2的n次方
     *
     * @param numOfStripes 数量必须是2的倍数
     */
    public StripedReadWriteLock(int numOfStripes) {
        if (numOfStripes % 2 != 0) {
            throw new RuntimeException("Cannot create a CacheLockProvider with an odd number of stripes");
        }

        if (numOfStripes == 0) {
            throw new RuntimeException("A zero size CacheLockProvider does not have useful semantics");
        }

        this.numOfStripes = numOfStripes;
        mutexs = new ReadWriteLockItem[numOfStripes];
        for (int i = 0; i < numOfStripes; i++) {
            mutexs[i] = new ReadWriteLockItem();
        }
    }


    /**
     * 获取锁
     *
     * @param key 要锁定的key
     * @return
     */
    public ReadWriteLockItem getLockForKey(final Object key) {
        int lockNumber = ConcurrencyUtil.selectLock(key, numOfStripes);
        return mutexs[lockNumber];
    }


    /**
     * 批量锁定
     *
     * @param keys 要锁定的keys
     * @return
     */
    public ReadWriteLockItem[] getAndWriteLockAllForKeys(Object... keys) {
        SortedMap<Integer, ReadWriteLockItem> lockMap = getLockMap(keys);
        ReadWriteLockItem[] locks = new ReadWriteLockItem[lockMap.size()];
        int i = 0;
        for (Map.Entry<Integer, ReadWriteLockItem> entry : lockMap.entrySet()) {
            entry.getValue().lock(LockType.WRITE);
            locks[i++] = entry.getValue();
        }
        return locks;
    }

    /**
     * 批量锁定
     *
     * @param timeout 超时时间
     * @param keys    要锁定的key
     * @return
     * @throws TimeoutException
     */
    public ReadWriteLockItem[] getAndWriteLockAllForKeys(long timeout, Object... keys) throws TimeoutException {
        SortedMap<Integer, ReadWriteLockItem> locksMap = getLockMap(keys);
        boolean lockHeld;
        List<ReadWriteLockItem> heldLocks = new ArrayList<>();
        ReadWriteLockItem[] locks = new ReadWriteLockItem[locksMap.size()];
        int i = 0;
        for (Map.Entry<Integer, ReadWriteLockItem> entry : locksMap.entrySet()) {
            try {
                ReadWriteLockItem lock = entry.getValue();
                lockHeld = lock.tryLock(LockType.WRITE, timeout);
                if (lockHeld) {
                    heldLocks.add(lock);
                }
            } catch (InterruptedException e) {
                lockHeld = false;
            }

            if (!lockHeld) {
                for (int j = heldLocks.size() - 1; j >= 0; j--) {
                    ReadWriteLockItem lock = heldLocks.get(j);
                    lock.unlock(LockType.WRITE);
                }
                throw new TimeoutException("could not acquire all locks in " + timeout + " ms");
            }
            locks[i++] = entry.getValue();

        }
        return locks;
    }


    /**
     * 批量解除锁定
     *
     * @param keys 锁定的keys
     */
    public void unlockWriteLockForAllKeys(Object... keys) {
        SortedMap<Integer, ReadWriteLockItem> locks = getLockMap(keys);
        for (Map.Entry<Integer, ReadWriteLockItem> entry : locks.entrySet()) {
            entry.getValue().unlock(LockType.WRITE);
        }
    }


    /**
     * 批量获取锁，返回按照锁声明顺序排序的锁map
     *
     * @param keys 要锁定的key
     * @return
     */
    public SortedMap<Integer, ReadWriteLockItem> getLockMap(final Object... keys) {
        SortedMap<Integer, ReadWriteLockItem> locks = new TreeMap<>();
        for (Object key : keys) {
            int lockNumber = ConcurrencyUtil.selectLock(key, numOfStripes);
            ReadWriteLockItem lock = mutexs[lockNumber];
            locks.put(lockNumber, lock);
        }
        return locks;
    }
}
