package com.ecommerce.inventory.listener;

import com.ecommerce.inventory.config.RabbitMQConfig;
import com.ecommerce.inventory.dto.StockEventDTO;
import com.ecommerce.inventory.service.StockService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RestoreEventListener {

    @Autowired
    private StockService stockService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_INVENTORY_RESTORE)
    public void handleInventoryRestore(StockEventDTO event) {
        System.out.println("--> [Inventory Service] Hoàn kho cho Order ID: " + event.getOrderId());
        stockService.restoreStock(event);
    }
}