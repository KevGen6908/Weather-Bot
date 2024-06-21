package ru.spring.core.project.weatherCommunication;

import com.github.prominence.openweathermap.api.OpenWeatherMapClient;
import com.github.prominence.openweathermap.api.enums.Language;
import com.github.prominence.openweathermap.api.enums.UnitSystem;
import com.github.prominence.openweathermap.api.model.Coordinate;
import com.github.prominence.openweathermap.api.model.forecast.WeatherForecast;
import com.github.prominence.openweathermap.api.model.weather.Weather;
import com.github.prominence.openweathermap.api.request.forecast.free.FiveDayThreeHourStepForecastRequestCustomizer;
import com.github.prominence.openweathermap.api.request.weather.single.SingleResultCurrentWeatherRequestCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.spring.core.project.config.BotConfig;
import ru.spring.core.project.entity.WeatherData;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
public class WeatherRequestHandler {
    OpenWeatherMapClient openWeatherClient;
    private BotConfig config;
    private static final Logger logger = LoggerFactory.getLogger(WeatherRequestHandler.class);

    @Autowired
    public WeatherRequestHandler(BotConfig configuration){
        config = configuration;
        openWeatherClient = new OpenWeatherMapClient(config.getOpenWeatherMapKey());
    }


    // этот метод оставил для теста
    public static String parseWeatherReturnString(Weather parseWeather, String city) {
        return  city + "\n\n" +"Now:\n"+
                Math.round(parseWeather.getTemperature().getValue())+"\n"
                +parseWeather.getWeatherState()+"\n"
                +parseWeather.getAtmosphericPressure()+"\n"
                +parseWeather.getHumidity()+"\n";
    };
    // этот метод оставил для теста
    String parseForecastReturnString(WeatherForecast parseWeather){
        return parseWeather.getForecastTimeISO()+"\n"
                +Math.round(parseWeather.getTemperature().getValue())+"\n"
                +parseWeather.getWeatherState()+"\n"
                +parseWeather.getAtmosphericPressure()+"\n"
                +parseWeather.getHumidity()+"\n";
    };

    // этот метод оставил для теста
    public String getAnswerCityNowReturnString(String cityName) {
            Weather currentWeather = openWeatherClient
                    .currentWeather()
                    .single()
                    .byCityName(cityName)
                    .language(Language.ENGLISH)
                    .unitSystem(UnitSystem.METRIC)
                    .retrieve()
                    .asJava();
            String response = parseWeatherReturnString(currentWeather, cityName);
            return response;
    }


    // этот метод оставил для теста
    public String getAnswerCityTodayReturnString(String cityName) {
            List<WeatherForecast> listForecast = openWeatherClient.forecast5Day3HourStep()
                    .byCityName(cityName).language(Language.RUSSIAN)
                    .unitSystem(UnitSystem.METRIC)
                    .count(8)
                    .retrieve().asJava().getWeatherForecasts();
            String ans = "Сегодня: \n";
            for (int i = 0; i < listForecast.size(); i++) {
                LocalTime now = LocalTime.now();
                if (listForecast.get(i).getForecastTime().toLocalTime().isAfter(now)) {
                    break;
                }
                ans += listForecast.get(i).getForecastTime().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")) + " ";
                ans += parseForecastReturnString(listForecast.get(i));
            }
            //String ans=ParseWeather(listForecast.get(i).getWeatherState());
            return ans;
    }

    public WeatherData getWeatherDataByCoordinatesNow(CoordinateForWeatherBot coordinateForWeatherBot) {
        double latitude=coordinateForWeatherBot.getLatitude();
        double longitude = coordinateForWeatherBot.getLatitude();

            Coordinate myCoordinate = Coordinate.of(latitude, longitude);
            SingleResultCurrentWeatherRequestCustomizer tempOdject = openWeatherClient
                    .currentWeather()
                    .single()
                    .byCoordinate(myCoordinate);
            return getWeatherDataByTempObjectNow(tempOdject,"",latitude,longitude);
    };

    public WeatherData getWeatherDataByCityNameNow(String cityName) {

            SingleResultCurrentWeatherRequestCustomizer tempOdject = openWeatherClient
                    .currentWeather()
                    .single()
                    .byCityName(cityName);

            WeatherData ans = getWeatherDataByTempObjectNow(tempOdject,cityName,0,0);
            ans.setTimeOfLoad(new Timestamp(System.currentTimeMillis()));
            return ans;

    };

