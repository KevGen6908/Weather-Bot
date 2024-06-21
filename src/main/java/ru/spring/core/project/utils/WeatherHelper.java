package ru.spring.core.project.utils;

import com.github.prominence.openweathermap.api.model.weather.Weather;
import ru.spring.core.project.entity.WeatherData;

import java.util.List;

public abstract class WeatherHelper {
    public static String formatWeather(WeatherData parseWeather, String city) {
        return  city + "\n\n" + "Data : " + parseWeather.getDate() + "\n"
                + "Time: "+ parseWeather.getTime() + "\n" +
                Math.round(parseWeather.getTemperature())+"\n"
                + "Weather state: " + parseWeather.getWeatherStateMain()+"\n"
                + "Pressure: " + parseWeather.getPressure()+"\n"
                + "Humidity: " + parseWeather.getHumidity()+"\n";
    };

    public static String formatWeather(List<WeatherData> parseWeather, String city) {
        String response = "";
        for (WeatherData weatherData : parseWeather) {
            response += formatWeather(weatherData, city);
        }
        return response;
    };
}
