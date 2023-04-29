package com.centralhardware.telegram.interactiveBookBot.engine.clickhouse.model;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record LogEntry (
        LocalDateTime dateTime,
        Long chatId,
        String username,
        String firstName,
        String lastName,
        Boolean isPremium,
        String action,
        String text,
        UUID bookId,
        Integer partId,
        UUID authorId
){ }
