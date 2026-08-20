package com.ecommerce.inventory.listener;

import com.ecommerce.inventory.config.RabbitMQConfig;
import com.ecommerce.inventory.dto.StockEventDTO;
import com.ecommerce.inventory.publisher.InventoryEventPublisher;
import com.ecommerce.inventory.service.StockService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OrderEventListener {

    @Autowired
    private StockService stockService;

    @Autowired
    private InventoryEventPublisher inventoryEventPublisher;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_ORDER_CREATED)
    public void handleOrderCreated(StockEventDTO event) {
        System.out.println("--> [Inventory Service] Nhận đơn hàng mới cần trừ kho: " + event.getOrderId());
        try {
            stockService.reserveStock(event);
            inventoryEventPublisher.publishInventoryResult(event.getOrderId(), "RESERVED");
        } catch (Exception e) {
            System.err.println("Trừ kho thất bại cho Order ID " + event.getOrderId() + ": " + e.getMessage());
            inventoryEventPublisher.publishInventoryResult(event.getOrderId(), "FAILED");
        }
    }
}