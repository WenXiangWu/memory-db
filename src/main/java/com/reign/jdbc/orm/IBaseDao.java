package com.reign.jdbc.orm;

import com.reign.jdbc.Param;
import com.reign.jdbc.Params;
import com.reign.jdbc.ResultSetHandler;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @ClassName: IBaseDao
 * @Description: 基础SQL接口
 * @Author: wuwx
 * @Date: 2021-04-02 14:50
 **/
public interface IBaseDao<T extends JdbcModel,PK extends Serializable> {

    /**
     * 创建新的实例，该方法会清除所有查询缓存
     * @param newInstance
     */
    void  create(T newInstance);

    /**
     * 创建新实例，会清除所有查询缓存
     * @param newInstance
     * @param canDelay
     */
    void create(T newInstance,boolean canDelay);

    /**
     * 创建新实例
     * @param newInstance
     * @param canDelay
     * @param keys  传递null或不传递时表示不清楚任何查询缓存，传递字符串 ALL_QUERY_CACHE表示清除所有缓存
     */
    void create(T newInstance,boolean canDelay,String... keys);


    /**
     * 通过主键查询
     * @param id
     * @return
     */
    T read(PK id);

    /**
     * 通过索引读取数据
     * @param keys
     * @return
     */
    T readByIndex(Object[] keys);

    /**
     * 锁定指定对象用于更新
     * @param id
     * @return
     */
    T readForUpdate(PK id);

    /**
     * 更新指定对象，指定对象必须是持久化对象
     * 此方法会导致该实体的二级缓存失效，不会导致任何查询缓存失效，如果需要查询缓存失效（您更新该实体的某些内容会引起查询结构的变化），请调用其他方法
     * @param transientObject
     */
    void update(T transientObject);


    /**
     * 更新指定对象，指定对象必须是持久化对象
     * 此方法会导致该实体的二级缓存失效，不会导致任何查询缓存失效，如果需要查询缓存失效（您更新该实体的某些内容会引起查询结构的变化），请调用其他方法
     * @param transientObject
     * @param keys 传递null或不传递时表示不清楚任何查询缓存，传递字符串 ALL_QUERY_CACHE表示清除所有缓存
     */
    void update(T transientObject,String... keys);

    /**
     * 删除指定对象，根据主键删除
     * 此方法会清除该实体的二级缓存，不会清除任何查询缓存
     * @param id
     */
    void delete(PK id);


    /**
     * 查询所有
     * @return
     */
    List<T> getModels();


    /**
     * 获取表大小
     * @return
     */
    int getModelSize();

    /**
     * 根据SQL查出结果集，返回第一条结果
     * @param sql
     * @return
     */
    T getFirstResultByHQLAndParam(String sql);

    T getFirstResultByHQLAndParam(String sql, Params params);

    /**
     * 根据SQL查出结果集
     * @param sql
     * @return
     */
    List<T> getResultByHQLAndParam(String sql);

    List<T> getResultByHQLAndParam(String sql,Params params);

    /**
     * 更新操作，此方法默认可以被延迟执行
     * 注意此方法会清除所有二级缓存和查询缓存；
     * @param sql
     * @param params
     */
    void update(String sql,Params params);

    /**
     * 更新操作，此方法默认可以被延迟执行
     * 注意此方法会清除所有二级缓存和查询缓存；
     * @param sql
     * @param params
     * @param canDelay 是否需要延迟执行
     */
    void update(String sql,Params params,boolean canDelay);


    /**
     *
     * @param sql
     * @param params
     * @param pk  需要清除的二级缓存id，如果传递null，则清除所有二级缓存
     * @param keys 传递null或不传递表示不清除任何查询缓存
     */
    void update(String sql,Params params,PK pk,String... keys);


    /**
     *
     * @param sql
     * @param params
     * @param canDelay
     * @param pk
     * @param keys
     */
    void update(String sql,Params params,boolean canDelay,PK pk,String... keys);

    /**
     * 求总和
     * @param sql
     * @param params
     * @return
     */
    long count(String sql,Params params);

    /**
     * 批量操作，会清除所有二级和查询缓存
     * @param sql
     * @param paramList
     */
    void batch(String sql , List<List<Param>> paramList);


    void batch(String sql , List<List<Param>> paramList,String ... keys);


    /**
     * 此方法不会使用查询缓存，也不会引起任何缓存的清理
     * @param sql
     * @param paramList
     * @return
     */
    List<Map<String,Object>> query(String sql,List<Param> paramList);

    /**
     * 不适用查询缓存也不会清理缓存
     * @param sql
     * @param params
     * @param handler
     * @param <E>
     * @return
     */
    <E> E query(String sql, List<Param> params, ResultSetHandler<E> handler);

    static final String ALL_QUERY_CACHE = "all";

}
