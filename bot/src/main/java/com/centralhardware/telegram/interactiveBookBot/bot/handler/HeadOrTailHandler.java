package com.centralhardware.telegram.interactiveBookBot.bot.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

@Component
@RequiredArgsConstructor
public class HeadOrTailHandler extends CallbackHandler{
    @Override
    void handle(CallbackQuery callbackQuery, String data) {
        AnswerCallbackQuery answerCallbackQuery = AnswerCallbackQuery
                .builder()
                .text(Math.random() > 0.5? "орел" : "решка")
                .showAlert(true)
                .callbackQueryId(callbackQuery.getId())
                .build();
        sender.send(answerCallbackQuery);
    }

    @Override
    boolean isAcceptable(String data) {
        return data.equalsIgnoreCase("head_or_tails");
    }
}
