package ru.spring.core.project.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Location;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.spring.core.project.BLusingBD.RequesterDataFromDBOrOpenWeatherMap;
import ru.spring.core.project.DBService.PlaceService;
import ru.spring.core.project.DBService.impl.UserServiceImpl;
import ru.spring.core.project.config.BotConfig;
import ru.spring.core.project.entity.Place;
import ru.spring.core.project.entity.User;
import ru.spring.core.project.repositories.PlaceRepository;
import ru.spring.core.project.repositories.UserRepository;
import ru.spring.core.project.repositories.WeatherDataRepository;
import ru.spring.core.project.statemachine.BotState;
import ru.spring.core.project.entity.WeatherData;
import ru.spring.core.project.weatherCommunication.CoordinateForWeatherBot;
import ru.spring.core.project.weatherCommunication.WeatherRequestHandler;


import java.util.*;
import java.util.function.Consumer;

import static ru.spring.core.project.utils.WeatherHelper.formatWeather;


@Service
public class Bot extends TelegramLongPollingBot {

    @Autowired
    private final BotConfig config;

    @Autowired
    private WeatherRequestHandler weatherRequestHandler;

    static final String HELP_MESSAGE = "Welcome!\n The /start command initializes the work of the bot\n " +
            "The /weather command provides functionality for choosing a convenient mode of operation of the bot.\n The /help command show you a description of all commands ";

    private static final Logger logger = LoggerFactory.getLogger(Bot.class);

    private BotState currentState = BotState.START;

    private Map<Long, User> currentUserMap = new HashMap<>();

    /**
     * A map defining the transition actions for the start state.
     * It maps user commands to their respective handlers when the bot is in the start state.
     *
     * The keys represent the user commands (e.g., "/start", "/help", "/weather").
     * The values are method references that handle the respective commands.
     */
    private final Map<String, Consumer<Update>> stateStartTransition = Map.of(
            "/start", this::startCommandReceived,
            "/help", this::helpCommandReceived,
            "/weather", this::handleWeatherCommand
    );

    /**
     * A map defining the transition actions for the weather state.
     * It maps user choices to their respective handlers when the bot is in the weather state.
     *
     * The keys represent the user choices (e.g., "Current Weather", "Full Weather Forecast").
     * The values are method references that handle the respective choices.
     */
    private final Map<String, Consumer<Update>> weatherStateTransition = Map.of(
            "Current Weather", this::handleLocationOrCity,
            "Full Weather Forecast", this::handleFullWeatherForecast
    );

    /**
     * A map defining the transition actions for the forecasted mode.
     * It maps user choices to their respective handlers when the bot is in the forecasted mode.
     *
     * The keys represent the user choices (e.g., "The weather forecast for the day", "The weather forecast for two days", "Five-day weather forecast", "Exit").
     * The values are method references that handle the respective choices.
     */
    private final Map<String, Consumer<Update>> handleForecastedMode = Map.of(
            "The weather forecast for the day", update -> handleForecastSelection(update, 0),
            "The weather forecast for two days", update -> handleForecastSelection(update, 1),
            "Five-day weather forecast", update -> handleForecastSelection(update, 4),
            "Exit", this::handleExit
    );

    /**
     * A map defining the transition actions for location or city input.
     * It maps user choices to their respective handlers when the bot prompts for location or city input.
     *
     * The keys represent the user choices (e.g., "Type City Name", "Send Location", "Exit").
     * The values are method references that handle the respective choices.
     */
    private final Map<String, Consumer<Update>> handleLocationOrCityAction = Map.of(
            "Type City Name", this::handleCityNameInput,
            //"Send Location", this::handleLocationInput,
            "Exit", this::handleExit
    );

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private PlaceRepository placeRepository;

    @Autowired
    private WeatherDataRepository weatherDataRepository;

    @Autowired
    private PlaceService placeService;

    @Autowired
    private RequesterDataFromDBOrOpenWeatherMap requesterData;

