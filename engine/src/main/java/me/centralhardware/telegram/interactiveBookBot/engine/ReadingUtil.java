package me.centralhardware.telegram.interactiveBookBot.engine;

import me.centralhardware.telegram.interactiveBookBot.engine.Storage.CurrentUser;
import me.centralhardware.telegram.interactiveBookBot.engine.cache.ReadingTimeCache;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

import java.util.StringTokenizer;

@Slf4j
@Component
@AllArgsConstructor
public class ReadingUtil {

    private final CurrentUser currentUser;
    private final ReadingTimeCache cache;

    public int getReadingTime(String text){
        Integer readingSpeed = currentUser.getReadingSpeed();

        if (cache.contains(Pair.of(text, readingSpeed))) return cache.get(Pair.of(text, readingSpeed));

        var delay = getDelay(text);

        cache.set(Pair.of(text, readingSpeed), delay);
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
