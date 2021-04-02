package com.reign.memorydb.index;

import com.reign.common.Lang;
import com.reign.jdbc.orm.JdbcEntity;
import com.reign.memorydb.MemoryTable;
import com.reign.memorydb.annotation.BTreeIndex;
import com.reign.memorydb.annotation.BTreeIndexs;

import java.util.Map;

/**
 * @ClassName: IndexManagerFactory
 * @Description: 索引管理器工厂
 * @Author: wuwx
 * @Date: 2021-04-02 17:17
 **/
public class IndexManagerFactory {

    /**
     * 初始化索引
     *
     * @param clazz
     * @param table
     * @param entity
     * @param indexTable
     */
    public static void initIndex(Class<?> clazz, MemoryTable table, JdbcEntity entity, Map indexTable) {
        BTreeIndexs indexs = Lang.getAnnotation(clazz, BTreeIndexs.class);
        if (null!=indexs){
            for (BTreeIndex index:indexs.value()){
                initIndex(index,table,entity,indexTable);
            }
        }else {
            BTreeIndex index = Lang.getAnnotation(clazz,BTreeIndex.class);
            initIndex(index,table,entity,indexTable);
        }

    }


    /**
     * 实际初始化索引，创建索引管理器
     * @param index
     * @param table
     * @param entity
     * @param indexTable
     */
    private static void initIndex(BTreeIndex index,MemoryTable<?,?> table,JdbcEntity entity,Map<String,IndexManager<?>> indexTable){
        if (index == null) return;
        IndexManager<?> manager = null;
        if (index.unique()){
            manager = new BTreeIndexManager(table,entity,index);
        }else {
            manager = new MultiBtreeIndexManager(table,entity,index);
        }
        indexTable.put(index.name(),manager);
    }
}
