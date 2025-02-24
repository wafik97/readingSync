
FROM openjdk:23-jdk-slim
WORKDIR /app
COPY target/reading-sync-service-1.0-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
