# Sử dụng môi trường Java 21 gọn nhẹ
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app
COPY target/payment-service-1.0-SNAPSHOT.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
