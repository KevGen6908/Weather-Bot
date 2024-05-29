package ru.spring.core.project.config;

import com.github.prominence.openweathermap.api.OpenWeatherMapClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WeatherConfig {
    @Value("${botConfig.apiKey}")
    private String apiKey;

    @Bean
    public OpenWeatherMapClient openWeatherMapClient() {
        return new OpenWeatherMapClient(apiKey);
    }
}
