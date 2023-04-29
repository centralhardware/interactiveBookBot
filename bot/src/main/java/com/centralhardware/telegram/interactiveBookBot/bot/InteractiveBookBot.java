package com.centralhardware.telegram.interactiveBookBot.bot;

import com.centralhardware.telegram.interactiveBookBot.bot.handler.CallbackHandler;
import com.centralhardware.telegram.interactiveBookBot.engine.Storage.CurrentUser;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class InteractiveBookBot extends TelegramLongPollingBot {

    private final TelegramBotInterceptor telegramBotInterceptor;
    private final TelegramSender sender;
    private final List<CallbackHandler> callbackHandlers;
    private final CurrentUser currentUser;

    @PostConstruct
    public void init(){
        sender.setAbsSender(this);
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            telegramBotInterceptor.process(update);

            if (sendStartMessage(update)) return;

            processCallback(update);
        } catch (Throwable t){
            log.warn("Error while processing update",t);
        }
    }

    private void processCallback(Update update) {
        if (!update.hasCallbackQuery()) return;

        var callbackQuery = update.getCallbackQuery();

        StreamEx.of(callbackHandlers)
                .filter(it -> it.isAcceptable(callbackQuery))
                .forEach(it -> it.handle(callbackQuery));
    }

    public boolean sendStartMessage(Update update) {
        if (!update.hasMessage() || !update.getMessage().getText().equals("/start")) return false;

        var message = SendMessage.builder()
                .chatId(currentUser.getChatId())
                .text("""
                        Данный бот позволяет проходить интерактивные книги (книги-игры)
                        Чтобы вернуться к выбору книги введите /start
                        Автор: @centralhardware
                        """)
                .build();

        sender.send(message);

        sender.sendChooseBook();

        return true;
    }

    @Getter
    private final String botUsername = System.getenv("BOT_USERNAME");

    @Getter
    private final String botToken = System.getenv("BOT_TOKEN");

}
