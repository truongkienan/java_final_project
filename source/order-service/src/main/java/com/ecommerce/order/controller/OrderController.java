package com.ecommerce.order.controller;

import com.ecommerce.catalog.grpc.CatalogGrpcServiceGrpc;
import com.ecommerce.catalog.grpc.ProductRequest;
import com.ecommerce.catalog.grpc.ProductResponse;
import com.ecommerce.order.config.RabbitMQConfig;
import com.ecommerce.order.entity.Invoice;
import com.ecommerce.order.entity.InvoiceDetail;
import com.ecommerce.order.publisher.InventoryEventPublisher;
import com.ecommerce.order.repository.InvoiceRepository;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private org.springframework.amqp.rabbit.core.RabbitTemplate rabbitTemplate;

    @Autowired
    private InventoryEventPublisher inventoryEventPublisher;

    // Tiêm (Inject) gRPC Client tự động kết nối tới cấu hình "catalog-service" trong properties
    @GrpcClient("catalog-service")
    private CatalogGrpcServiceGrpc.CatalogGrpcServiceBlockingStub catalogGrpcStub;

    @PostMapping("/checkout")
    public ResponseEntity<?> checkout(@RequestBody Invoice invoice) {
        double totalAmount = 0.0;

        if (invoice.getDetails() != null) {
            for (InvoiceDetail detail : invoice.getDetails()) {

                // 1. Tạo "Bức thư" gửi đi hỏi Catalog Service
                ProductRequest request = ProductRequest.newBuilder()
                        .setProductId(detail.getProductId())
                        .build();

                // 2. Gọi gRPC (Quá trình này bay qua mạng ảo và về trong chưa tới 1 phần nghìn giây!)
                ProductResponse response = catalogGrpcStub.checkProductPrice(request);

                // 3. Kiểm tra xem sản phẩm có tồn tại không
                if (!response.getExists()) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body("Lỗi: Sản phẩm có ID " + detail.getProductId() + " không tồn tại!");
                }

                // 4. Lấy giá chuẩn từ Server (Đề phòng hacker sửa giá trị trên Postman)
                detail.setPrice(response.getPrice());
                detail.setProductName(response.getProductName());

                // Cộng dồn vào tổng tiền
                totalAmount += response.getPrice() * detail.getQuantity();

                // Gắn ngược lại ID hóa đơn cha
                detail.setInvoice(invoice);
            }
        }

        // Cập nhật tổng tiền cuối cùng
        invoice.setTotalAmount(totalAmount);

        // Saga Tồn kho: đơn khởi tạo ở trạng thái chờ trừ kho, CHƯA phải PENDING (chờ thanh toán) -
        // inventory-service xử lý xong mới chuyển tiếp sang PENDING hoặc OUT_OF_STOCK
        invoice.setStatus("PENDING_INVENTORY");

        Invoice savedInvoice = invoiceRepository.save(invoice);

        // Phát thanh (Publish) 1 sự kiện thông báo qua RabbitMQ
        // Mang theo ID của khách hàng để người nhận biết phải xóa giỏ hàng của ai
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.ROUTING_KEY_ORDER_PLACED,
                savedInvoice.getMemberId()
        );

        // Saga Tồn kho: báo cho inventory-service trừ kho cho đơn vừa tạo
        inventoryEventPublisher.publishOrderCreated(savedInvoice);

        return ResponseEntity.ok(savedInvoice);
    }

    @GetMapping
    public ResponseEntity<?> getOrders() {
        return ResponseEntity.ok(invoiceRepository.findAllByOrderByOrderDateDesc());
    }

    @GetMapping("/member/{memberId}")
    public ResponseEntity<?> getOrdersByMember(@PathVariable("memberId") String memberId) {
        return ResponseEntity.ok(invoiceRepository.findAllByMemberIdOrderByOrderDateDesc(memberId));
    }

    // Lấy 1 đơn hàng theo ID - dùng cho frontend-service poll trạng thái sau khi checkout
    // (chờ inventory-service xử lý xong PENDING_INVENTORY -> PENDING/OUT_OF_STOCK)
    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderById(@PathVariable("id") UUID id) {
        Invoice invoice = invoiceRepository.findById(id).orElse(null);
        if (invoice == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(invoice);
    }

    // Hủy đơn - chỉ cho phép khi đơn còn PENDING (chưa thanh toán, không cần hoàn tiền)
    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable("id") UUID id) {
        Invoice invoice = invoiceRepository.findById(id).orElse(null);
        if (invoice == null) {
            return ResponseEntity.notFound().build();
        }
        if (!"PENDING".equals(invoice.getStatus())) {
            return ResponseEntity.badRequest().body("Chỉ có thể hủy đơn hàng đang ở trạng thái PENDING");
        }
        invoice.setStatus("CANCELLED");
        invoiceRepository.save(invoice);

        // Saga: đơn đang PENDING nghĩa là đã trừ kho thành công trước đó - hoàn lại kho
        inventoryEventPublisher.publishInventoryRestore(invoice);

        return ResponseEntity.ok(invoice);
    }

    // Khách yêu cầu hoàn tiền cho đơn đã PAID - chỉ đổi trạng thái, chờ Admin duyệt mới gọi PayPal thật
    @PutMapping("/{id}/request-refund")
    public ResponseEntity<?> requestRefund(@PathVariable("id") UUID id) {
        Invoice invoice = invoiceRepository.findById(id).orElse(null);
        if (invoice == null) {
            return ResponseEntity.notFound().build();
        }
        if (!"PAID".equals(invoice.getStatus())) {
            return ResponseEntity.badRequest().body("Chỉ có thể yêu cầu hoàn tiền cho đơn hàng đã PAID");
        }
        invoice.setStatus("REFUND_REQUESTED");
        invoiceRepository.save(invoice);
        return ResponseEntity.ok(invoice);
    }

    // Admin từ chối yêu cầu hoàn tiền - trả đơn về lại PAID
    @PutMapping("/{id}/reject-refund")
    public ResponseEntity<?> rejectRefund(@PathVariable("id") UUID id) {
        Invoice invoice = invoiceRepository.findById(id).orElse(null);
        if (invoice == null) {
            return ResponseEntity.notFound().build();
        }
        if (!"REFUND_REQUESTED".equals(invoice.getStatus())) {
            return ResponseEntity.badRequest().body("Đơn hàng không ở trạng thái chờ duyệt hoàn tiền");
        }
        invoice.setStatus("PAID");
        invoiceRepository.save(invoice);
        return ResponseEntity.ok(invoice);
    }

    // Admin xóa hẳn đơn hàng khỏi hệ thống - xóa cứng (không khôi phục được), cascade xóa luôn
    // các dòng invoice_details liên quan nhờ CascadeType.ALL khai báo trên Invoice.details.
    // Không xóa gì bên payment-service (lịch sử thanh toán vẫn giữ nguyên, thuộc domain riêng).
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteOrder(@PathVariable("id") UUID id) {
        if (!invoiceRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        invoiceRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}