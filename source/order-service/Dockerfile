# Sử dụng môi trường Java 21 gọn nhẹ
FROM eclipse-temurin:21-jre-alpine

# Thư mục làm việc trong Container
WORKDIR /app

# Copy file jar vừa build vào trong Container
COPY target/order-service-1.0-SNAPSHOT.jar app.jar

# Chạy ứng dụng
ENTRYPOINT ["java", "-jar", "app.jar"]
