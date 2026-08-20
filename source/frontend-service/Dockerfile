# Dùng nền Java 21 siêu nhẹ
FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

# Copy file .jar vào trong container
COPY target/*.jar app.jar

# Mở cổng mặc định của Spring Boot
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
