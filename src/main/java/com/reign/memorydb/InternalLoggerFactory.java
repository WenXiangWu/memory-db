package com.reign.memorydb;

/**
 * @ClassName: InternalLoggerFactory
 * @Description: 日志工厂
 * @Author: wuwx
 * @Date: 2021-04-01 16:30
 **/
public class InternalLoggerFactory {

    //默认日志工厂
    private static volatile InternalLoggerFactory defaultFactory;

    //默认错误日志类
    static volatile Logger errorLog;

    //异常堆栈行数
    static volatile int LINES = 100;

    static {
        final String name = InternalLoggerFactory.class.getName();
        InternalLoggerFactory f;
//        try {
//            f = new Slf4JLoggerFactory();
//
//        }


    }

    //创建日志
    public static Logger getLogger(String s) {
        return null;
    }
}