    private WeatherData getWeatherDataByTempObjectNow(SingleResultCurrentWeatherRequestCustomizer tempOdject,
                                                     String cityName, double latitude, double longitude) {

            Weather currentWeather = tempOdject
                    .language(Language.RUSSIAN)
                    .unitSystem(UnitSystem.METRIC)
                    .retrieve()
                    .asJava();
            WeatherData ans = parseWeather(currentWeather);

            ans.setTimeOfLoad(new Timestamp(System.currentTimeMillis()));

            return ans;

    };

    private WeatherData parseWeather(Weather currentWeather) {
        WeatherData weatherData= new WeatherData();
        weatherData.setDate(LocalDate.now());
        weatherData.setTime(LocalTime.now());
        weatherData.setDayOfWeek(LocalDate.now().getDayOfWeek());
        weatherData.setTemperature(Math.round((float)currentWeather.getTemperature().getValue()));
        weatherData.setHumidity((float)currentWeather.getHumidity().getValue());
        weatherData.setPressure((float)currentWeather.getAtmosphericPressure().getValue());
        weatherData.setWindSpeed((float)currentWeather.getWind().getSpeed());
        weatherData.setWeatherStateMain(currentWeather.getWeatherState().getName());
        weatherData.setWeatherStateDescription(currentWeather.getWeatherState().getDescription());

        return weatherData;
    }

    public List<WeatherData> getWeatherDataByCityNameNDay(String cityName, int amountDays) {
            FiveDayThreeHourStepForecastRequestCustomizer tempObject = openWeatherClient.forecast5Day3HourStep()
                    .byCityName(cityName);
            ArrayList<WeatherData> response = getWeatherDataByTempObjectNDay(tempObject,amountDays,cityName,0,0);
            for(WeatherData wd:response){
                wd.setTimeOfLoad(new Timestamp(System.currentTimeMillis()));
            }
            return response;
    }

    public  ArrayList<WeatherData> getWeatherDataByCoordsNDay(CoordinateForWeatherBot coordinateForWeatherBot, int amountDays) {
        double latitude = coordinateForWeatherBot.getLatitude();
        double longitude = coordinateForWeatherBot.getLongitude();
            Coordinate myCoordinate = Coordinate.of(latitude, longitude);
            FiveDayThreeHourStepForecastRequestCustomizer tempObject= openWeatherClient.forecast5Day3HourStep()
                    .byCoordinate(myCoordinate);
            ArrayList<WeatherData> ans = getWeatherDataByTempObjectNDay(tempObject,amountDays,"",latitude,longitude);
            for(WeatherData wd:ans){
                wd.setTimeOfLoad(new Timestamp(System.currentTimeMillis()));
            }
            return ans;
    }

     private ArrayList<WeatherData> getWeatherDataByTempObjectNDay(FiveDayThreeHourStepForecastRequestCustomizer tempObject,
                                                           int amountDays , String cityName, double latitude, double longitude) {

            List<WeatherForecast> listForecast = tempObject.language(Language.RUSSIAN)
                    .unitSystem(UnitSystem.METRIC)
                    .count(40)
                    .retrieve().asJava().getWeatherForecasts();


            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDate currentDate = LocalDate.now();
            ArrayList<WeatherData> listWeatherData = new ArrayList<>();

            for (int i = 0; i < listForecast.size(); i++) {
                LocalDateTime localDateTime = LocalDateTime.parse(listForecast.get(i).getForecastTimeISO(), formatter);
                LocalDate date = localDateTime.toLocalDate();
                LocalTime time = localDateTime.toLocalTime();

                if (!date.isBefore(currentDate.plusDays(amountDays+1))) {
                    break;
                }
                listWeatherData.add(parseForecast(listForecast.get(i), date, time));


            }

            return listWeatherData;
    }

    WeatherData parseForecast(WeatherForecast weatherForecast, LocalDate date, LocalTime time){
        WeatherData weatherData= new WeatherData();
        weatherData.setDate(date);
        weatherData.setTime(time);
        weatherData.setDayOfWeek(date.getDayOfWeek());
        weatherData.setTemperature(Math.round((float)weatherForecast.getTemperature().getValue()));
        weatherData.setHumidity((float)weatherForecast.getHumidity().getValue());
        weatherData.setPressure((float)weatherForecast.getAtmosphericPressure().getValue());
        weatherData.setWindSpeed((float)weatherForecast.getWind().getSpeed());
        weatherData.setWeatherStateMain(weatherForecast.getWeatherState().getName());
        weatherData.setWeatherStateDescription(weatherForecast.getWeatherState().getDescription());
        return weatherData;
    }

}
