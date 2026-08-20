package com.ecommerce.order.listener;

import com.ecommerce.order.config.RabbitMQConfig;
import com.ecommerce.order.entity.Invoice;
import com.ecommerce.order.publisher.InventoryEventPublisher;
import com.ecommerce.order.repository.InvoiceRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class PaymentEventListener {

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private InventoryEventPublisher inventoryEventPublisher;

    // Các trạng thái mà khi đơn chuyển sang, kho đã trừ trước đó cần được hoàn lại (compensating transaction)
    private static final Set<String> STATUSES_REQUIRE_STOCK_RESTORE = Set.of("CANCELLED", "FAILED", "REFUNDED");

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