package com.ecommerce.inventory.config;

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
    // Dùng đúng tên exchange "có dấu chấm" của order-service (nguồn phát thật của các event này) -
    // KHÔNG copy theo basket-service (đang có bug lệch tên "ecommerce_exchange", xử lý ở task riêng).
    public static final String EXCHANGE_NAME = "ecommerce.exchange";

    public static final String ROUTING_KEY_ORDER_CREATED = "order.created";
    public static final String QUEUE_ORDER_CREATED = "inventory.order.created.queue";

    public static final String ROUTING_KEY_INVENTORY_RESTORE = "inventory.restore";
    public static final String QUEUE_INVENTORY_RESTORE = "inventory.restore.queue";

    public static final String ROUTING_KEY_INVENTORY_RESULT = "inventory.result";

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue orderCreatedQueue() {
        return new Queue(QUEUE_ORDER_CREATED, true);
    }

    // Gọi trực tiếp orderCreatedQueue() thay vì nhận qua tham số - vì có 2 bean cùng kiểu Queue
    // trong file này, Spring không tự phân biệt được theo tên tham số (thiếu cờ compiler -parameters,
    // giống lỗi @RequestParam đã gặp trước đó trong AccountController). Gọi trực tiếp method tránh
    // hẳn vấn đề này (Spring @Configuration dùng CGLIB proxy nên vẫn trả về đúng bean singleton).
    @Bean
    public Binding bindingOrderCreated(TopicExchange exchange) {
        return BindingBuilder.bind(orderCreatedQueue()).to(exchange).with(ROUTING_KEY_ORDER_CREATED);
    }

    @Bean
    public Queue inventoryRestoreQueue() {
        return new Queue(QUEUE_INVENTORY_RESTORE, true);
    }

    @Bean
    public Binding bindingInventoryRestore(TopicExchange exchange) {
        return BindingBuilder.bind(inventoryRestoreQueue()).to(exchange).with(ROUTING_KEY_INVENTORY_RESTORE);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}