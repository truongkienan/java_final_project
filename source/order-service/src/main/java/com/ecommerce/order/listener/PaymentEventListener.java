package com.ecommerce.order.listener;

import com.ecommerce.order.config.RabbitMQConfig;
import com.ecommerce.order.entity.Invoice;
import com.ecommerce.order.publisher.InventoryEventPublisher;
import com.ecommerce.order.repository.InvoiceRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * SUBSCRIBER (bên nhận).
 *
 * Cơ chế giao tiếp: annotation @RabbitListener(queues = ...) báo cho Spring "hãy đứng canh Queue
 * này bằng 1 luồng nền (background thread), hễ có message rơi vào thì tự động gọi method bên
 * dưới". Method này KHÔNG được code nào trong project gọi trực tiếp - nó chỉ được kích hoạt khi
 * payment-service publish message thật sự tới Queue QUEUE_PAYMENT_SUCCESS.
 *
 * payment-service (nơi publish) hoàn toàn không biết class này tồn tại - nó chỉ biết "gửi vào
 * exchange ecommerce.exchange kèm routing key payment.success". Việc message đó được chuyển tới
 * đúng Queue này là do Binding (bindingPaymentSuccess trong order-service/RabbitMQConfig) quyết
 * định. Đây chính là điểm khác biệt với gọi hàm/API trực tiếp: 2 bên hoàn toàn không tham chiếu
 * tới nhau, chỉ cùng "thoả thuận ngầm" về tên routing key.
 */
@Service
public class PaymentEventListener {

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private InventoryEventPublisher inventoryEventPublisher;

    // Các trạng thái mà khi đơn chuyển sang, kho đã trừ trước đó cần được hoàn lại (compensating transaction)
    private static final Set<String> STATUSES_REQUIRE_STOCK_RESTORE = Set.of("CANCELLED", "FAILED", "REFUNDED");

    // @Transactional: giữ Hibernate session mở xuyên suốt method - bắt buộc phải có vì
    // đây là @RabbitListener (thread nền), không phải HTTP request nên không được Spring Boot's
    // Open-Session-In-View bảo vệ. Thiếu annotation này, invoice.getDetails() (@OneToMany LAZY)
    // trong toStockEvent() sẽ ném LazyInitializationException khi publishInventoryRestore() gọi tới.
    @Transactional
    @RabbitListener(queues = RabbitMQConfig.QUEUE_PAYMENT_SUCCESS)
    public void handlePaymentSuccess(Map<String, String> event) {
        String orderIdStr = event.get("orderId");
        String status = event.get("status"); // "PAID"

        System.out.println("--> [Order Service] Đã nhận thông báo thanh toán thành công cho Order: " + orderIdStr);

        try {
            UUID orderId = UUID.fromString(orderIdStr);
            Optional<Invoice> invoiceOpt = invoiceRepository.findById(orderId);
            if (invoiceOpt.isPresent()) {
                Invoice invoice = invoiceOpt.get();

                // Guard chống xử lý trùng (idempotency check) - cùng lý do với InventoryEventListener:
                // CheckoutController.paymentCancel() (GET /checkout/cancel) gọi lại payment-service
                // MỖI LẦN trang được tải/refresh (không phải chỉ 1 lần duy nhất), nên message này có
                // thể tới đây nhiều lần cho cùng 1 đơn. Không có guard, mỗi lần message tới lại publish
                // lại inventory.restore -> kho bị cộng dồn nhiều lần cho cùng 1 đơn (bug thực tế đã gặp:
                // F5 trang Order Cancelled 10 lần -> hoàn kho 10 lần).
                // Điều kiện hợp lệ khác nhau theo từng status:
                // - REFUNDED: chỉ hợp lệ khi đơn đang PAID hoặc REFUND_REQUESTED (chờ Admin duyệt hoàn tiền).
                // - PAID/FAILED/CANCELLED: chỉ hợp lệ khi đơn đang PENDING (vừa tạo PayPal order, chưa
                //   có kết quả) - nếu đơn đã rời khỏi PENDING từ trước (ví dụ đã là CANCELLED), bỏ qua.
                boolean validTransition = "REFUNDED".equals(status)
                        ? Set.of("PAID", "REFUND_REQUESTED").contains(invoice.getStatus())
                        : "PENDING".equals(invoice.getStatus());
                if (!validTransition) {
                    System.out.println("--> Bỏ qua (đơn đã xử lý từ trước, trạng thái hiện tại: " + invoice.getStatus() + ")");
                    return;
                }

                invoice.setStatus(status); // Cập nhật hóa đơn sang PAID
                invoiceRepository.save(invoice);
                System.out.println("--> Đã cập nhật DB thành công!");

                // Saga: nếu đơn đã trừ kho trước đó mà giờ hỏng (hủy/thất bại/hoàn tiền), hoàn lại kho
                if (STATUSES_REQUIRE_STOCK_RESTORE.contains(status)) {
                    inventoryEventPublisher.publishInventoryRestore(invoice);
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi xử lý hóa đơn: " + e.getMessage());
        }
    }
}