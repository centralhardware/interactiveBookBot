package com.centralhardware.telegram.interactiveBookBot.engine;

import com.centralhardware.telegram.interactiveBookBot.engine.Storage.CurrentUser;
import com.centralhardware.telegram.interactiveBookBot.engine.cache.RedisCache;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.StringTokenizer;

@Slf4j
@Component
@AllArgsConstructor
public class ReadingUtil {

    private final CurrentUser currentUser;
    private final RedisCache cache;

    public int getReadingTime(String text){
        if (cache.contains(text)) return cache.get(text);

        var delay = getDelay(text);

        cache.set(text, delay);
        log.info("Calculated delay {} seconds saved to cache(size {}) for text {}",
                delay,
                cache.size(),
                text);

        return delay;
    }

    private Integer getDelay(String text){
        StringTokenizer stringTokenizer = new StringTokenizer(text);
        int wordCount = stringTokenizer.countTokens();

        return  (int) (((double)wordCount / currentUser.getReadingSpeed()) * 60);
    }

}
