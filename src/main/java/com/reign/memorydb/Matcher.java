package com.reign.memorydb;

/**
 * @ClassName: Matcher
 * @Description: 自定义匹配器
 * @Author: wuwx
 * @Date: 2020-10-14 17:10
 **/
public interface Matcher<T> {
    boolean match(T value);
}
