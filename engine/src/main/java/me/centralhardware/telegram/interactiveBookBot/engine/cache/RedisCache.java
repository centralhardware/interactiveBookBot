package me.centralhardware.telegram.interactiveBookBot.engine.cache;

import jakarta.annotation.PostConstruct;
import me.centralhardware.telegram.interactiveBookBot.engine.redis.Redis;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public abstract class RedisCache<K, V>  implements Cache<K, V> {

    private static final String CACHE_VALUE_PATTERN = "cache:*";

    @Autowired
    private Redis redis;


    /**
     * Invalidate cache, if INVALIDATE_CACHE true
     */
    @PostConstruct
    public void init(){
        if (BooleanUtils.toBoolean(System.getenv("INVALIDATE_CACHE"))){
            invalidate();
        }
    }

    @Override
    public void set(K key, V value) {
        redis.put(key(key), value);
    }

    @Override
    public V get(K key) {
        return value(redis.get(key(key)));
    }

    @Override
    public boolean contains(K key) {
        return redis.exists(key(key));
    }

    @Override
    public Integer size() {
        return redis.keys(CACHE_VALUE_PATTERN).size();
    }

    @Override
    public void invalidate() {
        redis.keys(CACHE_VALUE_PATTERN)
                .forEach(redis::delete);
    }

    /**
     * Convert {@link K} to string key
     */
    protected abstract String key(K key);

    /**
     * COnvert string value to {@link  V}
     */
    protected abstract V value(String value);


}
