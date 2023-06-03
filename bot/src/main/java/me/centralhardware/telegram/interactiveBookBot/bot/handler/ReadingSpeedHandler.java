package me.centralhardware.telegram.interactiveBookBot.bot.handler;

import me.centralhardware.telegram.interactiveBookBot.bot.PartScheduler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

/**
 * Handle callback with reading speed
 */
@Component
@RequiredArgsConstructor
public class ReadingSpeedHandler extends CallbackHandler{

    private final PartScheduler partScheduler;

    @Override
    void handle(CallbackQuery callbackQuery, String data) {
        deleteInlineKeyboard(callbackQuery);
        partScheduler.scheduleNextPart();
    }

    @Override
    boolean isAcceptable(String data) {
        return data.startsWith("readingSpeed");
    }
}
