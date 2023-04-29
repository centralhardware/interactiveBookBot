package com.centralhardware.telegram.interactiveBookBot.bot;

import com.centralhardware.telegram.interactiveBookBot.engine.Engine;
import com.centralhardware.telegram.interactiveBookBot.engine.Storage.CurrentBook;
import com.centralhardware.telegram.interactiveBookBot.engine.Storage.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TelegramBotInterceptor {

    private final TelegramUtil telegramUtil;
    private final CurrentUser currentUser;
    private final CurrentBook currentBook;
    private final Engine engine;
    private final PartScheduler partScheduler;

    public void process(Update update)  {
        currentUser.setChatId(telegramUtil.getUserId(update));

        telegramUtil.logUpdate(update);
        currentUser.init(telegramUtil.getFrom(update).getUserName(),
                telegramUtil.getFrom(update).getFirstName(),
                telegramUtil.getFrom(update).getLastName(),
                telegramUtil.getFrom(update).getIsPremium());

        String text = Optional.of(update)
                .map(Update::getMessage)
                .map(Message::getText)
                .orElse(null);
        if (Objects.equals(text, "/start")){
            partScheduler.cancel();
        }

        if (partScheduler.isRunning()) {
            throw new RuntimeException();
        }

        initStorage(update);

        telegramUtil.saveStatisticIncome(update);

    }


    private void initStorage(Update update){

        String data = Optional.ofNullable(update)
                .map(Update::getCallbackQuery)
                .map(CallbackQuery::getData)
                .orElse(null);

        if (data == null) return;

        if (data.startsWith("nextPart")){
            currentBook.init(data);
            currentUser.setReadingSpeed(Integer.valueOf(data.split(":")[3]));
        } else if (data.startsWith("readingSpeed")){
            currentBook.setBookId(UUID.fromString(data.split(":")[1]));
            currentBook.setPartId(engine.getFirstPartNumber());
            currentUser.setReadingSpeed(Integer.valueOf(data.split(":")[2]));
        } else if (data.startsWith("bookRating")) {
            currentBook.setBookId(UUID.fromString(data.split(":")[1]));
        }
    }

}
