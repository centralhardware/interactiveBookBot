package me.centralhardware.telegram.interactiveBookBot.bot.handler;

import me.centralhardware.telegram.interactiveBookBot.bot.TelegramSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

/**
 * Base class for callback hanlder
 */
public abstract class CallbackHandler {

    @Autowired
    protected TelegramSender sender;

    abstract void handle(CallbackQuery callbackQuery, String data);

    /**
     * Process incoming callbackQuery
     */
    public void handle(CallbackQuery callbackQuery){
        handle(callbackQuery, callbackQuery.getData());
    }

    abstract boolean isAcceptable(String data);

    /**
     * @return True, if giving callbackQuery can be processed by this handler
     */
    public boolean isAcceptable(CallbackQuery callbackQuery){
        return isAcceptable(callbackQuery.getData());
    }

    /**
     * Delete message with inline keyboard from giving callback
     */
    public void deleteInlineKeyboard(CallbackQuery callbackQuery){
        var deleteMessage = DeleteMessage.builder()
                .chatId(callbackQuery.getFrom().getId())
                .messageId(callbackQuery.getMessage().getMessageId())
                .build();
        sender.send(deleteMessage);
    }

}
