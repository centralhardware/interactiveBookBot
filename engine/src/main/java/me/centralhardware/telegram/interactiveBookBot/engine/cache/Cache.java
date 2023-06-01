package me.centralhardware.telegram.interactiveBookBot.engine.cache;

public interface Cache<K,V> {

    void set(K key, V value);

    V get(K key);

    boolean contains(K key);

    Integer size();

    void invalidate();

}
