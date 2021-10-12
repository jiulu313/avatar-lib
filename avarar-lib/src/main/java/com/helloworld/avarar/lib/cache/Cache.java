package com.helloworld.avarar.lib.cache;

public interface Cache<K,V> {
    void save(K key, V value);
    V get(K key);
    V remove(K key);
}
