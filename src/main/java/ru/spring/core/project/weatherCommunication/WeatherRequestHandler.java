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
    private String parseWeatherReturnString(Weather parseWeather, String city){
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
    public String getAnswerCityNowReturnString(String cityName){
        try {
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
        catch(Exception e){
            System.out.println("Произошло исключение: " + e);
            return "error";
        }
    };


    // этот метод оставил для теста
    public String getAnswerCityTodayReturnString(String cityName){
        try {
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
        catch(Exception e){
            System.out.println("Произошло исключение: " + e);
            return "error";
        }
    };

    //Нужные методы:
    public WeatherData getWeatherDataByCoordinatesNow(CoordinateForWeatherBot coordinateForWeatherBot)throws Exception {
        double latitude=coordinateForWeatherBot.getLatitude();
        double longitude = coordinateForWeatherBot.getLatitude();
        try {

            Coordinate myCoordinate = Coordinate.of(latitude, longitude);
            SingleResultCurrentWeatherRequestCustomizer tempOdject = openWeatherClient
                    .currentWeather()
                    .single()
                    .byCoordinate(myCoordinate);
            return getWeatherDataByTempObjectNow(tempOdject,"",latitude,longitude);
        }
        catch (Exception ex) {
            logger.error("Город не найден! {}: {}",latitude, longitude);
            throw new Exception("The place was not found!", ex);
        }
    };

    public WeatherData getWeatherDataByCityNameNow(String cityName)throws Exception {
        try {
            SingleResultCurrentWeatherRequestCustomizer tempOdject = openWeatherClient
                    .currentWeather()
                    .single()
                    .byCityName(cityName);

            WeatherData ans = getWeatherDataByTempObjectNow(tempOdject,cityName,0,0);
            ans.setTimeOfLoad(new Timestamp(System.currentTimeMillis()));
            return ans;
        }
        catch (Exception ex) {
            throw new Exception("Город не найден!", ex);
        }
    };


    private WeatherData getWeatherDataByTempObjectNow(SingleResultCurrentWeatherRequestCustomizer tempOdject,
                                                     String cityName, double latitude, double longitude)throws Exception {
        try {
            Weather currentWeather = tempOdject
                    .language(Language.RUSSIAN)
                    .unitSystem(UnitSystem.METRIC)
                    .retrieve()
                    .asJava();
            WeatherData ans = parseWeather(currentWeather, cityName, latitude, longitude);

            ans.setTimeOfLoad(new Timestamp(System.currentTimeMillis()));

            return ans;
        }
        catch (Exception ex) {
            logger.error("Город не найден! {}: {}",cityName, ex);
            throw new Exception(ex);
        }
    };

    private WeatherData parseWeather(Weather currentWeather, String cityName, double latitude, double longitude) {
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


    // если передать 0 дней, выведет прогноз на остаток сегодняшнего дня
    // 1 день - на сегодня и на завтра
    // Максимом - 5 дней. Больше не умеет
    /**
     *
     *
     *
     *
     * **/
    public ArrayList<WeatherData> getWeatherDataByCityNameNDay(String cityName, int amountDays) throws Exception {
        try {
            FiveDayThreeHourStepForecastRequestCustomizer tempObject = openWeatherClient.forecast5Day3HourStep()
                    .byCityName(cityName);
            ArrayList<WeatherData> response = getWeatherDataByTempObjectNDay(tempObject,amountDays,cityName,0,0);
            for(WeatherData wd:response){
                wd.setTimeOfLoad(new Timestamp(System.currentTimeMillis()));
            }
            return response;
        } catch (Exception ex) {
            logger.error("Город не найден! {}: {}",cityName, ex);
            throw new Exception(ex);
        }
    }



    // если передать 0 дней, выведет прогноз на остаток сегодняшнего дня
    // 1 день - на сегодня и на завтра
    // Максимом - 5 дней. Больше не умеет
    public  ArrayList<WeatherData> getWeatherDataByCoordsNDay(CoordinateForWeatherBot coordinateForWeatherBot, int amountDays) throws Exception {
        double latitude = coordinateForWeatherBot.getLatitude();
        double longitude = coordinateForWeatherBot.getLongitude();
        try {
            Coordinate myCoordinate = Coordinate.of(latitude, longitude);
            FiveDayThreeHourStepForecastRequestCustomizer tempObject= openWeatherClient.forecast5Day3HourStep()
                    .byCoordinate(myCoordinate);
            ArrayList<WeatherData> ans = getWeatherDataByTempObjectNDay(tempObject,amountDays,"",latitude,longitude);
            for(WeatherData wd:ans){
                wd.setTimeOfLoad(new Timestamp(System.currentTimeMillis()));
            }
            return ans;
        } catch (Exception ex) {
            logger.error("Город не найден! {}: {}",latitude, longitude);
            throw new Exception(ex);
        }
    };

     private ArrayList<WeatherData> getWeatherDataByTempObjectNDay(FiveDayThreeHourStepForecastRequestCustomizer tempObject,
                                                           int amountDays , String cityName, double latitude, double longitude) throws Exception {
        try {

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
                listWeatherData.add(parseForecast(listForecast.get(i),cityName,date,time,latitude,longitude));


            }

            return listWeatherData;
        } catch (Exception ex) {
            logger.error("Город не найден! {}: {}",cityName, ex);
            throw new Exception(ex);
        }
    }

    WeatherData parseForecast(WeatherForecast weatherForecast, String cityName,LocalDate date,LocalTime time,
                              double latitude, double longitude){
        WeatherData weatherData= new WeatherData();
        weatherData.setDate(date);
        weatherData.setTime(time);
        weatherData.setDayOfWeek(date.getDayOfWeek());
        weatherData.setTemperature(Math.round((float)weatherForecast.getTemperature().getValue()));
        weatherData.setHumidity((float)weatherForecast.getHumidity().getValue());
        weatherData.setPressure((float)weatherForecast.getAtmosphericPressure().getValue()); // нужно перевести в мм рт ст
        weatherData.setWindSpeed((float)weatherForecast.getWind().getSpeed());
        weatherData.setWeatherStateMain(weatherForecast.getWeatherState().getName());
        weatherData.setWeatherStateDescription(weatherForecast.getWeatherState().getDescription());
        return weatherData;
    }






    ///////////////////////////////////////////////////////////// Остануться только эти методы


}
