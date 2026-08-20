package com.ecommerce.inventory.publisher;

import com.ecommerce.inventory.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class InventoryEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public InventoryEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    // Báo kết quả trừ kho về cho order-service - dùng đúng dạng payload {orderId, status}
    // giống hệt PaymentEventPublisher (payment-service) để order-service dùng chung 1 kiểu listener.
    public void publishInventoryResult(String orderId, String status) {
        Map<String, String> event = new HashMap<>();
        event.put("orderId", orderId);
        event.put("status", status);

        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.ROUTING_KEY_INVENTORY_RESULT, event);
        System.out.println("--> [RabbitMQ] Đã gửi kết quả trừ kho (" + status + ") cho Order ID: " + orderId);
    }
}