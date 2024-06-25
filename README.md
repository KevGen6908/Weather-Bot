# Telegram Weather Bot 
The application is available in Docker hub https://hub.docker.com/r/kevgen/telegram-weather-bot.
WeatherBot is a Telegram bot that provides weather forecasts for any location in the world. Built with Java and Spring Framework.

## Dependencies
- TelegramAPI java;
- OpenWeatherAPI; 
- JPA; 
- PostgreSQL;
### Common features:
- /start the command initializes the bot's operation by switching it to active mode
- /help shows a description of all commands
- /weather Takes the user to a custom menu where two scenarios are available: the current weather forecast and the Full weather forecast.
## Features
- In the scenario when the user selects the current weather forecast, two options are available: enter your location or write an interesting one
- In the scenario where the user selects the full weather forecast, there are three options for getting the forecast for the current day, two days ahead and five days ahead
- At the same time, if the user enters the city of interest to him and this city is already in the database, the application will pull up the weather forecast from the database, thereby reducing the number of calls to OpenWeatherAPI
# Launching the application
In order for the application to start, you need to download Docker image from Docker hub https://hub.docker.com/r/kevgen/telegram-weather-bot and launch the application with the command
```docker compose up -d weatherbot ```
#Link on bot 
https://t.me/killBonJoviWeatherBot
