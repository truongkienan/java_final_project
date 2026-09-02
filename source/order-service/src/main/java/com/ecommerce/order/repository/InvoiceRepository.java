package com.ecommerce.order.repository;

import com.ecommerce.order.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {
    List<Invoice> findAllByOrderByOrderDateDesc();
    List<Invoice> findAllByMemberIdOrderByOrderDateDesc(String memberId);

    // Dung cho PendingOrderCleanupJob: tim cac don con PENDING nhung da dat hang qua lau (khach
    // bo ngang khong hoan tat thanh toan tren PayPal) de tu dong huy + hoan kho.
    List<Invoice> findByStatusAndOrderDateBefore(String status, Date cutoff);
}