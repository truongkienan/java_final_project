package com.ecommerce.payment.config;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cấu hình RabbitMQ cho payment-service.
 *
 * payment-service CHỈ publish (routing key "payment.success", xem
 * com.ecommerce.payment.publisher.PaymentEventPublisher) - không consume message nào. Vì vậy
 * file này chỉ cần khai báo Exchange, không cần Queue/Binding: ai muốn nhận message của
 * payment-service (ở đây là order-service) phải tự khai báo Queue + Binding ở phía họ.
 *
 * Xem tổng quan luồng Saga đặt hàng tại javadoc của com.ecommerce.order.config.RabbitMQConfig -
 * payment-service đóng vai trò Bước 4 (thanh toán, xử lý độc lập ngoài luồng trừ kho).
 */
@Configuration
public class RabbitMQConfig {

    /**
     * Khai báo Exchange dùng chung "ecommerce.exchange" (loại Topic).
     * Spring Boot tự động gửi lệnh khai báo này lên RabbitMQ lúc khởi động - nếu Exchange đã
     * được service khác tạo trước rồi (cùng tên, cùng loại) thì đây chỉ là no-op, không lỗi.
     */
    @Bean
    public TopicExchange ecommerceExchange() {
        return new TopicExchange("ecommerce.exchange");
    }

    /** JSON converter dùng khi publish message (object -> JSON -> byte[]). */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
