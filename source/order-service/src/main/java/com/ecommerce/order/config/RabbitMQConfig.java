package com.ecommerce.order.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    // SỬA LẠI: Đồng bộ dấu chấm (.) thay vì dấu gạch dưới (_)
    public static final String EXCHANGE_NAME = "ecommerce.exchange";

    public static final String ROUTING_KEY_ORDER_PLACED = "order.placed";

    // THÊM MỚI: Tên Queue và Routing key để nhận thông báo thanh toán
    public static final String QUEUE_PAYMENT_SUCCESS = "order.payment.success.queue";
    public static final String ROUTING_KEY_PAYMENT_SUCCESS = "payment.success";

    // Saga Tồn kho: publish khi tạo đơn - không cần khai báo Queue ở đây vì inventory-service
    // là bên sở hữu Queue lắng nghe, order-service chỉ publish (giống hệt cách order.placed hoạt động).
    public static final String ROUTING_KEY_ORDER_CREATED = "order.created";

    // Saga Tồn kho: publish yêu cầu hoàn kho (compensating transaction) - cũng chỉ publish.
    public static final String ROUTING_KEY_INVENTORY_RESTORE = "inventory.restore";

    // Saga Tồn kho: order-service nhận kết quả trừ kho từ inventory-service qua Queue này.
    public static final String QUEUE_INVENTORY_RESULT = "order.inventory.result.queue";
    public static final String ROUTING_KEY_INVENTORY_RESULT = "inventory.result";

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    // Khai báo Queue lắng nghe thanh toán
    @Bean
    public Queue paymentSuccessQueue() {
        return new Queue(QUEUE_PAYMENT_SUCCESS);
    }

    // Kết nối Queue vào Exchange thông qua Routing Key
    // (gọi trực tiếp paymentSuccessQueue() thay vì nhận qua tham số - từ nay có 2 bean Queue trong
    // file này nên không thể để Spring tự inject theo tên tham số được, xem bug đã gặp ở inventory-service)
    @Bean
    public Binding bindingPaymentSuccess(TopicExchange exchange) {
        return BindingBuilder.bind(paymentSuccessQueue()).to(exchange).with(ROUTING_KEY_PAYMENT_SUCCESS);
    }

    // Saga Tồn kho: Queue lắng nghe kết quả trừ kho
    @Bean
    public Queue inventoryResultQueue() {
        return new Queue(QUEUE_INVENTORY_RESULT);
    }

    @Bean
    public Binding bindingInventoryResult(TopicExchange exchange) {
        return BindingBuilder.bind(inventoryResultQueue()).to(exchange).with(ROUTING_KEY_INVENTORY_RESULT);
    }

    // Dùng JSON thay vì Java serialization mặc định (bị chặn bởi cơ chế bảo mật của Spring AMQP)
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}