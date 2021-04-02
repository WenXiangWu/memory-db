package com.reign.memorydb;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @ClassName: BPlusTree
 * @Description: 唯一索引对应的索引树
 * @Author: wuwx
 * @Date: 2020-10-13 17:06
 **/
public class BPlusTree<T extends Comparable<T>, V> {

    /**
     * 默认阶数，10阶
     **/
    private static final int DEFAULT_M = 10;

    //M的大小，偶数个
    private int M;

    //叶子节点的头结点
    private LeafNode head;

    private Node root;

    public void print() {
        System.out.println("height" + getHeight());
        StringBuilder sb = new StringBuilder();
        this.root.print(sb, 1);
        System.out.println(sb.toString());

    }

    public BPlusTree() {
        this(DEFAULT_M);
    }

    public BPlusTree(int m) {
        this.M = m % 2 == 0 ? m : m - 1;
        //初始时根节点是一个叶子节点
        this.root = new LeafNode();
        this.head = (LeafNode) this.root;
    }

    /**
     * 设置key，value
     *
     * @param key
     * @param value
     */
    public void insert(T key, V value) {
        if (key == null) throw new NullPointerException("must not be null for key");
        Node<T, V> node = this.root.insert(key, value);
        if (node != null) this.root = node;
    }

    /**
     * 依据key查找
     *
     * @param key
     * @return
     */
    public V find(T key) {
        return (V) this.root.find(key);
    }

    /**
     * 根据自定义比较器查找
     *
     * @param key
     * @param comparator
     * @return
     */
    public List<V> find(T key, Comparator<T> comparator) {
        return (List<V>) this.root.find(key, comparator);
    }


