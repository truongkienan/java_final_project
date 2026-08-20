package com.ecommerce.basket.listener;

import com.ecommerce.basket.config.RabbitMQConfig;
import com.ecommerce.basket.repository.CartRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
