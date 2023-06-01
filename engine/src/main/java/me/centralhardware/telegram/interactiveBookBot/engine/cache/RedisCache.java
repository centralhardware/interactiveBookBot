package me.centralhardware.telegram.interactiveBookBot.engine.cache;

import me.centralhardware.telegram.interactiveBookBot.engine.hash.Hasher;
import me.centralhardware.telegram.interactiveBookBot.engine.redis.Redis;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisCache  implements Cache<Pair<String, Integer>, Integer> {

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
    public void set(Pair<String, Integer> key, Integer value) {
        redis.put(key(hasher.hash(key.getLeft()), key.getRight()), value);
    }

    @Override
    public Integer get(Pair<String, Integer> key) {
        return Integer.parseInt(redis.get(key(hasher.hash(key.getLeft()), key.getRight())));
    }

    @Override
    public boolean contains(Pair<String, Integer> key) {
        return redis.exists(key(hasher.hash(key.getLeft()), key.getRight()));
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

    private String key(String key, Integer readingSpeed){
        return CACHE_VALUE_PREFIX + key + ":" + readingSpeed;
    }


}
