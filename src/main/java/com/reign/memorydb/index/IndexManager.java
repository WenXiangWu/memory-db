package com.reign.memorydb.index;

import com.reign.jdbc.orm.JdbcModel;

import java.util.List;

/**
 * @ClassName: IndexManager
 * @Description: 索引管理规范
 * @Author: wuwx
 * @Date: 2021-04-02 17:17
 **/
public interface IndexManager<V extends JdbcModel> {

    /**
     * 获取索引名称
     *
     * @return
     */
    String name();


    /**
     * 索引中添加内容
     *
     * @param value
     */
    void insert(V value);

    /**
     * 索引中移除内容
     *
     * @param value
     */
    void remove(V value);


    /**
     * 索引中移除内容
     *
     * @param args
     */
    void remove(Object... args);

    /**
     * 更新对象
     *
     * @param oldValue
     * @param newValue
     */
    void update(V oldValue, V newValue);

    /**
     * 索引中查找对象
     *
     * @param value
     * @return
     */
    Object find(V value);

    /**
     * 查找对象
     *
     * @return
     */
    Object find(Object... args);


    /**
     * 条件查询
     *
     * @param start
     * @param end
     * @return
     */
    List<V> rangeFind(V start, V end);


    /**
     * 采用最左匹配查找对象
     *
     * @param args
     * @return
     */
    List<V> leftFind(Object... args);

    /**
     * 获取树高
     *
     * @return
     */
    int getHeight();


    /**
     * 清理
     */
    void clear();


}
