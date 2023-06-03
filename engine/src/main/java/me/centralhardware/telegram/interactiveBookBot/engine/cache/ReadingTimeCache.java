package me.centralhardware.telegram.interactiveBookBot.engine.cache;

import me.centralhardware.telegram.interactiveBookBot.engine.hash.Hasher;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ReadingTimeCache extends RedisCache<Pair<String, Integer>, Integer>{

    private static final String CACHE_VALUE_PREFIX = "cache:";

    @Autowired
    private Hasher hasher;

    @Override
    protected String key(Pair<String, Integer> key){
        return CACHE_VALUE_PREFIX + hasher.hash(key.getLeft()) + ":" + key.getRight();
    }

    @Override
    protected Integer value(String value) {
        return Integer.parseInt(value);
    }

}
