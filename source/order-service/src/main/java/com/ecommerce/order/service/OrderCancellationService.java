package com.ecommerce.order.service;

import com.ecommerce.order.entity.Invoice;
import com.ecommerce.order.publisher.InventoryEventPublisher;
import com.ecommerce.order.repository.InvoiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

// Logic huy 1 don dang PENDING - dung chung cho ca API huy thu cong (OrderController.cancelOrder,
// goi tu Dashboard/Client) lan job tu dong huy don qua han (PendingOrderCleanupJob). Tach ra rieng
// de tranh lap lai cung 1 doan code (set status + publish inventory.restore) o 2 noi.
@Service
public class OrderCancellationService {

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private InventoryEventPublisher inventoryEventPublisher;

    // Chuyen 1 Invoice dang PENDING sang CANCELLED va hoan lai kho da tru truoc do (compensating
    // transaction). KHONG tu kiem tra lai status o day - noi goi phai tu dam bao invoice dang PENDING
    // truoc khi goi ham nay (OrderController da check qua HTTP 400, PendingOrderCleanupJob da loc
    // qua cau truy van findByStatusAndOrderDateBefore).
    public void cancelPendingOrder(Invoice invoice) {
        invoice.setStatus("CANCELLED");
        invoiceRepository.save(invoice);
        inventoryEventPublisher.publishInventoryRestore(invoice);
    }
}
