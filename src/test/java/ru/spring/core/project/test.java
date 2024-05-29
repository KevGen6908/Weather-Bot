package ru.spring.core.project;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import ru.spring.core.project.BLusingBD.RequesterDataFromDBOrOpenWeatherMap;
import ru.spring.core.project.DBService.impl.PlaceServiceImpl;
import ru.spring.core.project.DBService.impl.UserServiceImpl;
import ru.spring.core.project.DBService.impl.WeatherDataImpl;
import ru.spring.core.project.entity.Place;
import ru.spring.core.project.entity.User;
import ru.spring.core.project.entity.WeatherData;
import ru.spring.core.project.repositories.PlaceRepository;
import ru.spring.core.project.repositories.UserRepository;
import ru.spring.core.project.repositories.WeatherDataRepository;
import ru.spring.core.project.weatherCommunication.CoordinateForWeatherBot;
import ru.spring.core.project.weatherCommunication.WeatherRequestHandler;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class test {
    @Test
    public void testUsingRepositories() throws Exception{
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("META-INF/applicationContext.xml");
        PlaceRepository placeRepository = context.getBean(PlaceRepository.class);
        UserRepository userRepository = context.getBean(UserRepository.class);
        WeatherDataRepository weatherDataRepository = context.getBean(WeatherDataRepository.class);

        EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("jpaData");
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        WeatherRequestHandler weatherRequestHandler = context.getBean(WeatherRequestHandler.class);


        User userKevGen = new User();
        userKevGen.setUserName("KevGen");
        userRepository.save(userKevGen);
        User userCherep = new User();
        userCherep.setUserName("Cherep");
        userRepository.save(userCherep);


        Place placeSPB =new Place();
        placeSPB.setPlaceName("Санкт-Петербург");
        placeSPB.setCoordinate(new CoordinateForWeatherBot(123,12));
        placeRepository.save(placeSPB);


        Place placeChelyabinsk =new Place();
        placeChelyabinsk.setPlaceName("Челябинск");
        placeRepository.save(placeChelyabinsk);



        Place placeMoscow =new Place();
        placeMoscow.setPlaceName("Москва");
        placeRepository.save(placeMoscow);





        Set<Place> setOfPlaces = new HashSet<>();
        setOfPlaces.add(placeSPB);
        setOfPlaces.add(placeChelyabinsk);
        setOfPlaces.add(placeMoscow);
        userKevGen.setSetOfPlaces(setOfPlaces);
        userCherep.setSetOfPlaces(setOfPlaces);



        userRepository.save(userKevGen);
        userRepository.save(userCherep);
        entityManager.close();



        Set<WeatherData> weatherChelyabinsk = new HashSet<>(weatherRequestHandler.getWeatherDataByCityNameNDay("Челябинск", 1));
        Set<WeatherData> weatherSPB = new HashSet<>(weatherRequestHandler.getWeatherDataByCityNameNDay("Санкт-Петербург", 1));
        Set<WeatherData> weatherMoskow = new HashSet<>(weatherRequestHandler.getWeatherDataByCityNameNDay("Москва", 1));
        for(WeatherData wd: weatherChelyabinsk ){
            wd.setPlace(placeChelyabinsk);
            weatherDataRepository.save(wd);

        }
        for(WeatherData wd: weatherMoskow ){
            wd.setPlace(placeMoscow);
            weatherDataRepository.save(wd);
        }
        for(WeatherData wd: weatherSPB ){
            wd.setPlace(placeSPB);
            weatherDataRepository.save(wd);

        }
        LocalDate date = LocalDate.of(2024, 5, 25);
        LocalTime time =LocalTime.of(14, 30, 0);
        List<WeatherData> lwd = weatherDataRepository.findAllWeatherDataByPlaceAndDateAfterCurrentTime("Челябинск",time,date);
        for(WeatherData wd :lwd){
            System.out.println(wd.toString());
        }

        List<Place> cits = placeRepository.findAllPlacesByPlaceName("Челябинск");


    }
    @Test
    public void testDBService() throws Exception {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("META-INF/applicationContext.xml");
        PlaceServiceImpl placeService = context.getBean(PlaceServiceImpl.class);
        UserServiceImpl userService = context.getBean(UserServiceImpl.class);
        WeatherDataImpl weatherDataService = context.getBean(WeatherDataImpl.class);
        RequesterDataFromDBOrOpenWeatherMap requesterDataFromDBOrOpenWeatherMap = context.getBean(RequesterDataFromDBOrOpenWeatherMap.class);

        User userKevGen = new User("KevGen",1L);
        userService.addUser(userKevGen);

        User userCherep = new User("Cherep",2L);


        Place placeSPB=new Place("Санкт-Петербург");
        Place placeMoscow=new Place("Москва");
        Place placeChelybinsk=new Place("Челябинск");
        placeService.addPlace(placeSPB);
        placeService.addPlace(placeChelybinsk);
        placeService.addPlace(placeMoscow);

        userKevGen.addNewPlace(placeSPB);
        userKevGen.addNewPlace(placeMoscow);
        userKevGen.addNewPlace(placeChelybinsk);

        userService.addUser(userKevGen);


        userCherep.addNewPlace(placeSPB);
        userCherep.addNewPlace(placeMoscow);
        userCherep.addNewPlace(placeChelybinsk);

        userService.addUser(userCherep);

        Set<WeatherData> weatherChelyabinsk = new HashSet<>(requesterDataFromDBOrOpenWeatherMap.getWeatherDataByPlaceNDay(placeChelybinsk, 5));
        Set<WeatherData> weatherSPB = new HashSet<>(requesterDataFromDBOrOpenWeatherMap.getWeatherDataByPlaceNDay(placeSPB, 5));
        Set<WeatherData> weatherMoskow = new HashSet<>(requesterDataFromDBOrOpenWeatherMap.getWeatherDataByPlaceNDay(placeMoscow, 5));

        for(WeatherData wd: weatherChelyabinsk ){
            wd.setPlace(placeChelybinsk);
            weatherDataService.addWeatherData(wd);
            System.out.println(wd.toString());

        }
        for(WeatherData wd: weatherMoskow ){
            wd.setPlace(placeMoscow);
            weatherDataService.addWeatherData(wd);
        }
        for(WeatherData wd: weatherSPB ){
            wd.setPlace(placeSPB);
            weatherDataService.addWeatherData(wd);
        }

        List<WeatherData> currWd = weatherDataService.getAllWeatherDataByPlaceAndDateNearCurrentTime("Челябинск", LocalTime.now(),LocalDate.now());
        System.out.println("Текущая погода в челябинске :\n\n\n\n "+ currWd.get(0).toString());




    }

    @Test
    public void testWeatherRequestHandler() throws Exception {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("META-INF/applicationContext.xml");
        WeatherRequestHandler weatherRequestHandler =context.getBean(WeatherRequestHandler.class);
        List<WeatherData> weatherSPB = weatherRequestHandler.getWeatherDataByCityNameNDay("Санкт-Петербург",0);
        for(WeatherData wd: weatherSPB ){
            System.out.println(wd.toString());
        }
    }

    @Test
    public void testRequesterDataFromDBOrOpenWeatherMap() throws Exception {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("META-INF/applicationContext.xml");
        PlaceServiceImpl placeService = context.getBean(PlaceServiceImpl.class);
        UserServiceImpl userService = context.getBean(UserServiceImpl.class);
        RequesterDataFromDBOrOpenWeatherMap requesterDataFromDBOrOpenWeatherMap = context.getBean(RequesterDataFromDBOrOpenWeatherMap.class);

        User userKevGen = new User("KevGen",1L);
        userService.addUser(userKevGen);

        User userCherep = new User("Cherep",2L);


        Place placeSPB=new Place("Санкт-Петербург");
        Place placeMoscow=new Place("Москва");
        Place placeChelybinsk=new Place("Челябинск");
        placeService.addPlace(placeSPB);
        placeService.addPlace(placeChelybinsk);
        placeService.addPlace(placeMoscow);

        userKevGen.addNewPlace(placeSPB);
        userKevGen.addNewPlace(placeMoscow);
        userKevGen.addNewPlace(placeChelybinsk);

        userService.addUser(userKevGen);


        userCherep.addNewPlace(placeSPB);
        userCherep.addNewPlace(placeMoscow);
        userCherep.addNewPlace(placeChelybinsk);

        userService.addUser(userCherep);
        //requesterDataFromDBOrOpenWeatherMap.getWeatherDataByPlaceNDay(placeSPB,5);
        List<WeatherData> lwd =requesterDataFromDBOrOpenWeatherMap.getWeatherDataByPlaceNDay(placeSPB,0);
        for(WeatherData wd: lwd){
            System.out.println(wd.toString());
        }


    }




    @Test
    public void testGettingFromDB() throws Exception {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("META-INF/applicationContext.xml");
        WeatherDataImpl weatherDataService = context.getBean(WeatherDataImpl.class);
        LocalDate currDate =  LocalDate.now();
        LocalTime currTime =  LocalTime.now();
        List<WeatherData> ans = new ArrayList<>();
        ans = weatherDataService.getAllWeatherDataByPlaceAndDateAfterCurrentTime("Санкт-Петербург",currTime,currDate);
        for(WeatherData wd: ans){
            System.out.println(wd.toString());
        }
    }

}
