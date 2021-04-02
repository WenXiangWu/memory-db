package com.reign.jdbc.orm;

import com.reign.jdbc.NameStrategy;
import com.reign.jdbc.orm.cache.CacheManager;

/**
 * @ClassName: JdbcEntity
 * @Description: 实体
 * @Author: wuwx
 * @Date: 2021-04-02 14:49
 **/
public class JdbcEntity {

    /**属性*/
    private JdbcField[] fields;


    /**id*/
    private IdEntity id;


    private IndexEntity index;

    /**类型*/
    private Class<?> clazz;

    /**增强*/
    private Class<?> enhanceClazz;

    /**命名策略*/
    private NameStrategy nameStrategy;

    /**实体名称*/
    private String entityName;

    /** idFields*/
    private JdbcField[] idFields;

    /**是否加强过高*/
    private boolean enhance;

    /**插入语句*/
    private String insertSQL;

    /**更新语句*/
    private String updateSQL;

    /**查询所有SQL*/
    private String selectAllSQL;

    /**查询SQL*/
    private String selectAllCountSQL;

    /**查询SQL*/
    private String selectSQL;

    /**查询SQL*/
    private String selectForUpdateSQL;

    /**删除SQL*/
    private String deleteSQL;

    /**表名*/
    private String tableName;

    /**延迟SQL是否启用*/
    private ThreadLocal<Boolean> delaySQLEnable;

    /**缓存管理器*/
    private CacheManager cacheManger;


    public JdbcField[] getFields() {
        return fields;
    }

    public void setFields(JdbcField[] fields) {
        this.fields = fields;
    }

    public IdEntity getId() {
        return id;
    }

    public void setId(IdEntity id) {
        this.id = id;
    }

    public IndexEntity getIndex() {
        return index;
    }

    public void setIndex(IndexEntity index) {
        this.index = index;
    }

    public Class<?> getEntityClass() {
        return clazz;
    }

    public void setEntityClass(Class<?> clazz) {
        this.clazz = clazz;
    }

    public Class<?> getEnhanceClazz() {
        return enhanceClazz;
    }

    public void setEnhanceClazz(Class<?> enhanceClazz) {
        this.enhanceClazz = enhanceClazz;
    }

    public NameStrategy getNameStrategy() {
        return nameStrategy;
    }

    public void setNameStrategy(NameStrategy nameStrategy) {
        this.nameStrategy = nameStrategy;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public boolean isEnhance() {
        return enhance;
    }

    public void setEnhance(boolean enhance) {
        this.enhance = enhance;
    }

    public String getInsertSQL() {
        return insertSQL;
    }

    public void setInsertSQL(String insertSQL) {
        this.insertSQL = insertSQL;
    }

    public String getUpdateSQL() {
        return updateSQL;
    }

    public void setUpdateSQL(String updateSQL) {
        this.updateSQL = updateSQL;
    }

    public String getSelectAllSQL() {
        return selectAllSQL;
    }

    public void setSelectAllSQL(String selectAllSQL) {
        this.selectAllSQL = selectAllSQL;
    }

    public String getSelectAllCountSQL() {
        return selectAllCountSQL;
    }

    public void setSelectAllCountSQL(String selectAllCountSQL) {
        this.selectAllCountSQL = selectAllCountSQL;
    }

    public String getSelectSQL() {
        return selectSQL;
    }

    public void setSelectSQL(String selectSQL) {
        this.selectSQL = selectSQL;
    }

    public String getSelectForUpdateSQL() {
        return selectForUpdateSQL;
    }

    public void setSelectForUpdateSQL(String selectForUpdateSQL) {
        this.selectForUpdateSQL = selectForUpdateSQL;
    }

    public String getDeleteSQL() {
        return deleteSQL;
    }

    public void setDeleteSQL(String deleteSQL) {
        this.deleteSQL = deleteSQL;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public ThreadLocal<Boolean> getDelaySQLEnable() {
        return delaySQLEnable;
    }

    public void setDelaySQLEnable(ThreadLocal<Boolean> delaySQLEnable) {
        this.delaySQLEnable = delaySQLEnable;
    }

    public CacheManager getCacheManger() {
        return cacheManger;
    }

    public void setCacheManger(CacheManager cacheManger) {
        this.cacheManger = cacheManger;
    }

    public JdbcField[] getIdFields() {
        return idFields;
    }

    public void setIdFields(JdbcField[] idFields) {
        this.idFields = idFields;
    }
}