    @Autowired
    public Bot(ClassPathXmlApplicationContext context) {
        this.config = context.getBean(BotConfig.class);

        weatherRequestHandler = context.getBean(WeatherRequestHandler.class);// = new WeatherRequestHandler(config);
        List<BotCommand> listOfCommands = new ArrayList();
        listOfCommands.add(new BotCommand("/start", "get a welcome message"));
        listOfCommands.add(new BotCommand("/help", "show help message"));
        listOfCommands.add(new BotCommand("/weather", "сhoosing a scenario option"));
        try {
            this.execute(new SetMyCommands(listOfCommands, new BotCommandScopeDefault(), null));
            logger.info("Create menu bot");
        } catch (TelegramApiException e) {
            logger.error("Error setting bot commands", e.getMessage());
        }
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getBotToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if(update.hasMessage() && update.getMessage().hasText()){
            long chatId = update.getMessage().getChatId();
            String message = update.getMessage().getText().trim();
            String userName = update.getMessage().getChat().getUserName();



            List<User> allUsers = userRepository.findAllUsersByChatId(chatId);
            User currentUser;
            if(allUsers.isEmpty()){
                currentUser = new User(userName, chatId);
                userService.addUser(currentUser);
            } else {
                currentUser = allUsers.get(0);
            }

            currentUserMap.put(chatId, currentUser);

            switch (currentState){
                case START:
                    Consumer<Update> startCommandHandler = stateStartTransition.get(message);
                    if(startCommandHandler != null){
                        startCommandHandler.accept(update);
                    } else {
                        sendMessage(chatId, "Unknown command. Type /help for assistance.");
                        changeBotState(BotState.START);
                    }
                    break;
                case MODE_SELECTION:
                    Consumer<Update> weatherCommandHandler = weatherStateTransition.get(message);
                    if(weatherCommandHandler != null){
                        weatherCommandHandler.accept(update);
                    } else {
                        sendMessage(chatId, "Unknown command. Type /help for assistance.");
                        changeBotState(BotState.START);
                    }
                    break;
                case CURRENT_WEATHER_ENTER_CITY:
                    if(message.equals("Type City Name")){
                        sendMessage(chatId, "Please type the name of the city.");
                        changeBotState(BotState.AWAITING_CITY_NAME);
                    }else if(message.equals("Exit")){
                        changeBotState(BotState.START);
                    }else{
                        sendMessage(chatId, "Unknown command. Type /help for assistance.");
                        changeBotState(BotState.START);
                    }
                    break;
                case AWAITING_CITY_NAME:
                    getWeatherCity(update);
                    break;
                case FULL_WEATHER_FORECAST:
                    Consumer<Update> fullWeatherCommand = handleForecastedMode.get(message);
                    if(fullWeatherCommand != null){
                        fullWeatherCommand.accept(update);
                    } else {
                        sendMessage(chatId, "Unknown command. Type /help for assistance.");
                        changeBotState(BotState.START);
                    }
                    break;
                case USER_ENTER_LOCATION_ONE:
                    if(message.equals("Type City Name")){
                        sendMessage(chatId, "Please type the name of the city.");
                        changeBotState(BotState.FORECAST_AWAITING_CITY_NAME_DAY);
                    }else if(message.equals("Send Location")){
                        sendMessage(chatId, "Anal plug in the USER_ENTER_LOCATION_ONE state for location");
                        changeBotState(BotState.START);
                    }else {
                        changeBotState(BotState.START);
                    }
                    break;
                case FORECAST_AWAITING_CITY_NAME_DAY:
                    try {
                        getForecastCity(chatId, message, 0);
                        changeBotState(BotState.START);
                    } catch (Exception e) {
                        logger.error("Problems getting a place {}", message);
                        changeBotState(BotState.START);
                    }
                    break;
                case USER_ENTER_LOCATION_TWO:
                    if(message.equals("Type City Name")){
                        sendMessage(chatId, "Please type the name of the city.");
                        changeBotState(BotState.FORECAST_AWAITING_CITY_NAME_TWO_DAYS);
                    }else if(message.equals("Send Location")){
                        sendMessage(chatId, "Anal plug in the USER_ENTER_LOCATION_ONE state for location");
                        changeBotState(BotState.START);
                    }else {
                        changeBotState(BotState.START);
                    }
                    break;
                case FORECAST_AWAITING_CITY_NAME_TWO_DAYS:
                    try {
                        getForecastCity(chatId, message, 1);
                        changeBotState(BotState.START);
                    } catch (Exception e) {
                        logger.error("Problems getting a place {}", message);
                        changeBotState(BotState.START);
                    }
                    break;
                case USER_ENTER_LOCATION_FIVE:
                    if(message.equals("Type City Name")){
                        sendMessage(chatId, "Please type the name of the city.");
                        changeBotState(BotState.FORECAST_AWAITING_CITY_NAME_FIVE_DAYS);
                    }else if(message.equals("Send Location")){
                        sendMessage(chatId, "Anal plug in the USER_ENTER_LOCATION_ONE state for location");
                        changeBotState(BotState.START);
                    }else {
                        changeBotState(BotState.START);
                    }
                    break;
                case FORECAST_AWAITING_CITY_NAME_FIVE_DAYS:
                    try {
                        getForecastCity(chatId, message, 4);
                        changeBotState(BotState.START);
                    } catch (Exception e) {
                        logger.error("Problems getting a place {}", message);
                        changeBotState(BotState.START);
                    }
                    break;
                default:
                    sendMessage(chatId, "Invalid state. Type /start to begin.");
                    changeBotState(BotState.START);
                    break;
            }
        }else if (update.getMessage().hasLocation()) {
            Location location = update.getMessage().getLocation();
            handleLocation(location, update.getMessage().getChatId());
            changeBotState(BotState.START);
        }
    }

