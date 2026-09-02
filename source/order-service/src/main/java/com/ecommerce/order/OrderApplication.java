package com.ecommerce.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

// @EnableScheduling: bat co che chay job dinh ky (@Scheduled) - can cho PendingOrderCleanupJob
// tu dong huy don PENDING bo ngang qua lau.
@SpringBootApplication
@EnableScheduling
public class OrderApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class, args);
    }
}
