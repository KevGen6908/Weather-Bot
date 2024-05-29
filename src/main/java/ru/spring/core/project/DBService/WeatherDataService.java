package ru.spring.core.project.DBService;

import ru.spring.core.project.entity.WeatherData;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface WeatherDataService {
    WeatherData addWeatherData(WeatherData weatherData);
    void deleteWeatherDataById(Long id);
    List<WeatherData>getAll();
    List<WeatherData>getAllByPlaceName(String placeName);
    List<WeatherData> getAllWeatherDataByPlaceAndDateAfterCurrentTime(String placeName, LocalTime time, LocalDate date );
    List<WeatherData> getAllWeatherDataByPlaceAndDateNearCurrentTime(String placeName, LocalTime time, LocalDate date );

}
