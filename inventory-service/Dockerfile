FROM eclipse-temurin:21-jre

WORKDIR /app
COPY target/*.jar app.jar

EXPOSE 8084
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=docker", "app.jar"]
