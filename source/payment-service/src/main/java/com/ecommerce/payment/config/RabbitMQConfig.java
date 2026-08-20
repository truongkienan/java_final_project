package com.ecommerce.payment.config;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Khai báo một Topic Exchange
    // Spring Boot sẽ tự động tạo Exchange này trên RabbitMQ lúc khởi động nếu chưa có
    @Bean
    public TopicExchange ecommerceExchange() {
        return new TopicExchange("ecommerce.exchange");
    }

    // Dùng JSON thay vì Java serialization mặc định (bị chặn bởi cơ chế bảo mật của Spring AMQP)
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}