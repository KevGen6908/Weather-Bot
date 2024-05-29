package ru.spring.core.project.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
@Configuration
public class LoggingConfig {
    public static void configure() throws IOException {
        // Получение корневого логгера
        Logger rootLogger = Logger.getLogger("");

        // Установка уровня логирования
        rootLogger.setLevel(Level.ALL);

        // Создание обработчика для записи в файл
        Handler fileHandler = new FileHandler("application.log");
        fileHandler.setLevel(Level.ALL);

        // Добавление обработчика к корневому логгеру
        rootLogger.addHandler(fileHandler);
    }
}
