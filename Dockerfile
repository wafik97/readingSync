
FROM openjdk:17-jdk-slim
# make sureof this
WORKDIR /app

COPY target/reading-sync-service.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
