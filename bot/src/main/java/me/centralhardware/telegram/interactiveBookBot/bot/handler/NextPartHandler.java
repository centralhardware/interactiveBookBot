package me.centralhardware.telegram.interactiveBookBot.bot.handler;

import me.centralhardware.telegram.interactiveBookBot.bot.PartScheduler;
import me.centralhardware.telegram.interactiveBookBot.engine.Storage.CurrentBook;
import me.centralhardware.telegram.interactiveBookBot.engine.Storage.CurrentUser;
import me.centralhardware.telegram.interactiveBookBot.engine.redis.Redis;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

@Component
@RequiredArgsConstructor
public class NextPartHandler extends CallbackHandler {

    private final Redis redis;
    private final CurrentUser currentUser;
    private final CurrentBook currentBook;
    private final PartScheduler partScheduler;

    @Override
    public void handle(CallbackQuery callbackQuery, String data) {
        if (data.split(":").length == 5){
            redis.sadd(redis.bookChoseFormatter.get(), currentUser.getChatId() + ":" + currentBook.getPartId() + ":" + data.split(":")[4]);
            redis.sadd(redis.lastBookChooses.get(), currentBook.getPartId() + ":" + data.split(":")[4]);
        }

        deleteInlineKeyboard(callbackQuery);
        partScheduler.scheduleNextPart();
    }

    @Override
    public boolean isAcceptable(String data) {
        return data.startsWith("nextPart");
    }
}
