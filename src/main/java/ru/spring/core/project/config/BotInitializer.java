package ru.spring.core.project.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import ru.spring.core.project.service.Bot;

@Component
public class BotInitializer {
    private final BotConfig botConfig;
    private ClassPathXmlApplicationContext context;
    @Autowired
    public BotInitializer(BotConfig botConfig, ClassPathXmlApplicationContext context) {
        this.botConfig = botConfig;
        this.context=context;
    }

    @EventListener({ContextRefreshedEvent.class})
    public void init() throws TelegramApiException {

        Bot bot = context.getBean(Bot.class);
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);                         //
        try {
            telegramBotsApi.registerBot(bot);
        } catch (TelegramApiException e) {
           // log.error("Error occurred : " + e.getMessage()); //
        }
    }
}
