FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml .
COPY src ./src
RUN mvn -DskipTests package

FROM eclipse-temurin:21-jre
LABEL authors="kevgen"
WORKDIR /app
RUN mkdir -p /app/logs
COPY --from=build /workspace/target/WeatherBot.jar /app/weatherbot.jar
ENTRYPOINT ["java", "-jar", "/app/weatherbot.jar"]
