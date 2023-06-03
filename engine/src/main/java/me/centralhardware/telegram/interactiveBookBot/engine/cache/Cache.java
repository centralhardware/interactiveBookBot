package me.centralhardware.telegram.interactiveBookBot.engine.cache;

/**
 * KV storage
 */
public interface Cache<K,V> {

    /**
     * Set value for key
     */
    void set(K key, V value);

    /**
     * Get value for key
     */
    V get(K key);

    /**
     * @return True, if giving key exists in storage
     */
    boolean contains(K key);

    /**
     * @return Count of keys
     */
    Integer size();

    /**
     * Remove of keys from storage
     */
    void invalidate();

}
