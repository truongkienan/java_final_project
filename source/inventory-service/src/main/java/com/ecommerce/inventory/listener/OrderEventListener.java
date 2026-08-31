package com.ecommerce.inventory.listener;

import com.ecommerce.inventory.config.RabbitMQConfig;
import com.ecommerce.inventory.dto.StockEventDTO;
import com.ecommerce.inventory.publisher.InventoryEventPublisher;
import com.ecommerce.inventory.service.StockService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * SUBSCRIBER (bên nhận) - Bước 2 của Saga đặt hàng.
 *
 * @RabbitListener(queues = QUEUE_ORDER_CREATED) khiến Spring tự canh Queue này bằng 1 luồng nền
 * và tự gọi handleOrderCreated mỗi khi có message tới - message đó do order-service publish
 * (routing key "order.created") ngay sau khi tạo Invoice, order-service publish xong là coi như
 * xong việc, không hề gọi trực tiếp hàm nào ở đây và cũng không chờ kết quả trừ kho ngay lập tức.
 *
 * Sau khi trừ kho xong (thành công hay thất bại), inventory-service lại ĐÓNG VAI PUBLISHER, gửi
 * tiếp message "inventory.result" ngược về cho order-service (xem InventoryEventPublisher) - đây
 * là ví dụ rõ nhất cho việc 1 service có thể vừa là Subscriber (nhận order.created) vừa là
 * Publisher (gửi inventory.result) trong cùng 1 luồng xử lý.
 */
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