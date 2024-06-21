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
        Logger rootLogger = Logger.getLogger("");

        rootLogger.setLevel(Level.ALL);

        Handler fileHandler = new FileHandler("application.log");

        fileHandler.setLevel(Level.ALL);

        rootLogger.addHandler(fileHandler);
    }
}
