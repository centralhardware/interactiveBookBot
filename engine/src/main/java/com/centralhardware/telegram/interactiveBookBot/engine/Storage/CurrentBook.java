package com.centralhardware.telegram.interactiveBookBot.engine.Storage;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CurrentBook extends Storage{

    private static final String BOOK_ID_KEY = "book_id";
    private static final String PART_ID_KEY = "part_id";

    public void init(String data){
        if (!data.startsWith("nextPart")) return;

        set(BOOK_ID_KEY, (UUID.fromString(data.split(":")[2])));
        set(PART_ID_KEY, Integer.valueOf(data.split(":")[1]));
    }

    public void setBookId(UUID bookId){
        set(BOOK_ID_KEY, bookId);
    }

    public void setPartId(Integer partId){
        set(PART_ID_KEY, partId);
    }

    public UUID getBookId(){
        return get(BOOK_ID_KEY);
    }

    public Integer getPartId(){
        return get(PART_ID_KEY);
    }

}
