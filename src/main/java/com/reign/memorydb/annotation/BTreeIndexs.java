package com.reign.memorydb.annotation;

import java.lang.annotation.*;

/**
 * @ClassName: BTreeIndexs
 * @Description: 多重索引
 * @Author: wuwx
 * @Date: 2021-04-02 17:27
 **/
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface BTreeIndexs {

    /**
     * 多重索引
     * @return
     */
    BTreeIndex[] value();
}
