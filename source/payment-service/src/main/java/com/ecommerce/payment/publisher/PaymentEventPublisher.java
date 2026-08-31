package com.ecommerce.payment.publisher;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * PUBLISHER (bên gửi) - Bước 4 của Saga đặt hàng.
 *
 * payment-service là service DUY NHẤT không có Listener nào cả (xem payment-service/RabbitMQConfig)
 * - nó chỉ đóng vai Publisher, không bao giờ đóng vai Subscriber. Sau khi xử lý thanh toán xong
 * (thành công hay thất bại), nó publish message "payment.success" rồi kết thúc, không quan tâm
 * order-service xử lý message đó ra sao hay khi nào - toàn bộ liên lạc là 1 chiều, bất đồng bộ.
 */
@Service
public class PaymentEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public PaymentEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishPaymentStatus(String invoiceId, String status) {
        Map<String, String> event = new HashMap<>();

        // Mẹo: Vẫn giữ nguyên tên Key là "orderId" để order-service đọc được,
        // nhưng Value thực chất lại là UUID của hóa đơn (invoiceId)
        event.put("orderId", invoiceId);
        event.put("status", status);

        rabbitTemplate.convertAndSend("ecommerce.exchange", "payment.success", event);
        System.out.println("--> [RabbitMQ] Đã gửi sự kiện Payment " + status + " cho Invoice ID: " + invoiceId);
    }

}