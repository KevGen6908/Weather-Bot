package ru.spring.core.project.weatherCommunication;

import java.util.Objects;
import jakarta.persistence.Embeddable;
@Embeddable
public class CoordinateForWeatherBot {                        //Название?
    private double latitude; // удалить
    private double longitude;
    public CoordinateForWeatherBot(){

    }
    public CoordinateForWeatherBot(double latitude, double longitude){
        this.latitude=latitude;
        this.longitude=longitude;
    }
    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    @Override
    public String toString() {
        return "Coordinate{" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CoordinateForWeatherBot that = (CoordinateForWeatherBot) o;
        return Double.compare(latitude, that.latitude) == 0 && Double.compare(longitude, that.longitude) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(latitude, longitude);
    }
}
