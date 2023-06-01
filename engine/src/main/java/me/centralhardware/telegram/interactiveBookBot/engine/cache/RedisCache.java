package me.centralhardware.telegram.interactiveBookBot.engine.cache;

import me.centralhardware.telegram.interactiveBookBot.engine.hash.Hasher;
import me.centralhardware.telegram.interactiveBookBot.engine.redis.Redis;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisCache  implements Cache<String, Integer> {

    private static final String CACHE_VALUE_PREFIX = "cache:";
    private static final String CACHE_VALUE_PATTERN = "cache:*";

    private final Redis redis;
    private final Hasher hasher;

    @PostConstruct
    public void init(){
        if (BooleanUtils.toBoolean(System.getenv("INVALIDATE_CACHE"))){
            invalidate();
        }
    }

    @Override
    public void set(String key, Integer value) {
        redis.put(key(hasher.hash(key)), value);
    }

    @Override
    public Integer get(String key) {
        return Integer.parseInt(redis.get(key(hasher.hash(key))));
    }

    @Override
    public boolean contains(String key) {
        return redis.exists(key(hasher.hash(key)));
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

    private String key(String key){
        return CACHE_VALUE_PREFIX + key;
    }


}
