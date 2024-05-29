package ru.spring.core.project.DBService;

import ru.spring.core.project.entity.Place;

import java.util.List;

public interface PlaceService {
    Place addPlace(Place place);
    void deleteById(Long id);
    void deleteByCityName(String cityName);
    List<Place> getAll();
    List<Place> getAllLinkedUserByChatId(Long chatId);
}
