package com.reign.memorydb;

import com.reign.common.Lang;
import com.reign.jdbc.Params;
import com.reign.jdbc.Type;
import com.reign.jdbc.orm.IBaseDao;
import com.reign.jdbc.orm.JdbcEntity;
import com.reign.jdbc.orm.JdbcField;
import com.reign.jdbc.orm.JdbcModel;
import com.reign.jdbc.orm.cache.SingleIdEntity;
import com.reign.memorydb.annotation.AutoId;
import com.reign.memorydb.annotation.SyncDBConfig;
import com.reign.memorydb.index.IndexManager;
import com.reign.memorydb.index.IndexManagerFactory;
import com.reign.memorydb.sequence.ISequenceDao;
import com.reign.memorydb.sequence.Sequence;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @ClassName: MemoryTable
 * @Description: 内存表
 * @Author: wuwx
 * @Date: 2021-04-01 18:37
 **/
public class MemoryTable<V extends AbstractDomain, K extends Serializable> {

    /**
     * 主表
     */
    private Map<String, V> mainTable = new HashMap<>();


    /**
     * 索引表
     */
    private Map<String, IndexManager<V>> indexTable = new HashMap<>();
    /**
     * 存储需要清理的key值
     */
    private Set<String> clearKeyTable = new HashSet<>();

    //jdbc相关
    private JdbcEntity entity;
    private IBaseDao<V, K> dao;
    private ISequenceDao sequenceDao;
    private Lock readLock;
    private Lock writeLock;
    private AtomicInteger id;
    private boolean autoId;
    private JdbcField idField;
    private long syncInterval;
    private AtomicInteger order;


    /**
     * 初始化
     *
     * @param dao
     * @param entity
     */
    public void init(IBaseDao<V, K> dao, JdbcEntity entity) {
        this.dao = dao;
        this.entity = entity;
        //建立索引
        IndexManagerFactory.initIndex(entity.getEntityClass(), this, entity, indexTable);

        //检验DB结构
        doTableValidation();

        //设置主键
        setId();

        //解释同步周期
        SyncDBConfig config = Lang.getAnnotation(entity.getEntityClass(), SyncDBConfig.class);
        if (null != config) {
            syncInterval = config.interval();
        } else {
            syncInterval = AsyncDBExecutor.INTERVAL;
        }

        this.order = new AtomicInteger(1);
        ReadWriteLock lock = new ReentrantReadWriteLock(false);
        this.readLock = lock.readLock();
        this.writeLock = lock.writeLock();
    }

    /**
     * 设置主键id
     */
    private void setId() {
        if (!(entity.getId() instanceof SingleIdEntity)){
            throw new RuntimeException("memory table not support complex primary key");
        }
        this.idField = entity.getIdFields()[0];

        //从db中载入最大主键
        AutoId autoId = Lang.getAnnotation(entity.getEntityClass(),AutoId.class);
        if (null!=autoId){
            this.autoId = true;
            if (null!=dao){
                int dbMaxId = getMaxId();
                if (null==id ||id.get()<dbMaxId){
                    id = new AtomicInteger(dbMaxId);
                }
                setSequence(id.get());
            }
        }

        if (this.autoId && null ==id){
            id = new AtomicInteger(0);
        }

    }

    /**
     * 设置sequence  TODO
     * @param maxId
     */
    private void setSequence(int maxId) {
        if (maxId == 0) return;
        if (null!=sequenceDao){

        }

    }

    /**
     * 发送SQL到db中查询最大主键  TODO
     * @return
     */
    private int getMaxId() {
        return 100;
    }

    /**
     * 校验db结构
     */
    public void doTableValidation() {
        try {
            List<Map<String, Object>> resultList = dao.query("DESC " + entity.getTableName(), Params.EMPTY);
            Map<String, Type> columnMap = new HashMap<>();
            for (Map<String, Object> map : resultList) {
                String columnName = (String) map.get("COLUMN_NAME");
                Type type = Lang.getJdbcType((String) map.get("COLUMN_TYPE"));
                columnMap.put(columnName.toLowerCase(), type);
            }

            JdbcField[] fields = entity.getFields();
            int count = 0;
            for (JdbcField field : fields) {
                if (field.ignore) {
                    //忽略的字段不验证
                    continue;
                }
                Type type = columnMap.get(field.columnName.toLowerCase());
                if (null == type) {
                    throw new RuntimeException("doTableValidation table " + entity.getTableName() + " don't has " + field.columnName + "  column");
                }
                if (!type.equals(field.jdbcType)) {
                    throw new RuntimeException("doTableValidation table " + entity.getTableName() + "  " + field.columnName + " type not match");
                }
                count++;
            }

            if (count != columnMap.size()) {
                throw new RuntimeException("doTableValidation table " + entity.getTableName() + " db column not match memory ");

            }


        } catch (RuntimeException e) {
            throw e;
        }

    }

    public <V extends JdbcModel> V readByIdKey(String idKey) {
        if (null==idKey) return null;
        try {
            this.readLock.lock();
            V v  = mainTable.get(idKey);
            if (null!=v){

            }
            return v;
        }finally {
            this.readLock.unlock();
        }
    }
}
