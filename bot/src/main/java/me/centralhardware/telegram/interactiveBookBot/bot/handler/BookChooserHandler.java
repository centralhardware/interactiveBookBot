package me.centralhardware.telegram.interactiveBookBot.bot.handler;

import me.centralhardware.telegram.interactiveBookBot.bot.InlineKeyboardBuilder;
import me.centralhardware.telegram.interactiveBookBot.engine.Engine;
import me.centralhardware.telegram.interactiveBookBot.engine.Model.ReadingSpeed;
import me.centralhardware.telegram.interactiveBookBot.engine.Storage.CurrentUser;
import me.centralhardware.telegram.interactiveBookBot.engine.redis.Redis;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import java.util.UUID;

/**
 * Handle book choose callback query
 * Callback data must contain UUID.
 */
@Component
@RequiredArgsConstructor
public class BookChooserHandler extends CallbackHandler{

    private final Engine engine;
    private final CurrentUser currentUser;
    private final Redis redis;

    @Override
    void handle(CallbackQuery callbackQuery, String data) {
        var message = SendMessage.builder()
                .chatId(currentUser.getChatId())
                .text("Вы выбрали книгу " + engine.getBookName(UUID.fromString(data)))
                .build();
        sender.send(message);
        redis.delete(redis.lastBookChooses.get());
        deleteInlineKeyboard(callbackQuery);

        sendChooseReadingSpeed(data);

    }



    private void sendChooseReadingSpeed(String data){
        var builder = InlineKeyboardBuilder.create()
                .setText("Выберите вашу скорость чтения")
                .row().button("Высокая", "readingSpeed:" + data + ":" + ReadingSpeed.HIGH.getReadingSpeeds(engine.getLang(UUID.fromString(data)))).endRow()
                .row().button("Средняя", "readingSpeed:" + data + ":" + ReadingSpeed.MIDDLE.getReadingSpeeds(engine.getLang(UUID.fromString(data)))).endRow()
                .row().button("Медленная", "readingSpeed:" + data + ":" + ReadingSpeed.SLOW.getReadingSpeeds(engine.getLang(UUID.fromString(data)))).endRow();
        sender.send(builder.build(currentUser.getChatId()));
    }

    @Override
    boolean isAcceptable(String data) {
        try {
            return engine.bookExist(UUID.fromString(data));
        } catch (IllegalArgumentException e){
            return false;
        }
    }
}
