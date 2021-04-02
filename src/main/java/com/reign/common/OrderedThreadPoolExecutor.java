package com.reign.common;

import java.util.LinkedList;
import java.util.concurrent.*;

/**
 * @ClassName: OrderedThreadPoolExecutor
 * @Description: 按照任务唯一标识，Hash相同标识的任务到同一个线程中执行；防止一系列相同的任务锁死线程池
 * @Author: wuwx
 * @Date: 2021-04-02 10:47
 **/
public class OrderedThreadPoolExecutor extends ThreadPoolExecutor {

    /**
     * childExecutors
     */
    private final ConcurrentHashMap<Object, Executor> childExecutors = new ConcurrentHashMap<>();

    public OrderedThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    }

    public OrderedThreadPoolExecutor(int threadNum) {
        super(threadNum, threadNum, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
    }

    @Override
    public void execute(Runnable command) {
        if (command instanceof OrderedRunnable) {
            OrderedRunnable orderedRunnable = (OrderedRunnable) command;
            doOrderedExecute(orderedRunnable);
        } else {
            doUnOrderedExecute(command);
        }
    }


    /**
     * 有序执行任务
     *
     * @param orderedRunnable
     */
    private void doOrderedExecute(OrderedRunnable orderedRunnable) {
        Executor executor = childExecutors.get(orderedRunnable.getOrder());
        if (null == executor) {
            executor = new ChildExecutor();
            Executor oldExecutor = childExecutors.putIfAbsent(orderedRunnable.getOrder(), executor);
            if (oldExecutor != null) {
                executor = oldExecutor;
            }
        }
        executor.execute(orderedRunnable);
    }

    /**
     * 无序执行命令
     *
     * @param command
     */
    private void doUnOrderedExecute(Runnable command) {
        super.execute(command);
    }


    /**
     * childExecutor子执行器，用来保证需要按照顺序执行的任务，只会占用一个线程
     */
    private final class ChildExecutor implements Executor, Runnable {

        private final LinkedList<OrderedRunnable> tasks = new LinkedList<>();

        ChildExecutor() {
            super();
        }

        @Override
        public void run() {
            Thread thread = Thread.currentThread();
            for (; ; ) {
                final OrderedRunnable task;
                synchronized (tasks) {
                    task = tasks.getFirst();
                }

                boolean ran = false;
                beforeExecute(thread, task);
                try {
                    task.run();
                    ran = true;
                    afterExecute(task, null);
                } catch (RuntimeException e) {
                    if (!ran) {
                        afterExecute(task, e);
                    }
                    throw e;
                } finally {
                    synchronized (tasks) {
                        tasks.removeFirst();
                        if (tasks.isEmpty()) {
                            childExecutors.remove(task.getOrder());
                            break;
                        }
                    }

                }

            }

        }

        @Override
        public void execute(Runnable command) {
            boolean needExecution;
            synchronized (tasks) {
                needExecution = tasks.isEmpty();
                tasks.add((OrderedRunnable) command);
            }
            if (needExecution) {
                doUnOrderedExecute(this);
            }
        }
    }

    /**
     * 测试类
     *
     * @param args
     */
    public static void main(String[] args) {
        OrderedThreadPoolExecutor executor = new OrderedThreadPoolExecutor(5);
        for (int i = 0; i < 10; i++) {
            String str = String.valueOf(1 / 2);
            OrderTask task = new OrderTask(str);
            executor.execute(task);
        }
    }

    public static class OrderTask implements OrderedRunnable {
        private String order;


        public OrderTask(String order) {
            this.order = order;
        }

        @Override
        public Object getOrder() {
            return order;
        }

        @Override
        public void run() {
            System.out.println(Thread.currentThread().getId() + "  execute me,my order id " + order);
            try {
                Thread.currentThread().sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
