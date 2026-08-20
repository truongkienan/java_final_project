package com.ecommerce.basket.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    public static final String EXCHANGE_NAME = "ecommerce_exchange"; // Bưu điện
    public static final String QUEUE_BASKET = "basket_queue"; // Hộp thư riêng của Basket Service
    public static final String ROUTING_KEY_ORDER_PLACED = "order.placed"; // Nhãn dán trên thư

    // Khai báo Hộp thư
    @Bean
    public Queue basketQueue() {
        return new Queue(QUEUE_BASKET, true);
    }

    // Khai báo Bưu điện
    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    // Liên kết Hộp thư với Bưu điện, báo rằng "Tôi chỉ nhận thư có nhãn order.placed"
    @Bean
    public Binding binding(Queue basketQueue, TopicExchange exchange) {
        return BindingBuilder.bind(basketQueue).to(exchange).with(ROUTING_KEY_ORDER_PLACED);
    }
}
