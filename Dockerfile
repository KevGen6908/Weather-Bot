FROM openjdk:23-ea-21-jdk
LABEL authors="kevgen"

WORKDIR /app
COPY ./target/WeatherBot.jar /app/weatherbot.jar

ENTRYPOINT ["java", "-jar", "/app/weatherbot.jar"]