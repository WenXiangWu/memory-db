package com.fastdb.index;

import java.util.*;

/**
 * @ClassName: MultiBPlusTree
 * @Description: 非唯一索引
 * @Author: wuwx
 * @Date: 2020-10-14 17:29
 **/
public class MultiBPlusTree<K extends Comparable<K>, V> {

    //默认集合大小
    private static final int DEFAUL_VALUES_PER_KEY = 3;

    //真正存储数据的BPlusTree
    private BPlusTree<K, Collection<V>> innerTree;

    public MultiBPlusTree() {
        innerTree = new BPlusTree<>();
    }

    /**
     * 插入数据
     *
     * @param key
     * @param value
     */
    public void insert(K key, V value) {
        List<V> collection = (List<V>) innerTree.find(key);
        if (null == collection) {
            collection = new ArrayList<>(DEFAUL_VALUES_PER_KEY);
            innerTree.insert(key, collection);
        }
        collection.add(value);
    }


    public Collection<V> find(K key) {
        return innerTree.find(key);
    }


    public List<V> find(K key, Comparator<K> comparator) {
        List<Collection<V>> result = innerTree.find(key, comparator);
        List<V> resultList = new ArrayList<>(result.size() * DEFAUL_VALUES_PER_KEY);
        for (Collection<V> collection : result) {
            resultList.addAll(collection);
        }
        return resultList;

    }

    /**
     * 范围查找
     *
     * @param start
     * @param end
     * @return
     */
    public List<V> rangeFind(K start, K end) {
        List<Collection<V>> result = innerTree.rangeFind(start, end);
        List<V> resultList = new ArrayList<>(result.size() * DEFAUL_VALUES_PER_KEY);
        for (Collection<V> collection : result) {
            resultList.addAll(collection);
        }
        return resultList;
    }


    public Collection<V> remove(K key) {
        return innerTree.remove(key);
    }

    public int getHeight() {
        return innerTree.getHeight();
    }


    public static void main(String[] args) {
        MultiBPlusTree<Integer, String> myTree = new MultiBPlusTree<>();
        int max = 1000000;
        long start = System.currentTimeMillis();
        for (int i=0;i<=max;i++){
            myTree.insert(i,String.valueOf(i));
        }
        System.out.println("time cost with BPlusTree"+(System.currentTimeMillis()-start));
        System.out.println("数据已经插入树");

        start = System.currentTimeMillis();
        Map<Integer,String> hashMap = new HashMap<>();
        for (int i=0;i<max;i++){
            hashMap.put(i,String.valueOf(i));
        }
        System.out.println("time cost with hashmap "+ (System.currentTimeMillis()-start));
        System.out.println("over~");
    }



}
