package ru.spring.core.project.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.spring.core.project.entity.User;
import ru.spring.core.project.entity.WeatherData;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface WeatherDataRepository extends JpaRepository<WeatherData,Long> {
    @Query(value = "SELECT wd.* " +
            "FROM WEATHER_DATA wd " +
            "JOIN PLACE p ON wd.place_id = p.id " +
            "WHERE p.place_name =  :cityName " +
            "  AND wd.date =  :currDate " +
            "  AND wd.\"time\" > (CAST(:currTime AS TIME) - INTERVAL '3 hour')ORDER BY wd.\"time\" ASC; " , nativeQuery = true)
    List<WeatherData> findAllWeatherDataByPlaceAndDateAfterCurrentTime(@Param("cityName") String cityName,
                                                @Param("currTime") LocalTime currTime,
                                                @Param("currDate") LocalDate currDate);

    @Query(value = "SELECT wd.* " +
            "FROM WEATHER_DATA wd " +
            "JOIN PLACE p ON wd.place_id = p.id " +
            "WHERE p.place_name =  :cityName " +
            "  ORDER BY wd.\"time\" ASC; " , nativeQuery = true)
    List<WeatherData> findAllWeatherDataByPlace(@Param("cityName") String cityName);



    @Query(value = "SELECT wd.* " +
            "FROM WEATHER_DATA wd " +
            "JOIN PLACE p ON wd.place_id = p.id " +
            "WHERE p.place_name = :cityName " +
            "  AND wd.date = :currDate " +
            "  AND wd.\"time\" < (CAST(:currTime AS TIME) + INTERVAL '1 hour 30 minutes') " +
            "  AND wd.\"time\" > (CAST(:currTime AS TIME) - INTERVAL '1 hour 30 minutes') " +
            "ORDER BY wd.\"time\" ASC", nativeQuery = true)
    List<WeatherData> findAllWeatherDataByPlaceAndDateNearCurrentTime(@Param("cityName") String cityName,
                                                                       @Param("currTime") LocalTime currTime,
                                                                       @Param("currDate") LocalDate currDate);
}
