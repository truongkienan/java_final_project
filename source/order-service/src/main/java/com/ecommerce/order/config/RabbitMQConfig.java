package com.ecommerce.order.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cấu hình RabbitMQ cho order-service.
 *
 * KHÁI NIỆM RABBITMQ DÙNG TRONG FILE NÀY:
 * - Exchange: trạm phân loại thư, KHÔNG giữ message. Publisher luôn gửi vào Exchange,
 *   không bao giờ gửi thẳng vào Queue.
 * - TopicExchange: 1 trong các loại Exchange (còn có Fanout, Direct, Headers). Với Topic,
 *   RabbitMQ định tuyến message tới Queue bằng cách so khớp "routing key" - hỗ trợ wildcard
 *   ("*", "#") dù project này chỉ dùng key cố định, chưa dùng wildcard.
 * - Queue: hộp thư thật sự giữ message cho tới khi có Consumer lấy đi.
 * - Binding: "tờ đăng ký nhận" nối 1 Queue vào 1 Exchange kèm điều kiện routing key.
 *   Không Binding thì Queue không bao giờ nhận được gì dù Exchange đã tồn tại.
 *
 * QUAN TRỌNG: 4 service (order/inventory/payment/basket) phải khai báo CÙNG một Exchange
 * (tên "ecommerce.exchange", loại Topic) - RabbitMQ coi tên Exchange là định danh duy nhất,
 * nên đây thực chất là 1 exchange vật lý dùng chung. Khai báo lại 1 exchange đã tồn tại với
 * đúng thông số là an toàn (no-op); khai báo sai loại (vd Topic vs Fanout) sẽ bị RabbitMQ từ chối.
 *
 * SAGA "ĐẶT HÀNG" (kiểu choreography: các service tự phản ứng với event của nhau,
 * KHÔNG có 1 nhạc trưởng trung tâm điều phối toàn bộ giao dịch):
 *
 *   1. order-service     : tạo Invoice (PENDING_INVENTORY) -> publish "order.created"
 *   2. inventory-service  : nhận order.created -> trừ kho -> publish "inventory.result" (RESERVED/FAILED)
 *   3. order-service     : nhận inventory.result -> Invoice = PENDING (nếu RESERVED) hoặc OUT_OF_STOCK (nếu FAILED)
 *   4. payment-service    : xử lý thanh toán (độc lập, ngoài luồng 3 bước trên) -> publish "payment.success"
 *   5. order-service     : nhận payment.success -> cập nhật Invoice.status;
 *                           nếu status rơi vào CANCELLED/FAILED/REFUNDED (tức đã trừ kho mà hỏng)
 *                           -> publish "inventory.restore" (COMPENSATING TRANSACTION - hoàn tác bước 2)
 *   6. inventory-service  : nhận inventory.restore -> hoàn lại số lượng tồn kho đã trừ
 *
 * Song song, KHÔNG thuộc saga: basket-service cũng nghe "order.placed" chỉ để tự xoá giỏ hàng
 * Redis - không có rollback, mất message ở đây không ảnh hưởng tính đúng đắn của đơn hàng.
 *
 * Xem thêm: com.ecommerce.order.listener.PaymentEventListener,
 *           com.ecommerce.order.listener.InventoryEventListener,
 *           com.ecommerce.order.publisher.InventoryEventPublisher.
 */
@Configuration
public class RabbitMQConfig {

    /** Tên Exchange dùng chung toàn hệ thống - phải giống hệt (từng ký tự) ở mọi service. */
    public static final String EXCHANGE_NAME = "ecommerce.exchange";

    /** order-service publish khi khách đặt hàng xong; basket-service là nơi lắng nghe để xoá giỏ hàng. */
    public static final String ROUTING_KEY_ORDER_PLACED = "order.placed";

    /** Bước 4-5 của Saga: payment-service publish, order-service nhận qua Queue riêng dưới đây. */
    public static final String QUEUE_PAYMENT_SUCCESS = "order.payment.success.queue";
    public static final String ROUTING_KEY_PAYMENT_SUCCESS = "payment.success";

    /**
     * Bước 1 của Saga: order-service publish khi vừa tạo đơn, yêu cầu trừ kho.
     * KHÔNG khai báo Queue/Binding ở đây vì order-service chỉ PUBLISH trên key này - bên nhận
     * (inventory-service) mới là nơi sở hữu Queue lắng nghe (xem inventory-service/RabbitMQConfig).
     */
    public static final String ROUTING_KEY_ORDER_CREATED = "order.created";

    /**
     * Bước 5 của Saga - compensating transaction: publish khi đơn đã trừ kho rồi nhưng sau đó
     * bị huỷ/thất bại/hoàn tiền, cần hoàn lại số lượng tồn kho đã trừ. Cũng chỉ publish ở đây,
     * inventory-service mới sở hữu Queue lắng nghe.
     */
    public static final String ROUTING_KEY_INVENTORY_RESTORE = "inventory.restore";

    /** Bước 2-3 của Saga: order-service nhận kết quả trừ kho (RESERVED/FAILED) qua Queue này. */
    public static final String QUEUE_INVENTORY_RESULT = "order.inventory.result.queue";
    public static final String ROUTING_KEY_INVENTORY_RESULT = "inventory.result";

    /** Khai báo Exchange dùng chung cho toàn hệ thống. */
    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    /** Hộp thư riêng của order-service, dùng để nhận thông báo thanh toán. */
    @Bean
    public Queue paymentSuccessQueue() {
        return new Queue(QUEUE_PAYMENT_SUCCESS);
    }

    /**
     * Đăng ký paymentSuccessQueue vào Exchange với routing key "payment.success".
     * Gọi trực tiếp paymentSuccessQueue() (thay vì nhận Queue qua tham số method) vì file này
     * có NHIỀU bean cùng kiểu Queue - nếu khai theo tham số, Spring không biết nên inject bean
     * Queue nào. Gọi thẳng method vẫn an toàn: @Configuration được Spring proxy bằng CGLIB nên
     * lời gọi này trả về đúng bean singleton đã tạo, không tạo ra Queue mới.
     */
    @Bean
    public Binding bindingPaymentSuccess(TopicExchange exchange) {
        return BindingBuilder.bind(paymentSuccessQueue()).to(exchange).with(ROUTING_KEY_PAYMENT_SUCCESS);
    }

    /** Hộp thư riêng của order-service, dùng để nhận kết quả trừ kho từ inventory-service. */
    @Bean
    public Queue inventoryResultQueue() {
        return new Queue(QUEUE_INVENTORY_RESULT);
    }

    /** Đăng ký inventoryResultQueue vào Exchange với routing key "inventory.result". */
    @Bean
    public Binding bindingInventoryResult(TopicExchange exchange) {
        return BindingBuilder.bind(inventoryResultQueue()).to(exchange).with(ROUTING_KEY_INVENTORY_RESULT);
    }

    /**
     * Bộ chuyển đổi JSON dùng cho cả publish (object -> JSON -> byte[])
     * lẫn consume (byte[] -> JSON -> object/DTO). Bắt buộc khai báo Bean này vì Spring AMQP
     * mặc định dùng Java serialization - bị chặn vì có lỗ hổng bảo mật (deserialization RCE).
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
