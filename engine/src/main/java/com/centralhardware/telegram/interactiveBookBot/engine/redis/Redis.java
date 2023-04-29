package com.centralhardware.telegram.interactiveBookBot.engine.redis;

import com.centralhardware.telegram.interactiveBookBot.engine.Storage.CurrentBook;
import com.centralhardware.telegram.interactiveBookBot.engine.Storage.CurrentUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@Slf4j
@Component
public class Redis{

    private final JedisPool pool;

    public final Supplier<String> endingsKeyFormatter;
    public final Function<String, String > endingsKeyForBookFormatter;
    public final Supplier<String> completePartFormatter;
    public final Supplier<String> bookRatingFormatter;
    public final Supplier<String> bookChoseFormatter;
    public final Supplier<String> lastBookChooses;

    public Redis(JedisPool pool, CurrentUser currentUser, CurrentBook currentBook){
        this.pool = pool;

        endingsKeyFormatter
                = () -> "countOfEndings:" + currentBook.getBookId() + ":" + currentUser.getChatId();
        endingsKeyForBookFormatter
                = bookId -> "countOfEndings:" + bookId + ":" + currentUser.getChatId();
        completePartFormatter =
                () -> "completeParts:" + currentBook.getBookId() + ":" + currentUser.getChatId();
        bookRatingFormatter =
                () -> "bookRating:" + currentBook.getBookId() + ":" + currentUser.getChatId();
        bookChoseFormatter =
                () -> "bookChose:" + currentBook.getBookId();
        lastBookChooses =
                () -> "lastBookChooses:" + currentBook.getBookId() + ":" + currentUser.getChatId();
    }

    public <V> void put(String key, V value){
        execute(jedis -> jedis.set(key, value.toString()));
    }

    @SuppressWarnings("unchecked")
    public <V> V get(String key){
        return (V) execute(jedis -> jedis.get(key));
    }

    public <V> void sadd(String key, V value){
        executeVoid(jedis -> jedis.sadd(key, value.toString()));
    }

    public Long scard(String key){
        return execute(jedis -> {
            Long count = jedis.scard(key);
            log.info("{} elements in set {}", count, key);
            return count;
        });
    }

    public <T> Boolean sismember(String key, T value){
        return execute(jedis -> jedis.sismember(key, value.toString()));
    }

    public List<String> smembers(String key){
        return execute(jedis -> jedis.smembers(key).stream().toList());
    }

    public Boolean exists(String key){
        return execute(jedis -> jedis.exists(key));
    }

    public void delete(String key){
        executeVoid(jedis -> jedis.del(key));
    }

    public Set<String> keys(String keys){
        return execute(jedis -> jedis.keys(keys));
    }

    public void executeVoid(Consumer<Jedis> operation){
        execute(jedis -> {operation.accept(jedis);return Void.class;});
    }

    public <V> V execute(Function<Jedis, V> operation){
        try (Jedis jedis = pool.getResource()){
            return operation.apply(jedis);
        }
    }

}
