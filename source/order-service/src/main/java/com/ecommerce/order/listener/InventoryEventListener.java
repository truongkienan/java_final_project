package com.ecommerce.order.listener;

import com.ecommerce.order.config.RabbitMQConfig;
import com.ecommerce.order.entity.Invoice;
import com.ecommerce.order.repository.InvoiceRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class InventoryEventListener {

    @Autowired
    private InvoiceRepository invoiceRepository;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_INVENTORY_RESULT)
    public void handleInventoryResult(Map<String, String> event) {
        String orderIdStr = event.get("orderId");
        String status = event.get("status"); // "RESERVED" hoặc "FAILED"

        System.out.println("--> [Order Service] Nhận kết quả trừ kho cho Order: " + orderIdStr + " - " + status);

        try {
            UUID orderId = UUID.fromString(orderIdStr);
            Optional<Invoice> invoiceOpt = invoiceRepository.findById(orderId);
            if (invoiceOpt.isPresent()) {
                Invoice invoice = invoiceOpt.get();
                if (!"PENDING_INVENTORY".equals(invoice.getStatus())) {
                    return; // Đơn không còn ở trạng thái chờ (đã xử lý/message bị gửi lặp) - bỏ qua
                }
                invoice.setStatus("RESERVED".equals(status) ? "PENDING" : "OUT_OF_STOCK");
                invoiceRepository.save(invoice);
            }
        } catch (Exception e) {
            System.err.println("Lỗi xử lý kết quả trừ kho: " + e.getMessage());
        }
    }
}