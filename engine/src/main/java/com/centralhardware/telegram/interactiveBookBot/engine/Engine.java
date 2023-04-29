package com.centralhardware.telegram.interactiveBookBot.engine;

import com.centralhardware.telegram.interactiveBookBot.engine.Model.*;
import com.centralhardware.telegram.interactiveBookBot.engine.Storage.CurrentBook;
import com.centralhardware.telegram.interactiveBookBot.engine.redis.Redis;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class Engine {
    private Map<UUID, Book> books;
    private List<IndexEntry> index;
    private Map<UUID, Long> countOfEndings;

    private final ResourcesUtil resourcesUtil;
    private final CurrentBook currentBook;
    private final Redis redis;

    @PostConstruct
    public void init(){
        index = resourcesUtil.loadIndex()
                .stream()
                .filter(IndexEntry::enabled)
                .map(it -> {
                    log.info("Loaded book {}", it.name());
                    return it;
                })
                .collect(Collectors.toList());
        books = StreamEx.of(index)
                .collect(Collectors.toMap(IndexEntry::id,
                        it -> resourcesUtil.loadBook(it.path())));
        countOfEndings = books.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        it -> it
                                .getValue()
                                .parts()
                                .stream()
                                .filter(part -> part.next().isEnding())
                                .count()));

        validateBooks();
    }

    private void validateBooks(){
        books.forEach((id, book) -> {
            AtomicInteger prevPart = new AtomicInteger(book.parts().get(0).number() - 1);
            book.parts().forEach(part -> {
                if (part.number() - prevPart.get() > 1){
                    throw new RuntimeException("Wrong content.json " + id + " part id " + part.number() + " prevPart " + prevPart.get());
                }
                prevPart.set(part.number());
            });
        });
    }


    public boolean bookExist(UUID bookId){
        return StreamEx.of(index)
                .filterBy(IndexEntry::id, bookId)
                .findAny()
                .isPresent();
    }

    public Map<UUID, String> getBookList(){
        return StreamEx.of(index)
                .collect(Collectors.toMap(IndexEntry::id,
                        IndexEntry::name));
    }

    public String getBookName(UUID id){
        return StreamEx.of(index)
                .filterBy(IndexEntry::id, id)
                .findFirst()
                .map(IndexEntry::name)
                .orElse(null);
    }

    public String getLang(UUID bookId){
        return StreamEx.of(index)
                .filterBy(IndexEntry::id, bookId)
                .findFirst()
                .map(IndexEntry::lang)
                .orElse(null);
    }

    public Integer getFirstPartNumber(){
        return StreamEx.of(getBook(currentBook.getBookId()).parts())
                .min(Comparator.comparing(Part::number))
                .map(Part::number)
                .orElse(null);
    }

    public List<String> getParagraph(){
        return StreamEx.of(getBook(currentBook.getBookId()).parts())
                .filterBy(Part::number, currentBook.getPartId())
                .findFirst()
                .map(Part::paragraphs)
                .orElse(null);
    }

    public InputStream getFile(String name, UUID bookId){
        String path = StreamEx.of(index)
                .filterBy(IndexEntry::id, bookId)
                .findFirst()
                .map(IndexEntry::path)
                .orElse(null);

        if (path == null) return null;

        return resourcesUtil.getFileFrom(ResourcesUtil.BASE_PATH  + path + "/" +  name);
    }

    public Next getNext(){
        return StreamEx.of(getBook(currentBook.getBookId()).parts())
                .filterBy(Part::number, currentBook.getPartId())
                .findFirst()
                .map(Part::next)
                .orElse(null);
    }

    public Integer getCurrentPartId(){
        return StreamEx.of(getBook(currentBook.getBookId()).parts())
                .filterBy(Part::number, currentBook.getPartId())
                .findFirst()
                .map(Part::number)
                .orElse(null);
    }

    public Long getCountOfEndings(){
        return getCountOfEndings(currentBook.getBookId());
    }

    public Long getCountOfEndings(UUID bookId){
        return countOfEndings.get(bookId);
    }

    public UUID getAuthorId(){
        return StreamEx.of(index)
                .filterBy(IndexEntry::id, currentBook.getBookId())
                .findFirst()
                .map(IndexEntry::authorId)
                .orElse(null);
    }

    public String getVariant(Integer partId, Integer nextPartId){
        return StreamEx.of(getBook(currentBook.getBookId()).parts())
                .filterBy(Part::number, partId)
                .findFirst()
                .map(Part::next)
                .map(Next::variants)
                .stream()
                .flatMap(List::stream)
                .filter(it -> Objects.equals(it.processTo(), nextPartId))
                .findFirst()
                .map(Variant::text)
                .orElse(null);
    }

    private Book getBook(UUID bookId){
        return books.get(bookId);
    }




}
