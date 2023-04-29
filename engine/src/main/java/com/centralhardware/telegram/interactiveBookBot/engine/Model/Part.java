package com.centralhardware.telegram.interactiveBookBot.engine.Model;

import java.util.List;

public record Part (
        Integer number,
        List<String> paragraphs,
        Next next
) { }
