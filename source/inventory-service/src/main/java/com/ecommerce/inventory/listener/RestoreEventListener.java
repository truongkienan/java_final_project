package com.ecommerce.inventory.listener;

import com.ecommerce.inventory.config.RabbitMQConfig;
import com.ecommerce.inventory.dto.StockEventDTO;
import com.ecommerce.inventory.service.StockService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * SUBSCRIBER (bên nhận) - Bước 6 của Saga đặt hàng, xử lý compensating transaction.
 *
 * Message tới Queue QUEUE_INVENTORY_RESTORE do order-service publish (routing key
 * "inventory.restore") khi phát hiện đơn đã trừ kho rồi nhưng lại bị CANCELLED/FAILED/REFUNDED
 * (xem PaymentEventListener bên order-service). inventory-service không biết TẠI SAO cần hoàn
 * kho (có thể do khách huỷ, thanh toán lỗi, hay hoàn tiền) - nó chỉ cần biết "orderId này, hoàn
 * lại đúng số lượng đã trừ", toàn bộ quyết định "khi nào cần hoàn" nằm ở phía order-service.
 */
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