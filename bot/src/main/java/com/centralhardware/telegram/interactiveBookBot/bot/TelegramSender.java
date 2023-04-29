package com.centralhardware.telegram.interactiveBookBot.bot;

import com.centralhardware.telegram.interactiveBookBot.engine.Engine;
import com.centralhardware.telegram.interactiveBookBot.engine.Storage.CurrentUser;
import com.centralhardware.telegram.interactiveBookBot.engine.limiter.Limiter;
import com.centralhardware.telegram.interactiveBookBot.engine.redis.Redis;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.EntryStream;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ActionType;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethodMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class TelegramSender {

    private final TelegramUtil telegramUtil;
    private final CurrentUser currentUser;
    private final Redis redis;
    private final Engine engine;
    private final Limiter  limiter;
    @Setter
    private AbsSender absSender;

    public void send(Object method){
        limiter.limit(() -> {
            try {
                telegramUtil.logSend(method);
                if (method instanceof BotApiMethodMessage botApiMethodMessage){
                    absSender.execute(botApiMethodMessage);
                } else if (method instanceof  SendPhoto sendPhoto){
                    absSender.execute(sendPhoto);
                } else if (method instanceof DeleteMessage deleteMessage){
                    absSender.execute(deleteMessage);
                } else if (method instanceof AnswerCallbackQuery answerCallbackQuery){
                    absSender.execute(answerCallbackQuery);
                }
                telegramUtil.saveStatisticOutcome(method);

            } catch (Throwable t){
                log.warn("",t);
            }
        });
    }

    public void sendAction(ActionType actionType){
        SendChatAction chatAction = SendChatAction.builder()
                .chatId(currentUser.getChatId())
                .action(actionType.toString())
                .build();
        send(chatAction);
    }

    public void sendChooseBook() {
        InlineKeyboardBuilder builder = InlineKeyboardBuilder.create()
                .setText("Выберите книгу ");

        EntryStream.of(engine.getBookList())
                .forEach(it -> builder.row().button(openEndings(it.getKey()) + it.getValue(), it.getKey().toString()).endRow());

        send(builder.build(currentUser.getChatId()));
    }

    private String openEndings(UUID bookId){
        long endings = redis.scard(redis.endingsKeyForBookFormatter.apply(bookId.toString()));
        long endingsCount = engine.getCountOfEndings(bookId);
        return String.format("(%s:%s)", endings, endingsCount);
    }

}
