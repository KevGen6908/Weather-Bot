package ru.spring.core.project;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Main {
    public static void main(String[] args) {
        Logger logger = LoggerFactory.getLogger(Main.class);
        logger.info("This is an info message.");
        new ClassPathXmlApplicationContext("META-INF/applicationContext.xml");
    }
}
