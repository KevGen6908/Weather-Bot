package ru.spring.core.project.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = {"ru.spring.core.project"})
public class BotConfig {
    @Value("${botConfig.token}")
     private String botToken;
    @Value("${botConfig.name}")
     private String botName;
    @Value("${botConfig.apiKey}")
     private String apiKey;

    public String getOpenWeatherMapKey() {
        return apiKey;
    }

    public String getBotToken() {
        return botToken;
    }

    public String getBotName() {
        return botName;
    }
}
