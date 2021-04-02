package com.reign.memorydb.annotation;

import java.lang.annotation.*;

/**
 * @ClassName: BTreeIndex
 * @Description: B+ tree索引
 * @Author: wuwx
 * @Date: 2021-04-02 17:24
 **/
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface BTreeIndex {
    /**
     * 索引名称
     * @return
     */
    String name();

    /**
     * 索引列
     * @return
     */
    String[] value();

    /**
     * 是否为唯一索引
     * @return
     */
    boolean unique() default  true;
}
