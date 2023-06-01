package me.centralhardware.telegram.interactiveBookBot.engine.Model;

import java.util.UUID;

public record IndexEntry (
        UUID id,
        String name,
        String path,
        UUID authorId,
        String lang,
        Boolean enabled
) { }
