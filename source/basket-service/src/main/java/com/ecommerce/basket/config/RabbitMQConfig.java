package com.ecommerce.basket.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cấu hình RabbitMQ cho basket-service.
 *
 * basket-service KHÔNG phải một bước của Saga đặt hàng (không có compensating transaction,
 * không ảnh hưởng tính đúng đắn của đơn hàng) - nó chỉ lắng nghe "order.placed" như một
 * side-effect (tác dụng phụ: 1 việc xảy ra kèm theo việc chính, nhưng không phải mục đích chính
 * và không ảnh hưởng kết quả của việc chính - giống bật đèn thì kèm theo toả nhiệt, dù đèn không
 * toả nhiệt được thì việc "có ánh sáng" vẫn không hề bị ảnh hưởng) đơn thuần để tự xoá giỏ hàng
 * Redis sau khi khách đặt hàng thành công. Nếu message này bị mất (vd basket-service đang down),
 * hệ quả chỉ là giỏ hàng cũ còn sót lại trong Redis - không gây sai lệch dữ liệu nghiêm trọng như
 * trong Saga tồn kho, nên không cần cơ chế đảm bảo giao message phức tạp (retry, dead-letter...).
 *
 * Ví von: Exchange = bưu điện trung tâm (không giữ thư); Queue = hộp thư riêng của
 * basket-service (giữ thư tới khi được đọc); Binding = tờ đăng ký gửi cho bưu điện, nội dung
 * "hộp thư của tôi chỉ nhận thư dán nhãn order.placed".
 */
@Configuration
public class RabbitMQConfig {

    /** Phải giống hệt tên Exchange bên order-service - nơi thật sự publish "order.placed". */
    public static final String EXCHANGE_NAME = "ecommerce.exchange";
    public static final String QUEUE_BASKET = "basket_queue";
    public static final String ROUTING_KEY_ORDER_PLACED = "order.placed";

    /** Khai báo hộp thư riêng của basket-service. Tham số true = durable (sống sót qua restart RabbitMQ). */
    @Bean
    public Queue basketQueue() {
        return new Queue(QUEUE_BASKET, true);
    }

    /**
     * Khai báo Exchange dùng chung toàn hệ thống. Việc 4 service cùng khai @Bean này ở 4 nơi
     * khác nhau là bình thường và an toàn - RabbitMQ chỉ tạo Exchange 1 lần, các lần khai báo
     * sau (từ service khác) chỉ là no-op miễn tên và loại Exchange khớp nhau.
     */
    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    /** Đăng ký basketQueue vào Exchange: "chỉ chuyển cho tôi thư có nhãn order.placed". */
    @Bean
    public Binding binding(Queue basketQueue, TopicExchange exchange) {
        return BindingBuilder.bind(basketQueue).to(exchange).with(ROUTING_KEY_ORDER_PLACED);
    }
}
