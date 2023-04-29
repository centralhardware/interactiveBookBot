package com.centralhardware.telegram.interactiveBookBot.engine.Model;

import java.util.UUID;

public record Author (
        UUID id,
        String name
){ }
