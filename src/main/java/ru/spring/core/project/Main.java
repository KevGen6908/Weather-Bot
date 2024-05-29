package ru.spring.core.project;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.spring.core.project.config.BotInitializer;

public class Main {
    public static void main(String[] args) throws TelegramApiException {
        Logger logger = LoggerFactory.getLogger(Main.class);
        logger.info("This is an info message.");
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("META-INF/applicationContext.xml");
        Test.test(context);
        context.getBean(BotInitializer.class).init();


    }
}