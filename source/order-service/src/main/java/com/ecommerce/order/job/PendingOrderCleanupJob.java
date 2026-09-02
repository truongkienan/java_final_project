package com.ecommerce.order.job;

import com.ecommerce.order.entity.Invoice;
import com.ecommerce.order.repository.InvoiceRepository;
import com.ecommerce.order.service.OrderCancellationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * Xử lý kịch bản "Đặt hàng xong nhưng không thanh toán" (xem mục VII.4 báo cáo): khách được
 * chuyển sang PayPal sau khi kho đã trừ thành công (đơn ở PENDING), nhưng đóng tab/bỏ ngang -
 * không bấm Cancel (không có sự kiện nào gửi về), khiến đơn kẹt PENDING vô thời hạn và kho bị
 * giữ mãi. Job này quét định kỳ, tự động hủy các đơn PENDING quá lâu để hoàn lại kho.
 *
 * Tái dùng đúng logic hủy thủ công (OrderCancellationService) - job chỉ khác API hủy ở chỗ
 * TỰ TÌM đơn cần hủy thay vì chờ người dùng bấm nút.
 */
@Component
public class PendingOrderCleanupJob {

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private OrderCancellationService orderCancellationService;

    // Số phút tối đa 1 đơn được giữ ở PENDING trước khi bị coi là "bỏ ngang" và tự động hủy.
    @Value("${order.pending-timeout-minutes}")
    private long pendingTimeoutMinutes;

    // @Transactional bắt buộc phải có: đây là @Scheduled (thread nền do Spring tự tạo), không phải
    // HTTP request nên không được Open-Session-In-View bảo vệ - thiếu annotation này,
    // invoice.getDetails() (@OneToMany LAZY) trong publishInventoryRestore() sẽ ném
    // LazyInitializationException, giống lý do @Transactional đã thêm ở PaymentEventListener.
    @Transactional
    @Scheduled(fixedRateString = "${order.cleanup-job-interval-ms}")
    public void cleanupExpiredPendingOrders() {
        Date cutoff = new Date(System.currentTimeMillis() - pendingTimeoutMinutes * 60_000L);
        List<Invoice> expiredOrders = invoiceRepository.findByStatusAndOrderDateBefore("PENDING", cutoff);

        for (Invoice invoice : expiredOrders) {
            System.out.println("--> [Order Service] Tự động hủy đơn PENDING quá hạn (" + pendingTimeoutMinutes
                    + " phút): " + invoice.getId());
            orderCancellationService.cancelPendingOrder(invoice);
        }
    }
}
