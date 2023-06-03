package me.centralhardware.telegram.interactiveBookBot.bot;

import me.centralhardware.telegram.interactiveBookBot.engine.Engine;
import me.centralhardware.telegram.interactiveBookBot.engine.Model.Next;
import me.centralhardware.telegram.interactiveBookBot.engine.ReadingUtil;
import me.centralhardware.telegram.interactiveBookBot.engine.Storage.CurrentBook;
import me.centralhardware.telegram.interactiveBookBot.engine.Storage.CurrentUser;
import me.centralhardware.telegram.interactiveBookBot.engine.redis.Redis;
import com.google.common.collect.ArrayListMultimap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.IntStreamEx;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ActionType;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Schedule book part paragraph sending.
 * For already opened part, send all paragraphs immediately.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PartScheduler {

    private final Engine engine;
    private final CurrentUser currentUser;
    private final CurrentBook currentBook;
    private final TelegramSender sender;
    private final ReadingUtil readingUtil;
    private final Redis redis;

    /**
     * Create {@link Timer} instance per user.
     */
    private final Map<Long, Timer> chatid2timer = new HashMap<>();
    /**
     * Store running status per user.
     */
    private final Map<Long, Boolean> chatid2running = new HashMap<>();


    /**
     * @return True, if paragraph sending in progress
     */
    public boolean isRunning(){
        return chatid2running.getOrDefault(currentUser.getChatId(), false);
    }

    /**
     * Cancel paragraphs sending.
     * Used when received /start command.
     */
    public void cancel(){
        if (!chatid2timer.containsKey(currentUser.getChatId())) return;

        chatid2timer.get(currentUser.getChatId()).cancel();
        chatid2running.put(currentUser.getChatId(), false);
    }

    public void scheduleNextPart() {
        Long chatId = currentBook.getChatId();

        boolean sync = redis.sismember(redis.completePartFormatter.get(), currentBook.getPartId());

        chatid2timer.put(currentUser.getChatId(), new Timer());

        chatid2running.put(currentUser.getChatId(), true);

        Timer timer = chatid2timer.get(currentUser.getChatId());

        AtomicInteger delay = new AtomicInteger(0);
        engine.getParagraph()
                .forEach(paragraph -> {
                    Runnable send = () -> {
                        currentBook.setChatId(chatId);

                        if (paragraph.startsWith("{")){
                            String imageName = paragraph.replace("{", "")
                                    .replace("}", "");
                            String path = imageName.split(":")[0];
                            String caption = imageName.split(":")[1];
                            SendPhoto photo = SendPhoto.builder()
                                    .chatId(currentUser.getChatId())
                                    .photo(new InputFile(engine.getFile(path, currentBook.getBookId()),caption))
                                    .caption(caption)
                                    .build();
                            sender.send(photo);
                            if (!sync) sender.sendAction(ActionType.UPLOADPHOTO);
                            return;
                        }

                        SendMessage message = SendMessage.builder()
                                .chatId(currentUser.getChatId())
                                .text(paragraph)
                                .build();
                        sender.send(message);
                        if (!sync) sender.sendAction(ActionType.TYPING);
                    };

                    if (sync){
                        send.run();
                    } else {
                        timer.schedule(createTask(send), delay.get() * 1000);
                        delay.addAndGet(readingUtil.getReadingTime(paragraph));
                    }
                });

        Runnable send = () -> {
            currentBook.setChatId(chatId);

            redis.sadd(redis.completePartFormatter.get(), currentBook.getPartId());

            Next next = engine.getNext();

            switch (next.getType()){
                case CHOOSE -> {
                    var builder = InlineKeyboardBuilder.create()
                            .setText("?");

                    next.variants()
                            .forEach(it -> {
                                if (it.evaluate() != null && it.evaluate().equalsIgnoreCase("head_or_tails")) {
                                    builder.row().button("Подбросить монетку", "head_or_tails").endRow();
                                    return;
                                }

                                builder
                                        .row()
                                        .button(it.text(), "nextPart:"+ it.processTo().toString() + ":" + currentBook.getBookId() + ":" + currentUser.getReadingSpeed() + ":" + currentBook.getPartId())
                                        .endRow();
                            });
                    sender.send(builder.build(currentUser.getChatId()));
                    chatid2running.put(currentUser.getChatId(), false);
                }
                case DIRECT -> {
                    var builder = InlineKeyboardBuilder.create()
                            .setText("?")
                            .row()
                            .button("дальше", "nextPart:"+ next.processTo() + ":" + currentBook.getBookId() + ":" + currentUser.getReadingSpeed())
                            .endRow();
                    sender.send(builder.build(currentUser.getChatId()));
                    chatid2running.put(currentUser.getChatId(), false);
                }
                case END -> {
                    SendMessage message = SendMessage.builder()
                            .chatId(currentUser.getChatId())
                            .text("Это конец. Спасибо за прохождение.")
                            .build();
                    sender.send(message);

                    redis.sadd(redis.endingsKeyFormatter.get(), engine.getCurrentPartId());

                    Long openEndingsCount = redis.scard(redis.endingsKeyFormatter.get());

                    message = SendMessage.builder()
                            .chatId(currentUser.getChatId())
                            .text(String.format("Вы открыли %s концовок из %s",
                                    openEndingsCount,
                                    engine.getCountOfEndings()))
                            .build();
                    sender.send(message);

                    sendStatistic();

                    sendRatingBook();
                    chatid2running.put(currentUser.getChatId(), false);
                }
                default -> throw new IllegalStateException();
            }
        };

        if (sync){
            send.run();
        } else {
            timer.schedule(createTask(send), delay.get() * 1000);
        }
    }

    private void sendStatistic(){
        try {
            var strings = redis.smembers(redis.bookChoseFormatter.get());

            var statistic = ArrayListMultimap.create();
            strings
                    .stream()
                    .map(it -> it.split(":"))
                    .forEach(it -> statistic.put(Integer.parseInt(it[2]), Integer.parseInt(it[1])));

            var lastChooses = redis.smembers(redis.lastBookChooses.get());
            Map<Integer, Integer> yourChose = lastChooses
                    .stream()
                    .collect(Collectors.toMap(it -> Integer.parseInt(it.split(":")[1]),
                            it -> Integer.parseInt(it.split(":")[0])));

            var res = new StringBuilder();
            res.append("Вы сделали следующие выборы").append("\n");

            yourChose
                    .forEach((k,v) -> {
                        long countYourChose = statistic.get(k).stream()
                                .filter(it -> Objects.equals(it, v))
                                .count();
                        long procent = (long) (((double)countYourChose / (statistic.get(k).size())) * 100);

                        res.append(engine.getVariant(k, v))
                                .append(" как и ").append(procent).append(" процентов пользователей ").append("\n");

                    });
            var message = SendMessage.builder()
                    .text(res.toString())
                    .chatId(currentUser.getChatId())
                    .build();
            sender.send(message);
        } catch (Throwable t){
            log.warn("", t);
        }


    }

    private void sendRatingBook(){
        if (redis.exists(redis.bookRatingFormatter.get())){
            sender.sendChooseBook();
            return;
        }

        var builder = InlineKeyboardBuilder.create()
                .setText("Оцените книгу:")
                .row();

        IntStreamEx.range(1, 6)
                .forEach(i -> builder.button(String.valueOf(i),
                        "bookRating:" + currentBook.getBookId() + ":" + i));

        builder.endRow();
        sender.send(builder.build(currentUser.getChatId()));
    }

    private TimerTask createTask(Runnable runnable){
        return new TimerTask() {
            @Override
            public void run() {
                runnable.run();
            }
        };
    }

}
