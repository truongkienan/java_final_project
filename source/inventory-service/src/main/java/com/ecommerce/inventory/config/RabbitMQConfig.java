package com.ecommerce.inventory.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cấu hình RabbitMQ cho inventory-service - bên "trừ kho / hoàn kho" trong Saga đặt hàng.
 *
 * Xem tổng quan toàn bộ luồng Saga (6 bước) tại javadoc của
 * com.ecommerce.order.config.RabbitMQConfig (order-service). Ở đây inventory-service đóng vai
 * trò Bước 2 (nhận order.created -> trừ kho -> báo kết quả) và Bước 6 (nhận inventory.restore
 * -> hoàn kho, đây là bước "compensating transaction" khi giao dịch ở nơi khác thất bại).
 *
 * EXCHANGE_NAME phải là chuỗi giống hệt (kể cả dấu chấm/gạch dưới) với các service khác. Với
 * RabbitMQ, tên Exchange là định danh duy nhất - đặt sai tên nghĩa là tạo ra một Exchange KHÁC,
 * và message publish từ service khác sẽ không bao giờ tới được Queue khai báo ở đây.
 */
@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "ecommerce.exchange";

    /** Bước 2 của Saga: nhận yêu cầu trừ kho ngay khi order-service vừa tạo đơn. */
    public static final String ROUTING_KEY_ORDER_CREATED = "order.created";
    public static final String QUEUE_ORDER_CREATED = "inventory.order.created.queue";

    /** Bước 6 của Saga (compensating transaction): nhận yêu cầu hoàn kho khi đơn hỏng sau khi đã trừ kho. */
    public static final String ROUTING_KEY_INVENTORY_RESTORE = "inventory.restore";
    public static final String QUEUE_INVENTORY_RESTORE = "inventory.restore.queue";

    /** inventory-service publish kết quả trừ kho về order-service - chỉ publish, không cần Queue ở đây. */
    public static final String ROUTING_KEY_INVENTORY_RESULT = "inventory.result";

    /** Khai báo Exchange dùng chung toàn hệ thống. */
    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    /** Hộp thư nhận yêu cầu trừ kho. Tham số true = durable (Queue sống sót qua restart RabbitMQ). */
    @Bean
    public Queue orderCreatedQueue() {
        return new Queue(QUEUE_ORDER_CREATED, true);
    }

    /**
     * Đăng ký orderCreatedQueue vào Exchange với routing key "order.created".
     * Gọi trực tiếp orderCreatedQueue() thay vì nhận Queue qua tham số method - vì file này có
     * nhiều bean cùng kiểu Queue, Spring không tự phân biệt được nếu dựa vào kiểu tham số. Gọi
     * thẳng method vẫn trả về đúng bean singleton nhờ cơ chế proxy CGLIB của @Configuration.
     */
    @Bean
    public Binding bindingOrderCreated(TopicExchange exchange) {
        return BindingBuilder.bind(orderCreatedQueue()).to(exchange).with(ROUTING_KEY_ORDER_CREATED);
    }

    /** Hộp thư nhận yêu cầu hoàn kho (compensating transaction). */
    @Bean
    public Queue inventoryRestoreQueue() {
        return new Queue(QUEUE_INVENTORY_RESTORE, true);
    }

    /** Đăng ký inventoryRestoreQueue vào Exchange với routing key "inventory.restore". */
    @Bean
    public Binding bindingInventoryRestore(TopicExchange exchange) {
        return BindingBuilder.bind(inventoryRestoreQueue()).to(exchange).with(ROUTING_KEY_INVENTORY_RESTORE);
    }

    /** JSON converter dùng chung cho cả publish (InventoryEventPublisher) và consume (@RabbitListener). */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
