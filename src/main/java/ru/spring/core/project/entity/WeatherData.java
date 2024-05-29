package ru.spring.core.project.entity;

import jakarta.persistence.*;

import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;
// объектами этого класса будем передавать данные между классами

@Entity
@Table(name = "WEATHER_DATA")
public class WeatherData {
/*
    double latitude; // удалить
    double longitude; //удалить
    String cityName; // надо будет удалить
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public void setCityName(String cityName) {
        this.cityName = cityName;
    }
    public String getCityName() {
        return cityName;
    }
*/


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "PLACE_ID", nullable = false)
    private Place place;

    @Column(name = "TIME_OF_LOAD")
    private Timestamp timeOfLoad;

    @Column(name = "TIME")
    private LocalTime time;

    @Column(name = "DATE")
    private LocalDate date;

    @Column(name = "DAY_OF_WEEK")
    DayOfWeek dayOfWeek;

    @Column(name = "TEMPERATURE")
    private int temperature;

    @Column(name = "WIND_SPEED")
    private float windSpeed;

    @Column(name = "HUMIDITY")
    private float humidity;

    @Column(name = "PRESSURE")
    private float pressure;

    @Column(name = "WEATHER_STATE_MAIN")
    private String weatherStateMain;

    @Column(name = "WEATHER_STATE_DESCRIPTION")
    private String weatherStateDescription;


    public void setId(Long id) {
        this.id = id;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }

    public void setTemperature(int temperature) {
        this.temperature = temperature;
    }

    public void setWindSpeed(float windSpeed) {
        this.windSpeed = windSpeed;
    }

    public void setHumidity(float humidity) {
        this.humidity = humidity;
    }

    public void setPressure(float pressure) {
        this.pressure = pressure;
    }

    public void setWeatherStateMain(String weatherStateMain) {
        this.weatherStateMain = weatherStateMain;
    }

    public void setDayOfWeek(DayOfWeek dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public void setTimeOfLoad(Timestamp timeOfLoad) {
        this.timeOfLoad = timeOfLoad;
    }

    public void setPlace(Place place) {
        this.place = place;
    }

    public Long getId() {
        return id;
    }

    public Place getPlace() {
        return place;
    }

    public Timestamp getTimeOfLoad() {
        return timeOfLoad;
    }

    public LocalTime getTime() {
        return time;
    }

    public LocalDate getDate() {
        return date;
    }

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    public int getTemperature() {
        return temperature;
    }

    public float getWindSpeed() {
        return windSpeed;
    }

    public float getHumidity() {
        return humidity;
    }

    public float getPressure() {
        return pressure;
    }

    public String getWeatherStateMain() {
        return weatherStateMain;
    }

    public String getWeatherStateDescription() {
        return weatherStateDescription;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WeatherData that = (WeatherData) o;
        return  temperature == that.temperature && Float.compare(windSpeed, that.windSpeed) == 0 && Float.compare(humidity, that.humidity) == 0 && Float.compare(pressure, that.pressure) == 0 && Objects.equals(id, that.id) && Objects.equals(timeOfLoad, that.timeOfLoad) && Objects.equals(time, that.time) && Objects.equals(date, that.date) && Objects.equals(dayOfWeek, that.dayOfWeek) && Objects.equals(weatherStateMain, that.weatherStateMain) && Objects.equals(weatherStateDescription, that.weatherStateDescription);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, timeOfLoad, time, date, dayOfWeek, temperature, windSpeed, humidity, pressure, weatherStateMain, weatherStateDescription);
    }

    @Override
    public String toString() {
        return "WeatherData{" +
                "id=" + id +
                ", timeOfLoad=" + timeOfLoad +
                ", time=" + time +
                ", date=" + date +
                ", day='" + dayOfWeek + '\'' +
                ", temperature=" + temperature +
                ", windSpeed=" + windSpeed +
                ", humidity=" + humidity +
                ", pressure=" + pressure +
                ", weatherStateMain='" + weatherStateMain + '\'' +
                ", weatherStateDescription='" + weatherStateDescription + '\'' +
                '}';
    }

    public void setWeatherStateDescription(String weatherStateDescription) {
        this.weatherStateDescription = weatherStateDescription;
    }
    public String weatherResponse() {
        String response =
                "\n" + "Time of load: " + timeOfLoad+
                 "\n" + "Date: " + date
                + "\n" + "Time: " + time
                + "\n" + "Day: " + dayOfWeek
                + "\n" + "Temperature: " + temperature
                + "\n" + "Wind speed: " + windSpeed
                + "\n" + "Humidity: " + humidity + "%"
                + "\n" + "Pressure: " + pressure
                + "\n" + "Weather conditions: " + weatherStateMain
                + "\n" + "Description of the weather: " + weatherStateDescription;
        return response;
    }




    public WeatherData(){

    };



}