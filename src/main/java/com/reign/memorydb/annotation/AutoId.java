package com.reign.memorydb.annotation;

import java.lang.annotation.*;

/**
 * @ClassName: AutoId
 * @Description: 自增键
 * @Author: wuwx
 * @Date: 2021-04-02 17:26
 **/
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface AutoId {
}
