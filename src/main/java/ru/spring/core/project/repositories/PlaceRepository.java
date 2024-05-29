package ru.spring.core.project.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import ru.spring.core.project.entity.Place;
import ru.spring.core.project.entity.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface PlaceRepository extends JpaRepository<Place,Long> {
    //По аннотации @Query можем использовать напрямую SQL запрос

    @Query(value = "SELECT p.* FROM PLACE p " +
            "JOIN USER_PLACE up ON p.id = up.PLACE_ID " +
            "JOIN USERS u ON up.USER_ID = u.id " +
            "WHERE u.chat_id = :userId", nativeQuery = true)
    List<Place> findAllPlacesByChatId(@Param("userId") Long userId);

    @Query(value = "SELECT * FROM PLACE WHERE place_name = :placeName", nativeQuery = true)
    List<Place> findAllPlacesByPlaceName(@Param("placeName") String placeName);

    @Query(value = "DELETE FROM Place WHERE place_name = :placeName", nativeQuery = true)
    void deleteByPlaceName(@Param("placeName") String placeName);

}
