package com.centralhardware.telegram.interactiveBookBot.bot.handler;

import com.centralhardware.telegram.interactiveBookBot.engine.Engine;
import com.centralhardware.telegram.interactiveBookBot.engine.Storage.CurrentBook;
import com.centralhardware.telegram.interactiveBookBot.engine.Storage.CurrentUser;
import com.centralhardware.telegram.interactiveBookBot.engine.redis.Redis;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

@Component
@RequiredArgsConstructor
public class BookRatingHandler extends CallbackHandler{

    private final Redis redis;
    private final Engine engine;
    private final CurrentUser currentUser;
    private final CurrentBook currentBook;

    @Override
    void handle(CallbackQuery callbackQuery, String data) {
        deleteInlineKeyboard(callbackQuery);
        redis.put(redis.bookRatingFormatter.get(), Integer.parseInt(data.split(":")[2]));
        var message = SendMessage.builder()
                .text(String.format("Вы поставили %s балов книге %s",
                        data.split(":")[2],
                        engine.getBookName(currentBook.getBookId())))
                .chatId(currentUser.getChatId())
                .build();
        sender.send(message);
        sender.sendChooseBook();
    }

    @Override
    boolean isAcceptable(String data) {
        return data.startsWith("bookRating");
    }
}
