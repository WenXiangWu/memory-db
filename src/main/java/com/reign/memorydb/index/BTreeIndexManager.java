package com.reign.memorydb.index;

import com.reign.jdbc.orm.IdEntity;
import com.reign.jdbc.orm.JdbcEntity;
import com.reign.jdbc.orm.JdbcField;
import com.reign.jdbc.orm.JdbcModel;
import com.reign.memorydb.BPlusTree;
import com.reign.memorydb.MemoryTable;
import com.reign.memorydb.annotation.BTreeIndex;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * @ClassName: BTreeIndexManager
 * @Description: b+树索引管理器
 * @Author: wuwx
 * @Date: 2021-04-02 17:16
 **/
public class BTreeIndexManager<V extends JdbcModel> implements IndexManager<V> {

    //实体对象
    private JdbcEntity entity;

    //索引名称
    private String name;

    //索引列
    private JdbcField[] fields;

    //索引树
    private BPlusTree<String, String> indexTree;

    //主键索引
    private IdEntity id;

    //主表
    private MemoryTable table;

    //最左前缀匹配器
    private Comparator<String> leftComparator;


    /**
     * 索引管理器构造函数
     *
     * @param table
     * @param entity
     * @param index
     */
    public BTreeIndexManager(MemoryTable table, JdbcEntity entity, BTreeIndex index) {
        this.entity = entity;
        this.table = table;
        this.name = index.name();
        String[] columns = index.value();
        List<JdbcField> indexFields = new ArrayList<>();
        for (String column : columns) {
            JdbcField temp = null;
            for (JdbcField field : entity.getFields()) {
                if (column.equals(field.propertyName)) {
                    temp = field;
                    break;
                }
            }
            if (null == temp) throw new RuntimeException("cannot found index column ,index:" + column);

            indexFields.add(temp);
        }
        this.fields = indexFields.toArray(new JdbcField[0]);
        this.indexTree = new BPlusTree<>();
        this.id = entity.getId();
        this.leftComparator = new Comparator<String>() {
            @Override
            public int compare(String key, String searchKey) {
                if (key.startsWith(searchKey)) {
                    return 0;
                }
                return key.compareTo(searchKey);
            }
        };
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public void insert(V value) {
        String idKey = id.getKeyValueByObject(value);
        String indexKey = getKeyValueByObject(value);
        indexTree.insert(indexKey, idKey);
    }

    /**
     * 根据对象获取索引key值
     *
     * @param obj
     * @return
     */
    private String getKeyValueByObject(Object obj) {
        try {
            Object[] array = new Object[fields.length];
            int index = 0;
            for (JdbcField field : fields) {
                field.field.setAccessible(true);
                array[index++] = field.field.get(obj);
            }
            return toString(array);
        } catch (Throwable t) {
            throw new RuntimeException("get Key error" + t);
        }

    }

    /**
     * 将数组转换为key
     *
     * @param array
     * @return
     */
    private String toString(Object[] array) {
        StringBuilder builder = new StringBuilder();
        for (Object obj : array) {
            builder.append(obj.toString()).append(":");
        }
        return builder.toString();
    }

    @Override
    public void remove(V value) {
        String indexKey = getKeyValueByObject(value);
        indexTree.remove(indexKey);
    }

    @Override
    public void remove(Object... args) {
        String indexKey = getKeyValueByObject(args);
        indexTree.remove(indexKey);
    }

    @Override
    public void update(V oldValue, V newValue) {
        if (oldValue == null) return;
        String oldIndexKey = getKeyValueByObject(oldValue);
        String newIndexKey = getKeyValueByObject(newValue);
        String idKey =id.getKeyValueByObject(newValue);
        if (!oldIndexKey.equals(newIndexKey)){
            indexTree.remove(oldIndexKey);
            indexTree.insert(newIndexKey,idKey);
        }
    }

    @Override
    public V find(V value) {
        String indexKey = getKeyValueByObject(value);
        String idKey = indexTree.find(indexKey);
        return table.readByIdKey(idKey);
    }

    @Override
    public V find(Object... args) {
        return null;
    }

    @Override
    public List<V> rangeFind(V start, V end) {
        return null;
    }

    @Override
    public List<V> leftFind(Object... args) {
        return null;
    }

    @Override
    public int getHeight() {
        return 0;
    }

    @Override
    public void clear() {

    }
}