    /**
     * 根据自定义比较器比较，全表扫描
     *
     * @param matcher
     * @return
     */
    public List<V> findAll(Matcher<V> matcher) {
        LeafNode<T, V> node = head;
        List<V> resultList = new ArrayList<>();
        while (node != null) {
            for (int i = 0; i < node.size; i++) {
                if (matcher.match((V) node.values[i])) {
                    resultList.add((V) node.values[i]);
                }
            }
            node = node.next;
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
    public List<V> rangeFind(T start, T end) {
        if (start.compareTo(end) >= 0) throw new RuntimeException("starter cannot greater than end");
        LeafNode<T, V> startNode = (LeafNode<T, V>) this.root.gteFind(start);
        LeafNode<T, V> endNode = (LeafNode<T, V>) this.root.lteFind(end);
        if (null == start) {
            return Collections.emptyList();
        } else {
            List<V> resultList = new ArrayList<>();
            LeafNode<T, V> node = startNode;
            boolean match = false;
            while (null != node && node != endNode) {
                for (int i = 0; i < node.size; i++) {
                    if (!match && start.compareTo((T) node.keys[i]) <= 0) {
                        match = true;
                    }
                    if (match) {
                        resultList.add((V) node.values[i]);
                    }
                }
                node = node.next;
            }

            for (int i = 0; i < endNode.size; i++) {
                if (!match && start.compareTo((T) node.keys[i]) <= 0) {
                    match = true;
                }
                if (end.compareTo((T) endNode.keys[i]) < 0) {
                    break;
                }
                if (match) {
                    resultList.add((V) endNode.values[i]);
                }

            }
            return resultList;
        }
    }

    /**
     * 移除指定元素
     *
     * @param key
     * @return
     */
    public V remove(T key) {
        V v = (V) this.root.remove(key);
        return v;
    }

    /**
     * 获取树高
     *
     * @return
     */
    public int getHeight() {
        int height = 1;
        Node node = this.root;
        while (!(node instanceof LeafNode)) {
            height++;
            node = ((InternalNode) node).pointers[0];
        }
        return height;
    }

    /**
     * 内部节点
     */
    abstract class Node<T extends Comparable<T>, V> {

        //父节点
        protected Node<T, V> parent;
        //keys节点key值
        protected Object[] keys;
        //节点大小
        protected int size;

        /**
         * 插入数据
         *
         * @param key
         * @param value
         * @return
         */
        abstract Node<T, V> insert(T key, V value);

        /**
         * 删除数据
         *
         * @param key
         * @return
         */
        abstract V remove(T key);

        /**
         * 查找数据
         *
         * @param key
         * @return
         */
        abstract V find(T key);

        /**
         * 查找
         *
         * @param key
         * @return
         */
        abstract Node<T, V> gteFind(T key);

        abstract Node<T, V> lteFind(T key);

        abstract List<V> find(T key, Comparator<T> comparator);

        /**
         * 打印节点
         *
         * @param sb
         * @param height
         */
        abstract void print(StringBuilder sb, int height);


        /**
         * 采用二分法查找key 值
         *
         * @param key
         * @return
         */
        protected final int binaryFind(T key) {
            //找到位置
            int start = 0;
            int end = this.size;
            int middle = (start + end) / 2;
            while (start < end) {
                if (start == middle) break;
                T middleKey = (T) this.keys[middle];
                int cvalue = key.compareTo(middleKey);
                if (cvalue == 0) return middle;
                if (cvalue < 0) {
                    end = middle;
                } else {
                    start = middle;
                }
                middle = (start + end) / 2;
            }

            T middleKey = (T) this.keys[middle];
            if (key.compareTo(middleKey) == 0) {
                //找到
                return middle;
            }
            //未找到
            return -1;
        }

    }

    /**
     * 非叶子节点，只存储keys
     *
     * @param <T>
     * @param <V>
     */
    class InternalNode<T extends Comparable<T>, V> extends Node<T, V> {

        //指向下一级的指针
        private Node<T, V>[] pointers;

        public InternalNode() {
            this.pointers = new Node[M];
            this.size = 0;
            this.keys = new Object[M];
        }

        @Override
        Node<T, V> insert(T key, V value) {
            int i = 1;
            for (; i < this.size; i++) {
                if (key.compareTo((T) this.keys[i]) < 0) break;
            }

            return this.pointers[i - 1].insert(key, value);
        }

        @Override
        V remove(T key) {
            int i = 1;
            for (; i < this.size; i++) {
                if (key.compareTo((T) this.keys[i]) < 0) break;
            }

            return this.pointers[i - 1].remove(key);
        }

        @Override
        V find(T key) {
            int i = 1;
            for (; i < this.size; i++) {
                if (key.compareTo((T) this.keys[i]) < 0) break;
            }
            return this.pointers[i - 1].find(key);
        }

        @Override
        Node<T, V> gteFind(T key) {
            int i = 1;
            for (; i < this.size; i++) {
                if (key.compareTo((T) this.keys[i]) < 0) break;
            }
            return this.pointers[i - 1].gteFind(key);
        }

        @Override
        Node<T, V> lteFind(T key) {
            int i = 1;
            for (; i < this.size; i++) {
                if (key.compareTo((T) this.keys[i]) < 0) break;
            }
            return this.pointers[i - 1].lteFind(key);
        }

        @Override
        List<V> find(T key, Comparator<T> comparator) {
            int i = 1;
            List<V> resultList = new ArrayList<>();
            for (; i < this.size; i++) {
                int cvalue = comparator.compare((T) this.keys[i], key);
                if (cvalue == 0) {
                    resultList.addAll(this.pointers[i - 1].find(key, comparator));
                } else if (cvalue > 0) {
                    break;
                }
            }
            resultList.addAll(this.pointers[i - 1].find(key, comparator));
            return resultList;
        }

        @Override
        void print(StringBuilder sb, int height) {
            int i = 0;
            sb.append("T").append(height).append("(");
            for (; i < this.size; i++) {
                sb.append(this.keys[i]).append(" ");
            }
            sb.append(") ");
            StringBuilder innerBuilder = new StringBuilder();
            for (i = 0; i < this.size; i++) {
                this.pointers[i].print(innerBuilder, height + 1);
            }
            innerBuilder.append("\n");
            sb.append("\n").append(innerBuilder.toString());

        }


        /**
         * 更新key值
         *
         * @param newKey
         * @param oldKey
         * @param node
         */
        public void update(T newKey, T oldKey, Node<T, V> node) {
            //找到位置
            int middle = binaryFind(oldKey);
            if (middle == -1) return;
            //找到位置了
            this.keys[middle] = newKey;
            this.pointers[middle] = node;
            if (middle == 0 && this.parent != null) {
                ((InternalNode) this.parent).update(newKey, oldKey, this);
            }
        }

        /**
         * 移除指针
         *
         * @param key
         */
        public void removePointer(T key) {
            //找到位置
            int middle = binaryFind(key);
            T headKey = (T) this.keys[0];
            System.arraycopy(this.keys, middle + 1, this.keys, middle, this.size - middle - 1);
            System.arraycopy(this.pointers, middle + 1, this.pointers, middle, this.size - middle - 1);
            this.keys[this.size - 1] = null;
            this.pointers[this.size - 1] = null;
            this.size--;

            int m = M / 2;
            if (this.size < m) {
                if (null == this.parent && this.size < 2) {
                    //头结点和子节点合并
                    root = this.pointers[0];
                    this.pointers[0].parent = null;
                } else if (null != this.parent) {
                    int index = ((InternalNode) this.parent).binaryFind(headKey);
                    InternalNode previous = (InternalNode) ((index > 0) ? ((InternalNode) this.parent).pointers[index - 1] : null);
                    InternalNode next = (InternalNode) ((index + 1 < this.parent.size) ? ((InternalNode) this.parent).pointers[index + 1] : null);

                    //少于m/2个节点
                    if (previous != null && previous.size > m) {
                        //找找前节点补借
                        //从尾部删除
                        T k = (T) previous.keys[previous.size - 1];
                        Node pointer = previous.pointers[previous.size - 1];
                        previous.deleteFromTail();
                        //加入头部
                        insertIntoHead(k, pointer);
                        ((InternalNode) this.parent).update(k, headKey, this);
                    } else if (next != null && next.size > m) {
                        //找找后节点补借
                        T k = (T) next.keys[0];
                        Node pointer = next.pointers[0];
                        next.deleteFromHead();
                        ((InternalNode) this.parent).update((T) next.keys[0], k, next);
                        //加入尾部
                        insertIntoTail(k, pointer);
                    } else {
                        if (previous != null && previous.size <= m) {
                            //同前面节点合并
                            for (int i = 0; i < this.size; i++) {
                                previous.insertIntoTail(this.keys[i], this.pointers[i]);
                            }
                            //父节点移除
                            ((InternalNode) this.parent).removePointer(headKey);
                        } else if (next != null && next.size <= m) {
                            //同后面节点合并
                            for (int i = 0; i < next.size; i++) {
                                previous.insertIntoTail(next.keys[i], next.pointers[i]);
                            }
                            //父节点移除
                            ((InternalNode) this.parent).removePointer((T) next.keys[0]);
                        } else {
                            BPlusTree.this.print();
                            throw new RuntimeException("unknown error");


                        }


                    }

                }

            }

        }

        /**
         * 插入到头部
         *
         * @param key
         * @param value
         */
        public void insertIntoHead(Object key, Node value) {
            System.arraycopy(this.keys, 0, this.keys, 1, this.size);
            System.arraycopy(this.pointers, 0, this.pointers, 1, this.size);
            this.keys[0] = key;
            this.pointers[0] = value;
            this.size++;
            value.parent = this;
        }

        /**
         * 插入到尾部
         *
         * @param key
         * @param value
         */
        public void insertIntoTail(Object key, Node value) {
            this.keys[this.size] = key;
            this.pointers[this.size] = value;
            this.size++;
            value.parent = this;
        }

        /**
         * 从头部删除元素
         */
        public void deleteFromHead() {
            System.arraycopy(this.keys, 1, this.keys, 0, this.size - 1);
            System.arraycopy(this.pointers, 1, this.pointers, 0, this.size - 1);
            this.keys[this.size - 1] = null;
            this.pointers[this.size - 1] = null;
            this.size--;
        }

        /**
         * 从尾部删除元素
         */
        public void deleteFromTail() {
            int index = this.size - 1;
            this.keys[index] = null;
            this.pointers[index] = null;
            this.size--;
        }


        /**
         * 插入元素
         *
         * @param leftKey
         * @param left
         * @param rightKey
         * @param right
         * @return
         */
        private Node<T, V> insert(T leftKey, Node<T, V> left, T rightKey, Node<T, V> right) {
            //数组为0时
            if (this.size == 0) {
                this.keys[0] = leftKey;
                this.keys[1] = rightKey;

                this.pointers[0] = left;
                this.pointers[1] = right;

                left.parent = this;
                right.parent = this;
                this.size += 2;
                return this;
            }


            if (this.size >= M) {
                //查找插入位置
                int i = 0;
                for (; i < this.size; i++) {
                    T curKey = (T) this.keys[i];
                    if (curKey.compareTo(rightKey) > 0) break;
                }

                //已满，需要分裂
                int m = this.size / 2;

                //split the internal node
                InternalNode<T, V> rightNode = new InternalNode<>();
                rightNode.size = this.size - m;
                System.arraycopy(this.keys, m, rightNode.keys, 0, this.size - m);
                System.arraycopy(this.pointers, m, rightNode.pointers, 0, this.size - m);

                //reset the children's parent to the new node
                for (int j = 0; j < rightNode.size; j++) {
                    rightNode.pointers[j].parent = rightNode;
                }

                //清理自己
                for (int j = m; j < this.size; j++) {
                    this.keys[j] = null;
                    this.pointers[j] = null;
                }
                this.size = m;

                //建立新的父节点
                if (this.parent == null) {
                    this.parent = new InternalNode<>();
                }
                rightNode.parent = this.parent;

                if (i >= m) {
                    rightNode.insert(null, null, rightKey, right);
                } else {
                    this.insert(null, null, rightKey, right);
                }

                return ((InternalNode<T, V>) this.parent).insert((T) this.keys[0], this, (T) rightNode.keys[0], rightNode);
            }

            //查找插入位置
            int i = 0;
            for (; i < this.size; i++) {
                T curKey = (T) this.keys[i];
                if (curKey.compareTo(rightKey) > 0) break;
            }

            //插入
            System.arraycopy(this.keys, i, this.keys, i + 1, size - i);
            System.arraycopy(this.pointers, i, this.pointers, i + 1, size - i);
            this.keys[i] = rightKey;
            this.pointers[i] = right;
            right.parent = this;
            this.size++;
            return null;
        }
    }


    /**
     * 叶子节点，用来存储keys和真正的value;
     *
     * @param <T>
     * @param <V>
     */
    class LeafNode<T extends Comparable<T>, V> extends Node<T, V> {

        //叶节点的前节点
        protected LeafNode<T, V> previous;

        //叶节点的后节点
        protected LeafNode<T, V> next;


        private Object[] values;

        public LeafNode() {
            this.size = 0;
            this.keys = new Object[M];
            this.values = new Object[M];
            this.parent = null;
        }


        @Override
        public Node<T, V> insert(T key, V value) {
            if (this.size >= M) {
                //走到这里表明新插入key的大小必定在该node数组中间；需要找到插入的位置
                int i = 0;
                for (; i < this.size; i++) {
                    T curKey = (T) this.keys[i];
                    int cvalue = curKey.compareTo(key);
                    if (cvalue == 0) {
                        //如果插入的key已经存在，则覆盖值
                        values[i] = value;
                        return null;
                    }
                    if (cvalue > 0) break;
                }
                //已满，分裂
                int m = this.size / 2;
                //分裂出一个右节点
                LeafNode<T, V> rightNode = new LeafNode<>();
                rightNode.size = this.size - m;
                //对右节点赋值，并且清理自己分裂出去的部分节点数据,保留左边的数据
                System.arraycopy(this.keys, m, rightNode.keys, 0, rightNode.size);
                System.arraycopy(this.values, m, rightNode.values, 0, rightNode.size);
                //清理原节点分裂出去的右边的数据
                for (int j = m; j < this.size; j++) {
                    this.keys[j] = null;
                    this.values[j] = null;
                }
                //设置原节点新size;因为M 已经保证必定是偶数；所以分裂出去的两个size相等
                this.size = m;
                //设置链接
                if (next != null) {
                    next.previous = rightNode;
                    rightNode.next = next;
                }
                if (previous == null) {
                    head = this;
                }
                rightNode.previous = this;
                this.next = rightNode;

                //插入节点;如果插入的位置大于原节点的中点，则查到分裂出去的右边节点中；否则查到原节点中
                if (i >= m) {
                    rightNode.insert(key, value);
                } else {
                    this.insert(key, value);
                }

                //设置父节点
                if (this.parent == null) {
                    this.parent = new InternalNode<>();
                }
                rightNode.parent = this.parent;
                //父节点插入
                return ((InternalNode<T, V>) this.parent).insert((T) this.keys[0], this, (T) rightNode.keys[0], rightNode);
            }


            //若节点没有满，则通过遍历节点中key并比较后找到合适位置并插入
            int i = 0;
            T headKey = (T) this.keys[0];
            for (; i < this.size; i++) {
                T curKey = (T) this.keys[i];
                int cvalue = curKey.compareTo(key);
                if (cvalue == 0) {
                    values[i] = value;
                    return null;
                }
                if (cvalue > 0) break;
            }

            System.arraycopy(this.keys, i, this.keys, i + 1, size - i);
            System.arraycopy(this.values, i, this.values, i + 1, size - i);
            this.keys[i] = key;
            this.values[i] = value;
            this.size++;

            //更新父节点
            if (i == 0 && this.parent != null) {
                ((InternalNode<T, V>) this.parent).update(key, headKey, this);
            }
            return null;
        }

        @Override
        V remove(T key) {
            if (this.size == 0) return null;
            int middle = binaryFind(key);
            if (middle != -1) {
                //节点中第一个值
                T headKey = (T) this.keys[0];
                V value = (V) this.values[middle];
                //将middle后面的节点向前移动一位覆盖原来的
                System.arraycopy(this.keys, middle + 1, this.keys, middle, this.size - middle - 1);
                System.arraycopy(this.values, middle + 1, this.values, middle, this.size - middle - 1);
                //删除最后一位
                this.keys[this.size - 1] = null;
                this.values[this.size - 1] = null;
                this.size--;

                int m = M / 2;
                if (this.size < m) {
                    //少于 m/2个节点; 有前驱节点并且前驱节点大于一半阶数，并且是同一个父节点
                    if (this.previous != null && this.previous.size > m && this.previous.parent == this.parent) {
                        //将前面一个节点的最后一个元素借过来，并加到当前节点的头部
                        T k = (T) this.previous.keys[this.previous.size - 1];
                        V v = (V) this.previous.values[this.previous.size - 1];
                        this.previous.deleteFromTail();
                        //加入当前节点头部
                        this.insertIntoHead(k, v);
                    } else if (this.next != null && this.next.size > m && this.next.parent == this.parent) {
                        //将后面节点的第一个元素借过来作为当前节点的最后一个元素
                        T k = (T) this.next.keys[0];
                        V v = (V) this.next.values[0];
                        this.next.deleteFromHead();
                        if (this.next.parent != null) {
                            ((InternalNode<T, V>) this.next.parent).update((T) this.next.keys[0], k, this.next);
                        }
                        //加入当前节点的尾部
                        this.insertIntoTail(k, v);
                    } else {
                        //与前面的节点合并
                        if (this.previous != null && this.previous.size <= m && this.previous.parent == this.parent) {
                            for (int i = 0; i < this.size; i++) {
                                this.previous.insertIntoTail(this.keys[i], this.values[i]);
                            }
                            //父节点移除
                            ((InternalNode<T, V>) this.parent).removePointer(headKey);
                            //更新头结点
                            headKey = (T) this.keys[0];
                            //修正叶子节点链接
                            this.parent = null;
                            LeafNode node = this.next;
                            if (node != null) {
                                node.previous = this.previous;
                                this.previous.next = node;
                            } else {
                                this.previous.next = null;
                            }

                        } else if (this.next != null && this.next.size <= m && this.next.parent == this.parent) {
                            //与后面节点合并
                            for (int i = 0; i < this.next.size; i++) {
                                this.insertIntoTail(this.next.keys[i], this.next.values[i]);
                            }
                            //父节点移除
                            ((InternalNode<T, V>) this.parent).removePointer((T) this.next.keys[0]);
                            //修正叶子节点链接
                            LeafNode node = this.next;
                            if (node.next != null) {
                                node.next.previous = this;
                                this.next = node.next;
                            } else {
                                this.next = null;
                            }

                        } else if (this.parent != null) {
                            System.out.println("BPlusTree Error");
                            BPlusTree.this.print();
                            throw new RuntimeException("unknown error");
                        }
                    }
                }
                if (null != this.parent && headKey.compareTo((T) this.keys[0]) != 0) {
                    ((InternalNode<T, V>) this.parent).update((T) this.keys[0], headKey, this);
                }
                return (V) value;
            } else {
                //没找到
                return null;
            }
        }


        /**
         * 插入头部
         *
         * @param key
         * @param value
         */
        public void insertIntoHead(Object key, Object value) {
            System.arraycopy(this.keys, 0, this.keys, 1, this.size);
            System.arraycopy(this.values, 0, this.values, 1, this.size);
            this.keys[0] = key;
            this.values[0] = value;
            this.size++;
        }

        /**
         * 删除头部节点
         */
        public void deleteFromHead() {
            System.arraycopy(this.keys, 1, this.keys, 0, this.size - 1);
            System.arraycopy(this.values, 1, this.values, 0, this.size - 1);
            this.keys[this.size - 1] = null;
            this.values[this.size - 1] = null;
            this.size--;
        }


        /**
         * 删除尾部节点
         */
        public void deleteFromTail() {
            int index = this.size - 1;
            this.keys[index] = null;
            this.values[index] = null;
            this.size--;
        }

        /**
         * 插入尾部
         *
         * @param key
         * @param value
         */
        public void insertIntoTail(Object key, Object value) {
            this.keys[this.size] = key;
            this.values[this.size] = value;
            this.size++;
        }

        @Override
        public V find(T key) {
            if (this.size == 0) return null;
            int middle = binaryFind(key);
            return middle != -1 ? (V) this.values[middle] : null;
        }

        /**
         * 如果key比第一个节点大，这个Node就包含需要查找的内容
         *
         * @param key
         * @return
         */
        @Override
        Node<T, V> gteFind(T key) {
            return key.compareTo((T) this.keys[0]) >= 0 ? this : null;
        }

        @Override
        Node<T, V> lteFind(T key) {
            return this;
        }

        @Override
        List<V> find(T key, Comparator<T> comparator) {
            if (this.size == 0) return Collections.emptyList();
            List<V> resultList = new ArrayList<>(this.size);
            for (int i = 0; i < this.size; i++) {
                int cvalue = comparator.compare((T) this.keys[i], key);
                if (cvalue == 0) {
                    resultList.add((V) this.values[i]);
                } else if (cvalue > 0) {
                    break;
                }
            }
            return resultList;
        }

        @Override
        void print(StringBuilder sb, int height) {
            sb.append("L").append(height).append("(");
            int i = 0;
            for (; i < this.size; i++) {
                sb.append(this.keys[i]).append(",").append(this.values[i]).append("_");
            }
            sb.deleteCharAt(sb.length() - 1);
            sb.append(") ");
        }


    }
}

