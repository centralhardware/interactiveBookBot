package com.centralhardware.telegram.interactiveBookBot.bot.handler;

import com.centralhardware.telegram.interactiveBookBot.bot.TelegramSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

public abstract class CallbackHandler {

    @Autowired
    protected TelegramSender sender;

    abstract void handle(CallbackQuery callbackQuery, String data);

    public void handle(CallbackQuery callbackQuery){
        handle(callbackQuery, callbackQuery.getData());
    }

    abstract boolean isAcceptable(String data);

    public boolean isAcceptable(CallbackQuery callbackQuery){
        return isAcceptable(callbackQuery.getData());
    }

    public void deleteInlineKeyboard(CallbackQuery callbackQuery){
        var deleteMessage = DeleteMessage.builder()
                .chatId(callbackQuery.getFrom().getId())
                .messageId(callbackQuery.getMessage().getMessageId())
                .build();
        sender.send(deleteMessage);
    }

}
