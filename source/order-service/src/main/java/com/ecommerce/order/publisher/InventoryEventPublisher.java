package com.ecommerce.order.publisher;

import com.ecommerce.order.config.RabbitMQConfig;
import com.ecommerce.order.dto.StockEventDTO;
import com.ecommerce.order.dto.StockItemDTO;
import com.ecommerce.order.entity.Invoice;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * PUBLISHER (bên gửi).
 *
 * Cơ chế giao tiếp: gọi rabbitTemplate.convertAndSend(exchange, routingKey, object) rồi KẾT THÚC
 * NGAY - không chờ, không biết bên nhận xử lý ra sao, thậm chí không biết có ai đang nghe hay
 * không. Đây là giao tiếp BẤT ĐỒNG BỘ (asynchronous), khác hẳn gọi REST API (nơi caller luôn phải
 * chờ response). Việc định tuyến message này tới đúng Queue của service nào là do RabbitMQ quyết
 * định dựa trên các Binding đã khai báo (xem RabbitMQConfig của từng service) - InventoryEventPublisher
 * hoàn toàn không biết, và không cần biết, inventory-service có đang chạy hay không.
 *
 * Nhờ vậy order-service và inventory-service KHÔNG PHỤ THUỘC trực tiếp vào nhau (loose coupling):
 * nếu inventory-service đang restart, message vẫn nằm chờ an toàn trong Queue (vì Queue durable),
 * order-service không hề bị lỗi hay phải chờ.
 */
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