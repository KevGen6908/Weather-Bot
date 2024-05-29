package ru.spring.core.project.DBService.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.spring.core.project.DBService.WeatherDataService;
import ru.spring.core.project.entity.WeatherData;
import ru.spring.core.project.repositories.WeatherDataRepository;

import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;
@Component
public class WeatherDataImpl implements WeatherDataService {
    @Autowired
    WeatherDataRepository weatherDataRepository;

    @Override
    public WeatherData addWeatherData(WeatherData weatherData){
        WeatherData savedWeatherData = weatherDataRepository.saveAndFlush(weatherData);
        return savedWeatherData;
    }

    @Override
    public void deleteWeatherDataById(Long id){
        weatherDataRepository.deleteById(id);
    }


    @Override
    public List<WeatherData> getAll(){
        return weatherDataRepository.findAll();
    }

    @Override
    public List<WeatherData>getAllByPlaceName(String placeName){
        return weatherDataRepository.findAllWeatherDataByPlace(placeName);
    }

    @Override
    public List<WeatherData> getAllWeatherDataByPlaceAndDateAfterCurrentTime(String placeName, LocalTime time, LocalDate date){
        return weatherDataRepository.findAllWeatherDataByPlaceAndDateAfterCurrentTime(placeName,time,date);
    }

    @Override
    public List<WeatherData> getAllWeatherDataByPlaceAndDateNearCurrentTime(String placeName, LocalTime time, LocalDate date ){
        return weatherDataRepository.findAllWeatherDataByPlaceAndDateNearCurrentTime(placeName, time, date);
    }
}
