FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/basket-service-1.0-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
