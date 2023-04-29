package com.centralhardware.telegram.interactiveBookBot.engine.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfiguration {


    @Bean
    public ObjectMapper getObjectMapper(){
        return new ObjectMapper();
    }

}