    /**
     * Exits the current state and updates the bot state to start.
     * @param update The update received from Telegram API containing the user message.
     */
    private void handleExit(Update update) {
        changeBotState(BotState.START);
    }

    /**
     * Changing the state of the bot after each step of the machine
     * @param newState the state from the list of the automaton from the BotStates enumeration**/
    private void changeBotState(BotState newState) {
        logger.info("Bot state changed to {}", newState);
        this.currentState = newState;
    }

    /**
     * Handles a user's location and sends weather information to the specified chat.
     *
     * This method processes the received location data, extracts the latitude and longitude,
     * and requests the current weather information for those coordinates. The weather information
     * is then sent to the user via a Telegram message.
     *
     * @param location The Location object containing the latitude and longitude of the user's location.
     * @param chatId The chat ID to which the weather information message will be sent.
     */
    private void handleLocation(Location location, long chatId) {

        sendMessage(chatId, "We will start finding your location.");
        Double latitude = location.getLatitude();
        Double longitude = location.getLongitude();
        SendMessage message = new SendMessage();
        message.setChatId(chatId);

        User currentUser = currentUserMap.get(chatId);
        CoordinateForWeatherBot currentLocation = new CoordinateForWeatherBot(latitude, longitude);
        if (currentUser == null) {
            sendMessage(chatId, "User not found. Please start the bot with /start command.");
            return;
        }

        Place userCoordinates = getCurrentPlace(currentLocation);
        currentUser.getSetOfPlaces().add(userCoordinates);
        userRepository.save(currentUser);

/*        var weatherData = requesterData.getWeatherDataByPlaceNow(userCoordinates);
        if (weatherData == null) {
            weatherData = writeWeatherForDayAndGetForNow(currentLocation, userCoordinates);
        }*/

        try {

            var response = weatherRequestHandler.getWeatherDataByCoordinatesNow(currentLocation);
            String weather = response.weatherResponse();
            message.setText(weather);
            execute(message);
            logger.info("Send message to user {}", chatId);
        } catch (TelegramApiException e) {
            logger.error("Error sending location", e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Sends a message to the user asking for city name input or location sharing for full weather forecast.
     * Updates the bot state to user enter location for full weather forecast.
     * @param update The update received from Telegram API containing the user message.
     */
    private void handleFullWeatherForecast(Update update) {
        long chatId = update.getMessage().getChatId();

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText("Please select the forecast option:");

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(true);
        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow row = new KeyboardRow();
        KeyboardButton buttonForecastDay = new KeyboardButton();
        buttonForecastDay.setText("The weather forecast for the day");
        row.add(buttonForecastDay);
        keyboard.add(row);

        row = new KeyboardRow();
        KeyboardButton buttonForecastTwoDays = new KeyboardButton();
        buttonForecastTwoDays.setText("The weather forecast for two days");
        row.add(buttonForecastTwoDays);
        keyboard.add(row);

        row = new KeyboardRow();
        KeyboardButton buttonForecastFiveDays = new KeyboardButton();
        buttonForecastFiveDays.setText("Five-day weather forecast");
        row.add(buttonForecastFiveDays);
        keyboard.add(row);

        row = new KeyboardRow();
        KeyboardButton buttonExit = new KeyboardButton();
        buttonExit.setText("Exit");
        row.add(buttonExit);
        keyboard.add(row);

        keyboardMarkup.setKeyboard(keyboard);
        sendMessage.setReplyMarkup(keyboardMarkup);

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            logger.error("Error sending message to user {}", chatId, e);
        }

        changeBotState(BotState.FULL_WEATHER_FORECAST);
    }

    /**
     * Processes the user's forecast selection and retrieves the weather forecast for the specified number of days.
     * Updates the bot state to start after processing.
     * @param update The update received from Telegram API containing the user message.
     * @param forecastDays The number of days for the weather forecast.
     */
    private void handleForecastSelection(Update update, int forecastDays) {
        long chatId = update.getMessage().getChatId();
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText("How would you like to provide your location for the " + (forecastDays + 1) + "-day forecast?");

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(true);
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();

        if (forecastDays != 0 && forecastDays != 1 && forecastDays != 4) {
            KeyboardButton buttonLocation = new KeyboardButton();
            buttonLocation.setText("Send Location");
            buttonLocation.setRequestLocation(true);
            row.add(buttonLocation);
        }

        KeyboardButton buttonCity = new KeyboardButton();
        buttonCity.setText("Type City Name");
        row.add(buttonCity);
        keyboard.add(row);

        row = new KeyboardRow();
        KeyboardButton buttonExit = new KeyboardButton();
        buttonExit.setText("Exit");
        row.add(buttonExit);
        keyboard.add(row);

        keyboardMarkup.setKeyboard(keyboard);
        sendMessage.setReplyMarkup(keyboardMarkup);

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            logger.error("Error sending message to user {}", chatId, e);
        }

        switch (forecastDays) {
            case 0:
                changeBotState(BotState.USER_ENTER_LOCATION_ONE);
                break;
            case 1:
                changeBotState(BotState.USER_ENTER_LOCATION_TWO);
                break;
            case 4:
                changeBotState(BotState.USER_ENTER_LOCATION_FIVE);
                break;
        }
    }

    /**
     * Sends a help message to the user with information on how to use the bot.
     * @param update The update received from Telegram API containing the user message.
     */
    private void helpCommandReceived(Update update) {
        long chatId = update.getMessage().getChatId();
        sendMessage(chatId, HELP_MESSAGE);
        changeBotState(BotState.START);
    }

    /**
     * Sends a welcome message to the user and updates the bot state to the mode selection.
     * @param update The update received from Telegram API containing the user message.
     */
    private void startCommandReceived(Update update) {
        long chatId = update.getMessage().getChatId();
        String response = "Hello " + update.getMessage().getFrom().getFirstName() + ". I provide an opportunity to find out the weather anywhere in the world at any time.";
        logger.info("Replied to user. response: " + response + " chatId: " + chatId + " name: " + update.getMessage().getFrom().getFirstName());
        sendMessage(chatId, response);
        changeBotState(BotState.START);
    }

    /**
     * Processes the user's input for the city name and retrieves weather information.
     * Updates the bot state to start after processing.
     * @param update The update received from Telegram API containing the user message.
     */
    private void handleCityNameInput(Update update) {
        long chatId = update.getMessage().getChatId();
        String cityName = update.getMessage().getText();
        try {
            List<WeatherData> weatherData = weatherRequestHandler.getWeatherDataByCityNameNDay(cityName, 0);
            sendWeatherData(chatId, weatherData);
        } catch (Exception e) {
            sendMessage(chatId, "Error occurred while processing your request.");
            logger.error("Error processing city name input", e);
        }

        changeBotState(BotState.MODE_SELECTION);
    }

    /**
     * Sends a formatted weather data message to the specified chat.

     * This method takes a list of WeatherData objects and formats each entry into a string containing the date,
     * time, temperature, and weather description. The formatted strings are then concatenated into a single message
     * which is sent to the user via the Telegram API.

     * @param chatId The chat ID to which the weather data message will be sent.
     * @param weatherDataList An ArrayList of WeatherData objects containing weather information to be formatted and sent.
     */
    private void sendWeatherData(long chatId, List<WeatherData> weatherDataList) {
        StringJoiner message = new StringJoiner("\n");
        for (WeatherData weatherData : weatherDataList) {
            message.add(String.format("Date: %s, Time: %s, Temperature: %.2f°C, Description: %s",
                    weatherData.getDate(),
                    weatherData.getTime(),
                    weatherData.getTemperature(),
                    weatherData.getWeatherStateMain()));
        }

        sendMessage(chatId, message.toString());
    }

    /**
     * Processes the user's input for location sharing and retrieves weather information.
     * Updates the bot state to start after processing.
     * @param update The update received from Telegram API containing the user message.
     */
    private void handleLocationInput(Update update){
        /*

        long chatId = update.getMessage().getChatId();
        Location location = update.getMessage().getLocation();
        if (location == null) {
            sendMessage(chatId, "Please send a valid location.");
            return;
        }

        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        ArrayList<WeatherData> weatherData = null;
        try {
            weatherData = weatherRequestHandler.getAnswerCoordsNDay(latitude, longitude, 0);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        sendWeatherData(chatId, weatherData);

        changeBotState(BotState.START); // Вернуться в исходное состояние

       */

    }

    /**
     * Sends a message to the user via the Telegram API.
     * @param chatId The chat ID of the user.
     * @param message The text message to be sent.
     */
    public void sendMessage(long chatId, String message) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(message);

        try {
            execute(sendMessage);
            logger.info("Message sent to user {}", chatId); // Добавляем лог об отправке сообщения пользователю
        } catch (TelegramApiException e) {
            logger.error("Error sending message to user {}", chatId, e); // Логируем ошибку, если не удалось отправить сообщение
        }
    }

