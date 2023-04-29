package com.centralhardware.telegram.interactiveBookBot.engine;

import com.centralhardware.telegram.interactiveBookBot.engine.Model.Book;
import com.centralhardware.telegram.interactiveBookBot.engine.Model.IndexEntry;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
@AllArgsConstructor
public class ResourcesUtil {

    public static final String BASE_PATH = "Books/";
    public static final String CONTENT_FILE_NAME = "content.json";
    public static final String INDEX_FILE_NAME = BASE_PATH + "index.json";

    private final ObjectMapper mapper;

    public String loadString(String fileName){
        try {
            return Resources.toString(Resources.getResource(fileName), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load file from resources", e);
        }
    }

    @SneakyThrows
    public InputStream getFileFrom(String fileName){
        return Resources.getResource(fileName).openStream();
    }

    public List<IndexEntry> loadIndex(){
        try {
            return List.of(mapper.readValue(loadString(INDEX_FILE_NAME), IndexEntry[].class));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse json array", e);
        }
    }

    public Book loadBook(String name){
        try {
            return mapper.readValue(loadString(BASE_PATH + name + "/" + CONTENT_FILE_NAME), Book.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse json entity", e);
        }
    }

}
