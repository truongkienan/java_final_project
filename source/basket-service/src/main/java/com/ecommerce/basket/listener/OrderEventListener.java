package com.ecommerce.basket.listener;

import com.ecommerce.basket.config.RabbitMQConfig;
import com.ecommerce.basket.repository.CartRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * SUBSCRIBER (bên nhận) - KHÔNG thuộc Saga đặt hàng, chỉ là side-effect độc lập (tác dụng phụ đi
 * kèm việc đặt hàng thành công, không phải mục đích chính và không ảnh hưởng kết quả đơn hàng -
 * xem giải thích chi tiết trong RabbitMQConfig.java cùng package config).
 *
 * Message tới Queue QUEUE_BASKET do order-service publish TRỰC TIẾP ngay trong OrderController
 * (không qua class Publisher riêng như InventoryEventPublisher) - xem OrderController.java, đoạn
 * gọi rabbitTemplate.convertAndSend(EXCHANGE_NAME, ROUTING_KEY_ORDER_PLACED, savedInvoice.getMemberId())
 * ngay sau khi lưu đơn hàng thành công. Nội dung message chỉ là 1 chuỗi memberId (không phải DTO
 * phức tạp), khớp với tham số String memberId của handleOrderPlacedEvent bên dưới.
 *
 * Khác với Saga tồn kho (nơi mất message sẽ làm sai lệch số liệu), basket-service không có bước
 * "hoàn tác" nếu lỡ mất message - hệ quả chỉ là giỏ hàng cũ còn sót lại trong Redis.
 */
@Component
public class OrderEventListener {

    @Autowired
    private CartRepository cartRepository;

    // Ngồi rình ở hộp thư basket_queue, hễ có thư là nhảy vào chạy hàm này
    @RabbitListener(queues = RabbitMQConfig.QUEUE_BASKET)
    public void handleOrderPlacedEvent(String memberId) {
        System.out.println("========== ĐÃ NHẬN THƯ TỪ RABBITMQ ==========");
        System.out.println("Khách hàng " + memberId + " vừa đặt hàng thành công!");

        // Tiến hành xóa giỏ hàng trong Redis
        cartRepository.deleteById(memberId);

        System.out.println("Đã tự động xóa giỏ hàng của " + memberId + " khỏi Redis.");
        System.out.println("==============================================");
    }
}
