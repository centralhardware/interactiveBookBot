package me.centralhardware.telegram.interactiveBookBot.engine.Storage;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class Storage {


    private static final Map<Long, Map<String, Object>> storage =
            new HashMap<>();
    private static final ThreadLocal<Long> chatId = new ThreadLocal<>();


    @SuppressWarnings("unchecked")
    protected  <T> T get(String key){
        return (T) storage.get(chatId.get()).get(key);
    }

    protected  <T> void set(String key, T value){
        log.info("Set value {} for key {} for user {}", value, key, chatId.get());
        storage.get(chatId.get()).put(key, value);
    }

    public void setChatId(Long chatId){
        log.info("Set value {} for key chatId", chatId);

        Storage.chatId.set(chatId);
        if (!storage.containsKey(chatId)){
            storage.put(chatId, new HashMap<>());
        }
    }

    public Long getChatId(){
        return chatId.get();
    }

}
