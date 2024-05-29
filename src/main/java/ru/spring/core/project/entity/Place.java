package ru.spring.core.project.entity;

import jakarta.persistence.*;
import ru.spring.core.project.weatherCommunication.CoordinateForWeatherBot;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "PLACE")
public class Place {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(name = "PLACE_NAME")
    private String placeName;



    @OneToMany(mappedBy = "place", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<WeatherData> setOfWeatherData;

    @ManyToMany(mappedBy = "setOfPlaces")
    private Set<User> setOfUser;

    @Embedded
    CoordinateForWeatherBot coordinate;

    public Set<User> getSetOfUser() {
        return setOfUser;
    }
    public Set<WeatherData> getSetOfWeatherData() { return setOfWeatherData; }

    public void setSetOfWeatherData(Set<WeatherData> setOfWeatherData) { this.setOfWeatherData = setOfWeatherData; }

    public void setSetOfUser(Set<User> setOfUser) {
        this.setOfUser = setOfUser;
    }
    public long getId() {
        return id;
    }

    public String getPlaceName() {
        return placeName;
    }


    public CoordinateForWeatherBot getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(CoordinateForWeatherBot coordinate) {
        this.coordinate = coordinate;
    }


    public void setId(long id) {
        this.id = id;
    }



    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }


    public Place(){

    }
    public Place(String placeName){
        this.placeName=placeName;
    }
    public Place(CoordinateForWeatherBot coordinateForWeatherBot){
        this.coordinate=coordinateForWeatherBot;
    }

    @Override
    public String toString() {
        return "Place{" +
                "id=" + id +
                ", placeName='" + placeName + '\'' +
                ", setOfWeatherData=" + setOfWeatherData +
                ", setOfUser=" + setOfUser +
                ", coordinate=" + coordinate +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Place place = (Place) o;
        return id == place.id && Objects.equals(placeName, place.placeName) && Objects.equals(setOfWeatherData, place.setOfWeatherData) && Objects.equals(setOfUser, place.setOfUser) && Objects.equals(coordinate, place.coordinate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, placeName, setOfWeatherData, setOfUser, coordinate);
    }


    public void addWeatherData(List<WeatherData> listOfWeatherData){
        if(setOfWeatherData==null)
            setOfWeatherData=new HashSet<>();
        setOfWeatherData.addAll(listOfWeatherData);
    }
    public void addWeatherData(WeatherData weatherData){
        if(setOfWeatherData==null)
            setOfWeatherData=new HashSet<>();
        setOfWeatherData.add(weatherData);
    }
}