package ru.spring.core.project.BLusingBD;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import ru.spring.core.project.DBService.impl.WeatherDataImpl;
import ru.spring.core.project.entity.Place;
import ru.spring.core.project.entity.WeatherData;
import ru.spring.core.project.weatherCommunication.CoordinateForWeatherBot;
import ru.spring.core.project.weatherCommunication.WeatherRequestHandler;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class RequesterDataFromDBOrOpenWeatherMap {
    @Autowired
    private WeatherRequestHandler weatherRequestHandler;

    @Autowired
    private ApplicationContext context;

    @Autowired
    public RequesterDataFromDBOrOpenWeatherMap() {
    }

    public List<WeatherData> getWeatherDataByPlaceNDay(Place place, int amountDays) throws Exception {
        WeatherDataImpl weatherDataService = context.getBean(WeatherDataImpl.class);
        LocalDate currDate =  LocalDate.now();
        LocalTime currTime =  LocalTime.now();
        List<WeatherData> ans = new ArrayList<>();

        if(!place.getPlaceName().equals("")){
            ans = weatherDataService.getAllWeatherDataByPlaceAndDateAfterCurrentTime(place.getPlaceName(),currTime,currDate);
            if(ans.isEmpty()){
                ans = weatherRequestHandler.getWeatherDataByCityNameNDay(place.getPlaceName(), amountDays);
                for(WeatherData wd :ans){
                    if(wd.getDate().equals(currDate)) {
                        wd.setPlace(place);
                        weatherDataService.addWeatherData(wd);
                    }
                }
            }

        } else if (place.getCoordinate()!=null) {
            ans = weatherRequestHandler.getWeatherDataByCoordsNDay(place.getCoordinate(), amountDays);
        }
        else{
            throw new Exception("Name and Coordinates is empty");
        }

        return ans;
    }


    public WeatherData getWeatherDataByPlaceNow(Place place) throws Exception {
        WeatherDataImpl weatherDataService = context.getBean(WeatherDataImpl.class);
        LocalDate currDate =  LocalDate.now();
        LocalTime currTime =  LocalTime.now();
        WeatherData ans;
        List<WeatherData> currentWeatherData = new ArrayList<>();

        if(!place.getPlaceName().equals("")){
            currentWeatherData = weatherDataService.getAllWeatherDataByPlaceAndDateNearCurrentTime(place.getPlaceName(),currTime,currDate);
            if(currentWeatherData.size()>0){
                ans = currentWeatherData.get(0);
                ans.setPlace(place);
                weatherDataService.addWeatherData(ans);
            }
            else {
                ans = weatherRequestHandler.getWeatherDataByCityNameNow(place.getPlaceName());
            }

        } else if (place.getCoordinate()!=null) {
            ans = weatherRequestHandler.getWeatherDataByCoordinatesNow(place.getCoordinate());
        }
        else{
            throw new Exception("Name and Coordinates are empty!");
        }

        return ans;
    }


}

