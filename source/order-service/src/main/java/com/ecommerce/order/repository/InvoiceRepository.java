package com.ecommerce.order.repository;

import com.ecommerce.order.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {
    List<Invoice> findAllByOrderByOrderDateDesc();
    List<Invoice> findAllByMemberIdOrderByOrderDateDesc(String memberId);
}