    /**
     * Sends a message to the user asking for city name input or location sharing for weather information.
     * Updates the bot state to location or city input state.
     * @param update The update received from Telegram API containing the user message.
     */
    private void handleLocationOrCity(Update update) {
        long chatId = update.getMessage().getChatId();
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText("How would you like to provide your location?");

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(true);
        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow row = new KeyboardRow();

        KeyboardButton buttonLocation = new KeyboardButton();
        buttonLocation.setText("Send Location");
        buttonLocation.setRequestLocation(true);
        row.add(buttonLocation);

        KeyboardButton buttonCity = new KeyboardButton();
        buttonCity.setText("Type City Name");
        row.add(buttonCity);
        keyboard.add(row);

        row = new KeyboardRow();
        KeyboardButton buttonExit = new KeyboardButton();
        buttonExit.setText("Exit");
        row.add(buttonExit);
        keyboard.add(row);

        keyboardMarkup.setKeyboard(keyboard);
        sendMessage.setReplyMarkup(keyboardMarkup);

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            logger.error("Error sending message to user {}", chatId, e);
        }

        changeBotState(BotState.CURRENT_WEATHER_ENTER_CITY);
    }

    /**
     * Sends a message to the user with options for weather information and updates the bot state to current weather enter city.
     * @param update The update received from Telegram API containing the user message.
     */
    private void handleWeatherCommand(Update update) {

        sendMessage(update.getMessage().getChatId(), "For the bot to work correctly, you need to select the operating mode. " +
                "After that, either select the enter city command or if you want to find out the current weather," +
                " you can enter your location.");

        long chatId = update.getMessage().getChatId();
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText("Please select the weather option:");

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(true);
        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow row = new KeyboardRow();
        KeyboardButton buttonCurrentWeather = new KeyboardButton();
        buttonCurrentWeather.setText("Current Weather");
        row.add(buttonCurrentWeather);
        keyboard.add(row);

        row = new KeyboardRow();
        KeyboardButton buttonTomorrowWeather = new KeyboardButton();
        buttonTomorrowWeather.setText("Full Weather Forecast");
        row.add(buttonTomorrowWeather);
        keyboard.add(row);

        keyboardMarkup.setKeyboard(keyboard);
        sendMessage.setReplyMarkup(keyboardMarkup);

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            logger.error("Error sending message to user {}", chatId, e);
        }

        changeBotState(BotState.MODE_SELECTION);
    }

    /**
     * Retrieves weather information based on the city name provided by the user.
     * @param update The update received from Telegram API containing the user message.
     */
    private void getWeatherCity(Update update) {
        long chatId = update.getMessage().getChatId();
        String cityName = update.getMessage().getText();

        User currentUser = currentUserMap.get(chatId);

        if (currentUser == null) {
            sendMessage(chatId, "User not found. Please start the bot with /start command.");
            return;
        }

        Place userPlace = getCurrentPlace(cityName);
        currentUser.getSetOfPlaces().add(userPlace);
        userRepository.save(currentUser);

        var weatherData = requesterData.getWeatherDataByPlaceNow(userPlace);
        if (weatherData == null) {
            weatherData = writeWeatherForDayAndGetForNow(cityName, userPlace);
        }

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(formatWeather(weatherData, cityName));

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            logger.error("Error requesting location", e);
        }

        changeBotState(BotState.START);
    }

    private WeatherData writeWeatherForDayAndGetForNow(String cityName, Place userPlace) {
        return writeWeatherForDayAndGetForNow(cityName, userPlace, 1);
    }

    private WeatherData writeWeatherForDayAndGetForNow(String cityName, Place userPlace, int days) {
        var cityWeather = weatherRequestHandler.getWeatherDataByCityNameNDay(cityName, days);
        for (WeatherData data : cityWeather) {
            data.setPlace(userPlace);
            weatherDataRepository.save(data);
        }
        return cityWeather.getFirst();
    }

    private WeatherData writeWeatherForDayAndGetForNow(CoordinateForWeatherBot coordinate, Place userPlace) {
        var cityWeather = weatherRequestHandler.getWeatherDataByCoordinatesNow(coordinate);
//        for (WeatherData data : cityWeather) {
//            data.setPlace(userPlace);
//            weatherDataRepository.save(data);
//        }
        return cityWeather; //.getFirst();
    }

    private Place getCurrentPlace(String cityName) {
        // Проверка, существует ли уже место с таким названием
        List<Place> existingPlaceOpt = placeRepository.findAllPlacesByPlaceName(cityName);
        if (existingPlaceOpt.isEmpty()) {
            var userPlace = new Place(cityName);
            placeService.addPlace(userPlace);
            return userPlace;
        }
        return existingPlaceOpt.getFirst();
    }

    private Place getCurrentPlace(CoordinateForWeatherBot coordinate) {
        var userPlace = new Place(coordinate);
        placeService.addPlace(userPlace);
        return userPlace;
    }


    /**
     * Retrieves the weather forecast for the specified number of days based on the city name provided by the user.
     * @param chatId The chat ID of the user.
     * @param cityName The name of the city for which the weather forecast is to be retrieved.
     * @param days The number of days for the weather forecast.
     */
    private void getForecastCity(long chatId, String cityName, int days) {
        List<WeatherData> response = weatherRequestHandler.getWeatherDataByCityNameNDay(cityName, days);
        logger.info("Weather forecast for the city {}:", cityName);

        User currentUser = currentUserMap.get(chatId);
        if (currentUser == null) {
            sendMessage(chatId, "User not found. Please start the bot with /start command.");
            return;
        }

        Place currentPlace = getCurrentPlace(cityName);
        currentUser.getSetOfPlaces().add(currentPlace);
        userRepository.save(currentUser);

        var weatherDataForBD = requesterData.getWeatherDataByPlaceNDay(currentPlace, days);
        if (weatherDataForBD == null){
            weatherDataForBD = (List<WeatherData>) writeWeatherForDayAndGetForNow(cityName, currentPlace, days);
        }



        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(formatWeather(weatherDataForBD, cityName));

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            logger.error("Error sending a message to the user {}", chatId, e);
        }

        changeBotState(BotState.START);
    }

}
