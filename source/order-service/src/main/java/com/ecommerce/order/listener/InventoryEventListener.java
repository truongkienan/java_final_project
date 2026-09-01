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

/**
 * SUBSCRIBER (bên nhận).
 *
 * Cơ chế giao tiếp: giống PaymentEventListener - @RabbitListener khiến Spring tự canh Queue
 * QUEUE_INVENTORY_RESULT bằng 1 luồng nền, tự gọi method này mỗi khi có message tới. Message này
 * do inventory-service publish (routing key "inventory.result") sau khi xử lý xong yêu cầu trừ
 * kho ở bước 1 của Saga - 2 service không gọi hàm lẫn nhau, chỉ trao đổi qua message bất đồng bộ.
 */
@Service
public class InventoryEventListener {

    @Autowired
    private InvoiceRepository invoiceRepository;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_INVENTORY_RESULT)
    public void handleInventoryResult(Map<String, String> event) {
        String orderIdStr = event.get("orderId");
        String status = event.get("status"); // "RESERVED" hoặc "FAILED"

        System.out.println("--> [Order Service] Nhận kết quả trừ kho cho Order: " + orderIdStr + " - " + status);

        // Lỗi DỮ LIỆU (message sai định dạng, không phải lỗi hạ tầng): retry cũng vô ích vì lần
        // sau parse lại vẫn sai y hệt (poison message) - bắt riêng, log rồi bỏ qua, KHÔNG throw
        // ra ngoài để tránh Spring nack + RabbitMQ redeliver lặp vô hạn cho 1 message không bao
        // giờ xử lý được.
        UUID orderId;
        try {
            orderId = UUID.fromString(orderIdStr);
        } catch (IllegalArgumentException e) {
            System.err.println("Message inventory.result có orderId không hợp lệ, bỏ qua: " + orderIdStr);
            return;
        }

        Optional<Invoice> invoiceOpt = invoiceRepository.findById(orderId);
        if (invoiceOpt.isEmpty()) {
            // Không tìm thấy Invoice - dữ liệu không đồng bộ (hiếm gặp), retry cũng không giải
            // quyết được gì nên bỏ qua thay vì throw.
            System.err.println("Không tìm thấy Invoice cho Order: " + orderIdStr);
            return;
        }

        Invoice invoice = invoiceOpt.get();
        // Guard chống xử lý trùng (idempotency check). RabbitMQ có thể gửi lại (redeliver)
        // đúng message này nếu order-service chưa kịp gửi ACK trước đó (crash, restart,
        // mất kết nối...). Nếu không có guard, 1 message inventory.result cũ/lặp có thể
        // ghi đè sai trạng thái của đơn đã chuyển sang bước khác từ lúc đó - ví dụ: đơn đã
        // bị Admin hủy và hoàn kho xong (CANCELLED), nhưng message RESERVED cũ đến muộn lại
        // set nhầm về lại PENDING, khiến hệ thống tưởng đơn còn giữ hàng trong khi kho đã
        // được hoàn trả từ trước - sai lệch dữ liệu nghiêm trọng. Vì vậy chỉ cập nhật khi
        // đơn THỰC SỰ còn đang chờ kết quả trừ kho (PENDING_INVENTORY).
        if (!"PENDING_INVENTORY".equals(invoice.getStatus())) {
            return; // Đơn không còn ở trạng thái chờ (đã xử lý/message bị gửi lặp) - bỏ qua
        }

        invoice.setStatus("RESERVED".equals(status) ? "PENDING" : "OUT_OF_STOCK");

        // Cố tình KHÔNG bọc try/catch quanh save(): đây là lỗi HẠ TẦNG (DB mất kết nối, timeout,
        // deadlock...) - retry sau đó CÓ THỂ thành công khi DB hồi phục. Để exception thoát ra
        // ngoài method, Spring sẽ tự NACK message này, RabbitMQ sẽ giữ lại và gửi lại (redeliver)
        // sau - thay vì nuốt lỗi rồi vẫn ACK oan như code cũ, khiến Invoice kẹt vĩnh viễn ở
        // PENDING_INVENTORY dù inventory-service đã trừ kho thành công thật sự.
        invoiceRepository.save(invoice);
    }
}