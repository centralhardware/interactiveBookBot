package com.centralhardware.telegram.interactiveBookBot.engine.configuration;

import com.google.common.util.concurrent.RateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@SuppressWarnings("UnstableApiUsage")
@Configuration
public class RateLimiterConfiguration {

    @Bean
    public RateLimiter getRateLimiter(){
        return RateLimiter.create(Double.parseDouble(System.getenv("TELEGRAM_RATE_LIMIT")));
    }

}
