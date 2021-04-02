package com.reign.log;

/**
 * @ClassName: Logger
 * @Description: 框架日志
 * @Author: wuwx
 * @Date: 2021-04-01 16:24
 **/
public interface Logger {

    //获取日志的名称
    String name();

    //是否可以输出Trace级别的日志
    boolean isTraceEnabled();

    //输出trace级别的日志
    void trace(String msg);

    //多参数trace日志
    void trace(String format,Object... arg);

    //输出trace级别日志
    void trace(String msg,Throwable t);

    //是否可以输出Debug级别的日志
    boolean isDebugEnabled();


    //TODO 输出各种级别的日志，包含trace，debug，fatal，error，warn,info

    //获取错误的原始原因
    Throwable getOriginThtowable(Throwable t);

    void debug(String toString);
}
