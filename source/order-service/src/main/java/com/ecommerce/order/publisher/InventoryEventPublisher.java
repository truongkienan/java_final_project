package com.ecommerce.order.publisher;

import com.ecommerce.order.config.RabbitMQConfig;
import com.ecommerce.order.dto.StockEventDTO;
import com.ecommerce.order.dto.StockItemDTO;
import com.ecommerce.order.entity.Invoice;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class InventoryEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public InventoryEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    private StockEventDTO toStockEvent(Invoice invoice) {
        StockEventDTO event = new StockEventDTO();
        event.setOrderId(invoice.getId().toString());
        List<StockItemDTO> items = invoice.getDetails().stream().map(detail -> {
            StockItemDTO item = new StockItemDTO();
            item.setProductId(detail.getProductId());
            item.setQuantity(detail.getQuantity());
            return item;
        }).collect(Collectors.toList());
        event.setItems(items);
        return event;
    }

    // Bước đầu của Saga: báo cho inventory-service biết đơn hàng vừa tạo, cần trừ kho.
    public void publishOrderCreated(Invoice invoice) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.ROUTING_KEY_ORDER_CREATED, toStockEvent(invoice));
        System.out.println("--> [RabbitMQ] Đã gửi yêu cầu trừ kho cho Order ID: " + invoice.getId());
    }

    // Compensating transaction: hoàn kho khi đơn đã trừ kho rồi mà bị CANCELLED/FAILED/REFUNDED.
    public void publishInventoryRestore(Invoice invoice) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.ROUTING_KEY_INVENTORY_RESTORE, toStockEvent(invoice));
        System.out.println("--> [RabbitMQ] Đã gửi yêu cầu hoàn kho cho Order ID: " + invoice.getId());
    }
}