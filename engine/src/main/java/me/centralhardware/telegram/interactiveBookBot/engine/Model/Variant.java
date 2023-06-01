package me.centralhardware.telegram.interactiveBookBot.engine.Model;

public record Variant (
        String text,
        Integer processTo,
        String evaluate
) { }
