package com.reign.memorydb.annotation;

import java.lang.annotation.*;

/**
 * @ClassName: SyncDBConfig
 * @Description: 与DB的同步配置
 * @Author: wuwx
 * @Date: 2021-04-02 17:27
 **/
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface SyncDBConfig {

    /**
     * 同步周期，单位ms；默认300ms
     * @return
     */
    long interval() default 300L;
}
